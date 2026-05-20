package com.fxalways.app.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PriceOcrScannerAction(
    scanLabel: String,
    readingLabel: String,
    detectedLabel: String,
    unavailableLabel: String,
    liveTitleLabel: String,
    liveHintLabel: String,
    useDetectedLabel: String,
    closeLabel: String,
    currentCurrencyLabel: String,
    switchingCurrencyLabel: String,
    targetCurrency: String,
    modifier: Modifier = Modifier,
    onPriceDetected: (amount: String, currencyCode: String?) -> Unit,
)
