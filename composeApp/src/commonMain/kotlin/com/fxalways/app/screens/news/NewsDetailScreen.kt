package com.fxalways.app.screens.news

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.fxalways.app.data.mock.NewsStory
import com.fxalways.app.screens.BackNavButton
import com.fxalways.app.screens.GhostButton
import com.fxalways.app.screens.ScreenScaffold
import com.fxalways.app.screens.ui
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.Eyebrow
import com.fxalways.designsystem.components.KeyValueRow
import com.fxalways.designsystem.components.Pill
import com.fxalways.designsystem.components.PillVariant
import com.fxalways.designsystem.components.ScreenHeader
import com.fxalways.designsystem.components.SectionLabel
import com.fxalways.designsystem.components.formatChange
import com.fxalways.designsystem.theme.FxTheme

@Composable
fun NewsDetailScreen(
    story: NewsStory?,
    onBack: () -> Unit = {},
    onOpenUrl: (String) -> Unit = {},
) {
    val selected = story ?: NewsStory(
        tag = "FX",
        impact = "MED",
        age = "Now",
        title = ui("Market update"),
        summary = ui("Latest currency market context."),
        moves = emptyList(),
        source = "FX Always",
        sourceUrl = "",
    )
    val impactColor = if (selected.impact.startsWith("HIGH")) FxTheme.colors.down else FxTheme.colors.accent
    ScreenScaffold {
        BackNavButton(label = ui("News"), onClick = onBack)
        ScreenHeader(
            ui("News detail"),
            sub = "${selected.tag} · ${selected.impact}",
            subtitle = "${selected.source.ifBlank { ui("Market source") }} · ${selected.age}",
        )
        BentoCard(padding = 14.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Pill(selected.tag, variant = PillVariant.Accent)
                    Eyebrow(selected.impact, color = impactColor)
                }
                Text(selected.title, style = FxTheme.typography.titleL, color = FxTheme.colors.text)
                Text(selected.summary, style = FxTheme.typography.body, color = FxTheme.colors.textDim)
            }
        }
        SectionLabel(ui("MARKET MOVES"))
        BentoCard(padding = 12.dp) {
            if (selected.moves.isEmpty()) {
                Text(ui("No direct currency move was detected for this story."), style = FxTheme.typography.caption, color = FxTheme.colors.textFaint)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    selected.moves.forEach { (code, change) ->
                        KeyValueRow(code, formatChange(change))
                    }
                }
            }
        }
        SectionLabel(ui("SOURCE"))
        BentoCard(padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                KeyValueRow(ui("Publisher"), selected.source.ifBlank { ui("Market source") })
                KeyValueRow(ui("Published"), selected.age)
                if (selected.sourceUrl.isNotBlank()) {
                    GhostButton(ui("Open original source"), onClick = { onOpenUrl(selected.sourceUrl) })
                } else {
                    Text(ui("This item is generated from the fallback market brief, so there is no external article link."), style = FxTheme.typography.caption, color = FxTheme.colors.textFaint)
                }
            }
        }
    }
}
