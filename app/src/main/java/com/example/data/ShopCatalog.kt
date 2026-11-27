package com.example.data

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AcidLime
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.HotMagenta
import com.example.ui.theme.IceWhite
import com.example.ui.theme.SolarGold
import com.example.ui.theme.SwarmViolet
import com.example.ui.theme.TankCrimson

enum class CurrencyType {
    FREE,
    COIN,
    CRYSTAL
}

enum class PlayerShapeId(
    val title: String,
    val subtitle: String,
    val currency: CurrencyType,
    val price: Int
) {
    APEX_ARROW(
        title = "Apeks Ok",
        subtitle = "Klasik keskin neon savaş uçağı",
        currency = CurrencyType.FREE,
        price = 0
    ),
    PRISM_DIAMOND(
        title = "Prizma Elmas",
        subtitle = "Dengeli çift kenarlı kristal çekirdek",
        currency = CurrencyType.COIN,
        price = 250
    ),
    NOVA_STAR(
        title = "Nova Yıldızı",
        subtitle = "4 köşeli süper parlak enerji yıldızı",
        currency = CurrencyType.COIN,
        price = 550
    ),
    VALKYRIE_WING(
        title = "Valkür Kanadı",
        subtitle = "Geniş çift kanatlı siber avcı",
        currency = CurrencyType.COIN,
        price = 950
    ),
    AEGIS_HEX(
        title = "Aegis Altıgen",
        subtitle = "Zırhlı altıgen enerji kalkanı",
        currency = CurrencyType.COIN,
        price = 1450
    ),
    CROWN_VORTEX(
        title = "Kozmik Girdap",
        subtitle = "Efsanevi 6 bıçaklı enerji prizması",
        currency = CurrencyType.CRYSTAL,
        price = 90
    )
}

enum class PlayerColorId(
    val title: String,
    val subtitle: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val currency: CurrencyType,
    val price: Int,
    val isAnimatedChroma: Boolean = false
) {
    ELECTRIC_CYAN(
        title = "Elektrik Turkuaz",
        subtitle = "Klasik siber enerji tonu",
        primaryColor = ElectricCyan,
        secondaryColor = Color(0xFF00A8CC),
        currency = CurrencyType.FREE,
        price = 0
    ),
    HOT_MAGENTA(
        title = "Neon Pembe",
        subtitle = "Yüksek voltajlı plazma tonu",
        primaryColor = HotMagenta,
        secondaryColor = Color(0xFFFF75B5),
        currency = CurrencyType.COIN,
        price = 220
    ),
    ACID_LIME(
        title = "Asit Limon",
        subtitle = "Radyoaktif neon parıltısı",
        primaryColor = AcidLime,
        secondaryColor = Color(0xFF38FF88),
        currency = CurrencyType.COIN,
        price = 380
    ),
    AMETHYST_PULSE(
        title = "Ametist Mor",
        subtitle = "Derin uzay rezonansı",
        primaryColor = SwarmViolet,
        secondaryColor = Color(0xFFD8B4FE),
        currency = CurrencyType.COIN,
        price = 560
    ),
    BLAZE_CORAL(
        title = "Alev Mercan",
        subtitle = "Aşırı ısınmış reaktör tonu",
        primaryColor = TankCrimson,
        secondaryColor = Color(0xFFFF9E44),
        currency = CurrencyType.COIN,
        price = 750
    ),
    SOLAR_GOLD(
        title = "Solar Altın",
        subtitle = "VIP şampiyon parıltısı",
        primaryColor = SolarGold,
        secondaryColor = Color(0xFFFFF176),
        currency = CurrencyType.CRYSTAL,
        price = 55
    ),
    HYPER_PLASMA(
        title = "Hiper Plazma",
        subtitle = "Saf beyaz çekirdek + çift neon hale",
        primaryColor = IceWhite,
        secondaryColor = ElectricCyan,
        currency = CurrencyType.CRYSTAL,
        price = 85
    ),
    PRISM_CHROMA(
        title = "Krom Gökkuşağı",
        subtitle = "Sürekli renk değiştiren efsanevi spektrum",
        primaryColor = ElectricCyan,
        secondaryColor = HotMagenta,
        currency = CurrencyType.CRYSTAL,
        price = 140,
        isAnimatedChroma = true
    )
}

