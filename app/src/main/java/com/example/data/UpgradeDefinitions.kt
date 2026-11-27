package com.example.data

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.AcidLime
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.HotMagenta
import com.example.ui.theme.SolarGold
import com.example.ui.theme.SwarmViolet
import com.example.ui.theme.TankCrimson

enum class UpgradeCategory {
    ACTIVE_WEAPON,
    PASSIVE_BOOST,
    EVOLUTION_SYNERGY
}

enum class PassiveId(
    val title: String,
    val shortTag: String,
    val descriptionPerLevel: String,
    val accentColor: Color
) {
    TEMPO_BOOTS(
        title = "Tempo Çizmeleri",
        shortTag = "HIZ",
        descriptionPerLevel = "Hareket hızını +%12 artırır",
        accentColor = ElectricCyan
    ),
    MAGNET_COIL(
        title = "Mıknatıs Bobini",
        shortTag = "MIKNATIS",
        descriptionPerLevel = "Ritim küresi çekim menzilini +%35 artırır",
        accentColor = AcidLime
    ),
    AMP_BOOSTER(
        title = "Amfi Güçlendirici",
        shortTag = "ALAN",
        descriptionPerLevel = "Tüm saldırı alanlarını +%18 büyütür",
        accentColor = HotMagenta
    ),
    DOUBLE_TIME(
        title = "Çift Zaman",
        shortTag = "BEKLEME",
        descriptionPerLevel = "Bekleme süresini azaltır ve hasarı +%16 artırır",
        accentColor = SolarGold
    )
}

enum class WeaponId(
    val title: String,
    val shortTag: String,
    val baseDescription: String,
    val levelUpDescription: String,
    val accentColor: Color,
    val synergyPassive: PassiveId,
    val evolutionTitle: String,
    val evolutionDescription: String
) {
    BEAT_SHOCKWAVE(
        title = "Bas Şok Dalgası",
        shortTag = "ŞOK",
        baseDescription = "Her 2 vuruşta bir etrafında neon şok dalgası yayar",
        levelUpDescription = "Dalga hasarını ve alan menzilini artırır",
        accentColor = ElectricCyan,
        synergyPassive = PassiveId.AMP_BOOSTER,
        evolutionTitle = "SİSMİK DROP",
        evolutionDescription = "Her vuruşta devasa çift katmanlı süper şok dalgası patlatır!"
    ),
    SYNTH_ORBIT(
        title = "Sentez Yörüngesi",
        shortTag = "YÖRÜNGE",
        baseDescription = "Karakterin etrafında dönen ritmik plazma küreleri",
        levelUpDescription = "Küre sayısını, dönüş hızını ve hasarı artırır",
        accentColor = HotMagenta,
        synergyPassive = PassiveId.TEMPO_BOOTS,
        evolutionTitle = "HİPER DİSKO HALKA",
        evolutionDescription = "6 yüksek hızlı altın bıçak yörüngede durmadan düşman biçer!"
    ),
    LASER_ARP(
        title = "Arpej Lazeri",
        shortTag = "LAZER",
        baseDescription = "Her vuruşta en yakın düşmanlara neon lazer ateşler",
        levelUpDescription = "Ek lazer ışını ve delme gücü ekler",
        accentColor = AcidLime,
        synergyPassive = PassiveId.DOUBLE_TIME,
        evolutionTitle = "KUANTUM ARPEJ",
        evolutionDescription = "Her vuruşta çoklu delici süper lazer yağmuru başlatır!"
    ),
    SUB_MINE(
        title = "Sub-Bas Mayını",
        shortTag = "MAYIN",
        baseDescription = "Güçlü vuruşta patlayan titreşimli bas mayınları bırakır",
        levelUpDescription = "Patlama yarıçapını ve mayın hasarını büyütür",
        accentColor = TankCrimson,
        synergyPassive = PassiveId.MAGNET_COIL,
        evolutionTitle = "KARA DELİK BAS",
        evolutionDescription = "Düşmanları merkeze çeken yerçekimi girdap mayınları oluşturur!"
    ),
    TREBLE_SLASH(
        title = "Tiz Kılıcı",
        shortTag = "KILIÇ",
        baseDescription = "Her 2 vuruşta bir hareket yönüne keskin ses dalgası savurur",
        levelUpDescription = "Kılıç yayını genişletir ve kritik hasarı artırır",
        accentColor = SwarmViolet,
        synergyPassive = PassiveId.DOUBLE_TIME,
        evolutionTitle = "360° FREKANS KASIRGASI",
        evolutionDescription = "Her vuruşta tüm etrafı saran 360° altın sonik kılıç çemberi!"
    ),
    FEEDBACK_NOVA(
        title = "Rezonans Zinciri",
        shortTag = "ZİNCİR",
        baseDescription = "Yakındaki düşmanlar arasında sıçrayan yüksek voltaj yayı",
        levelUpDescription = "Zincir sıçrama sayısını ve voltaj hasarını yükseltir",
        accentColor = SolarGold,
        synergyPassive = PassiveId.AMP_BOOSTER,
        evolutionTitle = "TESLA SENFONİSİ",
        evolutionDescription = "8 düşmana sıçrayan devasa plazma fırtınası her vuruşta tetiklenir!"
    )
}

object UpgradeConstants {
    const val MAX_WEAPON_LEVEL = 5
    const val MAX_PASSIVE_LEVEL = 5
}
