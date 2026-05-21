package com.fxalways.app.screens.portfolio

import com.fxalways.app.screens.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.fxalways.app.data.PortfolioCsvImportResult
import com.fxalways.app.data.PortfolioTransaction
import com.fxalways.app.data.PortfolioTransactionType
import com.fxalways.app.data.Watchlist
import com.fxalways.app.data.toPortfolioCsv
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.KeyValueRow
import com.fxalways.designsystem.theme.FxTheme

@Composable
internal fun PortfolioTransactionsCard(
    baseCurrency: String,
    holdings: List<PortfolioHolding>,
    transactions: List<PortfolioTransaction>,
    onRecordTransaction: (String, PortfolioTransactionType, Double, Double) -> Unit,
) {
    val codes = remember(holdings) { holdings.map { it.rate.code }.distinct() }
    var selectedCode by remember(codes) { mutableStateOf(codes.firstOrNull().orEmpty()) }
    if (selectedCode !in codes) selectedCode = codes.firstOrNull().orEmpty()
    var selectedType by remember { mutableStateOf(PortfolioTransactionType.Buy) }
    var amountText by remember(selectedCode) { mutableStateOf("") }
    var priceText by remember(selectedCode) { mutableStateOf("") }
    val amount = parseAmountInput(amountText)
    val price = parseAmountInput(priceText)
    val latestTransactions = remember(transactions) { transactions.sortedByDescending { it.createdAtMillis }.take(5) }

    BentoCard(Modifier.testTag("watchlist_transactions"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                codes.forEach { code ->
                    TransactionChip(
                        label = code,
                        selected = selectedCode == code,
                        modifier = Modifier.testTag("watchlist_transaction_asset_$code"),
                        onClick = { selectedCode = code },
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TransactionChip(
                    label = ui("Buy"),
                    selected = selectedType == PortfolioTransactionType.Buy,
                    modifier = Modifier.weight(1f).testTag("watchlist_transaction_buy"),
                    onClick = { selectedType = PortfolioTransactionType.Buy },
                )
                TransactionChip(
                    label = ui("Sell"),
                    selected = selectedType == PortfolioTransactionType.Sell,
                    modifier = Modifier.weight(1f).testTag("watchlist_transaction_sell"),
                    onClick = { selectedType = PortfolioTransactionType.Sell },
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TransactionInputField(
                    value = amountText,
                    placeholder = ui("amount"),
                    modifier = Modifier.weight(1f).testTag("watchlist_transaction_amount"),
                    onValueChange = { amountText = it },
                )
                TransactionInputField(
                    value = priceText,
                    placeholder = "${ui("price")} $baseCurrency",
                    modifier = Modifier.weight(1f).testTag("watchlist_transaction_price"),
                    onValueChange = { priceText = it },
                )
            }
            PrimaryButton(
                text = ui("Record transaction"),
                modifier = Modifier.fillMaxWidth().testTag("watchlist_transaction_record"),
                onClick = {
                    if (selectedCode.isNotBlank() && amount > 0.0 && price > 0.0) {
                        onRecordTransaction(selectedCode, selectedType, amount, price)
                        amountText = ""
                        priceText = ""
                    }
                },
            )
            if (latestTransactions.isEmpty()) {
                Text(
                    ui("No transactions yet"),
                    style = FxTheme.typography.caption,
                    color = FxTheme.colors.textDim,
                    modifier = Modifier.testTag("watchlist_no_transactions"),
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    latestTransactions.forEach { transaction ->
                        PortfolioTransactionRow(baseCurrency = baseCurrency, transaction = transaction)
                    }
                }
            }
        }
    }
}

@Composable
internal fun PortfolioImportExportCard(
    watchlist: Watchlist,
    onImportPortfolioCsv: (String) -> PortfolioCsvImportResult,
) {
    val clipboardManager = LocalClipboardManager.current
    var importText by remember { mutableStateOf("") }
    var importFeedback by remember { mutableStateOf<String?>(null) }
    var exportFeedback by remember { mutableStateOf<String?>(null) }
    val exportCsv = remember(watchlist) { watchlist.toPortfolioCsv() }
    val exportHoldingCount = watchlist.holdings.count { it.value > 0.0 || (watchlist.holdingCosts[it.key] ?: 0.0) > 0.0 }
    val exportTransactionCount = watchlist.transactions.size
    val holdingsCopy = ui("holdings")
    val transactionsCopy = ui("transactions")
    val skippedCopy = ui("skipped")
    val noValidRowsCopy = ui("No valid portfolio rows found")
    val exportCopiedCopy = ui("Export copied")

    BentoCard(Modifier.testTag("watchlist_import_export"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                ui("Portfolio CSV backup"),
                style = FxTheme.typography.bodyStrong,
                color = FxTheme.colors.text,
            )
            Text(
                ui("Copy a manual backup or paste one back in to restore portfolio data."),
                style = FxTheme.typography.caption,
                color = FxTheme.colors.textDim,
            )
            KeyValueRow(
                ui("Export CSV"),
                "$exportHoldingCount $holdingsCopy · $exportTransactionCount $transactionsCopy",
                ui("manual backup"),
                modifier = Modifier.testTag("watchlist_export_summary"),
            )
            PrimaryButton(
                text = ui("Copy export CSV"),
                modifier = Modifier.fillMaxWidth().testTag("watchlist_copy_export_csv"),
                onClick = {
                    clipboardManager.setText(AnnotatedString(exportCsv))
                    exportFeedback = exportCopiedCopy
                },
            )
            exportFeedback?.let {
                Text(
                    it,
                    style = FxTheme.typography.captionMono,
                    color = FxTheme.colors.accent,
                    modifier = Modifier.testTag("watchlist_export_feedback"),
                )
            }
            CsvTextBox(
                value = exportCsv,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier.testTag("watchlist_export_csv"),
            )
            KeyValueRow(
                ui("Import CSV"),
                ui("Paste rows below"),
                ui("merge safe"),
                modifier = Modifier.testTag("watchlist_import_summary"),
            )
            CsvTextBox(
                value = importText,
                onValueChange = { importText = it.take(4_000) },
                readOnly = false,
                placeholder = ui("Paste portfolio CSV"),
                modifier = Modifier.testTag("watchlist_import_csv"),
            )
            PrimaryButton(
                text = ui("Import CSV"),
                modifier = Modifier.fillMaxWidth().testTag("watchlist_import_csv_button"),
                onClick = {
                    val result = onImportPortfolioCsv(importText)
                    importFeedback = if (result.hasImports) {
                        "${result.importedHoldings} $holdingsCopy · ${result.importedTransactions} $transactionsCopy · ${result.skippedRows} $skippedCopy"
                    } else {
                        noValidRowsCopy
                    }
                    if (result.hasImports) importText = ""
                },
            )
            importFeedback?.let {
                Text(
                    it,
                    style = FxTheme.typography.captionMono,
                    color = if (it == noValidRowsCopy) FxTheme.colors.down else FxTheme.colors.accent,
                    modifier = Modifier.testTag("watchlist_import_feedback"),
                )
            }
        }
    }
}
