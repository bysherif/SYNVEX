package com.example.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.security.MessageDigest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.synvexPrefs by preferencesDataStore(name = "synvex_profile_prefs")

data class LocalBestStats(
    val bestSurvivalSeconds: Int = 0,
    val bestKills: Int = 0,
    val bestCombo: Int = 0
)

data class VipDailyClaimResult(
    val coins: Int,
    val crystals: Int,
    val streakDay: Int,
    val isMegaBonus: Boolean
)

data class PlayerProfile(
    val coins: Int = 220,
    val crystals: Int = 35,
    val equippedShape: PlayerShapeId = PlayerShapeId.APEX_ARROW,
    val equippedColor: PlayerColorId = PlayerColorId.ELECTRIC_CYAN,
    val equippedAura: PlayerAuraId = PlayerAuraId.PULSE_RING,
    val unlockedShapes: Set<PlayerShapeId> = setOf(PlayerShapeId.APEX_ARROW),
    val unlockedColors: Set<PlayerColorId> = setOf(PlayerColorId.ELECTRIC_CYAN),
    val unlockedAuras: Set<PlayerAuraId> = setOf(PlayerAuraId.PULSE_RING),
    val metaLevels: Map<MetaUpgradeId, Int> = emptyMap(),
    val bestStats: LocalBestStats = LocalBestStats(),
    val highestUnlockedStage: Int = 1,
    val selectedStage: Int = 1,
    val totalMissionsCompleted: Int = 0,
    val totalKills: Int = 0,
    val totalRuns: Int = 0,
    val accountXp: Int = 0,
    val claimedAchievements: Set<AchievementId> = emptySet(),
    val lastFreeGiftEpochMs: Long = 0L,
    val activeVipPlanId: String? = null,
    val vipAutoRenewing: Boolean = false,
    val lastVipDailyClaimEpochMs: Long = 0L,
    val vipClaimStreak: Int = 0
) {
    val activeVipPlan: VipSubscriptionPlan?
        get() = VipSubscriptionCatalog.findById(activeVipPlanId)

    val isVipSubscribed: Boolean
        get() = activeVipPlan != null

    val vipCoinXpMultiplierBonus: Float
        get() = (activeVipPlan?.coinXpBoostPercent ?: 0) / 100f

    val vipBonusMaxHp: Float
        get() = (activeVipPlan?.bonusMaxHp ?: 0).toFloat()

    val vipStreakCycleDay: Int
        get() = vipClaimStreak % 7

    val nextVipClaimIsMegaBonus: Boolean
        get() = (vipClaimStreak % 7) == 6

    fun canClaimDailyGift(nowMs: Long = System.currentTimeMillis()): Boolean {
        return lastFreeGiftEpochMs == 0L || (nowMs - lastFreeGiftEpochMs) >= DAILY_GIFT_COOLDOWN_MS
    }

    fun remainingDailyGiftMs(nowMs: Long = System.currentTimeMillis()): Long {
        if (lastFreeGiftEpochMs == 0L) return 0L
        return (DAILY_GIFT_COOLDOWN_MS - (nowMs - lastFreeGiftEpochMs)).coerceAtLeast(0L)
    }

    fun canClaimVipDailyReward(nowMs: Long = System.currentTimeMillis()): Boolean {
        if (!isVipSubscribed) return false
        return lastVipDailyClaimEpochMs == 0L || (nowMs - lastVipDailyClaimEpochMs) >= DAILY_GIFT_COOLDOWN_MS
    }

    fun remainingVipDailyMs(nowMs: Long = System.currentTimeMillis()): Long {
        if (!isVipSubscribed || lastVipDailyClaimEpochMs == 0L) return 0L
        return (DAILY_GIFT_COOLDOWN_MS - (nowMs - lastVipDailyClaimEpochMs)).coerceAtLeast(0L)
    }

    fun claimableStoreGiftsCount(nowMs: Long = System.currentTimeMillis()): Int {
        var count = 0
        if (canClaimDailyGift(nowMs)) count++
        if (canClaimVipDailyReward(nowMs)) count++
        return count
    }

    companion object {
        const val DAILY_GIFT_COOLDOWN_MS: Long = 24L * 60L * 60L * 1000L
    }
    fun metaLevel(id: MetaUpgradeId): Int = metaLevels[id] ?: 0

    val totalMetaUpgradeLevels: Int
        get() = metaLevels.values.sum()

    val accountLevel: Int
        get() = (1 + (accountXp / 180)).coerceAtLeast(1)

    val accountXpProgress: Float
        get() = ((accountXp % 180) / 180f).coerceIn(0f, 1f)

    val rankTitle: String
        get() = when {
            accountLevel >= 12 -> "Efsane"
            accountLevel >= 8 -> "Komutan"
            accountLevel >= 5 -> "Siber Avcı"
            accountLevel >= 3 -> "Savaşçı"
            else -> "Acemi"
        }

    val powerRating: Int
        get() {
            val metaPower = MetaUpgradeId.entries.sumOf { id -> metaLevel(id) * id.powerPerLevel }
            return 100 + metaPower + (accountLevel - 1) * 18 + (highestUnlockedStage - 1) * 45
        }

    val claimableAchievementsCount: Int
        get() = AchievementId.entries.count { it.isCompleted(this) && !claimedAchievements.contains(it) }

    val affordableUpgradesCount: Int
        get() = MetaUpgradeId.entries.count { id ->
            val lv = metaLevel(id)
            lv < id.maxLevel && coins >= id.costForNextLevel(lv)
        }
}

