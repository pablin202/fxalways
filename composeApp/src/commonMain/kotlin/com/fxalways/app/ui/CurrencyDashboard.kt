package com.fxalways.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import com.fxalways.app.Platform
import com.fxalways.app.PlatformConfig
import com.fxalways.app.domain.DashboardState
import com.fxalways.app.domain.HistoricalPoint
import com.fxalways.app.domain.MajorCurrencies
import com.fxalways.app.domain.RateCard
import kotlin.math.roundToInt

@Composable
fun CurrencyDashboard(controller: DashboardController) {
    val state by controller.state.collectAsState()
    var selectedLanguage by remember { mutableStateOf<String?>(null) }
    val strings = rememberFxStrings(selectedLanguage)

    MaterialTheme(colorScheme = fxColorScheme) {
        Scaffold(
            topBar = {
                FxTopBar(
                    isPremium = state.isPremium,
                    strings = strings,
                    selectedLanguage = selectedLanguage,
                    onLanguageSelected = { selectedLanguage = it },
                    onRefresh = controller::refresh,
                )
            },
        ) { padding ->
            DashboardContent(
                state = state,
                strings = strings,
                onAmountChange = controller::onAmountChange,
                onBaseChange = controller::onBaseChange,
                onQuoteChange = controller::onQuoteChange,
                onRefresh = controller::refresh,
                onBuyMonthly = controller::buyMonthly,
                modifier = Modifier.padding(padding),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FxTopBar(
    isPremium: Boolean,
    strings: FxStrings,
    selectedLanguage: String?,
    onLanguageSelected: (String) -> Unit,
    onRefresh: () -> Unit,
) {
    var languageMenuExpanded by remember { mutableStateOf(false) }
    val activeLanguage = selectedLanguage ?: Locale.current.language.lowercase()
    val activeLanguageLabel = SupportedLanguages.firstOrNull { it.code == activeLanguage }?.code?.uppercase() ?: "EN"

    TopAppBar(
        title = {
            Column {
                Text(strings.appName, fontWeight = FontWeight.Bold)
                Text(
                    text = if (isPremium) strings.proActive else strings.cachedRatesStatus,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        actions = {
            Box {
                TextButton(onClick = { languageMenuExpanded = true }) {
                    Text(activeLanguageLabel)
                }
                DropdownMenu(
                    expanded = languageMenuExpanded,
                    onDismissRequest = { languageMenuExpanded = false },
                ) {
                    SupportedLanguages.forEach { language ->
                        DropdownMenuItem(
                            text = { Text(language.label) },
                            onClick = {
                                languageMenuExpanded = false
                                onLanguageSelected(language.code)
                            },
                        )
                    }
                }
            }
            TextButton(onClick = onRefresh) {
                Text(strings.refresh)
            }
        },
    )
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun DashboardContent(
    state: DashboardState,
    strings: FxStrings,
    onAmountChange: (String) -> Unit,
    onBaseChange: (String) -> Unit,
    onQuoteChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onBuyMonthly: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFF8FAFC), Color(0xFFEFF6F3)),
                ),
            )
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ConverterPanel(state, strings, onAmountChange, onBaseChange, onQuoteChange)

        if (state.errorMessage != null) {
            ErrorPanel(state.errorMessage.ifBlank { strings.loadError }, strings, onRefresh)
        }

        ChartPanel(state, strings)

        if (PlatformConfig.platform == Platform.Ios && !state.isPremium) {
            ProPanel(strings, onBuyMonthly)
        }

        SectionTitle(strings.globalWatchlist)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            state.watchCards.forEach { card ->
                RateCardView(card, modifier = Modifier.weight(1f).fillMaxWidth())
            }
        }

        InsightPanel(strings)
    }
}

@Composable
private fun ConverterPanel(
    state: DashboardState,
    strings: FxStrings,
    onAmountChange: (String) -> Unit,
    onBaseChange: (String) -> Unit,
    onQuoteChange: (String) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 2.dp,
        color = Color.White,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(strings.converter, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(
                        state.latest?.let { strings.sourceLine(it.provider, it.date) } ?: strings.loadingRates,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                StatusDot(isLoading = state.isLoading)
            }

            OutlinedTextField(
                value = state.amount,
                onValueChange = onAmountChange,
                label = { Text(strings.amount) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CurrencySelector(strings.from, state.base, onBaseChange, Modifier.weight(1f))
                CurrencySelector(strings.to, state.quote, onQuoteChange, Modifier.weight(1f))
            }

            Text(
                text = state.convertedAmount?.let { "${state.quote} ${it.format(2)}" } ?: strings.waitingForData,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun CurrencySelector(
    label: String,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                Text(label, style = MaterialTheme.typography.labelSmall)
                Text(selected, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            MajorCurrencies.forEach { currency ->
                DropdownMenuItem(
                    text = { Text(currency) },
                    onClick = {
                        expanded = false
                        onSelected(currency)
                    },
                )
            }
        }
    }
}

@Composable
private fun ChartPanel(state: DashboardState, strings: FxStrings) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color.White,
        tonalElevation = 1.dp,
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("${state.base}/${state.quote}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(strings.historyLine(state.isPremium), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                state.historical?.points?.dailyChange()?.let { change ->
                    Text(
                        text = "${if (change >= 0) "+" else ""}${change.format(2)}%",
                        color = if (change >= 0) Color(0xFF16794D) else Color(0xFFB42318),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }

            RateChart(
                points = state.historical?.points.orEmpty(),
                modifier = Modifier.fillMaxWidth().aspectRatio(1.9f),
            )
        }
    }
}

@Composable
private fun RateChart(points: List<HistoricalPoint>, modifier: Modifier = Modifier) {
    val lineColor = MaterialTheme.colorScheme.primary
    val fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
    Canvas(modifier = modifier) {
        if (points.size < 2) return@Canvas

        val values = points.map { it.value }
        val min = values.minOrNull() ?: return@Canvas
        val max = values.maxOrNull() ?: return@Canvas
        val range = (max - min).takeIf { it != 0.0 } ?: 1.0
        val step = size.width / (points.lastIndex)

        fun pointAt(index: Int): Offset {
            val x = step * index
            val y = size.height - (((points[index].value - min) / range).toFloat() * size.height)
            return Offset(x, y)
        }

        val path = Path().apply {
            moveTo(pointAt(0).x, pointAt(0).y)
            for (index in 1..points.lastIndex) {
                lineTo(pointAt(index).x, pointAt(index).y)
            }
        }

        val area = Path().apply {
            addPath(path)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }

        drawPath(area, fillColor)
        drawPath(path, lineColor, style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
private fun ProPanel(strings: FxStrings, onBuyMonthly: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFF0F172A),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(strings.proTitle, color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    strings.proSubtitle,
                    color = Color(0xFFCBD5E1),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            Spacer(Modifier.width(12.dp))
            Button(onClick = onBuyMonthly) {
                Text(strings.subscribe)
            }
        }
    }
}

@Composable
private fun RateCardView(card: RateCard, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${card.pair.base}/${card.pair.quote}", fontWeight = FontWeight.Bold)
                card.dailyChangePct?.let {
                    Text(
                        "${if (it >= 0) "+" else ""}${it.format(2)}%",
                        color = if (it >= 0) Color(0xFF16794D) else Color(0xFFB42318),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
            Text(card.rate.format(4), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            RateChart(card.sparkline, Modifier.fillMaxWidth().height(56.dp))
        }
    }
}

@Composable
private fun InsightPanel(strings: FxStrings) {
    Surface(shape = RoundedCornerShape(8.dp), color = Color.White) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(strings.featuresTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(strings.cacheFeature)
            Text(strings.historyFeature)
            Text(strings.paywallFeature)
        }
    }
}

@Composable
private fun ErrorPanel(message: String, strings: FxStrings, onRefresh: () -> Unit) {
    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFFFF1F0)) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(message, modifier = Modifier.weight(1f), color = Color(0xFFB42318))
            TextButton(onClick = onRefresh) { Text(strings.retry) }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}

@Composable
private fun StatusDot(isLoading: Boolean) {
    Box(
        modifier = Modifier
            .size(12.dp)
            .background(if (isLoading) Color(0xFFF59E0B) else Color(0xFF22C55E), RoundedCornerShape(50)),
    )
}

private fun List<HistoricalPoint>.dailyChange(): Double? {
    if (size < 2) return null
    val previous = this[size - 2].value
    if (previous == 0.0) return null
    return ((last().value - previous) / previous) * 100.0
}

private fun Double.format(decimals: Int): String {
    val factor = when (decimals) {
        0 -> 1.0
        1 -> 10.0
        2 -> 100.0
        3 -> 1000.0
        else -> 10000.0
    }
    val rounded = (this * factor).roundToInt() / factor
    return rounded.toString()
}

private val fxColorScheme = lightColorScheme(
    primary = Color(0xFF006C52),
    secondary = Color(0xFF475569),
    surface = Color(0xFFF8FAFC),
    background = Color(0xFFF8FAFC),
    onPrimary = Color.White,
    onSurface = Color(0xFF0F172A),
)
