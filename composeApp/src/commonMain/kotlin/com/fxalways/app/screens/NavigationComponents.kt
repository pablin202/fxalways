package com.fxalways.app.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fxalways.designsystem.theme.FxTheme

@Composable
internal fun BackNavButton(label: String?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(FxTheme.shapes.field)
            .clickable(onClick = onClick)
            .padding(end = 12.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("←", style = FxTheme.typography.numberL.copy(fontSize = 34.sp), color = FxTheme.colors.text)
        if (label != null) {
            Text(label, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.textDim)
        }
    }
}
