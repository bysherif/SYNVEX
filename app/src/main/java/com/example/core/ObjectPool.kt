package com.example.core

import androidx.compose.ui.graphics.Color
import com.example.data.EnemyArchetype
import com.example.ui.theme.ElectricCyan

/**
 * Interface for all pooled simulation objects to guarantee zero-GC reuse.
 */
interface IPoolable {
    var isActive: Boolean
    fun onSpawn()
    fun onDespawn()
}

/**
 * Fixed-capacity zero-allocation object pool.
 * Iterates over active items by index without allocating iterators or wrapper lists.
 */
class ObjectPool<T : IPoolable>(
    private val capacity: Int,
    factory: () -> T
) {
    private val items: ArrayList<T> = ArrayList<T>(capacity).apply {
        repeat(capacity) { add(factory()) }
    }
    private val activeIndices = IntArray(capacity)
    var activeCount: Int = 0
        private set

    fun acquire(): T? {
        for (i in 0 until capacity) {
            val item = items[i]
            if (!item.isActive) {
                item.isActive = true
                item.onSpawn()
                activeIndices[activeCount++] = i
                return item
            }
        }
        return null
    }

    inline fun forEachActive(action: (T) -> Unit) {
        var i = 0
        while (i < activeCount) {
            val item = getActiveAt(i)
            if (item.isActive) {
                action(item)
            }
            i++
        }
    }

    fun getActiveAt(activeSlot: Int): T = items[activeIndices[activeSlot]]

    fun getRaw(index: Int): T = items[index]

    fun getActiveRawIndex(activeSlot: Int): Int = activeIndices[activeSlot]

    /**
     * Sweeps deactivated entities in O(activeCount) without any memory allocations.
     */
    fun compact() {
        var write = 0
        for (read in 0 until activeCount) {
            val rawIdx = activeIndices[read]
            val item = items[rawIdx]
            if (item.isActive) {
                activeIndices[write++] = rawIdx
            } else {
                item.onDespawn()
            }
        }
        activeCount = write
    }

    fun releaseAll() {
        for (i in 0 until activeCount) {
            val item = items[activeIndices[i]]
            item.isActive = false
            item.onDespawn()
        }
        activeCount = 0
    }
}

interface IDamageable {
    fun takeDamage(amount: Float, knockbackX: Float, knockbackY: Float): Boolean
}

class EnemyEntity : IPoolable, IDamageable {
    override var isActive: Boolean = false
    var archetype: EnemyArchetype = EnemyArchetype.SWARM
    var x: Float = 0f
    var y: Float = 0f
    var vx: Float = 0f
    var vy: Float = 0f
    var knockVx: Float = 0f
    var knockVy: Float = 0f
    var hp: Float = 10f
    var maxHp: Float = 10f
    var speed: Float = 100f
    var contactDamage: Float = 8f
    var radius: Float = 16f
    var xpValue: Int = 10
    var hitFlashTimer: Float = 0f
    var rotation: Float = 0f
    var spawnScale: Float = 0f
    var shootCooldown: Float = 2.5f
    var isMissionTarget: Boolean = false

    override fun onSpawn() {
        vx = 0f
        vy = 0f
        knockVx = 0f
        knockVy = 0f
        hitFlashTimer = 0f
        rotation = 0f
        spawnScale = 0.2f
        shootCooldown = 2.2f
        isMissionTarget = false
    }

    override fun onDespawn() {
        hitFlashTimer = 0f
        isMissionTarget = false
    }

    override fun takeDamage(amount: Float, knockbackX: Float, knockbackY: Float): Boolean {
        if (!isActive) return false
        hp -= amount
        // 0.1s white hit-flash requirement
        hitFlashTimer = 0.10f
        val resist = when (archetype) {
            EnemyArchetype.CORRUPTED_CORE -> 0f
            EnemyArchetype.TANK, EnemyArchetype.ELITE_BOSS -> 0.25f
            else -> 1f
        }
        knockVx += knockbackX * resist
        knockVy += knockbackY * resist
        if (hp <= 0f) {
            hp = 0f
            isActive = false
            return true
        }
        return false
    }
}

class RhythmOrbEntity : IPoolable {
    override var isActive: Boolean = false
    var x: Float = 0f
    var y: Float = 0f
    var vx: Float = 0f
    var vy: Float = 0f
    var xpValue: Int = 10
    var isMagnetized: Boolean = false
    var magnetSpeed: Float = 0f
    var tier: Int = 0 // 0 = cyan small, 1 = lime medium, 2 = gold mega, 3 = Ritim Coin pickup
    var isCoinDrop: Boolean = false
    var coinValue: Int = 0
    var pulsePhase: Float = 0f

    override fun onSpawn() {
        vx = 0f
        vy = 0f
        isMagnetized = false
        magnetSpeed = 0f
        tier = 0
        isCoinDrop = false
        coinValue = 0
        pulsePhase = 0f
    }

