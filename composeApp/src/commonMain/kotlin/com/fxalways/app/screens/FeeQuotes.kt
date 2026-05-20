package com.fxalways.app.screens

import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.components.formatRate

internal data class CustomFeeInput(
    val fixedFee: Double,
    val feePercent: Double,
    val markupPercent: Double,
)

internal data class EstimatedFeeQuote(
    val providerId: String,
    val provider: String,
    val badge: String?,
    val amount: String,
    val fee: String,
    val markup: String,
    val loss: String,
    val lossPercent: String,
    val effectiveRate: String,
    val deliverySpeed: String,
    val paymentMethod: String,
    val riskLabel: String,
    val bestFor: String,
    val lossTargetValue: Double,
    val lossPercentValue: Double,
    val isHighFee: Boolean = false,
)

private data class FeeProviderTemplate(
    val providerId: String,
    val provider: String,
    val badge: String? = null,
    val fixedFee: Double = 0.0,
    val feePercent: Double = 0.0,
    val markupPercent: Double = 0.0,
    val deliverySpeed: String = "Instant",
    val paymentMethod: String = "Debit/bank",
    val riskLabel: String = "Low",
    val bestFor: String = "Low-cost transfer",
)

internal fun estimatedFeeQuotes(
    sourceRate: FxRate,
    targetRate: FxRate,
    amount: Double,
    customFee: CustomFeeInput,
    selectedProviderCodes: List<String> = emptyList(),
): List<EstimatedFeeQuote> {
    val safeAmount = amount.coerceAtLeast(0.0)
    val templates = listOf(
        FeeProviderTemplate("mid_market", "Mid-market", "best", deliverySpeed = "Instant", paymentMethod = "Cash desk", riskLabel = "Low", bestFor = "Transparent baseline"),
        FeeProviderTemplate("wise", "Wise", fixedFee = 0.35, feePercent = 0.45, deliverySpeed = "Same day", paymentMethod = "Debit/bank", riskLabel = "Low", bestFor = "Low-cost transfer"),
        FeeProviderTemplate("revolut", "Revolut", feePercent = 0.80, markupPercent = 0.15, deliverySpeed = "Minutes", paymentMethod = "Card balance", riskLabel = "Low", bestFor = "Travel card spend"),
        FeeProviderTemplate("moneygram", "MoneyGram", fixedFee = 1.50, feePercent = 0.70, markupPercent = 0.60, deliverySpeed = "Minutes", paymentMethod = "Cash pickup", riskLabel = "Medium", bestFor = "Cash pickup"),
        FeeProviderTemplate("western_union", "Western Union", "high fee", fixedFee = 2.50, feePercent = 0.95, markupPercent = 1.20, deliverySpeed = "Same day", paymentMethod = "Cash pickup", riskLabel = "Medium", bestFor = "Broad cash network"),
        FeeProviderTemplate("remitly", "Remitly", fixedFee = 1.99, feePercent = 0.60, markupPercent = 0.95, deliverySpeed = "Same day", paymentMethod = "Bank or wallet", riskLabel = "Medium", bestFor = "Family remittance"),
        FeeProviderTemplate("paypal_xoom", "PayPal / Xoom", fixedFee = 2.99, feePercent = 0.80, markupPercent = 1.70, deliverySpeed = "Minutes", paymentMethod = "Wallet/bank", riskLabel = "Medium", bestFor = "PayPal identity"),
        FeeProviderTemplate("remessa_online", "Remessa Online", fixedFee = 0.90, feePercent = 0.55, markupPercent = 0.35, deliverySpeed = "Same day", paymentMethod = "Bank account", riskLabel = "Low", bestFor = "Brazil transfers"),
        FeeProviderTemplate("global66", "Global66", fixedFee = 0.75, feePercent = 0.65, markupPercent = 0.55, deliverySpeed = "Same day", paymentMethod = "Wallet/bank", riskLabel = "Low", bestFor = "LatAm account route"),
        FeeProviderTemplate("dolarapp", "DolarApp", fixedFee = 3.0, feePercent = 0.20, markupPercent = 0.30, deliverySpeed = "1-2 days", paymentMethod = "Digital dollar", riskLabel = "Medium", bestFor = "Digital dollar"),
        FeeProviderTemplate("airtm", "Airtm", fixedFee = 1.0, feePercent = 1.10, markupPercent = 1.80, deliverySpeed = "Same day", paymentMethod = "Digital wallet", riskLabel = "High", bestFor = "Emerging markets"),
        FeeProviderTemplate("card_payment", "Card payment", feePercent = 0.30, markupPercent = 2.70, deliverySpeed = "Instant", paymentMethod = "Card terminal", riskLabel = "Medium", bestFor = "Emergency card payment"),
        FeeProviderTemplate("atm_cash", "ATM cash", fixedFee = 4.0, feePercent = 1.0, markupPercent = 3.00, deliverySpeed = "Instant", paymentMethod = "Cash withdrawal", riskLabel = "High", bestFor = "Cash access"),
        FeeProviderTemplate("bank_transfer", "Bank transfer", "high fee", fixedFee = 5.0, feePercent = 0.80, markupPercent = 3.20, deliverySpeed = "1-2 days", paymentMethod = "Bank account", riskLabel = "Medium", bestFor = "Bank fallback"),
        FeeProviderTemplate("airport_exchange", "Airport exchange", "avoid", markupPercent = 8.50, deliverySpeed = "Instant", paymentMethod = "Airport cash", riskLabel = "Very high", bestFor = "Last resort"),
        FeeProviderTemplate("custom", "Custom", fixedFee = customFee.fixedFee, feePercent = customFee.feePercent, markupPercent = customFee.markupPercent, deliverySpeed = "Same day", paymentMethod = "Custom provider", riskLabel = "Medium", bestFor = "Custom provider"),
    )
    val selectedCodes = normalizeProviderPreferenceCodes(selectedProviderCodes, sourceRate.code, targetRate.code).toSet()
    val visibleTemplates = templates.filter { template ->
        template.providerId in setOf("mid_market", "custom") || template.providerId in selectedCodes
    }
    val midMarketTarget = convertedAmount(safeAmount, sourceRate, targetRate)
    val rawRate = if (sourceRate.rate == 0.0) 0.0 else targetRate.rate / sourceRate.rate

    return visibleTemplates.map { template ->
        val variableFee = safeAmount * template.feePercent.coerceAtLeast(0.0) / 100.0
        val fixedFee = template.fixedFee.coerceAtLeast(0.0)
        val sourceFee = (fixedFee + variableFee).coerceAtMost(safeAmount)
        val netSource = (safeAmount - sourceFee).coerceAtLeast(0.0)
        val markupMultiplier = (1.0 - template.markupPercent.coerceIn(0.0, 99.0) / 100.0)
        val receivedTarget = netSource * rawRate * markupMultiplier
        val lossTarget = (midMarketTarget - receivedTarget).coerceAtLeast(0.0)
        val lossPct = if (midMarketTarget > 0.0) lossTarget / midMarketTarget * 100.0 else 0.0
        val effectiveRate = if (safeAmount > 0.0) receivedTarget / safeAmount else 0.0
        val highFee = lossPct >= 3.0 || template.badge in setOf("high fee", "avoid")
        EstimatedFeeQuote(
            providerId = template.providerId,
            provider = template.provider,
            badge = template.badge ?: when {
                lossPct == 0.0 -> "best"
                lossPct >= 6.0 -> "avoid"
                lossPct >= 3.0 -> "high fee"
                else -> null
            },
            amount = formatConvertedAmount(targetRate, receivedTarget),
            fee = "${sourceRate.code} ${formatMoneyValue(sourceFee)}",
            markup = "${formatRate(template.markupPercent)}%",
            loss = "${targetRate.code} ${formatMoneyValue(lossTarget)}",
            lossPercent = "${formatRate(lossPct)}%",
            effectiveRate = "${formatRate(effectiveRate)} ${targetRate.code}",
            deliverySpeed = template.deliverySpeed,
            paymentMethod = template.paymentMethod,
            riskLabel = template.riskLabel,
            bestFor = template.bestFor,
            lossTargetValue = lossTarget,
            lossPercentValue = lossPct,
            isHighFee = highFee,
        )
    }.sortedWith(compareBy<EstimatedFeeQuote> { it.lossTargetValue }.thenBy { it.provider != "Custom" })
}
