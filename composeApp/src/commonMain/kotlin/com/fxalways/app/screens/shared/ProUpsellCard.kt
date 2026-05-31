package com.fxalways.app.screens.shared

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.CurrencyKind
import com.fxalways.designsystem.components.Eyebrow
import com.fxalways.designsystem.components.FlagDot
import com.fxalways.designsystem.theme.FxTheme

@Composable
internal fun ProUpsellCard(title: String, subtitle: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    BentoCard(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, FxTheme.colors.crypto.copy(alpha = 0.42f), FxTheme.shapes.card)
            .clickable(onClick = onClick),
        padding = 12.dp,
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            FlagDot("∞", CurrencyKind.Crypto, 34.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Eyebrow("FX/ PRO", color = FxTheme.colors.crypto)
                Text(title, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                Text(subtitle, style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
            }
            Box(
                modifier = Modifier
                    .clip(FxTheme.shapes.pill)
                    .border(1.dp, FxTheme.colors.crypto.copy(alpha = 0.48f), FxTheme.shapes.pill)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text("PRO →", style = FxTheme.typography.captionMono, color = FxTheme.colors.crypto)
            }
        }
    }
}