class HighScoreStore(private val context: Context) {
    private val keyBestSeconds = intPreferencesKey("best_survival_seconds")
    private val keyBestKills = intPreferencesKey("best_kills")
    private val keyBestCombo = intPreferencesKey("best_combo")

    private val keyCoins = intPreferencesKey("wallet_coins")
    private val keyCrystals = intPreferencesKey("wallet_crystals")
    private val keyEquippedShape = stringPreferencesKey("equipped_shape")
    private val keyEquippedColor = stringPreferencesKey("equipped_color")
    private val keyEquippedAura = stringPreferencesKey("equipped_aura")
    private val keyUnlockedShapes = stringSetPreferencesKey("unlocked_shapes")
    private val keyUnlockedColors = stringSetPreferencesKey("unlocked_colors")
    private val keyUnlockedAuras = stringSetPreferencesKey("unlocked_auras")
    private val keyHighestStage = intPreferencesKey("highest_unlocked_stage")
    private val keySelectedStage = intPreferencesKey("selected_stage")
    private val keyTotalMissions = intPreferencesKey("total_missions_completed")
    private val keyTotalKills = intPreferencesKey("total_kills")
    private val keyTotalRuns = intPreferencesKey("total_runs")
    private val keyAccountXp = intPreferencesKey("account_xp")
    private val keyClaimedAchievements = stringSetPreferencesKey("claimed_achievements")
    private val keyLastFreeGiftMs = longPreferencesKey("last_free_gift_epoch_ms")
    private val keyProcessedPlayTokens = stringSetPreferencesKey("processed_play_purchase_tokens")
    private val keyActiveVipPlanId = stringPreferencesKey("active_vip_plan_id")
    private val keyVipAutoRenewing = booleanPreferencesKey("vip_auto_renewing")
    private val keyLastVipDailyClaimMs = longPreferencesKey("last_vip_daily_claim_epoch_ms")
    private val keyVipClaimStreak = intPreferencesKey("vip_claim_streak")

    private fun metaKey(id: MetaUpgradeId) = intPreferencesKey("meta_${id.name}")

