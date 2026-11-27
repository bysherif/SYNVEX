package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ArenaMapId
import com.example.data.PlayerProfile
import com.example.data.StageCatalog
import com.example.ui.theme.AcidLime
import com.example.ui.theme.DimSlate
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.Exo2FontFamily
import com.example.ui.theme.HotMagenta
import com.example.ui.theme.IceWhite
import com.example.ui.theme.MistLavender
import com.example.ui.theme.NeonBorder
import com.example.ui.theme.NeonPanel
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SolarGold

@Composable
fun TitleScreen(
    profile: PlayerProfile,
    onSelectStage: (Int) -> Unit,
    onPlayClick: () -> Unit,
    onOpenUpgradesClick: () -> Unit,
    onOpenStoreClick: () -> Unit,
    onOpenAchievementsClick: () -> Unit,
    onOpenShopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val beatTransition = rememberInfiniteTransition(label = "title_bpm_pulse")
    val beatPulse by beatTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 469, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "title_pulse"
    )
    val selectedStageDef = StageCatalog.forNumber(profile.selectedStage)
    var viewedMapId by remember(selectedStageDef.mapId) { mutableStateOf(selectedStageDef.mapId) }
    val viewedMapStages = remember(viewedMapId) { StageCatalog.stagesForMap(viewedMapId) }
    val viewedMapUnlocked = profile.highestUnlockedStage >= viewedMapId.unlockStageNumber
    var showPrivacyDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_bassline_hero_1795593793355),
            contentDescription = stringResource(R.string.cd_hero_background),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to ObsidianVoid.copy(alpha = 0.88f),
                        0.36f to ObsidianVoid.copy(alpha = 0.50f),
                        0.66f to ObsidianVoid.copy(alpha = 0.78f),
                        1.0f to ObsidianVoid.copy(alpha = 0.96f)
                    )
                )
        )

        NeonAmbientParticles(accentColor = selectedStageDef.accentColor)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Top Player Rank + Power Score + Wallet Strip
            Column(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(NeonPanel.copy(alpha = 0.9f))
                            .border(1.dp, ElectricCyan.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(Icons.Filled.Shield, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(15.dp))
                        Text(
                            text = "SV ${profile.accountLevel} • ${profile.rankTitle}",
                            style = MaterialTheme.typography.labelMedium,
                            color = IceWhite
                        )
                    }

                    WalletStrip(
                        coins = profile.coins,
                        crystals = profile.crystals,
                        onWalletClick = onOpenStoreClick
                    )
                }

                // Account XP + Power Score + Auto-save Status Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(NeonPanel.copy(alpha = 0.78f))
                        .border(1.dp, NeonBorder, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(Icons.Filled.Bolt, contentDescription = null, tint = SolarGold, modifier = Modifier.size(15.dp))
                        Text(
                            text = "GÜÇ: ${profile.powerRating}",
                            style = MaterialTheme.typography.labelMedium,
                            color = SolarGold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 10.dp)
                            .height(6.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(ObsidianVoid)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(profile.accountXpProgress.coerceAtLeast(0.05f))
                                .height(6.dp)
                                .background(Brush.horizontalGradient(listOf(ElectricCyan, AcidLime)))
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (profile.isVipSubscribed) {
                                    Brush.horizontalGradient(listOf(SolarGold, HotMagenta))
                                } else {
                                    Brush.horizontalGradient(listOf(SolarGold.copy(alpha = 0.22f), HotMagenta.copy(alpha = 0.22f)))
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = if (profile.isVipSubscribed) SolarGold else SolarGold.copy(alpha = 0.65f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable(onClick = onOpenStoreClick)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                            .testTag("title_vip_badge_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "VIP Abonelik",
                            tint = if (profile.isVipSubscribed) ObsidianVoid else SolarGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = when {
                                profile.canClaimVipDailyReward() -> "VIP HEDİYE!"
                                profile.isVipSubscribed -> "VIP AKTİF"
                                else -> "VIP PASS"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (profile.isVipSubscribed) ObsidianVoid else SolarGold
                        )
                    }

                    Spacer(Modifier.width(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(ObsidianVoid.copy(alpha = 0.7f))
                            .border(1.dp, NeonBorder, RoundedCornerShape(10.dp))
                            .clickable { showPrivacyDialog = true }
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                            .testTag("title_privacy_policy_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PrivacyTip,
                            contentDescription = stringResource(R.string.privacy_policy_title),
                            tint = MistLavender,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = stringResource(R.string.privacy_policy_button),
                            style = MaterialTheme.typography.labelSmall,
                            color = MistLavender
                        )
                    }
                }
            }

            // 2. Centerpiece: SYNVEX Emblem + Equipped Ship Card
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.widthIn(max = 500.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(64.dp)
                            .graphicsLayer {
                                val s = 0.98f + beatPulse * 0.04f
                                scaleX = s
                                scaleY = s
                            }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.sweepGradient(listOf(ElectricCyan, HotMagenta, SolarGold, ElectricCyan))
                                )
                                .padding(2.5.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_synvex_logo_1795594792355),
                                contentDescription = stringResource(R.string.cd_game_logo),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = "SYNVEX",
                            style = TextStyle(
                                fontFamily = Exo2FontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 36.sp,
                                letterSpacing = 4.sp,
                                brush = Brush.horizontalGradient(listOf(ElectricCyan, IceWhite, HotMagenta))
                            )
                        )
                        Text(
                            text = stringResource(R.string.title_tagline),
                            style = MaterialTheme.typography.labelSmall,
                            color = MistLavender
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Equipped Ship & Stats Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(NeonPanel.copy(alpha = 0.88f))
                        .border(1.5.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .clickable(onClick = onOpenShopClick)
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LiveAvatarPreviewBox(
                        shape = profile.equippedShape,
                        color = profile.equippedColor,
                        aura = profile.equippedAura,
                        sizeDp = 56
                    )

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = "${profile.equippedShape.title} • ${profile.equippedColor.title}",
                            style = MaterialTheme.typography.titleMedium,
                            color = IceWhite
                        )
                        Text(
                            text = "${profile.totalKills} Yok Etme • ${profile.totalMissionsCompleted} Görev • ${profile.totalMetaUpgradeLevels} Yükseltme",
                            style = MaterialTheme.typography.labelSmall,
                            color = MistLavender
                        )
                    }

                    Icon(Icons.Filled.Palette, contentDescription = null, tint = HotMagenta, modifier = Modifier.size(22.dp))
                }
            }

            // 3. Bottom Stack: 5-Stage Campaign Map + 3 Quick Hub Buttons + Enter Arena CTA
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .fillMaxWidth()
            ) {
                // Multi-Map Campaign Stage Selector Card (3 Maps x 7 Stages = 21 Stages)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(NeonPanel.copy(alpha = 0.92f))
                        .border(1.5.dp, viewedMapId.accentColor.copy(alpha = 0.65f), RoundedCornerShape(18.dp))
                        .padding(11.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "HARİTA ${viewedMapId.mapNumber}: ${viewedMapId.title.uppercase()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = viewedMapId.accentColor
                        )
                        Text(
                            text = "${profile.highestUnlockedStage} / ${StageCatalog.stages.size} BÖLÜM",
                            style = MaterialTheme.typography.labelSmall,
                            color = MistLavender
                        )
                    }

                    // 6 World Map Selector Tabs (Scrollable — every 7 cleared stages unlocks a new world!)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ArenaMapId.entries.forEach { map ->
                            val isMapUnlocked = profile.highestUnlockedStage >= map.unlockStageNumber
                            val isMapSelected = viewedMapId == map
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        when {
                                            isMapSelected -> map.accentColor.copy(alpha = 0.24f)
                                            isMapUnlocked -> ObsidianVoid.copy(alpha = 0.7f)
                                            else -> ObsidianVoid.copy(alpha = 0.42f)
                                        }
                                    )
                                    .border(
                                        width = if (isMapSelected) 1.6.dp else 1.dp,
                                        color = if (isMapSelected) map.accentColor else NeonBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        viewedMapId = map
                                        if (isMapUnlocked && selectedStageDef.mapId != map) {
                                            val defaultStage = profile.highestUnlockedStage
                                                .coerceIn(map.startStage, map.endStage)
                                            onSelectStage(defaultStage)
                                        }
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("map_tab_${map.mapNumber}")
                            ) {
                                if (!isMapUnlocked) {
                                    Icon(
                                        Icons.Filled.Lock,
                                        contentDescription = null,
                                        tint = DimSlate,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                }
                                Text(
                                    text = "H${map.mapNumber}: ${map.shortName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isMapSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = when {
                                        isMapSelected -> map.accentColor
                                        isMapUnlocked -> IceWhite
                                        else -> DimSlate
                                    },
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        viewedMapStages.forEach { st ->
                            val unlocked = st.stageNumber <= profile.highestUnlockedStage
                            val completed = st.stageNumber < profile.highestUnlockedStage
                            val selected = st.stageNumber == profile.selectedStage

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                modifier = Modifier
                                    .width(88.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        when {
                                            selected -> st.accentColor.copy(alpha = 0.24f)
                                            unlocked -> ObsidianVoid.copy(alpha = 0.72f)
                                            else -> ObsidianVoid.copy(alpha = 0.45f)
                                        }
                                    )
                                    .border(
                                        width = if (selected) 2.dp else 1.dp,
                                        color = when {
                                            selected -> st.accentColor
                                            unlocked -> NeonBorder
                                            else -> DimSlate.copy(alpha = 0.4f)
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable(enabled = unlocked) { onSelectStage(st.stageNumber) }
                                    .padding(vertical = 8.dp, horizontal = 6.dp)
                                    .testTag("stage_chip_${st.stageNumber}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (!unlocked) {
                                        Icon(Icons.Filled.Lock, contentDescription = null, tint = DimSlate, modifier = Modifier.size(13.dp))
                                    } else if (completed) {
                                        Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = AcidLime, modifier = Modifier.size(13.dp))
                                    }
                                    Text(
                                        text = "BÖLÜM ${st.stageNumber}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (unlocked) IceWhite else DimSlate
                                    )
                                }
                                Text(
                                    text = st.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) st.accentColor else if (unlocked) MistLavender else DimSlate,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(ObsidianVoid.copy(alpha = 0.65f))
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = if (viewedMapUnlocked) {
                                    "Bölüm ${selectedStageDef.stageNumber}: ${selectedStageDef.title}"
                                } else {
                                    "Kilitli Harita • Bölüm ${viewedMapId.unlockStageNumber - 1} tamamlanınca açılır"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                color = if (viewedMapUnlocked) IceWhite else SolarGold
                            )
                            Text(
                                text = if (viewedMapUnlocked) selectedStageDef.subtitle else viewedMapId.subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MistLavender
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${selectedStageDef.missionSequence.size} Görev",
                                style = MaterialTheme.typography.labelSmall,
                                color = selectedStageDef.accentColor
                            )
                            Text(
                                text = "+${selectedStageDef.rewardCoins}C +${selectedStageDef.rewardCrystals}K",
                                style = MaterialTheme.typography.labelSmall,
                                color = SolarGold
                            )
                        }
                    }
                }

                // 4 Prominent Hub Buttons: Upgrades | Store Vault (KASA) | Achievements | Customization
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    HubActionTile(
                        label = "GÜÇ",
                        icon = Icons.Filled.Upgrade,
                        color = AcidLime,
                        badgeCount = profile.affordableUpgradesCount,
                        highlight = false,
                        onClick = onOpenUpgradesClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("upgrades_hub_button")
                    )
                    HubActionTile(
                        label = "KASA",
                        icon = Icons.Filled.ShoppingCart,
                        color = HotMagenta,
                        badgeCount = profile.claimableStoreGiftsCount(),
                        highlight = true,
                        onClick = onOpenStoreClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("store_vault_hub_button")
                    )
                    HubActionTile(
                        label = "ÖDÜL",
                        icon = Icons.Filled.EmojiEvents,
                        color = SolarGold,
                        badgeCount = profile.claimableAchievementsCount,
                        highlight = false,
                        onClick = onOpenAchievementsClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("achievements_hub_button")
                    )
                    HubActionTile(
                        label = "TASARIM",
                        icon = Icons.Filled.Palette,
                        color = ElectricCyan,
                        badgeCount = 0,
                        highlight = false,
                        onClick = onOpenShopClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("shop_button")
                    )
                }

                // Giant Action CTA: Enter Arena!
                Button(
                    onClick = onPlayClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = selectedStageDef.accentColor,
                        contentColor = ObsidianVoid
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .graphicsLayer {
                            val s = 1f + beatPulse * 0.018f
                            scaleX = s
                            scaleY = s
                        }
                        .testTag("play_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(26.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "OYUN ALANINA GİR • H${selectedStageDef.mapId.mapNumber} BÖLÜM ${selectedStageDef.stageNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (showPrivacyDialog) {
            PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
        }
    }
}

@Composable
private fun HubActionTile(
    label: String,
    icon: ImageVector,
    color: Color,
    badgeCount: Int,
    highlight: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (highlight) {
                        Brush.verticalGradient(listOf(color.copy(alpha = 0.28f), NeonPanel.copy(alpha = 0.95f)))
                    } else {
                        Brush.verticalGradient(listOf(NeonPanel.copy(alpha = 0.92f), NeonPanel.copy(alpha = 0.92f)))
                    }
                )
                .border(
                    width = if (badgeCount > 0 || highlight) 1.6.dp else 1.2.dp,
                    color = color.copy(alpha = if (badgeCount > 0 || highlight) 0.95f else 0.52f),
                    shape = RoundedCornerShape(14.dp)
                )
                .clickable(onClick = onClick)
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = IceWhite,
                maxLines = 1
            )
        }
        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 2.dp, end = 3.dp)
                    .size(17.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$badgeCount",
                    style = MaterialTheme.typography.labelSmall,
                    color = ObsidianVoid,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        } else if (highlight) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 2.dp, end = 3.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(SolarGold)
                    .padding(horizontal = 4.dp, vertical = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.labelSmall,
                    color = ObsidianVoid,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
