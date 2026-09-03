package com.fxalways.app.screens.settings

/** Play Store listing copy around the remittance wedge (issue #14). Source of truth for Play Console and docs/STORE_LISTING.md. */
internal data class StoreListing(
    val locale: String,
    val title: String,
    val shortDescription: String,
    val longDescription: String,
    val keywords: String,
) {
    init {
        require(title.length <= 30) { "Play title max 30 chars: ${title.length}" }
        require(shortDescription.length <= 80) { "Play short description max 80 chars: ${shortDescription.length}" }
    }
}

internal fun storeListingFor(language: String): StoreListing = when (language.lowercase().substringBefore("-")) {
    "es" -> StoreListingES
    "pt" -> StoreListingPT
    else -> StoreListingEN
}

internal val StoreListingEN = StoreListing(
    locale = "en-US",
    title = "FX Always: Send Money Smarter",
    shortDescription = "Compare real transfer costs, get exchange rate alerts, send when it pays off.",
    longDescription = """
FX Always shows what your family actually receives after fees and spread with Wise, Revolut, Western Union and local wallets, and tells you when the exchange rate is on your side. Exchange rate alerts, transfer cost comparison and a daily decision for your corridor.

TODAY'S DECISION
Open the app and see how much arrives today for your usual amount, which provider delivers the most and whether to send now or wait. One screen, no spreadsheet.

REAL TRANSFER COST
Fees, spread and delivery time side by side for AUD→ARS, AUD→COP, AUD→PEN, USD→MXN, GBP→INR and 150+ more pairs. The mid-market rate is the honest baseline; every provider is measured against it.

EXCHANGE RATE ALERTS
Set a target or let FX Always suggest one. Get a notification when your corridor hits a good moment or a cheaper provider shows up. No daily noise.

CURRENCY CONVERTER AND DAILY REFERENCE
ECB reference rates updated every business day, 165 currencies, crypto, 30-day ranges and history. Every number shows its date and source.

TRAVEL MONEY
Local price check, card-or-cash call, tipping guide and offline rates for your destination.

FREE AND PRO
Free: unlimited currencies, 2 alerts, 3 providers per corridor, 1 year of history. Pro: unlimited alerts, every provider and who was cheapest over time, OCR price scanner, portfolio P&L, 5 years of history.

FX Always does not move money. Rates are indicative and may differ from provider, card or cash exchange rates.
""".trimIndent(),
    keywords = "send money, money transfer, remittance, exchange rate alerts, currency converter, transfer fees, travel money",
)

internal val StoreListingES = StoreListing(
    locale = "es-419",
    title = "FX Always: Enviar dinero mejor",
    shortDescription = "Compará el costo real de enviar, alertas de cambio y enviá cuando conviene.",
    longDescription = """
FX Always te muestra cuánto recibe de verdad tu familia después de comisiones y spread con Wise, Revolut, Western Union y billeteras locales, y te avisa cuando el tipo de cambio juega a tu favor. Alertas de tipo de cambio, comparación de costos de envío y una decisión diaria para tu corredor.

DECISIÓN DE HOY
Abrí la app y mirá cuánto llega hoy con tu monto habitual, qué proveedor entrega más y si conviene enviar ahora o esperar. Una pantalla, sin planilla.

COSTO REAL DE ENVÍO
Comisiones, spread y tiempo de entrega lado a lado para AUD→ARS, AUD→COP, AUD→PEN, USD→MXN, EUR→COP y más de 150 pares. La cotización mid-market es la referencia honesta; cada proveedor se mide contra ella.

ALERTAS DE TIPO DE CAMBIO
Fijá un objetivo o dejá que FX Always te sugiera uno. Recibí una notificación cuando tu corredor tiene un buen momento o aparece un proveedor más barato. Sin ruido diario.

CONVERSOR DE MONEDAS Y COTIZACIÓN DEL DÍA
Cotización de referencia del BCE actualizada cada día hábil, 165 monedas, cripto, rangos de 30 días e historial. Cada número muestra su fecha y su fuente.

PLATA PARA VIAJAR
Chequeo de precios locales, decisión tarjeta o efectivo, guía de propinas y cotizaciones sin conexión para tu destino.

GRATIS Y PRO
Gratis: monedas ilimitadas, 2 alertas, 3 proveedores por corredor, 1 año de historial. Pro: alertas ilimitadas, todos los proveedores y quién fue el más barato en el tiempo, escáner OCR de precios, P&L de portafolio, 5 años de historial.

FX Always no mueve dinero. Las cotizaciones son indicativas y pueden diferir de las de proveedores, tarjetas o casas de cambio.
""".trimIndent(),
    keywords = "enviar dinero, remesas, cotización dólar, conversor de monedas, alerta tipo de cambio, comisiones de envío, plata para viajar",
)

internal val StoreListingPT = StoreListing(
    locale = "pt-BR",
    title = "FX Always: Remessa inteligente",
    shortDescription = "Compare o custo real de enviar, receba alertas de câmbio e envie na hora certa.",
    longDescription = """
O FX Always mostra quanto sua família realmente recebe depois das taxas e do spread com Wise, Revolut, Western Union e carteiras locais, e avisa quando a taxa de câmbio está a seu favor. Alertas de câmbio, comparação de custo de remessa e uma decisão diária para o seu corredor.

DECISÃO DE HOJE
Abra o app e veja quanto chega hoje com o seu valor habitual, qual provedor entrega mais e se vale enviar agora ou esperar. Uma tela, sem planilha.

CUSTO REAL DA REMESSA
Taxas, spread e prazo de entrega lado a lado para USD→BRL, EUR→BRL, AUD→BRL, GBP→BRL e mais de 150 pares. A cotação mid-market é a referência honesta; cada provedor é medido contra ela.

ALERTAS DE CÂMBIO
Defina um alvo ou deixe o FX Always sugerir um. Receba uma notificação quando seu corredor tiver um bom momento ou aparecer um provedor mais barato. Sem ruído diário.

CONVERSOR DE MOEDAS E COTAÇÃO DO DIA
Cotação de referência do BCE atualizada todo dia útil, 165 moedas, cripto, faixas de 30 dias e histórico. Cada número mostra data e fonte.

DINHEIRO PARA VIAGEM
Verificação de preços locais, decisão cartão ou dinheiro, guia de gorjetas e cotações offline para o seu destino.

GRÁTIS E PRO
Grátis: moedas ilimitadas, 2 alertas, 3 provedores por corredor, 1 ano de histórico. Pro: alertas ilimitados, todos os provedores e quem foi o mais barato ao longo do tempo, scanner OCR de preços, P&L de carteira, 5 anos de histórico.

O FX Always não movimenta dinheiro. As cotações são indicativas e podem diferir das de provedores, cartões ou casas de câmbio.
""".trimIndent(),
    keywords = "enviar dinheiro, remessa, cotação dólar, conversor de moedas, alerta de câmbio, taxas de remessa, dinheiro para viagem",
)
