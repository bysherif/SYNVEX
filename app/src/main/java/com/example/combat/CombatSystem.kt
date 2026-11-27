package com.example.combat

import androidx.compose.ui.graphics.Color
import com.example.core.BeatManager
import com.example.core.EnemyEntity
import com.example.core.PoolManager
import com.example.data.WeaponId
import com.example.player.PlayerState
import com.example.ui.theme.AcidLime
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.HotMagenta
import com.example.ui.theme.SolarGold
import com.example.ui.theme.SwarmViolet
import com.example.ui.theme.TankCrimson
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

/**
 * Beat-synchronized combat engine implementing:
 * 1. BeatShockwaveAttack (half-note AoE shockwave using PoolManager.overlapEnemiesNonAlloc + neon ring VFX)
 * 2. All 6 active weapons + 6 Evolution Super Synergies
 * 3. Zero-GC projectile, mine, chain-lightning, slash, and orbit blade simulation
 */
class CombatSystem(
    private val pool: PoolManager,
    private val beatManager: BeatManager,
    private val onEnemyKilled: (EnemyEntity) -> Unit,
    private val onScreenShake: (Float) -> Unit
) {
    private val orbitPosBuffer = FloatArray(2)
    private val visitedChainIndices = IntArray(12)
    private var orbitDamageCooldown = 0f

    /**
     * Triggered deterministically on every hardware-clock beat from BeatManager.onBeat(beatIndex).
     */
    fun onBeat(beatIndex: Int, player: PlayerState) {
        val areaMult = player.areaMultiplier
        val dmgMult = player.damageMultiplier
        val dtLevel = player.doubleTimeLevel

        // 1. BEAT SHOCKWAVE (Half-note: every 2 beats, OR every single beat when evolved to SİSMİK DROP!)
        val shockLv = player.weaponLevels[WeaponId.BEAT_SHOCKWAVE] ?: 0
        if (shockLv > 0) {
            val evolved = player.evolvedWeapons.contains(WeaponId.BEAT_SHOCKWAVE)
            val cadence = if (evolved || dtLevel >= 4) 1 else 2
            if (beatIndex % cadence == 0) {
                fireBeatShockwave(player, shockLv, evolved, areaMult, dmgMult)
            }
        }

        // 2. LASER ARP (Fires homing neon bolts every beat)
        val laserLv = player.weaponLevels[WeaponId.LASER_ARP] ?: 0
        if (laserLv > 0) {
            val evolved = player.evolvedWeapons.contains(WeaponId.LASER_ARP)
            fireLaserArp(player, laserLv, evolved, dmgMult)
        }

        // 3. SUB-BASS MINE (Detonates armed mines on strong beat; drops new mines every 2-3 beats)
        val mineLv = player.weaponLevels[WeaponId.SUB_MINE] ?: 0
        if (mineLv > 0) {
            val evolved = player.evolvedWeapons.contains(WeaponId.SUB_MINE)
            tickBassMinesOnBeat(player, beatIndex, mineLv, evolved, areaMult, dmgMult)
        }

        // 4. TREBLE SLASH (Every 2 beats, or every beat when evolved to 360° FREKANS KASIRGASI)
        val slashLv = player.weaponLevels[WeaponId.TREBLE_SLASH] ?: 0
        if (slashLv > 0) {
            val evolved = player.evolvedWeapons.contains(WeaponId.TREBLE_SLASH)
            if (evolved || beatIndex % 2 == 1) {
                fireTrebleSlash(player, slashLv, evolved, areaMult, dmgMult)
            }
        }

        // 5. FEEDBACK NOVA (Chain Lightning every 2 beats, or every beat when evolved to TESLA SENFONİSİ)
        val chainLv = player.weaponLevels[WeaponId.FEEDBACK_NOVA] ?: 0
        if (chainLv > 0) {
            val evolved = player.evolvedWeapons.contains(WeaponId.FEEDBACK_NOVA)
            if (evolved || beatIndex % 2 == 0) {
                fireFeedbackChain(player, chainLv, evolved, areaMult, dmgMult)
            }
        }
    }

    private fun fireBeatShockwave(
        player: PlayerState,
        level: Int,
        evolved: Boolean,
        areaMult: Float,
        dmgMult: Float
    ) {
        val baseRadius = (138f + level * 22f + if (evolved) 75f else 0f) * areaMult
        val baseDamage = (20f + level * 9f + if (evolved) 28f else 0f) * dmgMult

        // Spawn primary neon ring effect from PoolManager (animates scale + alpha fade, then returns to pool)
        pool.shockwaves.acquire()?.apply {
            x = player.x
            y = player.y
            startRadius = 24f
            maxRadius = baseRadius
            duration = if (evolved) 0.44f else 0.38f
            color = if (evolved) SolarGold else ElectricCyan
            strokeWidth = if (evolved) 12f else 8f
            isSuper = evolved
        }

        if (evolved) {
            // Secondary magenta bass ring for SİSMİK DROP evolution
            pool.shockwaves.acquire()?.apply {
                x = player.x
                y = player.y
                startRadius = 10f
                maxRadius = baseRadius * 0.72f
                duration = 0.32f
                color = HotMagenta
                strokeWidth = 7f
                isSuper = true
            }
        }

        beatManager.triggerShockwaveSfx()
        onScreenShake(if (evolved) 8f else 3.5f)

        // Zero-GC circle overlap damage check via IDamageable
        val knockPower = if (evolved) 280f else 175f
        pool.overlapEnemiesNonAlloc(player.x, player.y, baseRadius) { enemy ->
            val dx = enemy.x - player.x
            val dy = enemy.y - player.y
            val dist = hypot(dx, dy).coerceAtLeast(1f)
            val kx = (dx / dist) * knockPower
            val ky = (dy / dist) * knockPower
            applyDamageToEnemy(enemy, baseDamage, kx, ky, if (evolved) SolarGold else ElectricCyan, evolved, player.critChance)
        }
    }

    private fun fireLaserArp(
        player: PlayerState,
        level: Int,
        evolved: Boolean,
        dmgMult: Float
    ) {
        val boltCount = if (evolved) 5 else (1 + (level + 1) / 2)
        val damage = (16f + level * 7f + if (evolved) 16f else 0f) * dmgMult
        val speed = if (evolved) 720f else 560f
        val pierce = if (evolved) 4 else (1 + level / 3)

        // Find nearest enemy without allocating a sorted list
        val nearest = findNearestEnemy(player.x, player.y, 640f)
        val baseAngle = if (nearest != null) {
            atan2(nearest.y - player.y, nearest.x - player.x)
        } else {
            player.facingAngle
        }

        val spreadRad = if (boltCount > 1) 0.22f else 0f
        for (i in 0 until boltCount) {
            val offset = (i - (boltCount - 1) * 0.5f) * spreadRad
            val angle = baseAngle + offset
            pool.projectiles.acquire()?.apply {
                x = player.x + cos(angle) * 20f
                y = player.y + sin(angle) * 20f
                vx = cos(angle) * speed
                vy = sin(angle) * speed
                this.damage = damage
                radius = if (evolved) 11f else 8f
                pierceRemaining = pierce
                lifeRemaining = 1.35f
                color = if (evolved) SolarGold else AcidLime
                isSuper = evolved
            }
        }
    }

    private fun tickBassMinesOnBeat(
        player: PlayerState,
        beatIndex: Int,
        level: Int,
        evolved: Boolean,
        areaMult: Float,
        dmgMult: Float
    ) {
        // Countdown and detonate existing mines on beat
        val activeMines = pool.bassMines.activeCount
        for (i in 0 until activeMines) {
            val mine = pool.bassMines.getActiveAt(i)
            if (!mine.isActive) continue
            mine.armedBeatsRemaining--
            if (mine.armedBeatsRemaining <= 0) {
                detonateMine(mine)
            }
        }

        val dropCadence = if (evolved) 2 else 3
        if (beatIndex % dropCadence == 0) {
            pool.bassMines.acquire()?.apply {
                x = player.x
                y = player.y
                radius = (98f + level * 16f + if (evolved) 55f else 0f) * areaMult
                damage = (32f + level * 12f + if (evolved) 32f else 0f) * dmgMult
                armedBeatsRemaining = 2
                isBlackHole = evolved
            }
        }
    }

    private fun detonateMine(mine: com.example.core.BassMineEntity) {
        mine.isActive = false
        val col = if (mine.isBlackHole) SolarGold else TankCrimson
        pool.shockwaves.acquire()?.apply {
            x = mine.x
            y = mine.y
            startRadius = 16f
            maxRadius = mine.radius
            duration = 0.34f
            color = col
            strokeWidth = if (mine.isBlackHole) 10f else 7f
            isSuper = mine.isBlackHole
        }
        onScreenShake(if (mine.isBlackHole) 7f else 4f)
        pool.overlapEnemiesNonAlloc(mine.x, mine.y, mine.radius) { enemy ->
            val dx = enemy.x - mine.x
            val dy = enemy.y - mine.y
            val dist = hypot(dx, dy).coerceAtLeast(1f)
            applyDamageToEnemy(
                enemy = enemy,
                amount = mine.damage,
                kx = (dx / dist) * 210f,
                ky = (dy / dist) * 210f,
                color = col,
                isCrit = mine.isBlackHole
            )
        }
    }

    private fun fireTrebleSlash(
        player: PlayerState,
        level: Int,
        evolved: Boolean,
        areaMult: Float,
        dmgMult: Float
    ) {
        val radius = (118f + level * 18f + if (evolved) 58f else 0f) * areaMult
        val damage = (26f + level * 11f + if (evolved) 24f else 0f) * dmgMult
        val sweepDeg = if (evolved) 360f else (110f + level * 14f)
        val halfSweepRad = (sweepDeg * 0.5f) * (PI.toFloat() / 180f)
        val col = if (evolved) SolarGold else SwarmViolet

        pool.slashArcs.acquire()?.apply {
            x = player.x
            y = player.y
            angleRad = player.facingAngle
            this.radius = radius
            this.sweepDeg = sweepDeg
            duration = 0.25f
            color = col
            isFullCircle = evolved
        }

        pool.overlapEnemiesNonAlloc(player.x, player.y, radius) { enemy ->
            val dx = enemy.x - player.x
            val dy = enemy.y - player.y
            val angleToEnemy = atan2(dy, dx)
            val diff = normalizeAngle(angleToEnemy - player.facingAngle)
            if (evolved || kotlin.math.abs(diff) <= halfSweepRad) {
                val dist = hypot(dx, dy).coerceAtLeast(1f)
                applyDamageToEnemy(enemy, damage, (dx / dist) * 190f, (dy / dist) * 190f, col, evolved, player.critChance)
            }
        }
    }

    private fun fireFeedbackChain(
        player: PlayerState,
        level: Int,
        evolved: Boolean,
        areaMult: Float,
        dmgMult: Float
    ) {
        val maxBounces = if (evolved) 8 else (2 + level)
        val jumpRange = (190f + level * 20f) * areaMult
        val damage = (22f + level * 9f + if (evolved) 22f else 0f) * dmgMult
        val col = if (evolved) SolarGold else ElectricCyan

        var visitedCount = 0
        var originX = player.x
        var originY = player.y

        for (bounce in 0 until maxBounces) {
            var bestIdx = -1
            var bestDistSq = jumpRange * jumpRange
            val activeCount = pool.enemies.activeCount
            for (i in 0 until activeCount) {
                val rawIdx = pool.enemies.getActiveRawIndex(i)
                val enemy = pool.enemies.getRaw(rawIdx)
                if (!enemy.isActive) continue
                if (isIndexVisited(rawIdx, visitedCount)) continue
                val dx = enemy.x - originX
                val dy = enemy.y - originY
                val dSq = dx * dx + dy * dy
                if (dSq < bestDistSq) {
                    bestDistSq = dSq
                    bestIdx = rawIdx
                }
            }
            if (bestIdx < 0) break
            if (visitedCount < visitedChainIndices.size) {
                visitedChainIndices[visitedCount++] = bestIdx
            }
            val target = pool.enemies.getRaw(bestIdx)
            pool.lightningArcs.acquire()?.apply {
                x1 = originX
                y1 = originY
                x2 = target.x
                y2 = target.y
                duration = 0.22f
                color = col
                isSuper = evolved
            }
            originX = target.x
            originY = target.y
            applyDamageToEnemy(target, damage, 0f, 0f, col, evolved, player.critChance)
        }
    }

    /**
     * Per-frame continuous update for Synth Orbit blades, Black-Hole Mine gravity pull,
     * Laser projectiles, and pooled VFX lifetimes.
     */
    fun update(dt: Float, player: PlayerState) {
        val areaMult = player.areaMultiplier
        val dmgMult = player.damageMultiplier

        // 1. SYNTH ORBIT continuous blade collision
        val orbitLv = player.weaponLevels[WeaponId.SYNTH_ORBIT] ?: 0
        if (orbitLv > 0) {
            orbitDamageCooldown -= dt
            if (orbitDamageCooldown <= 0f) {
                orbitDamageCooldown = 0.14f
                val evolved = player.evolvedWeapons.contains(WeaponId.SYNTH_ORBIT)
                val count = if (evolved) 6 else (1 + orbitLv)
                val orbitRadius = (92f + if (evolved) 26f else 0f) * areaMult
                val bladeRadius = if (evolved) 22f else 15f
                val bladeDamage = (12f + orbitLv * 5f + if (evolved) 14f else 0f) * dmgMult
                val col = if (evolved) SolarGold else HotMagenta

                for (b in 0 until count) {
                    player.orbitBladePosition(b, count, orbitRadius, orbitPosBuffer)
                    val bx = orbitPosBuffer[0]
                    val by = orbitPosBuffer[1]
                    pool.overlapEnemiesNonAlloc(bx, by, bladeRadius) { enemy ->
                        val dx = enemy.x - player.x
                        val dy = enemy.y - player.y
                        val dist = hypot(dx, dy).coerceAtLeast(1f)
                        applyDamageToEnemy(enemy, bladeDamage, (dx / dist) * 120f, (dy / dist) * 120f, col, evolved, player.critChance)
                    }
                }
            }
        }

        // 2. BLACK HOLE BASS MINE gravity pull
        val mineCount = pool.bassMines.activeCount
        for (i in 0 until mineCount) {
            val mine = pool.bassMines.getActiveAt(i)
            if (!mine.isActive) continue
            mine.age += dt
            if (mine.isBlackHole) {
                val pullRadius = mine.radius * 1.45f
                pool.overlapEnemiesNonAlloc(mine.x, mine.y, pullRadius) { enemy ->
                    val dx = mine.x - enemy.x
                    val dy = mine.y - enemy.y
                    val dist = hypot(dx, dy).coerceAtLeast(8f)
                    val pull = 240f * dt
                    enemy.x += (dx / dist) * pull
                    enemy.y += (dy / dist) * pull
                }
            }
        }

        // 3. PROJECTILES movement & collision
        val projCount = pool.projectiles.activeCount
        for (i in 0 until projCount) {
            val p = pool.projectiles.getActiveAt(i)
            if (!p.isActive) continue
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.lifeRemaining -= dt
            if (p.lifeRemaining <= 0f) {
                p.isActive = false
                continue
            }
            val hits = pool.collectOverlappingEnemyIndices(p.x, p.y, p.radius)
            for (h in 0 until hits) {
                val enemy = pool.enemies.getRaw(pool.getOverlapRawIndex(h))
                if (!enemy.isActive) continue
                val spd = hypot(p.vx, p.vy).coerceAtLeast(1f)
                applyDamageToEnemy(
                    enemy = enemy,
                    amount = p.damage,
                    kx = (p.vx / spd) * 110f,
                    ky = (p.vy / spd) * 110f,
                    color = p.color,
                    isCrit = p.isSuper,
                    playerCritChance = player.critChance
                )
                p.pierceRemaining--
                if (p.pierceRemaining <= 0) {
                    p.isActive = false
                    break
                }
            }
        }

        // 4. UPDATE POOLED VISUAL EFFECTS (Shockwaves, Slashes, Lightning, Particles, Popups)
        pool.shockwaves.forEachActive { sw ->
            sw.elapsed += dt
            if (sw.elapsed >= sw.duration) sw.isActive = false
        }
        pool.slashArcs.forEachActive { sl ->
            sl.elapsed += dt
            if (sl.elapsed >= sl.duration) sl.isActive = false
        }
        pool.lightningArcs.forEachActive { arc ->
            arc.elapsed += dt
            if (arc.elapsed >= arc.duration) arc.isActive = false
        }
        pool.particles.forEachActive { pt ->
            pt.elapsed += dt
            pt.x += pt.vx * dt
            pt.y += pt.vy * dt
            pt.vx *= 0.92f
            pt.vy *= 0.92f
            if (pt.elapsed >= pt.duration) pt.isActive = false
        }
        pool.damagePopups.forEachActive { pop ->
            pop.elapsed += dt
            pop.y += pop.vy * dt
            pop.vy *= 0.94f
            if (pop.elapsed >= pop.duration) pop.isActive = false
        }
    }

    fun triggerDashBurstVfx(player: PlayerState) {
        pool.shockwaves.acquire()?.apply {
            x = player.x
            y = player.y
            startRadius = 14f
            maxRadius = 92f
            duration = 0.24f
            color = ElectricCyan
            strokeWidth = 6.5f
            isSuper = false
        }
        for (s in 0 until 6) {
            pool.particles.acquire()?.apply {
                val ang = player.facingAngle + PI.toFloat() + (Random.nextFloat() - 0.5f) * 1.1f
                val spd = 110f + Random.nextFloat() * 140f
                x = player.x
                y = player.y
                vx = cos(ang) * spd
                vy = sin(ang) * spd
                size = 4.5f
                duration = 0.24f
                color = ElectricCyan
            }
        }
        beatManager.triggerShockwaveSfx()
    }

    /**
     * Unleashes the player's 100%-charged Overdrive Nova:
     * Vaporizes all enemy plasma bullets, blasts every enemy within 390px with critical damage,
     * and restores +20 HP!
     */
    fun triggerOverdriveNova(player: PlayerState) {
        val novaRadius = 390f * player.areaMultiplier
        val novaDamage = 125f * player.damageMultiplier

        // Vaporize all enemy plasma projectiles on screen
        pool.enemyBullets.forEachActive { b ->
            b.isActive = false
            pool.particles.acquire()?.apply {
                x = b.x
                y = b.y
                vx = (Random.nextFloat() - 0.5f) * 120f
                vy = (Random.nextFloat() - 0.5f) * 120f
                size = 4f
                duration = 0.22f
                color = SolarGold
            }
        }

        pool.shockwaves.acquire()?.apply {
            x = player.x
            y = player.y
            startRadius = 24f
            maxRadius = novaRadius
            duration = 0.48f
            color = SolarGold
            strokeWidth = 16f
            isSuper = true
        }
        pool.shockwaves.acquire()?.apply {
            x = player.x
            y = player.y
            startRadius = 12f
            maxRadius = novaRadius * 0.72f
            duration = 0.36f
            color = HotMagenta
            strokeWidth = 10f
            isSuper = true
        }

        player.heal(20f)
        beatManager.triggerEvolutionSfx()
        onScreenShake(11f)

        pool.overlapEnemiesNonAlloc(player.x, player.y, novaRadius) { enemy ->
            val dx = enemy.x - player.x
            val dy = enemy.y - player.y
            val dist = hypot(dx, dy).coerceAtLeast(1f)
            applyDamageToEnemy(
                enemy = enemy,
                amount = novaDamage,
                kx = (dx / dist) * 340f,
                ky = (dy / dist) * 340f,
                color = SolarGold,
                isCrit = true,
                playerCritChance = 0f
            )
        }
    }

    private fun applyDamageToEnemy(
        enemy: EnemyEntity,
        amount: Float,
        kx: Float,
        ky: Float,
        color: Color,
        isCrit: Boolean,
        playerCritChance: Float = 0.08f
    ) {
        val rolledCrit = isCrit || (playerCritChance > 0f && Random.nextFloat() < playerCritChance)
        val finalAmount = if (rolledCrit && !isCrit) amount * 2.0f else amount
        val killed = enemy.takeDamage(finalAmount, kx, ky)

        // Pooled damage popup (pre-formats text once at spawn — zero per-frame String GC)
        pool.damagePopups.acquire()?.apply {
            x = enemy.x + (Random.nextFloat() - 0.5f) * 18f
            y = enemy.y - enemy.radius
            setDisplayValue(finalAmount.toInt().coerceAtLeast(1))
            this.color = if (rolledCrit) SolarGold else Color.White
            this.isCrit = rolledCrit
        }

        // Crisp hit spark particles (tuned for sustained 60 FPS)
        val sparkCount = if (killed) 5 else if (rolledCrit) 2 else 1
        for (s in 0 until sparkCount) {
            pool.particles.acquire()?.apply {
                val ang = Random.nextFloat() * 2f * PI.toFloat()
                val spd = if (killed) 105f + Random.nextFloat() * 185f else 70f + Random.nextFloat() * 85f
                x = enemy.x
                y = enemy.y
                vx = cos(ang) * spd
                vy = sin(ang) * spd
                size = if (killed) 5.2f else if (rolledCrit) 4.2f else 3.2f
                duration = if (killed) 0.34f else 0.18f
                this.color = if (rolledCrit) SolarGold else if (killed) enemy.archetype.color else color
            }
        }

        if (killed) {
            onEnemyKilled(enemy)
        }
    }

    private fun findNearestEnemy(px: Float, py: Float, maxRange: Float): EnemyEntity? {
        var best: EnemyEntity? = null
        var bestSq = maxRange * maxRange
        val count = pool.enemies.activeCount
        for (i in 0 until count) {
            val enemy = pool.enemies.getActiveAt(i)
            if (!enemy.isActive) continue
            val dx = enemy.x - px
            val dy = enemy.y - py
            val dSq = dx * dx + dy * dy
            if (dSq < bestSq) {
                bestSq = dSq
                best = enemy
            }
        }
        return best
    }

    private fun isIndexVisited(rawIdx: Int, count: Int): Boolean {
        for (i in 0 until count) {
            if (visitedChainIndices[i] == rawIdx) return true
        }
        return false
    }

    private fun normalizeAngle(angle: Float): Float {
        var a = angle
        val twoPi = 2f * PI.toFloat()
        while (a > PI.toFloat()) a -= twoPi
        while (a < -PI.toFloat()) a += twoPi
        return a
    }
}