    override fun onDespawn() {
        isMagnetized = false
    }
}

class ShockwaveEffect : IPoolable {
    override var isActive: Boolean = false
    var x: Float = 0f
    var y: Float = 0f
    var startRadius: Float = 24f
    var maxRadius: Float = 180f
    var elapsed: Float = 0f
    var duration: Float = 0.42f
    var color: Color = ElectricCyan
    var strokeWidth: Float = 8f
    var isSuper: Boolean = false

    val progress: Float
        get() = (elapsed / duration).coerceIn(0f, 1f)

    override fun onSpawn() {
        elapsed = 0f
        isSuper = false
        strokeWidth = 8f
    }

    override fun onDespawn() {
        elapsed = 0f
    }
}

class ProjectileEntity : IPoolable {
    override var isActive: Boolean = false
    var x: Float = 0f
    var y: Float = 0f
    var vx: Float = 0f
    var vy: Float = 0f
    var damage: Float = 18f
    var radius: Float = 9f
    var pierceRemaining: Int = 1
    var lifeRemaining: Float = 1.6f
    var color: Color = ElectricCyan
    var isSuper: Boolean = false

    override fun onSpawn() {
        pierceRemaining = 1
        lifeRemaining = 1.6f
        isSuper = false
    }

    override fun onDespawn() {
        lifeRemaining = 0f
    }
}

class BassMineEntity : IPoolable {
    override var isActive: Boolean = false
    var x: Float = 0f
    var y: Float = 0f
    var radius: Float = 110f
    var damage: Float = 45f
    var armedBeatsRemaining: Int = 2
    var isBlackHole: Boolean = false
    var age: Float = 0f

    override fun onSpawn() {
        armedBeatsRemaining = 2
        isBlackHole = false
        age = 0f
    }

    override fun onDespawn() {
        age = 0f
    }
}

class SlashArcEntity : IPoolable {
    override var isActive: Boolean = false
    var x: Float = 0f
    var y: Float = 0f
    var angleRad: Float = 0f
    var radius: Float = 140f
    var sweepDeg: Float = 120f
    var elapsed: Float = 0f
    var duration: Float = 0.26f
    var color: Color = ElectricCyan
    var isFullCircle: Boolean = false

    override fun onSpawn() {
        elapsed = 0f
        isFullCircle = false
    }

    override fun onDespawn() {
        elapsed = 0f
    }
}

class LightningArcEntity : IPoolable {
    override var isActive: Boolean = false
    var x1: Float = 0f
    var y1: Float = 0f
    var x2: Float = 0f
    var y2: Float = 0f
    var elapsed: Float = 0f
    var duration: Float = 0.22f
    var color: Color = ElectricCyan
    var isSuper: Boolean = false

    override fun onSpawn() {
        elapsed = 0f
        isSuper = false
    }

    override fun onDespawn() {
        elapsed = 0f
    }
}

class ParticleEntity : IPoolable {
    override var isActive: Boolean = false
    var x: Float = 0f
    var y: Float = 0f
    var vx: Float = 0f
    var vy: Float = 0f
    var size: Float = 4f
    var elapsed: Float = 0f
    var duration: Float = 0.35f
    var color: Color = ElectricCyan

    override fun onSpawn() {
        elapsed = 0f
    }

    override fun onDespawn() {
        elapsed = 0f
    }
}

class DamagePopupEntity : IPoolable {
    override var isActive: Boolean = false
    var x: Float = 0f
    var y: Float = 0f
    var vy: Float = -55f
    var value: Int = 0
    var cachedText: String = ""
    var elapsed: Float = 0f
    var duration: Float = 0.48f
    var color: Color = Color.White
    var isCrit: Boolean = false

    fun setDisplayValue(amount: Int) {
        value = amount
        cachedText = amount.toString()
    }

    override fun onSpawn() {
        elapsed = 0f
        vy = -58f
        isCrit = false
        cachedText = ""
    }

    override fun onDespawn() {
        elapsed = 0f
    }
}

class HazardZoneEntity : IPoolable {
    override var isActive: Boolean = false
    var x: Float = 0f
    var y: Float = 0f
    var radius: Float = 72f
    var elapsed: Float = 0f
    var telegraphDuration: Float = 1.35f
    var detonationFlashDuration: Float = 0.24f
    var damage: Float = 24f
    var detonated: Boolean = false

    val chargeProgress: Float
        get() = (elapsed / telegraphDuration).coerceIn(0f, 1f)

    override fun onSpawn() {
        elapsed = 0f
        telegraphDuration = 1.35f
        detonationFlashDuration = 0.24f
        damage = 24f
        detonated = false
    }

    override fun onDespawn() {
        elapsed = 0f
        detonated = false
    }
}