    val profileFlow: Flow<PlayerProfile> = context.synvexPrefs.data.map { prefs ->
        val shapes = (prefs[keyUnlockedShapes] ?: setOf(PlayerShapeId.APEX_ARROW.name))
            .mapNotNull { runCatching { PlayerShapeId.valueOf(it) }.getOrNull() }
            .toMutableSet()
            .apply { add(PlayerShapeId.APEX_ARROW) }

        val colors = (prefs[keyUnlockedColors] ?: setOf(PlayerColorId.ELECTRIC_CYAN.name))
            .mapNotNull { runCatching { PlayerColorId.valueOf(it) }.getOrNull() }
            .toMutableSet()
            .apply { add(PlayerColorId.ELECTRIC_CYAN) }

        val auras = (prefs[keyUnlockedAuras] ?: setOf(PlayerAuraId.PULSE_RING.name))
            .mapNotNull { runCatching { PlayerAuraId.valueOf(it) }.getOrNull() }
            .toMutableSet()
            .apply { add(PlayerAuraId.PULSE_RING) }

        val claimed = (prefs[keyClaimedAchievements] ?: emptySet())
            .mapNotNull { runCatching { AchievementId.valueOf(it) }.getOrNull() }
            .toSet()

        val equippedShape = prefs[keyEquippedShape]
            ?.let { runCatching { PlayerShapeId.valueOf(it) }.getOrNull() }
            ?.takeIf { shapes.contains(it) }
            ?: PlayerShapeId.APEX_ARROW

        val equippedColor = prefs[keyEquippedColor]
            ?.let { runCatching { PlayerColorId.valueOf(it) }.getOrNull() }
            ?.takeIf { colors.contains(it) }
            ?: PlayerColorId.ELECTRIC_CYAN

        val equippedAura = prefs[keyEquippedAura]
            ?.let { runCatching { PlayerAuraId.valueOf(it) }.getOrNull() }
            ?.takeIf { auras.contains(it) }
            ?: PlayerAuraId.PULSE_RING

        val metas = MetaUpgradeId.entries.associateWith { id -> prefs[metaKey(id)] ?: 0 }

        val highestStage = (prefs[keyHighestStage] ?: 1).coerceIn(1, StageCatalog.stages.size)
        val selectedStage = (prefs[keySelectedStage] ?: highestStage).coerceIn(1, highestStage)

        PlayerProfile(
            coins = prefs[keyCoins] ?: 220,
            crystals = prefs[keyCrystals] ?: 35,
            equippedShape = equippedShape,
            equippedColor = equippedColor,
            equippedAura = equippedAura,
            unlockedShapes = shapes,
            unlockedColors = colors,
            unlockedAuras = auras,
            metaLevels = metas,
            bestStats = LocalBestStats(
                bestSurvivalSeconds = prefs[keyBestSeconds] ?: 0,
                bestKills = prefs[keyBestKills] ?: 0,
                bestCombo = prefs[keyBestCombo] ?: 0
            ),
            highestUnlockedStage = highestStage,
            selectedStage = selectedStage,
            totalMissionsCompleted = prefs[keyTotalMissions] ?: 0,
            totalKills = prefs[keyTotalKills] ?: 0,
            totalRuns = prefs[keyTotalRuns] ?: 0,
            accountXp = prefs[keyAccountXp] ?: 0,
            claimedAchievements = claimed,
            lastFreeGiftEpochMs = prefs[keyLastFreeGiftMs] ?: 0L,
            activeVipPlanId = prefs[keyActiveVipPlanId]?.takeIf { it.isNotBlank() },
            vipAutoRenewing = prefs[keyVipAutoRenewing] ?: false,
            lastVipDailyClaimEpochMs = prefs[keyLastVipDailyClaimMs] ?: 0L,
            vipClaimStreak = prefs[keyVipClaimStreak] ?: 0
        )
    }

    suspend fun selectStage(stageNumber: Int) {
        context.synvexPrefs.edit { prefs ->
            val highest = (prefs[keyHighestStage] ?: 1).coerceIn(1, StageCatalog.stages.size)
            if (stageNumber in 1..highest) {
                prefs[keySelectedStage] = stageNumber
            }
        }
    }

