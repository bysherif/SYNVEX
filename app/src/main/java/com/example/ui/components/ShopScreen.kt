package com.example.ui.components

import android.graphics.Paint
import android.graphics.Path
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Upgrade
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.Warning
import com.example.R
import com.example.billing.PlayBillingUiState
import com.example.data.AchievementId
import com.example.data.CurrencyType
import com.example.data.MetaUpgradeId
import com.example.data.PlayerAuraId
import com.example.data.PlayerColorId
import com.example.data.PlayerProfile
import com.example.data.PlayerShapeId
import com.example.data.StoreOfferPack
import com.example.data.VipSubscriptionPlan
import com.example.game.StoreToastState
import com.example.ui.theme.AcidLime
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DimSlate
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.HotMagenta
import com.example.ui.theme.IceWhite
import com.example.ui.theme.MidnightStage
import com.example.ui.theme.MistLavender
import com.example.ui.theme.NeonBorder
import com.example.ui.theme.NeonPanel
import com.example.ui.theme.NeonPanelElevated
import com.example.ui.theme.ObsidianVoid
import com.example.ui.theme.SolarGold
import kotlin.math.PI

@Composable
fun LiveAvatarPreviewBox(
    shape: PlayerShapeId,
    color: PlayerColorId,
    aura: PlayerAuraId,
    sizeDp: Int = 112,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "avatar_preview")
    val animTime by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283185f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "avatar_time"
    )
    val beatCycle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 469, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "avatar_beat"
    )

    val path = remember { Path() }
    val fillPaint = remember { Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL } }
    val strokePaint = remember { Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND } }

    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(listOf(MidnightStage, ObsidianVoid))
            )
            .border(1.5.dp, NeonBorder, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val nc = drawContext.canvas.nativeCanvas
            val cx = size.width * 0.5f
            val cy = size.height * 0.5f
            val beatFlash = (1f - beatCycle).let { it * it }
            val r = (minOf(size.width, size.height) * 0.22f) * (1f + beatFlash * 0.14f)
            val primary = PlayerAvatarRenderer.resolvePrimaryArgb(color, animTime)
            val secondary = PlayerAvatarRenderer.resolveSecondaryArgb(color, animTime)

            PlayerAvatarRenderer.drawAura(
                canvas = nc,
                cx = cx,
                cy = cy,
                radius = r,
                auraId = aura,
                primaryArgb = primary,
                secondaryArgb = secondary,
                beatFlash = beatFlash,
                timeSeconds = animTime,
                fillPaint = fillPaint,
                strokePaint = strokePaint
            )
            PlayerAvatarRenderer.drawShape(
                canvas = nc,
                path = path,
                cx = cx,
                cy = cy,
                radius = r,
                facingAngle = -PI.toFloat() * 0.5f + kotlin.math.sin(animTime) * 0.35f,
                shapeId = shape,
                fillArgb = primary,
                accentArgb = secondary,
                fillPaint = fillPaint,
                strokePaint = strokePaint
            )
        }
    }
}

