package com.fxalways.app.screens.traveler

import com.fxalways.app.screens.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fxalways.app.data.LiveRatesState
import com.fxalways.app.data.SettingsBaseCurrencies
import com.fxalways.app.data.mock.FavoriteRates
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.app.subscription.cap
import com.fxalways.app.subscription.featureAccess
import com.fxalways.app.screens.converter.PriceScannerCard
import com.fxalways.app.screens.detail.LoadingSkeletonCard
import com.fxalways.app.screens.detail.compactRuntimeLabel
import com.fxalways.app.screens.detail.isInitialRateLoading
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.BentoTile
import com.fxalways.designsystem.components.BigValueText
import com.fxalways.designsystem.components.Eyebrow
import com.fxalways.designsystem.components.FlagDot
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.components.GridBg
import com.fxalways.designsystem.components.KeyValueRow
import com.fxalways.designsystem.components.MetricTile
import com.fxalways.designsystem.components.Pill
import com.fxalways.designsystem.components.PillVariant
import com.fxalways.designsystem.components.ScreenHeader
import com.fxalways.designsystem.components.SectionLabel
import com.fxalways.designsystem.components.formatChange
import com.fxalways.designsystem.components.formatRate
import com.fxalways.designsystem.theme.FxTheme
import com.fxalways.observability.Observability

