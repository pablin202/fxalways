package com.fxalways.app.screens.onboarding

import com.fxalways.app.screens.*
import com.fxalways.app.screens.profile.copy
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fxalways.app.Corridor
import com.fxalways.app.DeviceLocale
import com.fxalways.app.NotificationPermissionStatus
import com.fxalways.app.SendCadence
import com.fxalways.app.UserProfile
import com.fxalways.app.inferProfile
import com.fxalways.app.suggestedTargets
import com.fxalways.designsystem.components.Eyebrow
import com.fxalways.designsystem.components.GridBg
import com.fxalways.designsystem.components.Pill
import com.fxalways.designsystem.components.PillVariant
import com.fxalways.designsystem.theme.FxTheme
import kotlin.math.roundToInt

/** What onboarding learned about the user (issue #10). */
data class OnboardingResult(
    val corridor: Corridor,
    val profile: UserProfile,
    val notificationsRequested: Boolean,
)

internal data class CorridorCurrency(val code: String, val name: String, val flag: String)

/** Currencies offered in the onboarding picker; the backend serves rates for all of them. */
internal val CorridorCurrencies: List<CorridorCurrency> = listOf(
    CorridorCurrency("USD", "US Dollar", "🇺🇸"), CorridorCurrency("EUR", "Euro", "🇪🇺"), CorridorCurrency("GBP", "British Pound", "🇬🇧"),
    CorridorCurrency("AUD", "Australian Dollar", "🇦🇺"), CorridorCurrency("CAD", "Canadian Dollar", "🇨🇦"), CorridorCurrency("NZD", "New Zealand Dollar", "🇳🇿"),
    CorridorCurrency("CHF", "Swiss Franc", "🇨🇭"), CorridorCurrency("JPY", "Japanese Yen", "🇯🇵"), CorridorCurrency("MXN", "Mexican Peso", "🇲🇽"),
    CorridorCurrency("ARS", "Argentine Peso", "🇦🇷"), CorridorCurrency("COP", "Colombian Peso", "🇨🇴"), CorridorCurrency("PEN", "Peruvian Sol", "🇵🇪"),
    CorridorCurrency("CLP", "Chilean Peso", "🇨🇱"), CorridorCurrency("BRL", "Brazilian Real", "🇧🇷"), CorridorCurrency("UYU", "Uruguayan Peso", "🇺🇾"),
    CorridorCurrency("PYG", "Paraguayan Guarani", "🇵🇾"), CorridorCurrency("BOB", "Bolivian Boliviano", "🇧🇴"), CorridorCurrency("DOP", "Dominican Peso", "🇩🇴"),
    CorridorCurrency("GTQ", "Guatemalan Quetzal", "🇬🇹"), CorridorCurrency("HNL", "Honduran Lempira", "🇭🇳"), CorridorCurrency("NIO", "Nicaraguan Córdoba", "🇳🇮"),
    CorridorCurrency("CRC", "Costa Rican Colón", "🇨🇷"), CorridorCurrency("VES", "Venezuelan Bolívar", "🇻🇪"), CorridorCurrency("PHP", "Philippine Peso", "🇵🇭"),
    CorridorCurrency("INR", "Indian Rupee", "🇮🇳"), CorridorCurrency("PKR", "Pakistani Rupee", "🇵🇰"), CorridorCurrency("BDT", "Bangladeshi Taka", "🇧🇩"),
    CorridorCurrency("LKR", "Sri Lankan Rupee", "🇱🇰"), CorridorCurrency("NPR", "Nepalese Rupee", "🇳🇵"), CorridorCurrency("VND", "Vietnamese Dong", "🇻🇳"),
    CorridorCurrency("THB", "Thai Baht", "🇹🇭"), CorridorCurrency("IDR", "Indonesian Rupiah", "🇮🇩"), CorridorCurrency("MYR", "Malaysian Ringgit", "🇲🇾"),
    CorridorCurrency("SGD", "Singapore Dollar", "🇸🇬"), CorridorCurrency("HKD", "Hong Kong Dollar", "🇭🇰"), CorridorCurrency("CNY", "Chinese Yuan", "🇨🇳"),
    CorridorCurrency("KRW", "South Korean Won", "🇰🇷"), CorridorCurrency("NGN", "Nigerian Naira", "🇳🇬"), CorridorCurrency("KES", "Kenyan Shilling", "🇰🇪"),
    CorridorCurrency("GHS", "Ghanaian Cedi", "🇬🇭"), CorridorCurrency("ZAR", "South African Rand", "🇿🇦"), CorridorCurrency("EGP", "Egyptian Pound", "🇪🇬"),
    CorridorCurrency("MAD", "Moroccan Dirham", "🇲🇦"), CorridorCurrency("TRY", "Turkish Lira", "🇹🇷"), CorridorCurrency("AED", "UAE Dirham", "🇦🇪"),
    CorridorCurrency("SAR", "Saudi Riyal", "🇸🇦"), CorridorCurrency("PLN", "Polish Zloty", "🇵🇱"), CorridorCurrency("RON", "Romanian Leu", "🇷🇴"),
    CorridorCurrency("UAH", "Ukrainian Hryvnia", "🇺🇦"), CorridorCurrency("SEK", "Swedish Krona", "🇸🇪"), CorridorCurrency("NOK", "Norwegian Krone", "🇳🇴"),
    CorridorCurrency("DKK", "Danish Krone", "🇩🇰"), CorridorCurrency("CZK", "Czech Koruna", "🇨🇿"), CorridorCurrency("HUF", "Hungarian Forint", "🇭🇺"),
)

