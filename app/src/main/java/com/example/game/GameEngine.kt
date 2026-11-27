package com.example.game

import com.example.combat.CombatSystem
import com.example.core.BeatManager
import com.example.core.EquippedSlotInfo
import com.example.core.GamePhase
import com.example.core.HudSnapshot
import com.example.core.PoolManager
import com.example.core.StemLevels
import com.example.core.UpgradeCardOption
import com.example.data.ArenaMapId
import com.example.data.EnemyArchetype
import com.example.data.PlayerProfile
import com.example.data.StageCatalog
import com.example.data.StageDefinition
import com.example.data.WaveSchedule
import com.example.enemies.WaveSpawner
import com.example.player.PlayerState

/**
 * Thread-safe core real-time simulation orchestrator for SYNVEX.
 * Synchronizes simulation state via [simLock] so the dedicated GPU SurfaceView render thread
 * runs smoothly at 60 FPS decoupled from Compose UI updates, while live auto-saving progress!
 */
class GameEngine(
    val beatManager: BeatManager,
    private val onSaveProgressDelta: (
        deltaCoins: Int,
        deltaCrystals: Int,
        deltaKills: Int,
        deltaMissions: Int,
        survivalSeconds: Int,
        runKills: Int,
        maxCombo: Int,
        stageNumber: Int,
        victory: Boolean,
        isRunFinished: Boolean
    ) -> Unit
) {
    val simLock = Any()

    val pool = PoolManager()
    val player = PlayerState()

    @Volatile
    var phase: GamePhase = GamePhase.MENU
        private set

    var elapsedSeconds: Float = 0f
        private set

    var cameraX: Float = 0f
        private set
    var cameraY: Float = 0f
        private set

    var screenShakeMagnitude: Float = 0f
        private set

    var viewportHalfWidth: Float = 360f
    var viewportHalfHeight: Float = 720f

    // Real-time measured FPS (0.5s sliding window)
    @Volatile
    var currentFps: Int = 60
        private set
    private var fpsWindowStartNanos: Long = 0L
    private var fpsFrameCount: Int = 0

    @Volatile
    var profile: PlayerProfile = PlayerProfile()
        private set

    private var cachedWeapons: List<EquippedSlotInfo> = emptyList()
    private var cachedPassives: List<EquippedSlotInfo> = emptyList()
    private var currentUpgradeOptions: List<UpgradeCardOption> = emptyList()

    private var eliteAnnouncementText: String? = null
    private var eliteAnnouncementTimer: Float = 0f

    private var levelUpToastText: String? = null
    private var levelUpToastTimer: Float = 0f

    @Volatile
    var currentStage: StageDefinition = StageCatalog.forNumber(1)
        private set

    private var defeatReasonText: String? = null

    // Incremental auto-save ledger so mid-run checkpoints never double-count or lose progress!
    private var savedRunCoins: Int = 0
    private var savedRunCrystals: Int = 0
    private var savedRunKills: Int = 0
    private var savedRunMissions: Int = 0

    val missionSystem: ArenaMissionSystem = ArenaMissionSystem(
        pool = pool,
        beatManager = beatManager,
        onPlayerHazardHit = { dmg, cause ->
            val bypass = cause.startsWith("GÖREV") || cause.startsWith("BOSS SÜRESİ")
            if (player.takeDirectHazardDamage(dmg, bypassIFrames = bypass)) {
                beatManager.triggerDamageSfx()
                screenShakeMagnitude = (screenShakeMagnitude + 7f).coerceAtMost(11f)
                beatManager.updateComboStems(player.combo)
                if (player.hp <= 0f) {
                    defeatReasonText = cause
                    finishRun(victory = false)
                }
            }
        },
        onMissionReward = { coins, crystals, xp, heal ->
            player.addCoins(coins)
            player.addCrystals(crystals)
            player.heal(heal)
            player.addNovaCharge(20f)
            val gainedLevels = player.addXp(xp)
            if (gainedLevels > 0) {
                beatManager.triggerLevelUpSfx()
                levelUpToastText = "SEVİYE ${player.level}! (+${player.unspentSkillPoints} PUAN)"
                levelUpToastTimer = 2.6f
            }
            // Instant live auto-save checkpoint on every completed mission!
            flushAutoSaveLocked(victory = false, isRunFinished = false)
        },
        onStageCleared = {
            finishRun(victory = true)
        },
        onScreenShake = { impulse ->
            screenShakeMagnitude = (screenShakeMagnitude + impulse).coerceAtMost(11f)
        }
    )

    val combatSystem: CombatSystem = CombatSystem(
        pool = pool,
        beatManager = beatManager,
        onEnemyKilled = { enemy ->
            val isElite = enemy.archetype == EnemyArchetype.ELITE_BOSS
            player.registerKill(isElite = isElite)
            waveSpawner.spawnRhythmOrb(enemy)
            missionSystem.onEnemyKilled(enemy)
            beatManager.updateComboStems(player.combo)
        },
        onScreenShake = { impulse ->
            screenShakeMagnitude = (screenShakeMagnitude + impulse).coerceAtMost(9f)
        }
    )

    val waveSpawner: WaveSpawner = WaveSpawner(
        pool = pool,
        beatManager = beatManager,
        onPlayerDamaged = { _, cause ->
            screenShakeMagnitude = 8f
            beatManager.updateComboStems(player.combo)
            if (player.hp <= 0f) {
                defeatReasonText = cause
                finishRun(victory = false)
            }
        },
        onXpCollected = { xp ->
            val gainedLevels = player.addXp(xp)
            if (gainedLevels > 0) {
                beatManager.triggerLevelUpSfx()
                levelUpToastText = "SEVİYE ${player.level}! (+${player.unspentSkillPoints} PUAN)"
                levelUpToastTimer = 2.6f
            }
        },
        onCoinCollected = { coinValue ->
            player.addCoins(coinValue)
        },
        onEliteSpawned = { banner ->
            eliteAnnouncementText = banner
            eliteAnnouncementTimer = 3.2f
            screenShakeMagnitude = 7f
        },
        onEnemyShootSingle = { fx, fy, tx, ty, spd, dmg, fromBoss ->
            missionSystem.fireEnemyPlasmaBolt(fx, fy, tx, ty, spd, dmg, fromBoss)
        },
        onBossShootRadial = { bx, by, count, spd, dmg, offset ->
            missionSystem.fireBossRadialVolley(bx, by, count, spd, dmg, offset)
        }
    )

    init {
        beatManager.setOnBeatListener { beatIdx ->
            if (phase == GamePhase.PLAYING) {
                combatSystem.onBeat(beatIdx, player)
            }
        }
    }

    fun updateProfile(newProfile: PlayerProfile) {
        profile = newProfile
    }

    private fun refreshLoadoutCache() {
        cachedWeapons = player.buildEquippedWeaponsList()
        cachedPassives = player.buildEquippedPassivesList()
    }

    fun openShop() = synchronized(simLock) {
        if (phase == GamePhase.MENU) {
            phase = GamePhase.SHOP
        }
    }

    fun closeShop() = synchronized(simLock) {
        if (phase == GamePhase.SHOP) {
            phase = GamePhase.MENU
        }
    }

    fun startNewRun(stageNumber: Int = profile.selectedStage) = synchronized(simLock) {
        currentStage = StageCatalog.forNumber(stageNumber)
        ArenaBounds.activeMap = currentStage.mapId
        pool.releaseAll()
        player.reset(profile)
        if (currentStage.mapId == ArenaMapId.PLASMA_VOLCANO) {
            // Spawn cleanly below the center volcanic reactor pillar
            player.x = 0f
            player.y = 180f
        }
        waveSpawner.reset()
        missionSystem.reset(currentStage)
        refreshLoadoutCache()
        elapsedSeconds = 0f
        cameraX = player.x
        cameraY = player.y
        screenShakeMagnitude = 0f
        currentUpgradeOptions = emptyList()
        eliteAnnouncementText = null
        eliteAnnouncementTimer = 0f
        levelUpToastText = null
        levelUpToastTimer = 0f
        defeatReasonText = null
        savedRunCoins = 0
        savedRunCrystals = 0
        savedRunKills = 0
        savedRunMissions = 0
        fpsWindowStartNanos = System.nanoTime()
        fpsFrameCount = 0
        currentFps = 60
        phase = GamePhase.PLAYING
        beatManager.start()
    }

    fun triggerDash() = synchronized(simLock) {
        if (phase != GamePhase.PLAYING) return
        if (player.triggerDash()) {
            combatSystem.triggerDashBurstVfx(player)
        }
    }

    fun triggerOverdriveNova() = synchronized(simLock) {
        if (phase != GamePhase.PLAYING) return
        if (player.consumeNova()) {
            combatSystem.triggerOverdriveNova(player)
        }
    }

    fun openUpgradeMenu() = synchronized(simLock) {
        if (phase != GamePhase.PLAYING) return
        phase = GamePhase.LEVEL_UP
        beatManager.pause()
        player.joystick.onUp()
        currentUpgradeOptions = player.buildAllUpgradeOptions()
    }

    fun closeUpgradeMenu() = synchronized(simLock) {
        if (phase != GamePhase.LEVEL_UP) return
        currentUpgradeOptions = emptyList()
        phase = GamePhase.PLAYING
        beatManager.resume()
    }

    fun selectUpgrade(option: UpgradeCardOption) = synchronized(simLock) {
        if (phase != GamePhase.LEVEL_UP) return
        if (player.unspentSkillPoints <= 0 || option.isMaxed) return

        val wasEvolution = player.applyUpgrade(option)
        player.unspentSkillPoints = (player.unspentSkillPoints - 1).coerceAtLeast(0)
        refreshLoadoutCache()

        if (wasEvolution) {
            beatManager.triggerEvolutionSfx()
            screenShakeMagnitude = 8f
        } else {
            beatManager.triggerOrbSfx()
        }

        if (player.unspentSkillPoints > 0) {
            currentUpgradeOptions = player.buildAllUpgradeOptions()
        } else {
            currentUpgradeOptions = emptyList()
            phase = GamePhase.PLAYING
            beatManager.resume()
        }
    }

    fun pauseGame() = synchronized(simLock) {
        if (phase == GamePhase.PLAYING) {
            phase = GamePhase.PAUSED
            beatManager.pause()
            player.joystick.onUp()
            // Auto-save earned progress whenever the player pauses or backgrounds the app!
            flushAutoSaveLocked(victory = false, isRunFinished = false)
        }
    }

    fun resumeGame() = synchronized(simLock) {
        if (phase == GamePhase.PAUSED) {
            phase = GamePhase.PLAYING
            beatManager.resume()
        }
    }

    fun returnToMenu() = synchronized(simLock) {
        if (phase == GamePhase.PLAYING || phase == GamePhase.PAUSED || phase == GamePhase.LEVEL_UP) {
            flushAutoSaveLocked(victory = false, isRunFinished = false)
        }
        beatManager.stop()
        pool.releaseAll()
        player.joystick.onUp()
        phase = GamePhase.MENU
    }

    fun toggleMute() {
        beatManager.setMuted(!beatManager.isMuted)
    }

    private fun flushAutoSaveLocked(victory: Boolean, isRunFinished: Boolean) {
        val dCoins = (player.runCoins - savedRunCoins).coerceAtLeast(0)
        val dCrystals = (player.runCrystals - savedRunCrystals).coerceAtLeast(0)
        val dKills = (player.kills - savedRunKills).coerceAtLeast(0)
        val dMissions = (missionSystem.missionsCompleted - savedRunMissions).coerceAtLeast(0)

        savedRunCoins += dCoins
        savedRunCrystals += dCrystals
        savedRunKills += dKills
        savedRunMissions += dMissions

        if (dCoins > 0 || dCrystals > 0 || dKills > 0 || dMissions > 0 || victory || isRunFinished) {
            onSaveProgressDelta(
                dCoins,
                dCrystals,
                dKills,
                dMissions,
                elapsedSeconds.toInt(),
                player.kills,
                player.maxCombo,
                currentStage.stageNumber,
                victory,
                isRunFinished
            )
        }
    }

    private fun finishRun(victory: Boolean) {
        if (phase == GamePhase.GAME_OVER || phase == GamePhase.VICTORY) return
        phase = if (victory) GamePhase.VICTORY else GamePhase.GAME_OVER
        beatManager.pause()
        player.joystick.onUp()

        val survivalCoinBonus = (elapsedSeconds / 3f).toInt() + player.kills / 2 + missionSystem.missionsCompleted * 15
        val victoryCoins = if (victory) currentStage.rewardCoins else 18
        val victoryCrystals = if (victory) currentStage.rewardCrystals else 0
        player.addCoins(survivalCoinBonus + victoryCoins)
        val comboCrystalBonus = if (player.maxCombo >= 25) 10 else if (player.maxCombo >= 10) 5 else 2
        player.addCrystals(comboCrystalBonus + victoryCrystals)

        flushAutoSaveLocked(victory = victory, isRunFinished = true)
    }

    fun stepFrameLocked(dtSeconds: Float, nowNanos: Long) {
        val dt = dtSeconds.coerceIn(0.0005f, 0.05f)
        if (phase != GamePhase.PLAYING) {
            return
        }

        if (fpsWindowStartNanos == 0L) fpsWindowStartNanos = nowNanos
        fpsFrameCount++
        val windowElapsed = nowNanos - fpsWindowStartNanos
        if (windowElapsed >= 500_000_000L) {
            currentFps = ((fpsFrameCount * 1_000_000_000L) / windowElapsed).toInt().coerceIn(1, 240)
            fpsFrameCount = 0
            fpsWindowStartNanos = nowNanos
        }

        beatManager.updateClock(nowNanos)

        elapsedSeconds += dt
        if (elapsedSeconds >= WaveSchedule.VICTORY_DURATION_SECONDS) {
            elapsedSeconds = WaveSchedule.VICTORY_DURATION_SECONDS
            finishRun(victory = true)
            return
        }

        player.update(dt, beatManager.currentBeatProgress)

        val hitElectricWall = ArenaBounds.clampPlayer(player)
        if (hitElectricWall && player.wallShockCooldown <= 0f) {
            player.wallShockCooldown = 0.65f
            missionSystem.triggerWallShockFlash()
            screenShakeMagnitude = (screenShakeMagnitude + 6f).coerceAtMost(10f)
            if (player.x > ArenaBounds.HALF_WIDTH - player.radius - 6f) player.x -= ArenaBounds.WALL_PUSH_INWARD
            else if (player.x < -ArenaBounds.HALF_WIDTH + player.radius + 6f) player.x += ArenaBounds.WALL_PUSH_INWARD
            if (player.y > ArenaBounds.HALF_HEIGHT - player.radius - 6f) player.y -= ArenaBounds.WALL_PUSH_INWARD
            else if (player.y < -ArenaBounds.HALF_HEIGHT + player.radius + 6f) player.y += ArenaBounds.WALL_PUSH_INWARD

            if (player.takeDirectHazardDamage(ArenaBounds.WALL_SHOCK_DAMAGE, bypassIFrames = false)) {
                beatManager.triggerDamageSfx()
                beatManager.updateComboStems(player.combo)
                if (player.hp <= 0f) {
                    defeatReasonText = "ELEKTRİKLİ ARENA DUVARI"
                    finishRun(victory = false)
                    return
                }
            }
        }

        beatManager.updateComboStems(player.combo)

        val maxCamX = (ArenaBounds.HALF_WIDTH - viewportHalfWidth * 0.72f).coerceAtLeast(120f)
        val maxCamY = (ArenaBounds.HALF_HEIGHT - viewportHalfHeight * 0.72f).coerceAtLeast(120f)
        val targetCamX = player.x.coerceIn(-maxCamX, maxCamX)
        val targetCamY = player.y.coerceIn(-maxCamY, maxCamY)
        val camLerp = (10f * dt).coerceAtMost(1f)
        cameraX += (targetCamX - cameraX) * camLerp
        cameraY += (targetCamY - cameraY) * camLerp

        waveSpawner.update(
            dt = dt,
            elapsedSeconds = elapsedSeconds,
            viewportHalfWidth = viewportHalfWidth,
            viewportHalfHeight = viewportHalfHeight,
            player = player,
            stageHpMult = currentStage.enemyHpMultiplier,
            stageSpeedMult = currentStage.enemySpeedMultiplier
        )
        if (phase != GamePhase.PLAYING) return

        missionSystem.update(dt, elapsedSeconds, waveSpawner.currentWaveNumber, player)
        if (phase != GamePhase.PLAYING) return

        combatSystem.update(dt, player)
        pool.compactAll()

        if (screenShakeMagnitude > 0.05f) {
            screenShakeMagnitude *= 0.86f
        } else {
            screenShakeMagnitude = 0f
        }
        if (eliteAnnouncementTimer > 0f) {
            eliteAnnouncementTimer -= dt
            if (eliteAnnouncementTimer <= 0f) eliteAnnouncementText = null
        }
        if (levelUpToastTimer > 0f) {
            levelUpToastTimer -= dt
            if (levelUpToastTimer <= 0f) levelUpToastText = null
        }
    }

    fun snapshot(): HudSnapshot = synchronized(simLock) {
        snapshotLocked()
    }

    fun snapshotLocked(): HudSnapshot {
        val unlockedMap = if (phase == GamePhase.VICTORY) {
            StageCatalog.newlyUnlockedMapAfterStage(currentStage.stageNumber)?.title
        } else {
            null
        }
        return HudSnapshot(
            phase = phase,
            elapsedSeconds = elapsedSeconds,
            mapNumber = currentStage.mapId.mapNumber,
            mapTitle = currentStage.mapId.title,
            unlockedNewMapTitle = unlockedMap,
            stageNumber = currentStage.stageNumber,
            stageTitle = currentStage.title,
            stageTotalMissions = currentStage.missionSequence.size,
            hasNextStage = currentStage.stageNumber < StageCatalog.stages.size,
            waveNumber = waveSpawner.currentWaveNumber,
            level = player.level,
            xp = player.xp,
            xpToNext = player.xpToNext,
            unspentSkillPoints = player.unspentSkillPoints,
            hp = player.hp,
            maxHp = player.maxHp,
            kills = player.kills,
            combo = player.combo,
            maxCombo = player.maxCombo,
            runCoins = player.runCoins,
            runCrystals = player.runCrystals,
            dashReadyFraction = player.dashReadyFraction,
            isDashing = player.isDashing,
            novaChargeFraction = player.novaChargeFraction,
            isNovaReady = player.isNovaReady,
            bpm = beatManager.bpm,
            beatProgress = beatManager.currentBeatProgress,
            beatIndex = beatManager.beatIndex.coerceAtLeast(0),
            fps = currentFps,
            isMuted = beatManager.isMuted,
            stems = StemLevels(
                drums = beatManager.drumLevel,
                bass = beatManager.bassLevel,
                melody = beatManager.melodyLevel,
                overdrive = beatManager.overdriveLevel
            ),
            equippedWeapons = cachedWeapons,
            equippedPassives = cachedPassives,
            upgradeOptions = currentUpgradeOptions,
            evolvedCount = player.evolvedWeapons.size,
            profile = profile,
            showJoystickHint = !player.joystick.hasEverMoved && elapsedSeconds < 6f,
            eliteAnnouncement = eliteAnnouncementText,
            levelUpToast = levelUpToastText,
            activeMission = missionSystem.buildSnapshot(),
            missionsCompleted = missionSystem.missionsCompleted,
            missionBanner = missionSystem.bannerText,
            missionBannerPositive = missionSystem.bannerPositive,
            defeatReason = defeatReasonText
        )
    }
}
