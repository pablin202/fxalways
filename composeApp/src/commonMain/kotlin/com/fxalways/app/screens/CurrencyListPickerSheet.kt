package com.fxalways.app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import com.fxalways.app.subscription.cap
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.theme.FxTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CurrencyListPickerSheet(
    title: String,
    lockedSubtitle: String,
    currencies: List<FxRate>,
    selectedCodes: List<String>,
    limit: Int,
    isPremium: Boolean,
    onDismiss: () -> Unit,
    onOpenPaywall: () -> Unit,
    onApply: (List<String>) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    var showAll by remember { mutableStateOf(false) }
    var draftCodes by remember(selectedCodes) { mutableStateOf(selectedCodes) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val effectiveLimit = limit.cap(currencies.size).coerceAtLeast(1)
    fun applyDraftAndDismiss() {
        if (draftCodes.isNotEmpty()) {
            onApply(draftCodes.take(effectiveLimit))
        } else {
            onDismiss()
        }
    }
    val rows = remember(currencies, query) {
        val term = query.trim()
        currencies
            .distinctBy { it.code }
            .filter { currency ->
                term.isBlank() ||
                    currency.code.contains(term, ignoreCase = true) ||
                    currency.name.contains(term, ignoreCase = true)
            }
            .sortedWith(compareByDescending<FxRate> { it.code in PopularCurrencyCodes }.thenBy { it.code })
    }
    val visibleRows = remember(rows, query, showAll, draftCodes) {
        if (query.isNotBlank() || showAll || rows.size <= DefaultPickerVisibleLimit) {
            rows
        } else {
            val selected = rows.filter { it.code in draftCodes }
            (selected + rows.filterNot { it.code in draftCodes })
                .distinctBy { it.code }
                .take(DefaultPickerVisibleLimit)
        }
    }
    ModalBottomSheet(
        onDismissRequest = { applyDraftAndDismiss() },
        sheetState = sheetState,
        containerColor = FxTheme.colors.surface1,
        contentColor = FxTheme.colors.text,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .heightIn(max = 660.dp)
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(title, style = FxTheme.typography.titleL, color = FxTheme.colors.text)
                Text(
                    if (isPremium) {
                        "${draftCodes.size} ${ui("selected")} · ${ui("every supported currency available")}"
                    } else {
                        "${draftCodes.size}/$effectiveLimit ${ui("selected")} · ${ui("Pro unlocks the full list")}"
                    },
                    style = FxTheme.typography.caption,
                    color = FxTheme.colors.textFaint,
                )
            }
            BentoCard(padding = 12.dp) {
                BasicTextField(
                    value = query,
                    onValueChange = { query = it.take(24) },
                    singleLine = true,
                    textStyle = FxTheme.typography.body.copy(color = FxTheme.colors.text),
                    modifier = Modifier.fillMaxWidth().testTag("currency_list_search"),
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
                    modifier = Modifier.testTag("currency_list_show_all"),
                    onClick = { showAll = true },
                )
            }
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .heightIn(max = 430.dp)
                    .testTag("currency_list_scroll"),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(visibleRows, key = { it.code }) { currency ->
                    val selected = currency.code in draftCodes
                    val locked = !selected && draftCodes.size >= effectiveLimit
                    SettingChoiceRow(
                        title = "${currency.glyph}  ${currency.code}",
                        subtitle = if (locked && !isPremium) lockedSubtitle else "${assetKindLabel(currency)} · ${localizedCurrencyName(currency.name)}",
                        selected = selected,
                        actionLabel = if (selected) ui("added") else if (locked) ui("pro") else ui("add"),
                        modifier = Modifier.testTag("currency_list_${currency.code}"),
                        onClick = {
                            when {
                                selected -> draftCodes = draftCodes.filterNot { it == currency.code }
                                locked -> onOpenPaywall()
                                else -> draftCodes = (draftCodes + currency.code).distinct()
                            }
                        },
                    )
                }
                if (visibleRows.isEmpty()) {
                    item {
                        Text(ui("No currencies found"), style = FxTheme.typography.caption, color = FxTheme.colors.textFaint)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GhostButton(ui("Cancel"), Modifier.weight(1f), onClick = onDismiss)
                PrimaryButton(
                    ui("Apply"),
                    Modifier.weight(1f).testTag("currency_list_apply"),
                    onClick = { applyDraftAndDismiss() },
                )
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}
