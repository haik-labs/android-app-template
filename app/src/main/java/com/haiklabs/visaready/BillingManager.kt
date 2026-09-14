package com.haiklabs.visaready

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams

class BillingManager(context: Context, private val onPurchased: () -> Unit, private val onMessage: (String) -> Unit) {
    companion object { const val PRODUCT_ID = "visa_readiness_report" }
    private var details: ProductDetails? = null
    private val client = BillingClient.newBuilder(context)
        .setListener { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) purchases.orEmpty().forEach { purchase ->
                if (purchase.products.contains(PRODUCT_ID) && purchase.purchaseState == com.android.billingclient.api.Purchase.PurchaseState.PURCHASED) {
                    if (!purchase.isAcknowledged) client.acknowledgePurchase(AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()) {}
                    onPurchased()
                }
            }
        }
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()).build()

    fun connect() = client.startConnection(object : BillingClientStateListener {
        override fun onBillingServiceDisconnected() = Unit
        override fun onBillingSetupFinished(result: BillingResult) {
            if (result.responseCode != BillingClient.BillingResponseCode.OK) return
            val product = QueryProductDetailsParams.Product.newBuilder().setProductId(PRODUCT_ID).setProductType(BillingClient.ProductType.INAPP).build()
            client.queryProductDetailsAsync(QueryProductDetailsParams.newBuilder().setProductList(listOf(product)).build()) { _, products -> details = products.firstOrNull() }
            client.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build()) { _, purchases -> if (purchases.any { it.products.contains(PRODUCT_ID) }) onPurchased() }
        }
    })

    fun launch(activity: Activity) {
        val product = details ?: return onMessage("Checkout is not available yet. Install the Play Store build and configure product ‘$PRODUCT_ID’.")
        val params = BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(product).build()
        client.launchBillingFlow(activity, BillingFlowParams.newBuilder().setProductDetailsParamsList(listOf(params)).build())
    }
    fun close() = client.endConnection()
}
