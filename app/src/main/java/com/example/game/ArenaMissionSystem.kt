package com.example.game

import com.example.core.BeatManager
import com.example.core.EnemyEntity
import com.example.core.MissionSnapshot
import com.example.core.MissionType
import com.example.core.PoolManager
import com.example.data.ArenaMapId
import com.example.data.EnemyArchetype
import com.example.data.StageCatalog
import com.example.data.StageDefinition
import com.example.player.PlayerState
import com.example.ui.theme.AcidLime
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.HotMagenta
import com.example.ui.theme.SolarGold
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

data class ArenaPillar(
    val x: Float,
    val y: Float,
    val radius: Float
)

/**
 * Enclosed neon cyber-arena boundaries and tactical obstacle pillars.
 * Prevents endless kiting/wandering and creates real spatial tension!
 */
object ArenaBounds {
    const val HALF_WIDTH: Float = 520f
    const val HALF_HEIGHT: Float = 720f
    const val WALL_SHOCK_DAMAGE: Float = 18f
    const val WALL_PUSH_INWARD: Float = 16f

    @Volatile
    var activeMap: ArenaMapId = ArenaMapId.CYBER_CITY

    private val cyberPillars: Array<ArenaPillar> = arrayOf(
        ArenaPillar(-235f, -310f, 32f),
        ArenaPillar(235f, -310f, 32f),
        ArenaPillar(-235f, 310f, 32f),
        ArenaPillar(235f, 310f, 32f)
    )

    private val volcanoPillars: Array<ArenaPillar> = arrayOf(
        ArenaPillar(0f, 0f, 38f),
        ArenaPillar(-265f, -345f, 32f),
        ArenaPillar(265f, -345f, 32f),
        ArenaPillar(-265f, 345f, 32f),
        ArenaPillar(265f, 345f, 32f)
    )

    private val cosmicPillars: Array<ArenaPillar> = arrayOf(
        ArenaPillar(-260f, -370f, 30f),
        ArenaPillar(260f, -370f, 30f),
        ArenaPillar(-285f, 0f, 30f),
        ArenaPillar(285f, 0f, 30f),
        ArenaPillar(-260f, 370f, 30f),
        ArenaPillar(260f, 370f, 30f)
    )

    private val frostPillars: Array<ArenaPillar> = arrayOf(
        ArenaPillar(0f, -420f, 32f),
        ArenaPillar(0f, 420f, 32f),
        ArenaPillar(-260f, -210f, 30f),
        ArenaPillar(260f, -210f, 30f),
        ArenaPillar(-260f, 210f, 30f),
        ArenaPillar(260f, 210f, 30f)
    )

    private val toxicPillars: Array<ArenaPillar> = arrayOf(
        ArenaPillar(-230f, -360f, 33f),
        ArenaPillar(230f, -180f, 33f),
        ArenaPillar(-230f, 0f, 33f),
        ArenaPillar(230f, 180f, 33f),
        ArenaPillar(-230f, 360f, 33f)
    )

    private val solarPillars: Array<ArenaPillar> = arrayOf(
        ArenaPillar(0f, -445f, 34f),
        ArenaPillar(0f, 445f, 34f),
        ArenaPillar(-205f, -190f, 30f),
        ArenaPillar(205f, -190f, 30f),
        ArenaPillar(-205f, 190f, 30f),
        ArenaPillar(205f, 190f, 30f)
    )

    val activePillars: Array<ArenaPillar>
        get() = when (activeMap) {
            ArenaMapId.CYBER_CITY -> cyberPillars
            ArenaMapId.PLASMA_VOLCANO -> volcanoPillars
            ArenaMapId.COSMIC_VOID -> cosmicPillars
            ArenaMapId.FROST_CORE -> frostPillars
            ArenaMapId.TOXIC_LAB -> toxicPillars
            ArenaMapId.SOLAR_EMPIRE -> solarPillars
        }

    val pillars: Array<ArenaPillar>
        get() = activePillars

    /**
     * Clamps player strictly inside the electrified arena walls and pushes out of neon pillars.
     * Returns true if the player collided with the electrified outer barrier wall!
     */
    fun clampPlayer(player: PlayerState): Boolean {
        val minX = -HALF_WIDTH + player.radius
        val maxX = HALF_WIDTH - player.radius
        val minY = -HALF_HEIGHT + player.radius
        val maxY = HALF_HEIGHT - player.radius

        var hitWall = false
        if (player.x < minX) {
            player.x = minX + 1.5f
            hitWall = true
        } else if (player.x > maxX) {
            player.x = maxX - 1.5f
            hitWall = true
        }
        if (player.y < minY) {
            player.y = minY + 1.5f
            hitWall = true
        } else if (player.y > maxY) {
            player.y = maxY - 1.5f
            hitWall = true
        }

        // Resolve collisions with the active map's tactical pillars
        val currentPillars = activePillars
        for (i in currentPillars.indices) {
            val p = currentPillars[i]
            val dx = player.x - p.x
            val dy = player.y - p.y
            val minDist = player.radius + p.radius
            val dSq = dx * dx + dy * dy
            if (dSq < minDist * minDist) {
                val dist = hypot(dx, dy).coerceAtLeast(0.01f)
                val nx = dx / dist
                val ny = dy / dist
                player.x = p.x + nx * minDist
                player.y = p.y + ny * minDist
            }
        }
        return hitWall
    }