@Composable
fun WalletStrip(
    coins: Int,
    crystals: Int,
    onWalletClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(NeonPanel.copy(alpha = 0.92f))
                .border(1.2.dp, SolarGold.copy(alpha = 0.72f), RoundedCornerShape(20.dp))
                .then(
                    if (onWalletClick != null) {
                        Modifier
                            .clickable(onClick = onWalletClick)
                            .testTag("wallet_coin_add_button")
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = 9.dp, vertical = 5.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.MonetizationOn,
                contentDescription = stringResource(R.string.currency_coin),
                tint = SolarGold,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "$coins",
                style = MaterialTheme.typography.labelLarge,
                color = SolarGold
            )
            if (onWalletClick != null) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(SolarGold),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Coin Satın Al",
                        tint = ObsidianVoid,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(NeonPanel.copy(alpha = 0.92f))
                .border(1.2.dp, HotMagenta.copy(alpha = 0.75f), RoundedCornerShape(20.dp))
                .then(
                    if (onWalletClick != null) {
                        Modifier
                            .clickable(onClick = onWalletClick)
                            .testTag("wallet_crystal_add_button")
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = 9.dp, vertical = 5.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Diamond,
                contentDescription = stringResource(R.string.currency_crystal),
                tint = HotMagenta,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "$crystals",
                style = MaterialTheme.typography.labelLarge,
                color = HotMagenta
            )
            if (onWalletClick != null) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(HotMagenta),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Özel Kristal Satın Al",
                        tint = IceWhite,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ShopScreen(
    profile: PlayerProfile,
    billingState: PlayBillingUiState,
    initialTab: Int = 0,
    purchaseToast: StoreToastState? = null,
    onBack: () -> Unit,
    onPlayNow: () -> Unit,
    onBuyOrEquipShape: (PlayerShapeId) -> Unit,
    onBuyOrEquipColor: (PlayerColorId) -> Unit,
    onBuyOrEquipAura: (PlayerAuraId) -> Unit,
    onBuyMetaUpgrade: (MetaUpgradeId) -> Unit,
    onClaimAchievement: (AchievementId) -> Unit,
    onLaunchPlayPurchase: (StoreOfferPack) -> Unit,
    onLaunchVipSubscription: (VipSubscriptionPlan) -> Unit,
    onClaimVipDailyReward: () -> Unit,
    onManageSubscriptions: () -> Unit,
    onClaimDailyGift: (StoreOfferPack) -> Unit,
    onExchangeCoins: (StoreOfferPack) -> Unit,
    onRefreshPlayStore: () -> Unit,
    onOpenPlayStore: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 4)) }
    var previewShape by remember(profile.equippedShape) { mutableStateOf(profile.equippedShape) }
    var previewColor by remember(profile.equippedColor) { mutableStateOf(profile.equippedColor) }
    var previewAura by remember(profile.equippedAura) { mutableStateOf(profile.equippedAura) }
    var pendingStorePack by remember { mutableStateOf<StoreOfferPack?>(null) }
    var pendingVipPlan by remember { mutableStateOf<VipSubscriptionPlan?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(MidnightStage, ObsidianVoid))
            )
    ) {
        NeonAmbientParticles(
            accentColor = when (selectedTab) {
                1 -> HotMagenta
                2 -> SolarGold
                0 -> AcidLime
                else -> ElectricCyan
            }
        )

        Column(
            modifier = Modifier
                .widthIn(max = 580.dp)
                .fillMaxSize()
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Header: Back + Title + Wallet
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(CircleShape)
                            .background(NeonPanel)
                            .testTag("shop_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_main_menu),
                            tint = IceWhite
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.shop_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = ElectricCyan
                    )
                }

                WalletStrip(
                    coins = profile.coins,
                    crystals = profile.crystals,
                    onWalletClick = { selectedTab = 1 }
                )
            }

            // Compact Live Ship & Power Showcase Card
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(NeonPanel.copy(alpha = 0.9f))
                    .border(1.5.dp, ElectricCyan.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                LiveAvatarPreviewBox(
                    shape = previewShape,
                    color = previewColor,
                    aura = previewAura,
                    sizeDp = 82
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Filled.Bolt, contentDescription = null, tint = SolarGold, modifier = Modifier.size(15.dp))
                        Text(
                            text = "GÜÇ: ${profile.powerRating}  •  SV ${profile.accountLevel}",
                            style = MaterialTheme.typography.labelMedium,
                            color = SolarGold
                        )
                        if (profile.isVipSubscribed) {
                            Text(
                                text = "VIP",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = ObsidianVoid,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Brush.horizontalGradient(listOf(SolarGold, HotMagenta)))
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = "${previewShape.title} • ${previewColor.title}",
                        style = MaterialTheme.typography.titleMedium,
                        color = IceWhite
                    )
                    Text(
                        text = "✓ Tüm gelişmeler otomatik kaydedilir",
                        style = MaterialTheme.typography.labelSmall,
                        color = AcidLime
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Button(
                        onClick = onPlayNow,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 7.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricCyan,
                            contentColor = ObsidianVoid
                        ),
                        modifier = Modifier.testTag("shop_play_button")
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.action_play), style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            // 5 Dedicated Workshop & Storefront Category Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                ShopTabChip("GÜÇ", Icons.Filled.Upgrade, selectedTab == 0, AcidLime, profile.affordableUpgradesCount, Modifier.weight(1f)) {
                    selectedTab = 0
                }
                ShopTabChip("KASA", Icons.Filled.ShoppingCart, selectedTab == 1, HotMagenta, profile.claimableStoreGiftsCount(), Modifier.weight(1f).testTag("shop_tab_kasa")) {
                    selectedTab = 1
                }
                ShopTabChip("ÖDÜL", Icons.Filled.EmojiEvents, selectedTab == 2, SolarGold, profile.claimableAchievementsCount, Modifier.weight(1f)) {
                    selectedTab = 2
                }
                ShopTabChip("ŞEKİL", Icons.Filled.RocketLaunch, selectedTab == 3, ElectricCyan, 0, Modifier.weight(1f)) {
                    selectedTab = 3
                }
                ShopTabChip("RENK", Icons.Filled.Palette, selectedTab == 4, HotMagenta, 0, Modifier.weight(1f)) {
                    selectedTab = 4
                }
            }

            // Tab Content List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                when (selectedTab) {
                    0 -> {
                        item {
                            Text(
                                text = "KALICI SAVAŞ GÜÇLENDİRMELERİ",
                                style = MaterialTheme.typography.labelLarge,
                                color = AcidLime
                            )
                            Text(
                                text = "Her yükseltme tüm bölümlerde geçerlidir ve anında kaydedilir.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MistLavender
                            )
                        }
                        items(MetaUpgradeId.entries) { meta ->
                            val lv = profile.metaLevel(meta)
                            val maxed = lv >= meta.maxLevel
                            val cost = meta.costForNextLevel(lv)
                            val canBuy = !maxed && profile.coins >= cost
                            MetaUpgradeRow(
                                meta = meta,
                                level = lv,
                                cost = cost,
                                maxed = maxed,
                                canBuy = canBuy,
                                onBuy = { onBuyMetaUpgrade(meta) }
                            )
                        }
                    }

                    1 -> {
                        storeVaultItems(
                            profile = profile,
                            billingState = billingState,
                            onSelectPack = { pack -> pendingStorePack = pack },
                            onSelectVipPlan = { plan -> pendingVipPlan = plan },
                            onClaimVipDaily = onClaimVipDailyReward,
                            onManageSubscriptions = onManageSubscriptions,
                            onRefreshPlayStore = onRefreshPlayStore,
                            onOpenPlayStore = onOpenPlayStore
                        )
                    }

                    2 -> {
                        item {
                            Text(
                                text = "KALICI GÖREVLER & ÖDÜLLER",
                                style = MaterialTheme.typography.labelLarge,
                                color = SolarGold
                            )
                            Text(
                                text = "Oyun boyunca ilerlemen otomatik takip edilir. Tamamlanan ödülleri topla!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MistLavender
                            )
                        }
                        items(AchievementId.entries) { ach ->
                            val progress = ach.currentProgress(profile)
                            val completed = progress >= ach.targetValue
                            val claimed = profile.claimedAchievements.contains(ach)
                            AchievementRow(
                                achievement = ach,
                                currentProgress = progress,
                                completed = completed,
                                claimed = claimed,
                                onClaim = { onClaimAchievement(ach) }
                            )
                        }
                    }

                    3 -> {
                        item {
                            Text(
                                text = "GEMİ ŞEKİLLERİ",
                                style = MaterialTheme.typography.labelLarge,
                                color = ElectricCyan
                            )
                        }
                        items(PlayerShapeId.entries) { shape ->
                            val unlocked = profile.unlockedShapes.contains(shape)
                            val equipped = profile.equippedShape == shape
                            val canAfford = canAfford(shape.currency, shape.price, profile)
                            ShopCosmeticRow(
                                title = shape.title,
                                subtitle = shape.subtitle,
                                accentColor = if (shape.currency == CurrencyType.CRYSTAL) HotMagenta else ElectricCyan,
                                currency = shape.currency,
                                price = shape.price,
                                unlocked = unlocked,
                                equipped = equipped,
                                canAfford = canAfford,
                                leadingPreview = {
                                    LiveAvatarPreviewBox(
                                        shape = shape,
                                        color = previewColor,
                                        aura = previewAura,
                                        sizeDp = 56
                                    )
                                },
                                onRowClick = { previewShape = shape },
                                onActionClick = {
                                    previewShape = shape
                                    onBuyOrEquipShape(shape)
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "ENERJİ AURALARI",
                                style = MaterialTheme.typography.labelLarge,
                                color = AcidLime
                            )
                        }
                        items(PlayerAuraId.entries) { aura ->
                            val unlocked = profile.unlockedAuras.contains(aura)
                            val equipped = profile.equippedAura == aura
                            val canAfford = canAfford(aura.currency, aura.price, profile)
                            ShopCosmeticRow(
                                title = aura.title,
                                subtitle = aura.subtitle,
                                accentColor = if (aura.currency == CurrencyType.CRYSTAL) SolarGold else AcidLime,
                                currency = aura.currency,
                                price = aura.price,
                                unlocked = unlocked,
                                equipped = equipped,
                                canAfford = canAfford,
                                leadingPreview = {
                                    LiveAvatarPreviewBox(
                                        shape = previewShape,
                                        color = previewColor,
                                        aura = aura,
                                        sizeDp = 56
                                    )
                                },
                                onRowClick = { previewAura = aura },
                                onActionClick = {
                                    previewAura = aura
                                    onBuyOrEquipAura(aura)
                                }
                            )
                        }
                    }

                    4 -> {
                        item {
                            Text(
                                text = "NEON & VIP KROM RENKLER",
                                style = MaterialTheme.typography.labelLarge,
                                color = HotMagenta
                            )
                        }
                        items(PlayerColorId.entries) { colorItem ->
                            val unlocked = profile.unlockedColors.contains(colorItem)
                            val equipped = profile.equippedColor == colorItem
                            val canAfford = canAfford(colorItem.currency, colorItem.price, profile)
                            ShopCosmeticRow(
                                title = colorItem.title,
                                subtitle = colorItem.subtitle,
                                accentColor = colorItem.primaryColor,
                                currency = colorItem.currency,
                                price = colorItem.price,
                                unlocked = unlocked,
                                equipped = equipped,
                                canAfford = canAfford,
                                leadingPreview = {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (colorItem.isAnimatedChroma) {
                                                    Brush.sweepGradient(
                                                        listOf(ElectricCyan, AcidLime, SolarGold, HotMagenta, ElectricCyan)
                                                    )
                                                } else {
                                                    Brush.linearGradient(
                                                        listOf(colorItem.primaryColor, colorItem.secondaryColor)
                                                    )
                                                }
                                            )
                                            .border(2.dp, IceWhite.copy(alpha = 0.8f), CircleShape)
                                    )
                                },
                                onRowClick = { previewColor = colorItem },
                                onActionClick = {
                                    previewColor = colorItem
                                    onBuyOrEquipColor(colorItem)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Animated Store Toast Banner (Success or Error)
        AnimatedVisibility(
            visible = purchaseToast != null,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            val isErrorToast = purchaseToast?.isError == true
            val toastBrush = if (isErrorToast) {
                Brush.horizontalGradient(listOf(DangerRed.copy(alpha = 0.96f), SolarGold.copy(alpha = 0.94f)))
            } else {
                Brush.horizontalGradient(listOf(AcidLime.copy(alpha = 0.96f), ElectricCyan.copy(alpha = 0.96f)))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(toastBrush)
                    .border(1.5.dp, IceWhite, RoundedCornerShape(16.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = if (isErrorToast) Icons.Filled.Warning else Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = ObsidianVoid,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = purchaseToast?.message ?: "",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = ObsidianVoid
                )
            }
        }

        // Interactive Google Play / Store Dialog
        pendingStorePack?.let { pack ->
            StorePurchaseDialog(
                pack = pack,
                profile = profile,
                billingState = billingState,
                onDismiss = { pendingStorePack = null },
                onLaunchPlayPurchase = {
                    onLaunchPlayPurchase(pack)
                    pendingStorePack = null
                },
                onClaimDailyGift = {
                    onClaimDailyGift(pack)
                    pendingStorePack = null
                },
                onExchangeCoins = {
                    onExchangeCoins(pack)
                    pendingStorePack = null
                },
                onOpenPlayStore = {
                    onOpenPlayStore()
                    pendingStorePack = null
                }
            )
        }

        // Interactive Google Play VIP Subscription Dialog
        pendingVipPlan?.let { plan ->
            VipSubscriptionDialog(
                plan = plan,
                profile = profile,
                billingState = billingState,
                onDismiss = { pendingVipPlan = null },
                onSubscribe = {
                    onLaunchVipSubscription(plan)
                    pendingVipPlan = null
                },
                onManageSubscriptions = {
                    onManageSubscriptions()
                    pendingVipPlan = null
                },
                onOpenPlayStore = {
                    onOpenPlayStore()
                    pendingVipPlan = null
                }
            )
        }
    }
}

private fun canAfford(currency: CurrencyType, price: Int, profile: PlayerProfile): Boolean {
    return when (currency) {
        CurrencyType.FREE -> true
        CurrencyType.COIN -> profile.coins >= price
        CurrencyType.CRYSTAL -> profile.crystals >= price
    }
}

@Composable
private fun ShopTabChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    color: Color,
    badgeCount: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(if (selected) color.copy(alpha = 0.22f) else NeonPanel)
                .border(
                    width = if (selected) 1.5.dp else 1.dp,
                    color = if (selected) color else NeonBorder,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable(onClick = onClick)
                .padding(vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) color else DimSlate,
                modifier = Modifier.size(17.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (selected) IceWhite else MistLavender
            )
        }
        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(2.dp)
                    .size(15.dp)
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
        }
    }
}

