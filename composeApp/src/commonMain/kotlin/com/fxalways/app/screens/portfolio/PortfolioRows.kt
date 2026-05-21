package com.fxalways.app.screens.portfolio

import com.fxalways.app.screens.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fxalways.app.data.PortfolioTransaction
import com.fxalways.app.data.PortfolioTransactionType
import com.fxalways.designsystem.components.FlagDot
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.components.Pill
import com.fxalways.designsystem.components.PillVariant
import com.fxalways.designsystem.components.formatRate
import com.fxalways.designsystem.theme.FxTheme

@Composable
internal fun CsvTextBox(
    value: String,
    onValueChange: (String) -> Unit,
    readOnly: Boolean,
    modifier: Modifier = Modifier,
    placeholder: String = "",
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        readOnly = readOnly,
        minLines = 4,
        maxLines = 6,
        textStyle = FxTheme.typography.captionMono.copy(color = FxTheme.colors.text),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 96.dp)
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface1)
            .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        decorationBox = { innerTextField ->
            if (value.isBlank() && placeholder.isNotBlank()) {
                TextPlaceholder(placeholder)
            }
            innerTextField()
        },
    )
}

@Composable
internal fun TransactionInputField(
    value: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
) {
    BasicTextField(
        value = value,
        onValueChange = { raw -> onValueChange(raw.filter { it.isDigit() || it == '.' || it == ',' }.take(12)) },
        singleLine = true,
        textStyle = FxTheme.typography.numberBody.copy(color = FxTheme.colors.text, textAlign = TextAlign.End),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
        modifier = modifier
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface1)
            .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        decorationBox = { innerTextField ->
            if (value.isBlank()) {
                androidx.compose.material3.Text(
                    placeholder,
                    style = FxTheme.typography.captionMono,
                    color = FxTheme.colors.textGhost,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End,
                )
            }
            innerTextField()
        },
    )
}

@Composable
internal fun PortfolioTransactionRow(baseCurrency: String, transaction: PortfolioTransaction) {
    val pnl = transaction.realizedPnlBase
    Row(
        Modifier
            .fillMaxWidth()
            .testTag("watchlist_transaction_${transaction.id}")
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            androidx.compose.material3.Text(
                "${ui(transaction.type.name)} ${transaction.code} ${formatMoneyValue(transaction.amount)}",
                style = FxTheme.typography.bodyStrong,
                color = FxTheme.colors.text,
            )
            androidx.compose.material3.Text(
                "${baseCurrency} ${formatMoneyValue(transaction.priceBase)} · ${localizedShortAgeLabel(transaction.createdAtMillis)}",
                style = FxTheme.typography.captionMono,
                color = FxTheme.colors.textFaint,
            )
        }
        androidx.compose.material3.Text(
            if (transaction.type == PortfolioTransactionType.Sell) formatSignedMoney(pnl, baseCurrency) else ui("cost basis"),
            style = FxTheme.typography.captionMono,
            color = when {
                transaction.type == PortfolioTransactionType.Buy -> FxTheme.colors.textDim
                pnl >= 0.0 -> FxTheme.colors.up
                else -> FxTheme.colors.down
            },
        )
    }
}