    /**
     * Keeps enemies inside the arena walls and smoothly slides them around pillars.
     */
    fun clampEnemy(enemy: EnemyEntity) {
        val margin = enemy.radius + 4f
        if (enemy.x < -HALF_WIDTH + margin) enemy.x = -HALF_WIDTH + margin
        else if (enemy.x > HALF_WIDTH - margin) enemy.x = HALF_WIDTH - margin

        if (enemy.y < -HALF_HEIGHT + margin) enemy.y = -HALF_HEIGHT + margin
        else if (enemy.y > HALF_HEIGHT - margin) enemy.y = HALF_HEIGHT - margin

        if (enemy.archetype == EnemyArchetype.CORRUPTED_CORE) return

        val currentPillars = activePillars
        for (i in currentPillars.indices) {
            val p = currentPillars[i]
            val dx = enemy.x - p.x
            val dy = enemy.y - p.y
            val minDist = enemy.radius + p.radius
            val dSq = dx * dx + dy * dy
            if (dSq < minDist * minDist) {
                val dist = hypot(dx, dy).coerceAtLeast(0.01f)
                enemy.x = p.x + (dx / dist) * minDist
                enemy.y = p.y + (dy / dist) * minDist
            }
        }
    }

    fun isInsidePillar(x: Float, y: Float, pad: Float = 0f): Boolean {
        val currentPillars = activePillars
        for (i in currentPillars.indices) {
            val p = currentPillars[i]
            val dx = x - p.x
            val dy = y - p.y
            val r = p.radius + pad
            if (dx * dx + dy * dy <= r * r) return true
        }
        return false
    }

    fun pickSafeArenaPoint(minDistFromX: Float, minDistFromY: Float, minDist: Float, edgeMargin: Float = 95f): Pair<Float, Float> {
        val limitX = HALF_WIDTH - edgeMargin
        val limitY = HALF_HEIGHT - edgeMargin
        repeat(24) {
            val px = (Random.nextFloat() * 2f - 1f) * limitX
            val py = (Random.nextFloat() * 2f - 1f) * limitY
            if (!isInsidePillar(px, py, 64f)) {
                val dx = px - minDistFromX
                val dy = py - minDistFromY
                if (dx * dx + dy * dy >= minDist * minDist) {
                    return px to py
                }
            }
        }
        return 0f to (if (minDistFromY >= 0f) -360f else 360f)
    }
}

/**
 * Challenging rotating mission & arena-hazard director.
 * Gives every run clear tactical goals (capture beacons, destroy firing cores, survive sweeping
 * laser storms, hunt arena bosses) with real stakes, rewards, and failure penalties!
 */