enum class PlayerAuraId(
    val title: String,
    val subtitle: String,
    val currency: CurrencyType,
    val price: Int
) {
    PULSE_RING(
        title = "Nabız Halkası",
        subtitle = "Klasik darbeli enerji halesi",
        currency = CurrencyType.FREE,
        price = 0
    ),
    DOUBLE_ORBIT(
        title = "Çift Halka",
        subtitle = "İç içe dönen ikiz koruma halkası",
        currency = CurrencyType.COIN,
        price = 360
    ),
    SONIC_SPIKES(
        title = "Sonik Işınlar",
        subtitle = "Dört yöne yayılan enerji çizgileri",
        currency = CurrencyType.COIN,
        price = 720
    ),
    ROYAL_HALO(
        title = "Kraliyet Halesi",
        subtitle = "Altın yörünge parçacıklı VIP aura",
        currency = CurrencyType.CRYSTAL,
        price = 75
    )
}

/**
 * 6 Permanent RPG Meta-Upgrades automatically saved to DataStore.
 * Every upgrade noticeably strengthens the player in every run & raises Power Rating!
 */
enum class MetaUpgradeId(
    val title: String,
    val bonusDescription: String,
    val accentColor: Color,
    val baseCost: Int,
    val costStep: Int,
    val maxLevel: Int = 8,
    val powerPerLevel: Int = 25
) {
    ARMOR_CORE(
        title = "Zırh Çekirdeği",
        bonusDescription = "+20 Maks. Can / seviye",
        accentColor = AcidLime,
        baseCost = 120,
        costStep = 110,
        maxLevel = 12,
        powerPerLevel = 30
    ),
    AMP_OUTPUT(
        title = "Silah Gücü",
        bonusDescription = "+%12 Tüm Silah Hasarı / seviye",
        accentColor = HotMagenta,
        baseCost = 140,
        costStep = 125,
        maxLevel = 12,
        powerPerLevel = 35
    ),
    TURBO_DRIVE(
        title = "Turbo Motor",
        bonusDescription = "+%6 Hız ve Dash Yenilenme / seviye",
        accentColor = ElectricCyan,
        baseCost = 125,
        costStep = 115,
        maxLevel = 10,
        powerPerLevel = 28
    ),
    CRIT_CORE(
        title = "Kritik Çekirdek",
        bonusDescription = "+%6 Kritik Şansı (2x Hasar) / seviye",
        accentColor = SolarGold,
        baseCost = 150,
        costStep = 130,
        maxLevel = 10,
        powerPerLevel = 32
    ),
    MAGNET_FIELD(
        title = "Mıknatıs Bobini",
        bonusDescription = "+%18 XP ve Coin Çekim Menzili / seviye",
        accentColor = SwarmViolet,
        baseCost = 110,
        costStep = 100,
        maxLevel = 10,
        powerPerLevel = 22
    ),
    COIN_FREQ(
        title = "Hazine Avcısı",
        bonusDescription = "+%20 Coin ve XP Kazancı / seviye",
        accentColor = SolarGold,
        baseCost = 130,
        costStep = 120,
        maxLevel = 10,
        powerPerLevel = 24
    );

    fun costForNextLevel(currentLevel: Int): Int = baseCost + currentLevel * costStep
}

/**
 * Permanent Account Milestones / Quests tracked across runs and auto-saved!
 */
