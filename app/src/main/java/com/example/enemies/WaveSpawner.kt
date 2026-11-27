package com.example.enemies

import com.example.core.BeatManager
import com.example.core.EnemyEntity
import com.example.core.PoolManager
import com.example.data.EnemyArchetype
import com.example.data.WaveSchedule
import com.example.player.PlayerState
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.random.Random

/**
 * PoolManager-integrated 0-5 minute enemy wave spawner, enemy movement/hit-flash AI,
 * magnetic XP Rhythm Orbs, and golden Ritim Coin drops.
 */
class WaveSpawner(
    private val pool: PoolManager,
    private val beatManager: BeatManager,
    private val onPlayerDamaged: (amount: Float, cause: String) -> Unit,
    private val onXpCollected: (Int) -> Unit,
    private val onCoinCollected: (Int) -> Unit,
    private val onEliteSpawned: (String) -> Unit,
    private val onEnemyShootSingle: (fromX: Float, fromY: Float, targetX: Float, targetY: Float, speed: Float, damage: Float, fromBoss: Boolean) -> Unit,
    private val onBossShootRadial: (bossX: Float, bossY: Float, count: Int, speed: Float, damage: Float, offset: Float) -> Unit
) {
    private var spawnTimer: Float = 0.4f
    private var nextEliteIndex: Int = 0
    var currentWaveNumber: Int = 1
        private set

    fun reset() {
        spawnTimer = 0.35f
        nextEliteIndex = 0
        currentWaveNumber = 1
    }

    fun update(
        dt: Float,
        elapsedSeconds: Float,
        viewportHalfWidth: Float,
        viewportHalfHeight: Float,
        player: PlayerState,
        stageHpMult: Float = 1f,
        stageSpeedMult: Float = 1f
    ) {
        val wave = WaveSchedule.currentWave(elapsedSeconds)
        currentWaveNumber = wave.waveNumber

        if (nextEliteIndex < WaveSchedule.eliteSpawnTimestamps.size &&
            elapsedSeconds >= WaveSchedule.eliteSpawnTimestamps[nextEliteIndex]
        ) {
            nextEliteIndex++
            spawnEnemyInArena(
                archetype = EnemyArchetype.ELITE_BOSS,
                hpMultiplier = wave.hpMultiplier * stageHpMult,
                speedMultiplier = stageSpeedMult,
                playerX = player.x,
                playerY = player.y
            )
            onEliteSpawned("BOSS ARENAYA GİRDİ! (+15 KRİSTAL)")
        }

        spawnTimer -= dt
        if (spawnTimer <= 0f) {
            spawnTimer = wave.spawnIntervalSeconds
            for (b in 0 until wave.batchSize) {
                if (pool.enemies.activeCount >= 125) break
                val archetype = pickArchetype(wave.swarmWeight, wave.runnerWeight, wave.tankWeight)
                spawnEnemyInArena(
                    archetype = archetype,
                    hpMultiplier = wave.hpMultiplier * stageHpMult,
                    speedMultiplier = stageSpeedMult,
                    playerX = player.x,
                    playerY = player.y
                )
            }
        }

        updateEnemies(dt, wave.waveNumber, player)
        updateRhythmOrbs(dt, player)
    }

    fun spawnRhythmOrb(enemy: EnemyEntity) {
        val clampedX = enemy.x.coerceIn(-com.example.game.ArenaBounds.HALF_WIDTH + 24f, com.example.game.ArenaBounds.HALF_WIDTH - 24f)
        val clampedY = enemy.y.coerceIn(-com.example.game.ArenaBounds.HALF_HEIGHT + 24f, com.example.game.ArenaBounds.HALF_HEIGHT - 24f)

        pool.rhythmOrbs.acquire()?.apply {
            x = clampedX
            y = clampedY
            xpValue = enemy.archetype.xpValue
            tier = enemy.archetype.orbTier
            isCoinDrop = false
            coinValue = 0
            pulsePhase = Random.nextFloat() * 6.28f
        }

        val dropCoin = when (enemy.archetype) {
            EnemyArchetype.ELITE_BOSS, EnemyArchetype.CORRUPTED_CORE, EnemyArchetype.TANK -> true
            EnemyArchetype.RUNNER -> Random.nextFloat() < 0.34f
            EnemyArchetype.SWARM -> Random.nextFloat() < 0.25f
        }
        if (dropCoin) {
            pool.rhythmOrbs.acquire()?.apply {
                x = (clampedX + (Random.nextFloat() - 0.5f) * 28f)
                    .coerceIn(-com.example.game.ArenaBounds.HALF_WIDTH + 24f, com.example.game.ArenaBounds.HALF_WIDTH - 24f)
                y = (clampedY + (Random.nextFloat() - 0.5f) * 28f)
                    .coerceIn(-com.example.game.ArenaBounds.HALF_HEIGHT + 24f, com.example.game.ArenaBounds.HALF_HEIGHT - 24f)
                xpValue = 0
                tier = 3
                isCoinDrop = true
                coinValue = when (enemy.archetype) {
                    EnemyArchetype.ELITE_BOSS -> 35
                    EnemyArchetype.CORRUPTED_CORE -> 18
                    EnemyArchetype.TANK -> 12
                    EnemyArchetype.RUNNER -> 7
                    EnemyArchetype.SWARM -> 5
                }
                pulsePhase = Random.nextFloat() * 6.28f
            }
        }
    }

    private fun spawnEnemyInArena(
        archetype: EnemyArchetype,
        hpMultiplier: Float,
        speedMultiplier: Float = 1f,
        playerX: Float,
        playerY: Float
    ) {
        val enemy = pool.enemies.acquire() ?: return
        val edgeMargin = archetype.radius + 12f
        val maxX = com.example.game.ArenaBounds.HALF_WIDTH - edgeMargin
        val maxY = com.example.game.ArenaBounds.HALF_HEIGHT - edgeMargin

        var sx = 0f
        var sy = 0f
        var placed = false
        repeat(10) {
            val edge = Random.nextInt(4)
            when (edge) {
                0 -> {
                    sx = (Random.nextFloat() * 2f - 1f) * maxX
                    sy = -maxY
                }
                1 -> {
                    sx = (Random.nextFloat() * 2f - 1f) * maxX
                    sy = maxY
                }
                2 -> {
                    sx = -maxX
                    sy = (Random.nextFloat() * 2f - 1f) * maxY
                }
                else -> {
                    sx = maxX
                    sy = (Random.nextFloat() * 2f - 1f) * maxY
                }
            }
            val dx = sx - playerX
            val dy = sy - playerY
            if (dx * dx + dy * dy >= 240f * 240f) {
                placed = true
                return@repeat
            }
        }
        if (!placed) {
            sx = if (playerX >= 0f) -maxX else maxX
            sy = if (playerY >= 0f) -maxY else maxY
        }

        enemy.archetype = archetype
        enemy.x = sx
        enemy.y = sy
        enemy.maxHp = archetype.baseHp * hpMultiplier
        enemy.hp = enemy.maxHp
        enemy.speed = archetype.baseSpeed * speedMultiplier
        enemy.contactDamage = archetype.contactDamage
        enemy.radius = archetype.radius
        enemy.xpValue = archetype.xpValue
        enemy.shootCooldown = 1.8f + Random.nextFloat() * 1.4f
    }

    private fun pickArchetype(swarmW: Float, runnerW: Float, tankW: Float): EnemyArchetype {
        val total = swarmW + runnerW + tankW
        val roll = Random.nextFloat() * total
        return when {
            roll < swarmW -> EnemyArchetype.SWARM
            roll < swarmW + runnerW -> EnemyArchetype.RUNNER
            else -> EnemyArchetype.TANK
        }
    }

    private fun updateEnemies(dt: Float, waveNumber: Int, player: PlayerState) {
        val activeCount = pool.enemies.activeCount
        val beatPulseBoost = 1f + (1f - beatManager.currentBeatProgress) * 0.16f

        for (i in 0 until activeCount) {
            val e = pool.enemies.getActiveAt(i)
            if (!e.isActive) continue

            if (e.hitFlashTimer > 0f) {
                e.hitFlashTimer = (e.hitFlashTimer - dt).coerceAtLeast(0f)
            }
            if (e.spawnScale < 1f) {
                e.spawnScale = (e.spawnScale + dt * 5f).coerceAtMost(1f)
            }

            val dx = player.x - e.x
            val dy = player.y - e.y
            val dist = hypot(dx, dy).coerceAtLeast(0.001f)
            val dirX = dx / dist
            val dirY = dy / dist

            if (e.archetype == EnemyArchetype.CORRUPTED_CORE) {
                e.rotation += dt * 1.85f
                e.shootCooldown -= dt
                if (e.shootCooldown <= 0f) {
                    e.shootCooldown = (1.9f - waveNumber * 0.14f).coerceAtLeast(1.15f)
                    onEnemyShootSingle(e.x, e.y, player.x, player.y, 265f, 18f, false)
                }
            } else {
                val speed = e.speed * if (e.archetype == EnemyArchetype.RUNNER) beatPulseBoost else 1f
                e.vx = dirX * speed + e.knockVx
                e.vy = dirY * speed + e.knockVy
                e.x += e.vx * dt
                e.y += e.vy * dt

                e.knockVx *= 0.84f
                e.knockVy *= 0.84f
                e.rotation = atan2(dy, dx)

                // Ranged attacks for Tanks (all waves!) and tougher Elite Boss radial volleys!
                if (e.archetype == EnemyArchetype.ELITE_BOSS) {
                    e.shootCooldown -= dt
                    if (e.shootCooldown <= 0f) {
                        e.shootCooldown = (2.35f - waveNumber * 0.10f).coerceAtLeast(1.75f)
                        val volleyCount = if (waveNumber >= 3) 12 else 10
                        onBossShootRadial(e.x, e.y, volleyCount, 250f, 22f, e.rotation)
                    }
                } else if (e.archetype == EnemyArchetype.TANK && dist < 480f) {
                    e.shootCooldown -= dt
                    if (e.shootCooldown <= 0f) {
                        e.shootCooldown = (2.9f - waveNumber * 0.15f).coerceAtLeast(2.0f)
                        onEnemyShootSingle(e.x, e.y, player.x, player.y, 245f, 18f, false)
                    }
                }
            }

            com.example.game.ArenaBounds.clampEnemy(e)

            if (e.archetype != EnemyArchetype.CORRUPTED_CORE) {
                val checkEnd = (i + 5).coerceAtMost(activeCount)
                for (j in (i + 1) until checkEnd) {
                    val other = pool.enemies.getActiveAt(j)
                    if (!other.isActive || other.archetype == EnemyArchetype.CORRUPTED_CORE) continue
                    val ox = e.x - other.x
                    val oy = e.y - other.y
                    val minDist = (e.radius + other.radius) * 0.82f
                    val dSq = ox * ox + oy * oy
                    if (dSq > 0.01f && dSq < minDist * minDist) {
                        val d = hypot(ox, oy)
                        val push = (minDist - d) * 0.35f
                        val nx = ox / d
                        val ny = oy / d
                        e.x += nx * push
                        e.y += ny * push
                        other.x -= nx * push
                        other.y -= ny * push
                    }
                }
            }

            val touchDist = e.radius + player.radius * 0.84f
            if (dist < touchDist) {
                if (player.takeDamage(e.contactDamage)) {
                    beatManager.triggerDamageSfx()
                    val cause = when (e.archetype) {
                        EnemyArchetype.ELITE_BOSS -> "RİTİM MUHAFIZI SALDIRISI"
                        EnemyArchetype.TANK -> "AĞIR BAS TANKI ÇARPMASI"
                        EnemyArchetype.CORRUPTED_CORE -> "BOZUK ÇEKİRDEK TEMASI"
                        EnemyArchetype.RUNNER -> "HIZLI KOŞUCU PUSU"
                        EnemyArchetype.SWARM -> "SÜRÜ SALDIRISI"
                    }
                    onPlayerDamaged(e.contactDamage, cause)
                }
                if (e.archetype != EnemyArchetype.CORRUPTED_CORE) {
                    e.knockVx -= dirX * 140f
                    e.knockVy -= dirY * 140f
                }
            }
        }
    }

    private fun updateRhythmOrbs(dt: Float, player: PlayerState) {
        val magnetRad = player.magnetRadius
        val magnetRadSq = magnetRad * magnetRad
        val collectRad = player.radius + 14f
        val collectRadSq = collectRad * collectRad

        val count = pool.rhythmOrbs.activeCount
        for (i in 0 until count) {
            val orb = pool.rhythmOrbs.getActiveAt(i)
            if (!orb.isActive) continue
            orb.pulsePhase += dt * 5f

            val dx = player.x - orb.x
            val dy = player.y - orb.y
            val dSq = dx * dx + dy * dy

            if (dSq <= collectRadSq) {
                orb.isActive = false
                beatManager.triggerOrbSfx()
                if (orb.isCoinDrop) {
                    onCoinCollected(orb.coinValue)
                } else {
                    onXpCollected(orb.xpValue)
                }
                continue
            }

            if (!orb.isMagnetized && dSq <= magnetRadSq) {
                orb.isMagnetized = true
                orb.magnetSpeed = 180f
            }

            if (orb.isMagnetized) {
                orb.magnetSpeed = (orb.magnetSpeed + 980f * dt).coerceAtMost(920f)
                val dist = hypot(dx, dy).coerceAtLeast(1f)
                orb.x += (dx / dist) * orb.magnetSpeed * dt
                orb.y += (dy / dist) * orb.magnetSpeed * dt
            }
        }
    }
}
