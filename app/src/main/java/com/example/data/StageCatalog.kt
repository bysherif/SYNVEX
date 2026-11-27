package com.example.data

import androidx.compose.ui.graphics.Color
import com.example.core.MissionType
import com.example.ui.theme.AcidLime
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.HotMagenta
import com.example.ui.theme.SolarGold
import com.example.ui.theme.SwarmViolet
import com.example.ui.theme.TankCrimson

/**
 * 6 Distinct Playable Arena World Maps (7 stages per map = 42 total stages).
 * Every 7 cleared stages unlocks a brand-new world map with its own arena floor, pillars & atmosphere!
 */
enum class ArenaMapId(
    val mapNumber: Int,
    val title: String,
    val shortName: String,
    val subtitle: String,
    val accentColor: Color,
    val unlockStageNumber: Int
) {
    CYBER_CITY(
        mapNumber = 1,
        title = "Siber Şehir",
        shortName = "Siber",
        subtitle = "Neon ızgara zemini ve 4 kuleli klasik arena",
        accentColor = ElectricCyan,
        unlockStageNumber = 1
    ),
    PLASMA_VOLCANO(
        mapNumber = 2,
        title = "Plazma Volkanı",
        shortName = "Volkan",
        subtitle = "Lav çatlaklı volkanik zemin ve 5 reaktör sütunu",
        accentColor = TankCrimson,
        unlockStageNumber = 8
    ),
    COSMIC_VOID(
        mapNumber = 3,
        title = "Kozmik Boşluk",
        shortName = "Kozmik",
        subtitle = "Yıldız devresi arenası ve 6 kuantum dikilitaşı",
        accentColor = SwarmViolet,
        unlockStageNumber = 15
    ),
    FROST_CORE(
        mapNumber = 4,
        title = "Buzul Çekirdek",
        shortName = "Buzul",
        subtitle = "Kriyo buz kristali zemini ve 6 altıgen buz sütunu",
        accentColor = Color(0xFF67E8F9),
        unlockStageNumber = 22
    ),
    TOXIC_LAB(
        mapNumber = 5,
        title = "Toksik Laboratuvar",
        shortName = "Toksik",
        subtitle = "Biyo-asit ızgaraları ve 5 çapraz reaktör koridoru",
        accentColor = AcidLime,
        unlockStageNumber = 29
    ),
    SOLAR_EMPIRE(
        mapNumber = 6,
        title = "Altın İmparatorluk",
        shortName = "Altın",
        subtitle = "Kraliyet güneş tapınağı ve 6 imparatorluk dikilitaşı",
        accentColor = SolarGold,
        unlockStageNumber = 36
    );

    val startStage: Int get() = (mapNumber - 1) * 7 + 1
    val endStage: Int get() = mapNumber * 7
}

data class StageDefinition(
    val stageNumber: Int,
    val mapId: ArenaMapId,
    val title: String,
    val subtitle: String,
    val accentColor: Color,
    val floorTintArgb: Int,
    val enemyHpMultiplier: Float,
    val enemySpeedMultiplier: Float,
    val rewardCoins: Int,
    val rewardCrystals: Int,
    val missionSequence: List<MissionType>
)

