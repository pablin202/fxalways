package com.fxalways.app.screens.converter

import com.fxalways.app.screens.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fxalways.app.AppSettingsPrefs
import com.fxalways.app.data.mock.ConverterRates
import com.fxalways.app.data.AlertsState
import com.fxalways.app.data.AlertDirection
import com.fxalways.app.data.AlertKind
import com.fxalways.app.data.ExchangeApi
import com.fxalways.app.data.LiveRatesState
import com.fxalways.app.domain.ProviderQuoteDto
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.app.subscription.featureAccess
import com.fxalways.app.screens.alerts.findMatchingAlert
import com.fxalways.app.screens.providers.FreeFeeProviderIds
import com.fxalways.app.screens.providers.FreeQuoteProviderLimit
import com.fxalways.app.screens.providers.normalizeProviderPreferenceCodes
import com.fxalways.app.screens.providers.quoteCapableProviderCodes
import com.fxalways.app.screens.detail.LoadingSkeletonCard
import com.fxalways.app.screens.detail.RateTrustCard
import com.fxalways.app.screens.detail.RateTrustDetailsCard
import com.fxalways.app.screens.detail.compactRuntimeLabel
import com.fxalways.app.screens.detail.isInitialRateLoading
import com.fxalways.app.screens.shared.ProUpsellCard
import com.fxalways.designsystem.components.Eyebrow
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.components.LiveDot
import com.fxalways.designsystem.components.ScreenHeader
import com.fxalways.designsystem.components.SectionLabel
import com.fxalways.designsystem.components.formatRate
import com.fxalways.designsystem.theme.FxTheme
import com.fxalways.observability.Observability