private val BaseCurrencyChoices = listOf("USD", "EUR", "GBP", "AUD", "CAD", "NZD", "CHF")

internal fun corridorCurrency(code: String): CorridorCurrency =
    CorridorCurrencies.firstOrNull { it.code == code } ?: CorridorCurrency(code, code, "◆")

internal fun filterCorridorCurrencies(query: String, exclude: String): List<CorridorCurrency> {
    val q = query.trim().lowercase()
    if (q.isEmpty()) return emptyList()
    return CorridorCurrencies
        .filter { it.code != exclude }
        .filter { it.code.lowercase().startsWith(q) || it.name.lowercase().contains(q) }
        .sortedBy { if (it.code.lowercase().startsWith(q)) 0 else 1 }
        .take(8)
}

@Composable
fun OnboardingScreen(
    onComplete: (OnboardingResult) -> Unit = {},
    localCurrency: String = DeviceLocale.currencyCode,
    onRequestNotifications: () -> Unit = { NotificationPermissionStatus.requestIfNeeded() },
) {
    val defaultBase = remember(localCurrency) { if (CorridorCurrencies.any { it.code == localCurrency }) localCurrency else "USD" }
    var step by remember { mutableStateOf(0) }
    var base by remember { mutableStateOf(defaultBase) }
    var target by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf(500.0) }
    var cadence by remember { mutableStateOf(SendCadence.Once) }

    fun corridor() = Corridor(base, target ?: suggestedTargets(base).first(), amount, cadence)
    fun finish(notifications: Boolean) {
        val corridor = corridor()
        onComplete(OnboardingResult(corridor, inferProfile(corridor, localCurrency), notifications))
    }

    BoxWithConstraints(Modifier.fillMaxSize().background(FxTheme.colors.bg)) {
        val compactHeight = maxHeight < 720.dp
        GridBg(Modifier.matchParentSize().alpha(0.10f), radialMask = false)
        GridBg(Modifier.matchParentSize().alpha(0.30f))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding()
                .padding(horizontal = 18.dp, vertical = if (compactHeight) 10.dp else 18.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("FX/", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Pill("${step + 1}/3", variant = PillVariant.Ghost)
                    Text(
                        ui("Skip"),
                        style = FxTheme.typography.caption,
                        color = FxTheme.colors.textDim,
                        modifier = Modifier
                            .testTag("onboarding_skip")
                            .clip(FxTheme.shapes.field)
                            .clickable { finish(notifications = false) }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(if (compactHeight) 8.dp else 12.dp),
            ) {
                Spacer(Modifier.height(if (compactHeight) 6.dp else 18.dp))
                when (step) {
                    0 -> CorridorStep(
                        base = base,
                        target = target,
                        query = query,
                        localCurrency = defaultBase,
                        onBaseSelected = { base = it; if (target == it) target = null },
                        onTargetSelected = { target = it; query = "" },
                        onQueryChange = { query = it },
                    )
                    1 -> AmountStep(
                        base = base,
                        target = target ?: suggestedTargets(base).first(),
                        amount = amount,
                        cadence = cadence,
                        profile = inferProfile(corridor(), localCurrency),
                        onAmountChange = { amount = it },
                        onCadenceChange = { cadence = it },
                    )
                    else -> AlertsStep(
                        corridor = corridor(),
                        onEnable = { onRequestNotifications(); finish(notifications = true) },
                        onNotNow = { finish(notifications = false) },
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    (0..2).forEach { dot ->
                        val width by animateDpAsState(
                            targetValue = if (dot == step) 22.dp else 6.dp,
                            animationSpec = tween(durationMillis = 200),
                            label = "onboarding-dot",
                        )
                        Box(
                            Modifier.size(width = width, height = 6.dp).background(
                                color = if (dot == step) FxTheme.colors.accent else FxTheme.colors.textGhost,
                                shape = CircleShape,
                            ),
                        )
                    }
                }
                if (step < 2) {
                    PrimaryButton(
                        text = ui("Next  →"),
                        enabled = step != 0 || target != null,
                        modifier = Modifier.width(126.dp).testTag("onboarding_next"),
                    ) { step += 1 }
                } else {
                    Text(
                        ui("← Back"),
                        style = FxTheme.typography.caption,
                        color = FxTheme.colors.textDim,
                        modifier = Modifier.testTag("onboarding_back").clip(FxTheme.shapes.field).clickable { step = 1 }.padding(horizontal = 10.dp, vertical = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun CorridorStep(
    base: String,
    target: String?,
    query: String,
    localCurrency: String,
    onBaseSelected: (String) -> Unit,
    onTargetSelected: (String) -> Unit,
    onQueryChange: (String) -> Unit,
) {
    Eyebrow(ui("STEP 01 · YOUR CORRIDOR"), color = FxTheme.colors.accent)
    Text(ui("Which currencies\ndo you move money between?"), style = FxTheme.typography.titleXL, color = FxTheme.colors.text)
    Text(ui("From"), style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
    val bases = remember(localCurrency) { (listOf(localCurrency) + BaseCurrencyChoices).distinct() }
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        bases.forEach { code -> CurrencyChip(corridorCurrency(code), selected = code == base, tag = "onboarding_base_$code") { onBaseSelected(code) } }
    }
    Text(ui("To"), style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        textStyle = FxTheme.typography.body.copy(color = FxTheme.colors.text),
        modifier = Modifier.testTag("onboarding_target_search"),
        decorationBox = { inner ->
            Box(
                Modifier.fillMaxWidth().clip(FxTheme.shapes.field).background(FxTheme.colors.surface2)
                    .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field).padding(horizontal = 12.dp, vertical = 11.dp),
            ) {
                if (query.isBlank()) Text(ui("Search a currency or country"), style = FxTheme.typography.caption, color = FxTheme.colors.textGhost)
                inner()
            }
        },
    )
    val options = remember(query, base) {
        if (query.isBlank()) suggestedTargets(base).map(::corridorCurrency) else filterCorridorCurrencies(query, exclude = base)
    }
    if (query.isBlank()) Eyebrow(ui("POPULAR CORRIDORS"), color = FxTheme.colors.textFaint)
    ChipGrid(options, selected = target, tagPrefix = "onboarding_target_", onSelect = onTargetSelected)
    Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(
            if (target == null) ui("Pick where the money goes") else ui("We'll show what arrives after fees"),
            style = FxTheme.typography.caption,
            color = FxTheme.colors.textDim,
        )
        Pill("$base → ${target ?: "?"}", variant = if (target == null) PillVariant.Ghost else PillVariant.Accent, modifier = Modifier.testTag("onboarding_pair_pill"))
    }
}

@Composable
private fun ChipGrid(options: List<CorridorCurrency>, selected: String?, tagPrefix: String, onSelect: (String) -> Unit) {
    options.chunked(2).forEach { row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            row.forEach { option ->
                CurrencyChip(option, selected = option.code == selected, tag = "$tagPrefix${option.code}", modifier = Modifier.weight(1f), showName = true) { onSelect(option.code) }
            }
            if (row.size == 1) Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun CurrencyChip(
    currency: CorridorCurrency,
    selected: Boolean,
    tag: String,
    modifier: Modifier = Modifier,
    showName: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .testTag(tag)
            .clip(FxTheme.shapes.field)
            .background(if (selected) FxTheme.colors.accentSoft else FxTheme.colors.surface2)
            .border(1.dp, if (selected) FxTheme.colors.accentLine else FxTheme.colors.border, FxTheme.shapes.field)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(currency.flag, style = FxTheme.typography.caption)
        Text(currency.code, style = FxTheme.typography.captionMono, color = if (selected) FxTheme.colors.accent else FxTheme.colors.text)
        if (showName) Text(ui(currency.name), style = FxTheme.typography.caption, color = FxTheme.colors.textDim, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun AmountStep(
    base: String,
    target: String,
    amount: Double,
    cadence: SendCadence,
    profile: UserProfile,
    onAmountChange: (Double) -> Unit,
    onCadenceChange: (SendCadence) -> Unit,
) {
    Eyebrow(ui("STEP 02 · AMOUNT & CADENCE"), color = FxTheme.colors.accent)
    Text(ui("How much,\nhow often?"), style = FxTheme.typography.titleXL, color = FxTheme.colors.text)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(ui("You send"), style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
        Text("$base ${formatInputAmount(amount).ifBlank { "0" }}", style = FxTheme.typography.titleL, color = FxTheme.colors.text, modifier = Modifier.testTag("onboarding_amount_value"))
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Corridor.AMOUNT_ANCHORS.forEach { anchor ->
            val label = anchor.roundToInt().toString()
            Pill(
                label,
                variant = if (amount == anchor) PillVariant.Accent else PillVariant.Ghost,
                modifier = Modifier.weight(1f).testTag("onboarding_amount_$label").clickable { onAmountChange(anchor) },
            )
        }
    }
    Slider(
        value = amount.toFloat(),
        onValueChange = { onAmountChange((it / 50f).roundToInt() * 50.0) },
        valueRange = 50f..5_000f,
        colors = SliderDefaults.colors(thumbColor = FxTheme.colors.accent, activeTrackColor = FxTheme.colors.accent, inactiveTrackColor = FxTheme.colors.surface3),
        modifier = Modifier.fillMaxWidth().testTag("onboarding_amount_slider"),
    )
    Text(ui("How often"), style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SendCadence.entries.forEach { option ->
            Pill(
                ui(option.label()),
                variant = if (cadence == option) PillVariant.Accent else PillVariant.Ghost,
                modifier = Modifier.weight(1f).testTag("onboarding_cadence_${option.name}").clickable { onCadenceChange(option) },
            )
        }
    }
    Box(
        Modifier.fillMaxWidth().clip(FxTheme.shapes.field).background(FxTheme.colors.surface2).border(1.dp, FxTheme.colors.border, FxTheme.shapes.field).padding(12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(ui("WE'LL SET YOU UP AS"), color = FxTheme.colors.textFaint)
                Pill(ui(profile.copy().label), variant = PillVariant.Accent, modifier = Modifier.testTag("onboarding_profile_pill"))
            }
            Text(ui(profile.onboardingPromise()), style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
            Text("$base → $target · ${ui("you can change this anytime in Settings")}", style = FxTheme.typography.caption, color = FxTheme.colors.textGhost)
        }
    }
}

@Composable
private fun AlertsStep(corridor: Corridor, onEnable: () -> Unit, onNotNow: () -> Unit) {
    Eyebrow(ui("STEP 03 · ALERTS"), color = FxTheme.colors.accent)
    Text(ui("We'll tell you when\nthe rate is on your side."), style = FxTheme.typography.titleXL, color = FxTheme.colors.text)
    Text(
        ui("One notification when your corridor hits a good moment or a cheaper provider shows up. No daily noise."),
        style = FxTheme.typography.body,
        color = FxTheme.colors.textDim,
    )
    Box(Modifier.fillMaxWidth().clip(FxTheme.shapes.field).background(FxTheme.colors.surface2).border(1.dp, FxTheme.colors.border, FxTheme.shapes.field).padding(12.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Eyebrow(ui("SUGGESTED ALERT"), color = FxTheme.colors.textFaint)
            Text("${corridor.base} → ${corridor.target} · ${ui("rate above the last 7-day average")}", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text(ui("Created for you after the first rate refresh."), style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
        }
    }
    Spacer(Modifier.height(4.dp))
    PrimaryButton(text = ui("Enable alerts"), modifier = Modifier.fillMaxWidth().testTag("onboarding_enable_alerts"), onClick = onEnable)
    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            ui("Not now"),
            style = FxTheme.typography.caption,
            color = FxTheme.colors.textDim,
            modifier = Modifier.testTag("onboarding_not_now").clip(FxTheme.shapes.field).clickable(onClick = onNotNow).padding(horizontal = 14.dp, vertical = 10.dp),
        )
    }
}

internal fun SendCadence.label(): String = when (this) {
    SendCadence.Once -> "One time"
    SendCadence.Monthly -> "Every month"
    SendCadence.WhenItPays -> "When it pays off"
}

private fun UserProfile.onboardingPromise(): String = when (this) {
    UserProfile.Remittances -> "Home shows what arrives today, the cheapest provider and whether to send now or wait."
    UserProfile.Traveler -> "Trip budget, local prices and the card-or-cash call for your destination."
    UserProfile.Freelancer -> "What your invoice is worth today and the best moment to cash out."
    UserProfile.CryptoHolder -> "Crypto board, stablecoins and holdings up front."
    UserProfile.Savings -> "Watch your savings currencies and get drift alerts."
}
