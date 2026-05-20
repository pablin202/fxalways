package com.fxalways.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.CurrencyKind
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.theme.FxTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CurrencyPickerSheet(
    title: String,
    subtitle: String,
    currencies: List<FxRate>,
    selectedCode: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var showAll by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val rows = remember(currencies, query) {
        val term = query.trim()
        currencies
            .distinctBy { it.code }
            .filter { currency ->
                term.isBlank() ||
                    currency.code.contains(term, ignoreCase = true) ||
                    currency.name.contains(term, ignoreCase = true)
            }
            .sortedWith(compareByDescending<FxRate> { it.code in PopularCurrencyCodes }.thenBy { it.name })
    }
    val visibleRows = remember(rows, query, showAll, selectedCode) {
        if (query.isNotBlank() || showAll || rows.size <= DefaultPickerVisibleLimit) {
            rows
        } else {
            val selected = rows.firstOrNull { it.code == selectedCode }
            (listOfNotNull(selected) + rows.filterNot { it.code == selectedCode })
                .distinctBy { it.code }
                .take(DefaultPickerVisibleLimit)
        }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FxTheme.colors.surface1,
        contentColor = FxTheme.colors.text,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(max = 620.dp)
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(title, style = FxTheme.typography.titleL, color = FxTheme.colors.text)
                Text(subtitle, style = FxTheme.typography.caption, color = FxTheme.colors.textFaint)
            }
            BentoCard(padding = 12.dp) {
                BasicTextField(
                    value = query,
                    onValueChange = { query = it.take(24) },
                    singleLine = true,
                    textStyle = FxTheme.typography.body.copy(color = FxTheme.colors.text),
                    modifier = Modifier.fillMaxWidth().testTag("currency_picker_search"),
                    decorationBox = { innerTextField ->
                        if (query.isBlank()) {
                            Text(ui("Search currency"), style = FxTheme.typography.body, color = FxTheme.colors.textGhost)
                        }
                        innerTextField()
                    },
                )
            }
            if (query.isBlank() && !showAll && rows.size > visibleRows.size) {
                SettingChoiceRow(
                    title = "${ui("Showing top")} ${visibleRows.size}/${rows.size}",
                    subtitle = ui("Search currency"),
                    selected = false,
                    actionLabel = ui("Show all"),
                    modifier = Modifier.testTag("currency_picker_show_all"),
                    onClick = { showAll = true },
                )
            }
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .height(390.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(visibleRows, key = { it.code }) { currency ->
                    SettingChoiceRow(
                        title = "${currency.glyph}  ${currency.code}",
                        subtitle = "${assetKindLabel(currency)} · ${localizedCurrencyName(currency.name)}",
                        selected = currency.code == selectedCode,
                        modifier = Modifier.testTag("currency_picker_${currency.code}"),
                        onClick = { onSelect(currency.code) },
                    )
                }
                if (visibleRows.isEmpty()) {
                    item {
                        Text(ui("No currencies found"), style = FxTheme.typography.caption, color = FxTheme.colors.textFaint)
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
internal fun assetKindLabel(currency: FxRate): String =
    when {
        currency.kind == CurrencyKind.Crypto && currency.code in StablecoinCodes -> ui("Stablecoin")
        currency.kind == CurrencyKind.Crypto -> ui("Crypto")
        else -> ui("Fiat")
    }
