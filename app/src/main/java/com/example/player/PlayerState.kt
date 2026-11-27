package com.example.player

import com.example.core.EquippedSlotInfo
import com.example.core.UpgradeCardOption
import com.example.data.MetaUpgradeId
import com.example.data.PassiveId
import com.example.data.PlayerProfile
import com.example.data.UpgradeCategory
import com.example.data.UpgradeConstants
import com.example.data.WeaponId
import com.example.ui.theme.SolarGold
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

/**
 * Floating virtual joystick state (screen-space coordinates for rendering + normalized vector).
 */
class FloatingJoystickState {
    var isActive: Boolean = false
    var originX: Float = 0f
    var originY: Float = 0f
    var currentX: Float = 0f
    var currentY: Float = 0f
    var dirX: Float = 0f
    var dirY: Float = 0f
    var magnitude: Float = 0f
    var maxRadiusPx: Float = 135f
    var hasEverMoved: Boolean = false

    fun onDown(x: Float, y: Float, maxRadius: Float) {
        isActive = true
        originX = x
        originY = y
        currentX = x
        currentY = y
        dirX = 0f
        dirY = 0f
        magnitude = 0f
        maxRadiusPx = maxRadius.coerceAtLeast(80f)
    }

    fun onDrag(x: Float, y: Float) {
        if (!isActive) return
        val dx = x - originX
        val dy = y - originY
        val dist = hypot(dx, dy)
        if (dist > 4f) {
            hasEverMoved = true
            val clamped = dist.coerceAtMost(maxRadiusPx)
            val inv = 1f / dist
            dirX = dx * inv
            dirY = dy * inv
            magnitude = (clamped / maxRadiusPx).coerceIn(0f, 1f)
            currentX = originX + dirX * clamped
            currentY = originY + dirY * clamped
        } else {
            dirX = 0f
            dirY = 0f
            magnitude = 0f
            currentX = originX
            currentY = originY
        }
    }

    fun onUp() {
        isActive = false
        dirX = 0f
        dirY = 0f
        magnitude = 0f
    }

    fun reset() {
        onUp()
        hasEverMoved = false
    }
}

class PlayerState {
    var x: Float = 0f
    var y: Float = 0f
    var facingAngle: Float = -PI.toFloat() / 2f
    var radius: Float = 20f

    var hp: Float = 100f
    var maxHp: Float = 100f
    var invulnerableTimer: Float = 0f
    var wallShockCooldown: Float = 0f
    var hitFlashTimer: Float = 0f

    // Active Tactical Dash (Kaçınma) state
    var dashCooldown: Float = 0f
    var dashMaxCooldown: Float = 2.8f
        private set
    var dashActiveTimer: Float = 0f
    private var dashDirX: Float = 0f
    private var dashDirY: Float = -1f

    // Active Overdrive Nova (Süper Patlama) charge (0..100)
    var novaCharge: Float = 35f
        private set

    var level: Int = 1
    var xp: Int = 0
    var xpToNext: Int = 55
    var unspentSkillPoints: Int = 0

    var kills: Int = 0
    var combo: Int = 0
    var maxCombo: Int = 0
    var comboDecayTimer: Float = 0f

    var runCoins: Int = 0
    var runCrystals: Int = 0

    // Permanent meta bonuses from Upgrade Garage
    private var metaDamageBonus: Float = 0f
    private var metaSpeedBonus: Float = 0f
    private var metaCritBonus: Float = 0f
    private var metaMagnetBonus: Float = 0f
    var metaCoinMultiplier: Float = 1f
        private set

    var pulseScale: Float = 1f
    var orbitAngle: Float = 0f

    val joystick = FloatingJoystickState()

    val weaponLevels = LinkedHashMap<WeaponId, Int>()
    val evolvedWeapons = HashSet<WeaponId>()
    val passiveLevels = LinkedHashMap<PassiveId, Int>()

    val isDashing: Boolean
        get() = dashActiveTimer > 0f

    val dashReadyFraction: Float
        get() = (1f - (dashCooldown / dashMaxCooldown.coerceAtLeast(0.5f))).coerceIn(0f, 1f)

    val novaChargeFraction: Float
        get() = (novaCharge / 100f).coerceIn(0f, 1f)

    val isNovaReady: Boolean
        get() = novaCharge >= 100f

    val critChance: Float
        get() = (0.06f + metaCritBonus + (combo.coerceAtMost(40) * 0.002f)).coerceAtMost(0.75f)

    val moveSpeed: Float
        get() {
            val tempoLv = passiveLevels[PassiveId.TEMPO_BOOTS] ?: 0
            return 250f * (1f + metaSpeedBonus) * (1f + tempoLv * 0.12f)
        }