enum class AchievementId(
    val title: String,
    val description: String,
    val targetValue: Int,
    val rewardCoins: Int,
    val rewardCrystals: Int
) {
    FIRST_BLOOD(
        title = "İlk Avcı",
        description = "Toplam 25 düşman yok et",
        targetValue = 25,
        rewardCoins = 120,
        rewardCrystals = 10
    ),
    MISSION_HUNTER(
        title = "Görev Uzmanı",
        description = "Toplam 3 arena görevi tamamla",
        targetValue = 3,
        rewardCoins = 180,
        rewardCrystals = 15
    ),
    STAGE_CONQUEROR(
        title = "Bölüm Fatihi",
        description = "Bölüm 1'i tamamlayıp Bölüm 2'yi aç",
        targetValue = 2,
        rewardCoins = 260,
        rewardCrystals = 25
    ),
    COMBO_MASTER(
        title = "Kombo Makinesi",
        description = "Tek seferde 20x komboya ulaş",
        targetValue = 20,
        rewardCoins = 160,
        rewardCrystals = 15
    ),
    UPGRADE_ENGINEER(
        title = "Çelik Mühendis",
        description = "Toplam 3 kalıcı güçlendirme satın al",
        targetValue = 3,
        rewardCoins = 160,
        rewardCrystals = 20
    ),
    CYBER_LEGEND(
        title = "Arena Efsanesi",
        description = "Toplam 150 düşman yok et",
        targetValue = 150,
        rewardCoins = 420,
        rewardCrystals = 40
    );

    fun currentProgress(profile: PlayerProfile): Int = when (this) {
        FIRST_BLOOD, CYBER_LEGEND -> profile.totalKills
        MISSION_HUNTER -> profile.totalMissionsCompleted
        STAGE_CONQUEROR -> profile.highestUnlockedStage
        COMBO_MASTER -> profile.bestStats.bestCombo
        UPGRADE_ENGINEER -> profile.totalMetaUpgradeLevels
    }

    fun isCompleted(profile: PlayerProfile): Boolean = currentProgress(profile) >= targetValue
}

enum class StorePackCategory {
    FEATURED_BUNDLE,
    COIN_PACK,
    CRYSTAL_PACK,
    FREE_GIFT,
    COIN_EXCHANGE
}

data class StoreOfferPack(
    val id: String,
    val googlePlayProductId: String? = null, // Official Google Play Console INAPP consumable SKU ID
    val category: StorePackCategory,
    val title: String,
    val subtitle: String,
    val coinAmount: Int,
    val crystalAmount: Int,
    val priceLabel: String, // Fallback display label when Play Store localized price hasn't loaded yet
    val coinExchangeCost: Int = 0, // >0 only for COIN_EXCHANGE packs
    val badge: String? = null,
    val accentColor: Color
) {
    val isRealMoneyIap: Boolean get() = googlePlayProductId != null
}

object StoreOfferCatalog {
    val featuredBundle = StoreOfferPack(
        id = "bundle_synvex_super",
        googlePlayProductId = "synvex_bundle_super",
        category = StorePackCategory.FEATURED_BUNDLE,
        title = "SYNVEX SÜPER PAKET",
        subtitle = "Hızlı gelişim ve VIP tasarımlar için dev başlangıç kasası!",
        coinAmount = 1500,
        crystalAmount = 120,
        priceLabel = "₺149,99",
        badge = "%50 AVANTAJ",
        accentColor = HotMagenta
    )

    val freeGiftPack = StoreOfferPack(
        id = "gift_daily_supply",
        googlePlayProductId = null,
        category = StorePackCategory.FREE_GIFT,
        title = "Günlük Ücretsiz Destek Kasası",
        subtitle = "Her 24 saatte bir ücretsiz coin ve özel kristal desteği al!",
        coinAmount = 100,
        crystalAmount = 10,
        priceLabel = "ÜCRETSİZ",
        badge = "GÜNLÜK",
        accentColor = AcidLime
    )

