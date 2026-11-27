package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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
import com.example.billing.PlayBillingConnectionState
import com.example.billing.PlayBillingUiState
import com.example.data.PlayerProfile
import com.example.data.StoreOfferCatalog
import com.example.data.StoreOfferPack
import com.example.data.StorePackCategory
import com.example.data.VipSubscriptionPlan
import com.example.ui.theme.AcidLime
import com.example.ui.theme.DangerRed
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
import kotlin.math.PI
import kotlin.math.sin

private data class AmbientMote(
    val xFraction: Float,
    val yOffset: Float,
    val speed: Float,
    val radiusDp: Float,
    val swayAmplitude: Float,
    val phase: Float,
    val colorIndex: Int
)

/**
 * Lightweight, always-alive ambient neon energy particle canvas for TitleScreen & ShopScreen.
 * Zero per-frame allocations inside onDraw.
 */
@Composable
fun NeonAmbientParticles(
    modifier: Modifier = Modifier,
    accentColor: Color = ElectricCyan
) {
    val motes = remember {
        List(18) { i ->
            val seed = (i * 37 + 13) % 100
            AmbientMote(
                xFraction = ((i * 19 + 7) % 94 + 3) / 100f,
                yOffset = (seed / 100f),
                speed = 0.35f + (i % 4) * 0.16f,
                radiusDp = 2.2f + (i % 4) * 1.3f,
                swayAmplitude = 10f + (i % 5) * 6f,
                phase = i * 0.72f,
                colorIndex = i % 4
            )
        }
    }

    val transition = rememberInfiniteTransition(label = "ambient_neon_motes")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "mote_progress"
    )
    val glowPulse by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mote_glow"
    )

    val palette = remember(accentColor) {
        listOf(accentColor, HotMagenta, SolarGold, AcidLime)
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return@Canvas

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(accentColor.copy(alpha = 0.12f * glowPulse), Color.Transparent),
                center = Offset(w * 0.18f, h * 0.14f),
                radius = w * 0.55f
            ),
            radius = w * 0.55f,
            center = Offset(w * 0.18f, h * 0.14f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(HotMagenta.copy(alpha = 0.10f * (1.45f - glowPulse)), Color.Transparent),
                center = Offset(w * 0.86f, h * 0.72f),
                radius = w * 0.52f
            ),
            radius = w * 0.52f,
            center = Offset(w * 0.86f, h * 0.72f)
        )

        val twoPi = (2.0 * PI).toFloat()
        for (i in motes.indices) {
            val m = motes[i]
            val travel = (m.yOffset - progress * m.speed + 2f) % 1f
            val cy = travel * h
            val cx = m.xFraction * w + sin(progress * twoPi + m.phase) * m.swayAmplitude
            val edgeFade = when {
                travel < 0.12f -> travel / 0.12f
                travel > 0.88f -> (1f - travel) / 0.12f
                else -> 1f
            }.coerceIn(0f, 1f)
            val baseColor = palette[m.colorIndex]
            val r = m.radiusDp.dp.toPx()

            drawCircle(
                color = baseColor.copy(alpha = 0.18f * edgeFade * glowPulse),
                radius = r * 3.1f,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = baseColor.copy(alpha = 0.65f * edgeFade),
                radius = r,
                center = Offset(cx, cy)
            )
        }
    }
}

private fun resolveDisplayPrice(pack: StoreOfferPack, billingState: PlayBillingUiState): String {
    val sku = pack.googlePlayProductId ?: return pack.priceLabel
    return billingState.livePrices[sku] ?: pack.priceLabel
}

private fun formatCooldownHours(remainingMs: Long): String {
    val totalMinutes = (remainingMs / 60_000L).coerceAtLeast(1L)
    val hours = totalMinutes / 60L
    val mins = totalMinutes % 60L
    return if (hours > 0L) "${hours}sa ${mins}dk sonra" else "${mins}dk sonra"
}

/**
 * Populates the production Google Play Billing KASA / MAĞAZA storefront inside ShopScreen's LazyColumn.
 */
