package com.fxalways.app.screens.dashboard

import com.fxalways.app.screens.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.BentoTile
import com.fxalways.designsystem.components.FlagDot
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.components.GridBg
import com.fxalways.designsystem.components.Pill
import com.fxalways.designsystem.components.PillVariant
import com.fxalways.designsystem.components.SparkLine
import com.fxalways.designsystem.components.formatChange
import com.fxalways.designsystem.components.formatRate
import com.fxalways.designsystem.theme.FxTheme

@Composable
internal fun CryptoMetricTile(
    label: String,
    value: String,
    sub: String?,
    modifier: Modifier = Modifier,
) {
    BentoTile(
        modifier = modifier.heightIn(min = 98.dp),
        padding = 14.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                label.uppercase(),
                style = FxTheme.typography.eyebrow,
                color = FxTheme.colors.textFaint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    value,
                    style = FxTheme.typography.numberBody,
                    color = FxTheme.colors.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    sub.orEmpty(),
                    style = FxTheme.typography.captionMono,
                    color = FxTheme.colors.textDim,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp,
                )
            }
        }
    }
}

@Composable
internal fun CryptoAssetRow(rate: FxRate, baseCurrency: String, onClick: () -> Unit) {
    val inversePrice = if (rate.rate > 0.0) 1.0 / rate.rate else 0.0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_crypto_${rate.code}")
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlagDot(rate.glyph, rate.kind, 34.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(rate.code, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                Text(rate.name, style = FxTheme.typography.caption, color = FxTheme.colors.textFaint, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text("$baseCurrency ${formatMoneyValue(inversePrice)} · ${ui("per coin")}", style = FxTheme.typography.captionMono, color = FxTheme.colors.textDim)
        }
        SparkLine(rate.sparkline, Modifier.size(64.dp, 26.dp), color = if (rate.change24h >= 0) FxTheme.colors.up else FxTheme.colors.down, showLastDot = true)
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.widthIn(min = 64.dp)) {
            Text(formatCryptoAmount(rate.rate), style = FxTheme.typography.numberBody, color = FxTheme.colors.text)
            Spacer(Modifier.height(2.dp))
            Text(formatChange(rate.change24h), style = FxTheme.typography.captionMono, color = if (rate.change24h >= 0) FxTheme.colors.up else FxTheme.colors.down)
        }
    }
}

@Composable
internal fun HeroRateCard(rate: FxRate, baseCurrency: String) {
    BentoCard(Modifier.fillMaxWidth().height(168.dp), padding = 14.dp) {
        GridBg(Modifier.matchParentSize().alpha(0.22f))
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FlagDot(rate.glyph, rate.kind, size = 28.dp)
                    Text("$baseCurrency → ${rate.code}", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                }
                Pill(ui("pinned"), variant = PillVariant.Ghost)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Text(formatRate(rate.rate), style = FxTheme.typography.numberXL.copy(fontSize = 44.sp, lineHeight = 44.sp), color = FxTheme.colors.text)
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(formatChange(rate.change24h), style = FxTheme.typography.numberBody, color = if (rate.change24h >= 0) FxTheme.colors.up else FxTheme.colors.down)
                    com.fxalways.designsystem.components.Eyebrow(ui("VS YESTERDAY"))
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    com.fxalways.designsystem.components.Eyebrow(ui("30D RANGE"))
                    Text(sparklineRangeLabel(rate), style = FxTheme.typography.captionMono, color = FxTheme.colors.textDim)
                }
                SparkLine(rate.sparkline, Modifier.size(108.dp, 38.dp), color = FxTheme.colors.accent, showLastDot = true)
            }
        }
    }
}
