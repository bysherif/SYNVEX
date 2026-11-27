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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.billing.PlayBillingUiState
import com.example.data.PlayerProfile
import com.example.data.VipSubscriptionCatalog
import com.example.data.VipSubscriptionPlan
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
import com.example.ui.theme.SwarmViolet

private fun resolveSubDisplayPrice(plan: VipSubscriptionPlan, billingState: PlayBillingUiState): String {
    val live = billingState.livePrices[plan.googlePlayProductId]
    return if (!live.isNullOrBlank()) "$live (${plan.periodLabel})" else plan.priceLabel
}

private fun formatVipCooldown(remainingMs: Long): String {
    val totalMinutes = (remainingMs / 60_000L).coerceAtLeast(1L)
    val hours = totalMinutes / 60L
    val mins = totalMinutes % 60L
    return if (hours > 0L) "${hours}sa ${mins}dk sonra" else "${mins}dk sonra"
}

/**
 * Adds the VIP Subscription Pass section (Active VIP Daily Reward Claim + 7-Day Streak + Subscription Plans)
 * into the KASA / Store LazyColumn.
 */
fun LazyListScope.vipSubscriptionStoreItems(
    profile: PlayerProfile,
    billingState: PlayBillingUiState,
    onSelectPlan: (VipSubscriptionPlan) -> Unit,
    onClaimVipDaily: () -> Unit,
    onManageSubscriptions: () -> Unit
) {
    item(key = "vip_sub_header") {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = SolarGold, modifier = Modifier.size(19.dp))
                Text(
                    text = "VIP ABONELİK • GÜNLÜK ÖZEL HEDİYELER",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = SolarGold
                )
            }
            Text(
                text = "Abone ol, her gün Sarı Coin + Kırmızı Kristal, hoş geldin kasası ve 7. gün 2x Mega Ödül kazan!",
                style = MaterialTheme.typography.bodySmall,
                color = MistLavender
            )
        }
    }

    val activePlan = profile.activeVipPlan
    if (activePlan != null) {
        item(key = "vip_active_status_card") {
            val nowMs = System.currentTimeMillis()
            ActiveVipStatusCard(
                plan = activePlan,
                profile = profile,
                canClaimToday = profile.canClaimVipDailyReward(nowMs),
                cooldownLabel = formatVipCooldown(profile.remainingVipDailyMs(nowMs)),
                onClaimDaily = onClaimVipDaily,
                onManageSubscriptions = onManageSubscriptions
            )
        }
    }

    items(VipSubscriptionCatalog.plans, key = { "vip_plan_${it.id}" }) { plan ->
        val isCurrentActive = activePlan?.id == plan.id
        val isLiveSku = plan.googlePlayProductId in billingState.availableSubscriptionIds
        VipPlanCard(
            plan = plan,
            displayPrice = resolveSubDisplayPrice(plan, billingState),
            isCurrentActive = isCurrentActive,
            isLiveSku = isLiveSku,
            onClick = { onSelectPlan(plan) }
        )
    }

    item(key = "vip_policy_footer") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ObsidianVoid.copy(alpha = 0.6f))
                .border(1.dp, NeonBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
            Text(
                text = "Abonelikler Google Play üzerinden güvenle yenilenir. İstediğin zaman Google Play Store → Abonelikler menüsünden iptal edebilirsin.",
                style = MaterialTheme.typography.labelSmall,
                color = MistLavender
            )
        }
    }
}