fun LazyListScope.storeVaultItems(
    profile: PlayerProfile,
    billingState: PlayBillingUiState,
    onSelectPack: (StoreOfferPack) -> Unit,
    onSelectVipPlan: (VipSubscriptionPlan) -> Unit,
    onClaimVipDaily: () -> Unit,
    onManageSubscriptions: () -> Unit,
    onRefreshPlayStore: () -> Unit,
    onOpenPlayStore: () -> Unit
) {
    // 1. Official Google Play & Mobile Carrier Billing Status Bar
    item(key = "store_google_play_header") {
        GooglePlayStatusCard(
            billingState = billingState,
            onRefresh = onRefreshPlayStore,
            onOpenPlayStore = onOpenPlayStore
        )
    }

    // 2. Pending Payment Notification (e.g., Mobile Carrier / Play Store async confirmation)
    if (billingState.hasPendingPayment) {
        item(key = "store_pending_payment") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SolarGold.copy(alpha = 0.16f))
                    .border(1.5.dp, SolarGold.copy(alpha = 0.8f), RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Filled.Schedule, contentDescription = null, tint = SolarGold, modifier = Modifier.size(22.dp))
                Text(
                    text = "Bekleyen Google Play / Mobil Ödeme işlemin var. Onaylandığında bakiyen otomatik olarak yüklenecek.",
                    style = MaterialTheme.typography.bodySmall,
                    color = IceWhite
                )
            }
        }
    }

    // 2.5 VIP Subscription Pass Section (Daily 400 Yellow Coin + 10 Red Crystal, Welcome Gift & 7-Day Mega Bonus)
    vipSubscriptionStoreItems(
        profile = profile,
        billingState = billingState,
        onSelectPlan = onSelectVipPlan,
        onClaimVipDaily = onClaimVipDaily,
        onManageSubscriptions = onManageSubscriptions
    )

    // 3. Featured Super Bundle Banner
    item(key = "store_featured_bundle") {
        val pack = StoreOfferCatalog.featuredBundle
        FeaturedBundleBanner(
            pack = pack,
            displayPrice = resolveDisplayPrice(pack, billingState),
            isLiveSku = pack.googlePlayProductId in billingState.availableProductIds,
            onClick = { onSelectPack(pack) }
        )
    }

    // 4. Daily Free Arena Supply Gift Card (Real 24h Cooldown)
    item(key = "store_free_gift") {
        val nowMs = System.currentTimeMillis()
        val canClaim = profile.canClaimDailyGift(nowMs)
        val remainingMs = profile.remainingDailyGiftMs(nowMs)
        FreeSupplyGiftCard(
            pack = StoreOfferCatalog.freeGiftPack,
            canClaim = canClaim,
            cooldownLabel = if (canClaim) null else formatCooldownHours(remainingMs),
            onClick = { onSelectPack(StoreOfferCatalog.freeGiftPack) }
        )
    }

    // 5. Coin Packs Section (2x2 Grid)
    item(key = "store_coin_header") {
        StoreSectionHeader(
            title = "COİN PAKETLERİ",
            subtitle = "Google Play Kart, Mobil Ödeme ve Play Bakiyesi ile güvenli alım",
            accentColor = SolarGold,
            icon = Icons.Filled.MonetizationOn
        )
    }
    items(StoreOfferCatalog.coinPacks.chunked(2), key = { row -> "coin_row_${row.first().id}" }) { rowPacks ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            rowPacks.forEach { pack ->
                StoreOfferGridCard(
                    pack = pack,
                    displayPrice = resolveDisplayPrice(pack, billingState),
                    isCrystalFocus = false,
                    onClick = { onSelectPack(pack) },
                    modifier = Modifier.weight(1f)
                )
            }
            if (rowPacks.size == 1) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }

    // 6. Special Vex Crystal Packs Section (2x2 Grid)
    item(key = "store_crystal_header") {
        Spacer(modifier = Modifier.height(4.dp))
        StoreSectionHeader(
            title = "ÖZEL VEX KRİSTAL PAKETLERİ",
            subtitle = "VIP gemi şekilleri, krom renkler ve Kraliyet Halesi için özel kristal",
            accentColor = HotMagenta,
            icon = Icons.Filled.Diamond
        )
    }
    items(StoreOfferCatalog.crystalPacks.chunked(2), key = { row -> "crystal_row_${row.first().id}" }) { rowPacks ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            rowPacks.forEach { pack ->
                StoreOfferGridCard(
                    pack = pack,
                    displayPrice = resolveDisplayPrice(pack, billingState),
                    isCrystalFocus = true,
                    onClick = { onSelectPack(pack) },
                    modifier = Modifier.weight(1f)
                )
            }
            if (rowPacks.size == 1) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }

    // 7. In-Game Coin -> Special Crystal Exchange
    item(key = "store_exchange_header") {
        Spacer(modifier = Modifier.height(4.dp))
        StoreSectionHeader(
            title = "COİN → ÖZEL KRİSTAL TAKASI",
            subtitle = "Arenada kazandığın Coin'leri anında Özel Vex Kristale çevir",
            accentColor = ElectricCyan,
            icon = Icons.Filled.SwapHoriz
        )
    }
    items(StoreOfferCatalog.exchangePacks, key = { it.id }) { pack ->
        val canAfford = profile.coins >= pack.coinExchangeCost
        CoinExchangeRow(
            pack = pack,
            canAfford = canAfford,
            onClick = { if (canAfford) onSelectPack(pack) }
        )
    }
}

