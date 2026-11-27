package com.example.core

import androidx.compose.ui.graphics.Color
import com.example.data.PassiveId
import com.example.data.PlayerProfile
import com.example.data.UpgradeCategory
import com.example.data.WeaponId

enum class GamePhase {
    MENU,
    SHOP,
    PLAYING,
    LEVEL_UP,
    PAUSED,
    GAME_OVER,
    VICTORY
}

data class StemLevels(
    val drums: Float = 1f,
    val bass: Float = 0f,
    val melody: Float = 0f,
    val overdrive: Float = 0f
)

data class UpgradeCardOption(
    val id: String,
    val category: UpgradeCategory,
    val weaponId: WeaponId? = null,
    val passiveId: PassiveId? = null,
    val title: String,
    val subtitle: String,
    val currentLevel: Int,
    val nextLevel: Int,
    val accentColor: Color,
    val synergyHint: String? = null,
    val isMaxed: Boolean = false
)

data class EquippedSlotInfo(
    val shortTag: String,
    val level: Int,
    val isEvolved: Boolean,
    val isPassive: Boolean,
    val color: Color
)

enum class MissionType {
    CAPTURE_BEACON,
    DESTROY_CORES,
    SURVIVE_LASER_STORM,
    HUNT_BOSS
}

data class MissionSnapshot(
    val type: MissionType,
    val missionNumber: Int,
    val totalMissionsInStage: Int,
    val title: String,
    val detail: String,
    val progress: Float,
    val timeLeftSeconds: Float,
    val maxTimeSeconds: Float,
    val rewardText: String,
    val isUrgent: Boolean
)

data class HudSnapshot(
    val phase: GamePhase = GamePhase.MENU,
    val elapsedSeconds: Float = 0f,
    val mapNumber: Int = 1,
    val mapTitle: String = "Siber Şehir",
    val unlockedNewMapTitle: String? = null,
    val stageNumber: Int = 1,
    val stageTitle: String = "Neon Kafes",
    val stageTotalMissions: Int = 3,
    val hasNextStage: Boolean = true,
    val waveNumber: Int = 1,
    val level: Int = 1,
    val xp: Int = 0,
    val xpToNext: Int = 55,
    val unspentSkillPoints: Int = 0,
    val hp: Float = 100f,
    val maxHp: Float = 100f,
    val kills: Int = 0,
    val combo: Int = 0,
    val maxCombo: Int = 0,
    val runCoins: Int = 0,
    val runCrystals: Int = 0,
    val dashReadyFraction: Float = 1f,
    val isDashing: Boolean = false,
    val novaChargeFraction: Float = 0f,
    val isNovaReady: Boolean = false,
    val bpm: Int = 128,
    val beatProgress: Float = 0f,
    val beatIndex: Int = 0,
    val fps: Int = 60,
    val isMuted: Boolean = false,
    val stems: StemLevels = StemLevels(),
    val equippedWeapons: List<EquippedSlotInfo> = emptyList(),
    val equippedPassives: List<EquippedSlotInfo> = emptyList(),
    val upgradeOptions: List<UpgradeCardOption> = emptyList(),
    val evolvedCount: Int = 0,
    val profile: PlayerProfile = PlayerProfile(),
    val showJoystickHint: Boolean = true,
    val eliteAnnouncement: String? = null,
    val levelUpToast: String? = null,
    val activeMission: MissionSnapshot? = null,
    val missionsCompleted: Int = 0,
    val missionBanner: String? = null,
    val missionBannerPositive: Boolean = true,
    val defeatReason: String? = null
)