@Composable
fun ConverterScreen(
    liveState: LiveRatesState,
    alertsState: AlertsState = AlertsState(),
    subscriptionState: SubscriptionState,
    selectedCurrencyCodes: List<String> = emptyList(),
    selectedProviderCodes: List<String> = emptyList(),
    onCurrencyCodesChange: (List<String>) -> Unit = {},
    onOpenPaywall: () -> Unit,
    onOpenPaywallSource: (String) -> Unit = { onOpenPaywall() },
    onCreateTransferAlert: (FxRate, FxRate, Double) -> Unit = { _, _, _ -> },
    onOpenProviderUrl: (String) -> Unit = {},
    enableLiveProviderQuotes: Boolean = false,
) {
    val access = subscriptionState.featureAccess()
    val focusManager = LocalFocusManager.current
    var showCurrencyPicker by remember { mutableStateOf(false) }
    val availableRates = remember(liveState.baseCurrency, liveState.favorites, liveState.compare, liveState.converter, liveState.allFiat, liveState.crypto, subscriptionState.isPremium) {
        liveState.converterAvailableRates(subscriptionState.isPremium)
    }
    val targetCodes = remember(liveState.baseCurrency, selectedCurrencyCodes, availableRates, access.converterCurrencyLimit) {
        converterTargetCodes(
            selectedCurrencyCodes = selectedCurrencyCodes,
            availableRates = availableRates,
            baseCurrency = liveState.baseCurrency,
            limit = access.converterCurrencyLimit,
        )
    }
    val rates = remember(liveState.baseCurrency, liveState.converter, availableRates, targetCodes) {
        val byCode = (availableRates + liveState.converter.ifEmpty { ConverterRates }).distinctBy { it.code }.associateBy { it.code }
        (listOfNotNull(byCode[liveState.baseCurrency]) + targetCodes.mapNotNull { byCode[it] })
            .distinctBy { it.code }
    }
    val initialTarget = remember(liveState.baseCurrency, rates) {
        rates.firstOrNull { it.code != liveState.baseCurrency }?.code ?: liveState.baseCurrency
    }
    var sourceCode by remember(liveState.baseCurrency) { mutableStateOf(liveState.baseCurrency) }
    var targetCode by remember(liveState.baseCurrency, initialTarget) { mutableStateOf(initialTarget) }
    var amountText by remember { mutableStateOf(sanitizeAmountInput(AppSettingsPrefs.converterAmountText())) }
    var amountFocused by remember { mutableStateOf(false) }
    var customFixedFeeText by remember { mutableStateOf("0") }
    var customFeePercentText by remember { mutableStateOf("1.00") }
    var customMarkupPercentText by remember { mutableStateOf("2.50") }
    var remittanceCadence by remember { mutableStateOf("Monthly") }
    var recipientProfile by remember { mutableStateOf("Family support") }
    var transferPurpose by remember { mutableStateOf("Family") }
    var transferDecisionHistory by remember { mutableStateOf(emptyList<TransferDecision>()) }
    var scannedPriceText by remember { mutableStateOf("25") }
    var priceScannerHistory by remember { mutableStateOf(emptyList<PriceScannerHistoryEntry>()) }
    var providerComparisonFocused by remember { mutableStateOf(false) }
    val sourceRate = rates.firstOrNull { it.code == sourceCode }
        ?: rates.firstOrNull { it.code == liveState.baseCurrency }
        ?: rates.first()
    val targetRate = rates.firstOrNull { it.code == targetCode && it.code != sourceRate.code }
        ?: rates.firstOrNull { it.code != sourceRate.code }
        ?: sourceRate
    var localMarketRateText by remember(sourceRate.code, targetRate.code) { mutableStateOf(formatRate(targetRate.rate * 1.08)) }
    val amountValue = parseAmountInput(amountText)
    val localMarketRate = parseAmountInput(localMarketRateText).takeIf { it > 0.0 } ?: targetRate.rate
    val customFee = CustomFeeInput(
        fixedFee = parseAmountInput(customFixedFeeText),
        feePercent = parseAmountInput(customFeePercentText),
        markupPercent = parseAmountInput(customMarkupPercentText),
    )
    val providerCodes = remember(selectedProviderCodes, sourceRate.code, targetRate.code) {
        normalizeProviderPreferenceCodes(selectedProviderCodes, sourceRate.code, targetRate.code)
    }
    var backendProviderQuotes by remember { mutableStateOf(emptyList<ProviderQuoteDto>()) }
    var providerQuotesLoading by remember { mutableStateOf(false) }
    var providerQuotesError by remember { mutableStateOf<String?>(null) }
    val providerQuoteApi = remember(enableLiveProviderQuotes) { if (enableLiveProviderQuotes) ExchangeApi() else null }
    LaunchedEffect(
        enableLiveProviderQuotes,
        sourceRate.code,
        targetRate.code,
        amountValue,
        providerCodes,
        subscriptionState.isPremium,
    ) {
        if (!enableLiveProviderQuotes || providerQuoteApi == null || amountValue <= 0.0 || sourceRate.code == targetRate.code) {
            backendProviderQuotes = emptyList()
            providerQuotesLoading = false
            providerQuotesError = null
            return@LaunchedEffect
        }
        providerQuotesLoading = true
        providerQuotesError = null
        runCatching {
            providerQuoteApi.providerQuotes(
                base = sourceRate.code,
                target = targetRate.code,
                amount = amountValue,
                providers = providerCodes.quoteCapableProviderCodes(),
                isPremium = subscriptionState.isPremium,
            )
        }.onSuccess { response ->
            backendProviderQuotes = response.quotes
            providerQuotesError = null
        }.onFailure { throwable ->
            backendProviderQuotes = emptyList()
            providerQuotesError = throwable.message
        }
        providerQuotesLoading = false
    }
    val allFeeQuotes = estimatedFeeQuotes(sourceRate, targetRate, amountValue, customFee, providerCodes)
        .withBackendProviderQuotes(backendProviderQuotes, targetRate)
    val feeQuotes = if (access.canUseFullFeeComparison) {
        allFeeQuotes.take(EstimatedFeeQuoteCount)
    } else {
        val freeProviderIds = FreeFeeProviderIds + providerCodes.quoteCapableProviderCodes().take(FreeQuoteProviderLimit)
        allFeeQuotes.filter { it.providerId in freeProviderIds }
    }
    val bestQuote = feeQuotes.minByOrNull { it.lossTargetValue }
    val bestRealWorldQuote = feeQuotes
        .filterNot { it.provider == "Mid-market" }
        .minByOrNull { it.lossTargetValue }
    val worstQuote = feeQuotes.maxByOrNull { it.lossTargetValue }
    val customQuote = feeQuotes.firstOrNull { it.provider == "Custom" }
    val potentialSavings = bestQuote?.let { best ->
        worstQuote?.let { worst -> (worst.lossTargetValue - best.lossTargetValue).coerceAtLeast(0.0) }
    } ?: 0.0
    val timingInsight = remember(sourceRate, targetRate) { smartTimingInsight(sourceRate, targetRate) }
    if (showCurrencyPicker) {
        CurrencyListPickerSheet(
            title = ui("Edit converter list"),
            lockedSubtitle = ui("Pro unlocks more converter currencies"),
            currencies = availableRates.filterNot { it.code == liveState.baseCurrency },
            selectedCodes = targetCodes,
            limit = access.converterCurrencyLimit,
            isPremium = subscriptionState.isPremium,
            onDismiss = { showCurrencyPicker = false },
            onOpenPaywall = {
                showCurrencyPicker = false
                onOpenPaywallSource("converter_currency_limit")
            },
            onApply = { codes ->
                showCurrencyPicker = false
                onCurrencyCodesChange(codes)
                if (targetCode !in codes && codes.isNotEmpty()) {
                    targetCode = codes.first()
                }
            },
        )
    }
    ScreenScaffold {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LiveDot()
            Eyebrow(ui("MID"), color = FxTheme.colors.accent)
            Text(
                compactRuntimeLabel(liveState.updatedLabel),
                style = FxTheme.typography.captionMono,
                color = FxTheme.colors.textFaint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        ScreenHeader(ui("Convert"), subtitle = ui("Multi-currency · live to 4 decimals"))
        RateTrustCard(
            liveState = liveState,
            modifier = Modifier.testTag("converter_rate_trust"),
        )
        RateTrustDetailsCard(
            liveState = liveState,
            modifier = Modifier.testTag("converter_trust_details"),
        )
        if (liveState.isInitialRateLoading()) {
            LoadingSkeletonCard(
                title = ui("Preparing converter rates"),
                rows = 4,
                modifier = Modifier.testTag("converter_loading_skeleton"),
            )
            LoadingSkeletonCard(
                title = ui("Preparing fee estimates"),
                rows = 5,
                modifier = Modifier.testTag("converter_fee_loading_skeleton"),
            )
        } else {
        ConverterAmountCard(
            sourceRate = sourceRate,
            targetRate = targetRate,
            amountText = amountText,
            amountValue = amountValue,
            amountFocused = amountFocused,
            onAmountChange = { raw ->
                amountText = sanitizeAmountInput(raw)
                AppSettingsPrefs.setConverterAmountText(amountText)
                trackFirstConversion(amountText, sourceRate.code, targetRate.code)
            },
            onAmountFocusChange = { amountFocused = it },
            onDone = { focusManager.clearFocus() },
        )
        ConverterRateListCard(
            rates = rates,
            sourceRate = sourceRate,
            targetRate = targetRate,
            amountValue = amountValue,
            onTargetSelected = { rate ->
                targetCode = rate.code
                Observability.event(
                    "converter_target_selected",
                    mapOf("source" to sourceRate.code, "target" to rate.code),
                )
                focusManager.clearFocus()
            },
        )
        ConverterActionsRow(
            onReverse = {
                val previousSource = sourceRate
                val previousTarget = targetRate
                sourceCode = previousTarget.code
                targetCode = previousSource.code
                amountText = formatInputAmount(convertedAmount(amountValue, previousSource, previousTarget))
                AppSettingsPrefs.setConverterAmountText(amountText)
                Observability.event(
                    "converter_reversed",
                    mapOf("source" to previousSource.code, "target" to previousTarget.code),
                )
                focusManager.clearFocus()
            },
            onEditList = { showCurrencyPicker = true },
        )
        ConversionDecisionCard(
            sourceRate = sourceRate,
            targetRate = targetRate,
            amountValue = amountValue,
            convertedAmount = convertedAmount(amountValue, sourceRate, targetRate),
            timingInsight = timingInsight,
            bestRoute = bestRealWorldQuote ?: bestQuote,
            isPremium = subscriptionState.isPremium,
            onCreateAlert = { onCreateTransferAlert(sourceRate, targetRate, transferAlertTarget(sourceRate, targetRate)) },
            onCompareProviders = {
                providerComparisonFocused = true
                Observability.event("provider_matrix_focused")
            },
            onOpenPaywall = { onOpenPaywallSource("provider_lock") },
        )
        SectionLabel("${ui("SMART TIMING")} · ${sourceRate.code} → ${targetRate.code}", right = if (subscriptionState.isPremium) ui("Pro") else ui("Preview"))
        SmartTimingCard(
            insight = timingInsight,
            isPremium = subscriptionState.isPremium,
            onOpenPaywall = { onOpenPaywallSource("timing_lock") },
        )
        SectionLabel("${ui("LOCAL RATE NOTEBOOK")} · ${sourceRate.code} → ${targetRate.code}")
        LocalRateNotebookCard(
            sourceRate = sourceRate,
            targetRate = targetRate,
            localMarketRateText = localMarketRateText,
            localMarketRate = localMarketRate,
            onLocalMarketRateChange = { localMarketRateText = sanitizeAmountInput(it) },
        )
        SectionLabel("${ui("PRICE SCANNER")} · ${targetRate.code} → ${sourceRate.code}", right = if (subscriptionState.isPremium) ui("OCR beta") else ui("Preview"))
        PriceScannerCard(
            sourceRate = sourceRate,
            targetRate = targetRate,
            scannedPriceText = scannedPriceText,
            localMarketRate = localMarketRate,
            isPremium = subscriptionState.isPremium,
            onScannedPriceChange = { scannedPriceText = sanitizeAmountInput(it) },
            onScannedPriceDetected = { amount, detectedCurrency ->
                scannedPriceText = sanitizeAmountInput(amount)
                val normalizedCurrency = detectedCurrency?.uppercase()
                val detectedTargetRate = rates.firstOrNull { it.code == normalizedCurrency && it.code != sourceRate.code } ?: targetRate
                if (normalizedCurrency != null && normalizedCurrency != targetRate.code && rates.any { it.code == normalizedCurrency && it.code != sourceRate.code }) {
                    targetCode = normalizedCurrency
                }
                Observability.event(
                    "price_scanner_result_used",
                    mapOf("source" to sourceRate.code, "target" to (normalizedCurrency ?: targetRate.code)),
                )
                priceScannerHistory = (listOf(
                    PriceScannerHistoryEntry(
                        amountText = sanitizeAmountInput(amount),
                        targetCode = detectedTargetRate.code,
                        sourceCode = sourceRate.code,
                        liveSourceCost = liveSourceCostFor(parseAmountInput(amount), detectedTargetRate),
                        hiddenCost = hiddenCostFor(parseAmountInput(amount), detectedTargetRate, localMarketRate),
                    ),
                ) + priceScannerHistory).take(4)
            },
            history = priceScannerHistory,
            onOpenPaywall = { onOpenPaywallSource("ocr_lock") },
        )
        SectionLabel("${ui("FEES")} · ${sourceRate.code} → ${targetRate.code}", right = if (access.canUseFullFeeComparison) ui("Estimated") else ui("Preview"))
        FeeRealityCheckCard(
            quote = bestRealWorldQuote ?: bestQuote,
        )
        SectionLabel("${ui("REMITTANCE PLAN")} · ${sourceRate.code} → ${targetRate.code}", right = if (subscriptionState.isPremium) ui("Pro") else ui("Preview"))
        RemittancePlannerCard(
            sourceRate = sourceRate,
            targetRate = targetRate,
            amountValue = amountValue,
            quote = bestRealWorldQuote ?: bestQuote,
            cadence = remittanceCadence,
            recipientProfile = recipientProfile,
            isPremium = subscriptionState.isPremium,
            onCadenceChange = { remittanceCadence = it },
            onRecipientProfileChange = { recipientProfile = it },
            onOpenPaywall = { onOpenPaywallSource("remittance_plan_lock") },
        )
        SectionLabel("${ui("TRANSFER INTENT")} · ${sourceRate.code} → ${targetRate.code}", right = if (subscriptionState.isPremium) ui("Pro") else ui("Preview"))
        TransferIntentCard(
            sourceRate = sourceRate,
            targetRate = targetRate,
            amountValue = amountValue,
            quote = bestRealWorldQuote ?: bestQuote,
            purpose = transferPurpose,
            history = transferDecisionHistory,
            matchingAlert = alertsState.alerts.findMatchingAlert(
                baseCurrency = sourceRate.code,
                quote = targetRate.code,
                target = transferAlertTarget(sourceRate, targetRate),
                direction = AlertDirection.Above,
                kind = AlertKind.Target,
            ),
            isPremium = subscriptionState.isPremium,
            onPurposeChange = { transferPurpose = it },
            onDecisionSaved = { decision ->
                transferDecisionHistory = (listOf(decision) + transferDecisionHistory).take(5)
            },
            onCreateAlert = { onCreateTransferAlert(sourceRate, targetRate, transferAlertTarget(sourceRate, targetRate)) },
            onOpenProviderUrl = onOpenProviderUrl,
            onOpenPaywall = { onOpenPaywallSource("transfer_intent_lock") },
        )
        SectionLabel(
            ui("PROVIDER MATRIX"),
            right = when {
                providerQuotesLoading -> ui("Refreshing")
                backendProviderQuotes.any { it.status == "live" } -> ui("Live")
                backendProviderQuotes.isNotEmpty() -> ui("Backend")
                access.canUseFullFeeComparison -> ui("Estimated")
                else -> ui("Preview")
            },
        )
        if (providerComparisonFocused) {
            Text(
                "${ui("PROVIDER MATRIX")} · ${ui("Ready")}",
                style = FxTheme.typography.captionMono,
                color = FxTheme.colors.accent,
                modifier = Modifier.testTag("provider_matrix_focus_feedback"),
            )
        }
        ProviderRecommendationCard(
            quote = bestRealWorldQuote ?: bestQuote,
            potentialSavings = potentialSavings,
            isPremium = subscriptionState.isPremium,
            isLoading = providerQuotesLoading,
            modifier = Modifier.testTag("converter_provider_recommendation"),
            onOpenPaywall = { onOpenPaywallSource("provider_lock") },
        )
        ProviderMatrixCard(
            base = sourceRate.code,
            target = targetRate.code,
            amountValue = amountValue,
            quotes = feeQuotes.filterNot { it.provider == "Mid-market" },
            isLoading = providerQuotesLoading,
            errorMessage = providerQuotesError,
            isPremium = subscriptionState.isPremium,
            onOpenPaywall = { onOpenPaywallSource("provider_lock") },
        )
        ProviderSummaryCard(
            targetRate = targetRate,
            bestQuote = bestQuote,
            customQuote = customQuote,
            midMarketValue = convertedAmount(amountValue, sourceRate, targetRate),
            potentialSavings = potentialSavings,
        )
        ProviderComparisonHistoryCard(
            sourceRate = sourceRate,
            targetRate = targetRate,
            amountValue = amountValue,
            customFee = customFee,
            selectedProviderCodes = providerCodes,
            isPremium = subscriptionState.isPremium,
            onOpenPaywall = { onOpenPaywallSource("provider_history_lock") },
        )
        CustomCostCard(
            sourceCode = sourceRate.code,
            fixedFeeText = customFixedFeeText,
            feePercentText = customFeePercentText,
            markupPercentText = customMarkupPercentText,
            onFixedFeeChange = { customFixedFeeText = sanitizeAmountInput(it) },
            onFeePercentChange = { customFeePercentText = sanitizeAmountInput(it) },
            onMarkupPercentChange = { customMarkupPercentText = sanitizeAmountInput(it) },
        )
        FeeQuotesListCard(feeQuotes)
        if (!access.canUseFullFeeComparison) {
            ProUpsellCard(
                title = ui("See the real transfer cost"),
                subtitle = ui("Pro unlocks the complete provider list; estimates update with your amount."),
                modifier = Modifier.testTag("converter_fee_upsell"),
                onClick = { onOpenPaywallSource("provider_lock") },
            )
        }
        }
    }
}

private const val EstimatedFeeQuoteCount = 8

/** Emits `first_conversion` exactly once per install: the first time the user types a non-zero amount. */
internal fun trackFirstConversion(amountText: String, base: String, target: String) {
    if (AppSettingsPrefs.firstConversionTracked()) return
    val amount = amountText.replace(",", ".").toDoubleOrNull() ?: return
    if (amount <= 0.0) return
    AppSettingsPrefs.setFirstConversionTracked()
    Observability.event("first_conversion", mapOf("base" to base, "target" to target, "amount_bucket" to amountBucket(amount)))
}

/** Coarse amount bucket so the funnel can segment small vs. remittance-sized conversions without logging exact amounts. */
internal fun amountBucket(amount: Double): String = when {
    amount < 100 -> "lt_100"
    amount < 1_000 -> "100_1k"
    amount < 10_000 -> "1k_10k"
    else -> "gte_10k"
}