@Composable
private fun GooglePlayStatusCard(
    billingState: PlayBillingUiState,
    onRefresh: () -> Unit,
    onOpenPlayStore: () -> Unit
) {
    val isReady = billingState.connectionState == PlayBillingConnectionState.READY
    val liveCount = billingState.availableProductIds.size + billingState.availableSubscriptionIds.size
    val statusColor = when {
        isReady && liveCount > 0 -> AcidLime
        isReady -> ElectricCyan
        billingState.connectionState == PlayBillingConnectionState.CONNECTING -> SolarGold
        else -> DangerRed
    }
    val statusText = when {
        isReady && liveCount > 0 -> "Google Play Bağlı • $liveCount paket & abonelik aktif"
        isReady -> "Google Play Bağlı • Play Console ürünleri bekleniyor"
        billingState.connectionState == PlayBillingConnectionState.CONNECTING -> "Google Play Store'a bağlanılıyor..."
        else -> "Google Play ödeme servisi kullanılamıyor"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NeonPanel.copy(alpha = 0.94f))
            .border(1.2.dp, statusColor.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = statusColor, modifier = Modifier.size(18.dp))
                Column {
                    Text(
                        text = "GOOGLE PLAY GÜVENLİ ÖDEME",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = IceWhite
                    )
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor
                    )
                }
            }
        }

        // Accepted payment rails strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PaymentMethodPill(icon = Icons.Filled.Smartphone, label = "Mobil Ödeme", color = ElectricCyan)
            PaymentMethodPill(icon = Icons.Filled.CreditCard, label = "Kart / Play Bakiye", color = SolarGold)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onRefresh,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .testTag("refresh_play_store_button")
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, tint = ElectricCyan, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(5.dp))
                Text("Satın Alımları Yenile", style = MaterialTheme.typography.labelSmall, color = IceWhite, maxLines = 1)
            }

            OutlinedButton(
                onClick = onOpenPlayStore,
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .testTag("open_play_store_button")
            ) {
                Icon(Icons.Filled.OpenInNew, contentDescription = null, tint = AcidLime, modifier = Modifier.size(15.dp))
                Spacer(Modifier.width(5.dp))
                Text("Play Store'da Aç", style = MaterialTheme.typography.labelSmall, color = AcidLime, maxLines = 1)
            }
        }
    }
}

@Composable
private fun PaymentMethodPill(
    icon: ImageVector,
    label: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(ObsidianVoid.copy(alpha = 0.7f))
            .border(1.dp, color.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MistLavender)
    }
}

