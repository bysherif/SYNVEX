package com.example.game

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.billing.PlayBillingManager
import com.example.billing.PlayBillingUiState
import com.example.core.BeatManager
import com.example.core.GamePhase
import com.example.core.HudSnapshot
import com.example.core.UpgradeCardOption
import com.example.data.AchievementId
import com.example.data.CrystalPackOption
import com.example.data.HighScoreStore
import com.example.data.MetaUpgradeId
import com.example.data.PlayerAuraId
import com.example.data.PlayerColorId
import com.example.data.PlayerShapeId
import com.example.data.StageCatalog
import com.example.data.StoreOfferPack
import com.example.data.VipSubscriptionPlan
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class StoreToastState(
    val message: String,
    val isError: Boolean = false
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val profileStore = HighScoreStore(application.applicationContext)
    val beatManager = BeatManager(bpm = 128)

    private val _purchaseToast = MutableStateFlow<StoreToastState?>(null)
    val purchaseToast: StateFlow<StoreToastState?> = _purchaseToast.asStateFlow()
    private var toastClearJob: Job? = null

    private val billingManager = PlayBillingManager(
        context = application.applicationContext,
        scope = viewModelScope,
        onVerifiedPurchase = { purchaseToken, pack, quantity ->
            profileStore.recordVerifiedPlayPurchase(purchaseToken, pack, quantity)
        },
        onVerifiedSubscription = { plan, purchaseToken, isAutoRenewing ->
            profileStore.syncVerifiedSubscription(plan, purchaseToken, isAutoRenewing)
        },
        onToastMessage = { message, isError ->
            showStoreToast(message, isError)
        }
    )

    val billingState: StateFlow<PlayBillingUiState> = billingManager.uiState

    val engine: GameEngine = GameEngine(
        beatManager = beatManager,
        onSaveProgressDelta = { deltaCoins, deltaCrystals, deltaKills, deltaMissions, survivalSeconds, runKills, maxCombo, stageNumber, victory, isRunFinished ->
            viewModelScope.launch {
                profileStore.saveProgressDelta(
                    deltaCoins = deltaCoins,
                    deltaCrystals = deltaCrystals,
                    deltaKills = deltaKills,
                    deltaMissions = deltaMissions,
                    survivalSeconds = survivalSeconds,
                    runKills = runKills,
                    maxCombo = maxCombo,
                    stageNumber = stageNumber,
                    victory = victory,
                    isRunFinished = isRunFinished
                )
            }
        }
    )

    private val _hudState = MutableStateFlow(engine.snapshot())
    val hudState: StateFlow<HudSnapshot> = _hudState.asStateFlow()

    private val _shopInitialTab = MutableStateFlow(0)
    val shopInitialTab: StateFlow<Int> = _shopInitialTab.asStateFlow()

    @Volatile
    private var lastHudSyncNanos: Long = 0L

    init {
        viewModelScope.launch {
            profileStore.profileFlow.collect { profile ->
                engine.updateProfile(profile)
                _hudState.value = engine.snapshot()
            }
        }
        billingManager.startConnection()
    }

    private fun showStoreToast(message: String, isError: Boolean) {
        toastClearJob?.cancel()
        _purchaseToast.value = StoreToastState(message = message, isError = isError)
        toastClearJob = viewModelScope.launch {
            delay(if (isError) 3800L else 3000L)
            _purchaseToast.value = null
        }
    }

    fun onHostResumed() {
        billingManager.onHostResumed()
    }

    fun refreshPlayStore() {
        billingManager.refreshStore()
    }

    fun launchRealPurchase(activity: Activity?, pack: StoreOfferPack) {
        if (activity == null) {
            showStoreToast("Google Play ödeme penceresi başlatılamadı.", true)
            return
        }
        billingManager.launchPurchaseFlow(activity, pack)
    }

    fun launchVipSubscription(activity: Activity?, plan: VipSubscriptionPlan) {
        if (activity == null) {
            showStoreToast("Google Play abonelik ekranı başlatılamadı.", true)
            return
        }
        billingManager.launchSubscriptionFlow(activity, plan)
    }

    fun claimVipDailyReward() {
        viewModelScope.launch {
            val result = profileStore.claimVipDailyReward()
            if (result != null) {
                val prefix = if (result.isMegaBonus) {
                    "7. GÜN 2x MEGA VIP KASA!"
                } else {
                    "Günlük VIP Hediye (${result.streakDay}/7) Alındı!"
                }
                showStoreToast(
                    "$prefix +${result.coins} Sarı Coin & +${result.crystals} Kırmızı Kristal",
                    false
                )
            } else {
                showStoreToast("Günlük VIP hediyesi zaten alındı veya aktif abonelik yok.", true)
            }
        }
    }

    fun openPlayStoreListing(context: Context) {
        PlayBillingManager.openPlayStoreListing(context)
    }

    fun openPlaySubscriptions(context: Context) {
        val activeSku = engine.profile.activeVipPlan?.googlePlayProductId
        PlayBillingManager.openPlaySubscriptionsManagement(context, activeSku)
    }

    fun claimDailyFreeGift(pack: StoreOfferPack) {
        viewModelScope.launch {
            val claimed = profileStore.claimDailyFreeGift(pack)
            if (claimed) {
                showStoreToast("Günlük Destek Kasası alındı! +${pack.coinAmount} Coin & +${pack.crystalAmount} Kristal", false)
            } else {
                showStoreToast("Günlük ücretsiz kasa zaten alındı. 24 saat dolunca tekrar açılır.", true)
            }
        }
    }

    fun exchangeCoinsForCrystals(pack: StoreOfferPack) {
        viewModelScope.launch {
            val ok = profileStore.exchangeCoinsForCrystals(pack)
            if (ok) {
                showStoreToast("Takas tamamlandı! +${pack.crystalAmount} Özel Kristal eklendi.", false)
            } else {
                showStoreToast("Yetersiz Coin bakiyesi (${pack.coinExchangeCost} Coin gerekli).", true)
            }
        }
    }

    /**
     * Called from the dedicated GPU SurfaceView render thread while holding [GameEngine.simLock].
     * Throttles Compose HUD emissions to ~4 Hz (240ms) unless phase, skill points, or Nova readiness change.
     */
    fun onFrameLocked(dtSeconds: Float, nowNanos: Long) {
        val prevPhase = engine.phase
        val prevPoints = engine.player.unspentSkillPoints
        val prevNovaReady = engine.player.isNovaReady
        engine.stepFrameLocked(dtSeconds, nowNanos)
        if (engine.phase != prevPhase ||
            engine.player.unspentSkillPoints != prevPoints ||
            engine.player.isNovaReady != prevNovaReady ||
            nowNanos - lastHudSyncNanos >= 240_000_000L
        ) {
            lastHudSyncNanos = nowNanos
            _hudState.value = engine.snapshotLocked()
        }
    }

    fun selectStage(stageNumber: Int) {
        viewModelScope.launch {
            profileStore.selectStage(stageNumber)
        }
    }

    fun startNewRun() {
        engine.startNewRun(engine.profile.selectedStage)
        _hudState.value = engine.snapshot()
    }

    fun startNextStage() {
        val next = (engine.currentStage.stageNumber + 1).coerceAtMost(StageCatalog.stages.size)
        viewModelScope.launch {
            profileStore.selectStage(next)
        }
        engine.startNewRun(next)
        _hudState.value = engine.snapshot()
    }

    fun triggerDash() {
        engine.triggerDash()
        _hudState.value = engine.snapshot()
    }

    fun triggerOverdriveNova() {
        engine.triggerOverdriveNova()
        _hudState.value = engine.snapshot()
    }

    fun openShop(initialTab: Int = 0) {
        _shopInitialTab.value = initialTab
        engine.openShop()
        _hudState.value = engine.snapshot()
        if (initialTab == 1) {
            billingManager.onHostResumed()
        }
    }

    fun closeShop() {
        engine.closeShop()
        _hudState.value = engine.snapshot()
    }

    fun openUpgradeMenu() {
        engine.openUpgradeMenu()
        _hudState.value = engine.snapshot()
    }

    fun closeUpgradeMenu() {
        engine.closeUpgradeMenu()
        _hudState.value = engine.snapshot()
    }

    fun selectUpgrade(option: UpgradeCardOption) {
        engine.selectUpgrade(option)
        _hudState.value = engine.snapshot()
    }

    fun togglePause() {
        if (engine.phase == GamePhase.PLAYING) {
            engine.pauseGame()
        } else if (engine.phase == GamePhase.PAUSED) {
            engine.resumeGame()
        }
        _hudState.value = engine.snapshot()
    }

    fun pauseIfPlaying() {
        if (engine.phase == GamePhase.PLAYING) {
            engine.pauseGame()
            _hudState.value = engine.snapshot()
        }
    }

    fun toggleMute() {
        engine.toggleMute()
        _hudState.value = engine.snapshot()
    }

    fun returnToMenu() {
        engine.returnToMenu()
        _hudState.value = engine.snapshot()
    }

    fun buyOrEquipShape(shape: PlayerShapeId) {
        viewModelScope.launch {
            profileStore.buyAndEquipShape(shape)
        }
    }

    fun buyOrEquipColor(color: PlayerColorId) {
        viewModelScope.launch {
            profileStore.buyAndEquipColor(color)
        }
    }

    fun buyOrEquipAura(aura: PlayerAuraId) {
        viewModelScope.launch {
            profileStore.buyAndEquipAura(aura)
        }
    }

    fun buyMetaUpgrade(id: MetaUpgradeId) {
        viewModelScope.launch {
            profileStore.buyMetaUpgrade(id)
        }
    }

    fun claimAchievement(id: AchievementId) {
        viewModelScope.launch {
            profileStore.claimAchievement(id)
        }
    }

    fun claimCrystalPack(pack: CrystalPackOption) {
        viewModelScope.launch {
            profileStore.claimCrystalPack(pack)
        }
    }

    override fun onCleared() {
        super.onCleared()
        billingManager.endConnection()
        beatManager.stop()
    }
}
