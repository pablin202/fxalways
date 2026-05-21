package com.fxalways.app.screens.traveler

import com.fxalways.app.screens.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.KeyValueRow
import com.fxalways.designsystem.theme.FxTheme

internal data class TravelerDestination(
    val code: String,
    val city: String,
    val flag: String,
    val symbol: String,
    val tipping: String,
    val tippingNote: String,
    val tax: String,
    val taxNote: String,
    val cashNote: String,
    val cashBufferPct: Double,
    val paymentRails: List<String>,
    val priceGuide: List<TravelerPriceGuide>,
)

internal data class TravelerPriceGuide(
    val label: String,
    val localAmount: Double,
)

@Composable
internal fun TravelerCostTemplatesCard(
    destination: TravelerDestination,
    dailyBudgetLocal: Double,
    isPremium: Boolean,
    onOpenPaywall: () -> Unit,
) {
    val templates = remember(destination.code, dailyBudgetLocal) {
        travelerCostTemplates(destination, dailyBudgetLocal)
    }
    BentoCard(Modifier.testTag("traveler_cost_templates"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            templates.take(if (isPremium) templates.size else 2).forEachIndexed { index, template ->
                KeyValueRow(
                    ui(template.label),
                    "${destination.symbol}${formatMoneyValue(template.dailyTotal)}",
                    "${ui("daily template")} · ${template.detail}",
                    modifier = Modifier.testTag("traveler_cost_template_$index"),
                )
            }
            if (!isPremium) {
                GhostButton(
                    text = ui("Pro unlocks premium city templates."),
                    modifier = Modifier.fillMaxWidth().testTag("traveler_cost_template_upsell"),
                    onClick = onOpenPaywall,
                )
            }
        }
    }
}

private data class TravelerCostTemplate(
    val label: String,
    val dailyTotal: Double,
    val detail: String,
)

private fun travelerCostTemplates(
    destination: TravelerDestination,
    dailyBudgetLocal: Double,
): List<TravelerCostTemplate> {
    val meal = destination.priceGuide.firstOrNull { item ->
        item.label.contains("meal", ignoreCase = true) ||
            item.label.contains("ramen", ignoreCase = true) ||
            item.label.contains("tacos", ignoreCase = true)
    } ?: destination.priceGuide.firstOrNull()
    val transit = destination.priceGuide.firstOrNull { item ->
        item.label.contains("metro", ignoreCase = true) ||
            item.label.contains("transit", ignoreCase = true) ||
            item.label.contains("tube", ignoreCase = true)
    }
    val coffee = destination.priceGuide.firstOrNull { it.label.contains("coffee", ignoreCase = true) }
    val mealAmount = meal?.localAmount ?: dailyBudgetLocal * 0.35
    val transitAmount = transit?.localAmount ?: dailyBudgetLocal * 0.10
    val coffeeAmount = coffee?.localAmount ?: dailyBudgetLocal * 0.05
    return listOf(
        TravelerCostTemplate("Backpacker", mealAmount + transitAmount + coffeeAmount, "${meal?.label ?: "Meal"} + ${transit?.label ?: "Transit"}"),
        TravelerCostTemplate("Comfort", mealAmount * 2.0 + transitAmount * 2.0 + coffeeAmount, "${meal?.label ?: "Meal"} x2 + ${transit?.label ?: "Transit"}"),
        TravelerCostTemplate("Business", mealAmount * 3.0 + transitAmount * 2.0 + coffeeAmount * 2.0, "${meal?.label ?: "Meal"} x3 + ${taxiTemplateLabel(destination)}"),
    )
}

private fun taxiTemplateLabel(destination: TravelerDestination): String =
    destination.priceGuide.firstOrNull { it.label.contains("taxi", ignoreCase = true) }?.label ?: "Taxi"

internal fun travelerDestination(code: String): TravelerDestination =
    travelerDestinations[code] ?: TravelerDestination(
        code = code,
        city = code,
        flag = "◆",
        symbol = "$code ",
        tipping = "Check",
        tippingNote = "varies by city",
        tax = "Varies",
        taxNote = "verify locally",
        cashNote = "mixed payments",
        cashBufferPct = 0.20,
        paymentRails = listOf("Visa", "Mastercard"),
        priceGuide = listOf(
            TravelerPriceGuide("Coffee", 4.0),
            TravelerPriceGuide("Casual meal", 18.0),
            TravelerPriceGuide("Taxi start", 8.0),
        ),
    )