    val magnetRadius: Float
        get() {
            val magLv = passiveLevels[PassiveId.MAGNET_COIL] ?: 0
            return 155f * (1f + metaMagnetBonus) * (1f + magLv * 0.35f)
        }

    val areaMultiplier: Float
        get() {
            val ampLv = passiveLevels[PassiveId.AMP_BOOSTER] ?: 0
            return 1f + ampLv * 0.18f
        }

    val damageMultiplier: Float
        get() {
            val dtLv = passiveLevels[PassiveId.DOUBLE_TIME] ?: 0
            val comboBonus = (combo.coerceAtMost(60) * 0.005f)
            return (1f + metaDamageBonus) * (1f + dtLv * 0.16f) * (1f + comboBonus)
        }

    val doubleTimeLevel: Int
        get() = passiveLevels[PassiveId.DOUBLE_TIME] ?: 0

    fun reset(profile: PlayerProfile = PlayerProfile()) {
        x = 0f
        y = 0f
        facingAngle = -PI.toFloat() / 2f
        val armorLv = profile.metaLevel(MetaUpgradeId.ARMOR_CORE)
        val ampLv = profile.metaLevel(MetaUpgradeId.AMP_OUTPUT)
        val turboLv = profile.metaLevel(MetaUpgradeId.TURBO_DRIVE)
        val critLv = profile.metaLevel(MetaUpgradeId.CRIT_CORE)
        val magLv = profile.metaLevel(MetaUpgradeId.MAGNET_FIELD)
        val coinLv = profile.metaLevel(MetaUpgradeId.COIN_FREQ)

        maxHp = 110f + armorLv * 20f + profile.vipBonusMaxHp
        hp = maxHp
        metaDamageBonus = ampLv * 0.12f
        metaSpeedBonus = turboLv * 0.06f
        metaCritBonus = critLv * 0.06f
        metaMagnetBonus = magLv * 0.18f
        metaCoinMultiplier = 1f + coinLv * 0.20f + profile.vipCoinXpMultiplierBonus

        dashMaxCooldown = (2.8f - turboLv * 0.16f).coerceAtLeast(1.4f)
        dashCooldown = 0f
        dashActiveTimer = 0f
        novaCharge = 35f

        invulnerableTimer = 0f
        wallShockCooldown = 0f
        hitFlashTimer = 0f
        level = 1
        xp = 0
        xpToNext = 50
        unspentSkillPoints = 0
        kills = 0
        combo = 0
        maxCombo = 0
        comboDecayTimer = 0f
        runCoins = 0
        runCrystals = 0
        pulseScale = 1f
        orbitAngle = 0f
        joystick.reset()
        weaponLevels.clear()
        evolvedWeapons.clear()
        passiveLevels.clear()
        // Action-packed starting arsenal: Shockwave + Homing Laser Bolts right from Second 1!
        weaponLevels[WeaponId.BEAT_SHOCKWAVE] = 1
        weaponLevels[WeaponId.LASER_ARP] = 1
    }

    /**
     * Triggers a high-speed invulnerable dodge burst in the joystick/facing direction!
     */
    fun triggerDash(): Boolean {
        if (dashCooldown > 0f || dashActiveTimer > 0f) return false
        if (joystick.isActive && joystick.magnitude > 0.1f) {
            dashDirX = joystick.dirX
            dashDirY = joystick.dirY
        } else {
            dashDirX = cos(facingAngle)
            dashDirY = sin(facingAngle)
        }
        dashActiveTimer = 0.20f
        dashCooldown = dashMaxCooldown
        invulnerableTimer = invulnerableTimer.coerceAtLeast(0.36f)
        return true
    }

    fun addNovaCharge(amount: Float) {
        novaCharge = (novaCharge + amount).coerceIn(0f, 100f)
    }

    fun consumeNova(): Boolean {
        if (!isNovaReady) return false
        novaCharge = 0f
        invulnerableTimer = invulnerableTimer.coerceAtLeast(0.55f)
        return true
    }