@Composable
private fun ActiveVipStatusCard(
    plan: VipSubscriptionPlan,
    profile: PlayerProfile,
    canClaimToday: Boolean,
    cooldownLabel: String,
    onClaimDaily: () -> Unit,
    onManageSubscriptions: () -> Unit
) {
    val pulseTransition = rememberInfiniteTransition(label = "vip_active_pulse")
    val glow by pulseTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1300, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "vip_glow"
    )
    val isNextMega = profile.nextVipClaimIsMegaBonus
    val todayCoins = if (isNextMega) plan.megaDay7Coins else plan.dailyCoins
    val todayCrystals = if (isNextMega) plan.megaDay7Crystals else plan.dailyCrystals
    val streakFilled = profile.vipStreakCycleDay

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        plan.accentColor.copy(alpha = 0.26f),
                        SwarmViolet.copy(alpha = 0.20f),
                        NeonPanel
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    listOf(SolarGold, plan.accentColor.copy(alpha = 0.8f + glow * 0.2f), AcidLime)
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(14.dp)
            .testTag("active_vip_status_card"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(SolarGold.copy(alpha = 0.22f))
                        .border(1.5.dp, SolarGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = SolarGold, modifier = Modifier.size(22.dp))
                }
                Column {
                    Text(
                        text = "${plan.title} • AKTİF",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = SolarGold
                    )
                    Text(
                        text = if (profile.vipAutoRenewing) "Otomatik yenileme açık • VIP avantajlar aktif" else "Dönem sonuna kadar aktif",
                        style = MaterialTheme.typography.labelSmall,
                        color = AcidLime
                    )
                }
            }
            Text(
                text = "VIP",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = ObsidianVoid,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Brush.horizontalGradient(listOf(SolarGold, AcidLime)))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }

        // Active Gameplay Perks Summary Strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            VipMiniPerkChip(
                icon = Icons.Filled.Bolt,
                label = "+%${plan.coinXpBoostPercent} Coin & XP",
                color = ElectricCyan,
                modifier = Modifier.weight(1f)
            )
            VipMiniPerkChip(
                icon = Icons.Filled.Favorite,
                label = "+${plan.bonusMaxHp} Maks. Can",
                color = AcidLime,
                modifier = Modifier.weight(1f)
            )
        }

        // 7-Day Loyalty Streak Tracker (Day 7 = 2x Mega Chest)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ObsidianVoid.copy(alpha = 0.65f))
                .border(1.dp, NeonBorder, RoundedCornerShape(12.dp))
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "7 GÜNLÜK ABONE SADAKAT SERİSİ",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = IceWhite
                )
                Text(
                    text = "7. Gün: 2x MEGA KASA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = HotMagenta
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (day in 1..7) {
                    val isDone = day <= streakFilled
                    val isMegaSlot = day == 7
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(26.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(
                                when {
                                    isDone -> AcidLime.copy(alpha = 0.28f)
                                    isMegaSlot -> HotMagenta.copy(alpha = 0.24f)
                                    else -> NeonPanel
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = when {
                                    isDone -> AcidLime
                                    isMegaSlot -> HotMagenta
                                    else -> NeonBorder
                                },
                                shape = RoundedCornerShape(7.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isMegaSlot) "2x" else "$day",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = when {
                                isDone -> AcidLime
                                isMegaSlot -> HotMagenta
                                else -> MistLavender
                            }
                        )
                    }
                }
            }
        }

        // Daily VIP Claim CTA Button
        Button(
            onClick = onClaimDaily,
            enabled = canClaimToday,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isNextMega) HotMagenta else SolarGold,
                contentColor = ObsidianVoid,
                disabledContainerColor = NeonPanelElevated,
                disabledContentColor = DimSlate
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .graphicsLayer {
                    if (canClaimToday) {
                        val s = 1f + glow * 0.018f
                        scaleX = s
                        scaleY = s
                    }
                }
                .testTag("claim_vip_daily_button")
        ) {
            Icon(
                imageVector = if (canClaimToday) Icons.Filled.CardGiftcard else Icons.Filled.Schedule,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = if (canClaimToday) {
                    val tag = if (isNextMega) "7. GÜN MEGA: " else "GÜNLÜK VIP AL: "
                    "$tag+${todayCoins} Coin & +${todayCrystals} Kristal"
                } else {
                    "Bugünkü VIP Hediyen Alındı ($cooldownLabel)"
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        OutlinedButton(
            onClick = onManageSubscriptions,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .testTag("manage_play_subscriptions_button")
        ) {
            Icon(Icons.Filled.OpenInNew, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                text = "Google Play'de Aboneliği Yönet / İptal Et",
                style = MaterialTheme.typography.labelSmall,
                color = ElectricCyan
            )
        }
    }
}

@Composable
private fun VipPlanCard(
    plan: VipSubscriptionPlan,
    displayPrice: String,
    isCurrentActive: Boolean,
    isLiveSku: Boolean,
    onClick: () -> Unit
) {
    val accent = plan.accentColor
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        accent.copy(alpha = 0.22f),
                        NeonPanel.copy(alpha = 0.96f),
                        ObsidianVoid.copy(alpha = 0.95f)
                    )
                )
            )
            .border(
                width = if (isCurrentActive) 2.dp else 1.6.dp,
                color = if (isCurrentActive) AcidLime else accent.copy(alpha = 0.85f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag("vip_plan_card_${plan.id}"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Title & Badge Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.22f))
                        .border(1.5.dp, accent, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (plan.id == "vip_elite") Icons.Filled.AutoAwesome else Icons.Filled.Star,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = plan.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = IceWhite
                    )
                    Text(
                        text = if (isLiveSku) "${plan.periodLabel} • Google Play" else plan.periodLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = accent
                    )
                }
            }

            Text(
                text = if (isCurrentActive) "AKTİF" else plan.badge,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = ObsidianVoid,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isCurrentActive) AcidLime else accent)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }

        // Daily VIP Gifts Highlight Row (Yellow Coin + Red Crystal)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ObsidianVoid.copy(alpha = 0.72f))
                .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "HER GÜN ABONE HEDİYESİ",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MistLavender
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = SolarGold, modifier = Modifier.size(18.dp))
                        Text(
                            text = "+${plan.dailyCoins} Sarı Coin",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = SolarGold
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Filled.Diamond, contentDescription = null, tint = HotMagenta, modifier = Modifier.size(18.dp))
                        Text(
                            text = "+${plan.dailyCrystals} Kırmızı Kristal",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = HotMagenta
                        )
                    }
                }
            }
        }

        // Subscriber-Exclusive Bonus Perks List
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            VipBenefitLine(
                icon = Icons.Filled.CardGiftcard,
                text = "Anında Hoş Geldin Hediyesi: +${plan.welcomeCoins} Sarı Coin & +${plan.welcomeCrystals} Kırmızı Kristal",
                tint = AcidLime
            )
            VipBenefitLine(
                icon = Icons.Filled.EmojiEvents,
                text = "7. Gün Sadakat Kasası: +${plan.megaDay7Coins} Sarı Coin & +${plan.megaDay7Crystals} Kırmızı Kristal (2x)",
                tint = SolarGold
            )
            VipBenefitLine(
                icon = Icons.Filled.Bolt,
                text = "Oyun İçi Kalıcı VIP Gücü: +%${plan.coinXpBoostPercent} Coin & XP  •  +${plan.bonusMaxHp} Maks. Can",
                tint = ElectricCyan
            )
            VipBenefitLine(
                icon = Icons.Filled.Shield,
                text = "Özel Parlayan VIP Abone Rozeti (Ana Ekran & Mağaza)",
                tint = HotMagenta
            )
        }

        // Price CTA Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "İstediğin zaman iptal et",
                style = MaterialTheme.typography.labelSmall,
                color = MistLavender
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isCurrentActive) {
                            Brush.horizontalGradient(listOf(AcidLime, ElectricCyan))
                        } else {
                            Brush.horizontalGradient(listOf(accent, SolarGold))
                        }
                    )
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            ) {
                Text(
                    text = if (isCurrentActive) "AKTİF PLAN" else displayPrice,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = ObsidianVoid
                )
            }
        }
    }
}