    val coinPacks: List<StoreOfferPack> = listOf(
        StoreOfferPack(
            id = "coin_pouch",
            googlePlayProductId = "synvex_coin_450",
            category = StorePackCategory.COIN_PACK,
            title = "Çırak Kesesi",
            subtitle = "İlk kalıcı güçlendirmeler için ideal",
            coinAmount = 450,
            crystalAmount = 0,
            priceLabel = "₺49,99",
            badge = null,
            accentColor = SolarGold
        ),
        StoreOfferPack(
            id = "coin_chest",
            googlePlayProductId = "synvex_coin_1200",
            category = StorePackCategory.COIN_PACK,
            title = "Savaşçı Sandığı",
            subtitle = "+200 ekstra bonus coin dahil",
            coinAmount = 1200,
            crystalAmount = 0,
            priceLabel = "₺119,99",
            badge = "+200 BONUS",
            accentColor = SolarGold
        ),
        StoreOfferPack(
            id = "coin_vault",
            googlePlayProductId = "synvex_coin_3200",
            category = StorePackCategory.COIN_PACK,
            title = "Komutan Hazinesi",
            subtitle = "Tüm silah ve zırhları hızla yükselt",
            coinAmount = 3200,
            crystalAmount = 20,
            priceLabel = "₺249,99",
            badge = "POPÜLER",
            accentColor = SolarGold
        ),
        StoreOfferPack(
            id = "coin_legend",
            googlePlayProductId = "synvex_coin_7500",
            category = StorePackCategory.COIN_PACK,
            title = "Efsane Kasası",
            subtitle = "Maksimum seviye için dev altın stoğu",
            coinAmount = 7500,
            crystalAmount = 60,
            priceLabel = "₺499,99",
            badge = "EN İYİ DEĞER",
            accentColor = SolarGold
        )
    )

    val crystalPacks: List<StoreOfferPack> = listOf(
        StoreOfferPack(
            id = "crystal_spark",
            googlePlayProductId = "synvex_crystal_45",
            category = StorePackCategory.CRYSTAL_PACK,
            title = "Kristal Kıvılcımı",
            subtitle = "Özel renk ve aura açmak için",
            coinAmount = 0,
            crystalAmount = 45,
            priceLabel = "₺69,99",
            badge = "ÖZEL COİN",
            accentColor = HotMagenta
        ),
        StoreOfferPack(
            id = "crystal_plasma",
            googlePlayProductId = "synvex_crystal_120",
            category = StorePackCategory.CRYSTAL_PACK,
            title = "Plazma Demeti",
            subtitle = "VIP gemi ve krom renklerin kilidini aç",
            coinAmount = 250,
            crystalAmount = 120,
            priceLabel = "₺169,99",
            badge = "POPÜLER",
            accentColor = HotMagenta
        ),
        StoreOfferPack(
            id = "crystal_cosmic",
            googlePlayProductId = "synvex_crystal_300",
            category = StorePackCategory.CRYSTAL_PACK,
            title = "Kozmik Kasa",
            subtitle = "%30 ekstra özel Vex Kristal hediyeli",
            coinAmount = 600,
            crystalAmount = 300,
            priceLabel = "₺349,99",
            badge = "%30 BONUS",
            accentColor = SwarmViolet
        ),
        StoreOfferPack(
            id = "crystal_supernova",
            googlePlayProductId = "synvex_crystal_750",
            category = StorePackCategory.CRYSTAL_PACK,
            title = "Süpernova Hazinesi",
            subtitle = "Tüm efsanevi içerikler için VIP özel kasa",
            coinAmount = 1500,
            crystalAmount = 750,
            priceLabel = "₺699,99",
            badge = "VIP",
            accentColor = ElectricCyan
        )
    )

    val exchangePacks: List<StoreOfferPack> = listOf(
        StoreOfferPack(
            id = "exchange_25",
            googlePlayProductId = null,
            category = StorePackCategory.COIN_EXCHANGE,
            title = "25 Özel Vex Kristal",
            subtitle = "Oyun içi Coin ile anında özel kristale çevir",
            coinAmount = 0,
            crystalAmount = 25,
            priceLabel = "450 Coin",
            coinExchangeCost = 450,
            badge = "TAKAS",
            accentColor = ElectricCyan
        ),
        StoreOfferPack(
            id = "exchange_70",
            googlePlayProductId = null,
            category = StorePackCategory.COIN_EXCHANGE,
            title = "70 Özel Vex Kristal",
            subtitle = "%15 daha avantajlı Coin → Kristal dönüşümü",
            coinAmount = 0,
            crystalAmount = 70,
            priceLabel = "1100 Coin",
            coinExchangeCost = 1100,
            badge = "AVANTAJLI",
            accentColor = ElectricCyan
        )
    )

    val realMoneyPacks: List<StoreOfferPack> = buildList {
        add(featuredBundle)
        addAll(coinPacks)
        addAll(crystalPacks)
    }