    /**
     * Live incremental auto-save called after EVERY mission completion, pause-exit, or run finish
     * so player progress, coins, crystals, kills, and account XP are NEVER lost!
     */
    suspend fun saveProgressDelta(
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
    ) {
        context.synvexPrefs.edit { prefs ->
            val prevSec = prefs[keyBestSeconds] ?: 0
            val prevKills = prefs[keyBestKills] ?: 0
            val prevCombo = prefs[keyBestCombo] ?: 0
            if (survivalSeconds > prevSec) prefs[keyBestSeconds] = survivalSeconds
            if (runKills > prevKills) prefs[keyBestKills] = runKills
            if (maxCombo > prevCombo) prefs[keyBestCombo] = maxCombo

            val curCoins = prefs[keyCoins] ?: 220
            val curCrystals = prefs[keyCrystals] ?: 35
            prefs[keyCoins] = curCoins + deltaCoins.coerceAtLeast(0)
            prefs[keyCrystals] = curCrystals + deltaCrystals.coerceAtLeast(0)

            val prevMissions = prefs[keyTotalMissions] ?: 0
            prefs[keyTotalMissions] = prevMissions + deltaMissions.coerceAtLeast(0)

            val prevTotalKills = prefs[keyTotalKills] ?: 0
            prefs[keyTotalKills] = prevTotalKills + deltaKills.coerceAtLeast(0)

            val xpGain = deltaKills.coerceAtLeast(0) * 3 +
                deltaMissions.coerceAtLeast(0) * 35 +
                if (victory) 120 else 0
            val prevAccountXp = prefs[keyAccountXp] ?: 0
            prefs[keyAccountXp] = prevAccountXp + xpGain

            if (isRunFinished) {
                prefs[keyTotalRuns] = (prefs[keyTotalRuns] ?: 0) + 1
            }

            if (victory) {
                val prevHighest = prefs[keyHighestStage] ?: 1
                val nextStage = (stageNumber + 1).coerceAtMost(StageCatalog.stages.size)
                if (nextStage > prevHighest) {
                    prefs[keyHighestStage] = nextStage
                }
                prefs[keySelectedStage] = nextStage
            }
        }
    }

    suspend fun claimAchievement(id: AchievementId): Boolean {
        var success = false
        context.synvexPrefs.edit { prefs ->
            val claimed = (prefs[keyClaimedAchievements] ?: emptySet()).toMutableSet()
            if (claimed.contains(id.name)) return@edit

            val totalKills = prefs[keyTotalKills] ?: 0
            val totalMissions = prefs[keyTotalMissions] ?: 0
            val highestStage = prefs[keyHighestStage] ?: 1
            val bestCombo = prefs[keyBestCombo] ?: 0
            val totalMetaLevels = MetaUpgradeId.entries.sumOf { prefs[metaKey(it)] ?: 0 }

            val current = when (id) {
                AchievementId.FIRST_BLOOD, AchievementId.CYBER_LEGEND -> totalKills
                AchievementId.MISSION_HUNTER -> totalMissions
                AchievementId.STAGE_CONQUEROR -> highestStage
                AchievementId.COMBO_MASTER -> bestCombo
                AchievementId.UPGRADE_ENGINEER -> totalMetaLevels
            }

            if (current >= id.targetValue) {
                claimed.add(id.name)
                prefs[keyClaimedAchievements] = claimed
                prefs[keyCoins] = (prefs[keyCoins] ?: 220) + id.rewardCoins
                prefs[keyCrystals] = (prefs[keyCrystals] ?: 35) + id.rewardCrystals
                prefs[keyAccountXp] = (prefs[keyAccountXp] ?: 0) + 60
                success = true
            }
        }
        return success
    }