@Composable
fun TravelerScreen(
    liveState: LiveRatesState,
    subscriptionState: SubscriptionState = SubscriptionState(isPremium = false),
    selectedCurrency: String = "JPY",
    budgetBase: Double = 100.0,
    onBack: (() -> Unit)? = null,
    onCurrencySelected: (String) -> Unit = {},
    onBudgetChange: (Double) -> Unit = {},
    onOpenPaywall: () -> Unit = {},
    onOpenPaywallSource: (String) -> Unit = { onOpenPaywall() },
) {
    val access = subscriptionState.featureAccess()
    val travelRates = remember(liveState.baseCurrency, liveState.favorites, liveState.compare, liveState.converter, liveState.allFiat) {
        liveState.portfolioRates().filterNot { it.code == liveState.baseCurrency }
    }
    val destinationLimit = if (access.canUseAdvancedTraveler) travelRates.size else 8
    val visibleDestinations = remember(travelRates, selectedCurrency, destinationLimit) {
        compactCurrencyChoices(travelRates, selectedCurrency, destinationLimit)
    }
    val selectedRate = travelRates.firstOrNull { it.code == selectedCurrency }
        ?: visibleDestinations.firstOrNull()
        ?: FavoriteRates.first()
    val destination = travelerDestination(selectedRate.code)
    var tripDays by remember { mutableStateOf(3) }
    val budgetLocal = budgetBase * selectedRate.rate
    val dailyBudgetLocal = budgetLocal / tripDays.coerceAtLeast(1).toDouble()
    val cashBufferLocal = budgetLocal * destination.cashBufferPct
    val cardSpendLocal = (budgetLocal - cashBufferLocal).coerceAtLeast(0.0)
    val anchorPrice = destination.priceGuide.firstOrNull { item ->
        val label = item.label.lowercase()
        label.contains("meal") || label.contains("lunch") || label.contains("ramen") || label.contains("tacos") || label.contains("pub")
    } ?: destination.priceGuide.firstOrNull()
    val anchorPurchases = anchorPrice?.localAmount?.takeIf { it > 0.0 }?.let { budgetLocal / it } ?: 0.0
    val cheatAmounts = listOf(1, 5, 10, 20, 50, 100, 250, 500).take(access.travelerCheatSheetLimit.cap(8))
    val baseDefinition = liveState.allFiat.firstOrNull { it.code == liveState.baseCurrency }
        ?: SettingsBaseCurrencies.firstOrNull { it.code == liveState.baseCurrency }
    val baseRate = remember(liveState.baseCurrency, baseDefinition) {
        FxRate(
            code = liveState.baseCurrency,
            name = baseDefinition?.name ?: liveState.baseCurrency,
            glyph = baseDefinition?.glyph ?: "◆",
            rate = 1.0,
            change24h = 0.0,
            sparkline = listOf(1f, 1f, 1f),
        )
    }
    var budgetText by remember { mutableStateOf(if (budgetBase > 0.0) formatMoneyValue(budgetBase) else "") }
    var travelerScannedPriceText by remember(selectedRate.code) { mutableStateOf(destination.priceGuide.firstOrNull()?.localAmount?.let(::formatMoneyValue) ?: "25") }
    var travelerPriceHistory by remember(selectedRate.code) { mutableStateOf(emptyList<PriceScannerHistoryEntry>()) }
    var showDestinationPicker by remember { mutableStateOf(false) }
    if (showDestinationPicker) {
        CurrencyPickerSheet(
            title = ui("Choose destination"),
            subtitle = "${travelRates.size} ${ui("live currencies")} · ${liveState.baseCurrency} ${ui("base")}",
            currencies = travelRates,
            selectedCode = selectedRate.code,
            onDismiss = { showDestinationPicker = false },
            onSelect = { code ->
                showDestinationPicker = false
                onCurrencySelected(code)
            },
        )
    }
    ScreenScaffold {
        if (onBack != null) {
            BackNavButton(label = ui("More"), onClick = onBack)
        }
        ScreenHeader(
            ui("Traveler"),
            sub = "${destination.city.uppercase()} · ${selectedRate.code}",
            subtitle = if (liveState.isLive) "${ui("Live")} ${liveState.baseCurrency} ${ui("rates")} · ${compactRuntimeLabel(liveState.updatedLabel)}" else "${ui("Offline snapshot")} · ${liveState.baseCurrency} ${ui("base")}",
        )
        if (liveState.isInitialRateLoading()) {
            LoadingSkeletonCard(
                title = ui("Preparing traveler rates"),
                rows = 5,
                modifier = Modifier.testTag("traveler_loading_skeleton"),
            )
            LoadingSkeletonCard(
                title = ui("Preparing destination tools"),
                rows = 6,
                modifier = Modifier.testTag("traveler_destination_loading_skeleton"),
            )
        } else {
            BentoCard(
                Modifier
                    .fillMaxWidth()
                    .height(156.dp)
                    .testTag("traveler_hero")
                    .clickable { showDestinationPicker = true },
                padding = 14.dp,
            ) {
                GridBg(Modifier.matchParentSize().alpha(0.18f))
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FlagDot(baseDefinition?.glyph ?: "◆", size = 28.dp)
                        Text("1 ${liveState.baseCurrency}", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                        Text("→", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.textFaint)
                        Text(selectedRate.code, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                        FlagDot(destination.flag, size = 28.dp)
                    }
                    BigValueText("${destination.symbol}${formatRate(selectedRate.rate)}")
                    Text("${formatChange(selectedRate.change24h)} ${ui("today")} · ${ui("mid-market")}", style = FxTheme.typography.captionMono, color = if (selectedRate.change24h >= 0) FxTheme.colors.up else FxTheme.colors.down)
                }
            }

            TravelerLocalEtiquetteSection(destination, budgetLocal)

            SectionLabel(ui("DESTINATION"))
            BentoCard(padding = 12.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    visibleDestinations.chunked(4).forEach { rowRates ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowRates.forEach { rate ->
                                val item = travelerDestination(rate.code)
                                Pill(
                                    "${item.flag} ${rate.code}",
                                    variant = if (rate.code == selectedRate.code) PillVariant.Accent else PillVariant.Ghost,
                                    modifier = Modifier
                                        .testTag("traveler_destination_${rate.code}")
                                        .clickable { onCurrencySelected(rate.code) },
                                )
                            }
                        }
                    }
                    SettingChoiceRow(
                        title = if (subscriptionState.isPremium) ui("More destinations") else ui("Selected destination"),
                        subtitle = if (subscriptionState.isPremium) {
                            "${ui("Search")} ${travelRates.size} ${ui("supported live currencies")}"
                        } else {
                            "${ui("Free includes")} ${selectedRate.code}; ${ui("Search supported live currencies")}"
                        },
                        selected = false,
                        actionLabel = ui("more +"),
                        modifier = Modifier.testTag("traveler_more_destinations"),
                        onClick = { showDestinationPicker = true },
                    )
                }
            }

            SectionLabel(ui("TRIP BUDGET"))
            BentoCard(Modifier.testTag("traveler_budget_card"), padding = 12.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Eyebrow("${ui("BUDGET")} · ${liveState.baseCurrency}")
                            BasicTextField(
                                value = budgetText,
                                onValueChange = { raw ->
                                    val next = raw.filter { it.isDigit() || it == '.' || it == ',' }.take(12)
                                    budgetText = next
                                    onBudgetChange(parseAmountInput(next))
                                },
                                singleLine = true,
                                textStyle = FxTheme.typography.numberL.copy(color = FxTheme.colors.text),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.fillMaxWidth().testTag("traveler_budget_input"),
                            )
                        }
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Eyebrow(ui("LOCAL"))
                            Text("${destination.symbol}${formatMoneyValue(budgetLocal)}", style = FxTheme.typography.numberL, color = FxTheme.colors.text)
                        }
                    }
                    Row(
                        Modifier.fillMaxWidth().testTag("traveler_days_control"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(ui("Trip days"), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                            Text(ui("Daily budget = local budget / days"), style = FxTheme.typography.caption, color = FxTheme.colors.textFaint)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Pill("-", modifier = Modifier.testTag("traveler_days_decrease").clickable { tripDays = (tripDays - 1).coerceAtLeast(1) })
                            Pill("$tripDays ${ui("days")}", variant = PillVariant.Accent, modifier = Modifier.testTag("traveler_days_value"))
                            Pill("+", modifier = Modifier.testTag("traveler_days_increase").clickable { tripDays = (tripDays + 1).coerceAtMost(30) })
                        }
                    }
                    KeyValueRow(ui("Local budget"), "${destination.symbol}${formatMoneyValue(budgetLocal)}")
                    KeyValueRow(ui("Daily budget"), "${destination.symbol}${formatMoneyValue(dailyBudgetLocal)} · $tripDays ${ui("days")}")
                    KeyValueRow(ui("Cash buffer"), "${destination.symbol}${formatMoneyValue(cashBufferLocal)} · ${(destination.cashBufferPct * 100).toInt()}% ${ui("of local budget")}")
                }
            }

            SectionLabel(ui("SPEND PLAN"))
            BentoCard(Modifier.testTag("traveler_spend_plan"), padding = 12.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MetricTile(ui("Daily budget"), "${destination.symbol}${formatMoneyValue(dailyBudgetLocal)}", "$tripDays ${ui("days")}", Modifier.weight(1f).height(76.dp))
                        MetricTile(ui("Card spend"), "${destination.symbol}${formatMoneyValue(cardSpendLocal)}", ui("after cash buffer"), Modifier.weight(1f).height(76.dp))
                    }
                    KeyValueRow(ui("Cash buffer"), "${destination.symbol}${formatMoneyValue(cashBufferLocal)} · ${(destination.cashBufferPct * 100).toInt()}%")
                    if (anchorPrice != null) {
                        KeyValueRow(ui("Local meals"), "${formatMoneyValue(anchorPurchases)}x ${ui(anchorPrice.label)} · ${ui("guide estimate")}")
                    }
                    KeyValueRow(ui("Formula"), "${ui("Cash buffer")} = ${ui("Local budget")} x ${(destination.cashBufferPct * 100).toInt()}%")
                }
            }

            SectionLabel(ui("COST TEMPLATES"), right = destination.city)
            TravelerCostTemplatesCard(
                destination = destination,
                dailyBudgetLocal = dailyBudgetLocal,
                isPremium = subscriptionState.isPremium,
                onOpenPaywall = { onOpenPaywallSource("traveler_cost_template_lock") },
            )

            SectionLabel(ui("Scan traveler price"), right = if (subscriptionState.isPremium) ui("OCR beta") else ui("Preview"))
            Box(Modifier.testTag("traveler_price_scanner")) {
                PriceScannerCard(
                    sourceRate = baseRate,
                    targetRate = selectedRate,
                    scannedPriceText = travelerScannedPriceText,
                    localMarketRate = selectedRate.rate,
                    isPremium = subscriptionState.isPremium,
                    onScannedPriceChange = { travelerScannedPriceText = sanitizeAmountInput(it) },
                    onScannedPriceDetected = { amount, detectedCurrency ->
                        travelerScannedPriceText = sanitizeAmountInput(amount)
                        detectedCurrency?.uppercase()?.takeIf { it == selectedRate.code }?.let {
                            Observability.event("traveler_price_scanned", mapOf("currency" to it))
                        }
                        travelerPriceHistory = (listOf(
                            PriceScannerHistoryEntry(
                                amountText = sanitizeAmountInput(amount),
                                targetCode = selectedRate.code,
                                sourceCode = liveState.baseCurrency,
                                liveSourceCost = liveSourceCostFor(parseAmountInput(amount), selectedRate),
                                hiddenCost = 0.0,
                            ),
                        ) + travelerPriceHistory).take(3)
                    },
                    history = travelerPriceHistory,
                    onOpenPaywall = { onOpenPaywallSource("ocr_lock") },
                )
            }

            SectionLabel(ui("OFFLINE PACK"), right = if (liveState.isLive) ui("Live") else ui("CACHED"))
            BentoCard(Modifier.testTag("traveler_offline_pack"), padding = 12.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    KeyValueRow(
                        if (liveState.isLive) ui("Saved snapshot") else ui("Ready from cached rates"),
                        compactRuntimeLabel(liveState.updatedLabel),
                    )
                    KeyValueRow(
                        ui("Rate snapshot"),
                        "1 ${liveState.baseCurrency} = ${formatRate(selectedRate.rate)} ${selectedRate.code}",
                    )
                    KeyValueRow(
                        ui("ATM cash target"),
                        "${destination.symbol}${formatMoneyValue(cashBufferLocal)} · ${(destination.cashBufferPct * 100).toInt()}%",
                    )
                    KeyValueRow(ui("DCC rule"), ui("Decline conversion; pay in local currency."))
                    KeyValueRow(ui("Receipt check"), ui("Compare terminal rate against this mid-market snapshot."))
                }
            }

            SectionLabel(ui("CHEAT SHEET"))
            BentoCard(Modifier.testTag("traveler_cheat_sheet"), padding = 12.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    cheatAmounts.forEach { amount ->
                        Box(Modifier.testTag("traveler_cheat_$amount")) {
                            KeyValueRow("$amount ${liveState.baseCurrency}", "${destination.symbol}${formatMoneyValue(amount * selectedRate.rate)}")
                        }
                    }
                }
            }
            SectionLabel(ui("LOCAL PRICE GUIDE"), right = ui("guide estimate"))
            BentoCard(Modifier.testTag("traveler_price_guide"), padding = 12.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (destination.priceGuide.isEmpty()) {
                        KeyValueRow(ui("No data"), ui("guide estimate"))
                    } else {
                        destination.priceGuide.forEach { item ->
                            val basePrice = item.localAmount / selectedRate.rate
                            KeyValueRow(ui(item.label), "${destination.symbol}${formatMoneyValue(item.localAmount)} · ${liveState.baseCurrency} ${formatMoneyValue(basePrice)}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TravelerLocalEtiquetteSection(destination: TravelerDestination, budgetLocal: Double) {
    SectionLabel(ui("LOCAL ETIQUETTE"), right = "${ui("Source")} · ${destination.city}")
    Row(Modifier.testTag("traveler_local_etiquette"), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        MetricTile(ui("TIPPING"), destination.tipping, ui(destination.tippingNote), Modifier.weight(1f))
        MetricTile(ui("TAX"), ui(destination.tax), ui(destination.taxNote), Modifier.weight(1f))
    }
    BentoCard(Modifier.fillMaxWidth().testTag("traveler_before_pay"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            KeyValueRow(
                ui("TIPPING"),
                destination.tipping,
                ui(destination.tippingNote),
                modifier = Modifier.testTag("traveler_before_pay_tipping"),
            )
            KeyValueRow(
                ui("CARDS ACCEPTED"),
                destination.paymentRails.joinToString(" · "),
                ui(destination.cashNote),
                modifier = Modifier.testTag("traveler_before_pay_cards"),
            )
            KeyValueRow(
                ui("ATM cash target"),
                "${destination.symbol}${formatMoneyValue(budgetLocal * destination.cashBufferPct)}",
                "${(destination.cashBufferPct * 100).toInt()}% ${ui("of local budget")}",
                modifier = Modifier.testTag("traveler_before_pay_cash"),
            )
            KeyValueRow(
                ui("DCC rule"),
                ui("Decline conversion; pay in local currency."),
                ui("Compare terminal rate against this mid-market snapshot."),
                modifier = Modifier.testTag("traveler_before_pay_dcc"),
            )
            destination.priceGuide.firstOrNull()?.let { item ->
                KeyValueRow(
                    ui("LOCAL PRICE GUIDE"),
                    "${ui(item.label)} · ${destination.symbol}${formatMoneyValue(item.localAmount)}",
                    ui("guide estimate"),
                    modifier = Modifier.testTag("traveler_before_pay_price_norm"),
                )
            }
        }
    }
    BentoTile(Modifier.fillMaxWidth().testTag("traveler_payment_rails")) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Eyebrow(ui("CARDS ACCEPTED"))
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    destination.paymentRails.forEach { Pill(it) }
                }
            }
            Text(ui(destination.cashNote), style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
        }
    }
}
