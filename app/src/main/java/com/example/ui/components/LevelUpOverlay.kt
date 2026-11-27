package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.UpgradeCardOption
import com.example.data.PassiveId
import com.example.data.UpgradeCategory
import com.example.data.UpgradeConstants
import com.example.data.WeaponId
import com.example.ui.theme.AcidLime
import com.example.ui.theme.DimSlate
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.HotMagenta
import com.example.ui.theme.IceWhite
import com.example.ui.theme.MistLavender
import com.example.ui.theme.NeonBorder
import com.example.ui.theme.NeonPanel
import com.example.ui.theme.NeonPanelElevated
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SolarGold

/**
 * On-Demand Free-Choice Arsenal Upgrade Overlay!
 * Opens ONLY when the player taps the Upgrade button on the HUD, allowing them to freely pick
 * ANY weapon, passive, or unlocked Evolution Synergy they want using saved Skill Points.
 */
@Composable
fun LevelUpOverlay(
    level: Int,
    unspentPoints: Int,
    options: List<UpgradeCardOption>,
    onSelectUpgrade: (UpgradeCardOption) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableIntStateOf(0) } // 0=All, 1=Weapons, 2=Passives

    val filteredOptions = remember(options, selectedFilter) {
        when (selectedFilter) {
            1 -> options.filter {
                it.category == UpgradeCategory.EVOLUTION_SYNERGY || it.category == UpgradeCategory.ACTIVE_WEAPON
            }
            2 -> options.filter { it.category == UpgradeCategory.PASSIVE_BOOST }
            else -> options
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "evolution_glow")
    val evoGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "evo_glow_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid.copy(alpha = 0.88f))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Bar: Title + Skill Points Badge + Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "YETENEK ATÖLYESİ",
                            style = MaterialTheme.typography.headlineMedium,
                            color = IceWhite
                        )
                        Text(
                            text = stringResource(R.string.hud_level_format, level),
                            style = MaterialTheme.typography.labelMedium,
                            color = ObsidianVoid,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ElectricCyan)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = if (unspentPoints > 0) {
                            "İstediğin yeteneğe dokunarak yükselt ($unspentPoints puan kaldı)"
                        } else {
                            "Mevcut silah ve sinerji tariflerini inceleyebilirsin"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MistLavender
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .background(NeonPanel)
                        .testTag("close_upgrade_button")
                ) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.action_resume), tint = IceWhite)
                }
            }

            // Skill Points Banner + Filter Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterPill("TÜMÜ", selectedFilter == 0, ElectricCyan) { selectedFilter = 0 }
                    FilterPill("SİLAHLAR", selectedFilter == 1, HotMagenta) { selectedFilter = 1 }
                    FilterPill("PASİFLER", selectedFilter == 2, AcidLime) { selectedFilter = 2 }
                }

                Text(
                    text = if (unspentPoints > 0) "+$unspentPoints PUAN" else "0 PUAN",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (unspentPoints > 0) ObsidianVoid else DimSlate,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (unspentPoints > 0) AcidLime else NeonPanel)
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                )
            }

            // Scrollable Full Arsenal Upgrade List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                itemsIndexed(filteredOptions, key = { _, item -> item.id }) { index, option ->
                    ArsenalUpgradeRow(
                        option = option,
                        index = index,
                        canUpgrade = unspentPoints > 0 && !option.isMaxed,
                        evoGlowAlpha = evoGlowAlpha,
                        onClick = { onSelectUpgrade(option) }
                    )
                }
            }

            // Bottom Resume Button
            Button(
                onClick = onClose,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricCyan,
                    contentColor = ObsidianVoid
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("upgrade_resume_button")
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("ARENAYA DÖN", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = if (selected) ObsidianVoid else MistLavender,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) color else NeonPanel)
            .border(1.dp, if (selected) color else NeonBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp)
    )
}

