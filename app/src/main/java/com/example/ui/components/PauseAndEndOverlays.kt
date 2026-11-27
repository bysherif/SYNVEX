package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.core.HudSnapshot
import com.example.ui.theme.AcidLime
import com.example.ui.theme.DangerRed
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.HotMagenta
import com.example.ui.theme.IceWhite
import com.example.ui.theme.MistLavender
import com.example.ui.theme.NeonBorder
import com.example.ui.theme.NeonPanel
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SolarGold

@Composable
fun PauseOverlay(
    hud: HudSnapshot,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onMainMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid.copy(alpha = 0.82f))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(NeonPanel)
                .border(1.5.dp, ElectricCyan.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.pause_title),
                style = MaterialTheme.typography.headlineMedium,
                color = ElectricCyan,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(R.string.pause_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MistLavender
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(ObsidianVoid.copy(alpha = 0.55f))
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryMetric(stringResource(R.string.stat_survival_time), formatTimer(hud.elapsedSeconds), ElectricCyan)
                SummaryMetric(stringResource(R.string.stat_kills), "${hud.kills}", AcidLime)
                SummaryMetric(stringResource(R.string.stat_level), "${hud.level}", HotMagenta)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onResume,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan, contentColor = ObsidianVoid),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("resume_button")
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.action_resume), style = MaterialTheme.typography.labelLarge)
            }

            OutlinedButton(
                onClick = onRestart,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("pause_restart_button")
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, tint = IceWhite)
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.action_restart), style = MaterialTheme.typography.labelLarge, color = IceWhite)
            }

            OutlinedButton(
                onClick = onMainMenu,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("pause_menu_button")
            ) {
                Icon(Icons.Filled.Home, contentDescription = null, tint = MistLavender)
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.action_main_menu), style = MaterialTheme.typography.labelLarge, color = MistLavender)
            }
        }
    }
}

@Composable
fun RunSummaryOverlay(
    hud: HudSnapshot,
    isVictory: Boolean,
    onRestart: () -> Unit,
    onNextStage: () -> Unit,
    onOpenShop: () -> Unit,
    onMainMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = if (isVictory) SolarGold else HotMagenta

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianVoid.copy(alpha = 0.88f))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(NeonPanel)
                .border(2.dp, accent.copy(alpha = 0.8f), RoundedCornerShape(24.dp))
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isVictory) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = SolarGold,
                    modifier = Modifier.size(44.dp)
                )
            }

            Text(
                text = if (isVictory) "BÖLÜM ${hud.stageNumber} GEÇİLDİ!" else stringResource(R.string.game_over_title),
                style = MaterialTheme.typography.displaySmall,
                color = accent,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Harita ${hud.mapNumber}: ${hud.mapTitle} • Bölüm ${hud.stageNumber} (${hud.stageTitle})",
                style = MaterialTheme.typography.labelMedium,
                color = MistLavender,
                textAlign = TextAlign.Center
            )

            if (isVictory && !hud.unlockedNewMapTitle.isNullOrBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ElectricCyan.copy(alpha = 0.20f))
                        .border(1.5.dp, ElectricCyan, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = SolarGold, modifier = Modifier.size(20.dp))
                    Text(
                        text = "YENİ HARİTA AÇILDI: ${hud.unlockedNewMapTitle.uppercase()}!",
                        style = MaterialTheme.typography.labelLarge,
                        color = IceWhite
                    )
                }
            }

            if (!isVictory && !hud.defeatReason.isNullOrBlank()) {
                Text(
                    text = "YANDIN • ${hud.defeatReason}",
                    style = MaterialTheme.typography.labelMedium,
                    color = DangerRed,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(DangerRed.copy(alpha = 0.16f))
                        .border(1.dp, DangerRed.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                )
            }

            // Earned Rewards Banner
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SolarGold.copy(alpha = 0.14f))
                    .border(1.dp, SolarGold.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = SolarGold, modifier = Modifier.size(20.dp))
                        Text("+${hud.runCoins} COİN", style = MaterialTheme.typography.titleMedium, color = SolarGold)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(Icons.Filled.Diamond, contentDescription = null, tint = HotMagenta, modifier = Modifier.size(20.dp))
                        Text("+${hud.runCrystals} KRİSTAL", style = MaterialTheme.typography.titleMedium, color = HotMagenta)
                    }
                }
                Text(
                    text = "✓ Tüm coin, kristal ve bölüm ilerlemen otomatik kaydedildi",
                    style = MaterialTheme.typography.labelSmall,
                    color = AcidLime,
                    textAlign = TextAlign.Center
                )
            }

            // Metrics Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ObsidianVoid.copy(alpha = 0.6f))
                    .border(1.dp, NeonBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    SummaryMetric(stringResource(R.string.stat_survival_time), formatTimer(hud.elapsedSeconds), ElectricCyan)
                    SummaryMetric(stringResource(R.string.stat_kills), "${hud.kills}", AcidLime)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    SummaryMetric("GÖREV", "${hud.missionsCompleted}/${hud.stageTotalMissions}", SolarGold)
                    SummaryMetric(stringResource(R.string.stat_level), "SV ${hud.level}", HotMagenta)
                }
            }

            if (isVictory && hud.hasNextStage) {
                val nextButtonText = if (!hud.unlockedNewMapTitle.isNullOrBlank()) {
                    "YENİ HARİTAYA GEÇ (BÖLÜM ${hud.stageNumber + 1})"
                } else {
                    "SONRAKİ BÖLÜM (BÖLÜM ${hud.stageNumber + 1})"
                }
                Button(
                    onClick = onNextStage,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AcidLime, contentColor = ObsidianVoid),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("next_stage_button")
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.size(8.dp))
                    Text(nextButtonText, style = MaterialTheme.typography.labelLarge)
                }
            }

            Button(
                onClick = onRestart,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isVictory && hud.hasNextStage) NeonPanel else accent,
                    contentColor = if (isVictory && hud.hasNextStage) IceWhite else ObsidianVoid
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("restart_button")
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null)
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.action_restart), style = MaterialTheme.typography.labelLarge)
            }

            OutlinedButton(
                onClick = onOpenShop,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("summary_shop_button")
            ) {
                Icon(Icons.Filled.Storefront, contentDescription = null, tint = SolarGold)
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.action_open_shop), style = MaterialTheme.typography.labelLarge, color = SolarGold)
            }

            OutlinedButton(
                onClick = onMainMenu,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("menu_button")
            ) {
                Icon(Icons.Filled.Home, contentDescription = null, tint = IceWhite)
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.action_main_menu), style = MaterialTheme.typography.labelLarge, color = IceWhite)
            }
        }
    }
}

@Composable
private fun SummaryMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MistLavender
        )
    }
}