    suspend fun buyAndEquipShape(shape: PlayerShapeId): Boolean {
        var success = false
        context.synvexPrefs.edit { prefs ->
            val unlocked = (prefs[keyUnlockedShapes] ?: setOf(PlayerShapeId.APEX_ARROW.name)).toMutableSet()
            if (unlocked.contains(shape.name)) {
                prefs[keyEquippedShape] = shape.name
                success = true
                return@edit
            }
            val coins = prefs[keyCoins] ?: 220
            val crystals = prefs[keyCrystals] ?: 35
            when (shape.currency) {
                CurrencyType.FREE -> {
                    unlocked.add(shape.name)
                    prefs[keyUnlockedShapes] = unlocked
                    prefs[keyEquippedShape] = shape.name
                    success = true
                }
                CurrencyType.COIN -> if (coins >= shape.price) {
                    prefs[keyCoins] = coins - shape.price
                    unlocked.add(shape.name)
                    prefs[keyUnlockedShapes] = unlocked
                    prefs[keyEquippedShape] = shape.name
                    success = true
                }
                CurrencyType.CRYSTAL -> if (crystals >= shape.price) {
                    prefs[keyCrystals] = crystals - shape.price
                    unlocked.add(shape.name)
                    prefs[keyUnlockedShapes] = unlocked
                    prefs[keyEquippedShape] = shape.name
                    success = true
                }
            }
        }
        return success
    }

    suspend fun buyAndEquipColor(color: PlayerColorId): Boolean {
        var success = false
        context.synvexPrefs.edit { prefs ->
            val unlocked = (prefs[keyUnlockedColors] ?: setOf(PlayerColorId.ELECTRIC_CYAN.name)).toMutableSet()
            if (unlocked.contains(color.name)) {
                prefs[keyEquippedColor] = color.name
                success = true
                return@edit
            }
            val coins = prefs[keyCoins] ?: 220
            val crystals = prefs[keyCrystals] ?: 35
            when (color.currency) {
                CurrencyType.FREE -> {
                    unlocked.add(color.name)
                    prefs[keyUnlockedColors] = unlocked
                    prefs[keyEquippedColor] = color.name
                    success = true
                }
                CurrencyType.COIN -> if (coins >= color.price) {
                    prefs[keyCoins] = coins - color.price
                    unlocked.add(color.name)
                    prefs[keyUnlockedColors] = unlocked
                    prefs[keyEquippedColor] = color.name
                    success = true
                }
                CurrencyType.CRYSTAL -> if (crystals >= color.price) {
                    prefs[keyCrystals] = crystals - color.price
                    unlocked.add(color.name)
                    prefs[keyUnlockedColors] = unlocked
                    prefs[keyEquippedColor] = color.name
                    success = true
                }
            }
        }
        return success
    }

    suspend fun buyAndEquipAura(aura: PlayerAuraId): Boolean {
        var success = false
        context.synvexPrefs.edit { prefs ->
            val unlocked = (prefs[keyUnlockedAuras] ?: setOf(PlayerAuraId.PULSE_RING.name)).toMutableSet()
            if (unlocked.contains(aura.name)) {
                prefs[keyEquippedAura] = aura.name
                success = true
                return@edit
            }
            val coins = prefs[keyCoins] ?: 220
            val crystals = prefs[keyCrystals] ?: 35
            when (aura.currency) {
                CurrencyType.FREE -> {
                    unlocked.add(aura.name)
                    prefs[keyUnlockedAuras] = unlocked
                    prefs[keyEquippedAura] = aura.name
                    success = true
                }
                CurrencyType.COIN -> if (coins >= aura.price) {
                    prefs[keyCoins] = coins - aura.price
                    unlocked.add(aura.name)
                    prefs[keyUnlockedAuras] = unlocked
                    prefs[keyEquippedAura] = aura.name
                    success = true
                }
                CurrencyType.CRYSTAL -> if (crystals >= aura.price) {
                    prefs[keyCrystals] = crystals - aura.price
                    unlocked.add(aura.name)
                    prefs[keyUnlockedAuras] = unlocked
                    prefs[keyEquippedAura] = aura.name
                    success = true
                }
            }
        }
        return success
    }

