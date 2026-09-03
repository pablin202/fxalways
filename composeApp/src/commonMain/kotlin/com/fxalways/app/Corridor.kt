package com.fxalways.app

/** How often the user moves money on their corridor (onboarding step 2, issue #10). */
enum class SendCadence {
    Once,
    Monthly,
    WhenItPays,
}

/** The pair, typical amount and cadence a user told us about during onboarding. */
data class Corridor(
    val base: String,
    val target: String,
    val amount: Double,
    val cadence: SendCadence,
) {
    fun encode(): String = "$base,$target,$amount,${cadence.name}"

    companion object {
        val AMOUNT_ANCHORS = listOf(200.0, 500.0, 1_000.0, 2_000.0)

        fun decode(raw: String?): Corridor? {
            val parts = raw.orEmpty().split(",").map { it.trim() }
            if (parts.size != 4) return null
            val amount = parts[2].toDoubleOrNull() ?: return null
            val cadence = SendCadence.entries.firstOrNull { it.name == parts[3] } ?: return null
            if (parts[0].length != 3 || parts[1].length != 3) return null
            return Corridor(parts[0].uppercase(), parts[1].uppercase(), amount, cadence)
        }
    }
}

/** Destinations people mostly visit rather than send money to. */
internal val TouristDestinations = setOf("JPY", "EUR", "GBP", "THB", "USD", "CHF", "AUD", "NZD", "CAD", "SGD", "HKD", "KRW", "IDR", "VND", "TRY", "EGP", "MAD", "AED")

/**
 * Infers the [UserProfile] from the corridor instead of asking (issue #10):
 * - monthly or "when it pays" → Remittances
 * - base USD/EUR and target is the device's local currency → Freelancer (getting paid from abroad)
 * - one-off to a tourist destination → Traveler
 * - one-off elsewhere → Remittances
 */
fun inferProfile(corridor: Corridor, localCurrency: String): UserProfile = when {
    corridor.base in setOf("USD", "EUR") && corridor.target == localCurrency && corridor.base != localCurrency -> UserProfile.Freelancer
    corridor.cadence == SendCadence.Monthly || corridor.cadence == SendCadence.WhenItPays -> UserProfile.Remittances
    corridor.target in TouristDestinations -> UserProfile.Traveler
    else -> UserProfile.Remittances
}

/** Suggested destination chips per base currency, most common corridors first. */
fun suggestedTargets(base: String): List<String> {
    val byBase = when (base) {
        "AUD" -> listOf("ARS", "COP", "PEN", "BRL", "MXN", "PHP", "INR", "USD")
        "USD" -> listOf("MXN", "COP", "GTQ", "PHP", "INR", "DOP", "BRL", "EUR")
        "EUR" -> listOf("MAD", "COP", "TRY", "ARS", "BRL", "USD", "GBP", "INR")
        "GBP" -> listOf("INR", "PKR", "NGN", "PHP", "EUR", "USD", "BRL", "MXN")
        "CAD" -> listOf("INR", "PHP", "MXN", "COP", "USD", "BRL", "EUR", "PEN")
        "NZD" -> listOf("PHP", "INR", "AUD", "USD", "EUR", "GBP", "MXN", "BRL")
        "CHF" -> listOf("EUR", "USD", "BRL", "COP", "INR", "GBP", "MXN", "TRY")
        else -> listOf("USD", "EUR", "MXN", "BRL", "INR", "PHP", "GBP", "COP")
    }
    return byBase.filter { it != base }
}