    val allGooglePlayProductIds: List<String> = realMoneyPacks.mapNotNull { it.googlePlayProductId }

    fun findByProductId(productId: String): StoreOfferPack? {
        return realMoneyPacks.firstOrNull { it.googlePlayProductId == productId }
    }
}

data class VipSubscriptionPlan(
    val id: String,
    val googlePlayProductId: String, // Official Google Play Console SUBS product ID
    val title: String,
    val subtitle: String,
    val periodLabel: String,
    val priceLabel: String, // Fallback display price until Google Play localized price loads
    val dailyCoins: Int,
    val dailyCrystals: Int,
    val megaDay7Coins: Int,
    val megaDay7Crystals: Int,
    val welcomeCoins: Int,
    val welcomeCrystals: Int,
    val coinXpBoostPercent: Int,
    val bonusMaxHp: Int,
    val badge: String,
    val accentColor: Color
)

object VipSubscriptionCatalog {
    val vipMonthly = VipSubscriptionPlan(
        id = "vip_monthly",
        googlePlayProductId = "synvex_vip_monthly",
        title = "SYNVEX VIP PASS",
        subtitle = "Her gün 400 Sarı Coin + 10 Kırmızı Kristal, hoş geldin kasası ve VIP güçlendirmeler!",
        periodLabel = "Aylık Abonelik",
        priceLabel = "₺129,99 / Ay",
        dailyCoins = 400,
        dailyCrystals = 10,
        megaDay7Coins = 800,
        megaDay7Crystals = 25,
        welcomeCoins = 1000,
        welcomeCrystals = 35,
        coinXpBoostPercent = 25,
        bonusMaxHp = 20,
        badge = "POPÜLER VIP",
        accentColor = SolarGold
    )

    val vipElite = VipSubscriptionPlan(
        id = "vip_elite",
        googlePlayProductId = "synvex_vip_elite",
        title = "SYNVEX ELİT PASS",
        subtitle = "En yüksek günlük 650 Sarı Coin + 18 Kırmızı Kristal ve dev %40 arena kazancı!",
        periodLabel = "3 Aylık Abonelik",
        priceLabel = "₺299,99 / 3 Ay",
        dailyCoins = 650,
        dailyCrystals = 18,
        megaDay7Coins = 1300,
        megaDay7Crystals = 40,
        welcomeCoins = 2500,
        welcomeCrystals = 85,
        coinXpBoostPercent = 40,
        bonusMaxHp = 35,
        badge = "%25 AVANTAJ",
        accentColor = HotMagenta
    )

    val plans: List<VipSubscriptionPlan> = listOf(vipMonthly, vipElite)

    val allGooglePlaySubIds: List<String> = plans.map { it.googlePlayProductId }

    fun findById(id: String?): VipSubscriptionPlan? {
        if (id.isNullOrBlank()) return null
        return plans.firstOrNull { it.id == id }
    }

    fun findByProductId(productId: String?): VipSubscriptionPlan? {
        if (productId.isNullOrBlank()) return null
        return plans.firstOrNull { it.googlePlayProductId == productId }
    }
}

data class CrystalPackOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val crystalAmount: Int,
    val coinCost: Int,
    val badge: String
)

object CrystalStoreCatalog {
    val packs = listOf(
        CrystalPackOption(
            id = "exchange_small",
            title = "25 Vex Kristal",
            subtitle = "Coin ile anında dönüştür",
            crystalAmount = 25,
            coinCost = 200,
            badge = "TAKAS"
        ),
        CrystalPackOption(
            id = "exchange_medium",
            title = "65 Vex Kristal",
            subtitle = "Ekstra %15 bonuslu kristal paketi",
            crystalAmount = 65,
            coinCost = 450,
            badge = "POPÜLER"
        ),
        CrystalPackOption(
            id = "bonus_vault",
            title = "Başlangıç Kasası (+100)",
            subtitle = "Özel tasarım ve güçleri denemen için hediye kristal",
            crystalAmount = 100,
            coinCost = 0,
            badge = "HEDİYE"
        )
    )
}
