package com.example

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.GamePhase
import com.example.game.GameViewModel
import com.example.ui.components.GameCanvas
import com.example.ui.components.GameHud
import com.example.ui.components.LevelUpOverlay
import com.example.ui.components.PauseOverlay
import com.example.ui.components.RunSummaryOverlay
import com.example.ui.components.ShopScreen
import com.example.ui.components.TitleScreen
import com.example.ui.theme.SynvexTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        requestHighestRefreshRate()

        setContent {
            SynvexTheme {
                SynvexApp()
            }
        }
    }

    private fun requestHighestRefreshRate() {
        try {
            val activeDisplay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                display
            } else {
                @Suppress("DEPRECATION")
                windowManager.defaultDisplay
            }
            val bestMode = activeDisplay?.supportedModes?.maxByOrNull { it.refreshRate }
            if (bestMode != null) {
                val lp = window.attributes
                lp.preferredDisplayModeId = bestMode.modeId
                lp.preferredRefreshRate = bestMode.refreshRate
                window.attributes = lp
            }
        } catch (_: Throwable) {
        }
    }
}

@Composable
fun SynvexApp(
    viewModel: GameViewModel = viewModel()
) {
    val hud by viewModel.hudState.collectAsStateWithLifecycle()
    val billingState by viewModel.billingState.collectAsStateWithLifecycle()
    val shopInitialTab by viewModel.shopInitialTab.collectAsStateWithLifecycle()
    val purchaseToast by viewModel.purchaseToast.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val hostActivity = remember(context) { context.findActivity() }

    // Stable callback so Compose recompositions never churn GameCanvas AndroidView
    val onFrameLockedCallback = remember(viewModel) {
        { dt: Float, now: Long -> viewModel.onFrameLocked(dt, now) }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> viewModel.pauseIfPlaying()
                Lifecycle.Event.ON_RESUME -> viewModel.onHostResumed()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    BackHandler(enabled = hud.phase != GamePhase.MENU) {
        when (hud.phase) {
            GamePhase.SHOP -> viewModel.closeShop()
            GamePhase.PLAYING -> viewModel.togglePause()
            GamePhase.PAUSED -> viewModel.togglePause()
            GamePhase.LEVEL_UP -> viewModel.closeUpgradeMenu()
            GamePhase.GAME_OVER, GamePhase.VICTORY -> viewModel.returnToMenu()
            GamePhase.MENU -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (hud.phase) {
            GamePhase.MENU -> {
                TitleScreen(
                    profile = hud.profile,
                    onSelectStage = { viewModel.selectStage(it) },
                    onPlayClick = { viewModel.startNewRun() },
                    onOpenUpgradesClick = { viewModel.openShop(initialTab = 0) },
                    onOpenStoreClick = { viewModel.openShop(initialTab = 1) },
                    onOpenAchievementsClick = { viewModel.openShop(initialTab = 2) },
                    onOpenShopClick = { viewModel.openShop(initialTab = 3) }
                )
            }

            GamePhase.SHOP -> {
                ShopScreen(
                    profile = hud.profile,
                    billingState = billingState,
                    initialTab = shopInitialTab,
                    purchaseToast = purchaseToast,
                    onBack = { viewModel.closeShop() },
                    onPlayNow = { viewModel.startNewRun() },
                    onBuyOrEquipShape = { viewModel.buyOrEquipShape(it) },
                    onBuyOrEquipColor = { viewModel.buyOrEquipColor(it) },
                    onBuyOrEquipAura = { viewModel.buyOrEquipAura(it) },
                    onBuyMetaUpgrade = { viewModel.buyMetaUpgrade(it) },
                    onClaimAchievement = { viewModel.claimAchievement(it) },
                    onLaunchPlayPurchase = { viewModel.launchRealPurchase(hostActivity, it) },
                    onLaunchVipSubscription = { viewModel.launchVipSubscription(hostActivity, it) },
                    onClaimVipDailyReward = { viewModel.claimVipDailyReward() },
                    onManageSubscriptions = { viewModel.openPlaySubscriptions(context) },
                    onClaimDailyGift = { viewModel.claimDailyFreeGift(it) },
                    onExchangeCoins = { viewModel.exchangeCoinsForCrystals(it) },
                    onRefreshPlayStore = { viewModel.refreshPlayStore() },
                    onOpenPlayStore = { viewModel.openPlayStoreListing(context) }
                )
            }

            else -> {
                GameCanvas(
                    engine = viewModel.engine,
                    isPlaying = hud.phase == GamePhase.PLAYING,
                    onFrameLocked = onFrameLockedCallback
                )

                GameHud(
                    hud = hud,
                    onPauseClick = { viewModel.togglePause() },
                    onMuteClick = { viewModel.toggleMute() },
                    onOpenUpgrades = { viewModel.openUpgradeMenu() },
                    onDashClick = { viewModel.triggerDash() },
                    onNovaClick = { viewModel.triggerOverdriveNova() }
                )

                when (hud.phase) {
                    GamePhase.LEVEL_UP -> {
                        LevelUpOverlay(
                            level = hud.level,
                            unspentPoints = hud.unspentSkillPoints,
                            options = hud.upgradeOptions,
                            onSelectUpgrade = { viewModel.selectUpgrade(it) },
                            onClose = { viewModel.closeUpgradeMenu() }
                        )
                    }
                    GamePhase.PAUSED -> {
                        PauseOverlay(
                            hud = hud,
                            onResume = { viewModel.togglePause() },
                            onRestart = { viewModel.startNewRun() },
                            onMainMenu = { viewModel.returnToMenu() }
                        )
                    }
                    GamePhase.GAME_OVER -> {
                        RunSummaryOverlay(
                            hud = hud,
                            isVictory = false,
                            onRestart = { viewModel.startNewRun() },
                            onNextStage = { viewModel.startNextStage() },
                            onOpenShop = {
                                viewModel.returnToMenu()
                                viewModel.openShop(initialTab = 0)
                            },
                            onMainMenu = { viewModel.returnToMenu() }
                        )
                    }
                    GamePhase.VICTORY -> {
                        RunSummaryOverlay(
                            hud = hud,
                            isVictory = true,
                            onRestart = { viewModel.startNewRun() },
                            onNextStage = { viewModel.startNextStage() },
                            onOpenShop = {
                                viewModel.returnToMenu()
                                viewModel.openShop(initialTab = 0)
                            },
                            onMainMenu = { viewModel.returnToMenu() }
                        )
                    }
                    else -> Unit
                }
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