@Composable
private fun StoreSectionHeader(
    title: String,
    subtitle: String,
    accentColor: Color,
    icon: ImageVector
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = accentColor
            )
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MistLavender
        )
    }
}

@Composable
private fun FeaturedBundleBanner(
    pack: StoreOfferPack,
    displayPrice: String,
    isLiveSku: Boolean,
    onClick: () -> Unit
) {
    val shimmer = rememberInfiniteTransition(label = "featured_shimmer")
    val pulse by shimmer.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "featured_pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        HotMagenta.copy(alpha = 0.28f),
                        SwarmViolet.copy(alpha = 0.24f),
                        NeonPanel
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        HotMagenta.copy(alpha = 0.75f + pulse * 0.25f),
                        SolarGold,
                        ElectricCyan.copy(alpha = 0.75f + pulse * 0.25f)
                    )
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag("store_pack_${pack.id}"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = SolarGold, modifier = Modifier.size(18.dp))
                Text(
                    text = if (isLiveSku) "ÖNE ÇIKAN • GOOGLE PLAY" else "ÖNE ÇIKAN KAMPANYA",
                    style = MaterialTheme.typography.labelMedium,
                    color = SolarGold
                )
            }
            if (pack.badge != null) {
                Text(
                    text = pack.badge,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = ObsidianVoid,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Brush.horizontalGradient(listOf(SolarGold, AcidLime)))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Text(
            text = pack.title,
            style = MaterialTheme.typography.titleLarge,
            color = IceWhite
        )
        Text(
            text = pack.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MistLavender
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                RewardChip(
                    amount = "+${pack.coinAmount}",
                    label = "Coin",
                    color = SolarGold,
                    icon = Icons.Filled.MonetizationOn
                )
                RewardChip(
                    amount = "+${pack.crystalAmount}",
                    label = "Kristal",
                    color = HotMagenta,
                    icon = Icons.Filled.Diamond
                )
            }

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        val s = 1f + pulse * 0.03f
                        scaleX = s
                        scaleY = s
                    }
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.horizontalGradient(listOf(HotMagenta, SolarGold)))
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            ) {
                Text(
                    text = displayPrice,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = ObsidianVoid
                )
            }
        }
    }
}

@Composable
private fun FreeSupplyGiftCard(
    pack: StoreOfferPack,
    canClaim: Boolean,
    cooldownLabel: String?,
    onClick: () -> Unit
) {
    val accent = if (canClaim) AcidLime else DimSlate
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(accent.copy(alpha = if (canClaim) 0.20f else 0.12f), NeonPanel)
                )
            )
            .border(1.5.dp, accent.copy(alpha = if (canClaim) 0.8f else 0.45f), RoundedCornerShape(16.dp))
            .clickable(enabled = canClaim, onClick = onClick)
            .padding(12.dp)
            .testTag("store_pack_${pack.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(accent.copy(alpha = 0.22f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (canClaim) Icons.Filled.CardGiftcard else Icons.Filled.Schedule,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(26.dp)
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = pack.title,
                style = MaterialTheme.typography.titleSmall,
                color = IceWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (canClaim) {
                    "+${pack.coinAmount} Coin  •  +${pack.crystalAmount} Özel Kristal"
                } else {
                    "Bugün alındı • ${cooldownLabel ?: "24 saat sonra"}"
                },
                style = MaterialTheme.typography.labelMedium,
                color = if (canClaim) AcidLime else MistLavender
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (canClaim) AcidLime else NeonPanelElevated)
                .padding(horizontal = 12.dp, vertical = 9.dp)
        ) {
            Text(
                text = if (canClaim) "ÜCRETSİZ" else "ALINDI",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (canClaim) ObsidianVoid else DimSlate
            )
        }
    }
}

