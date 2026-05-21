package com.fxalways.app.screens.news

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.fxalways.app.data.mock.NewsStory
import com.fxalways.app.screens.ui
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.Eyebrow
import com.fxalways.designsystem.components.Pill
import com.fxalways.designsystem.components.PillVariant
import com.fxalways.designsystem.components.formatChange
import com.fxalways.designsystem.theme.FxTheme

@Composable
internal fun StoryCard(story: NewsStory, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    BentoCard(
        padding = 12.dp,
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Pill(story.tag, variant = PillVariant.Ghost)
                    Eyebrow(ui(story.impact), color = if (story.impact.startsWith("HIGH")) FxTheme.colors.down else FxTheme.colors.accent)
                }
                Text(story.age, style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
            }
            Text(story.title, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text(story.summary, style = FxTheme.typography.body, color = FxTheme.colors.textDim)
            if (story.source.isNotBlank()) {
                Text(
                    if (story.sourceUrl.isNotBlank()) "${story.source} · ${ui("tap for details")}" else story.source,
                    style = FxTheme.typography.captionMono,
                    color = if (story.sourceUrl.isNotBlank()) FxTheme.colors.accent else FxTheme.colors.textFaint,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(ui("MOVES"))
                story.moves.forEach { (code, change) ->
                    Pill("$code ${formatChange(change)}", variant = if (change >= 0) PillVariant.Up else PillVariant.Down)
                }
            }
        }
    }
}

internal fun NewsStory.safeTestTagKey(): String =
    title
        .filter { it.isLetterOrDigit() }
        .take(18)
        .ifBlank { tag }