class ArenaMissionSystem(
    private val pool: PoolManager,
    private val beatManager: BeatManager,
    private val onPlayerHazardHit: (damage: Float, cause: String) -> Unit,
    private val onMissionReward: (coins: Int, crystals: Int, xp: Int, healAmount: Float) -> Unit,
    private val onStageCleared: () -> Unit,
    private val onScreenShake: (Float) -> Unit
) {
    var activeStage: StageDefinition = StageCatalog.forNumber(1)
        private set

    // Active Mission State (read directly by SurfaceView canvas for zero-GC rendering)
    var hasActiveMission: Boolean = false
        private set
    var activeType: MissionType = MissionType.CAPTURE_BEACON
        private set
    var missionNumber: Int = 0
        private set
    var missionsCompleted: Int = 0
        private set

    var timeLeft: Float = 0f
        private set
    var maxTime: Float = 26f
        private set

    // Mission 1: Capture Beacon fields
    var captureX: Float = 0f
        private set
    var captureY: Float = 0f
        private set
    var captureRadius: Float = 86f
        private set
    var captureHoldSeconds: Float = 0f
        private set
    var captureTargetSeconds: Float = 5.5f
        private set
    var isPlayerInBeacon: Boolean = false
        private set

    // Mission 2: Corrupted Cores fields
    var coresTotal: Int = 3
        private set
    var coresDestroyed: Int = 0
        private set

    // Mission 3: Laser Storm fields
    private var stormLaserTimer: Float = 0f
    private var stormHazardTimer: Float = 0f

    // Mission 4: Boss Hunt fields
    var bossMaxHp: Float = 1f
        private set
    var bossCurrentHp: Float = 0f
        private set
    private var bossHazardTimer: Float = 3.2f
    private var beaconRushTimer: Float = 3.5f

    // Live Directional Target Marker for Canvas compass arrow
    var hasTargetMarker: Boolean = false
        private set
    var targetMarkerX: Float = 0f
        private set
    var targetMarkerY: Float = 0f
        private set

    // Ambient hazard spawner between/during waves
    private var ambientHazardTimer: Float = 4.2f
    private var ambientLaserTimer: Float = 16f
    private var nextMissionDelay: Float = 2.0f

    // Banner state (shown on HUD for mission complete / fail)
    var bannerText: String? = null
        private set
    var bannerPositive: Boolean = true
        private set
    private var bannerTimer: Float = 0f

    // Wall shock visual flash timer
    var wallShockFlash: Float = 0f
        private set

    fun reset(stage: StageDefinition = StageCatalog.forNumber(1)) {
        activeStage = stage
        ArenaBounds.activeMap = stage.mapId
        hasActiveMission = false
        missionNumber = 0
        missionsCompleted = 0
        timeLeft = 0f
        maxTime = 26f
        captureHoldSeconds = 0f
        isPlayerInBeacon = false
        coresDestroyed = 0
        hasTargetMarker = false
        ambientHazardTimer = 4.2f
        ambientLaserTimer = (16f - stage.stageNumber * 1.1f).coerceAtLeast(7.5f)
        nextMissionDelay = 1.8f
        bannerText = "HARİTA ${stage.mapId.mapNumber} • BÖLÜM ${stage.stageNumber}: ${stage.title.uppercase()}"
        bannerPositive = true
        bannerTimer = 2.6f
        wallShockFlash = 0f
    }

    fun triggerWallShockFlash() {
        wallShockFlash = 0.35f
    }

    fun update(dt: Float, elapsedSeconds: Float, waveNumber: Int, player: PlayerState) {
        if (wallShockFlash > 0f) {
            wallShockFlash = (wallShockFlash - dt).coerceAtLeast(0f)
        }
        if (bannerTimer > 0f) {
            bannerTimer -= dt
            if (bannerTimer <= 0f) bannerText = null
        }

        // 1. Start next mission after brief breather
        if (!hasActiveMission) {
            nextMissionDelay -= dt
            if (nextMissionDelay <= 0f) {
                startNextMission(waveNumber, player)
            }
        } else {
            updateActiveMission(dt, waveNumber, player)
        }

        // 2. Ambient red orbital bass-drop strike zones (keeps movement intense!)
        ambientHazardTimer -= dt
        if (ambientHazardTimer <= 0f) {
            val baseCd = (3.6f - waveNumber * 0.42f - activeStage.stageNumber * 0.14f).coerceAtLeast(1.35f)
            ambientHazardTimer = baseCd
            val count = if (waveNumber >= 3 || activeStage.stageNumber >= 3) 2 else 1
            for (k in 0 until count) {
                spawnHazardNearPlayer(
                    player,
                    spread = if (k == 0) 80f else 175f,
                    damage = 26f + waveNumber * 3.5f + activeStage.stageNumber * 2f
                )
            }
        }

        // 3. Ambient sweeping laser beams (Wave 1+ on Stage 2+, Wave 2+ on Stage 1)
        if ((waveNumber >= 2 || activeStage.stageNumber >= 2) && (!hasActiveMission || activeType != MissionType.SURVIVE_LASER_STORM)) {
            ambientLaserTimer -= dt
            if (ambientLaserTimer <= 0f) {
                ambientLaserTimer = (11.5f - waveNumber * 1.3f - activeStage.stageNumber * 0.5f).coerceAtLeast(4.8f)
                spawnLaserTargetingPlayer(player, damage = 28f + waveNumber * 3f + activeStage.stageNumber * 2f)
            }
        }

        // 4. Update all pooled hazards, lasers, and enemy plasma bullets
        updateHazardZones(dt, player)
        updateLaserBeams(dt, player)
        updateEnemyBullets(dt, player)
    }

    private fun startNextMission(waveNumber: Int, player: PlayerState) {
        val sequence = activeStage.missionSequence
        val nextIndex = missionsCompleted.coerceIn(0, sequence.lastIndex)
        missionNumber = nextIndex + 1
        activeType = sequence[nextIndex]
        hasActiveMission = true
        hasTargetMarker = false

        when (activeType) {
            MissionType.CAPTURE_BEACON -> {
                maxTime = 34f
                timeLeft = maxTime
                captureTargetSeconds = 7.5f + activeStage.stageNumber * 0.65f
                captureHoldSeconds = 0f
                captureRadius = 82f
                isPlayerInBeacon = false
                beaconRushTimer = 2.8f
                val (bx, by) = ArenaBounds.pickSafeArenaPoint(player.x, player.y, minDist = 280f, edgeMargin = 135f)
                captureX = bx
                captureY = by
                hasTargetMarker = true
                targetMarkerX = bx
                targetMarkerY = by
                showBanner("GÖREV $missionNumber/${sequence.size}: FREKANS KULESİNİ ELE GEÇİR!", positive = true)
            }

            MissionType.DESTROY_CORES -> {
                maxTime = 44f
                timeLeft = maxTime
                coresTotal = when {
                    activeStage.stageNumber >= 6 -> 6
                    activeStage.stageNumber >= 3 -> 5
                    else -> 4
                }
                coresDestroyed = 0
                val hpScale = activeStage.enemyHpMultiplier * (1f + (waveNumber - 1) * 0.28f)
                val cornerSpots = arrayOf(
                    -370f to -520f,
                    370f to -520f,
                    0f to -540f,
                    0f to 540f,
                    -370f to 500f,
                    370f to 500f,
                    -390f to 0f,
                    390f to 0f
                )
                cornerSpots.shuffle()
                var spawned = 0
                for (spot in cornerSpots) {
                    if (spawned >= coresTotal) break
                    val core = pool.enemies.acquire() ?: break
                    core.archetype = EnemyArchetype.CORRUPTED_CORE
                    core.x = spot.first
                    core.y = spot.second
                    core.maxHp = EnemyArchetype.CORRUPTED_CORE.baseHp * hpScale
                    core.hp = core.maxHp
                    core.speed = 0f
                    core.contactDamage = EnemyArchetype.CORRUPTED_CORE.contactDamage
                    core.radius = EnemyArchetype.CORRUPTED_CORE.radius
                    core.xpValue = EnemyArchetype.CORRUPTED_CORE.xpValue
                    core.shootCooldown = 1.1f + spawned * 0.45f
                    core.isMissionTarget = true
                    spawned++
                }
                coresTotal = spawned.coerceAtLeast(1)
                updateNearestTargetMarker(EnemyArchetype.CORRUPTED_CORE, player)
                showBanner("GÖREV $missionNumber/${sequence.size}: $coresTotal BOZUK ÇEKİRDEĞİ İMHA ET!", positive = true)
            }

            MissionType.SURVIVE_LASER_STORM -> {
                maxTime = 22f + activeStage.stageNumber * 1.5f
                timeLeft = maxTime
                stormLaserTimer = 0.65f
                stormHazardTimer = 1.0f
                hasTargetMarker = false
                onScreenShake(6.5f)
                showBanner("GÖREV $missionNumber/${sequence.size}: LAZER FIRTINASINDAN SAĞ ÇIK!", positive = false)
            }

            MissionType.HUNT_BOSS -> {
                maxTime = 62f
                timeLeft = maxTime
                bossHazardTimer = 3.0f
                val hpScale = activeStage.enemyHpMultiplier * (1f + (waveNumber - 1) * 0.30f)
                val (bx, by) = ArenaBounds.pickSafeArenaPoint(player.x, player.y, minDist = 300f, edgeMargin = 110f)
                val boss = pool.enemies.acquire()
                if (boss != null) {
                    boss.archetype = EnemyArchetype.ELITE_BOSS
                    boss.x = bx
                    boss.y = by
                    boss.maxHp = EnemyArchetype.ELITE_BOSS.baseHp * hpScale
                    boss.hp = boss.maxHp
                    boss.speed = EnemyArchetype.ELITE_BOSS.baseSpeed * activeStage.enemySpeedMultiplier
                    boss.contactDamage = EnemyArchetype.ELITE_BOSS.contactDamage
                    boss.radius = EnemyArchetype.ELITE_BOSS.radius
                    boss.xpValue = EnemyArchetype.ELITE_BOSS.xpValue
                    boss.shootCooldown = 1.6f
                    boss.isMissionTarget = true
                    bossMaxHp = boss.maxHp
                    bossCurrentHp = boss.hp
                    hasTargetMarker = true
                    targetMarkerX = bx
                    targetMarkerY = by
                }
                onScreenShake(8.5f)
                showBanner("BÖLÜM ${activeStage.stageNumber} BOSS'U: RİTİM MUHAFIZINI YEN!", positive = false)
            }
        }
    }

    private fun updateActiveMission(dt: Float, waveNumber: Int, player: PlayerState) {
        timeLeft -= dt

        when (activeType) {
            MissionType.CAPTURE_BEACON -> {
                val dx = player.x - captureX
                val dy = player.y - captureY
                val inside = (dx * dx + dy * dy) <= (captureRadius * captureRadius)
                isPlayerInBeacon = inside
                hasTargetMarker = !inside
                targetMarkerX = captureX
                targetMarkerY = captureY

                if (inside) {
                    captureHoldSeconds += dt
                    beaconRushTimer -= dt
                    if (beaconRushTimer <= 0f) {
                        beaconRushTimer = 3.2f
                        spawnBeaconDefenders(player, waveNumber)
                    }
                    if (captureHoldSeconds >= captureTargetSeconds) {
                        completeMission(
                            player = player,
                            coins = 50 + missionNumber * 8,
                            crystals = 6,
                            xp = 95,
                            heal = 12f,
                            empBlast = true
                        )
                        return
                    }
                } else if (captureHoldSeconds > 0f) {
                    captureHoldSeconds = (captureHoldSeconds - dt * 0.45f).coerceAtLeast(0f)
                }

                if (timeLeft <= 0f) {
                    failMission(player, penaltyDamage = 38f, reason = "GÖREV BAŞARISIZ: FREKANS PATLAMASI", waveNumber = waveNumber)
                }
            }

            MissionType.DESTROY_CORES -> {
                updateNearestTargetMarker(EnemyArchetype.CORRUPTED_CORE, player)
                if (coresDestroyed >= coresTotal) {
                    completeMission(
                        player = player,
                        coins = 65 + missionNumber * 10,
                        crystals = 8,
                        xp = 130,
                        heal = 16f,
                        empBlast = true
                    )
                    return
                }
                if (timeLeft <= 0f) {
                    // Detonate remaining corrupted cores
                    pool.enemies.forEachActive { e ->
                        if (e.archetype == EnemyArchetype.CORRUPTED_CORE) {
                            e.isActive = false
                            spawnDetonationRing(e.x, e.y, 130f, DangerRed)
                        }
                    }
                    failMission(player, penaltyDamage = 40f, reason = "GÖREV BAŞARISIZ: ÇEKİRDEK İNFİLAKI", waveNumber = waveNumber)
                }
            }

            MissionType.SURVIVE_LASER_STORM -> {
                stormLaserTimer -= dt
                if (stormLaserTimer <= 0f) {
                    stormLaserTimer = (1.75f - waveNumber * 0.12f - activeStage.stageNumber * 0.05f).coerceAtLeast(1.05f)
                    spawnLaserTargetingPlayer(player, damage = 30f + waveNumber * 2.5f + activeStage.stageNumber * 2f)
                    // High chance of cross-axis laser for intense dodging
                    if (Random.nextFloat() < 0.72f) {
                        spawnRandomLaser(damage = 28f + waveNumber * 2.5f)
                    }
                }
                stormHazardTimer -= dt
                if (stormHazardTimer <= 0f) {
                    stormHazardTimer = (1.35f - activeStage.stageNumber * 0.06f).coerceAtLeast(0.9f)
                    spawnHazardNearPlayer(player, spread = 120f, damage = 28f + waveNumber * 2.5f)
                }

                if (timeLeft <= 0f) {
                    completeMission(
                        player = player,
                        coins = 60 + missionNumber * 10,
                        crystals = 9,
                        xp = 125,
                        heal = 14f,
                        empBlast = false
                    )
                }
            }

            MissionType.HUNT_BOSS -> {
                var bossAlive = false
                pool.enemies.forEachActive { e ->
                    if (e.archetype == EnemyArchetype.ELITE_BOSS && e.isMissionTarget) {
                        bossAlive = true
                        bossCurrentHp = e.hp
                        bossMaxHp = e.maxHp.coerceAtLeast(1f)
                        hasTargetMarker = true
                        targetMarkerX = e.x
                        targetMarkerY = e.y
                    }
                }
                if (!bossAlive) {
                    completeMission(
                        player = player,
                        coins = 105 + missionNumber * 15,
                        crystals = 18,
                        xp = 210,
                        heal = 22f,
                        empBlast = true
                    )
                    return
                }
                bossHazardTimer -= dt
                if (bossHazardTimer <= 0f) {
                    bossHazardTimer = (3.4f - activeStage.stageNumber * 0.2f).coerceAtLeast(1.9f)
                    spawnHazardNearPlayer(player, spread = 95f, damage = 28f + activeStage.stageNumber * 2.5f)
                    if (activeStage.stageNumber >= 3 && Random.nextFloat() < 0.45f) {
                        spawnLaserTargetingPlayer(player, damage = 30f + activeStage.stageNumber * 2f)
                    }
                }
                if (timeLeft <= 0f) {
                    // Enrage boss + hit player
                    pool.enemies.forEachActive { e ->
                        if (e.archetype == EnemyArchetype.ELITE_BOSS && e.isMissionTarget) {
                            e.speed *= 1.50f
                            e.isMissionTarget = false
                        }
                    }
                    failMission(player, penaltyDamage = 46f, reason = "BOSS SÜRESİ DOLDU: ŞOK DALGASI", waveNumber = waveNumber)
                }
            }
        }
    }

    fun onEnemyKilled(enemy: EnemyEntity) {
        if (!hasActiveMission) return
        if (activeType == MissionType.DESTROY_CORES && enemy.archetype == EnemyArchetype.CORRUPTED_CORE) {
            coresDestroyed++
            spawnDetonationRing(enemy.x, enemy.y, 120f, SolarGold)
            onScreenShake(5f)
        }
    }

    private fun completeMission(
        player: PlayerState,
        coins: Int,
        crystals: Int,
        xp: Int,
        heal: Float,
        empBlast: Boolean
    ) {
        hasActiveMission = false
        hasTargetMarker = false
        isPlayerInBeacon = false
        missionsCompleted++
        nextMissionDelay = 3.2f

        onMissionReward(coins, crystals, xp, heal)
        beatManager.triggerEvolutionSfx()
        onScreenShake(7f)

        if (empBlast) {
            spawnDetonationRing(player.x, player.y, 260f, ElectricCyan)
            pool.overlapEnemiesNonAlloc(player.x, player.y, 260f) { enemy ->
                if (enemy.archetype != EnemyArchetype.ELITE_BOSS) {
                    val dx = enemy.x - player.x
                    val dy = enemy.y - player.y
                    val d = hypot(dx, dy).coerceAtLeast(1f)
                    enemy.takeDamage(55f, (dx / d) * 320f, (dy / d) * 320f)
                }
            }
        }

        if (missionsCompleted >= activeStage.missionSequence.size) {
            showBanner("BÖLÜM ${activeStage.stageNumber} TAMAMLANDI!", positive = true)
            onStageCleared()
        } else {
            showBanner("GÖREV TAMAMLANDI! +$coins COİN  +$crystals KRİSTAL", positive = true)
        }
    }

    private fun failMission(player: PlayerState, penaltyDamage: Float, reason: String, waveNumber: Int) {
        hasActiveMission = false
        hasTargetMarker = false
        isPlayerInBeacon = false
        nextMissionDelay = 4.5f

        spawnDetonationRing(player.x, player.y, 220f, DangerRed)
        onScreenShake(10f)
        showBanner("GÖREV BAŞARISIZ! -${penaltyDamage.toInt()} CAN!", positive = false)
        onPlayerHazardHit(penaltyDamage, reason)

        // Ambush pack on mission fail!
        val ambushCount = 4 + waveNumber
        for (i in 0 until ambushCount) {
            val e = pool.enemies.acquire() ?: break
            val ang = (i.toFloat() / ambushCount) * 2f * PI.toFloat()
            e.archetype = EnemyArchetype.RUNNER
            e.x = (player.x + cos(ang) * 230f).coerceIn(-ArenaBounds.HALF_WIDTH + 30f, ArenaBounds.HALF_WIDTH - 30f)
            e.y = (player.y + sin(ang) * 230f).coerceIn(-ArenaBounds.HALF_HEIGHT + 30f, ArenaBounds.HALF_HEIGHT - 30f)
            e.maxHp = EnemyArchetype.RUNNER.baseHp * (1f + waveNumber * 0.2f)
            e.hp = e.maxHp
            e.speed = EnemyArchetype.RUNNER.baseSpeed * 1.08f
            e.contactDamage = EnemyArchetype.RUNNER.contactDamage
            e.radius = EnemyArchetype.RUNNER.radius
            e.xpValue = EnemyArchetype.RUNNER.xpValue
        }
    }

    private fun updateNearestTargetMarker(archetype: EnemyArchetype, player: PlayerState) {
        var found = false
        var bestSq = Float.MAX_VALUE
        var bx = 0f
        var by = 0f
        pool.enemies.forEachActive { e ->
            if (e.archetype == archetype) {
                val dx = e.x - player.x
                val dy = e.y - player.y
                val dSq = dx * dx + dy * dy
                if (dSq < bestSq) {
                    bestSq = dSq
                    bx = e.x
                    by = e.y
                    found = true
                }
            }
        }
        hasTargetMarker = found
        if (found) {
            targetMarkerX = bx
            targetMarkerY = by
        }
    }

    private fun spawnBeaconDefenders(player: PlayerState, waveNumber: Int) {
        val count = 2 + (activeStage.stageNumber / 2).coerceAtMost(2)
        for (i in 0 until count) {
            val e = pool.enemies.acquire() ?: break
            val ang = Random.nextFloat() * 2f * PI.toFloat()
            val arch = if (i == 0 && waveNumber >= 2) EnemyArchetype.RUNNER else EnemyArchetype.SWARM
            e.archetype = arch
            e.x = (player.x + cos(ang) * 280f).coerceIn(-ArenaBounds.HALF_WIDTH + 30f, ArenaBounds.HALF_WIDTH - 30f)
            e.y = (player.y + sin(ang) * 280f).coerceIn(-ArenaBounds.HALF_HEIGHT + 30f, ArenaBounds.HALF_HEIGHT - 30f)
            e.maxHp = arch.baseHp * activeStage.enemyHpMultiplier * (1f + waveNumber * 0.15f)
            e.hp = e.maxHp
            e.speed = arch.baseSpeed * activeStage.enemySpeedMultiplier * 1.05f
            e.contactDamage = arch.contactDamage
            e.radius = arch.radius
            e.xpValue = arch.xpValue
        }
    }

    fun spawnHazardNearPlayer(player: PlayerState, spread: Float, damage: Float) {
        val hz = pool.hazardZones.acquire() ?: return
        val ang = Random.nextFloat() * 2f * PI.toFloat()
        val dist = Random.nextFloat() * spread
        hz.x = (player.x + cos(ang) * dist).coerceIn(-ArenaBounds.HALF_WIDTH + 72f, ArenaBounds.HALF_WIDTH - 72f)
        hz.y = (player.y + sin(ang) * dist).coerceIn(-ArenaBounds.HALF_HEIGHT + 72f, ArenaBounds.HALF_HEIGHT - 72f)
        hz.radius = 72f
        hz.telegraphDuration = 1.15f
        hz.damage = damage
    }

    private fun spawnLaserTargetingPlayer(player: PlayerState, damage: Float) {
        val beam = pool.laserBeams.acquire() ?: return
        beam.isHorizontal = Random.nextBoolean()
        beam.position = if (beam.isHorizontal) {
            player.y.coerceIn(-ArenaBounds.HALF_HEIGHT + 45f, ArenaBounds.HALF_HEIGHT - 45f)
        } else {
            player.x.coerceIn(-ArenaBounds.HALF_WIDTH + 45f, ArenaBounds.HALF_WIDTH - 45f)
        }
        beam.halfWidth = 23f
        beam.telegraphDuration = 1.12f
        beam.activeDuration = 0.72f
        beam.damage = damage
    }

    private fun spawnRandomLaser(damage: Float) {
        val beam = pool.laserBeams.acquire() ?: return
        beam.isHorizontal = Random.nextBoolean()
        beam.position = if (beam.isHorizontal) {
            (Random.nextFloat() * 2f - 1f) * (ArenaBounds.HALF_HEIGHT - 80f)
        } else {
            (Random.nextFloat() * 2f - 1f) * (ArenaBounds.HALF_WIDTH - 80f)
        }
        beam.halfWidth = 21f
        beam.telegraphDuration = 1.15f
        beam.activeDuration = 0.68f
        beam.damage = damage
    }

    fun fireEnemyPlasmaBolt(fromX: Float, fromY: Float, targetX: Float, targetY: Float, speed: Float, damage: Float, fromBoss: Boolean) {
        val b = pool.enemyBullets.acquire() ?: return
        val dx = targetX - fromX
        val dy = targetY - fromY
        val dist = hypot(dx, dy).coerceAtLeast(1f)
        b.x = fromX
        b.y = fromY
        b.vx = (dx / dist) * speed
        b.vy = (dy / dist) * speed
        b.radius = if (fromBoss) 10f else 8f
        b.damage = damage
        b.lifeRemaining = 4.2f
        b.fromBoss = fromBoss
    }

    fun fireBossRadialVolley(bossX: Float, bossY: Float, bulletCount: Int, speed: Float, damage: Float, angleOffset: Float) {
        val step = (2f * PI.toFloat()) / bulletCount
        for (i in 0 until bulletCount) {
            val b = pool.enemyBullets.acquire() ?: break
            val ang = angleOffset + i * step
            b.x = bossX
            b.y = bossY
            b.vx = cos(ang) * speed
            b.vy = sin(ang) * speed
            b.radius = 9.5f
            b.damage = damage
            b.lifeRemaining = 4.5f
            b.fromBoss = true
        }
    }

    private fun updateHazardZones(dt: Float, player: PlayerState) {
        pool.hazardZones.forEachActive { hz ->
            hz.elapsed += dt
            if (!hz.detonated && hz.elapsed >= hz.telegraphDuration) {
                hz.detonated = true
                spawnDetonationRing(hz.x, hz.y, hz.radius * 1.1f, DangerRed)
                onScreenShake(4.5f)
                val dx = player.x - hz.x
                val dy = player.y - hz.y
                val hitR = hz.radius + player.radius * 0.7f
                if (dx * dx + dy * dy <= hitR * hitR) {
                    onPlayerHazardHit(hz.damage, "TEHLİKE BÖLGESİ PATLAMASI")
                }
            }
            if (hz.elapsed >= hz.telegraphDuration + hz.detonationFlashDuration) {
                hz.isActive = false
            }
        }
    }

    private fun updateLaserBeams(dt: Float, player: PlayerState) {
        pool.laserBeams.forEachActive { beam ->
            beam.elapsed += dt
            if (beam.hitCooldown > 0f) {
                beam.hitCooldown -= dt
            }
            if (beam.isFiring && beam.hitCooldown <= 0f) {
                val dist = if (beam.isHorizontal) {
                    abs(player.y - beam.position)
                } else {
                    abs(player.x - beam.position)
                }
                if (dist <= beam.halfWidth + player.radius * 0.75f) {
                    beam.hitCooldown = 0.45f
                    onScreenShake(7f)
                    onPlayerHazardHit(beam.damage, "LAZER IŞINI İSABETİ")
                }
            }
            if (beam.elapsed >= beam.telegraphDuration + beam.activeDuration) {
                beam.isActive = false
            }
        }
    }

    private fun updateEnemyBullets(dt: Float, player: PlayerState) {
        pool.enemyBullets.forEachActive { b ->
            b.x += b.vx * dt
            b.y += b.vy * dt
            b.lifeRemaining -= dt

            if (b.lifeRemaining <= 0f ||
                b.x < -ArenaBounds.HALF_WIDTH || b.x > ArenaBounds.HALF_WIDTH ||
                b.y < -ArenaBounds.HALF_HEIGHT || b.y > ArenaBounds.HALF_HEIGHT ||
                ArenaBounds.isInsidePillar(b.x, b.y, b.radius * 0.5f)
            ) {
                b.isActive = false
                return@forEachActive
            }

            val dx = player.x - b.x
            val dy = player.y - b.y
            val hitR = b.radius + player.radius * 0.8f
            if (dx * dx + dy * dy <= hitR * hitR) {
                b.isActive = false
                val cause = if (b.fromBoss) "BOSS PLAZMA ATIŞI" else "DÜŞMAN ENERJİ MERMİSİ"
                onPlayerHazardHit(b.damage, cause)
            }
        }
    }

    private fun spawnDetonationRing(x: Float, y: Float, maxR: Float, color: androidx.compose.ui.graphics.Color) {
        pool.shockwaves.acquire()?.apply {
            this.x = x
            this.y = y
            startRadius = 16f
            maxRadius = maxR
            duration = 0.34f
            this.color = color
            strokeWidth = 9f
            isSuper = true
        }
    }

    private fun showBanner(text: String, positive: Boolean) {
        bannerText = text
        bannerPositive = positive
        bannerTimer = 3.0f
    }

    fun buildSnapshot(): MissionSnapshot? {
        if (!hasActiveMission) return null
        val tLeft = timeLeft.coerceAtLeast(0f)
        val totalMissions = activeStage.missionSequence.size
        return when (activeType) {
            MissionType.CAPTURE_BEACON -> MissionSnapshot(
                type = activeType,
                missionNumber = missionNumber,
                totalMissionsInStage = totalMissions,
                title = "FREKANS KULESİNİ ELE GEÇİR",
                detail = if (isPlayerInBeacon) {
                    "Şarj oluyor: %.1f / %.1f sn".format(captureHoldSeconds, captureTargetSeconds)
                } else {
                    "Oku takip et ve bölgede kal!"
                },
                progress = (captureHoldSeconds / captureTargetSeconds).coerceIn(0f, 1f),
                timeLeftSeconds = tLeft,
                maxTimeSeconds = maxTime,
                rewardText = "+${45 + missionNumber * 8}C +6K",
                isUrgent = tLeft < 7.5f
            )

            MissionType.DESTROY_CORES -> MissionSnapshot(
                type = activeType,
                missionNumber = missionNumber,
                totalMissionsInStage = totalMissions,
                title = "BOZUK ÇEKİRDEKLERİ İMHA ET",
                detail = "Yok edilen: $coresDestroyed / $coresTotal",
                progress = (coresDestroyed.toFloat() / coresTotal.coerceAtLeast(1)).coerceIn(0f, 1f),
                timeLeftSeconds = tLeft,
                maxTimeSeconds = maxTime,
                rewardText = "+${60 + missionNumber * 10}C +8K",
                isUrgent = tLeft < 8.5f
            )

            MissionType.SURVIVE_LASER_STORM -> MissionSnapshot(
                type = activeType,
                missionNumber = missionNumber,
                totalMissionsInStage = totalMissions,
                title = "LAZER FIRTINASINDAN SAĞ ÇIK",
                detail = "Kırmızı lazer ve bombalardan kaç!",
                progress = ((maxTime - tLeft) / maxTime.coerceAtLeast(1f)).coerceIn(0f, 1f),
                timeLeftSeconds = tLeft,
                maxTimeSeconds = maxTime,
                rewardText = "+${55 + missionNumber * 10}C +8K",
                isUrgent = tLeft < 5f
            )

            MissionType.HUNT_BOSS -> {
                val dmgProgress = (1f - (bossCurrentHp / bossMaxHp.coerceAtLeast(1f))).coerceIn(0f, 1f)
                val hpPct = ((bossCurrentHp / bossMaxHp.coerceAtLeast(1f)) * 100f).toInt().coerceIn(0, 100)
                MissionSnapshot(
                    type = activeType,
                    missionNumber = missionNumber,
                    totalMissionsInStage = totalMissions,
                    title = "BÖLÜM BOSS'U: RİTİM MUHAFIZI",
                    detail = "Boss Canı: %$hpPct",
                    progress = dmgProgress,
                    timeLeftSeconds = tLeft,
                    maxTimeSeconds = maxTime,
                    rewardText = "+${activeStage.rewardCoins}C +${activeStage.rewardCrystals}K",
                    isUrgent = tLeft < 10f
                )
            }
        }
    }
}