    fun update(dt: Float, beatProgress: Float) {
        if (invulnerableTimer > 0f) {
            invulnerableTimer = (invulnerableTimer - dt).coerceAtLeast(0f)
        }
        if (wallShockCooldown > 0f) {
            wallShockCooldown = (wallShockCooldown - dt).coerceAtLeast(0f)
        }
        if (hitFlashTimer > 0f) {
            hitFlashTimer = (hitFlashTimer - dt).coerceAtLeast(0f)
        }
        if (dashCooldown > 0f) {
            dashCooldown = (dashCooldown - dt).coerceAtLeast(0f)
        }

        if (dashActiveTimer > 0f) {
            dashActiveTimer = (dashActiveTimer - dt).coerceAtLeast(0f)
            val dashSpeed = 780f * (1f + metaSpeedBonus * 0.5f)
            x += dashDirX * dashSpeed * dt
            y += dashDirY * dashSpeed * dt
        } else if (joystick.isActive && joystick.magnitude > 0.02f) {
            val speed = moveSpeed * joystick.magnitude
            x += joystick.dirX * speed * dt
            y += joystick.dirY * speed * dt
            facingAngle = atan2(joystick.dirY, joystick.dirX)
        }

        val beatBounce = (1f - beatProgress).let { it * it }
        val moveAmp = if (isDashing) 0.22f else if (joystick.isActive && joystick.magnitude > 0.05f) 0.14f else 0.07f
        val smoothBreath = 0.025f * sin(beatProgress * 2f * PI.toFloat())
        pulseScale = 1f + beatBounce * moveAmp + smoothBreath

        val hasEvolvedOrbit = evolvedWeapons.contains(WeaponId.SYNTH_ORBIT)
        val orbitBaseSpeed = if (hasEvolvedOrbit) 4.4f else 2.6f
        val beatSurge = 1f + beatBounce * 0.45f
        orbitAngle = (orbitAngle + orbitBaseSpeed * beatSurge * dt) % (2f * PI.toFloat())

        if (combo > 0) {
            comboDecayTimer -= dt
            if (comboDecayTimer <= 0f) {
                combo = (combo - 2).coerceAtLeast(0)
                comboDecayTimer = if (combo > 0) 0.45f else 0f
            }
        }
    }

    fun registerKill(isElite: Boolean = false) {
        kills++
        combo++
        if (combo > maxCombo) maxCombo = combo
        comboDecayTimer = 3.8f
        addNovaCharge(if (isElite) 28f else 4.2f)
        if (isElite) {
            runCrystals += 15
            runCoins += (45 * metaCoinMultiplier).toInt()
        }
    }

    fun addCoins(baseAmount: Int) {
        runCoins += (baseAmount * metaCoinMultiplier).toInt().coerceAtLeast(1)
    }

    fun addCrystals(amount: Int) {
        runCrystals += amount.coerceAtLeast(0)
    }

    fun takeDamage(amount: Float): Boolean {
        if (invulnerableTimer > 0f) return false
        hp = (hp - amount).coerceAtLeast(0f)
        invulnerableTimer = 0.38f
        hitFlashTimer = 0.14f
        combo = (combo * 0.65f).toInt()
        return true
    }

    fun takeDirectHazardDamage(amount: Float, bypassIFrames: Boolean = false): Boolean {
        if (!bypassIFrames && invulnerableTimer > 0.14f) return false
        hp = (hp - amount).coerceAtLeast(0f)
        invulnerableTimer = 0.30f
        hitFlashTimer = 0.16f
        combo = (combo * 0.60f).toInt()
        return true
    }

    fun heal(amount: Float) {
        if (amount > 0f) {
            hp = (hp + amount).coerceAtMost(maxHp)
        }
    }

    fun addXp(amount: Int): Int {
        val scaledXp = (amount * (1f + (metaCoinMultiplier - 1f) * 0.5f)).toInt().coerceAtLeast(1)
        xp += scaledXp
        var levelsGained = 0
        while (xp >= xpToNext) {
            xp -= xpToNext
            level++
            unspentSkillPoints++
            levelsGained++
            xpToNext = (xpToNext * 1.25f + 18f).toInt()
            hp = (hp + maxHp * 0.08f).coerceAtMost(maxHp)
        }
        return levelsGained
    }

