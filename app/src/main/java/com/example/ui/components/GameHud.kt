package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.EquippedSlotInfo
import com.example.core.HudSnapshot
import com.example.core.MissionSnapshot
import com.example.core.MissionType
import com.example.core.StemLevels
import com.example.ui.theme.AcidLime
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DimSlate
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.HotMagenta
import com.example.ui.theme.IceWhite
import com.example.ui.theme.MistLavender
import com.example.ui.theme.NeonBorder
import com.example.ui.theme.NeonPanel
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SolarGold

@Composable
fun GameHud(
    hud: HudSnapshot,
    onPauseClick: () -> Unit,
    onMuteClick: () -> Unit,
    onOpenUpgrades: () -> Unit,
    onDashClick: () -> Unit,
    onNovaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val beatPulse = (1f - hud.beatProgress).let { it * it }
    val hasPoints = hud.unspentSkillPoints > 0

    Box(modifier = modifier.fillMaxSize()) {
        // --- TOP HUD STACK ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            // 1. Full-width XP Progress Bar
            val xpFraction = (hud.xp.toFloat() / hud.xpToNext.coerceAtLeast(1)).coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(9.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(ObsidianVoid.copy(alpha = 0.85f))
                    .border(1.dp, NeonBorder, RoundedCornerShape(6.dp))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(xpFraction)
                        .height(9.dp)
                        .background(Brush.horizontalGradient(listOf(ElectricCyan, AcidLime)))
                )
            }

            // 2. Primary Telemetry Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Level + Stage + Timer
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.hud_level_format, hud.level),
                            style = MaterialTheme.typography.labelLarge,
                            color = ObsidianVoid,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AcidLime)
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                        Text(
                            text = "H${hud.mapNumber} • BÖLÜM ${hud.stageNumber}",
                            style = MaterialTheme.typography.labelMedium,
                            color = ElectricCyan,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NeonPanel.copy(alpha = 0.88f))
                                .border(1.dp, ElectricCyan.copy(alpha = 0.55f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Text(
                            text = formatTimer(hud.elapsedSeconds),
                            style = MaterialTheme.typography.titleLarge,
                            color = IceWhite
                        )
                        Text(
                            text = "×${hud.kills}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MistLavender
                        )
                    }
                }

                // Center: Pulsing BPM + Live FPS + Combo
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(NeonPanel.copy(alpha = 0.88f))
                            .border(
                                width = 1.dp,
                                color = if (hud.combo >= 15) HotMagenta else ElectricCyan.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (hud.beatIndex % 2 == 0) ElectricCyan else HotMagenta)
                        )
                        Text(
                            text = stringResource(R.string.hud_bpm_format, hud.bpm),
                            style = MaterialTheme.typography.labelSmall,
                            color = IceWhite
                        )
                        Text("•", style = MaterialTheme.typography.labelSmall, color = DimSlate)
                        Text(
                            text = stringResource(R.string.hud_fps_format, hud.fps),
                            style = MaterialTheme.typography.labelSmall,
                            color = AcidLime
                        )
                    }

                    if (hud.combo > 0) {
                        val comboColor = when {
                            hud.combo >= 30 -> SolarGold
                            hud.combo >= 15 -> HotMagenta
                            hud.combo >= 5 -> ElectricCyan
                            else -> MistLavender
                        }
                        Text(
                            text = stringResource(R.string.hud_combo_format, hud.combo),
                            style = MaterialTheme.typography.labelLarge,
                            color = comboColor
                        )
                    }
                }

                // Right: Mute & Pause Icon Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onMuteClick,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(CircleShape)
                            .background(NeonPanel.copy(alpha = 0.82f))
                            .testTag("mute_button")
                    ) {
                        Icon(
                            imageVector = if (hud.isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = stringResource(
                                if (hud.isMuted) R.string.action_unmute else R.string.action_mute
                            ),
                            tint = if (hud.isMuted) DimSlate else ElectricCyan
                        )
                    }
                    IconButton(
                        onClick = onPauseClick,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(CircleShape)
                            .background(NeonPanel.copy(alpha = 0.82f))
                            .testTag("pause_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Pause,
                            contentDescription = stringResource(R.string.action_pause),
                            tint = IceWhite
                        )
                    }
                }
            }

            // 3. HP + Run Coins + 4-Channel Stem Equalizer Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonPanel.copy(alpha = 0.8f))
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Favorite,
                            contentDescription = null,
                            tint = if (hud.hp / hud.maxHp > 0.35f) HotMagenta else DangerRed,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "${hud.hp.toInt()}/${hud.maxHp.toInt()}",
                            style = MaterialTheme.typography.labelSmall,
                            color = IceWhite
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NeonPanel.copy(alpha = 0.8f))
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MonetizationOn,
                            contentDescription = null,
                            tint = SolarGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "${hud.runCoins}",
                            style = MaterialTheme.typography.labelSmall,
                            color = SolarGold
                        )
                    }
                }

                StemEqualizerStrip(stems = hud.stems, beatPulse = beatPulse)
            }

            // 3b. Active Mission Tracker Card
            hud.activeMission?.let { mission ->
                MissionTrackerCard(mission = mission)
            }

            // 3c. Mission Complete / Mission Failed Alert Banner
            AnimatedVisibility(
                visible = hud.missionBanner != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                val bannerColor = if (hud.missionBannerPositive) AcidLime else DangerRed
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(bannerColor.copy(alpha = 0.22f))
                        .border(1.5.dp, bannerColor, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (hud.missionBannerPositive) Icons.Filled.Flag else Icons.Filled.Warning,
                        contentDescription = null,
                        tint = bannerColor,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = hud.missionBanner.orEmpty(),
                        style = MaterialTheme.typography.labelMedium,
                        color = bannerColor
                    )
                }
            }

            // 4. Non-Blocking Level-Up Toast Banner
            AnimatedVisibility(
                visible = hud.levelUpToast != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AcidLime.copy(alpha = 0.2f))
                        .border(1.5.dp, AcidLime, RoundedCornerShape(12.dp))
                        .clickable(onClick = onOpenUpgrades)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = AcidLime, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = hud.levelUpToast.orEmpty(),
                        style = MaterialTheme.typography.labelMedium,
                        color = AcidLime
                    )
                }
            }

            // 5. Elite Boss Warning Banner
            AnimatedVisibility(
                visible = hud.eliteAnnouncement != null,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SolarGold.copy(alpha = 0.2f))
                        .border(1.5.dp, SolarGold, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = SolarGold, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = hud.eliteAnnouncement.orEmpty(),
                        style = MaterialTheme.typography.labelMedium,
                        color = SolarGold
                    )
                }
            }
        }

        // --- BOTTOM STRIP: ACTIVE COMBAT ABILITIES (DASH + NOVA) + LOADOUT + UPGRADE ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Active Combat Controls Row (Right-aligned ergonomic thumb buttons)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (hud.showJoystickHint) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(NeonPanel.copy(alpha = 0.85f))
                            .border(1.dp, ElectricCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Filled.TouchApp, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                        Text(
                            text = stringResource(R.string.hint_joystick),
                            style = MaterialTheme.typography.labelSmall,
                            color = IceWhite
                        )
                    }
                } else {
                    Spacer(Modifier.width(1.dp))
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // OVERDRIVE NOVA Button (Charges with kills, blasts arena + clears enemy bullets!)
                    val novaReady = hud.isNovaReady
                    val novaPct = (hud.novaChargeFraction * 100f).toInt().coerceIn(0, 100)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .size(62.dp)
                            .graphicsLayer {
                                if (novaReady) {
                                    val s = 1f + beatPulse * 0.08f
                                    scaleX = s
                                    scaleY = s
                                }
                            }
                            .clip(CircleShape)
                            .background(
                                if (novaReady) {
                                    Brush.radialGradient(listOf(SolarGold, HotMagenta))
                                } else {
                                    Brush.radialGradient(listOf(NeonPanel.copy(alpha = 0.92f), ObsidianVoid.copy(alpha = 0.92f)))
                                }
                            )
                            .border(
                                width = if (novaReady) 2.5.dp else 1.5.dp,
                                color = if (novaReady) IceWhite else HotMagenta.copy(alpha = 0.55f),
                                shape = CircleShape
                            )
                            .clickable(enabled = novaReady, onClick = onNovaClick)
                            .testTag("nova_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = "Süper Nova",
                            tint = if (novaReady) ObsidianVoid else HotMagenta,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (novaReady) "NOVA!" else "%$novaPct",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (novaReady) ObsidianVoid else IceWhite
                        )
                    }

                    // TACTICAL DASH (ATIL) Button (Short cooldown invulnerability dodge burst!)
                    val dashReady = hud.dashReadyFraction >= 0.99f
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(
                                if (dashReady) {
                                    Brush.radialGradient(listOf(ElectricCyan, Color(0xFF007EA7)))
                                } else {
                                    Brush.radialGradient(listOf(NeonPanel.copy(alpha = 0.9f), ObsidianVoid.copy(alpha = 0.9f)))
                                }
                            )
                            .border(
                                width = if (dashReady) 2.2.dp else 1.5.dp,
                                color = if (dashReady) IceWhite else ElectricCyan.copy(alpha = 0.45f),
                                shape = CircleShape
                            )
                            .clickable(enabled = dashReady, onClick = onDashClick)
                            .testTag("dash_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = "Hızlı Kaçınma",
                            tint = if (dashReady) ObsidianVoid else ElectricCyan,
                            modifier = Modifier.size(21.dp)
                        )
                        Text(
                            text = if (dashReady) "ATIL" else "%${(hud.dashReadyFraction * 100f).toInt()}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (dashReady) ObsidianVoid else MistLavender
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    hud.equippedWeapons.forEach { slot -> LoadoutChip(slot = slot) }
                    hud.equippedPassives.forEach { slot -> LoadoutChip(slot = slot) }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .graphicsLayer {
                            if (hasPoints) {
                                val s = 1f + beatPulse * 0.08f
                                scaleX = s
                                scaleY = s
                            }
                        }
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (hasPoints) {
                                Brush.horizontalGradient(listOf(AcidLime, ElectricCyan))
                            } else {
                                Brush.horizontalGradient(listOf(NeonPanel, NeonPanel))
                            }
                        )
                        .border(
                            width = if (hasPoints) 2.dp else 1.dp,
                            color = if (hasPoints) IceWhite else NeonBorder,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable(onClick = onOpenUpgrades)
                        .testTag("open_upgrade_button")
                        .padding(horizontal = 14.dp, vertical = 11.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Upgrade,
                        contentDescription = stringResource(R.string.action_upgrade_skills),
                        tint = if (hasPoints) ObsidianVoid else ElectricCyan,
                        modifier = Modifier.size(19.dp)
                    )
                    Text(
                        text = if (hasPoints) "YÜKSELT (+${hud.unspentSkillPoints})" else "YETENEK",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (hasPoints) ObsidianVoid else IceWhite
                    )
                }
            }
        }
    }
}

