package com.fxalways.app.screens.dashboard

import androidx.compose.runtime.Composable
import com.fxalways.app.data.LiveRatesState
import com.fxalways.app.screens.detail.compactRuntimeLabel
import com.fxalways.app.screens.detail.isInitialRateLoading
import com.fxalways.app.screens.localizedRuntimeLabel
import com.fxalways.app.screens.ui
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.components.formatRate
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * What the Home screen says about how fresh the FX numbers are.
 *
 * FX rates come from a daily reference (ECB via Frankfurter), so the UI must never
 * imply intraday ticks. The badge states the nature of the data; the detail states
 * the reference date and when this device last synced it.
 */
internal data class RateFreshness(
    val badge: String,
    val detail: String,
    val isStale: Boolean,
)

@Composable
internal fun rateFreshness(liveState: LiveRatesState): RateFreshness {
    val loading = liveState.isInitialRateLoading()
    val rateDate = liveState.rateDate?.let { formatRateDate(it) }
    return when {
        loading -> RateFreshness(
            badge = ui("Loading"),
            detail = ui("fetching today's reference"),
            isStale = false,
        )
        liveState.isOfflineCache -> RateFreshness(
            badge = ui("OFFLINE"),
            detail = listOfNotNull(
                rateDate?.let { "${ui("last rate")} $it" },
                liveState.cachedAtMillis?.let { "${ui("saved")} ${localizedAge(it)}" },
            ).joinToString(" · ").ifBlank { localizedRuntimeLabel(liveState.updatedLabel) },
            isStale = true,
        )
        liveState.isLive -> RateFreshness(
            badge = ui("DAILY REFERENCE"),
            detail = listOfNotNull(
                rateDate?.let { "${ui("Rate of")} $it" },
                liveState.refreshedAtMillis?.let { "${ui("synced")} ${formatClock(it)}" },
            ).joinToString(" · ").ifBlank { compactRuntimeLabel(liveState.updatedLabel) },
            isStale = false,
        )
        else -> RateFreshness(
            badge = ui("Preview"),
            detail = ui("sample data until the first sync"),
            isStale = true,
        )
    }
}

/** "0.8499 — 0.8549" over the sparkline window; the label next to it must say "30D". */
internal fun sparklineRangeLabel(rate: FxRate): String {
    val low = rate.sparkline.minOrNull()?.toDouble() ?: rate.rate
    val high = rate.sparkline.maxOrNull()?.toDouble() ?: rate.rate
    return "${formatRate(low)} — ${formatRate(high)}"
}

private val monthKeys = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

/** "2026-09-03" → "3 Sep" (month localised through ui()). Unknown formats pass through untouched. */
@Composable
internal fun formatRateDate(isoDate: String): String {
    val date = runCatching { LocalDate.parse(isoDate) }.getOrNull() ?: return isoDate
    return "${date.dayOfMonth} ${ui(monthKeys[date.monthNumber - 1])}"
}

internal fun formatClock(epochMillis: Long): String {
    val local = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(TimeZone.currentSystemDefault())
    return "${local.hour.toString().padStart(2, '0')}:${local.minute.toString().padStart(2, '0')}"
}

@Composable
internal fun localizedAge(epochMillis: Long): String {
    val minutes = ((Clock.System.now().toEpochMilliseconds() - epochMillis).coerceAtLeast(0)) / 60_000
    return when {
        minutes < 1 -> ui("just now")
        minutes < 60 -> "${minutes}m ${ui("ago")}"
        minutes < 1_440 -> "${minutes / 60}h ${ui("ago")}"
        else -> "${minutes / 1_440}d ${ui("ago")}"
    }
}