@Composable
private fun StoreOfferGridCard(
    pack: StoreOfferPack,
    displayPrice: String,
    isCrystalFocus: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = pack.accentColor
    val mainIcon = if (isCrystalFocus) Icons.Filled.Diamond else Icons.Filled.MonetizationOn
    val mainAmountText = if (isCrystalFocus) "+${pack.crystalAmount}" else "+${pack.coinAmount}"
    val mainUnitText = if (isCrystalFocus) "Özel Kristal" else "Coin"
    val bonusText = when {
        isCrystalFocus && pack.coinAmount > 0 -> "+${pack.coinAmount} Bonus Coin"
        !isCrystalFocus && pack.crystalAmount > 0 -> "+${pack.crystalAmount} Bonus Kristal"
        else -> null
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    listOf(accent.copy(alpha = 0.16f), NeonPanel)
                )
            )
            .border(1.5.dp, accent.copy(alpha = 0.65f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
            .testTag("store_pack_${pack.id}")
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (pack.badge != null) {
                Text(
                    text = pack.badge,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = ObsidianVoid,
                    maxLines = 1,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accent)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(18.dp))
            }

            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(accent.copy(alpha = 0.35f), ObsidianVoid.copy(alpha = 0.5f))
                        )
                    )
                    .border(1.5.dp, accent.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(mainIcon, contentDescription = null, tint = accent, modifier = Modifier.size(28.dp))
            }

            Text(
                text = mainAmountText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = IceWhite
            )
            Text(
                text = mainUnitText,
                style = MaterialTheme.typography.labelSmall,
                color = accent
            )
            Text(
                text = pack.title,
                style = MaterialTheme.typography.labelMedium,
                color = MistLavender,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            if (bonusText != null) {
                Text(
                    text = bonusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCrystalFocus) SolarGold else HotMagenta,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Spacer(modifier = Modifier.height(15.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = displayPrice,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = ObsidianVoid
                )
            }
        }
    }
}

@Composable
private fun CoinExchangeRow(
    pack: StoreOfferPack,
    canAfford: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NeonPanel)
            .border(
                width = 1.5.dp,
                color = if (canAfford) ElectricCyan.copy(alpha = 0.7f) else NeonBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = canAfford, onClick = onClick)
            .padding(12.dp)
            .testTag("store_pack_${pack.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(HotMagenta.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Diamond, contentDescription = null, tint = HotMagenta, modifier = Modifier.size(26.dp))
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(pack.title, style = MaterialTheme.typography.titleSmall, color = IceWhite)
                if (pack.badge != null) {
                    Text(
                        text = pack.badge,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ObsidianVoid,
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(ElectricCyan)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }
            Text(pack.subtitle, style = MaterialTheme.typography.bodySmall, color = MistLavender)
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (canAfford) SolarGold else NeonPanelElevated)
                .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Filled.MonetizationOn,
                contentDescription = null,
                tint = if (canAfford) ObsidianVoid else DimSlate,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = "${pack.coinExchangeCost}",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (canAfford) ObsidianVoid else DimSlate
            )
        }
    }
}

@Composable
private fun RewardChip(
    amount: String,
    label: String,
    color: Color,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(ObsidianVoid.copy(alpha = 0.72f))
            .border(1.dp, color.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(15.dp))
        Text(
            text = "$amount $label",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = IceWhite
        )
    }
}