@Composable
private fun AchievementRow(
    achievement: AchievementId,
    currentProgress: Int,
    completed: Boolean,
    claimed: Boolean,
    onClaim: () -> Unit
) {
    val ratio = (currentProgress.toFloat() / achievement.targetValue.coerceAtLeast(1)).coerceIn(0f, 1f)
    val accent = when {
        claimed -> DimSlate
        completed -> AcidLime
        else -> SolarGold
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (completed && !claimed) AcidLime.copy(alpha = 0.12f) else NeonPanel)
            .border(
                width = if (completed && !claimed) 1.8.dp else 1.dp,
                color = if (completed && !claimed) AcidLime else NeonBorder,
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accent.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (claimed) Icons.Filled.CheckCircle else Icons.Filled.EmojiEvents,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(25.dp)
            )
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(achievement.title, style = MaterialTheme.typography.titleMedium, color = IceWhite)
                Text(
                    text = "${currentProgress.coerceAtMost(achievement.targetValue)}/${achievement.targetValue}",
                    style = MaterialTheme.typography.labelSmall,
                    color = accent
                )
            }
            Text(achievement.description, style = MaterialTheme.typography.labelSmall, color = MistLavender)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(ObsidianVoid)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(ratio)
                        .height(6.dp)
                        .background(accent)
                )
            }
            Text(
                text = "Ödül: +${achievement.rewardCoins} Coin  +${achievement.rewardCrystals} Kristal",
                style = MaterialTheme.typography.labelSmall,
                color = SolarGold
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(
                    when {
                        claimed -> NeonPanelElevated
                        completed -> AcidLime
                        else -> NeonPanelElevated
                    }
                )
                .clickable(enabled = completed && !claimed, onClick = onClaim)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = when {
                    claimed -> "ALINDI"
                    completed -> "AL"
                    else -> "%${(ratio * 100).toInt()}"
                },
                style = MaterialTheme.typography.labelMedium,
                color = when {
                    claimed -> DimSlate
                    completed -> ObsidianVoid
                    else -> MistLavender
                }
            )
        }
    }
}

