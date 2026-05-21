package com.fxalways.app.screens.news

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fxalways.app.screens.ui
import com.fxalways.designsystem.components.Eyebrow
import com.fxalways.designsystem.theme.FxTheme

@Composable
internal fun NewsSearchField(query: String, onQueryChange: (String) -> Unit) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        textStyle = FxTheme.typography.body.copy(color = FxTheme.colors.text),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(FxTheme.shapes.field)
                    .background(FxTheme.colors.surface2)
                    .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
                    .padding(horizontal = 12.dp, vertical = 11.dp),
            ) {
                if (query.isBlank()) {
                    Text(ui("Search headlines, tags or currencies"), style = FxTheme.typography.caption, color = FxTheme.colors.textGhost)
                }
                innerTextField()
            }
        },
    )
}

@Composable
internal fun NewsFilterRow(
    label: String,
    options: List<String>,
    selected: String,
    enabled: Boolean,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Eyebrow(label)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { option ->
                Box(
                    modifier = Modifier
                        .clip(FxTheme.shapes.pill)
                        .background(if (selected == option) FxTheme.colors.accentSoft else Color.Transparent)
                        .border(
                            1.dp,
                            if (selected == option) FxTheme.colors.accentLine else FxTheme.colors.border,
                            FxTheme.shapes.pill,
                        )
                        .clickable { onSelect(option) }
                        .padding(horizontal = 11.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (enabled || selected == option) option else "$option Pro",
                        style = FxTheme.typography.captionMono,
                        color = if (selected == option) FxTheme.colors.accent else FxTheme.colors.textDim,
                    )
                }
            }
        }
    }
}