@Composable
private fun ArsenalUpgradeRow(
    option: UpgradeCardOption,
    index: Int,
    canUpgrade: Boolean,
    evoGlowAlpha: Float,
    onClick: () -> Unit
) {
    val isEvolution = option.category == UpgradeCategory.EVOLUTION_SYNERGY
    val borderColor = when {
        isEvolution -> SolarGold.copy(alpha = evoGlowAlpha)
        canUpgrade -> option.accentColor.copy(alpha = 0.72f)
        else -> NeonBorder
    }

    val cardBrush = if (isEvolution) {
        Brush.linearGradient(
            listOf(SolarGold.copy(alpha = 0.25f), HotMagenta.copy(alpha = 0.16f), NeonPanelElevated)
        )
    } else {
        Brush.linearGradient(
            listOf(option.accentColor.copy(alpha = if (canUpgrade) 0.13f else 0.05f), NeonPanel)
        )
    }

    val badgeLabel = when (option.category) {
        UpgradeCategory.EVOLUTION_SYNERGY -> stringResource(R.string.badge_evolution)
        UpgradeCategory.ACTIVE_WEAPON -> stringResource(R.string.badge_weapon)
        UpgradeCategory.PASSIVE_BOOST -> stringResource(R.string.badge_passive)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(cardBrush)
            .border(
                width = if (isEvolution) 2.2.dp else 1.2.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = canUpgrade, onClick = onClick)
            .testTag("upgrade_card_$index")
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icon Box
        Box(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(option.accentColor.copy(alpha = 0.18f))
                .border(1.2.dp, option.accentColor.copy(alpha = 0.75f), RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = upgradeIconFor(option),
                contentDescription = stringResource(R.string.cd_upgrade_icon),
                tint = option.accentColor,
                modifier = Modifier.size(26.dp)
            )
        }

        // Info Column
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = badgeLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isEvolution) ObsidianVoid else option.accentColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(5.dp))
                        .background(if (isEvolution) SolarGold else option.accentColor.copy(alpha = 0.16f))
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                )
                Text(
                    text = when {
                        isEvolution -> "★ EVRİM"
                        option.isMaxed -> "MAX"
                        option.currentLevel == 0 -> stringResource(R.string.badge_new)
                        else -> "SV ${option.currentLevel} → ${option.nextLevel}"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isEvolution || option.isMaxed) SolarGold else AcidLime
                )
            }

            Text(
                text = option.title,
                style = MaterialTheme.typography.titleMedium,
                color = if (isEvolution) SolarGold else IceWhite
            )

            Text(
                text = option.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MistLavender
            )

            // Level Pips
            if (!isEvolution && option.nextLevel <= UpgradeConstants.MAX_WEAPON_LEVEL) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (pip in 1..UpgradeConstants.MAX_WEAPON_LEVEL) {
                        val filled = pip <= option.currentLevel
                        val nextPip = !option.isMaxed && pip == option.nextLevel && canUpgrade
                        Box(
                            modifier = Modifier
                                .size(if (nextPip) 8.dp else 6.5.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        nextPip -> AcidLime
                                        filled -> option.accentColor
                                        else -> Color.White.copy(alpha = 0.15f)
                                    }
                                )
                        )
                    }
                }
            }

            if (option.synergyHint != null) {
                Text(
                    text = option.synergyHint,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isEvolution) SolarGold else ElectricCyan.copy(alpha = 0.85f)
                )
            }
        }

        // Right Upgrade Action Pill
        if (option.isMaxed) {
            Text(
                text = "MAX",
                style = MaterialTheme.typography.labelMedium,
                color = SolarGold,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(SolarGold.copy(alpha = 0.16f))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            )
        } else if (canUpgrade) {
            Text(
                text = if (isEvolution) "EVRİM!" else "+1",
                style = MaterialTheme.typography.labelLarge,
                color = ObsidianVoid,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isEvolution) SolarGold else AcidLime)
                    .padding(horizontal = 12.dp, vertical = 9.dp)
            )
        }
    }
}

private fun upgradeIconFor(option: UpgradeCardOption): ImageVector {
    if (option.category == UpgradeCategory.EVOLUTION_SYNERGY) return Icons.Filled.AutoAwesome
    option.weaponId?.let { weapon ->
        return when (weapon) {
            WeaponId.BEAT_SHOCKWAVE -> Icons.Filled.Waves
            WeaponId.SYNTH_ORBIT -> Icons.Filled.TrackChanges
            WeaponId.LASER_ARP -> Icons.Filled.FlashOn
            WeaponId.SUB_MINE -> Icons.Filled.Radar
            WeaponId.TREBLE_SLASH -> Icons.Filled.GraphicEq
            WeaponId.FEEDBACK_NOVA -> Icons.Filled.Bolt
        }
    }
    option.passiveId?.let { passive ->
        return when (passive) {
            PassiveId.TEMPO_BOOTS -> Icons.AutoMirrored.Filled.DirectionsRun
            PassiveId.MAGNET_COIL -> Icons.Filled.Hub
            PassiveId.AMP_BOOSTER -> Icons.Filled.Waves
            PassiveId.DOUBLE_TIME -> Icons.Filled.Speed
        }
    }
    return Icons.Filled.AutoAwesome
}