@Composable
private fun ShopCosmeticRow(
    title: String,
    subtitle: String,
    accentColor: Color,
    currency: CurrencyType,
    price: Int,
    unlocked: Boolean,
    equipped: Boolean,
    canAfford: Boolean,
    leadingPreview: @Composable () -> Unit,
    onRowClick: () -> Unit,
    onActionClick: () -> Unit
) {
    val isPremium = currency == CurrencyType.CRYSTAL

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (equipped) accentColor.copy(alpha = 0.14f) else NeonPanel)
            .border(
                width = if (equipped) 2.dp else 1.dp,
                color = when {
                    equipped -> accentColor
                    isPremium -> HotMagenta.copy(alpha = 0.55f)
                    else -> NeonBorder
                },
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onRowClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        leadingPreview()

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = IceWhite
                )
                if (isPremium) {
                    Text(
                        text = "VIP",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = ObsidianVoid,
                        modifier = Modifier
                            .clip(RoundedCornerShape(5.dp))
                            .background(HotMagenta)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    )
                }
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MistLavender
            )
        }

        val btnBg = when {
            equipped -> AcidLime.copy(alpha = 0.18f)
            unlocked -> ElectricCyan
            canAfford && isPremium -> HotMagenta
            canAfford -> SolarGold
            else -> NeonPanelElevated
        }
        val btnText = when {
            equipped -> AcidLime
            unlocked -> ObsidianVoid
            canAfford -> ObsidianVoid
            else -> DimSlate
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(btnBg)
                .border(
                    width = 1.dp,
                    color = if (equipped) AcidLime else Color.Transparent,
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable(enabled = !equipped && (unlocked || canAfford), onClick = onActionClick)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            when {
                equipped -> {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = AcidLime, modifier = Modifier.size(16.dp))
                    Text("SEÇİLİ", style = MaterialTheme.typography.labelMedium, color = AcidLime)
                }
                unlocked -> {
                    Text("KUŞAN", style = MaterialTheme.typography.labelMedium, color = btnText)
                }
                else -> {
                    Icon(
                        imageVector = if (!canAfford) Icons.Filled.Lock else if (isPremium) Icons.Filled.Diamond else Icons.Filled.MonetizationOn,
                        contentDescription = null,
                        tint = btnText,
                        modifier = Modifier.size(15.dp)
                    )
                    Text("$price", style = MaterialTheme.typography.labelMedium, color = btnText)
                }
            }
        }
    }
}