@Composable
private fun VipBenefitLine(
    icon: ImageVector,
    text: String,
    tint: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(15.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = IceWhite
        )
    }
}

@Composable
private fun VipMiniPerkChip(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(ObsidianVoid.copy(alpha = 0.65f))
            .border(1.dp, color.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun VipSubscriptionDialog(
    plan: VipSubscriptionPlan,
    profile: PlayerProfile,
    billingState: PlayBillingUiState,
    onDismiss: () -> Unit,
    onSubscribe: () -> Unit,
    onManageSubscriptions: () -> Unit,
    onOpenPlayStore: () -> Unit
) {
    val isAlreadyActive = profile.activeVipPlan?.id == plan.id
    val livePrice = resolveSubDisplayPrice(plan, billingState)
    val isSkuActive = plan.googlePlayProductId in billingState.availableSubscriptionIds
    val accent = plan.accentColor

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.verticalGradient(listOf(NeonPanelElevated, ObsidianVoid)))
                .border(2.dp, accent.copy(alpha = 0.9f), RoundedCornerShape(24.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.22f))
                    .border(2.dp, accent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = accent, modifier = Modifier.size(34.dp))
            }

            Text(
                text = plan.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = IceWhite,
                textAlign = TextAlign.Center
            )
            Text(
                text = plan.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MistLavender,
                textAlign = TextAlign.Center
            )

            // Complete VIP Rewards Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(NeonPanel.copy(alpha = 0.92f))
                    .border(1.dp, NeonBorder, RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "ABONELİK AVANTAJLARI",
                    style = MaterialTheme.typography.labelSmall,
                    color = accent
                )
                VipBenefitLine(
                    icon = Icons.Filled.MonetizationOn,
                    text = "Günlük +${plan.dailyCoins} Sarı Coin & +${plan.dailyCrystals} Kırmızı Kristal",
                    tint = SolarGold
                )
                VipBenefitLine(
                    icon = Icons.Filled.CardGiftcard,
                    text = "Anında Hoş Geldin: +${plan.welcomeCoins} Coin & +${plan.welcomeCrystals} Kristal",
                    tint = AcidLime
                )
                VipBenefitLine(
                    icon = Icons.Filled.EmojiEvents,
                    text = "Her 7. Günde 2x Kasa: +${plan.megaDay7Coins} Coin & +${plan.megaDay7Crystals} Kristal",
                    tint = HotMagenta
                )
                VipBenefitLine(
                    icon = Icons.Filled.CheckCircle,
                    text = "Kalıcı +%${plan.coinXpBoostPercent} Coin/XP & +${plan.bonusMaxHp} Maks. Can + VIP Rozet",
                    tint = ElectricCyan
                )
            }

            // Transparent Google Play Subscription Disclosure (Play Policy Compliant)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ElectricCyan.copy(alpha = 0.12f))
                    .border(1.dp, ElectricCyan.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(16.dp))
                    Text(
                        text = "Google Play Abonelik ($livePrice)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                }
                Text(
                    text = if (isSkuActive) {
                        "Ödeme Google Play hesabından (Kart, Mobil Ödeme veya Play Bakiye) alınır. Dönem bitmeden iptal edilmezse otomatik yenilenir."
                    } else {
                        "Abonelik Kodu: ${plan.googlePlayProductId}. Google Play Console'da abonelik aktif edildiğinde doğrudan ödeme penceresi açılır."
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MistLavender
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("KAPAT", style = MaterialTheme.typography.labelLarge, color = MistLavender)
                }

                Button(
                    onClick = if (isAlreadyActive) onManageSubscriptions else onSubscribe,
                    enabled = !billingState.isProcessingPurchase,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isAlreadyActive) ElectricCyan else accent,
                        contentColor = ObsidianVoid
                    ),
                    modifier = Modifier
                        .weight(1.6f)
                        .height(48.dp)
                        .testTag("confirm_vip_subscribe_button")
                ) {
                    Text(
                        text = when {
                            isAlreadyActive -> "ABONELİĞİ YÖNET"
                            billingState.isProcessingPurchase -> "BEKLENİYOR..."
                            else -> "ABONE OL"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                }
            }

            if (!isSkuActive && !isAlreadyActive) {
                OutlinedButton(
                    onClick = onOpenPlayStore,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                ) {
                    Icon(Icons.Filled.OpenInNew, contentDescription = null, tint = AcidLime, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "GOOGLE PLAY STORE'A GİT",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AcidLime
                    )
                }
            }
        }
    }
}