class LaserBeamEntity : IPoolable {
    override var isActive: Boolean = false
    var isHorizontal: Boolean = true
    var position: Float = 0f // world Y if horizontal, world X if vertical
    var halfWidth: Float = 20f
    var elapsed: Float = 0f
    var telegraphDuration: Float = 1.30f
    var activeDuration: Float = 0.65f
    var damage: Float = 26f
    var hitCooldown: Float = 0f

    val isFiring: Boolean
        get() = elapsed >= telegraphDuration && elapsed < (telegraphDuration + activeDuration)

    val telegraphProgress: Float
        get() = (elapsed / telegraphDuration).coerceIn(0f, 1f)

    override fun onSpawn() {
        elapsed = 0f
        telegraphDuration = 1.30f
        activeDuration = 0.65f
        damage = 26f
        hitCooldown = 0f
    }

    override fun onDespawn() {
        elapsed = 0f
        hitCooldown = 0f
    }
}

class EnemyBulletEntity : IPoolable {
    override var isActive: Boolean = false
    var x: Float = 0f
    var y: Float = 0f
    var vx: Float = 0f
    var vy: Float = 0f
    var radius: Float = 8.5f
    var damage: Float = 16f
    var lifeRemaining: Float = 4.0f
    var fromBoss: Boolean = false

    override fun onSpawn() {
        radius = 8.5f
        damage = 16f
        lifeRemaining = 4.0f
        fromBoss = false
    }

    override fun onDespawn() {
        lifeRemaining = 0f
    }
}

/**
 * Centralized zero-GC PoolManager managing all combat & arena entities plus a reusable
 * overlap query buffer (Android equivalent of Physics2D.OverlapCircleNonAlloc).
 */
class PoolManager {
    val enemies = ObjectPool(180) { EnemyEntity() }
    val rhythmOrbs = ObjectPool(200) { RhythmOrbEntity() }
    val shockwaves = ObjectPool(32) { ShockwaveEffect() }
    val projectiles = ObjectPool(110) { ProjectileEntity() }
    val bassMines = ObjectPool(20) { BassMineEntity() }
    val slashArcs = ObjectPool(20) { SlashArcEntity() }
    val lightningArcs = ObjectPool(48) { LightningArcEntity() }
    val particles = ObjectPool(120) { ParticleEntity() }
    val damagePopups = ObjectPool(40) { DamagePopupEntity() }
    val hazardZones = ObjectPool(18) { HazardZoneEntity() }
    val laserBeams = ObjectPool(8) { LaserBeamEntity() }
    val enemyBullets = ObjectPool(64) { EnemyBulletEntity() }

    // Non-allocating overlap buffer (mirrors OverlapCircleNonAlloc)
    private val overlapBuffer = IntArray(180)

    /**
     * Finds all active enemies intersecting a circle (cx, cy, radius) without allocating any collections.
     * Returns count written into overlapBuffer and invokes [onHit] for each hit enemy.
     */
    inline fun overlapEnemiesNonAlloc(
        cx: Float,
        cy: Float,
        radius: Float,
        onHit: (EnemyEntity) -> Unit
    ): Int {
        val count = collectOverlappingEnemyIndices(cx, cy, radius)
        for (i in 0 until count) {
            val enemy = enemies.getRaw(getOverlapRawIndex(i))
            if (enemy.isActive) {
                onHit(enemy)
            }
        }
        return count
    }

    fun collectOverlappingEnemyIndices(cx: Float, cy: Float, radius: Float): Int {
        var hitCount = 0
        val active = enemies.activeCount
        for (i in 0 until active) {
            val rawIdx = enemies.getActiveRawIndex(i)
            val enemy = enemies.getRaw(rawIdx)
            if (!enemy.isActive) continue
            val dx = enemy.x - cx
            val dy = enemy.y - cy
            val combined = radius + enemy.radius
            if (dx * dx + dy * dy <= combined * combined) {
                if (hitCount < overlapBuffer.size) {
                    overlapBuffer[hitCount++] = rawIdx
                }
            }
        }
        return hitCount
    }

    fun getOverlapRawIndex(slot: Int): Int = overlapBuffer[slot]

    fun compactAll() {
        enemies.compact()
        rhythmOrbs.compact()
        shockwaves.compact()
        projectiles.compact()
        bassMines.compact()
        slashArcs.compact()
        lightningArcs.compact()
        particles.compact()
        damagePopups.compact()
        hazardZones.compact()
        laserBeams.compact()
        enemyBullets.compact()
    }

    fun releaseAll() {
        enemies.releaseAll()
        rhythmOrbs.releaseAll()
        shockwaves.releaseAll()
        projectiles.releaseAll()
        bassMines.releaseAll()
        slashArcs.releaseAll()
        lightningArcs.releaseAll()
        particles.releaseAll()
        damagePopups.releaseAll()
        hazardZones.releaseAll()
        laserBeams.releaseAll()
        enemyBullets.releaseAll()
    }
}