@Composable
private fun StemEqualizerStrip(
    stems: StemLevels,
    beatPulse: Float
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(NeonPanel.copy(alpha = 0.82f))
            .border(1.dp, NeonBorder, RoundedCornerShape(8.dp))
            .padding(horizontal = 7.dp, vertical = 4.dp)
    ) {
        Icon(Icons.Filled.GraphicEq, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(13.dp))
        StemMeter(label = stringResource(R.string.stem_drums), level = stems.drums, pulse = beatPulse, color = ElectricCyan)
        StemMeter(label = stringResource(R.string.stem_bass), level = stems.bass, pulse = beatPulse, color = AcidLime)
        StemMeter(label = stringResource(R.string.stem_melody), level = stems.melody, pulse = beatPulse, color = HotMagenta)
        StemMeter(label = stringResource(R.string.stem_overdrive), level = stems.overdrive, pulse = beatPulse, color = SolarGold)
    }
}

@Composable
private fun StemMeter(
    label: String,
    level: Float,
    pulse: Float,
    color: Color
) {
    val active = level > 0.25f
    val barHeight = (4f + level * 9f * (0.7f + pulse * 0.4f)).dp
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.5.dp)
                .height(13.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .height(barHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (active) color else DimSlate.copy(alpha = 0.4f))
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (active) color else DimSlate
        )
    }
}

