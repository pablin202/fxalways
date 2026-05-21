package com.fxalways.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fxalways.app.data.LiveRatesState
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.CurrencyRow
import com.fxalways.designsystem.components.Eyebrow
import com.fxalways.designsystem.components.LiveDot
import com.fxalways.designsystem.components.ScreenHeader
import com.fxalways.designsystem.components.SectionLabel
import com.fxalways.designsystem.components.formatRate
import com.fxalways.designsystem.theme.FxTheme

@Composable
fun OfflineScreen(
    liveState: LiveRatesState = LiveRatesState(),
    onRefresh: () -> Unit = {},
) {
    val primaryRate = liveState.favorites.firstOrNull()
        ?: liveState.converter.firstOrNull { it.code != liveState.baseCurrency }
        ?: liveState.compare.firstOrNull()
    ScreenScaffold {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LiveDot(color = FxTheme.colors.down)
            Eyebrow(ui("OFFLINE"), color = FxTheme.colors.down)
            Text(localizedRuntimeLabel(liveState.updatedLabel), style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
        }
        ScreenHeader(
            ui("No connection"),
            subtitle = if (liveState.isOfflineCache) {
                ui("Showing rates from your last sync")
            } else {
                ui("Connect once to save rates for offline use")
            },
        )
        if (primaryRate != null) {
            BentoCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Eyebrow("${ui("LAST KNOWN")} · ${liveState.baseCurrency} → ${primaryRate.code}", color = FxTheme.colors.down)
                    Text(formatRate(primaryRate.rate), style = FxTheme.typography.numberXL, color = FxTheme.colors.textDim)
                    Text(localizedRuntimeLabel(liveState.updatedLabel), style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
                }
            }
        } else {
            BentoCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Eyebrow(ui("LAST KNOWN"), color = FxTheme.colors.down)
                    Text(ui("No saved rates yet"), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.textDim)
                }
            }
        }
        PrimaryButton("↻  ${ui("Retry connection")}", onClick = onRefresh)
        SectionLabel(ui("CACHED FAVORITES"))
        BentoCard(padding = 0.dp) {
            Column {
                liveState.favorites.take(4).forEach { CurrencyRow(localizedRate(it), dense = true, enabled = false) }
            }
        }
        Text(
            "╌╌╌  ${ui("saved locally")}  ╌╌╌",
            style = FxTheme.typography.captionMono,
            color = FxTheme.colors.textGhost,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}