    suspend fun buyMetaUpgrade(id: MetaUpgradeId): Boolean {
        var success = false
        context.synvexPrefs.edit { prefs ->
            val lv = prefs[metaKey(id)] ?: 0
            if (lv >= id.maxLevel) return@edit
            val cost = id.costForNextLevel(lv)
            val coins = prefs[keyCoins] ?: 220
            if (coins >= cost) {
                prefs[keyCoins] = coins - cost
                prefs[metaKey(id)] = lv + 1
                prefs[keyAccountXp] = (prefs[keyAccountXp] ?: 0) + 25
                success = true
            }
        }
        return success
    }

    suspend fun claimCrystalPack(pack: CrystalPackOption): Boolean {
        var success = false
        context.synvexPrefs.edit { prefs ->
            val coins = prefs[keyCoins] ?: 220
            val crystals = prefs[keyCrystals] ?: 35
            if (pack.coinCost == 0) {
                prefs[keyCrystals] = crystals + pack.crystalAmount
                success = true
            } else if (coins >= pack.coinCost) {
                prefs[keyCoins] = coins - pack.coinCost
                prefs[keyCrystals] = crystals + pack.crystalAmount
                success = true
            }
        }
        return success
    }

    /**
     * Grants entitlement ONLY for a genuine Google Play Billing verified purchase.
     * Uses SHA-256 purchaseToken hashing in DataStore to guarantee idempotency (no replay or double credit).
     */
    suspend fun recordVerifiedPlayPurchase(
        purchaseToken: String,
        pack: StoreOfferPack,
        quantity: Int = 1
    ): Boolean {
        if (purchaseToken.isBlank()) return false
        val safeQty = quantity.coerceIn(1, 20)
        val tokenHash = sha256Hex(purchaseToken)
        var credited = false
        context.synvexPrefs.edit { prefs ->
            val seen = (prefs[keyProcessedPlayTokens] ?: emptySet()).toMutableSet()
            if (seen.contains(tokenHash)) {
                return@edit
            }
            seen.add(tokenHash)
            prefs[keyProcessedPlayTokens] = seen
            val coins = prefs[keyCoins] ?: 220
            val crystals = prefs[keyCrystals] ?: 35
            prefs[keyCoins] = coins + (pack.coinAmount.coerceAtLeast(0) * safeQty)
            prefs[keyCrystals] = crystals + (pack.crystalAmount.coerceAtLeast(0) * safeQty)
            prefs[keyAccountXp] = (prefs[keyAccountXp] ?: 0) + (25 * safeQty)
            credited = true
        }
        return credited
    }

    /**
     * Claims the daily free arena supply pack with a real persisted 24-hour cooldown.
     */
    suspend fun claimDailyFreeGift(pack: StoreOfferPack, nowMs: Long = System.currentTimeMillis()): Boolean {
        if (pack.category != StorePackCategory.FREE_GIFT) return false
        var claimed = false
        context.synvexPrefs.edit { prefs ->
            val lastMs = prefs[keyLastFreeGiftMs] ?: 0L
            if (lastMs == 0L || (nowMs - lastMs) >= PlayerProfile.DAILY_GIFT_COOLDOWN_MS) {
                prefs[keyLastFreeGiftMs] = nowMs
                prefs[keyCoins] = (prefs[keyCoins] ?: 220) + pack.coinAmount.coerceAtLeast(0)
                prefs[keyCrystals] = (prefs[keyCrystals] ?: 35) + pack.crystalAmount.coerceAtLeast(0)
                prefs[keyAccountXp] = (prefs[keyAccountXp] ?: 0) + 15
                claimed = true
            }
        }
        return claimed
    }

