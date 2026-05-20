package com.fxalways.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.fxalways.designsystem.theme.FxTheme

@Composable
actual fun PriceOcrScannerAction(
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
    modifier: Modifier,
    onPriceDetected: (amount: String, currencyCode: String?) -> Unit,
) {
    Box(
        modifier
            .fillMaxWidth()
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2)
            .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
            .alpha(0.56f)
            .padding(vertical = 13.dp, horizontal = 14.dp)
            .testTag("price_scanner_scan_button"),
        contentAlignment = Alignment.Center,
    ) {
        Text("$scanLabel · $unavailableLabel", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.textDim)
    }
}