@Composable
internal fun PortfolioHoldingRow(
    baseCurrency: String,
    holding: PortfolioHolding,
    portfolioValue: Double,
    canEditCostBasis: Boolean,
    onAmountChange: (Double) -> Unit,
    onCostChange: (Double) -> Unit,
    onOpenDetail: () -> Unit,
) {
    val rate = holding.rate
    val amount = holding.amount
    val focusManager = LocalFocusManager.current
    var amountText by remember(rate.code) { mutableStateOf(if (amount > 0.0) formatRate(amount) else "") }
    var amountFocused by remember(rate.code) { mutableStateOf(false) }
    var costText by remember(rate.code) { mutableStateOf(if (holding.averageCostBase > 0.0) formatRate(holding.averageCostBase) else "") }
    var costFocused by remember(rate.code) { mutableStateOf(false) }
    LaunchedEffect(amount, amountFocused) {
        if (!amountFocused) amountText = if (amount > 0.0) formatRate(amount) else ""
    }
    LaunchedEffect(holding.averageCostBase, costFocused) {
        if (!costFocused) costText = if (holding.averageCostBase > 0.0) formatRate(holding.averageCostBase) else ""
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("watchlist_holding_${rate.code}")
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlagDot(rate.glyph, rate.kind, 28.dp, modifier = Modifier.clickable(onClick = onOpenDetail))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            androidx.compose.material3.Text(
                "${rate.code} ${ui("holding")}",
                style = FxTheme.typography.bodyStrong,
                color = FxTheme.colors.text,
                modifier = Modifier
                    .testTag("watchlist_detail_${rate.code}")
                    .clickable(onClick = onOpenDetail),
            )
            val holdingSubtitle = if (amount <= 0.0) {
                "${ui("Tracking live rate")} ${formatRate(rate.rate)} · ${ui("enter amount held")}"
            } else if (canEditCostBasis && holding.hasCostBasis) {
                "${formatSignedMoney(holding.unrealizedPnlBase, baseCurrency)} ${ui("unrealized")} · ${holding.weightLabel(portfolioValue)} · ${holding.dailyChangeLabel(baseCurrency)}"
            } else {
                "$baseCurrency ${formatMoneyValue(holding.baseValue)} · ${holding.weightLabel(portfolioValue)} · ${holding.dailyChangeLabel(baseCurrency)}"
            }
            androidx.compose.material3.Text(
                holdingSubtitle,
                style = FxTheme.typography.captionMono,
                color = if (amount <= 0.0) {
                    FxTheme.colors.textFaint
                } else if (canEditCostBasis && holding.hasCostBasis) {
                    if (holding.unrealizedPnlBase >= 0.0) FxTheme.colors.up else FxTheme.colors.down
                } else if (holding.dailyChangeInBase >= 0.0) {
                    FxTheme.colors.up
                } else {
                    FxTheme.colors.down
                },
            )
        }
        Column(
            modifier = Modifier.width(112.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            BasicTextField(
                value = amountText,
                onValueChange = { raw ->
                    val next = raw.filter { it.isDigit() || it == '.' || it == ',' }.take(12)
                    amountText = next
                    onAmountChange(parseAmountInput(next))
                },
                singleLine = true,
                textStyle = FxTheme.typography.numberBody.copy(color = FxTheme.colors.text, textAlign = TextAlign.End),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("watchlist_amount_${rate.code}")
                    .clip(FxTheme.shapes.field)
                    .background(if (amountFocused) FxTheme.colors.accentSoft else FxTheme.colors.surface2)
                    .border(1.dp, if (amountFocused) FxTheme.colors.accent else FxTheme.colors.border, FxTheme.shapes.field)
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .onFocusChanged { amountFocused = it.isFocused },
                decorationBox = { innerTextField ->
                    if (amountText.isBlank()) {
                        androidx.compose.material3.Text(
                            ui("amount"),
                            style = FxTheme.typography.captionMono,
                            color = FxTheme.colors.textGhost,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End,
                        )
                    }
                    innerTextField()
                },
            )
            if (amountFocused) {
                androidx.compose.material3.Text(
                    ui("done"),
                    style = FxTheme.typography.captionMono,
                    color = FxTheme.colors.accent,
                    modifier = Modifier
                        .testTag("watchlist_amount_done_${rate.code}")
                        .clickable { focusManager.clearFocus() },
                )
            }
            if (canEditCostBasis) {
                BasicTextField(
                    value = costText,
                    onValueChange = { raw ->
                        val next = raw.filter { it.isDigit() || it == '.' || it == ',' }.take(12)
                        costText = next
                        onCostChange(parseAmountInput(next))
                    },
                    singleLine = true,
                    textStyle = FxTheme.typography.captionMono.copy(color = FxTheme.colors.text, textAlign = TextAlign.End),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("watchlist_cost_${rate.code}")
                        .clip(FxTheme.shapes.field)
                        .background(if (costFocused) FxTheme.colors.accentSoft else FxTheme.colors.surface1)
                        .border(1.dp, if (costFocused) FxTheme.colors.accent else FxTheme.colors.border, FxTheme.shapes.field)
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                        .onFocusChanged { costFocused = it.isFocused },
                    decorationBox = { innerTextField ->
                        if (costText.isBlank()) {
                            androidx.compose.material3.Text(
                                ui("avg cost"),
                                style = FxTheme.typography.captionMono,
                                color = FxTheme.colors.textGhost,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End,
                            )
                        }
                        innerTextField()
                    },
                )
            }
        }
    }
}

@Composable
internal fun WatchlistCurrencyRow(
    rate: FxRate,
    selected: Boolean,
    locked: Boolean,
    amount: Double,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("watchlist_currency_${rate.code}")
            .clip(FxTheme.shapes.field)
            .background(if (selected) FxTheme.colors.accentSoft else Color.Transparent)
            .clickable(onClick = onToggle)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlagDot(rate.glyph, rate.kind, 28.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            androidx.compose.material3.Text(rate.code, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            androidx.compose.material3.Text(
                if (amount > 0.0) "${formatRate(amount)} ${ui("held")} · ${localizedCurrencyName(rate.name)}" else localizedCurrencyName(rate.name),
                style = FxTheme.typography.caption,
                color = FxTheme.colors.textFaint,
            )
        }
        androidx.compose.material3.Text(formatRate(rate.rate), style = FxTheme.typography.numberBody, color = FxTheme.colors.textDim)
        Pill(
            text = when {
                selected -> ui("tracked")
                locked -> ui("pro")
                else -> ui("add")
            },
            variant = if (selected) PillVariant.Accent else if (locked) PillVariant.Accent else PillVariant.Ghost,
        )
    }
}

@Composable
private fun TextPlaceholder(value: String) {
    androidx.compose.material3.Text(value, style = FxTheme.typography.captionMono, color = FxTheme.colors.textGhost)
}