object StageCatalog {
    val stages: List<StageDefinition> = listOf(
        // ==================== HARİTA 1: SİBER ŞEHİR (BÖLÜM 1-7) ====================
        StageDefinition(
            stageNumber = 1,
            mapId = ArenaMapId.CYBER_CITY,
            title = "Neon Kafes",
            subtitle = "5 aşamalı arena protokolü: kuleler, çekirdekler, lazer fırtınası ve Muhafız",
            accentColor = ElectricCyan,
            floorTintArgb = 0xFF0A1424.toInt(),
            enemyHpMultiplier = 1.15f,
            enemySpeedMultiplier = 1.04f,
            rewardCoins = 190,
            rewardCrystals = 20,
            missionSequence = listOf(
                MissionType.CAPTURE_BEACON,
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.CAPTURE_BEACON,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 2,
            mapId = ArenaMapId.CYBER_CITY,
            title = "Voltaj Labirenti",
            subtitle = "Çifte lazer fırtınası ve yüksek gerilimli düşman akınları",
            accentColor = AcidLime,
            floorTintArgb = 0xFF0B1A18.toInt(),
            enemyHpMultiplier = 1.45f,
            enemySpeedMultiplier = 1.10f,
            rewardCoins = 270,
            rewardCrystals = 28,
            missionSequence = listOf(
                MissionType.SURVIVE_LASER_STORM,
                MissionType.CAPTURE_BEACON,
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 3,
            mapId = ArenaMapId.CYBER_CITY,
            title = "Kırmızı Alarm",
            subtitle = "6 aşamalı yoğun kuşatma, ağır tank ateşi ve çift çekirdek dalgası",
            accentColor = HotMagenta,
            floorTintArgb = 0xFF1A0B1B.toInt(),
            enemyHpMultiplier = 1.80f,
            enemySpeedMultiplier = 1.16f,
            rewardCoins = 360,
            rewardCrystals = 38,
            missionSequence = listOf(
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.CAPTURE_BEACON,
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 4,
            mapId = ArenaMapId.CYBER_CITY,
            title = "Bas Reaktörü",
            subtitle = "Aralıksız çekirdek patlamaları ve hızlı koşucu pusuları",
            accentColor = SolarGold,
            floorTintArgb = 0xFF1A140A.toInt(),
            enemyHpMultiplier = 2.20f,
            enemySpeedMultiplier = 1.22f,
            rewardCoins = 460,
            rewardCrystals = 50,
            missionSequence = listOf(
                MissionType.CAPTURE_BEACON,
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.CAPTURE_BEACON,
                MissionType.DESTROY_CORES,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 5,
            mapId = ArenaMapId.CYBER_CITY,
            title = "Synvex Kalesi",
            subtitle = "7 aşamalı zorlu kale savunması — her saniye kritik!",
            accentColor = SwarmViolet,
            floorTintArgb = 0xFF140B24.toInt(),
            enemyHpMultiplier = 2.65f,
            enemySpeedMultiplier = 1.28f,
            rewardCoins = 600,
            rewardCrystals = 70,
            missionSequence = listOf(
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.CAPTURE_BEACON,
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.CAPTURE_BEACON,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 6,
            mapId = ArenaMapId.CYBER_CITY,
            title = "Hiper Çekirdek",
            subtitle = "Ölümcül lazer taraması, elit sürüsü ve aşırı yüklü reaktör",
            accentColor = TankCrimson,
            floorTintArgb = 0xFF210B0A.toInt(),
            enemyHpMultiplier = 3.15f,
            enemySpeedMultiplier = 1.33f,
            rewardCoins = 760,
            rewardCrystals = 90,
            missionSequence = listOf(
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 7,
            mapId = ArenaMapId.CYBER_CITY,
            title = "Siber Geçit Finali",
            subtitle = "Çift Boss finali — bunu geçince HARİTA 2: PLAZMA VOLKANI açılır!",
            accentColor = DangerRed,
            floorTintArgb = 0xFF240610.toInt(),
            enemyHpMultiplier = 3.70f,
            enemySpeedMultiplier = 1.38f,
            rewardCoins = 1000,
            rewardCrystals = 130,
            missionSequence = listOf(
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.HUNT_BOSS,
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.CAPTURE_BEACON,
                MissionType.HUNT_BOSS
            )
        ),

        // ==================== HARİTA 2: PLAZMA VOLKANI (BÖLÜM 8-14) ====================
        StageDefinition(
            stageNumber = 8,
            mapId = ArenaMapId.PLASMA_VOLCANO,
            title = "Magma Kapısı",
            subtitle = "Yeni harita! Merkez reaktörlü lav arenasında ilk kuşatma",
            accentColor = TankCrimson,
            floorTintArgb = 0xFF240D06.toInt(),
            enemyHpMultiplier = 4.10f,
            enemySpeedMultiplier = 1.40f,
            rewardCoins = 1150,
            rewardCrystals = 140,
            missionSequence = listOf(
                MissionType.CAPTURE_BEACON,
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 9,
            mapId = ArenaMapId.PLASMA_VOLCANO,
            title = "Lav Damarı",
            subtitle = "Kızgın çatlaklar arasında çifte lazer ve ağır tank yağmuru",
            accentColor = SolarGold,
            floorTintArgb = 0xFF281205.toInt(),
            enemyHpMultiplier = 4.55f,
            enemySpeedMultiplier = 1.43f,
            rewardCoins = 1300,
            rewardCrystals = 155,
            missionSequence = listOf(
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 10,
            mapId = ArenaMapId.PLASMA_VOLCANO,
            title = "Kül Fırtınası",
            subtitle = "7 aşamalı volkanik alarm — durmaksızın hızlanan sürüler",
            accentColor = DangerRed,
            floorTintArgb = 0xFF2B080A.toInt(),
            enemyHpMultiplier = 5.05f,
            enemySpeedMultiplier = 1.46f,
            rewardCoins = 1480,
            rewardCrystals = 175,
            missionSequence = listOf(
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.CAPTURE_BEACON,
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.CAPTURE_BEACON,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 11,
            mapId = ArenaMapId.PLASMA_VOLCANO,
            title = "Obsidyen Ocak",
            subtitle = "Yüksek basınçlı çekirdekler ve daralan lazer koridorları",
            accentColor = HotMagenta,
            floorTintArgb = 0xFF260916.toInt(),
            enemyHpMultiplier = 5.60f,
            enemySpeedMultiplier = 1.49f,
            rewardCoins = 1650,
            rewardCrystals = 195,
            missionSequence = listOf(
                MissionType.CAPTURE_BEACON,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 12,
            mapId = ArenaMapId.PLASMA_VOLCANO,
            title = "Kor Çekirdeği",
            subtitle = "Çifte kule savunması ve amansız elit baskını",
            accentColor = TankCrimson,
            floorTintArgb = 0xFF2A0C06.toInt(),
            enemyHpMultiplier = 6.20f,
            enemySpeedMultiplier = 1.52f,
            rewardCoins = 1850,
            rewardCrystals = 215,
            missionSequence = listOf(
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.HUNT_BOSS,
                MissionType.CAPTURE_BEACON,
                MissionType.DESTROY_CORES,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 13,
            mapId = ArenaMapId.PLASMA_VOLCANO,
            title = "Termal Kıyamet",
            subtitle = "Volkan zirvesinde 7 ölümcül görev ve yoğun mermi fırtınası",
            accentColor = SolarGold,
            floorTintArgb = 0xFF2B1404.toInt(),
            enemyHpMultiplier = 6.85f,
            enemySpeedMultiplier = 1.55f,
            rewardCoins = 2100,
            rewardCrystals = 240,
            missionSequence = listOf(
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.CAPTURE_BEACON,
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 14,
            mapId = ArenaMapId.PLASMA_VOLCANO,
            title = "Volkan Kralı Finali",
            subtitle = "Çift Boss volkan finali — bunu geçince HARİTA 3: KOZMİK BOŞLUK açılır!",
            accentColor = DangerRed,
            floorTintArgb = 0xFF300608.toInt(),
            enemyHpMultiplier = 7.60f,
            enemySpeedMultiplier = 1.58f,
            rewardCoins = 2500,
            rewardCrystals = 290,
            missionSequence = listOf(
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.CAPTURE_BEACON,
                MissionType.HUNT_BOSS,
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.CAPTURE_BEACON,
                MissionType.HUNT_BOSS
            )
        ),

        // ==================== HARİTA 3: KOZMİK BOŞLUK (BÖLÜM 15-21) ====================
        StageDefinition(
            stageNumber = 15,
            mapId = ArenaMapId.COSMIC_VOID,
            title = "Yıldız Eşiği",
            subtitle = "Yeni harita! 6 dikilitaşlı kuantum uzay arenasına hoş geldin",
            accentColor = SwarmViolet,
            floorTintArgb = 0xFF120826.toInt(),
            enemyHpMultiplier = 8.40f,
            enemySpeedMultiplier = 1.60f,
            rewardCoins = 2750,
            rewardCrystals = 310,
            missionSequence = listOf(
                MissionType.CAPTURE_BEACON,
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 16,
            mapId = ArenaMapId.COSMIC_VOID,
            title = "Nebula Yarığı",
            subtitle = "Kuantum fırtınaları ve hiper hızlı koşucu orduları",
            accentColor = ElectricCyan,
            floorTintArgb = 0xFF09142B.toInt(),
            enemyHpMultiplier = 9.25f,
            enemySpeedMultiplier = 1.63f,
            rewardCoins = 3000,
            rewardCrystals = 340,
            missionSequence = listOf(
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 17,
            mapId = ArenaMapId.COSMIC_VOID,
            title = "Kara Delik Hattı",
            subtitle = "Çekirdek patlamaları ve çifte lazer çapraz ateşi",
            accentColor = HotMagenta,
            floorTintArgb = 0xFF1B0726.toInt(),
            enemyHpMultiplier = 10.2f,
            enemySpeedMultiplier = 1.66f,
            rewardCoins = 3300,
            rewardCrystals = 370,
            missionSequence = listOf(
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.CAPTURE_BEACON,
                MissionType.HUNT_BOSS,
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 18,
            mapId = ArenaMapId.COSMIC_VOID,
            title = "Astral Kuşatma",
            subtitle = "Uzay koridorlarında 7 aşamalı kesintisiz savaş",
            accentColor = AcidLime,
            floorTintArgb = 0xFF0A1B1E.toInt(),
            enemyHpMultiplier = 11.2f,
            enemySpeedMultiplier = 1.68f,
            rewardCoins = 3650,
            rewardCrystals = 410,
            missionSequence = listOf(
                MissionType.CAPTURE_BEACON,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 19,
            mapId = ArenaMapId.COSMIC_VOID,
            title = "Süpernova Kapısı",
            subtitle = "Yıldız reaktörünü çökert ve çift Muhafız'ı aş",
            accentColor = SolarGold,
            floorTintArgb = 0xFF1D1308.toInt(),
            enemyHpMultiplier = 12.3f,
            enemySpeedMultiplier = 1.71f,
            rewardCoins = 4000,
            rewardCrystals = 450,
            missionSequence = listOf(
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.HUNT_BOSS,
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 20,
            mapId = ArenaMapId.COSMIC_VOID,
            title = "Zaman Bükülmesi",
            subtitle = "Efsanevi zorluk — son kozmik kapıdan hemen öncesi",
            accentColor = SwarmViolet,
            floorTintArgb = 0xFF15062B.toInt(),
            enemyHpMultiplier = 13.6f,
            enemySpeedMultiplier = 1.74f,
            rewardCoins = 4500,
            rewardCrystals = 500,
            missionSequence = listOf(
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.HUNT_BOSS,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 21,
            mapId = ArenaMapId.COSMIC_VOID,
            title = "Kozmik Geçit Finali",
            subtitle = "Çift Boss uzay finali — bunu geçince HARİTA 4: BUZUL ÇEKİRDEK açılır!",
            accentColor = DangerRed,
            floorTintArgb = 0xFF240519.toInt(),
            enemyHpMultiplier = 15.0f,
            enemySpeedMultiplier = 1.76f,
            rewardCoins = 5000,
            rewardCrystals = 550,
            missionSequence = listOf(
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.CAPTURE_BEACON,
                MissionType.HUNT_BOSS,
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.CAPTURE_BEACON,
                MissionType.HUNT_BOSS
            )
        ),

        // ==================== HARİTA 4: BUZUL ÇEKİRDEK (BÖLÜM 22-28) ====================
        StageDefinition(
            stageNumber = 22,
            mapId = ArenaMapId.FROST_CORE,
            title = "Kriyo Kapısı",
            subtitle = "Yeni harita! Altıgen buz kristalleri arasında dondurucu kuşatma",
            accentColor = Color(0xFF67E8F9),
            floorTintArgb = 0xFF071B29.toInt(),
            enemyHpMultiplier = 16.2f,
            enemySpeedMultiplier = 1.78f,
            rewardCoins = 5300,
            rewardCrystals = 580,
            missionSequence = listOf(
                MissionType.CAPTURE_BEACON,
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 23,
            mapId = ArenaMapId.FROST_CORE,
            title = "Buzul Yarığı",
            subtitle = "Dondurucu lazer koridorları ve yoğun koşucu baskınları",
            accentColor = ElectricCyan,
            floorTintArgb = 0xFF061E2E.toInt(),
            enemyHpMultiplier = 17.5f,
            enemySpeedMultiplier = 1.80f,
            rewardCoins = 5600,
            rewardCrystals = 610,
            missionSequence = listOf(
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 24,
            mapId = ArenaMapId.FROST_CORE,
            title = "Kutup Reaktörü",
            subtitle = "6 çekirdekli yüksek basınçlı buz istasyonu savunması",
            accentColor = Color(0xFF38BDF8),
            floorTintArgb = 0xFF082032.toInt(),
            enemyHpMultiplier = 18.8f,
            enemySpeedMultiplier = 1.82f,
            rewardCoins = 6000,
            rewardCrystals = 640,
            missionSequence = listOf(
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.HUNT_BOSS,
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 25,
            mapId = ArenaMapId.FROST_CORE,
            title = "Kar Fırtınası",
            subtitle = "Aralıksız plazma ateşi ve kesintisiz dalga akışı",
            accentColor = SwarmViolet,
            floorTintArgb = 0xFF0C1830.toInt(),
            enemyHpMultiplier = 20.2f,
            enemySpeedMultiplier = 1.84f,
            rewardCoins = 6400,
            rewardCrystals = 680,
            missionSequence = listOf(
                MissionType.CAPTURE_BEACON,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 26,
            mapId = ArenaMapId.FROST_CORE,
            title = "Sıfır Mutlak",
            subtitle = "Buzul muhafızlarıyla çifte boss karşılaşması",
            accentColor = HotMagenta,
            floorTintArgb = 0xFF10132C.toInt(),
            enemyHpMultiplier = 21.8f,
            enemySpeedMultiplier = 1.86f,
            rewardCoins = 6900,
            rewardCrystals = 720,
            missionSequence = listOf(
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.CAPTURE_BEACON,
                MissionType.HUNT_BOSS,
                MissionType.CAPTURE_BEACON,
                MissionType.DESTROY_CORES,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 27,
            mapId = ArenaMapId.FROST_CORE,
            title = "Kristal Zirve",
            subtitle = "Buzul çekirdeğinin kalbinde 8 aşamalı sınav",
            accentColor = Color(0xFF67E8F9),
            floorTintArgb = 0xFF072336.toInt(),
            enemyHpMultiplier = 23.4f,
            enemySpeedMultiplier = 1.88f,
            rewardCoins = 7400,
            rewardCrystals = 770,
            missionSequence = listOf(
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.HUNT_BOSS,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 28,
            mapId = ArenaMapId.FROST_CORE,
            title = "Buzul Kralı Finali",
            subtitle = "Bunu geçince HARİTA 5: TOKSİK LABORATUVAR açılır!",
            accentColor = DangerRed,
            floorTintArgb = 0xFF15142E.toInt(),
            enemyHpMultiplier = 25.0f,
            enemySpeedMultiplier = 1.90f,
            rewardCoins = 8200,
            rewardCrystals = 850,
            missionSequence = listOf(
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.HUNT_BOSS,
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.CAPTURE_BEACON,
                MissionType.HUNT_BOSS
            )
        ),

        // ==================== HARİTA 5: TOKSİK LABORATUVAR (BÖLÜM 29-35) ====================
        StageDefinition(
            stageNumber = 29,
            mapId = ArenaMapId.TOXIC_LAB,
            title = "Karantina Girişi",
            subtitle = "Yeni harita! Çapraz biyo-reaktör koridorunda asit alarmı",
            accentColor = AcidLime,
            floorTintArgb = 0xFF0B1F11.toInt(),
            enemyHpMultiplier = 26.8f,
            enemySpeedMultiplier = 1.92f,
            rewardCoins = 8600,
            rewardCrystals = 890,
            missionSequence = listOf(
                MissionType.CAPTURE_BEACON,
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 30,
            mapId = ArenaMapId.TOXIC_LAB,
            title = "Asit Kanalı",
            subtitle = "Sızıntı odalarında çifte lazer ve ağır zırhlı dalgalar",
            accentColor = Color(0xFF4ADE80),
            floorTintArgb = 0xFF0A2313.toInt(),
            enemyHpMultiplier = 28.6f,
            enemySpeedMultiplier = 1.94f,
            rewardCoins = 9100,
            rewardCrystals = 940,
            missionSequence = listOf(
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 31,
            mapId = ArenaMapId.TOXIC_LAB,
            title = "Mutasyon Odası",
            subtitle = "Hızlanan mutasyon sürüleri ve ardışık çekirdek imhası",
            accentColor = SolarGold,
            floorTintArgb = 0xFF17210B.toInt(),
            enemyHpMultiplier = 30.5f,
            enemySpeedMultiplier = 1.96f,
            rewardCoins = 9600,
            rewardCrystals = 990,
            missionSequence = listOf(
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.CAPTURE_BEACON,
                MissionType.HUNT_BOSS,
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 32,
            mapId = ArenaMapId.TOXIC_LAB,
            title = "Biyo-Reaktör 9",
            subtitle = "Dar koridorlarda kesintisiz lazer taraması ve kule baskını",
            accentColor = ElectricCyan,
            floorTintArgb = 0xFF09211B.toInt(),
            enemyHpMultiplier = 32.5f,
            enemySpeedMultiplier = 1.98f,
            rewardCoins = 10200,
            rewardCrystals = 1050,
            missionSequence = listOf(
                MissionType.CAPTURE_BEACON,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 33,
            mapId = ArenaMapId.TOXIC_LAB,
            title = "Zehirli Çekirdek",
            subtitle = "8 aşamalı aşırı yüklü laboratuvar çatışması",
            accentColor = AcidLime,
            floorTintArgb = 0xFF0D2612.toInt(),
            enemyHpMultiplier = 34.8f,
            enemySpeedMultiplier = 2.00f,
            rewardCoins = 10900,
            rewardCrystals = 1120,
            missionSequence = listOf(
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.HUNT_BOSS,
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 34,
            mapId = ArenaMapId.TOXIC_LAB,
            title = "Radyasyon Eşiği",
            subtitle = "En yüksek alarm seviyesi — son biyo kapısına bir adım",
            accentColor = HotMagenta,
            floorTintArgb = 0xFF1A1B0E.toInt(),
            enemyHpMultiplier = 37.2f,
            enemySpeedMultiplier = 2.02f,
            rewardCoins = 11600,
            rewardCrystals = 1200,
            missionSequence = listOf(
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.HUNT_BOSS,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 35,
            mapId = ArenaMapId.TOXIC_LAB,
            title = "Toksik Hükümdar",
            subtitle = "Bunu geçince HARİTA 6: ALTIN İMPARATORLUK açılır!",
            accentColor = DangerRed,
            floorTintArgb = 0xFF1F160A.toInt(),
            enemyHpMultiplier = 40.0f,
            enemySpeedMultiplier = 2.04f,
            rewardCoins = 12500,
            rewardCrystals = 1350,
            missionSequence = listOf(
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.CAPTURE_BEACON,
                MissionType.HUNT_BOSS,
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.CAPTURE_BEACON,
                MissionType.HUNT_BOSS
            )
        ),

        // ==================== HARİTA 6: ALTIN İMPARATORLUK (BÖLÜM 36-42) ====================
        StageDefinition(
            stageNumber = 36,
            mapId = ArenaMapId.SOLAR_EMPIRE,
            title = "Güneş Kapısı",
            subtitle = "Yeni harita! Kraliyet altın tapınağında seçkin muhafızlar",
            accentColor = SolarGold,
            floorTintArgb = 0xFF211808.toInt(),
            enemyHpMultiplier = 43.0f,
            enemySpeedMultiplier = 2.06f,
            rewardCoins = 13200,
            rewardCrystals = 1420,
            missionSequence = listOf(
                MissionType.CAPTURE_BEACON,
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 37,
            mapId = ArenaMapId.SOLAR_EMPIRE,
            title = "Altın Koridor",
            subtitle = "Taht odası sütunları arasında yüksek tempolu baskın",
            accentColor = Color(0xFFFBBF24),
            floorTintArgb = 0xFF251A07.toInt(),
            enemyHpMultiplier = 46.2f,
            enemySpeedMultiplier = 2.08f,
            rewardCoins = 14000,
            rewardCrystals = 1500,
            missionSequence = listOf(
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.HUNT_BOSS,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 38,
            mapId = ArenaMapId.SOLAR_EMPIRE,
            title = "Kraliyet Kalkanı",
            subtitle = "Ağır tank birlikleri ve yoğun çapraz lazer ateşi",
            accentColor = TankCrimson,
            floorTintArgb = 0xFF281508.toInt(),
            enemyHpMultiplier = 49.5f,
            enemySpeedMultiplier = 2.10f,
            rewardCoins = 15000,
            rewardCrystals = 1600,
            missionSequence = listOf(
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.HUNT_BOSS,
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 39,
            mapId = ArenaMapId.SOLAR_EMPIRE,
            title = "Solar Fırtına",
            subtitle = "Güneş patlamaları ve aralıksız çekirdek kuşatması",
            accentColor = HotMagenta,
            floorTintArgb = 0xFF261214.toInt(),
            enemyHpMultiplier = 53.0f,
            enemySpeedMultiplier = 2.12f,
            rewardCoins = 16200,
            rewardCrystals = 1720,
            missionSequence = listOf(
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.CAPTURE_BEACON,
                MissionType.HUNT_BOSS,
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 40,
            mapId = ArenaMapId.SOLAR_EMPIRE,
            title = "Taht Salonu",
            subtitle = "İmparatorluğun elit muhafızları tüm gücüyle saldırıyor",
            accentColor = SolarGold,
            floorTintArgb = 0xFF291D06.toInt(),
            enemyHpMultiplier = 57.0f,
            enemySpeedMultiplier = 2.14f,
            rewardCoins = 17500,
            rewardCrystals = 1850,
            missionSequence = listOf(
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.HUNT_BOSS,
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 41,
            mapId = ArenaMapId.SOLAR_EMPIRE,
            title = "Efsane Eşiği",
            subtitle = "Büyük finalden önceki son imtihan — tüm gücünü kuşan!",
            accentColor = ElectricCyan,
            floorTintArgb = 0xFF1F1B0C.toInt(),
            enemyHpMultiplier = 61.5f,
            enemySpeedMultiplier = 2.16f,
            rewardCoins = 19000,
            rewardCrystals = 2050,
            missionSequence = listOf(
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.HUNT_BOSS,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.DESTROY_CORES,
                MissionType.CAPTURE_BEACON,
                MissionType.HUNT_BOSS
            )
        ),
        StageDefinition(
            stageNumber = 42,
            mapId = ArenaMapId.SOLAR_EMPIRE,
            title = "Sonsuz Synvex Tacı",
            subtitle = "Büyük final! Altın İmparatorluğun mutlak hükümdarını devir!",
            accentColor = DangerRed,
            floorTintArgb = 0xFF2E120A.toInt(),
            enemyHpMultiplier = 66.0f,
            enemySpeedMultiplier = 2.20f,
            rewardCoins = 25000,
            rewardCrystals = 3000,
            missionSequence = listOf(
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.CAPTURE_BEACON,
                MissionType.HUNT_BOSS,
                MissionType.DESTROY_CORES,
                MissionType.SURVIVE_LASER_STORM,
                MissionType.CAPTURE_BEACON,
                MissionType.HUNT_BOSS
            )
        )
    )

    fun forNumber(stageNumber: Int): StageDefinition {
        return stages.firstOrNull { it.stageNumber == stageNumber } ?: stages.first()
    }

    fun stagesForMap(mapId: ArenaMapId): List<StageDefinition> {
        return stages.filter { it.mapId == mapId }
    }

    /**
     * Returns the newly unlocked [ArenaMapId] when completing [clearedStageNumber]
     * (e.g. completing Stage 7 unlocks Harita 2: Plazma Volkanı; Stage 14 unlocks Harita 3: Kozmik Boşluk).
     */
    fun newlyUnlockedMapAfterStage(clearedStageNumber: Int): ArenaMapId? {
        val nextStageNumber = clearedStageNumber + 1
        return ArenaMapId.entries.firstOrNull { it.unlockStageNumber == nextStageNumber }
    }
}