private val travelerDestinations = mapOf(
    "JPY" to TravelerDestination(
        code = "JPY",
        city = "Tokyo",
        flag = "🇯🇵",
        symbol = "¥",
        tipping = "0%",
        tippingNote = "not customary",
        tax = "10%",
        taxNote = "often included",
        cashNote = "cash useful",
        cashBufferPct = 0.25,
        paymentRails = listOf("Visa", "Mastercard", "Suica"),
        priceGuide = listOf(
            TravelerPriceGuide("Coffee", 450.0),
            TravelerPriceGuide("Ramen", 1_100.0),
            TravelerPriceGuide("Metro ride", 220.0),
            TravelerPriceGuide("Taxi start", 500.0),
        ),
    ),
    "EUR" to TravelerDestination(
        code = "EUR",
        city = "Eurozone",
        flag = "🇪🇺",
        symbol = "€",
        tipping = "5-10%",
        tippingNote = "service dependent",
        tax = "Included",
        taxNote = "VAT in price",
        cashNote = "cards common",
        cashBufferPct = 0.15,
        paymentRails = listOf("Visa", "Mastercard", "SEPA"),
        priceGuide = listOf(
            TravelerPriceGuide("Coffee", 3.5),
            TravelerPriceGuide("Casual meal", 18.0),
            TravelerPriceGuide("Transit ticket", 2.5),
            TravelerPriceGuide("Taxi start", 5.0),
        ),
    ),
    "GBP" to TravelerDestination(
        code = "GBP",
        city = "London",
        flag = "🇬🇧",
        symbol = "£",
        tipping = "10-12.5%",
        tippingNote = "often optional",
        tax = "Included",
        taxNote = "VAT in price",
        cashNote = "contactless first",
        cashBufferPct = 0.10,
        paymentRails = listOf("Visa", "Mastercard", "Oyster"),
        priceGuide = listOf(
            TravelerPriceGuide("Coffee", 3.8),
            TravelerPriceGuide("Pub meal", 18.0),
            TravelerPriceGuide("Tube ride", 2.8),
            TravelerPriceGuide("Taxi start", 4.2),
        ),
    ),
    "MXN" to TravelerDestination(
        code = "MXN",
        city = "Mexico City",
        flag = "🇲🇽",
        symbol = "$",
        tipping = "10-15%",
        tippingNote = "restaurants",
        tax = "16%",
        taxNote = "usually included",
        cashNote = "carry cash",
        cashBufferPct = 0.30,
        paymentRails = listOf("Visa", "Mastercard", "Cash"),
        priceGuide = listOf(
            TravelerPriceGuide("Coffee", 55.0),
            TravelerPriceGuide("Tacos", 120.0),
            TravelerPriceGuide("Metro ride", 5.0),
            TravelerPriceGuide("Taxi start", 50.0),
        ),
    ),
    "BRL" to TravelerDestination(
        code = "BRL",
        city = "Sao Paulo",
        flag = "🇧🇷",
        symbol = "R$",
        tipping = "10%",
        tippingNote = "often service charge",
        tax = "Included",
        taxNote = "varies by item",
        cashNote = "cards common",
        cashBufferPct = 0.20,
        paymentRails = listOf("Visa", "Mastercard", "Pix"),
        priceGuide = listOf(
            TravelerPriceGuide("Coffee", 9.0),
            TravelerPriceGuide("Lunch", 45.0),
            TravelerPriceGuide("Metro ride", 5.0),
            TravelerPriceGuide("Taxi start", 6.0),
        ),
    ),
    "AUD" to TravelerDestination(
        code = "AUD",
        city = "Sydney",
        flag = "🇦🇺",
        symbol = "A$",
        tipping = "0-10%",
        tippingNote = "optional",
        tax = "10%",
        taxNote = "GST included",
        cashNote = "cards common",
        cashBufferPct = 0.10,
        paymentRails = listOf("Visa", "Mastercard", "Opal"),
        priceGuide = listOf(
            TravelerPriceGuide("Coffee", 5.0),
            TravelerPriceGuide("Casual meal", 24.0),
            TravelerPriceGuide("Transit ride", 4.5),
            TravelerPriceGuide("Taxi start", 6.5),
        ),
    ),
    "CAD" to TravelerDestination(
        code = "CAD",
        city = "Toronto",
        flag = "🇨🇦",
        symbol = "C$",
        tipping = "15-20%",
        tippingNote = "restaurants",
        tax = "+ tax",
        taxNote = "often added",
        cashNote = "cards common",
        cashBufferPct = 0.10,
        paymentRails = listOf("Visa", "Mastercard", "Interac"),
        priceGuide = listOf(
            TravelerPriceGuide("Coffee", 4.5),
            TravelerPriceGuide("Casual meal", 22.0),
            TravelerPriceGuide("Transit fare", 3.4),
            TravelerPriceGuide("Taxi start", 4.5),
        ),
    ),
    "CHF" to TravelerDestination(
        code = "CHF",
        city = "Zurich",
        flag = "🇨🇭",
        symbol = "Fr ",
        tipping = "0-10%",
        tippingNote = "round up",
        tax = "Included",
        taxNote = "VAT in price",
        cashNote = "cards common",
        cashBufferPct = 0.10,
        paymentRails = listOf("Visa", "Mastercard", "Twint"),
        priceGuide = listOf(
            TravelerPriceGuide("Coffee", 5.0),
            TravelerPriceGuide("Casual meal", 28.0),
            TravelerPriceGuide("Transit ticket", 4.4),
            TravelerPriceGuide("Taxi start", 8.0),
        ),
    ),
)