@Composable
fun StorePurchaseDialog(
    pack: StoreOfferPack,
    profile: PlayerProfile,
    billingState: PlayBillingUiState,
    onDismiss: () -> Unit,
    onLaunchPlayPurchase: () -> Unit,
    onClaimDailyGift: () -> Unit,
    onExchangeCoins: () -> Unit,
    onOpenPlayStore: () -> Unit
) {
    val isExchange = pack.category == StorePackCategory.COIN_EXCHANGE
    val isFreeGift = pack.category == StorePackCategory.FREE_GIFT
    val isRealMoney = pack.isRealMoneyIap
    val livePrice = resolveDisplayPrice(pack, billingState)
    val isSkuActive = pack.googlePlayProductId != null && pack.googlePlayProductId in billingState.availableProductIds
    val canConfirm = when {
        isExchange -> profile.coins >= pack.coinExchangeCost
        isFreeGift -> profile.canClaimDailyGift()
        else -> !billingState.isProcessingPurchase
    }
    val accent = pack.accentColor

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(NeonPanelElevated, ObsidianVoid)
                    )
                )
                .border(2.dp, accent.copy(alpha = 0.85f), RoundedCornerShape(24.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.2f))
                    .border(2.dp, accent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (pack.category) {
                        StorePackCategory.FREE_GIFT -> Icons.Filled.CardGiftcard
                        StorePackCategory.CRYSTAL_PACK -> Icons.Filled.Diamond
                        StorePackCategory.COIN_EXCHANGE -> Icons.Filled.SwapHoriz
                        StorePackCategory.FEATURED_BUNDLE -> Icons.Filled.AutoAwesome
                        StorePackCategory.COIN_PACK -> Icons.Filled.ShoppingCart
                    },
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(34.dp)
                )
            }

            Text(
                text = pack.title,
                style = MaterialTheme.typography.titleLarge,
                color = IceWhite,
                textAlign = TextAlign.Center
            )
            Text(
                text = pack.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MistLavender,
                textAlign = TextAlign.Center
            )

            // Reward breakdown card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(NeonPanel.copy(alpha = 0.9f))
                    .border(1.dp, NeonBorder, RoundedCornerShape(14.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "PAKET İÇERİĞİ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MistLavender
                )
                if (pack.coinAmount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.MonetizationOn, contentDescription = null, tint = SolarGold, modifier = Modifier.size(20.dp))
                        Text(
                            text = "+${pack.coinAmount} Arena Coin",
                            style = MaterialTheme.typography.titleSmall,
                            color = SolarGold
                        )
                    }
                }
                if (pack.crystalAmount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Diamond, contentDescription = null, tint = HotMagenta, modifier = Modifier.size(20.dp))
                        Text(
                            text = "+${pack.crystalAmount} Özel Vex Kristal",
                            style = MaterialTheme.typography.titleSmall,
                            color = HotMagenta
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = AcidLime, modifier = Modifier.size(18.dp))
                    Text(
                        text = if (isRealMoney) {
                            "Google Play onayı sonrası anında teslim & otomatik kayıt"
                        } else {
                            "Anında hesaba eklenir ve otomatik kaydedilir"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = AcidLime
                    )
                }
            }

            // Real Google Play Payment Info Notice
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
                        text = when {
                            isExchange -> "Oyun İçi Takas: ${pack.coinExchangeCost} Coin harcanır"
                            isFreeGift -> "24 saatte bir ücretsiz alınabilir"
                            else -> "Google Play Faturalandırma ($livePrice)"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ElectricCyan
                    )
                }
                if (isRealMoney) {
                    Text(
                        text = if (isSkuActive) {
                            "Kredi/Banka Kartı, Mobil Ödeme (Operatör Faturası) ve Google Play Bakiyesi ile ödeyebilirsin."
                        } else {
                            "Ürün Kodu: ${pack.googlePlayProductId}. Ödeme penceresi Google Play Store yayını ve ürün tanımlaması sonrası aktif olur."
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MistLavender
                    )
                }
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
                    Text("İPTAL", style = MaterialTheme.typography.labelLarge, color = MistLavender)
                }

                Button(
                    onClick = {
                        when {
                            isExchange -> onExchangeCoins()
                            isFreeGift -> onClaimDailyGift()
                            else -> onLaunchPlayPurchase()
                        }
                    },
                    enabled = canConfirm,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accent,
                        contentColor = ObsidianVoid
                    ),
                    modifier = Modifier
                        .weight(1.6f)
                        .height(48.dp)
                        .testTag("confirm_purchase_button")
                ) {
                    Text(
                        text = when {
                            isExchange -> "TAKAS YAP"
                            isFreeGift -> "ÜCRETSİZ AL"
                            billingState.isProcessingPurchase -> "BEKLENİYOR..."
                            else -> "GOOGLE PLAY İLE ÖDE"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                }
            }

            if (isRealMoney && !isSkuActive) {
                OutlinedButton(
                    onClick = onOpenPlayStore,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("dialog_open_play_store_button")
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