@Composable
private fun MetaUpgradeRow(
    meta: MetaUpgradeId,
    level: Int,
    cost: Int,
    maxed: Boolean,
    canBuy: Boolean,
    onBuy: () -> Unit
) {
    val progress = (level.toFloat() / meta.maxLevel.coerceAtLeast(1)).coerceIn(0f, 1f)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(NeonPanel)
            .border(1.dp, if (canBuy) meta.accentColor.copy(alpha = 0.7f) else NeonBorder, RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(meta.accentColor.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Upgrade, contentDescription = null, tint = meta.accentColor, modifier = Modifier.size(26.dp))
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(meta.title, style = MaterialTheme.typography.titleMedium, color = IceWhite)
                Text("SV $level/${meta.maxLevel}", style = MaterialTheme.typography.labelSmall, color = meta.accentColor)
            }
            Text(meta.bonusDescription, style = MaterialTheme.typography.labelSmall, color = MistLavender)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(ObsidianVoid)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(6.dp)
                        .background(meta.accentColor)
                )
            }
        }

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(if (maxed) AcidLime.copy(alpha = 0.18f) else if (canBuy) SolarGold else NeonPanelElevated)
                .clickable(enabled = canBuy, onClick = onBuy)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (maxed) {
                Text("MAX", style = MaterialTheme.typography.labelMedium, color = AcidLime)
            } else {
                Icon(
                    Icons.Filled.MonetizationOn,
                    contentDescription = null,
                    tint = if (canBuy) ObsidianVoid else DimSlate,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    "$cost",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (canBuy) ObsidianVoid else DimSlate
                )
            }
        }
    }
}


