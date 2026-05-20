package com.fxalways.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.GridBg
import com.fxalways.designsystem.components.LiveDot
import com.fxalways.designsystem.theme.FxTheme

@Composable
internal fun StartupLoadingScreen(baseCurrency: String, language: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        contentAlignment = Alignment.Center,
    ) {
        GridBg(Modifier.matchParentSize().alpha(0.10f), radialMask = false)
        GridBg(Modifier.matchParentSize().alpha(0.22f))
        BentoCard(padding = 18.dp) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LiveDot(Modifier.size(10.dp))
                Text("${localizedUiText(language, "Preparing workspace")} · $baseCurrency", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                Text(localizedUiText(language, "Loading account, preferences and rates"), style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
            }
        }
    }
}

@Composable
internal fun ScreenScaffold(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        content()
        Spacer(Modifier.height(152.dp))
    }
}