    /**
     * Converts earned in-game Coins into Special Vex Crystals (strictly checks sufficient Coin balance).
     */
    suspend fun exchangeCoinsForCrystals(pack: StoreOfferPack): Boolean {
        if (pack.category != StorePackCategory.COIN_EXCHANGE || pack.coinExchangeCost <= 0) return false
        var exchanged = false
        context.synvexPrefs.edit { prefs ->
            val coins = prefs[keyCoins] ?: 220
            if (coins >= pack.coinExchangeCost) {
                prefs[keyCoins] = coins - pack.coinExchangeCost
                prefs[keyCrystals] = (prefs[keyCrystals] ?: 35) + pack.crystalAmount.coerceAtLeast(0)
                exchanged = true
            }
        }
        return exchanged
    }

    /**
     * Synchronizes active Google Play Subscription entitlement.
     * Grants the one-time Welcome Bonus (Hoş Geldin Hediyesi) idempotently per purchaseToken.
     */
    suspend fun syncVerifiedSubscription(
        plan: VipSubscriptionPlan?,
        purchaseToken: String?,
        isAutoRenewing: Boolean
    ): Boolean {
        var grantedWelcomeBonus = false
        context.synvexPrefs.edit { prefs ->
            if (plan == null) {
                prefs.remove(keyActiveVipPlanId)
                prefs[keyVipAutoRenewing] = false
                return@edit
            }
            prefs[keyActiveVipPlanId] = plan.id
            prefs[keyVipAutoRenewing] = isAutoRenewing

            if (!purchaseToken.isNullOrBlank()) {
                val tokenHash = sha256Hex("sub_${plan.googlePlayProductId}_$purchaseToken")
                val seen = (prefs[keyProcessedPlayTokens] ?: emptySet()).toMutableSet()
                if (!seen.contains(tokenHash)) {
                    seen.add(tokenHash)
                    prefs[keyProcessedPlayTokens] = seen
                    prefs[keyCoins] = (prefs[keyCoins] ?: 220) + plan.welcomeCoins
                    prefs[keyCrystals] = (prefs[keyCrystals] ?: 35) + plan.welcomeCrystals
                    prefs[keyAccountXp] = (prefs[keyAccountXp] ?: 0) + 80
                    grantedWelcomeBonus = true
                }
            }
        }
        return grantedWelcomeBonus
    }

    /**
     * Claims the active VIP subscriber's daily reward (e.g. +400 Yellow Coin & +10 Red Crystal)
     * with a persisted 24-hour cooldown and a 7th-day 2x Mega Loyalty Chest!
     */
    suspend fun claimVipDailyReward(nowMs: Long = System.currentTimeMillis()): VipDailyClaimResult? {
        var result: VipDailyClaimResult? = null
        context.synvexPrefs.edit { prefs ->
            val plan = VipSubscriptionCatalog.findById(prefs[keyActiveVipPlanId]) ?: return@edit
            val lastMs = prefs[keyLastVipDailyClaimMs] ?: 0L
            if (lastMs != 0L && (nowMs - lastMs) < PlayerProfile.DAILY_GIFT_COOLDOWN_MS) {
                return@edit
            }

            val nextStreak = (prefs[keyVipClaimStreak] ?: 0) + 1
            val isMega = (nextStreak % 7) == 0
            val rewardCoins = if (isMega) plan.megaDay7Coins else plan.dailyCoins
            val rewardCrystals = if (isMega) plan.megaDay7Crystals else plan.dailyCrystals

            prefs[keyLastVipDailyClaimMs] = nowMs
            prefs[keyVipClaimStreak] = nextStreak
            prefs[keyCoins] = (prefs[keyCoins] ?: 220) + rewardCoins
            prefs[keyCrystals] = (prefs[keyCrystals] ?: 35) + rewardCrystals
            prefs[keyAccountXp] = (prefs[keyAccountXp] ?: 0) + if (isMega) 60 else 30

            result = VipDailyClaimResult(
                coins = rewardCoins,
                crystals = rewardCrystals,
                streakDay = if (isMega) 7 else (nextStreak % 7),
                isMegaBonus = isMega
            )
        }
        return result
    }

    private fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}
