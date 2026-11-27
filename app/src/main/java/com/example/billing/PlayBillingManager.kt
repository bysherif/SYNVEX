package com.example.billing

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.example.data.StoreOfferCatalog
import com.example.data.StoreOfferPack
import com.example.data.VipSubscriptionCatalog
import com.example.data.VipSubscriptionPlan
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

enum class PlayBillingConnectionState {
    CONNECTING,
    READY,
    UNAVAILABLE,
    ERROR
}

data class PlayBillingUiState(
    val connectionState: PlayBillingConnectionState = PlayBillingConnectionState.CONNECTING,
    val livePrices: Map<String, String> = emptyMap(),
    val availableProductIds: Set<String> = emptySet(),
    val availableSubscriptionIds: Set<String> = emptySet(),
    val activeSubscriptionProductId: String? = null,
    val isProcessingPurchase: Boolean = false,
    val hasPendingPayment: Boolean = false,
    val lastMessage: String? = null,
    val isError: Boolean = false
)

/**
 * Production Google Play Billing Library 8.x manager for SYNVEX.
 * Supports both one-time consumable packs (INAPP) and auto-renewing VIP subscriptions (SUBS)
 * natively via Credit/Debit Cards, Mobile Carrier Billing (Mobil Ödeme), Google Play Balance,
 * and Gift Codes — with pending transaction support, automatic service reconnection,
 * token consumption (INAPP), and mandatory purchase acknowledgement (SUBS).
 */