@Composable
private fun LoadoutChip(slot: EquippedSlotInfo) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (slot.isEvolved) SolarGold.copy(alpha = 0.2f) else NeonPanel.copy(alpha = 0.85f))
            .border(
                width = if (slot.isEvolved) 1.5.dp else 1.dp,
                color = slot.color.copy(alpha = if (slot.isEvolved) 0.95f else 0.55f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        if (slot.isEvolved) {
            Icon(Icons.Filled.Bolt, contentDescription = null, tint = SolarGold, modifier = Modifier.size(12.dp))
        }
        Text(
            text = slot.shortTag,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = slot.color
        )
        Text(
            text = if (slot.isEvolved) "MAX" else "S${slot.level}",
            style = MaterialTheme.typography.labelSmall,
            color = IceWhite
        )
    }
}

@Composable
private fun MissionTrackerCard(mission: MissionSnapshot) {
    val accent = when {
        mission.isUrgent -> DangerRed
        mission.type == MissionType.CAPTURE_BEACON -> ElectricCyan
        mission.type == MissionType.DESTROY_CORES -> HotMagenta
        mission.type == MissionType.SURVIVE_LASER_STORM -> DangerRed
        else -> SolarGold
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(NeonPanel.copy(alpha = 0.88f))
            .border(1.5.dp, accent.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 7.dp)
            .testTag("mission_tracker_card"),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (mission.isUrgent) Icons.Filled.Warning else Icons.Filled.Flag,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "GÖREV ${mission.missionNumber}/${mission.totalMissionsInStage}: ${mission.title}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = IceWhite
                )
            }
            Text(
                text = "${mission.timeLeftSeconds.toInt()}s",
                style = MaterialTheme.typography.labelLarge,
                color = if (mission.isUrgent) DangerRed else SolarGold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = mission.detail,
                style = MaterialTheme.typography.labelSmall,
                color = MistLavender
            )
            Text(
                text = mission.rewardText,
                style = MaterialTheme.typography.labelSmall,
                color = AcidLime
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(ObsidianVoid)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(mission.progress.coerceIn(0f, 1f))
                    .height(6.dp)
                    .background(accent)
            )
        }
    }
}

fun formatTimer(secondsFloat: Float): String {
    val total = secondsFloat.toInt().coerceAtLeast(0)
    val mins = total / 60
    val secs = total % 60
    return "%02d:%02d".format(mins, secs)
}