    fun buildAllUpgradeOptions(): List<UpgradeCardOption> {
        val list = mutableListOf<UpgradeCardOption>()

        for ((weapon, lv) in weaponLevels) {
            if (lv >= UpgradeConstants.MAX_WEAPON_LEVEL && !evolvedWeapons.contains(weapon)) {
                val passiveLv = passiveLevels[weapon.synergyPassive] ?: 0
                if (passiveLv >= 1) {
                    list.add(
                        UpgradeCardOption(
                            id = "evo_${weapon.name}",
                            category = UpgradeCategory.EVOLUTION_SYNERGY,
                            weaponId = weapon,
                            passiveId = weapon.synergyPassive,
                            title = weapon.evolutionTitle,
                            subtitle = weapon.evolutionDescription,
                            currentLevel = lv,
                            nextLevel = 6,
                            accentColor = SolarGold,
                            synergyHint = "${weapon.title} + ${weapon.synergyPassive.title}",
                            isMaxed = false
                        )
                    )
                }
            }
        }

        for (weapon in WeaponId.entries) {
            val currentLv = weaponLevels[weapon] ?: 0
            val evolved = evolvedWeapons.contains(weapon)
            val isMax = evolved || currentLv >= UpgradeConstants.MAX_WEAPON_LEVEL
            val desc = when {
                evolved -> weapon.evolutionDescription
                currentLv == 0 -> weapon.baseDescription
                else -> weapon.levelUpDescription
            }
            list.add(
                UpgradeCardOption(
                    id = "wpn_${weapon.name}",
                    category = UpgradeCategory.ACTIVE_WEAPON,
                    weaponId = weapon,
                    title = if (evolved) "★ ${weapon.evolutionTitle}" else weapon.title,
                    subtitle = desc,
                    currentLevel = currentLv,
                    nextLevel = (currentLv + 1).coerceAtMost(UpgradeConstants.MAX_WEAPON_LEVEL),
                    accentColor = if (evolved) SolarGold else weapon.accentColor,
                    synergyHint = if (evolved) {
                        "EVRİM AKTİF"
                    } else {
                        "Sinerji: ${weapon.synergyPassive.title} → ${weapon.evolutionTitle}"
                    },
                    isMaxed = isMax
                )
            )
        }

        for (passive in PassiveId.entries) {
            val currentLv = passiveLevels[passive] ?: 0
            val isMax = currentLv >= UpgradeConstants.MAX_PASSIVE_LEVEL
            val pairedWeapons = WeaponId.entries
                .filter { it.synergyPassive == passive }
                .joinToString(" / ") { it.shortTag }
            list.add(
                UpgradeCardOption(
                    id = "pas_${passive.name}",
                    category = UpgradeCategory.PASSIVE_BOOST,
                    passiveId = passive,
                    title = passive.title,
                    subtitle = passive.descriptionPerLevel,
                    currentLevel = currentLv,
                    nextLevel = (currentLv + 1).coerceAtMost(UpgradeConstants.MAX_PASSIVE_LEVEL),
                    accentColor = passive.accentColor,
                    synergyHint = "Sinerji Silahları: $pairedWeapons",
                    isMaxed = isMax
                )
            )
        }

        if (list.all { it.isMaxed }) {
            list.add(
                UpgradeCardOption(
                    id = "overdrive_heal",
                    category = UpgradeCategory.PASSIVE_BOOST,
                    title = "Tam Şarj Rezonansı",
                    subtitle = "Canı tamamen yeniler ve Maksimum Canı +25 artırır",
                    currentLevel = 5,
                    nextLevel = 6,
                    accentColor = SolarGold,
                    isMaxed = false
                )
            )
        }

        return list
    }

    fun generateUpgradeOptions(rng: Random = Random.Default): List<UpgradeCardOption> {
        val all = buildAllUpgradeOptions().filter { !it.isMaxed }
        val evos = all.filter { it.category == UpgradeCategory.EVOLUTION_SYNERGY }
        val rest = all.filter { it.category != UpgradeCategory.EVOLUTION_SYNERGY }.shuffled(rng)
        return (evos + rest).take(3)
    }

    fun applyUpgrade(option: UpgradeCardOption): Boolean {
        if (option.isMaxed) return false
        return when (option.category) {
            UpgradeCategory.EVOLUTION_SYNERGY -> {
                option.weaponId?.let { evolvedWeapons.add(it) }
                hp = (hp + maxHp * 0.25f).coerceAtMost(maxHp)
                true
            }
            UpgradeCategory.ACTIVE_WEAPON -> {
                val w = option.weaponId ?: return false
                weaponLevels[w] = ((weaponLevels[w] ?: 0) + 1).coerceAtMost(UpgradeConstants.MAX_WEAPON_LEVEL)
                false
            }
            UpgradeCategory.PASSIVE_BOOST -> {
                val p = option.passiveId
                if (p != null) {
                    passiveLevels[p] = ((passiveLevels[p] ?: 0) + 1).coerceAtMost(UpgradeConstants.MAX_PASSIVE_LEVEL)
                } else {
                    maxHp += 25f
                    hp = maxHp
                }
                false
            }
        }
    }

    fun buildEquippedWeaponsList(): List<EquippedSlotInfo> {
        return weaponLevels.map { (weapon, lv) ->
            val evolved = evolvedWeapons.contains(weapon)
            EquippedSlotInfo(
                shortTag = weapon.shortTag,
                level = lv,
                isEvolved = evolved,
                isPassive = false,
                color = if (evolved) SolarGold else weapon.accentColor
            )
        }
    }

    fun buildEquippedPassivesList(): List<EquippedSlotInfo> {
        return passiveLevels.map { (passive, lv) ->
            EquippedSlotInfo(
                shortTag = passive.shortTag,
                level = lv,
                isEvolved = false,
                isPassive = true,
                color = passive.accentColor
            )
        }
    }

    fun orbitBladePosition(index: Int, total: Int, radius: Float, out: FloatArray) {
        val angle = orbitAngle + (index.toFloat() / total.toFloat()) * 2f * PI.toFloat()
        out[0] = x + cos(angle) * radius
        out[1] = y + sin(angle) * radius
    }
}