class PlayBillingManager(
    context: Context,
    private val scope: CoroutineScope,
    private val onVerifiedPurchase: suspend (purchaseToken: String, pack: StoreOfferPack, quantity: Int) -> Boolean,
    private val onVerifiedSubscription: suspend (plan: VipSubscriptionPlan?, purchaseToken: String?, isAutoRenewing: Boolean) -> Boolean,
    private val onToastMessage: (message: String, isError: Boolean) -> Unit
) {
    private val appContext = context.applicationContext
    private val productDetailsMap = ConcurrentHashMap<String, ProductDetails>()
    private val subscriptionDetailsMap = ConcurrentHashMap<String, ProductDetails>()
    private val consumingTokens = ConcurrentHashMap.newKeySet<String>()
    private val acknowledgingTokens = ConcurrentHashMap.newKeySet<String>()

    private val _uiState = MutableStateFlow(PlayBillingUiState())
    val uiState: StateFlow<PlayBillingUiState> = _uiState.asStateFlow()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                _uiState.update { it.copy(isProcessingPurchase = false) }
                if (!purchases.isNullOrEmpty()) {
                    purchases.forEach { handlePurchase(it) }
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                _uiState.update {
                    it.copy(
                        isProcessingPurchase = false,
                        lastMessage = "Satın alma işlemi iptal edildi.",
                        isError = false
                    )
                }
                onToastMessage("Satın alma işlemi iptal edildi.", false)
            }
            BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                _uiState.update { it.copy(isProcessingPurchase = false) }
                queryExistingPurchases()
            }
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE,
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {
                _uiState.update {
                    it.copy(
                        connectionState = PlayBillingConnectionState.UNAVAILABLE,
                        isProcessingPurchase = false,
                        lastMessage = "Google Play ödeme servisine ulaşılamıyor.",
                        isError = true
                    )
                }
                onToastMessage("Google Play ödeme servisine ulaşılamıyor. Play Store hesabını kontrol et.", true)
            }
            else -> {
                _uiState.update {
                    it.copy(
                        isProcessingPurchase = false,
                        lastMessage = "Ödeme başlatılamadı (Kod: ${billingResult.responseCode}).",
                        isError = true
                    )
                }
                onToastMessage("Google Play ödemesi tamamlanamadı (Kod: ${billingResult.responseCode}).", true)
            }
        }
    }

    private val billingClient: BillingClient = BillingClient.newBuilder(appContext)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()
                .build()
        )
        .enableAutoServiceReconnection()
        .build()

    fun startConnection() {
        if (billingClient.isReady) {
            queryCatalogAndPendingPurchases()
            return
        }
        _uiState.update { it.copy(connectionState = PlayBillingConnectionState.CONNECTING) }
        try {
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        _uiState.update {
                            it.copy(
                                connectionState = PlayBillingConnectionState.READY,
                                isError = false
                            )
                        }
                        queryCatalogAndPendingPurchases()
                    } else {
                        val state = if (billingResult.responseCode == BillingClient.BillingResponseCode.BILLING_UNAVAILABLE) {
                            PlayBillingConnectionState.UNAVAILABLE
                        } else {
                            PlayBillingConnectionState.ERROR
                        }
                        _uiState.update {
                            it.copy(
                                connectionState = state,
                                lastMessage = "Google Play bağlantısı kurulamadı (${billingResult.responseCode}).",
                                isError = true
                            )
                        }
                    }
                }

                override fun onBillingServiceDisconnected() {
                    // enableAutoServiceReconnection() automatically handles reconnecting on the next API call.
                }
            })
        } catch (_: Throwable) {
            _uiState.update {
                it.copy(
                    connectionState = PlayBillingConnectionState.UNAVAILABLE,
                    lastMessage = "Bu cihazda Google Play faturalandırma servisi kullanılamıyor.",
                    isError = true
                )
            }
        }
    }

    fun onHostResumed() {
        if (billingClient.isReady) {
            queryExistingPurchases()
            queryActiveSubscriptions()
            if (productDetailsMap.isEmpty()) {
                queryProductDetails()
            }
            if (subscriptionDetailsMap.isEmpty()) {
                querySubscriptionDetails()
            }
        } else {
            startConnection()
        }
    }

    fun refreshStore() {
        if (!billingClient.isReady) {
            startConnection()
            onToastMessage("Google Play Store bağlantısı yenileniyor...", false)
        } else {
            queryCatalogAndPendingPurchases()
            onToastMessage("Google Play ürünleri, abonelikler ve satın alımlar güncellendi.", false)
        }
    }

    private fun queryCatalogAndPendingPurchases() {
        queryProductDetails()
        querySubscriptionDetails()
        queryExistingPurchases()
        queryActiveSubscriptions()
    }

    private fun queryProductDetails() {
        val productIds = StoreOfferCatalog.allGooglePlayProductIds
        if (productIds.isEmpty()) return

        val productList = productIds.map { sku ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(sku)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, queryProductDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val list = queryProductDetailsResult.productDetailsList
                val prices = mutableMapOf<String, String>()
                val availableIds = mutableSetOf<String>()
                for (details in list) {
                    productDetailsMap[details.productId] = details
                    availableIds.add(details.productId)
                    val formatted = details.oneTimePurchaseOfferDetails?.formattedPrice
                    if (!formatted.isNullOrBlank()) {
                        prices[details.productId] = formatted
                    }
                }
                _uiState.update { state ->
                    state.copy(
                        connectionState = PlayBillingConnectionState.READY,
                        livePrices = state.livePrices + prices,
                        availableProductIds = availableIds,
                        isError = false
                    )
                }
            }
        }
    }

    private fun querySubscriptionDetails() {
        val subIds = VipSubscriptionCatalog.allGooglePlaySubIds
        if (subIds.isEmpty()) return

        val subList = subIds.map { sku ->
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(sku)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        }
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(subList)
            .build()

        billingClient.queryProductDetailsAsync(params) { billingResult, queryProductDetailsResult ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val list = queryProductDetailsResult.productDetailsList
                val subPrices = mutableMapOf<String, String>()
                val availableSubs = mutableSetOf<String>()
                for (details in list) {
                    subscriptionDetailsMap[details.productId] = details
                    availableSubs.add(details.productId)
                    val offer = details.subscriptionOfferDetails?.firstOrNull()
                    val formatted = offer?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
                    if (!formatted.isNullOrBlank()) {
                        subPrices[details.productId] = formatted
                    }
                }
                _uiState.update { state ->
                    state.copy(
                        connectionState = PlayBillingConnectionState.READY,
                        livePrices = state.livePrices + subPrices,
                        availableSubscriptionIds = availableSubs,
                        isError = false
                    )
                }
            }
        }
    }

    private fun queryExistingPurchases() {
        if (!billingClient.isReady) return
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()
        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                var anyPending = false
                purchases.forEach { purchase ->
                    if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                        anyPending = true
                    } else if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        handlePurchase(purchase)
                    }
                }
                _uiState.update { it.copy(hasPendingPayment = anyPending) }
            }
        }
    }

    private fun queryActiveSubscriptions() {
        if (!billingClient.isReady) return
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val activeSub = purchases.firstOrNull { purchase ->
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED &&
                        purchase.products.any { VipSubscriptionCatalog.findByProductId(it) != null }
                }
                if (activeSub != null) {
                    val productId = activeSub.products.firstOrNull { VipSubscriptionCatalog.findByProductId(it) != null }
                    val plan = VipSubscriptionCatalog.findByProductId(productId)
                    if (plan != null) {
                        handleSubscriptionPurchase(activeSub, plan)
                    }
                } else {
                    _uiState.update { it.copy(activeSubscriptionProductId = null) }
                    scope.launch(Dispatchers.IO) {
                        onVerifiedSubscription(null, null, false)
                    }
                }
            }
        }
    }

    /**
     * Launches the official Google Play Billing payment sheet (Card, Mobil Ödeme / Carrier Billing, Play Balance).
     * Returns true if the Google Play payment flow was successfully launched.
     */
    fun launchPurchaseFlow(activity: Activity, pack: StoreOfferPack): Boolean {
        val productId = pack.googlePlayProductId ?: return false

        if (!billingClient.isReady) {
            startConnection()
            onToastMessage("Google Play bağlantısı kuruluyor, lütfen tekrar dene.", true)
            return false
        }

        val details = productDetailsMap[productId]
        if (details == null) {
            // Re-query catalog in case Play Console SKUs were just activated
            queryProductDetails()
            onToastMessage(
                "Bu ürün ($productId) henüz Google Play Console'da aktif değil. Play Store'dan kontrol edebilirsin.",
                true
            )
            return false
        }

        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .build()

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams))
            .build()

        _uiState.update { it.copy(isProcessingPurchase = true) }
        val result = billingClient.launchBillingFlow(activity, flowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _uiState.update { it.copy(isProcessingPurchase = false) }
            onToastMessage("Google Play ödeme penceresi açılamadı (Kod: ${result.responseCode}).", true)
            return false
        }
        return true
    }

    /**
     * Launches the official Google Play Subscription billing sheet (Card, Mobil Ödeme, Play Balance)
     * with the required subscription offerToken.
     */
    fun launchSubscriptionFlow(activity: Activity, plan: VipSubscriptionPlan): Boolean {
        val productId = plan.googlePlayProductId

        if (!billingClient.isReady) {
            startConnection()
            onToastMessage("Google Play bağlantısı kuruluyor, lütfen tekrar dene.", true)
            return false
        }

        val details = subscriptionDetailsMap[productId]
        if (details == null) {
            querySubscriptionDetails()
            onToastMessage(
                "Abonelik ($productId) henüz Google Play Console'da aktif değil. Play Store'dan kontrol edebilirsin.",
                true
            )
            return false
        }

        val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken
        if (offerToken.isNullOrBlank()) {
            onToastMessage("Google Play abonelik teklifi bulunamadı. Play Console temel planını kontrol et.", true)
            return false
        }

        val productParams = BillingFlowParams.ProductDetailsParams.newBuilder()
            .setProductDetails(details)
            .setOfferToken(offerToken)
            .build()

        val flowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(listOf(productParams))
            .build()

        _uiState.update { it.copy(isProcessingPurchase = true) }
        val result = billingClient.launchBillingFlow(activity, flowParams)
        if (result.responseCode != BillingClient.BillingResponseCode.OK) {
            _uiState.update { it.copy(isProcessingPurchase = false) }
            onToastMessage("Google Play abonelik ekranı açılamadı (Kod: ${result.responseCode}).", true)
            return false
        }
        return true
    }

    private fun handlePurchase(purchase: Purchase) {
        val matchedSubProductId = purchase.products.firstOrNull { VipSubscriptionCatalog.findByProductId(it) != null }
        val matchedSubPlan = VipSubscriptionCatalog.findByProductId(matchedSubProductId)
        if (matchedSubPlan != null) {
            handleSubscriptionPurchase(purchase, matchedSubPlan)
            return
        }

        when (purchase.purchaseState) {
            Purchase.PurchaseState.PENDING -> {
                _uiState.update {
                    it.copy(
                        hasPendingPayment = true,
                        lastMessage = "Ödeme onayı bekleniyor (Mobil Ödeme / Play Store). Onaylandığında bakiyen otomatik yüklenecek."
                    )
                }
                onToastMessage("Ödeme onayı bekleniyor — onaylandığında bakiyen otomatik eklenecek.", false)
            }
            Purchase.PurchaseState.PURCHASED -> {
                val token = purchase.purchaseToken
                if (token.isBlank() || !consumingTokens.add(token)) return

                val productId = purchase.products.firstOrNull()
                val pack = productId?.let { StoreOfferCatalog.findByProductId(it) }
                if (pack == null) {
                    consumingTokens.remove(token)
                    return
                }

                // Consume first so consumable packs can be bought again, and grant entitlement upon verified consumption
                val consumeParams = ConsumeParams.newBuilder()
                    .setPurchaseToken(token)
                    .build()

                billingClient.consumeAsync(consumeParams) { consumeResult, _ ->
                    if (consumeResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        scope.launch(Dispatchers.IO) {
                            val credited = onVerifiedPurchase(token, pack, purchase.quantity)
                            consumingTokens.remove(token)
                            _uiState.update { it.copy(hasPendingPayment = false) }
                            if (credited) {
                                val qty = purchase.quantity.coerceIn(1, 20)
                                val parts = buildList {
                                    if (pack.coinAmount > 0) add("+${pack.coinAmount * qty} Coin")
                                    if (pack.crystalAmount > 0) add("+${pack.crystalAmount * qty} Özel Kristal")
                                }.joinToString(" & ")
                                onToastMessage("Google Play Ödeme Onaylandı! $parts hesabına yüklendi.", false)
                            }
                        }
                    } else {
                        consumingTokens.remove(token)
                        onToastMessage("Satın alma teslimatı doğrulanamadı, lütfen 'Yenile' butonuna dokun.", true)
                    }
                }
            }
            else -> Unit
        }
    }

    private fun handleSubscriptionPurchase(purchase: Purchase, plan: VipSubscriptionPlan) {
        when (purchase.purchaseState) {
            Purchase.PurchaseState.PENDING -> {
                _uiState.update {
                    it.copy(
                        hasPendingPayment = true,
                        lastMessage = "VIP Abonelik onayı bekleniyor. Onaylandığında VIP Pass otomatik aktif olacak."
                    )
                }
                onToastMessage("VIP Abonelik onayı bekleniyor — onaylandığında otomatik aktif olacak.", false)
            }
            Purchase.PurchaseState.PURCHASED -> {
                val token = purchase.purchaseToken
                if (token.isBlank()) return

                if (purchase.isAcknowledged) {
                    _uiState.update {
                        it.copy(
                            activeSubscriptionProductId = plan.googlePlayProductId,
                            hasPendingPayment = false
                        )
                    }
                    scope.launch(Dispatchers.IO) {
                        val grantedWelcome = onVerifiedSubscription(plan, token, purchase.isAutoRenewing)
                        if (grantedWelcome) {
                            onToastMessage(
                                "${plan.title} Aktif! Hoş geldin hediyesi: +${plan.welcomeCoins} Sarı Coin & +${plan.welcomeCrystals} Kırmızı Kristal!",
                                false
                            )
                        }
                    }
                    return
                }

                if (!acknowledgingTokens.add(token)) return
                val ackParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(token)
                    .build()

                billingClient.acknowledgePurchase(ackParams) { ackResult ->
                    acknowledgingTokens.remove(token)
                    if (ackResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        _uiState.update {
                            it.copy(
                                activeSubscriptionProductId = plan.googlePlayProductId,
                                hasPendingPayment = false
                            )
                        }
                        scope.launch(Dispatchers.IO) {
                            val grantedWelcome = onVerifiedSubscription(plan, token, purchase.isAutoRenewing)
                            if (grantedWelcome) {
                                onToastMessage(
                                    "${plan.title} Aktif! Hoş geldin hediyesi: +${plan.welcomeCoins} Sarı Coin & +${plan.welcomeCrystals} Kırmızı Kristal!",
                                    false
                                )
                            } else {
                                onToastMessage("${plan.title} aboneliğin başarıyla doğrulandı!", false)
                            }
                        }
                    } else {
                        onToastMessage("Abonelik doğrulanamadı, lütfen 'Yenile' butonuna dokun.", true)
                    }
                }
            }
            else -> Unit
        }
    }

    fun endConnection() {
        try {
            if (billingClient.isReady) {
                billingClient.endConnection()
            }
        } catch (_: Throwable) {
        }
    }

    companion object {
        /**
         * Opens this app's Google Play Store page directly (falls back to HTTPS Play Store in browser).
         */
        fun openPlayStoreListing(context: Context) {
            val pkg = context.packageName
            val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")).apply {
                setPackage("com.android.vending")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(marketIntent)
            } catch (_: ActivityNotFoundException) {
                val webIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$pkg")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                runCatching { context.startActivity(webIntent) }
            }
        }

        /**
         * Opens Google Play Store's official Subscription Management page for transparent renewal/cancellation.
         */
        fun openPlaySubscriptionsManagement(context: Context, subscriptionSku: String? = null) {
            val pkg = context.packageName
            val uri = if (!subscriptionSku.isNullOrBlank()) {
                Uri.parse("https://play.google.com/store/account/subscriptions?sku=$subscriptionSku&package=$pkg")
            } else {
                Uri.parse("https://play.google.com/store/account/subscriptions?package=$pkg")
            }
            val playIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage("com.android.vending")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(playIntent)
            } catch (_: ActivityNotFoundException) {
                val webIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                runCatching { context.startActivity(webIntent) }
            }
        }
    }
}
