package com.fxalways.app.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fxalways.app.AppSettingsPrefs
import com.fxalways.app.AlertTestNotifier
import com.fxalways.app.BackupSettings
import com.fxalways.app.DeviceLocale
import com.fxalways.app.ExternalUrlOpener
import com.fxalways.app.NotificationPermissionStatus
import com.fxalways.app.Platform
import com.fxalways.app.PlatformConfig
import com.fxalways.app.ThemeMode
import com.fxalways.app.PlatformBackHandler
import com.fxalways.app.UserProfile
import com.fxalways.app.UserBackupGateway
import com.fxalways.app.UserBackupSnapshot
import com.fxalways.app.UserBackupState
import com.fxalways.app.isDefaultLocalBackup
import com.fxalways.app.data.mock.CompareRates
import com.fxalways.app.data.mock.ConverterRates
import com.fxalways.app.data.mock.CryptoRates
import com.fxalways.app.data.mock.DetailSeries
import com.fxalways.app.data.mock.FavoriteRates
import com.fxalways.app.data.mock.NewsStory
import com.fxalways.app.data.AlertsState
import com.fxalways.app.data.AlertsStore
import com.fxalways.app.data.AlertDirection
import com.fxalways.app.data.AlertKind
import com.fxalways.app.data.DetailStore
import com.fxalways.app.data.DetailUiState
import com.fxalways.app.data.LiveRatesState
import com.fxalways.app.data.LiveRatesStore
import com.fxalways.app.data.NewsStore
import com.fxalways.app.data.NewsUiState
import com.fxalways.app.data.PortfolioCsvImportResult
import com.fxalways.app.data.PriceAlert
import com.fxalways.app.data.PortfolioTransaction
import com.fxalways.app.data.PortfolioTransactionType
import com.fxalways.app.data.SettingsBaseCurrencies
import com.fxalways.app.data.Watchlist
import com.fxalways.app.data.WatchlistState
import com.fxalways.app.data.WatchlistStore
import com.fxalways.app.data.importPortfolioCsv
import com.fxalways.app.data.matchesDefinition
import com.fxalways.app.data.toPortfolioCsv
import com.fxalways.app.subscription.SubscriptionPlan
import com.fxalways.app.subscription.SubscriptionPlanKind
import com.fxalways.app.subscription.SubscriptionState
import com.fxalways.app.subscription.cap
import com.fxalways.app.subscription.createSubscriptionGateway
import com.fxalways.app.subscription.featureAccess
import com.fxalways.app.ui.SupportedLanguages
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.BentoTile
import com.fxalways.designsystem.components.BigValueText
import com.fxalways.designsystem.components.CurrencyKind
import com.fxalways.designsystem.components.CurrencyRow
import com.fxalways.designsystem.components.Eyebrow
import com.fxalways.designsystem.components.FlagDot
import com.fxalways.designsystem.components.FxBottomBar
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.components.GridBg
import com.fxalways.designsystem.components.KeyValueRow
import com.fxalways.designsystem.components.LiveDot
import com.fxalways.designsystem.components.MetricTile
import com.fxalways.designsystem.components.Period
import com.fxalways.designsystem.components.Pill
import com.fxalways.designsystem.components.PillVariant
import com.fxalways.designsystem.components.PriceChart
import com.fxalways.designsystem.components.ScreenHeader
import com.fxalways.designsystem.components.SectionLabel
import com.fxalways.designsystem.components.SegmentedPeriods
import com.fxalways.designsystem.components.SparkLine
import com.fxalways.designsystem.components.formatChange
import com.fxalways.designsystem.components.formatRate
import com.fxalways.designsystem.theme.FxTheme
import com.fxalways.observability.Observability
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

enum class FxTab(val label: String) {
    Rates("Rates"),
    Convert("Convert"),
    Compare("Compare"),
    News("News"),
    More("More"),
}

private enum class MoreRoute {
    Menu,
    Alerts,
    Watchlist,
    Traveler,
    Settings,
}

private val MoreRoute.analyticsName: String
    get() = when (this) {
        MoreRoute.Menu -> "more"
        MoreRoute.Alerts -> "alerts"
        MoreRoute.Watchlist -> "watchlist"
        MoreRoute.Traveler -> "traveler"
        MoreRoute.Settings -> "settings"
    }

private val LocalAppLanguage = staticCompositionLocalOf { "en" }

@Composable
private fun ui(text: String): String = localizedUiText(LocalAppLanguage.current, text)

private fun localizedUiText(language: String, text: String): String {
    val normalized = language.lowercase().substringBefore("-").substringBefore("_")
    return uiTranslations[normalized]?.get(text) ?: uiTranslations["en"]?.get(text) ?: text
}

private val uiTranslations = mapOf(
    "en" to mapOf(
        "No topic stories" to "No topic stories",
        "No currency stories" to "No currency stories",
        "No matching stories" to "No matching stories",
        "Try a broader filter or refresh the feed." to "Try a broader filter or refresh the feed.",
        "No live stories match this search." to "No live stories match this search.",
    ),
    "es" to mapOf(
        "Rates" to "Tipos",
        "Convert" to "Convertir",
        "Compare" to "Comparar",
        "News" to "Noticias",
        "More" to "Más",
        "LIVE" to "EN VIVO",
        "CACHED" to "CACHÉ",
        "Edit" to "Editar",
        "See all" to "Ver todo",
        "CRYPTO MARKET" to "MERCADO CRYPTO",
        "Crypto" to "Crypto",
        "Fiat" to "Fiat",
        "Stablecoins" to "Stablecoins",
        "Stablecoin" to "Stablecoin",
        "24H avg" to "Prom. 24H",
        "Strongest" to "Más fuerte",
        "major crypto assets" to "activos crypto principales",
        "per coin" to "por moneda",
        "live crypto movers" to "movimientos crypto en vivo",
        "No crypto rates yet" to "Sin rates crypto todavía",
        "Pro shows the full crypto board across compare, alerts and portfolio." to "Pro muestra el tablero crypto completo en comparación, alertas y portfolio.",
        "Pro" to "Pro",
        "Free" to "Free",
        "Preview" to "Vista previa",
        "Live" to "En vivo",
        "Loading" to "Cargando",
        "Estimated" to "Estimado",
        "Active" to "Activo",
        "Ready" to "Listo",
        "Next" to "Siguiente",
        "Unlimited" to "Ilimitado",
        "Preparing workspace" to "Preparando espacio",
        "Loading account, preferences and rates" to "Cargando cuenta, preferencias y tipos",
        "Live backend unavailable · using cached UI data" to "Backend no disponible · usando datos en caché",
        "base" to "base",
        "favorites" to "favoritos",
        "VOLATILITY · 24H" to "VOLATILIDAD · 24H",
        "FAVORITES" to "FAVORITOS",
        "Unlock full watchlists" to "Desbloquear watchlists completas",
        "Pro adds more favorites, extended history, alerts and complete fee comparison." to "Pro agrega más favoritos, histórico extendido, alertas y comparación completa de fees.",
        "CRYPTO" to "CRYPTO",
        "pinned" to "fijado",
        "Edit converter list" to "Editar lista del conversor",
        "Edit list" to "Editar lista",
        "Reverse" to "Invertir",
        "Pro unlocks more converter currencies" to "Pro desbloquea más monedas en el conversor",
        "MID" to "MEDIO",
        "Multi-currency · live to 4 decimals" to "Multimoneda · en vivo a 4 decimales",
        "YOU SEND" to "ENVIAS",
        "Converted to" to "Convertido a",
        "FEES" to "FEES",
        "Best received" to "Mejor recepción",
        "Worst loss" to "Mayor pérdida",
        "vs mid-market" to "vs mercado medio",
        "Mid-market value" to "Valor medio",
        "Your custom cost" to "Tu costo personalizado",
        "Effective rate" to "Tasa efectiva",
        "CUSTOM COST" to "COSTO PERSONALIZADO",
        "Fixed fee" to "Fee fijo",
        "Fee %" to "Fee %",
        "FX markup" to "Margen FX",
        "Fee" to "Fee",
        "Markup" to "Margen",
        "Lost" to "Perdido",
        "Card payment" to "Pago con tarjeta",
        "ATM cash" to "Cajero ATM",
        "Airport exchange" to "Cambio en aeropuerto",
        "Custom" to "Personalizado",
        "avoid" to "evitar",
        "See the real transfer cost" to "Ver el costo real de transferencia",
        "Pro unlocks the complete provider list; estimates update with your amount." to "Pro desbloquea la lista completa de proveedores; los estimados se actualizan con tu monto.",
        "Base currency · source amount" to "Moneda base · monto origen",
        "Selected destination" to "Destino seleccionado",
        "Mid-market" to "Mercado medio",
        "best" to "mejor",
        "Bank transfer" to "Transferencia bancaria",
        "high fee" to "fee alto",
        "cached preview" to "vista en caché",
        "mid-market" to "mercado medio",
        "LOADING HISTORY" to "CARGANDO HISTÓRICO",
        "HISTORY" to "HISTÓRICO",
        "24H RANGE" to "RANGO 24H",
        "History unavailable · using cached preview" to "Histórico no disponible · usando vista en caché",
        "Unlock long-range history" to "Desbloquear histórico largo",
        "Pro adds 1Y and all-time detail, full event context and deeper market overlays." to "Pro agrega 1A y todo el histórico, contexto de eventos y overlays de mercado.",
        "STATISTICS" to "ESTADÍSTICAS",
        "Open" to "Apertura",
        "High" to "Máximo",
        "Low" to "Mínimo",
        "Range" to "Rango",
        "Volatility" to "Volatilidad",
        "Average" to "Promedio",
        "RELATED NEWS" to "NOTICIAS RELACIONADAS",
        "Loading related news" to "Cargando noticias relacionadas",
        "No related news" to "Sin noticias relacionadas",
        "EVENTS · ANNOTATED" to "EVENTOS · ANOTADOS",
        "No annotated events" to "Sin eventos anotados",
        "Add another alert" to "Agregar otra alerta",
        "Alert me above" to "Alertarme arriba de",
        "Edit comparison" to "Editar comparación",
        "Pro unlocks more comparison currencies" to "Pro desbloquea más monedas para comparar",
        "Movers" to "Movimientos",
        "Strongest" to "Más fuertes",
        "Weakest" to "Más débiles",
        "STRONGEST" to "MÁS FUERTE",
        "WEAKEST" to "MÁS DÉBIL",
        "No data" to "Sin datos",
        "No comparison currencies" to "Sin monedas para comparar",
        "The saved list is unavailable for" to "La lista guardada no está disponible para",
        "Edit the comparison set to choose active currencies." to "Edita el set de comparación para elegir monedas activas.",
        "Compare every tracked currency" to "Comparar todas las monedas seguidas",
        "Free compares" to "Free compara",
        "COMPARE BOARD" to "TABLERO",
        "Average move" to "Movimiento promedio",
        "Momentum spread" to "Spread momentum",
        "Asset mix" to "Mix de activos",
        "crypto" to "crypto",
        "Pro unlocks the full board and advanced overlays." to "Pro desbloquea el panel completo y overlays avanzados.",
        "OVERLAY · 1M" to "OVERLAY · 1M",
        "per 1" to "por 1",
        "Choose destination" to "Elegir destino",
        "Traveler" to "Viajes",
        "Converter" to "Conversor",
        "Watchlist" to "Watchlist",
        "Settings" to "Ajustes",
        "Alerts" to "Alertas",
        "DESTINATION" to "DESTINO",
        "More destinations" to "Más destinos",
        "Search supported live currencies" to "Buscar monedas soportadas en vivo",
        "Search" to "Buscar",
        "supported live currencies" to "monedas soportadas en vivo",
        "live currencies" to "monedas en vivo",
        "Free shows" to "Free muestra",
        "Pro unlocks every supported currency" to "Pro desbloquea todas las monedas soportadas",
        "more +" to "más +",
        "Free keeps the destination picker focused on the most common travel currencies." to "Free mantiene el selector enfocado en las monedas de viaje más comunes.",
        "TRIP BUDGET" to "PRESUPUESTO",
        "BUDGET" to "PRESUPUESTO",
        "LOCAL" to "LOCAL",
        "Trip days" to "Días de viaje",
        "Daily budget = local budget / days" to "Presupuesto diario = presupuesto local / días",
        "Local budget" to "Presupuesto local",
        "Daily range" to "Rango diario",
        "Cash buffer" to "Reserva de efectivo",
        "of local budget" to "del presupuesto local",
        "SPEND PLAN" to "PLAN DE GASTO",
        "Daily budget" to "Presupuesto diario",
        "Card spend" to "Gasto con tarjeta",
        "after cash buffer" to "después de reserva",
        "Local meals" to "Comidas locales",
        "guide estimate" to "estimado guía",
        "Formula" to "Fórmula",
        "CHEAT SHEET" to "GUÍA RÁPIDA",
        "Unlock full traveler mode" to "Desbloquear modo viajero completo",
        "Pro adds complete cheat sheets, offline context and more local money tips." to "Pro agrega guías completas, contexto offline y más consejos locales.",
        "LOCAL ETIQUETTE" to "COSTUMBRES LOCALES",
        "TIPPING" to "PROPINA",
        "TAX" to "IMPUESTO",
        "CARDS ACCEPTED" to "TARJETAS ACEPTADAS",
        "LOCAL PRICE GUIDE" to "GUÍA DE PRECIOS",
        "Estimates" to "Estimados",
        "TOOLS" to "HERRAMIENTAS",
        "Travel, preferences and account" to "Viajes, preferencias y cuenta",
        "Local cheat sheets and offline rates" to "Guías locales y rates offline",
        "Market stream and sentiment" to "Noticias de mercado y sentimiento",
        "Theme mode, base currency and version" to "Tema, moneda base y versión",
        "Upgrade to Pro" to "Actualizar a Pro",
        "Language" to "Idioma",
        "Local base" to "Base local",
        "Region" to "Región",
        "COMING NEXT" to "PRÓXIMO",
        "WIDGETS" to "WIDGETS",
        "home screen and watch glance" to "inicio y vista rápida",
        "monthly plan controls" to "controles del plan mensual",
        "PRICE TARGETS" to "OBJETIVOS DE PRECIO",
        "Watch breakouts without watching charts." to "Sigue rupturas sin mirar gráficos.",
        "Android checks every 15 min when online. iOS saves alerts now; push delivery is next." to "Android revisa cada 15 min online. iOS guarda alertas ahora; push viene después.",
        "CUSTOM ALERT" to "ALERTA PERSONALIZADA",
        "Target rate" to "Tipo objetivo",
        "Daily move %" to "Movimiento diario %",
        "Keep existing alert active" to "Mantener alerta existente activa",
        "Reactivate existing alert" to "Reactivar alerta existente",
        "Unlock custom alerts" to "Desbloquear alertas personalizadas",
        "QUICK CREATE" to "CREACIÓN RÁPIDA",
        "Create unlimited alerts" to "Crear alertas ilimitadas",
        "Enter a target above 0" to "Ingresa un objetivo mayor a 0",
        "ACTIVE ALERTS" to "ALERTAS ACTIVAS",
        "NO ALERTS YET" to "SIN ALERTAS",
        "Create one from a favorite currency or from any detail screen." to "Crea una desde una moneda favorita o desde detalles.",
        "CUSTOM TRACKING" to "SEGUIMIENTO",
        "Tracked currencies" to "Monedas seguidas",
        "Add amounts below to value your portfolio." to "Agrega montos abajo para valorar tu portfolio.",
        "PORTFOLIO HOLDINGS" to "PORTFOLIO",
        "Choose currencies below to start tracking." to "Elige monedas abajo para empezar.",
        "HOW IT WORKS" to "CÓMO FUNCIONA",
        "Watchlist follows rates. Portfolio value appears after you enter how much you hold." to "La watchlist sigue los rates. El valor aparece al ingresar cuánto tienes.",
        "ADD OR REMOVE" to "AGREGAR O QUITAR",
        "Track unlimited currencies" to "Seguir monedas ilimitadas",
        "amount" to "monto",
        "done" to "listo",
        "tracked" to "seguida",
        "pro" to "pro",
        "add" to "agregar",
        "on" to "activa",
        "paused" to "pausada",
        "CURRENT" to "ACTUAL",
        "24H MOVE" to "MOV. 24H",
        "LAST HIT" to "ÚLTIMO HIT",
        "Never" to "Nunca",
        "monitoring" to "monitoreando",
        "pause" to "pausar",
        "resume" to "reanudar",
        "test" to "probar",
        "MARKET STREAM" to "MERCADO",
        "MARKET PREVIEW" to "VISTA MERCADO",
        "SENTIMENT" to "SENTIMIENTO",
        "REFRESHING" to "ACTUALIZANDO",
        "BULLISH" to "ALCISTA",
        "NEUTRAL" to "NEUTRAL",
        "BEARISH" to "BAJISTA",
        "Feed" to "Feed",
        "Updated" to "Actualizado",
        "Current" to "Actual",
        "REGION" to "REGIÓN",
        "CURRENCY" to "MONEDA",
        "TOPIC" to "TEMA",
        "RECENT LINES" to "LÍNEAS RECIENTES",
        "Refreshing market stream…" to "Actualizando mercado…",
        "Personalize the market stream" to "Personalizar noticias de mercado",
        "Market update" to "Actualización de mercado",
        "Latest currency market context." to "Último contexto del mercado de divisas.",
        "News detail" to "Detalle de noticia",
        "Market source" to "Fuente de mercado",
        "MARKET MOVES" to "MOVIMIENTOS",
        "No direct currency move was detected for this story." to "No se detectó movimiento directo para esta noticia.",
        "SOURCE" to "FUENTE",
        "Publisher" to "Publicador",
        "Published" to "Publicado",
        "Open original source" to "Abrir fuente original",
        "Choose base currency" to "Elegir moneda base",
        "FX/ Pro active" to "FX/ Pro activo",
        "FX/ Free" to "FX/ Free",
        "Signed in with" to "Sesión iniciada con",
        "DEV" to "DEV",
        "Debug-only local gate override" to "Override local solo debug",
        "Version" to "Versión",
        "Search currency" to "Buscar moneda",
        "No currencies found" to "No se encontraron monedas",
        "Cancel" to "Cancelar",
        "Apply" to "Aplicar",
        "System" to "Sistema",
        "Light" to "Claro",
        "Dark" to "Oscuro",
        "Follow device appearance" to "Seguir apariencia del dispositivo",
        "Use the bright interface" to "Usar interfaz clara",
        "Use the dark trading interface" to "Usar interfaz oscura",
        "Guest backup active" to "Backup invitado activo",
        "Backup unavailable" to "Backup no disponible",
        "Preferences, alerts and watchlist sync to Firebase" to "Preferencias, alertas y watchlist sincronizan con Firebase",
        "Firebase Auth has not started on this platform" to "Firebase Auth no inició en esta plataforma",
        "Firebase guest" to "Invitado Firebase",
        "Local iOS guest" to "Invitado local iOS",
        "Restores on any signed-in device" to "Se restaura en cualquier dispositivo con sesión",
        "account" to "cuenta",
        "active" to "activo",
        "ACTIVE" to "ACTIVO",
        "offline" to "offline",
        "syncing" to "sincronizando",
        "Sync pending" to "Sync pendiente",
        "Target" to "Objetivo",
        "Daily move" to "Movimiento diario",
        "Above" to "Arriba",
        "Below" to "Abajo",
        "Down" to "Abajo",
        "create" to "crear",
        "waiting" to "esperando",
        "target hit" to "objetivo alcanzado",
        "target reached" to "objetivo alcanzado",
        "base changed" to "base cambiada",
        "MOVES" to "MOVIMIENTOS",
        "Search headlines, tags or currencies" to "Buscar titulares, tags o monedas",
        "No market stories yet" to "Sin noticias de mercado",
        "No search matches" to "Sin coincidencias",
        "No topic stories" to "Sin noticias de este tema",
        "No currency stories" to "Sin noticias para esta moneda",
        "No matching stories" to "Sin noticias coincidentes",
        "Try a broader filter or refresh the feed." to "Prueba un filtro más amplio o actualiza el feed.",
        "No live stories match this search." to "No hay noticias en vivo para esta búsqueda.",
        "No live market stories have arrived yet." to "Aún no llegaron noticias de mercado en vivo.",
        "Open strongest" to "Abrir más fuerte",
        "Watching" to "Siguiendo",
        "alert" to "alerta",
        "alerts" to "alertas",
        "PAIR" to "PAR",
        "Create" to "Crear",
        "Existing alert reactivated" to "Alerta existente reactivada",
        "alert created" to "alerta creada",
        "current" to "actual",
        "holding" to "posición",
        "held" to "en cartera",
        "Tracking live rate" to "Siguiendo rate en vivo",
        "enter amount held" to "ingresa el monto en cartera",
        "holdings valued" to "posiciones valoradas",
        "selected" to "seleccionadas",
        "every supported currency available" to "todas las monedas disponibles",
        "Pro unlocks the full list" to "Pro desbloquea la lista completa",
        "focus" to "foco",
        "Showing" to "Mostrando",
        "stories" to "noticias",
        "News backend unavailable" to "Backend de noticias no disponible",
        "tap for details" to "toca para detalles",
        "MED" to "MEDIO",
        "HIGH" to "ALTO",
        "LOW" to "BAJO",
        "No live headlines are currently tied to" to "No hay titulares en vivo vinculados a",
        "Fetching market headlines" to "Buscando titulares de mercado para",
        "Events will appear here when stories include" to "Los eventos aparecerán aquí cuando las noticias incluyan",
        "Derived" to "Derivado",
        "Market source" to "Fuente de mercado",
        "This item is generated from the fallback market brief, so there is no external article link." to "Este item viene del resumen de respaldo, por eso no hay link externo.",
        "not customary" to "no es habitual",
        "often included" to "a menudo incluido",
        "cash useful" to "efectivo útil",
        "service dependent" to "depende del servicio",
        "VAT in price" to "IVA incluido",
        "cards common" to "tarjetas comunes",
        "often optional" to "suele ser opcional",
        "contactless first" to "contactless primero",
        "restaurants" to "restaurantes",
        "usually included" to "normalmente incluido",
        "carry cash" to "llevar efectivo",
        "often service charge" to "suele ser cargo de servicio",
        "varies by item" to "varía por item",
        "optional" to "opcional",
        "GST included" to "GST incluido",
        "often added" to "suele agregarse",
        "round up" to "redondear",
        "Check" to "Revisar",
        "Varies" to "Variable",
        "verify locally" to "verificar localmente",
        "mixed payments" to "pagos mixtos",
        "Coffee" to "Café",
        "Casual meal" to "Comida casual",
        "Taxi start" to "Inicio taxi",
        "Metro ride" to "Viaje metro",
        "Transit ticket" to "Ticket transporte",
        "Transit ride" to "Viaje transporte",
        "Transit fare" to "Tarifa transporte",
        "Pub meal" to "Comida pub",
        "Lunch" to "Almuerzo",
        "Active plan" to "Plan activo",
        "Entitlement is active" to "Entitlement activo",
        "Alerts, extended history and unlimited watchlists" to "Alertas, histórico extendido y watchlists ilimitadas",
        "The full picture.\nMore rates. More context." to "El panorama completo.\nMás rates. Más contexto.",
        "Unlimited alerts, deeper history, expanded comparisons, traveler tools and watchlists on one membership." to "Alertas ilimitadas, más histórico, comparaciones ampliadas, viajes y watchlists en una membresía.",
        "Built for people who move money, travel, track currencies or need alerts before rates move away." to "Hecho para quienes mueven dinero, viajan, siguen monedas o necesitan alertas antes de que cambien los rates.",
        "PRO UNLOCKS" to "PRO DESBLOQUEA",
        "FREE VS PRO" to "FREE VS PRO",
        "Free" to "Free",
        "Pro unlock" to "Pro desbloquea",
        "Custom alerts" to "Alertas custom",
        "1 active alert" to "1 alerta activa",
        "Unlimited pairs + ranges" to "Pares y rangos ilimitados",
        "Compare board" to "Tablero comparativo",
        "4 currencies" to "4 monedas",
        "Every tracked currency" to "Toda moneda seguida",
        "Crypto catalog" to "Catálogo crypto",
        "BTC, ETH, USDT, USDC" to "BTC, ETH, USDT, USDC",
        "Search and add up to 200 crypto assets" to "Busca y agrega hasta 200 activos crypto",
        "Traveler" to "Viajes",
        "Focused destinations" to "Destinos principales",
        "All destinations + full cheat sheet" to "Todos los destinos + guía completa",
        "Watchlist" to "Watchlist",
        "4 tracked currencies" to "4 monedas seguidas",
        "Unlimited portfolio tracking" to "Portfolio ilimitado",
        "News" to "Noticias",
        "Top stories only" to "Solo historias principales",
        "Full regional stream" to "Stream regional completo",
        "History" to "Histórico",
        "30 days" to "30 días",
        "1Y + all-time where available" to "1A + todo el histórico donde esté disponible",
        "Fresh market rates" to "Rates frescos",
        "Backend-backed mid-market rates with automatic refresh." to "Rates de mercado medio desde backend con actualización automática.",
        "Unlimited alerts" to "Alertas ilimitadas",
        "Price, range, daily and weekly targets." to "Objetivos de precio, rango, diarios y semanales.",
        "Traveler mode" to "Modo viajero",
        "Auto-location, cheat sheets and offline rates." to "Auto-ubicación, guías rápidas y rates offline.",
        "Fee comparison" to "Comparación de fees",
        "Expanded provider estimates by amount and currency pair." to "Estimados ampliados por proveedor, monto y par.",
        "Bigger watchlists" to "Watchlists más grandes",
        "Track more currencies across converter, compare and portfolio." to "Sigue más monedas en conversor, comparación y portfolio.",
        "Long-range history" to "Histórico largo",
        "Unlock 1Y and all-time detail views where history is available." to "Desbloquea 1A y todo el histórico donde esté disponible.",
        "Billed through Google Play on Android and App Store on iOS." to "Facturado por Google Play en Android y App Store en iOS.",
        "Processing..." to "Procesando...",
        "Continue" to "Continuar",
        "Purchases unavailable" to "Compras no disponibles",
        "Start FX/ Pro" to "Empezar FX/ Pro",
        "Restore purchase  ·  Terms  ·  Privacy" to "Restaurar compra  ·  Términos  ·  Privacidad",
        "FX/ Pro is active" to "FX/ Pro está activo",
        "Available" to "Disponible",
        "Not configured" to "No configurado",
        "Monthly" to "Mensual",
        "Yearly" to "Anual",
        "Lifetime" to "De por vida",
        "monthly" to "mensual",
        "yearly" to "anual",
        "lifetime" to "de por vida",
        "Paid every month" to "Pago mensual",
        "Best long-term value" to "Mejor valor a largo plazo",
        "One payment" to "Un solo pago",
        "One payment, permanent access" to "Un solo pago, acceso permanente",
        "BEST VALUE" to "MEJOR VALOR",
        "FOREVER" to "PARA SIEMPRE",
        "month" to "mes",
        "year" to "año",
        "Pro active" to "Pro activo",
        "No RevenueCat package is configured for" to "No hay paquete RevenueCat configurado para",
        "No offering packages are configured in RevenueCat." to "No hay paquetes de offering configurados en RevenueCat.",
        "RevenueCat unavailable." to "RevenueCat no disponible.",
        "RevenueCat key missing. Add REVENUECAT_API_KEY to enable live purchases." to "Falta la key de RevenueCat. Agrega REVENUECAT_API_KEY para activar compras reales.",
        "RevenueCat key missing. Add REVENUECAT_API_KEY before testing purchases." to "Falta la key de RevenueCat. Agrega REVENUECAT_API_KEY antes de probar compras.",
        "RevenueCat key missing. Restore is not connected yet." to "Falta la key de RevenueCat. Restaurar todavía no está conectado.",
        "Purchase did not complete." to "La compra no se completó.",
        "Restore failed." to "La restauración falló.",
        "Dev override only affects local debug gating." to "El override dev solo afecta el acceso local debug.",
        "saved" to "guardado",
        "allowed" to "permitidas",
        "review" to "revisar",
        "Notifications allowed" to "Notificaciones permitidas",
        "Notifications unavailable on this device" to "Notificaciones no disponibles en este dispositivo",
        "Notifications can be enabled from Android settings" to "Puedes activar notificaciones desde Ajustes de Android",
        "Android can deliver local price alerts while checks run in the background" to "Android puede enviar alertas locales mientras los chequeos corren en segundo plano",
        "Android permission is required before local price alerts can be delivered" to "Android necesita permiso antes de enviar alertas locales",
        "Alerts sync with your account; iOS push delivery is next" to "Las alertas sincronizan con tu cuenta; push en iOS viene después",
        "Review" to "Revisar",
        "Android phone" to "teléfono Android",
        "iPhone" to "iPhone",
        "Auto-refresh off" to "Auto-refresh apagado",
        "Auto-refresh every" to "Auto-refresh cada",
        "min" to "min",
        "cached" to "caché",
        "loading" to "cargando",
        "updated just now" to "actualizado recién",
        "updated" to "actualizado",
        "refreshed" to "actualizado",
        "synced just now" to "sincronizado recién",
        "synced" to "sincronizado",
        "ago" to "atrás",
        "away" to "restante",
        "pts away" to "pts restante",
        "to target" to "al objetivo",
        "pts to move" to "pts al movimiento",
        "waiting for live rate" to "esperando rate en vivo",
        "waiting for 24h change" to "esperando cambio 24h",
        "target" to "objetivo",
        "alert at" to "alerta en",
        "Now" to "Ahora",
        "min stale" to "min desactualizado",
        "saved locally" to "guardado localmente",
        "BASE" to "BASE",
        "currencies" to "monedas",
        "rates" to "rates",
        "Offline snapshot" to "Snapshot offline",
        "price targets and breakouts" to "objetivos y rupturas",
        "custom tracking" to "seguimiento personalizado",
        "Free includes" to "Free incluye",
        "Pro unlocks every pair, range and breakout alert." to "Pro desbloquea cada par, rango y alerta de ruptura.",
        "Pro unlocks bigger watchlists across rates, alerts and portfolio tracking." to "Pro desbloquea watchlists más grandes en rates, alertas y portfolio.",
        "added" to "agregada",
        "select" to "seleccionar",
        "set free" to "poner free",
        "set pro" to "poner pro",
        "Simulate" to "Simular",
        "US Dollar" to "Dólar estadounidense",
        "Euro" to "Euro",
        "British Pound" to "Libra esterlina",
        "Japanese Yen" to "Yen japonés",
        "Australian Dollar" to "Dólar australiano",
        "Canadian Dollar" to "Dólar canadiense",
        "Swiss Franc" to "Franco suizo",
        "Chinese Yuan" to "Yuan chino",
        "Brazilian Real" to "Real brasileño",
        "Mexican Peso" to "Peso mexicano",
        "No connection" to "Sin conexión",
        "OFFLINE" to "OFFLINE",
        "LAST KNOWN" to "ÚLTIMO CONOCIDO",
        "Retry connection" to "Reintentar conexión",
        "Showing rates from your last sync · 4 min ago" to "Mostrando rates del último sync · hace 4 min",
        "CACHED FAVORITES" to "FAVORITOS EN CACHÉ",
        "Skip" to "Saltar",
        "Get started" to "Empezar",
        "Next  →" to "Siguiente  →",
        "Fresh rates.\nAlways ready." to "Rates frescos.\nSiempre listos.",
        "The app starts with your local base currency and keeps rates refreshed from the backend." to "La app inicia con tu moneda local y mantiene los rates actualizados desde el backend.",
        "See the cost\nbefore you send." to "Ve el costo\nantes de enviar.",
        "Compare estimated provider fees by amount and currency pair, then unlock deeper comparisons with Pro." to "Compara fees estimados por monto y par de monedas, y desbloquea comparaciones más profundas con Pro.",
        "Your wallet\nfollows the map." to "Tu billetera\nsigue el mapa.",
        "Auto-detect local currency on landing. Offline-safe last rates. Per-country tipping built in." to "Detecta la moneda local al iniciar. Últimos rates disponibles offline. Propinas por país incluidas.",
        "Start private.\nRestore later." to "Empieza privado.\nRestaura después.",
        "A guest backup is created silently. You can connect Google on Android or Apple on iOS when you want portability." to "Se crea un backup invitado en silencio. Puedes conectar Google en Android o Apple en iOS cuando quieras portabilidad.",
        "STEP 01 · LIVE RATES" to "PASO 01 · RATES EN VIVO",
        "STEP 02 · FEES THAT MATTER" to "PASO 02 · FEES IMPORTANTES",
        "STEP 03 · TRAVEL READY" to "PASO 03 · LISTO PARA VIAJAR",
        "STEP 04 · BACKUP" to "PASO 04 · BACKUP",
        "FOR YOU" to "PARA TI",
        "Profile" to "Perfil",
        "Choose your focus" to "Elige tu foco",
        "Travel money setup" to "Setup de dinero para viajes",
        "Trip budget, local cash buffer and destination rates stay near the top." to "Presupuesto, efectivo local y tasas de destino quedan cerca del inicio.",
        "Budget + core destinations" to "Presupuesto + destinos core",
        "Full cheat sheet + all destinations" to "Guia completa + todos los destinos",
        "Crypto holder" to "Crypto holder",
        "Crypto portfolio focus" to "Foco en portfolio crypto",
        "Crypto board, stablecoins and holdings get priority across Home and Portfolio." to "Crypto, stablecoins y posiciones tienen prioridad en Home y Portfolio.",
        "BTC, ETH, USDT, USDC" to "BTC, ETH, USDT, USDC",
        "Expanded crypto catalog + holdings" to "Catalogo crypto expandido + holdings",
        "Remittances" to "Remesas",
        "Send money smarter" to "Enviar dinero mejor",
        "Provider cost, timing and alerts stay visible for repeat transfers." to "Costo por proveedor, timing y alertas quedan visibles para envios frecuentes.",
        "Mid-market + custom cost" to "Mid-market + costo custom",
        "Full provider comparison + alerts" to "Comparacion completa + alertas",
        "Freelancer" to "Freelancer",
        "Multi-currency income" to "Ingresos multi-moneda",
        "Converter, base currency and income pairs are tuned for cross-border work." to "Conversor, moneda base y pares de ingreso se ajustan al trabajo cross-border.",
        "Converter + saved pairs" to "Conversor + pares guardados",
        "Timing + portfolio + alerts" to "Timing + portfolio + alertas",
        "Savings" to "Ahorro",
        "Savings and allocation" to "Ahorro y allocation",
        "Portfolio allocation, long-range context and alerts are treated as the main workflow." to "Allocation, contexto largo y alertas pasan a ser el flujo principal.",
        "Portfolio snapshot" to "Snapshot de portfolio",
        "P&L, allocation and long history" to "P&L, allocation e historico largo",
        "Free focus" to "Foco Free",
        "Pro focus" to "Foco Pro",
        "Suggested pair" to "Par sugerido",
        "Suggested alert" to "Alerta sugerida",
        "Show all" to "Mostrar todo",
        "Showing top" to "Mostrando top",
        "Destination rate near 30d high" to "Rate de destino cerca del maximo 30d",
        "Trip cash budget" to "Presupuesto efectivo de viaje",
        "BTC/ETH daily move above 3%" to "Movimiento diario BTC/ETH mayor a 3%",
        "BTC, ETH and stablecoins" to "BTC, ETH y stablecoins",
        "Target rate above last 7d average" to "Rate objetivo sobre promedio 7d",
        "Receiver currency balance" to "Balance en moneda destino",
        "Invoice pair moves 1% in a day" to "Par de factura se mueve 1% en un dia",
        "Client payment currencies" to "Monedas de pago de clientes",
        "Portfolio allocation drift above 5%" to "Desvio de allocation mayor a 5%",
        "Core savings currencies" to "Monedas core de ahorro",
        "Pro unlocks the full regional stream." to "Pro desbloquea el stream regional completo.",
        "Pro unlocks more stories and filters by region, currencies and topics." to "Pro desbloquea más noticias y filtros por región, monedas y temas.",
        "fees" to "fees",
        "today" to "hoy",
        "days" to "días",
    ),
    "pt" to mapOf(
        "Rates" to "Cotações", "Convert" to "Converter", "Compare" to "Comparar", "News" to "Notícias", "More" to "Mais",
        "Settings" to "Ajustes", "Alerts" to "Alertas", "Watchlist" to "Watchlist", "Traveler" to "Viagem",
        "LIVE" to "AO VIVO", "CACHED" to "CACHE", "Edit" to "Editar", "See all" to "Ver tudo", "Preview" to "Prévia",
        "Live" to "Ao vivo", "Loading" to "Carregando", "Estimated" to "Estimado", "Active" to "Ativo", "Ready" to "Pronto",
        "Next" to "Próximo", "Unlimited" to "Ilimitado", "Preparing workspace" to "Preparando área", "base" to "base",
        "favorites" to "favoritos", "FAVORITES" to "FAVORITOS", "CRYPTO" to "CRYPTO", "pinned" to "fixado",
        "MID" to "MÉDIO", "YOU SEND" to "VOCÊ ENVIA", "Converted to" to "Convertido para", "FEES" to "TAXAS",
        "Reverse" to "Inverter", "Edit list" to "Editar lista", "HISTORY" to "HISTÓRICO", "STATISTICS" to "ESTATÍSTICAS",
        "Open" to "Abertura", "High" to "Máxima", "Low" to "Mínima", "Range" to "Faixa", "Volatility" to "Volatilidade",
        "Average" to "Média", "RELATED NEWS" to "NOTÍCIAS RELACIONADAS", "Movers" to "Movimentos", "Strongest" to "Mais fortes",
        "Weakest" to "Mais fracas", "STRONGEST" to "MAIS FORTE", "WEAKEST" to "MAIS FRACA",
        "No data" to "Sem dados", "The saved list is unavailable for" to "A lista salva não está disponível para",
        "Edit the comparison set to choose active currencies." to "Edite o conjunto de comparação para escolher moedas ativas.",
        "DESTINATION" to "DESTINO", "TRIP BUDGET" to "ORÇAMENTO", "BUDGET" to "ORÇAMENTO",
        "LOCAL" to "LOCAL", "Daily range" to "Faixa diária", "Cash buffer" to "Reserva em dinheiro", "CHEAT SHEET" to "GUIA RÁPIDO",
        "LOCAL ETIQUETTE" to "COSTUMES LOCAIS", "TIPPING" to "GORJETA", "TAX" to "IMPOSTO", "CARDS ACCEPTED" to "CARTÕES ACEITOS",
        "LOCAL PRICE GUIDE" to "GUIA DE PREÇOS", "TOOLS" to "FERRAMENTAS", "COMING NEXT" to "EM BREVE",
        "PRICE TARGETS" to "ALVOS DE PREÇO", "CUSTOM ALERT" to "ALERTA PERSONALIZADO", "Target rate" to "Taxa alvo",
        "Daily move %" to "Movimento diário %", "QUICK CREATE" to "CRIAÇÃO RÁPIDA", "ACTIVE ALERTS" to "ALERTAS ATIVOS",
        "CUSTOM TRACKING" to "RASTREAMENTO", "Tracked currencies" to "Moedas acompanhadas", "PORTFOLIO HOLDINGS" to "CARTEIRA",
        "HOW IT WORKS" to "COMO FUNCIONA", "ADD OR REMOVE" to "ADICIONAR OU REMOVER", "amount" to "valor", "done" to "pronto",
        "tracked" to "seguida", "add" to "adicionar", "on" to "ativo", "paused" to "pausado", "CURRENT" to "ATUAL",
        "LAST HIT" to "ÚLTIMO ACERTO", "Never" to "Nunca", "monitoring" to "monitorando", "pause" to "pausar", "resume" to "retomar",
        "test" to "testar", "MARKET STREAM" to "MERCADO", "MARKET PREVIEW" to "PRÉVIA", "SENTIMENT" to "SENTIMENTO",
        "REFRESHING" to "ATUALIZANDO", "BULLISH" to "ALTA", "NEUTRAL" to "NEUTRO", "BEARISH" to "BAIXA",
        "Feed" to "Feed", "Updated" to "Atualizado", "REGION" to "REGIÃO", "CURRENCY" to "MOEDA", "TOPIC" to "TEMA",
        "RECENT LINES" to "LINHAS RECENTES", "Market update" to "Atualização de mercado", "News detail" to "Detalhe da notícia",
        "SOURCE" to "FONTE", "Publisher" to "Publicador", "Published" to "Publicado", "Choose base currency" to "Escolher moeda base",
        "Search currency" to "Buscar moeda", "No currencies found" to "Nenhuma moeda encontrada", "Cancel" to "Cancelar", "Apply" to "Aplicar",
        "System" to "Sistema", "Light" to "Claro", "Dark" to "Escuro", "Version" to "Versão", "active" to "ativo",
        "offline" to "offline", "Target" to "Alvo", "Daily move" to "Movimento diário", "Above" to "Acima", "Below" to "Abaixo",
        "Down" to "Baixo", "MOVES" to "MOVIMENTOS", "Continue" to "Continuar", "Processing..." to "Processando...",
        "Purchases unavailable" to "Compras indisponíveis", "Available" to "Disponível", "Not configured" to "Não configurado",
        "Monthly" to "Mensal", "Yearly" to "Anual", "Lifetime" to "Vitalício", "monthly" to "mensal", "yearly" to "anual",
        "lifetime" to "vitalício", "Paid every month" to "Pago todo mês", "Best long-term value" to "Melhor valor no longo prazo",
        "One payment" to "Pagamento único", "One payment, permanent access" to "Pagamento único, acesso permanente",
        "BEST VALUE" to "MELHOR VALOR", "FOREVER" to "PARA SEMPRE", "allowed" to "permitidas", "review" to "revisar",
        "Notifications allowed" to "Notificações permitidas", "Review" to "Revisar",
        "Android can deliver local price alerts while checks run in the background" to "Android pode enviar alertas locais enquanto as verificações rodam em segundo plano",
        "Android permission is required before local price alerts can be delivered" to "Android precisa de permissão antes de enviar alertas locais",
        "Android phone" to "telefone Android", "Auto-refresh off" to "Auto-refresh desligado", "Auto-refresh every" to "Auto-refresh a cada",
        "min" to "min", "cached" to "cache", "loading" to "carregando", "updated just now" to "atualizado agora",
        "updated" to "atualizado", "refreshed" to "atualizado", "synced just now" to "sincronizado agora",
        "synced" to "sincronizado", "ago" to "atrás", "away" to "distante", "to target" to "até o alvo",
        "waiting for live rate" to "aguardando taxa ao vivo", "waiting for 24h change" to "aguardando variação 24h",
        "target" to "alvo", "alert at" to "alerta em", "Now" to "Agora", "min stale" to "min desatualizado",
        "saved locally" to "salvo localmente", "BASE" to "BASE", "currencies" to "moedas", "rates" to "taxas",
        "Search" to "Buscar", "supported live currencies" to "moedas ao vivo suportadas", "live currencies" to "moedas ao vivo",
        "Offline snapshot" to "Snapshot offline", "select" to "selecionar", "added" to "adicionada",
        "No connection" to "Sem conexão", "Skip" to "Pular", "Get started" to "Começar", "Next  →" to "Próximo  →",
    ),
    "fr" to mapOf(
        "Rates" to "Taux", "Convert" to "Convertir", "Compare" to "Comparer", "News" to "Actus", "More" to "Plus",
        "Settings" to "Réglages", "Alerts" to "Alertes", "Watchlist" to "Suivi", "Traveler" to "Voyage",
        "LIVE" to "EN DIRECT", "CACHED" to "CACHE", "Edit" to "Modifier", "See all" to "Tout voir", "Preview" to "Aperçu",
        "Live" to "En direct", "Loading" to "Chargement", "Estimated" to "Estimé", "Active" to "Actif", "Ready" to "Prêt",
        "Unlimited" to "Illimité", "base" to "base", "favorites" to "favoris", "FAVORITES" to "FAVORIS", "FEES" to "FRAIS",
        "Reverse" to "Inverser", "Edit list" to "Modifier", "HISTORY" to "HISTORIQUE", "STATISTICS" to "STATISTIQUES",
        "Open" to "Ouverture", "High" to "Haut", "Low" to "Bas", "Range" to "Plage", "Average" to "Moyenne",
        "RELATED NEWS" to "ACTUS LIÉES", "STRONGEST" to "PLUS FORT", "WEAKEST" to "PLUS FAIBLE", "No data" to "Aucune donnée",
        "DESTINATION" to "DESTINATION", "TRIP BUDGET" to "BUDGET", "CHEAT SHEET" to "AIDE-MÉMOIRE", "TIPPING" to "POURBOIRE",
        "TAX" to "TAXE", "TOOLS" to "OUTILS", "PRICE TARGETS" to "OBJECTIFS", "CUSTOM ALERT" to "ALERTE PERSONNALISÉE",
        "Target rate" to "Taux cible", "Daily move %" to "Variation quotidienne %", "ACTIVE ALERTS" to "ALERTES ACTIVES",
        "Tracked currencies" to "Devises suivies", "PORTFOLIO HOLDINGS" to "PORTEFEUILLE", "ADD OR REMOVE" to "AJOUTER OU RETIRER",
        "amount" to "montant", "done" to "terminé", "tracked" to "suivi", "add" to "ajouter", "paused" to "en pause",
        "CURRENT" to "ACTUEL", "Never" to "Jamais", "pause" to "pause", "resume" to "reprendre", "test" to "tester",
        "MARKET STREAM" to "MARCHÉ", "SENTIMENT" to "SENTIMENT", "BULLISH" to "HAUSSIER", "NEUTRAL" to "NEUTRE", "BEARISH" to "BAISSIER",
        "Updated" to "Mis à jour", "REGION" to "RÉGION", "CURRENCY" to "DEVISE", "TOPIC" to "SUJET", "News detail" to "Détail",
        "SOURCE" to "SOURCE", "Publisher" to "Éditeur", "Choose base currency" to "Choisir la devise de base",
        "Search currency" to "Chercher une devise", "No currencies found" to "Aucune devise trouvée", "Cancel" to "Annuler", "Apply" to "Appliquer",
        "System" to "Système", "Light" to "Clair", "Dark" to "Sombre", "Version" to "Version", "active" to "actif",
        "Above" to "Au-dessus", "Below" to "En dessous", "Continue" to "Continuer", "Processing..." to "Traitement...",
        "Available" to "Disponible", "No connection" to "Pas de connexion", "Skip" to "Ignorer", "Get started" to "Commencer", "Next  →" to "Suivant  →",
    ),
    "de" to mapOf(
        "Rates" to "Kurse", "Convert" to "Umrechnen", "Compare" to "Vergleichen", "News" to "News", "More" to "Mehr",
        "Settings" to "Einstellungen", "Alerts" to "Alarme", "Watchlist" to "Watchlist", "Traveler" to "Reise",
        "LIVE" to "LIVE", "CACHED" to "CACHE", "Edit" to "Bearbeiten", "See all" to "Alle sehen", "Preview" to "Vorschau",
        "Loading" to "Laden", "Estimated" to "Geschätzt", "Active" to "Aktiv", "Ready" to "Bereit", "Unlimited" to "Unbegrenzt",
        "favorites" to "Favoriten", "FAVORITES" to "FAVORITEN", "FEES" to "GEBÜHREN", "Reverse" to "Umkehren", "HISTORY" to "HISTORIE",
        "STATISTICS" to "STATISTIK", "Open" to "Start", "High" to "Hoch", "Low" to "Tief", "Range" to "Spanne", "Average" to "Durchschnitt",
        "STRONGEST" to "STÄRKSTE", "WEAKEST" to "SCHWÄCHSTE", "No data" to "Keine Daten", "DESTINATION" to "ZIEL",
        "TRIP BUDGET" to "REISEBUDGET", "CHEAT SHEET" to "KURZINFO", "TIPPING" to "TRINKGELD", "TAX" to "STEUER",
        "TOOLS" to "TOOLS", "CUSTOM ALERT" to "EIGENER ALARM", "Target rate" to "Zielkurs", "ACTIVE ALERTS" to "AKTIVE ALARME",
        "Tracked currencies" to "Verfolgte Währungen", "ADD OR REMOVE" to "HINZUFÜGEN ODER ENTFERNEN", "amount" to "Betrag",
        "done" to "fertig", "tracked" to "verfolgt", "add" to "hinzufügen", "paused" to "pausiert", "CURRENT" to "AKTUELL",
        "Never" to "Nie", "pause" to "pausieren", "resume" to "fortsetzen", "MARKET STREAM" to "MARKT", "SENTIMENT" to "STIMMUNG",
        "BULLISH" to "BULLISCH", "NEUTRAL" to "NEUTRAL", "BEARISH" to "BÄRISCH", "Updated" to "Aktualisiert",
        "REGION" to "REGION", "CURRENCY" to "WÄHRUNG", "TOPIC" to "THEMA", "SOURCE" to "QUELLE",
        "Search currency" to "Währung suchen", "No currencies found" to "Keine Währungen gefunden", "Cancel" to "Abbrechen", "Apply" to "Anwenden",
        "System" to "System", "Light" to "Hell", "Dark" to "Dunkel", "Version" to "Version", "Above" to "Über", "Below" to "Unter",
        "Continue" to "Weiter", "Processing..." to "Verarbeitung...", "Available" to "Verfügbar", "No connection" to "Keine Verbindung",
        "Skip" to "Überspringen", "Get started" to "Loslegen", "Next  →" to "Weiter  →",
    ),
    "id" to mapOf("Rates" to "Kurs", "Convert" to "Konversi", "Compare" to "Bandingkan", "News" to "Berita", "More" to "Lainnya", "Settings" to "Pengaturan", "Alerts" to "Peringatan", "Watchlist" to "Watchlist", "Traveler" to "Perjalanan", "LIVE" to "LIVE", "CACHED" to "CACHE", "Edit" to "Edit", "Preview" to "Pratinjau", "Loading" to "Memuat", "Unlimited" to "Tanpa batas", "FAVORITES" to "FAVORIT", "FEES" to "BIAYA", "HISTORY" to "RIWAYAT", "STATISTICS" to "STATISTIK", "DESTINATION" to "TUJUAN", "TRIP BUDGET" to "ANGGARAN", "TOOLS" to "ALAT", "CUSTOM ALERT" to "PERINGATAN KHUSUS", "ACTIVE ALERTS" to "PERINGATAN AKTIF", "amount" to "jumlah", "done" to "selesai", "tracked" to "dipantau", "add" to "tambah", "paused" to "jeda", "CURRENT" to "SAAT INI", "Never" to "Tidak pernah", "REGION" to "WILAYAH", "CURRENCY" to "MATA UANG", "TOPIC" to "TOPIK", "Search currency" to "Cari mata uang", "No currencies found" to "Mata uang tidak ditemukan", "Cancel" to "Batal", "Apply" to "Terapkan", "System" to "Sistem", "Light" to "Terang", "Dark" to "Gelap", "Version" to "Versi", "Continue" to "Lanjutkan", "Skip" to "Lewati", "Get started" to "Mulai", "Next  →" to "Berikutnya  →"),
    "ru" to mapOf("Rates" to "Курсы", "Convert" to "Конвертер", "Compare" to "Сравнить", "News" to "Новости", "More" to "Ещё", "Settings" to "Настройки", "Alerts" to "Оповещения", "Watchlist" to "Список", "Traveler" to "Путешествия", "LIVE" to "ОНЛАЙН", "CACHED" to "КЭШ", "Edit" to "Изменить", "Preview" to "Просмотр", "Loading" to "Загрузка", "Unlimited" to "Без лимита", "FAVORITES" to "ИЗБРАННОЕ", "FEES" to "КОМИССИИ", "HISTORY" to "ИСТОРИЯ", "STATISTICS" to "СТАТИСТИКА", "DESTINATION" to "НАПРАВЛЕНИЕ", "TRIP BUDGET" to "БЮДЖЕТ", "TOOLS" to "ИНСТРУМЕНТЫ", "CUSTOM ALERT" to "СВОЁ ОПОВЕЩЕНИЕ", "ACTIVE ALERTS" to "АКТИВНЫЕ ОПОВЕЩЕНИЯ", "amount" to "сумма", "done" to "готово", "tracked" to "отслеживается", "add" to "добавить", "paused" to "пауза", "CURRENT" to "ТЕКУЩИЙ", "Never" to "Никогда", "REGION" to "РЕГИОН", "CURRENCY" to "ВАЛЮТА", "TOPIC" to "ТЕМА", "Search currency" to "Найти валюту", "No currencies found" to "Валюты не найдены", "Cancel" to "Отмена", "Apply" to "Применить", "System" to "Система", "Light" to "Светлая", "Dark" to "Тёмная", "Version" to "Версия", "Continue" to "Продолжить", "Skip" to "Пропустить", "Get started" to "Начать", "Next  →" to "Далее  →"),
    "zh" to mapOf("Rates" to "汇率", "Convert" to "换算", "Compare" to "比较", "News" to "新闻", "More" to "更多", "Settings" to "设置", "Alerts" to "提醒", "Watchlist" to "关注", "Traveler" to "旅行", "LIVE" to "实时", "CACHED" to "缓存", "Edit" to "编辑", "Preview" to "预览", "Loading" to "加载中", "Unlimited" to "无限", "FAVORITES" to "收藏", "FEES" to "费用", "HISTORY" to "历史", "STATISTICS" to "统计", "DESTINATION" to "目的地", "TRIP BUDGET" to "旅行预算", "TOOLS" to "工具", "CUSTOM ALERT" to "自定义提醒", "ACTIVE ALERTS" to "活动提醒", "amount" to "金额", "done" to "完成", "tracked" to "已关注", "add" to "添加", "paused" to "暂停", "CURRENT" to "当前", "Never" to "从未", "REGION" to "地区", "CURRENCY" to "货币", "TOPIC" to "主题", "Search currency" to "搜索货币", "No currencies found" to "未找到货币", "Cancel" to "取消", "Apply" to "应用", "System" to "系统", "Light" to "浅色", "Dark" to "深色", "Version" to "版本", "Continue" to "继续", "Skip" to "跳过", "Get started" to "开始", "Next  →" to "下一步  →"),
    "ja" to mapOf("Rates" to "レート", "Convert" to "換算", "Compare" to "比較", "News" to "ニュース", "More" to "その他", "Settings" to "設定", "Alerts" to "アラート", "Watchlist" to "ウォッチ", "Traveler" to "旅行", "LIVE" to "ライブ", "CACHED" to "キャッシュ", "Edit" to "編集", "Preview" to "プレビュー", "Loading" to "読み込み中", "Unlimited" to "無制限", "FAVORITES" to "お気に入り", "FEES" to "手数料", "HISTORY" to "履歴", "STATISTICS" to "統計", "DESTINATION" to "目的地", "TRIP BUDGET" to "旅行予算", "TOOLS" to "ツール", "CUSTOM ALERT" to "カスタムアラート", "ACTIVE ALERTS" to "有効なアラート", "amount" to "金額", "done" to "完了", "tracked" to "追跡中", "add" to "追加", "paused" to "一時停止", "CURRENT" to "現在", "Never" to "なし", "REGION" to "地域", "CURRENCY" to "通貨", "TOPIC" to "トピック", "Search currency" to "通貨を検索", "No currencies found" to "通貨が見つかりません", "Cancel" to "キャンセル", "Apply" to "適用", "System" to "システム", "Light" to "ライト", "Dark" to "ダーク", "Version" to "バージョン", "Continue" to "続ける", "Skip" to "スキップ", "Get started" to "開始", "Next  →" to "次へ  →"),
    "hi" to mapOf("Rates" to "दरें", "Convert" to "कन्वर्ट", "Compare" to "तुलना", "News" to "समाचार", "More" to "और", "Settings" to "सेटिंग्स", "Alerts" to "अलर्ट", "Watchlist" to "वॉचलिस्ट", "Traveler" to "यात्रा", "LIVE" to "लाइव", "CACHED" to "कैश", "Edit" to "संपादित", "Preview" to "पूर्वावलोकन", "Loading" to "लोड हो रहा", "Unlimited" to "असीमित", "FAVORITES" to "पसंदीदा", "FEES" to "शुल्क", "HISTORY" to "इतिहास", "STATISTICS" to "आंकड़े", "DESTINATION" to "गंतव्य", "TRIP BUDGET" to "यात्रा बजट", "TOOLS" to "टूल", "CUSTOM ALERT" to "कस्टम अलर्ट", "ACTIVE ALERTS" to "सक्रिय अलर्ट", "amount" to "राशि", "done" to "हो गया", "tracked" to "ट्रैक", "add" to "जोड़ें", "paused" to "रुका", "CURRENT" to "वर्तमान", "Never" to "कभी नहीं", "REGION" to "क्षेत्र", "CURRENCY" to "मुद्रा", "TOPIC" to "विषय", "Search currency" to "मुद्रा खोजें", "No currencies found" to "मुद्रा नहीं मिली", "Cancel" to "रद्द", "Apply" to "लागू", "System" to "सिस्टम", "Light" to "लाइट", "Dark" to "डार्क", "Version" to "संस्करण", "Continue" to "जारी रखें", "Skip" to "छोड़ें", "Get started" to "शुरू करें", "Next  →" to "अगला  →"),
    "ar" to mapOf("Rates" to "الأسعار", "Convert" to "تحويل", "Compare" to "مقارنة", "News" to "الأخبار", "More" to "المزيد", "Settings" to "الإعدادات", "Alerts" to "تنبيهات", "Watchlist" to "المتابعة", "Traveler" to "السفر", "LIVE" to "مباشر", "CACHED" to "مخزن", "Edit" to "تعديل", "Preview" to "معاينة", "Loading" to "جار التحميل", "Unlimited" to "غير محدود", "FAVORITES" to "المفضلة", "FEES" to "الرسوم", "HISTORY" to "السجل", "STATISTICS" to "إحصاءات", "DESTINATION" to "الوجهة", "TRIP BUDGET" to "ميزانية السفر", "TOOLS" to "أدوات", "CUSTOM ALERT" to "تنبيه مخصص", "ACTIVE ALERTS" to "تنبيهات نشطة", "amount" to "المبلغ", "done" to "تم", "tracked" to "متابع", "add" to "إضافة", "paused" to "متوقف", "CURRENT" to "الحالي", "Never" to "أبداً", "REGION" to "المنطقة", "CURRENCY" to "العملة", "TOPIC" to "الموضوع", "Search currency" to "ابحث عن عملة", "No currencies found" to "لا توجد عملات", "Cancel" to "إلغاء", "Apply" to "تطبيق", "System" to "النظام", "Light" to "فاتح", "Dark" to "داكن", "Version" to "الإصدار", "Continue" to "متابعة", "Skip" to "تخطي", "Get started" to "ابدأ", "Next  →" to "التالي  →"),
    "bn" to mapOf("Rates" to "রেট", "Convert" to "রূপান্তর", "Compare" to "তুলনা", "News" to "খবর", "More" to "আরও", "Settings" to "সেটিংস", "Alerts" to "অ্যালার্ট", "Watchlist" to "ওয়াচলিস্ট", "Traveler" to "ভ্রমণ", "LIVE" to "লাইভ", "CACHED" to "ক্যাশ", "Edit" to "সম্পাদনা", "Preview" to "প্রিভিউ", "Loading" to "লোড হচ্ছে", "Unlimited" to "সীমাহীন", "FAVORITES" to "প্রিয়", "FEES" to "ফি", "HISTORY" to "ইতিহাস", "STATISTICS" to "পরিসংখ্যান", "DESTINATION" to "গন্তব্য", "TRIP BUDGET" to "ভ্রমণ বাজেট", "TOOLS" to "টুল", "CUSTOM ALERT" to "কাস্টম অ্যালার্ট", "ACTIVE ALERTS" to "সক্রিয় অ্যালার্ট", "amount" to "পরিমাণ", "done" to "শেষ", "tracked" to "ট্র্যাক", "add" to "যোগ", "paused" to "বিরতি", "CURRENT" to "বর্তমান", "Never" to "কখনও না", "REGION" to "অঞ্চল", "CURRENCY" to "মুদ্রা", "TOPIC" to "বিষয়", "Search currency" to "মুদ্রা খুঁজুন", "No currencies found" to "মুদ্রা পাওয়া যায়নি", "Cancel" to "বাতিল", "Apply" to "প্রয়োগ", "System" to "সিস্টেম", "Light" to "লাইট", "Dark" to "ডার্ক", "Version" to "সংস্করণ", "Continue" to "চালিয়ে যান", "Skip" to "এড়িয়ে যান", "Get started" to "শুরু করুন", "Next  →" to "পরবর্তী  →"),
    "ur" to mapOf("Rates" to "ریٹس", "Convert" to "کنورٹ", "Compare" to "موازنہ", "News" to "خبریں", "More" to "مزید", "Settings" to "ترتیبات", "Alerts" to "الرٹس", "Watchlist" to "واچ لسٹ", "Traveler" to "سفر", "LIVE" to "لائیو", "CACHED" to "کیش", "Edit" to "ترمیم", "Preview" to "پیش منظر", "Loading" to "لوڈ ہو رہا ہے", "Unlimited" to "لامحدود", "FAVORITES" to "پسندیدہ", "FEES" to "فیس", "HISTORY" to "تاریخ", "STATISTICS" to "اعداد", "DESTINATION" to "منزل", "TRIP BUDGET" to "سفر بجٹ", "TOOLS" to "ٹولز", "CUSTOM ALERT" to "کسٹم الرٹ", "ACTIVE ALERTS" to "فعال الرٹس", "amount" to "رقم", "done" to "ہو گیا", "tracked" to "ٹریک", "add" to "شامل", "paused" to "روکا", "CURRENT" to "موجودہ", "Never" to "کبھی نہیں", "REGION" to "علاقہ", "CURRENCY" to "کرنسی", "TOPIC" to "موضوع", "Search currency" to "کرنسی تلاش کریں", "No currencies found" to "کرنسی نہیں ملی", "Cancel" to "منسوخ", "Apply" to "لاگو", "System" to "سسٹم", "Light" to "لائٹ", "Dark" to "ڈارک", "Version" to "ورژن", "Continue" to "جاری رکھیں", "Skip" to "چھوڑیں", "Get started" to "شروع کریں", "Next  →" to "اگلا  →"),
)

@Composable
fun FxAppShell() {
    val initialProfile = remember { AppSettingsPrefs.userProfile() }
    val initialPreset = remember(initialProfile) { initialProfile.preset() }
    var selectedTab by remember { mutableStateOf(FxTab.Rates) }
    var moreRoute by remember { mutableStateOf(MoreRoute.Menu) }
    var detailRate by remember { mutableStateOf<FxRate?>(null) }
    var detailNewsStory by remember { mutableStateOf<NewsStory?>(null) }
    var showPaywall by remember { mutableStateOf(false) }
    var subscriptionActionInProgress by remember { mutableStateOf(false) }
    var themeMode by remember { mutableStateOf(AppSettingsPrefs.themeMode()) }
    var appLanguage by remember { mutableStateOf(AppSettingsPrefs.language()) }
    var baseCurrency by remember { mutableStateOf(AppSettingsPrefs.baseCurrency()) }
    var travelerCurrency by remember { mutableStateOf(AppSettingsPrefs.travelerCurrency()) }
    var travelerBudgetBase by remember { mutableStateOf(AppSettingsPrefs.travelerBudgetBase()) }
    var converterCurrencyCodes by remember { mutableStateOf(AppSettingsPrefs.converterCurrencyCodes()) }
    var compareCurrencyCodes by remember { mutableStateOf(AppSettingsPrefs.compareCurrencyCodes()) }
    var userProfile by remember { mutableStateOf(AppSettingsPrefs.userProfile()) }
    val liveStore = remember { LiveRatesStore(initialBaseCurrency = baseCurrency) }
    val newsStore = remember { NewsStore(initialLanguage = appLanguage) }
    val alertsStore = remember { AlertsStore() }
    val watchlistStore = remember { WatchlistStore() }
    val detailStore = remember { DetailStore() }
    val subscriptionGateway = remember { createSubscriptionGateway() }
    val cachedPremium = remember { AppSettingsPrefs.cachedPremium() }
    var subscriptionReady by remember { mutableStateOf(cachedPremium != null) }
    var subscriptionState by remember { mutableStateOf(SubscriptionState(isPremium = cachedPremium == true)) }
    var backupState by remember { mutableStateOf(UserBackupState()) }
    var backupReady by remember { mutableStateOf(false) }
    var backupSyncing by remember { mutableStateOf(false) }
    var startupReady by remember { mutableStateOf(false) }
    var lastSyncedAtMillis by remember { mutableStateOf<Long?>(null) }
    val scope = rememberCoroutineScope()
    val liveState by liveStore.state.collectAsState()
    val newsState by newsStore.state.collectAsState()
    val alertsState by alertsStore.state.collectAsState()
    val watchlistState by watchlistStore.state.collectAsState()
    val detailState by detailStore.state.collectAsState()
    LaunchedEffect(Unit) {
        if (converterCurrencyCodes.isEmpty()) {
            converterCurrencyCodes = initialPreset.converterCodes
            AppSettingsPrefs.setConverterCurrencyCodes(converterCurrencyCodes)
        }
        if (compareCurrencyCodes.isEmpty()) {
            compareCurrencyCodes = initialPreset.compareCodes
            AppSettingsPrefs.setCompareCurrencyCodes(compareCurrencyCodes)
        }
        if (watchlistState.watchlist == Watchlist()) {
            watchlistStore.replaceFromBackup(Watchlist(codes = initialPreset.watchlistCodes))
        }
        if (travelerCurrency == "JPY" && initialPreset.travelerCurrency != "JPY") {
            travelerCurrency = initialPreset.travelerCurrency
            AppSettingsPrefs.setTravelerCurrency(travelerCurrency)
        }
        if (AppSettingsPrefs.converterAmountText() == "1000" && initialPreset.suggestedAmount != "1000") {
            AppSettingsPrefs.setConverterAmountText(initialPreset.suggestedAmount)
        }
    }
    fun selectTab(tab: FxTab) {
        showPaywall = false
        detailRate = null
        detailNewsStory = null
        selectedTab = tab
        Observability.event("tab_selected", mapOf("tab" to tab.label))
        if (tab != FxTab.More) {
            moreRoute = MoreRoute.Menu
        }
    }
    fun openPaywall(source: String) {
        Observability.event("paywall_opened", mapOf("source" to source))
        showPaywall = true
    }
    fun openDetail(rate: FxRate, source: String) {
        Observability.event("currency_detail_opened", mapOf("source" to source, "currency" to rate.code))
        detailRate = rate
    }
    fun openStory(story: NewsStory, source: String) {
        Observability.event("news_story_opened", mapOf("source" to source, "tag" to story.tag))
        detailNewsStory = story
    }
    fun openMoreRoute(route: MoreRoute) {
        Observability.event("more_route_opened", mapOf("route" to route.analyticsName))
        moreRoute = route
    }
    LaunchedEffect(selectedTab, moreRoute, detailRate, detailNewsStory, showPaywall, startupReady) {
        if (startupReady) {
            val screenName = when {
                showPaywall -> "paywall"
                detailNewsStory != null -> "news_detail"
                detailRate != null -> "currency_detail"
                selectedTab == FxTab.More -> moreRoute.analyticsName
                else -> selectedTab.label
            }
            Observability.screen(
                screenName,
                mapOf(
                    "tab" to selectedTab.label,
                    "base_currency" to baseCurrency,
                    "language" to appLanguage,
                ),
            )
        }
    }
    LaunchedEffect(subscriptionState.isPremium, backupState.uid) {
        Observability.setUserId(backupState.uid)
        Observability.setUserProperty("premium", subscriptionState.isPremium.toString())
    }
    LaunchedEffect(liveStore) {
        liveStore.startAutoRefresh()
    }
    LaunchedEffect(startupReady, baseCurrency) {
        if (startupReady) {
            newsStore.setCurrency(baseCurrency)
        }
    }
    LaunchedEffect(Unit) {
        subscriptionState = subscriptionGateway.currentState()
        AppSettingsPrefs.setCachedPremium(subscriptionState.isPremium)
        subscriptionReady = true
        backupState = UserBackupGateway.ensureUser()
        if (backupState.isAvailable) {
            runCatching {
                val localSnapshot = buildUserBackupSnapshot(
                    themeMode,
                    appLanguage,
                    baseCurrency,
                    travelerCurrency,
                    travelerBudgetBase,
                    converterCurrencyCodes,
                    compareCurrencyCodes,
                    userProfile,
                    alertsState,
                    watchlistState,
                )
                val remoteSnapshot = UserBackupGateway.pullSnapshot()
                if (remoteSnapshot != null && localSnapshot.isDefaultLocalBackup()) {
                    themeMode = applyUserBackupSnapshot(
                        snapshot = remoteSnapshot,
                        alertsStore = alertsStore,
                        watchlistStore = watchlistStore,
                        liveStore = liveStore,
                        onConverterCurrencyCodes = { converterCurrencyCodes = it },
                        onCompareCurrencyCodes = { compareCurrencyCodes = it },
                        onTravelerCurrency = { travelerCurrency = it },
                        onTravelerBudgetBase = { travelerBudgetBase = it },
                        onUserProfile = { userProfile = it },
                        onLanguage = {
                            appLanguage = it
                            newsStore.setLanguage(it)
                        },
                    )
                    baseCurrency = remoteSnapshot.settings.baseCurrency
                } else if (remoteSnapshot == null) {
                    UserBackupGateway.pushSnapshot(localSnapshot)
                    lastSyncedAtMillis = localSnapshot.updatedAtMillis
                } else {
                    lastSyncedAtMillis = remoteSnapshot.updatedAtMillis
                }
            }.onFailure { error ->
                Observability.recordException(error, mapOf("flow" to "startup_backup"))
                backupState = backupState.copy(isAvailable = false, errorMessage = error.message)
            }
        }
        backupReady = backupState.isAvailable
        startupReady = true
    }
    LaunchedEffect(themeMode, appLanguage, baseCurrency, travelerCurrency, travelerBudgetBase, converterCurrencyCodes, compareCurrencyCodes, userProfile, alertsState, watchlistState, backupReady) {
        if (backupReady) {
            runCatching {
                val snapshot = buildUserBackupSnapshot(
                    themeMode,
                    appLanguage,
                    baseCurrency,
                    travelerCurrency,
                    travelerBudgetBase,
                    converterCurrencyCodes,
                    compareCurrencyCodes,
                    userProfile,
                    alertsState,
                    watchlistState,
                )
                UserBackupGateway.pushSnapshot(snapshot)
                lastSyncedAtMillis = snapshot.updatedAtMillis
            }.onFailure { error ->
                Observability.recordException(error, mapOf("flow" to "auto_backup_sync"))
                backupState = backupState.copy(isAvailable = false, errorMessage = error.message)
                backupReady = false
            }
        }
    }
    PlatformBackHandler(enabled = showPaywall || detailNewsStory != null || detailRate != null || selectedTab == FxTab.More && moreRoute != MoreRoute.Menu) {
        when {
            showPaywall -> showPaywall = false
            detailNewsStory != null -> detailNewsStory = null
            detailRate != null -> detailRate = null
            selectedTab == FxTab.More && moreRoute != MoreRoute.Menu -> moreRoute = MoreRoute.Menu
        }
    }
    val systemDark = isSystemInDarkTheme()
    val dark = when (themeMode) {
        ThemeMode.System -> systemDark
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    FxTheme(dark = dark) {
        CompositionLocalProvider(LocalAppLanguage provides appLanguage) {
        Column(
            Modifier
                .fillMaxSize()
                .background(FxTheme.colors.bg)
                .safeContentPadding(),
        ) {
            Box(Modifier.weight(1f)) {
                if (!startupReady) {
                    StartupLoadingScreen(baseCurrency, appLanguage)
                } else if (showPaywall) {
                    PaywallScreen(
                        subscriptionState = subscriptionState,
                        actionInProgress = subscriptionActionInProgress,
                        userProfile = userProfile,
                        onClose = { showPaywall = false },
                        onStart = { planKind ->
                            scope.launch {
                                subscriptionActionInProgress = true
                                try {
                                    Observability.event("purchase_started", mapOf("plan" to planKind.name))
                                    subscriptionState = subscriptionGateway.purchasePlan(planKind)
                                    AppSettingsPrefs.setCachedPremium(subscriptionState.isPremium)
                                    subscriptionReady = true
                                    showPaywall = !subscriptionState.isPremium
                                    Observability.event("purchase_finished", mapOf("premium" to subscriptionState.isPremium.toString()))
                                } catch (error: CancellationException) {
                                    throw error
                                } catch (error: Exception) {
                                    Observability.recordException(error, mapOf("flow" to "purchase", "plan" to planKind.name))
                                } finally {
                                    subscriptionActionInProgress = false
                                }
                            }
                        },
                        onRestore = {
                            scope.launch {
                                subscriptionActionInProgress = true
                                try {
                                    Observability.event("purchase_restore_started", mapOf("source" to "paywall"))
                                    subscriptionState = subscriptionGateway.restore()
                                    AppSettingsPrefs.setCachedPremium(subscriptionState.isPremium)
                                    subscriptionReady = true
                                    showPaywall = !subscriptionState.isPremium
                                    Observability.event("purchase_restore_finished", mapOf("premium" to subscriptionState.isPremium.toString()))
                                } catch (error: CancellationException) {
                                    throw error
                                } catch (error: Exception) {
                                    Observability.recordException(error, mapOf("flow" to "purchase_restore", "source" to "paywall"))
                                } finally {
                                    subscriptionActionInProgress = false
                                }
                            }
                        },
                        onOpenUrl = ExternalUrlOpener::open,
                    )
                } else if (detailNewsStory != null) {
                    NewsDetailScreen(
                        story = detailNewsStory,
                        onBack = { detailNewsStory = null },
                        onOpenUrl = ExternalUrlOpener::open,
                    )
                } else if (detailRate != null) {
                    DetailScreen(
                        liveState = liveState,
                        alertsState = alertsState,
                        subscriptionState = subscriptionState,
                        subscriptionReady = subscriptionReady,
                        detailState = detailState,
                        newsState = newsState,
                        rate = detailRate,
                        onBack = { detailRate = null },
                        onOpenPaywall = { openPaywall("currency_detail") },
                        onLoadHistory = detailStore::load,
                        onOpenUrl = ExternalUrlOpener::open,
                        onOpenStory = { openStory(it, "currency_detail") },
                        onCreateAlert = { rate ->
                            if (
                                canCreateAlert(subscriptionState, alertsState.alerts.size) ||
                                alertsState.alerts.findQuickAlert(liveState.baseCurrency, rate) != null
                            ) {
                                alertsStore.addQuickAlert(liveState.baseCurrency, rate)
                            } else {
                                openPaywall("currency_detail_alert_limit")
                            }
                        },
                    )
                } else {
                    when (selectedTab) {
                        FxTab.Rates -> {
                            if (liveState.errorMessage != null && !liveState.isLive) {
                                OfflineScreen(
                                    liveState,
                                    onRefresh = {
                                        Observability.event("rates_refresh", mapOf("source" to "offline"))
                                        liveStore.refresh()
                                    },
                                )
                            } else {
                                DashboardScreen(
                                    liveState = liveState,
                                    subscriptionState = subscriptionState,
                                    trackedCurrencyCodes = compareCurrencyCodes,
                                    userProfile = userProfile,
                                    onRefresh = {
                                        Observability.event("rates_refresh", mapOf("source" to "dashboard"))
                                        liveStore.refresh()
                                    },
                                    onOpenPaywall = { openPaywall("dashboard") },
                                    onOpenDetail = { openDetail(it, "dashboard") },
                                    onEditFavorites = {
                                        if (subscriptionState.isPremium) {
                                            selectTab(FxTab.More)
                                            openMoreRoute(MoreRoute.Watchlist)
                                        } else {
                                            openPaywall("dashboard_favorites")
                                        }
                                    },
                                    onSeeAllCrypto = {
                                        val cryptoCodes = liveState.visibleDashboardCryptoRates(subscriptionState.isPremium, compareCurrencyCodes).map { it.code }
                                        if (cryptoCodes.isNotEmpty()) {
                                            Observability.event("dashboard_crypto_see_all", mapOf("count" to cryptoCodes.size.toString()))
                                            compareCurrencyCodes = cryptoCodes
                                            AppSettingsPrefs.setCompareCurrencyCodes(cryptoCodes)
                                            selectTab(FxTab.Compare)
                                        }
                                    },
                                )
                            }
                        }
                        FxTab.Convert -> ConverterScreen(
                            liveState = liveState,
                            subscriptionState = subscriptionState,
                            selectedCurrencyCodes = converterCurrencyCodes,
                            onCurrencyCodesChange = { codes ->
                                Observability.event("converter_currencies_changed", mapOf("count" to codes.size.toString()))
                                converterCurrencyCodes = codes
                                AppSettingsPrefs.setConverterCurrencyCodes(codes)
                            },
                            onOpenPaywall = { openPaywall("converter") },
                        )
                        FxTab.Compare -> CompareScreen(
                            liveState = liveState,
                            subscriptionState = subscriptionState,
                            selectedCurrencyCodes = compareCurrencyCodes,
                            onCurrencyCodesChange = { codes ->
                                Observability.event("compare_currencies_changed", mapOf("count" to codes.size.toString()))
                                compareCurrencyCodes = codes
                                AppSettingsPrefs.setCompareCurrencyCodes(codes)
                            },
                            onOpenPaywall = { openPaywall("compare") },
                            onOpenDetail = { openDetail(it, "compare") },
                        )
                        FxTab.News -> NewsScreen(
                            newsState = newsState,
                            subscriptionState = subscriptionState,
                            onRefresh = {
                                Observability.event("news_refresh")
                                newsStore.refresh()
                            },
                            onRegionSelected = newsStore::setRegion,
                            onCurrencySelected = newsStore::setCurrency,
                            onOpenStory = { openStory(it, "news") },
                            onOpenPaywall = { openPaywall("news") },
                        )
                        FxTab.More -> when (moreRoute) {
                            MoreRoute.Menu -> MoreScreen(
                                subscriptionState = subscriptionState,
                                alertsCount = alertsState.activeCount,
                                watchlistCount = watchlistState.watchlist.codes.size,
                                onOpenAlerts = { openMoreRoute(MoreRoute.Alerts) },
                                onOpenWatchlist = { openMoreRoute(MoreRoute.Watchlist) },
                                onOpenTraveler = { openMoreRoute(MoreRoute.Traveler) },
                                onOpenSettings = { openMoreRoute(MoreRoute.Settings) },
                                onOpenNews = { selectTab(FxTab.News) },
                                onOpenPaywall = { openPaywall("more") },
                            )
                            MoreRoute.Alerts -> AlertsScreen(
                                liveState = liveState,
                                alertsState = alertsState,
                                subscriptionState = subscriptionState,
                                onBack = { moreRoute = MoreRoute.Menu },
                                onOpenPaywall = { openPaywall("alerts") },
                                onCreateAlert = { rate ->
                                    if (
                                        canCreateAlert(subscriptionState, alertsState.alerts.size) ||
                                        alertsState.alerts.findQuickAlert(liveState.baseCurrency, rate) != null
                                    ) {
                                        Observability.event("alert_created", mapOf("type" to "quick", "currency" to rate.code))
                                        alertsStore.addQuickAlert(liveState.baseCurrency, rate)
                                    } else {
                                        openPaywall("alert_limit")
                                    }
                                },
                                onCreateManualAlert = { rate, direction, target, kind ->
                                    if (
                                        canCreateAlert(subscriptionState, alertsState.alerts.size) ||
                                        alertsState.alerts.findMatchingAlert(liveState.baseCurrency, rate.code, target, direction, kind) != null
                                    ) {
                                        Observability.event("alert_created", mapOf("type" to "manual", "currency" to rate.code))
                                        alertsStore.addAlert(liveState.baseCurrency, rate.code, target, direction, kind)
                                    } else {
                                        openPaywall("alert_limit")
                                    }
                                },
                                onResumeAlert = alertsStore::resumeAlert,
                                onToggleAlert = alertsStore::toggleAlert,
                                onDeleteAlert = alertsStore::deleteAlert,
                                onTestAlert = AlertTestNotifier::show,
                            )
                            MoreRoute.Watchlist -> WatchlistScreen(
                                liveState = liveState,
                                watchlistState = watchlistState,
                                subscriptionState = subscriptionState,
                                onBack = { moreRoute = MoreRoute.Menu },
                                onOpenPaywall = { openPaywall("watchlist") },
                                onToggleCurrency = { code ->
                                    val selected = code in watchlistState.watchlist.codes
                                    val canAdd = selected ||
                                        subscriptionState.featureAccess().hasUnlimitedWatchlistCurrencies ||
                                        watchlistState.watchlist.codes.size < subscriptionState.featureAccess().watchlistCurrencyLimit
                                    if (!watchlistStore.toggle(code, canAdd)) {
                                        openPaywall("watchlist_limit")
                                    } else {
                                        Observability.event("watchlist_toggle", mapOf("currency" to code))
                                    }
                                },
                                onSetHolding = watchlistStore::setHolding,
                                onSetHoldingCost = watchlistStore::setHoldingCost,
                                onRecordTransaction = watchlistStore::recordTransaction,
                                onImportPortfolioCsv = watchlistStore::importPortfolioCsv,
                                onOpenDetail = { openDetail(it, "watchlist") },
                            )
                            MoreRoute.Traveler -> TravelerScreen(
                                liveState = liveState,
                                subscriptionState = subscriptionState,
                                selectedCurrency = travelerCurrency,
                                budgetBase = travelerBudgetBase,
                                onBack = { moreRoute = MoreRoute.Menu },
                                onCurrencySelected = { code ->
                                    Observability.event("traveler_currency_changed", mapOf("currency" to code))
                                    travelerCurrency = code
                                    AppSettingsPrefs.setTravelerCurrency(code)
                                },
                                onBudgetChange = { amount ->
                                    Observability.event("traveler_budget_changed")
                                    travelerBudgetBase = amount
                                    AppSettingsPrefs.setTravelerBudgetBase(amount)
                                },
                                onOpenPaywall = { openPaywall("traveler") },
                            )
                            MoreRoute.Settings -> SettingsScreen(
                                themeMode = themeMode,
                                appLanguage = appLanguage,
                                baseCurrency = baseCurrency,
                                userProfile = userProfile,
                                availableBaseCurrencies = liveState.allFiat,
                                backupState = backupState,
                                backupSyncing = backupSyncing,
                                lastSyncedAtMillis = lastSyncedAtMillis,
                                subscriptionState = subscriptionState,
                                onBack = { moreRoute = MoreRoute.Menu },
                                onOpenPaywall = { openPaywall("settings") },
                                onOpenUrl = ExternalUrlOpener::open,
                                onRestorePurchase = {
                                    scope.launch {
                                        Observability.event("purchase_restore_started", mapOf("source" to "settings"))
                                        subscriptionState = subscriptionGateway.restore()
                                        AppSettingsPrefs.setCachedPremium(subscriptionState.isPremium)
                                        subscriptionReady = true
                                        Observability.event("purchase_restore_finished", mapOf("premium" to subscriptionState.isPremium.toString()))
                                    }
                                },
                                onSyncNow = {
                                    scope.launch {
                                        backupSyncing = true
                                        runCatching {
                                            val snapshot = buildUserBackupSnapshot(
                                                themeMode,
                                                appLanguage,
                                                baseCurrency,
                                                travelerCurrency,
                                                travelerBudgetBase,
                                                converterCurrencyCodes,
                                                compareCurrencyCodes,
                                                userProfile,
                                                alertsState,
                                                watchlistState,
                                            )
                                            UserBackupGateway.pushSnapshot(snapshot)
                                            backupState = UserBackupGateway.ensureUser()
                                            lastSyncedAtMillis = snapshot.updatedAtMillis
                                        }.onFailure { error ->
                                            Observability.recordException(error, mapOf("flow" to "manual_backup_sync"))
                                            backupState = backupState.copy(errorMessage = error.message)
                                        }
                                        backupSyncing = false
                                    }
                                },
                                onLinkGoogle = {
                                    scope.launch {
                                        backupSyncing = true
                                        backupReady = false
                                        runCatching {
                                            val snapshot = buildUserBackupSnapshot(
                                                themeMode,
                                                appLanguage,
                                                baseCurrency,
                                                travelerCurrency,
                                                travelerBudgetBase,
                                                converterCurrencyCodes,
                                                compareCurrencyCodes,
                                                userProfile,
                                                alertsState,
                                                watchlistState,
                                            )
                                            val result = when (PlatformConfig.platform) {
                                                Platform.Android -> UserBackupGateway.linkWithGoogle(snapshot)
                                                Platform.Ios -> UserBackupGateway.linkWithApple(snapshot)
                                            }
                                            backupState = result.state
                                            val appliedTheme = applyUserBackupSnapshot(
                                                snapshot = result.snapshot,
                                                alertsStore = alertsStore,
                                                watchlistStore = watchlistStore,
                                                liveStore = liveStore,
                                                onConverterCurrencyCodes = { converterCurrencyCodes = it },
                                                onCompareCurrencyCodes = { compareCurrencyCodes = it },
                                                onTravelerCurrency = { travelerCurrency = it },
                                                onTravelerBudgetBase = { travelerBudgetBase = it },
                                                onUserProfile = { userProfile = it },
                                                onLanguage = {
                                                    appLanguage = it
                                                    newsStore.setLanguage(it)
                                                },
                                            )
                                            themeMode = appliedTheme
                                            baseCurrency = result.snapshot.settings.baseCurrency
                                            lastSyncedAtMillis = result.snapshot.updatedAtMillis
                                            backupReady = true
                                        }.onFailure { error ->
                                            Observability.recordException(error, mapOf("flow" to "link_backup_identity"))
                                            backupState = backupState.copy(errorMessage = error.message)
                                            backupReady = backupState.isAvailable
                                        }
                                        backupSyncing = false
                                    }
                                },
                                onSignOut = {
                                    scope.launch {
                                        backupSyncing = true
                                        backupReady = false
                                        runCatching {
                                            val snapshot = buildUserBackupSnapshot(
                                                themeMode,
                                                appLanguage,
                                                baseCurrency,
                                                travelerCurrency,
                                                travelerBudgetBase,
                                                converterCurrencyCodes,
                                                compareCurrencyCodes,
                                                userProfile,
                                                alertsState,
                                                watchlistState,
                                            )
                                            val result = UserBackupGateway.signOutToAnonymous(snapshot)
                                            backupState = result.state
                                            lastSyncedAtMillis = result.snapshot.updatedAtMillis
                                            backupReady = true
                                        }.onFailure { error ->
                                            Observability.recordException(error, mapOf("flow" to "sign_out_to_anonymous"))
                                            backupState = backupState.copy(errorMessage = error.message)
                                            backupReady = backupState.isAvailable
                                        }
                                        backupSyncing = false
                                    }
                                },
                                onDevPremiumChange = { enabled ->
                                    scope.launch {
                                        subscriptionState = subscriptionGateway.setDevPremium(enabled)
                                        AppSettingsPrefs.setCachedPremium(subscriptionState.isPremium)
                                        subscriptionReady = true
                                    }
                                },
                                onThemeModeChange = { mode ->
                                    Observability.event("theme_changed", mapOf("theme" to mode.name))
                                    themeMode = mode
                                    AppSettingsPrefs.setThemeMode(mode)
                                },
                                onLanguageChange = { code ->
                                    Observability.event("language_changed", mapOf("language" to code))
                                    appLanguage = code
                                    AppSettingsPrefs.setLanguage(code)
                                    newsStore.setLanguage(code)
                                },
                                onBaseCurrencyChange = { code ->
                                    Observability.event("base_currency_changed", mapOf("currency" to code))
                                    baseCurrency = code
                                    AppSettingsPrefs.setBaseCurrency(code)
                                    liveStore.setBaseCurrency(code)
                                },
                                onUserProfileChange = { profile ->
                                    Observability.event("profile_changed", mapOf("profile" to profile.name))
                                    val preset = profile.preset()
                                    userProfile = profile
                                    AppSettingsPrefs.setUserProfile(profile)
                                    converterCurrencyCodes = preset.converterCodes
                                    compareCurrencyCodes = preset.compareCodes
                                    travelerCurrency = preset.travelerCurrency
                                    AppSettingsPrefs.setConverterCurrencyCodes(converterCurrencyCodes)
                                    AppSettingsPrefs.setCompareCurrencyCodes(compareCurrencyCodes)
                                    AppSettingsPrefs.setTravelerCurrency(travelerCurrency)
                                    AppSettingsPrefs.setConverterAmountText(preset.suggestedAmount)
                                    if (watchlistState.watchlist.holdings.isEmpty() && watchlistState.watchlist.transactions.isEmpty()) {
                                        watchlistStore.replaceFromBackup(Watchlist(codes = preset.watchlistCodes))
                                    }
                                },
                            )
                        }
                    }
                }
            }
            if (startupReady) {
                FxBottomBar(
                    tabs = FxTab.entries.map { ui(it.label) },
                    selectedIndex = selectedTab.ordinal,
                    onSelect = {
                        selectTab(FxTab.entries[it])
                    },
                    iconKeys = FxTab.entries.map { it.label },
                )
            }
        }
        }
    }
}

@Composable
private fun StartupLoadingScreen(baseCurrency: String, language: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        contentAlignment = Alignment.Center,
    ) {
        GridBg(Modifier.matchParentSize().alpha(0.10f), radialMask = false)
        GridBg(Modifier.matchParentSize().alpha(0.22f))
        BentoCard(padding = 18.dp) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LiveDot(Modifier.size(10.dp))
                Text("${localizedUiText(language, "Preparing workspace")} · $baseCurrency", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                Text(localizedUiText(language, "Loading account, preferences and rates"), style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
            }
        }
    }
}

@Composable
private fun ScreenScaffold(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = content,
    )
}

private data class ProfileCopy(
    val title: String,
    val label: String,
    val subtitle: String,
    val freeFocus: String,
    val proFocus: String,
)

private fun UserProfile.copy(): ProfileCopy =
    when (this) {
        UserProfile.Traveler -> ProfileCopy(
            title = "Travel money setup",
            label = "Traveler",
            subtitle = "Trip budget, local cash buffer and destination rates stay near the top.",
            freeFocus = "Budget + core destinations",
            proFocus = "Full cheat sheet + all destinations",
        )
        UserProfile.CryptoHolder -> ProfileCopy(
            title = "Crypto portfolio focus",
            label = "Crypto holder",
            subtitle = "Crypto board, stablecoins and holdings get priority across Home and Portfolio.",
            freeFocus = "BTC, ETH, USDT, USDC",
            proFocus = "Expanded crypto catalog + holdings",
        )
        UserProfile.Remittances -> ProfileCopy(
            title = "Send money smarter",
            label = "Remittances",
            subtitle = "Provider cost, timing and alerts stay visible for repeat transfers.",
            freeFocus = "Mid-market + custom cost",
            proFocus = "Full provider comparison + alerts",
        )
        UserProfile.Freelancer -> ProfileCopy(
            title = "Multi-currency income",
            label = "Freelancer",
            subtitle = "Converter, base currency and income pairs are tuned for cross-border work.",
            freeFocus = "Converter + saved pairs",
            proFocus = "Timing + portfolio + alerts",
        )
        UserProfile.Savings -> ProfileCopy(
            title = "Savings and allocation",
            label = "Savings",
            subtitle = "Portfolio allocation, long-range context and alerts are treated as the main workflow.",
            freeFocus = "Portfolio snapshot",
            proFocus = "P&L, allocation and long history",
        )
    }

private data class ProfilePreset(
    val initialTab: FxTab,
    val moreRoute: MoreRoute = MoreRoute.Menu,
    val converterCodes: List<String>,
    val compareCodes: List<String>,
    val watchlistCodes: List<String>,
    val travelerCurrency: String,
    val suggestedAmount: String,
    val suggestedPair: String,
    val suggestedProvider: String,
    val suggestedAlert: String,
    val suggestedHolding: String,
)

private fun UserProfile.preset(): ProfilePreset =
    when (this) {
        UserProfile.Traveler -> ProfilePreset(
            initialTab = FxTab.More,
            moreRoute = MoreRoute.Traveler,
            converterCodes = listOf("EUR", "GBP", "JPY"),
            compareCodes = listOf("EUR", "GBP", "JPY", "MXN"),
            watchlistCodes = listOf("EUR", "GBP", "JPY", "MXN"),
            travelerCurrency = "JPY",
            suggestedAmount = "1000",
            suggestedPair = "USD -> JPY",
            suggestedProvider = "Wise / Revolut",
            suggestedAlert = "Destination rate near 30d high",
            suggestedHolding = "Trip cash budget",
        )
        UserProfile.CryptoHolder -> ProfilePreset(
            initialTab = FxTab.More,
            moreRoute = MoreRoute.Watchlist,
            converterCodes = listOf("BTC", "ETH", "USDT", "USDC"),
            compareCodes = listOf("BTC", "ETH", "USDT", "USDC"),
            watchlistCodes = listOf("BTC", "ETH", "USDT", "USDC"),
            travelerCurrency = "EUR",
            suggestedAmount = "1000",
            suggestedPair = "USD -> BTC",
            suggestedProvider = "Mid-market crypto rate",
            suggestedAlert = "BTC/ETH daily move above 3%",
            suggestedHolding = "BTC, ETH and stablecoins",
        )
        UserProfile.Remittances -> ProfilePreset(
            initialTab = FxTab.Convert,
            converterCodes = listOf("MXN", "EUR", "GBP", "BRL"),
            compareCodes = listOf("MXN", "EUR", "GBP", "BRL"),
            watchlistCodes = listOf("MXN", "EUR", "GBP", "BRL"),
            travelerCurrency = "MXN",
            suggestedAmount = "500",
            suggestedPair = "USD -> MXN",
            suggestedProvider = "Wise first, compare bank transfer",
            suggestedAlert = "Target rate above last 7d average",
            suggestedHolding = "Receiver currency balance",
        )
        UserProfile.Freelancer -> ProfilePreset(
            initialTab = FxTab.Convert,
            converterCodes = listOf("EUR", "GBP", "AUD", "CAD"),
            compareCodes = listOf("EUR", "GBP", "AUD", "CAD"),
            watchlistCodes = listOf("EUR", "GBP", "AUD", "CAD"),
            travelerCurrency = "EUR",
            suggestedAmount = "2500",
            suggestedPair = "USD -> EUR",
            suggestedProvider = "Wise / bank transfer",
            suggestedAlert = "Invoice pair moves 1% in a day",
            suggestedHolding = "Client payment currencies",
        )
        UserProfile.Savings -> ProfilePreset(
            initialTab = FxTab.More,
            moreRoute = MoreRoute.Watchlist,
            converterCodes = listOf("EUR", "CHF", "BTC", "ETH"),
            compareCodes = listOf("EUR", "CHF", "BTC", "ETH"),
            watchlistCodes = listOf("EUR", "CHF", "BTC", "ETH"),
            travelerCurrency = "CHF",
            suggestedAmount = "1000",
            suggestedPair = "USD -> CHF",
            suggestedProvider = "Mid-market baseline",
            suggestedAlert = "Portfolio allocation drift above 5%",
            suggestedHolding = "Core savings currencies",
        )
    }

@Composable
private fun ProfileInsightCard(
    profile: UserProfile,
    isPremium: Boolean,
    modifier: Modifier = Modifier,
) {
    val copy = profile.copy()
    val preset = profile.preset()
    BentoCard(modifier.fillMaxWidth(), padding = 14.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Eyebrow("${ui("FOR YOU")} · ${ui(copy.label)}", color = FxTheme.colors.accent)
                Pill(if (isPremium) ui("Pro") else ui("Free"), variant = if (isPremium) PillVariant.Accent else PillVariant.Ghost)
            }
            Text(ui(copy.title), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text(ui(copy.subtitle), style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ProfileMetricTile(ui("Free focus"), ui(copy.freeFocus), null, Modifier.weight(1f).testTag("dashboard_profile_free_focus"))
                ProfileMetricTile(ui("Pro focus"), ui(copy.proFocus), null, Modifier.weight(1f).testTag("dashboard_profile_pro_focus"))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ProfileMetricTile(ui("Suggested pair"), preset.suggestedPair, preset.suggestedProvider, Modifier.weight(1f).testTag("dashboard_profile_pair"))
                ProfileMetricTile(ui("Suggested alert"), ui(preset.suggestedAlert), ui(preset.suggestedHolding), Modifier.weight(1f).testTag("dashboard_profile_alert"))
            }
        }
    }
}

@Composable
private fun ProfileMetricTile(
    label: String,
    value: String,
    sub: String?,
    modifier: Modifier = Modifier,
) {
    BentoTile(
        modifier = modifier.heightIn(min = 108.dp),
        padding = 13.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text(
                label.uppercase(),
                style = FxTheme.typography.eyebrow,
                color = FxTheme.colors.textFaint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                value,
                style = FxTheme.typography.bodyStrong,
                color = FxTheme.colors.text,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp,
            )
            sub?.let {
                Text(
                    it,
                    style = FxTheme.typography.caption,
                    color = FxTheme.colors.textDim,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp,
                )
            }
        }
    }
}

@Composable
fun DashboardScreen(
    liveState: LiveRatesState,
    subscriptionState: SubscriptionState,
    trackedCurrencyCodes: List<String> = emptyList(),
    userProfile: UserProfile = UserProfile.Traveler,
    onRefresh: () -> Unit,
    onOpenPaywall: () -> Unit,
    onOpenDetail: (FxRate) -> Unit,
    onEditFavorites: () -> Unit,
    onSeeAllCrypto: () -> Unit,
) {
    val access = subscriptionState.featureAccess()
    val preset = userProfile.preset()
    val profileFavorites = remember(liveState.favorites, userProfile, access.favoriteLimit) {
        val ordered = liveState.favorites.sortedWith(compareBy<FxRate> {
            val index = preset.watchlistCodes.indexOf(it.code)
            if (index == -1) Int.MAX_VALUE else index
        }.thenBy { it.code })
        ordered.take(access.favoriteLimit.cap(ordered.size))
    }
    val visibleFavorites = profileFavorites
    val visibleCrypto = liveState.visibleDashboardCryptoRates(subscriptionState.isPremium, trackedCurrencyCodes)
    val cryptoAverageMove = visibleCrypto.takeIf { it.isNotEmpty() }?.map { it.change24h }?.average() ?: 0.0
    val strongestCrypto = visibleCrypto.maxByOrNull { it.change24h }
    val stablecoinCount = visibleCrypto.count { it.code in StablecoinCodes }
    ScreenScaffold {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                LiveDot(Modifier.size(9.dp))
                Eyebrow(if (liveState.isLive) ui("LIVE") else ui("CACHED"), color = FxTheme.colors.accent)
            }
            Text(localizedRuntimeLabel(liveState.updatedLabel), style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint, textAlign = TextAlign.End)
        }
        ScreenHeader(
            title = ui("Rates"),
            subtitle = "${ui("base")} · ${liveState.baseCurrency}  ·  ${visibleFavorites.size}/${liveState.favorites.size} ${ui("favorites")} · ${localizedRuntimeLabel(liveState.autoRefreshLabel)}",
            right = { Text("↻", style = FxTheme.typography.numberL, color = FxTheme.colors.textDim, modifier = Modifier.clickable(onClick = onRefresh)) },
        )
        if (liveState.errorMessage != null) {
            Text(ui("Live backend unavailable · using cached UI data"), style = FxTheme.typography.captionMono, color = FxTheme.colors.down)
        }
        ProfileInsightCard(
            profile = userProfile,
            isPremium = subscriptionState.isPremium,
            modifier = Modifier.testTag("dashboard_profile_card"),
        )
        HeroRateCard(visibleFavorites.firstOrNull() ?: FavoriteRates.first(), liveState.baseCurrency)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricTile(ui("VOLATILITY · 24H"), "0.42%", null, Modifier.weight(1f).height(76.dp))
            liveState.favorites.firstOrNull { it.code == "GBP" }?.let { MetricTile("GBP · 1H", formatRate(it.rate), formatChange(it.change24h), Modifier.weight(1f).height(76.dp)) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            liveState.favorites.firstOrNull { it.code == "JPY" }?.let { MetricTile("JPY · 1H", formatRate(it.rate), formatChange(it.change24h), Modifier.weight(1f).height(76.dp)) }
            liveState.favorites.firstOrNull { it.code == "MXN" }?.let { MetricTile("MXN · 1H", formatRate(it.rate), formatChange(it.change24h), Modifier.weight(1f).height(76.dp)) }
        }
        SectionLabel(
            "${ui("FAVORITES")} · ${visibleFavorites.size}",
            right = if (subscriptionState.isPremium) ui("Edit") else ui("Pro"),
            onRightClick = onEditFavorites,
        )
        BentoCard(padding = 0.dp) {
            Column {
                visibleFavorites.forEach { rate ->
                    CurrencyRow(localizedRate(rate), dense = true, onClick = { onOpenDetail(rate) })
                }
            }
        }
        if (!subscriptionState.isPremium) {
            ProUpsellCard(
                title = ui("Unlock full watchlists"),
                subtitle = ui("Pro adds more favorites, extended history, alerts and complete fee comparison."),
                onClick = onOpenPaywall,
            )
        }
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 6.dp).testTag("dashboard_crypto_header"),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Eyebrow(ui("CRYPTO MARKET"))
            Text(
                ui("See all"),
                style = FxTheme.typography.captionMono,
                color = FxTheme.colors.accent,
                modifier = Modifier.testTag("dashboard_crypto_see_all").clickable(onClick = onSeeAllCrypto),
            )
        }
        if (visibleCrypto.isEmpty()) {
            BentoCard(Modifier.testTag("dashboard_crypto_empty"), padding = 12.dp) {
                Text(ui("No crypto rates yet"), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.textDim)
            }
        } else {
            BentoCard(Modifier.testTag("dashboard_crypto_snapshot"), padding = 14.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        CryptoMetricTile(ui("Crypto"), "${visibleCrypto.size}", ui("major crypto assets"), Modifier.weight(1f).testTag("dashboard_crypto_count"))
                        CryptoMetricTile(ui("24H avg"), formatChange(cryptoAverageMove), strongestCrypto?.code, Modifier.weight(1f).testTag("dashboard_crypto_avg"))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        CryptoMetricTile(ui("Stablecoins"), "$stablecoinCount", "USDT / USDC", Modifier.weight(1f).testTag("dashboard_crypto_stablecoins"))
                        CryptoMetricTile(ui("Strongest"), strongestCrypto?.code ?: "--", strongestCrypto?.let { formatChange(it.change24h) }, Modifier.weight(1f).testTag("dashboard_crypto_strongest"))
                    }
                    Text(ui("live crypto movers"), style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
                }
            }
            BentoCard(Modifier.testTag("dashboard_crypto_list"), padding = 0.dp) {
                Column {
                    visibleCrypto.forEach { rate ->
                        CryptoAssetRow(rate, liveState.baseCurrency, onClick = { onOpenDetail(rate) })
                    }
                }
            }
            if (!subscriptionState.isPremium && liveState.crypto.size > visibleCrypto.size) {
                Box(Modifier.testTag("dashboard_crypto_upsell")) {
                    ProUpsellCard(
                        title = ui("Unlock full watchlists"),
                        subtitle = ui("Pro shows the full crypto board across compare, alerts and portfolio."),
                        onClick = onOpenPaywall,
                    )
                }
            }
        }
    }
}

private val DefaultCryptoCodes = listOf("BTC", "ETH", "USDT", "USDC")
private val StablecoinCodes = setOf("USDT", "USDC", "DAI", "BUSD", "PYUSD", "USDS")

@Composable
private fun CryptoMetricTile(
    label: String,
    value: String,
    sub: String?,
    modifier: Modifier = Modifier,
) {
    BentoTile(
        modifier = modifier.heightIn(min = 98.dp),
        padding = 14.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                label.uppercase(),
                style = FxTheme.typography.eyebrow,
                color = FxTheme.colors.textFaint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    value,
                    style = FxTheme.typography.numberBody,
                    color = FxTheme.colors.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    sub.orEmpty(),
                    style = FxTheme.typography.captionMono,
                    color = FxTheme.colors.textDim,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 15.sp,
                )
            }
        }
    }
}

@Composable
private fun CryptoAssetRow(rate: FxRate, baseCurrency: String, onClick: () -> Unit) {
    val inversePrice = if (rate.rate > 0.0) 1.0 / rate.rate else 0.0
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dashboard_crypto_${rate.code}")
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlagDot(rate.glyph, rate.kind, 34.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(rate.code, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                Text(rate.name, style = FxTheme.typography.caption, color = FxTheme.colors.textFaint, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text("$baseCurrency ${formatMoneyValue(inversePrice)} · ${ui("per coin")}", style = FxTheme.typography.captionMono, color = FxTheme.colors.textDim)
        }
        SparkLine(rate.sparkline, Modifier.size(64.dp, 26.dp), color = if (rate.change24h >= 0) FxTheme.colors.up else FxTheme.colors.down, showLastDot = true)
        Column(horizontalAlignment = Alignment.End, modifier = Modifier.widthIn(min = 64.dp)) {
            Text(formatCryptoAmount(rate.rate), style = FxTheme.typography.numberBody, color = FxTheme.colors.text)
            Spacer(Modifier.height(2.dp))
            Text(formatChange(rate.change24h), style = FxTheme.typography.captionMono, color = if (rate.change24h >= 0) FxTheme.colors.up else FxTheme.colors.down)
        }
    }
}

@Composable
private fun HeroRateCard(rate: FxRate, baseCurrency: String) {
    BentoCard(Modifier.fillMaxWidth().height(158.dp), padding = 14.dp) {
        GridBg(Modifier.matchParentSize().alpha(0.22f))
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FlagDot(rate.glyph, rate.kind, size = 28.dp)
                    Text("$baseCurrency → ${rate.code}", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                }
                Pill(ui("pinned"), variant = PillVariant.Ghost)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Text(formatRate(rate.rate), style = FxTheme.typography.numberXL.copy(fontSize = 44.sp, lineHeight = 44.sp), color = FxTheme.colors.text)
                Text(formatChange(rate.change24h), style = FxTheme.typography.numberBody, color = if (rate.change24h >= 0) FxTheme.colors.up else FxTheme.colors.down)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Eyebrow(ui("24H RANGE"))
                    Text("${formatRate(rate.sparkline.minOrNull()?.toDouble() ?: rate.rate)} — ${formatRate(rate.sparkline.maxOrNull()?.toDouble() ?: rate.rate)}", style = FxTheme.typography.captionMono, color = FxTheme.colors.textDim)
                }
                SparkLine(rate.sparkline, Modifier.size(108.dp, 38.dp), color = FxTheme.colors.accent, showLastDot = true)
            }
        }
    }
}

@Composable
fun ConverterScreen(
    liveState: LiveRatesState,
    subscriptionState: SubscriptionState,
    selectedCurrencyCodes: List<String> = emptyList(),
    onCurrencyCodesChange: (List<String>) -> Unit = {},
    onOpenPaywall: () -> Unit,
) {
    val access = subscriptionState.featureAccess()
    val focusManager = LocalFocusManager.current
    var showCurrencyPicker by remember { mutableStateOf(false) }
    val availableRates = remember(liveState.baseCurrency, liveState.favorites, liveState.compare, liveState.converter, liveState.allFiat, liveState.crypto, subscriptionState.isPremium) {
        liveState.converterAvailableRates(subscriptionState.isPremium)
    }
    val targetCodes = remember(liveState.baseCurrency, selectedCurrencyCodes, availableRates, access.converterCurrencyLimit) {
        converterTargetCodes(
            selectedCurrencyCodes = selectedCurrencyCodes,
            availableRates = availableRates,
            baseCurrency = liveState.baseCurrency,
            limit = access.converterCurrencyLimit,
        )
    }
    val rates = remember(liveState.baseCurrency, liveState.converter, availableRates, targetCodes) {
        val byCode = (availableRates + liveState.converter.ifEmpty { ConverterRates }).distinctBy { it.code }.associateBy { it.code }
        (listOfNotNull(byCode[liveState.baseCurrency]) + targetCodes.mapNotNull { byCode[it] })
            .distinctBy { it.code }
    }
    val initialTarget = remember(liveState.baseCurrency, rates) {
        rates.firstOrNull { it.code != liveState.baseCurrency }?.code ?: liveState.baseCurrency
    }
    var sourceCode by remember(liveState.baseCurrency) { mutableStateOf(liveState.baseCurrency) }
    var targetCode by remember(liveState.baseCurrency, initialTarget) { mutableStateOf(initialTarget) }
    var amountText by remember { mutableStateOf(sanitizeAmountInput(AppSettingsPrefs.converterAmountText())) }
    var amountFocused by remember { mutableStateOf(false) }
    var customFixedFeeText by remember { mutableStateOf("0") }
    var customFeePercentText by remember { mutableStateOf("1.00") }
    var customMarkupPercentText by remember { mutableStateOf("2.50") }
    val sourceRate = rates.firstOrNull { it.code == sourceCode }
        ?: rates.firstOrNull { it.code == liveState.baseCurrency }
        ?: rates.first()
    val targetRate = rates.firstOrNull { it.code == targetCode && it.code != sourceRate.code }
        ?: rates.firstOrNull { it.code != sourceRate.code }
        ?: sourceRate
    val amountValue = parseAmountInput(amountText)
    val customFee = CustomFeeInput(
        fixedFee = parseAmountInput(customFixedFeeText),
        feePercent = parseAmountInput(customFeePercentText),
        markupPercent = parseAmountInput(customMarkupPercentText),
    )
    val allFeeQuotes = estimatedFeeQuotes(sourceRate, targetRate, amountValue, customFee)
    val feeQuotes = if (access.canUseFullFeeComparison) {
        allFeeQuotes.take(EstimatedFeeQuoteCount)
    } else {
        allFeeQuotes.filter { it.provider in FreeFeeProviders }
    }
    val bestQuote = feeQuotes.minByOrNull { it.lossTargetValue }
    val worstQuote = feeQuotes.maxByOrNull { it.lossTargetValue }
    val customQuote = feeQuotes.firstOrNull { it.provider == "Custom" }
    val potentialSavings = bestQuote?.let { best ->
        worstQuote?.let { worst -> (worst.lossTargetValue - best.lossTargetValue).coerceAtLeast(0.0) }
    } ?: 0.0
    val timingInsight = remember(sourceRate, targetRate) { smartTimingInsight(sourceRate, targetRate) }
    if (showCurrencyPicker) {
        CurrencyListPickerSheet(
            title = ui("Edit converter list"),
            lockedSubtitle = ui("Pro unlocks more converter currencies"),
            currencies = availableRates.filterNot { it.code == liveState.baseCurrency },
            selectedCodes = targetCodes,
            limit = access.converterCurrencyLimit,
            isPremium = subscriptionState.isPremium,
            onDismiss = { showCurrencyPicker = false },
            onOpenPaywall = {
                showCurrencyPicker = false
                onOpenPaywall()
            },
            onApply = { codes ->
                showCurrencyPicker = false
                onCurrencyCodesChange(codes)
                if (targetCode !in codes && codes.isNotEmpty()) {
                    targetCode = codes.first()
                }
            },
        )
    }
    ScreenScaffold {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LiveDot()
            Eyebrow(ui("MID"), color = FxTheme.colors.accent)
            Text(
                localizedRuntimeLabel(liveState.updatedLabel),
                style = FxTheme.typography.captionMono,
                color = FxTheme.colors.textFaint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        ScreenHeader(ui("Convert"), subtitle = ui("Multi-currency · live to 4 decimals"))
        BentoCard(padding = 14.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Eyebrow(ui("YOU SEND"))
                    Pill(sourceRate.code, variant = PillVariant.Accent)
                }
                BasicTextField(
                    value = amountText,
                    onValueChange = { raw ->
                        amountText = sanitizeAmountInput(raw)
                        AppSettingsPrefs.setConverterAmountText(amountText)
                    },
                    singleLine = true,
                    textStyle = FxTheme.typography.numberXL.copy(color = FxTheme.colors.text, fontSize = 38.sp, lineHeight = 40.sp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(FxTheme.shapes.field)
                        .background(if (amountFocused) FxTheme.colors.accentSoft else FxTheme.colors.surface2)
                        .border(1.dp, if (amountFocused) FxTheme.colors.accentLine else FxTheme.colors.border, FxTheme.shapes.field)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                        .onFocusChanged { amountFocused = it.isFocused },
                    decorationBox = { innerTextField ->
                        if (amountText.isBlank()) {
                            Text(
                                "0.00",
                                style = FxTheme.typography.numberXL.copy(fontSize = 38.sp, lineHeight = 40.sp),
                                color = FxTheme.colors.textGhost,
                            )
                        }
                        innerTextField()
                    },
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("${ui("Converted to")} ${targetRate.code}", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
                    Text(
                        formatConvertedAmount(targetRate, convertedAmount(amountValue, sourceRate, targetRate)),
                        style = FxTheme.typography.numberBody,
                        color = FxTheme.colors.accent,
                    )
                }
            }
        }
        BentoCard(padding = 8.dp) {
            Column {
                rates.forEach { rate ->
                    ConverterRow(
                        rate = rate,
                        amount = if (rate.code == sourceRate.code) amountValue else convertedAmount(amountValue, sourceRate, rate),
                        selected = rate.code == targetRate.code,
                        source = rate.code == sourceRate.code,
                        onClick = {
                            if (rate.code != sourceRate.code) {
                                targetCode = rate.code
                                Observability.event(
                                    "converter_target_selected",
                                    mapOf("source" to sourceRate.code, "target" to rate.code),
                                )
                                focusManager.clearFocus()
                            }
                        },
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GhostButton(
                "⇄  ${ui("Reverse")}",
                Modifier.weight(1f),
                onClick = {
                    val previousSource = sourceRate
                    val previousTarget = targetRate
                    sourceCode = previousTarget.code
                    targetCode = previousSource.code
                    amountText = formatInputAmount(convertedAmount(amountValue, previousSource, previousTarget))
                    AppSettingsPrefs.setConverterAmountText(amountText)
                    Observability.event(
                        "converter_reversed",
                        mapOf("source" to previousSource.code, "target" to previousTarget.code),
                    )
                    focusManager.clearFocus()
                },
            )
            GhostButton("≡  ${ui("Edit list")}", Modifier.weight(1f).testTag("converter_edit_list"), onClick = { showCurrencyPicker = true })
        }
        SectionLabel("${ui("SMART TIMING")} · ${sourceRate.code} → ${targetRate.code}", right = if (subscriptionState.isPremium) ui("Pro") else ui("Preview"))
        SmartTimingCard(
            insight = timingInsight,
            isPremium = subscriptionState.isPremium,
            onOpenPaywall = onOpenPaywall,
        )
        SectionLabel("${ui("FEES")} · ${sourceRate.code} → ${targetRate.code}", right = if (access.canUseFullFeeComparison) ui("Estimated") else ui("Preview"))
        BentoCard(padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricTile(
                        ui("Best provider"),
                        bestQuote?.provider?.let { ui(it) } ?: "--",
                        bestQuote?.let { "${ui("Recipient gets")} ${it.amount}" },
                        Modifier.weight(1f).testTag("converter_best_provider"),
                    )
                    MetricTile(
                        ui("Potential savings"),
                        formatConvertedAmount(targetRate, potentialSavings),
                        ui("vs worst visible provider"),
                        Modifier.weight(1f).testTag("converter_provider_savings"),
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricTile(
                        ui("Mid-market value"),
                        formatConvertedAmount(targetRate, convertedAmount(amountValue, sourceRate, targetRate)),
                        ui("before fees and markup"),
                        Modifier.weight(1f).testTag("converter_mid_market_value"),
                    )
                    MetricTile(
                        ui("Best loss"),
                        bestQuote?.loss ?: "${targetRate.code} 0.00",
                        bestQuote?.provider?.let { ui(it) },
                        Modifier.weight(1f).testTag("converter_best_loss"),
                    )
                }
                bestQuote?.let {
                    KeyValueRow(
                        ui("Best route"),
                        "${ui(it.provider)} · ${ui("Recipient gets")} ${it.amount}",
                        "${ui("Loss vs mid-market")} ${it.loss} (${it.lossPercent})",
                        modifier = Modifier.testTag("converter_best_route"),
                    )
                }
                customQuote?.let {
                    KeyValueRow(ui("Your custom cost"), it.loss, "${ui("Effective rate")} ${it.effectiveRate}")
                }
            }
        }
        BentoCard(padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Eyebrow(ui("CUSTOM COST"))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FeeInputField(
                        label = ui("Fixed fee"),
                        value = customFixedFeeText,
                        suffix = sourceRate.code,
                        modifier = Modifier.weight(1f),
                        onValueChange = { customFixedFeeText = sanitizeAmountInput(it) },
                    )
                    FeeInputField(
                        label = ui("Fee %"),
                        value = customFeePercentText,
                        suffix = "%",
                        modifier = Modifier.weight(1f),
                        onValueChange = { customFeePercentText = sanitizeAmountInput(it) },
                    )
                    FeeInputField(
                        label = ui("FX markup"),
                        value = customMarkupPercentText,
                        suffix = "%",
                        modifier = Modifier.weight(1f),
                        onValueChange = { customMarkupPercentText = sanitizeAmountInput(it) },
                    )
                }
            }
        }
        BentoCard(padding = 0.dp) {
            Column { feeQuotes.forEachIndexed { index, quote -> FeeComparisonRow(quote, rank = index + 1) } }
        }
        if (!access.canUseFullFeeComparison) {
            ProUpsellCard(
                title = ui("See the real transfer cost"),
                subtitle = ui("Pro unlocks the complete provider list; estimates update with your amount."),
                onClick = onOpenPaywall,
            )
        }
    }
}

@Composable
private fun SmartTimingCard(
    insight: SmartTimingInsight,
    isPremium: Boolean,
    onOpenPaywall: () -> Unit,
) {
    BentoCard(Modifier.testTag("converter_smart_timing"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(ui(insight.signal), style = FxTheme.typography.bodyStrong, color = insight.color())
                    Text(insight.action, style = FxTheme.typography.caption, color = FxTheme.colors.textDim, modifier = Modifier.testTag("converter_timing_action"))
                }
                Box(
                    Modifier
                        .testTag("converter_timing_score")
                        .clip(FxTheme.shapes.field)
                        .background(FxTheme.colors.surface2)
                        .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("${insight.score}/100", style = FxTheme.typography.numberBody, color = insight.color(), textAlign = TextAlign.End)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                insight.horizons.take(if (isPremium) insight.horizons.size else 1).forEach { horizon ->
                    SmartTimingHorizonRow(horizon)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TimingUseCaseTile(ui("Travel"), insight.travelAdvice, Modifier.weight(1f).testTag("converter_timing_travel"))
                TimingUseCaseTile(ui("Savings"), insight.savingsAdvice, Modifier.weight(1f).testTag("converter_timing_savings"))
                TimingUseCaseTile(ui("Remit"), insight.remittanceAdvice, Modifier.weight(1f).testTag("converter_timing_remit"))
            }
            if (!isPremium) {
                GhostButton(
                    text = ui("Unlock 30d and 90d timing"),
                    modifier = Modifier.fillMaxWidth().testTag("converter_timing_upsell"),
                    onClick = onOpenPaywall,
                )
            }
        }
    }
}

@Composable
private fun SmartTimingHorizonRow(horizon: TimingHorizon) {
    Row(
        Modifier
            .fillMaxWidth()
            .testTag("converter_timing_${horizon.label.lowercase()}")
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(horizon.label, style = FxTheme.typography.captionMono, color = FxTheme.colors.textDim)
            Text(horizon.rangeLabel, style = FxTheme.typography.caption, color = FxTheme.colors.textFaint)
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(horizon.positionLabel, style = FxTheme.typography.captionMono, color = FxTheme.colors.text)
            Text("${ui("Trend")} ${horizon.trendLabel} · ${ui("Vol")} ${horizon.volatilityLabel}", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
        }
    }
}

@Composable
private fun TimingUseCaseTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface1)
            .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
            .padding(9.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(value, style = FxTheme.typography.caption, color = FxTheme.colors.text, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun SmartTimingInsight.color(): Color =
    when (signal) {
        "Strong rate" -> FxTheme.colors.up
        "Good time" -> FxTheme.colors.accent
        else -> FxTheme.colors.textDim
    }

@Composable
private fun ConverterRow(
    rate: FxRate,
    amount: Double,
    selected: Boolean,
    source: Boolean,
    onClick: () -> Unit,
) {
    val bg = when {
        selected -> FxTheme.colors.accentSoft
        source -> FxTheme.colors.surface2
        else -> Color.Transparent
    }
    val border = when {
        selected -> FxTheme.colors.accentLine
        source -> FxTheme.colors.border
        else -> Color.Transparent
    }
    val contentColor = when {
        selected -> FxTheme.colors.accent
        source -> FxTheme.colors.textDim
        else -> FxTheme.colors.text
    }
    Row(
        Modifier
            .fillMaxWidth()
            .testTag("converter_row_${rate.code}")
            .clip(FxTheme.shapes.field)
            .background(bg)
            .border(if (selected || source) 1.dp else 0.dp, border, FxTheme.shapes.field)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        FlagDot(rate.glyph, rate.kind, 32.dp)
        Column(Modifier.weight(1f)) {
            Text(rate.code, style = FxTheme.typography.bodyStrong, color = if (source) FxTheme.colors.textDim else FxTheme.colors.text)
            Text(
                if (source) ui("Base currency · source amount") else if (selected) ui("Selected destination") else localizedCurrencyName(rate.name),
                style = FxTheme.typography.caption,
                color = if (selected) FxTheme.colors.accent else FxTheme.colors.textFaint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            formatConvertedAmount(rate, amount),
            style = if (source || selected) FxTheme.typography.numberL.copy(fontSize = 24.sp) else FxTheme.typography.numberL,
            color = contentColor,
            maxLines = 1,
        )
    }
}

@Composable
private fun FeeComparisonRow(quote: EstimatedFeeQuote, rank: Int) {
    Column(
        Modifier
            .fillMaxWidth()
            .testTag("fee_quote_${quote.provider}")
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("#$rank", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(ui(quote.provider), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                    quote.badge?.let { Pill(ui(it), variant = if (quote.isHighFee) PillVariant.Down else PillVariant.Up) }
                }
                Text(
                    "${ui("Recipient gets")} ${quote.amount}",
                    style = FxTheme.typography.captionMono,
                    color = FxTheme.colors.accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    "${ui("Fee")} ${quote.fee} · ${ui("Markup")} ${quote.markup}",
                    style = FxTheme.typography.captionMono,
                    color = FxTheme.colors.textFaint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(quote.amount, style = FxTheme.typography.numberBody, color = FxTheme.colors.text, textAlign = TextAlign.End)
                Text("${ui("Lost")} ${quote.loss}", style = FxTheme.typography.captionMono, color = if (quote.lossTargetValue > 0.0) FxTheme.colors.down else FxTheme.colors.up)
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${ui("Effective rate")} ${quote.effectiveRate}", style = FxTheme.typography.captionMono, color = FxTheme.colors.textDim)
            Text("${ui("Loss vs mid-market")} ${quote.lossPercent}", style = FxTheme.typography.captionMono, color = FxTheme.colors.textDim)
        }
    }
}

@Composable
private fun FeeInputField(
    label: String,
    value: String,
    suffix: String,
    modifier: Modifier = Modifier,
    onValueChange: (String) -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint, maxLines = 1, overflow = TextOverflow.Ellipsis)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = FxTheme.typography.numberBody.copy(color = FxTheme.colors.text),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("fee_input_$label")
                .clip(FxTheme.shapes.field)
                .background(if (focused) FxTheme.colors.accentSoft else FxTheme.colors.surface2)
                .border(1.dp, if (focused) FxTheme.colors.accentLine else FxTheme.colors.border, FxTheme.shapes.field)
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .onFocusChanged { focused = it.isFocused },
            decorationBox = { innerTextField ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(Modifier.weight(1f)) {
                        if (value.isBlank()) {
                            Text("0", style = FxTheme.typography.numberBody, color = FxTheme.colors.textGhost)
                        }
                        innerTextField()
                    }
                    Text(suffix, style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
                }
            },
        )
    }
}

private const val EstimatedFeeQuoteCount = 8
private val FreeFeeProviders = setOf("Mid-market", "Custom")

private data class CustomFeeInput(
    val fixedFee: Double,
    val feePercent: Double,
    val markupPercent: Double,
)

private data class EstimatedFeeQuote(
    val provider: String,
    val badge: String?,
    val amount: String,
    val fee: String,
    val markup: String,
    val loss: String,
    val lossPercent: String,
    val effectiveRate: String,
    val lossTargetValue: Double,
    val isHighFee: Boolean = false,
)

private data class FeeProviderTemplate(
    val provider: String,
    val badge: String? = null,
    val fixedFee: Double = 0.0,
    val feePercent: Double = 0.0,
    val markupPercent: Double = 0.0,
)

private data class SmartTimingInsight(
    val score: Int,
    val signal: String,
    val action: String,
    val travelAdvice: String,
    val savingsAdvice: String,
    val remittanceAdvice: String,
    val horizons: List<TimingHorizon>,
)

private data class TimingHorizon(
    val label: String,
    val rangeLabel: String,
    val positionLabel: String,
    val trendLabel: String,
    val volatilityLabel: String,
    val position: Double,
    val trendPct: Double,
    val volatilityPct: Double,
)

private fun smartTimingInsight(sourceRate: FxRate, targetRate: FxRate): SmartTimingInsight {
    val pairSeries = pairRateSeries(sourceRate, targetRate)
    val horizons = listOf(
        timingHorizon("7D", pairSeries, 7),
        timingHorizon("30D", pairSeries, 30),
        timingHorizon("90D", pairSeries, 90),
    )
    val primary = horizons.first()
    val score = timingScore(primary)
    val signal = when {
        score >= 82 -> "Strong rate"
        score >= 58 -> "Good time"
        else -> "Wait"
    }
    val action = when (signal) {
        "Strong rate" -> "Convert now: the pair is near the top of its recent range."
        "Good time" -> "Convert in tranches: current rate is better than average but not stretched."
        else -> "Wait or set an alert: current rate is below its recent advantage zone."
    }
    return SmartTimingInsight(
        score = score,
        signal = signal,
        action = action,
        travelAdvice = when (signal) {
            "Wait" -> "Cover essentials only"
            "Good time" -> "Buy partial budget"
            else -> "Lock trip cash"
        },
        savingsAdvice = when (signal) {
            "Wait" -> "Use alerts"
            "Good time" -> "Average in"
            else -> "Move larger slice"
        },
        remittanceAdvice = when (signal) {
            "Wait" -> "Delay if flexible"
            "Good time" -> "Send staged"
            else -> "Send now"
        },
        horizons = horizons,
    )
}

private fun pairRateSeries(sourceRate: FxRate, targetRate: FxRate): List<Double> {
    val targetSeries = targetRate.sparkline.ifEmpty { listOf(targetRate.rate.toFloat()) }.map { it.toDouble() }
    val sourceSeries = sourceRate.sparkline.ifEmpty { listOf(sourceRate.rate.toFloat()) }.map { it.toDouble() }
    val points = maxOf(2, targetSeries.size, sourceSeries.size)
    return List(points) { index ->
        val target = targetSeries.valueAtScaledIndex(index, points, targetRate.rate)
        val source = sourceSeries.valueAtScaledIndex(index, points, sourceRate.rate)
        if (source == 0.0) 0.0 else target / source
    }
}

private fun List<Double>.valueAtScaledIndex(index: Int, total: Int, fallback: Double): Double {
    if (isEmpty()) return fallback
    if (size == 1 || total <= 1) return first()
    val scaled = (index.toDouble() / (total - 1).coerceAtLeast(1)) * (size - 1)
    return this[scaled.toInt().coerceIn(0, lastIndex)]
}

private fun timingHorizon(label: String, series: List<Double>, points: Int): TimingHorizon {
    val window = series.takeLast(points.coerceAtMost(series.size)).ifEmpty { series }
    val current = window.lastOrNull() ?: 0.0
    val open = window.firstOrNull() ?: current
    val high = window.maxOrNull() ?: current
    val low = window.minOrNull() ?: current
    val average = window.average().takeIf { !it.isNaN() } ?: current
    val spread = high - low
    val position = if (spread <= 0.0) 0.5 else ((current - low) / spread).coerceIn(0.0, 1.0)
    val trendPct = if (open == 0.0) 0.0 else ((current - open) / open) * 100.0
    val volatilityPct = if (average == 0.0) 0.0 else (spread / average) * 100.0
    return TimingHorizon(
        label = label,
        rangeLabel = "${formatRate(low)} - ${formatRate(high)}",
        positionLabel = "${(position * 100).toInt()}% of range",
        trendLabel = formatSignedPercent(trendPct),
        volatilityLabel = "${formatRate(volatilityPct)}%",
        position = position,
        trendPct = trendPct,
        volatilityPct = volatilityPct,
    )
}

private fun timingScore(horizon: TimingHorizon): Int {
    val positionScore = horizon.position * 72.0
    val trendScore = ((horizon.trendPct + 2.0) / 4.0).coerceIn(0.0, 1.0) * 20.0
    val volatilityPenalty = (horizon.volatilityPct / 12.0).coerceIn(0.0, 1.0) * 10.0
    return (positionScore + trendScore + 18.0 - volatilityPenalty).toInt().coerceIn(0, 100)
}

private fun estimatedFeeQuotes(
    sourceRate: FxRate,
    targetRate: FxRate,
    amount: Double,
    customFee: CustomFeeInput,
): List<EstimatedFeeQuote> {
    val safeAmount = amount.coerceAtLeast(0.0)
    val templates = listOf(
        FeeProviderTemplate("Mid-market", "best"),
        FeeProviderTemplate("Wise", fixedFee = 0.35, feePercent = 0.45),
        FeeProviderTemplate("Revolut", feePercent = 0.80, markupPercent = 0.15),
        FeeProviderTemplate("Card payment", feePercent = 0.30, markupPercent = 2.70),
        FeeProviderTemplate("ATM cash", fixedFee = 4.0, feePercent = 1.0, markupPercent = 3.00),
        FeeProviderTemplate("Bank transfer", "high fee", fixedFee = 5.0, feePercent = 0.80, markupPercent = 3.20),
        FeeProviderTemplate("Airport exchange", "avoid", markupPercent = 8.50),
        FeeProviderTemplate("Custom", fixedFee = customFee.fixedFee, feePercent = customFee.feePercent, markupPercent = customFee.markupPercent),
    )
    val midMarketTarget = convertedAmount(safeAmount, sourceRate, targetRate)
    val rawRate = if (sourceRate.rate == 0.0) 0.0 else targetRate.rate / sourceRate.rate

    return templates.map { template ->
        val variableFee = safeAmount * template.feePercent.coerceAtLeast(0.0) / 100.0
        val fixedFee = template.fixedFee.coerceAtLeast(0.0)
        val sourceFee = (fixedFee + variableFee).coerceAtMost(safeAmount)
        val netSource = (safeAmount - sourceFee).coerceAtLeast(0.0)
        val markupMultiplier = (1.0 - template.markupPercent.coerceIn(0.0, 99.0) / 100.0)
        val receivedTarget = netSource * rawRate * markupMultiplier
        val lossTarget = (midMarketTarget - receivedTarget).coerceAtLeast(0.0)
        val lossPct = if (midMarketTarget > 0.0) lossTarget / midMarketTarget * 100.0 else 0.0
        val effectiveRate = if (safeAmount > 0.0) receivedTarget / safeAmount else 0.0
        val highFee = lossPct >= 3.0 || template.badge in setOf("high fee", "avoid")
        EstimatedFeeQuote(
            provider = template.provider,
            badge = template.badge ?: when {
                lossPct == 0.0 -> "best"
                lossPct >= 6.0 -> "avoid"
                lossPct >= 3.0 -> "high fee"
                else -> null
            },
            amount = formatConvertedAmount(targetRate, receivedTarget),
            fee = "${sourceRate.code} ${formatMoneyValue(sourceFee)}",
            markup = "${formatRate(template.markupPercent)}%",
            loss = "${targetRate.code} ${formatMoneyValue(lossTarget)}",
            lossPercent = "${formatRate(lossPct)}%",
            effectiveRate = "${formatRate(effectiveRate)} ${targetRate.code}",
            lossTargetValue = lossTarget,
            isHighFee = highFee,
        )
    }.sortedWith(compareBy<EstimatedFeeQuote> { it.lossTargetValue }.thenBy { it.provider != "Custom" })
}

private fun convertedAmount(amount: Double, sourceRate: FxRate, targetRate: FxRate): Double =
    if (sourceRate.rate == 0.0) {
        0.0
    } else {
        amount / sourceRate.rate * targetRate.rate
    }

private fun formatConvertedAmount(rate: FxRate, amount: Double): String =
    "${rate.code} ${if (rate.kind == CurrencyKind.Crypto) formatCryptoAmount(amount) else formatMoneyValue(amount)}"

private fun formatCryptoAmount(value: Double): String =
    when {
        value <= 0.0 -> "0"
        value < 0.000001 -> "<0.000001"
        value < 1.0 -> formatRate(value)
        else -> formatMoneyValue(value)
    }

private fun formatInputAmount(value: Double): String =
    when {
        value <= 0.0 -> ""
        value >= 100.0 -> formatMoneyValue(value).replace(",", "")
        value >= 1.0 -> formatRate(value)
        else -> formatRate(value)
    }

private fun sanitizeAmountInput(value: String): String {
    val filtered = value.filter { it.isDigit() || it == '.' || it == ',' }.take(14)
    val decimalIndex = filtered.indexOfLast { it == '.' || it == ',' }
    if (decimalIndex < 0) return filtered
    val decimal = filtered[decimalIndex]
    val before = filtered.take(decimalIndex).filter { it.isDigit() }
    val after = filtered.drop(decimalIndex + 1).filter { it.isDigit() }
    return "$before$decimal$after"
}

@Composable
fun DetailScreen(
    liveState: LiveRatesState = LiveRatesState(),
    alertsState: AlertsState = AlertsState(),
    subscriptionState: SubscriptionState = SubscriptionState(isPremium = false),
    subscriptionReady: Boolean = true,
    detailState: DetailUiState = DetailUiState(),
    newsState: NewsUiState = NewsUiState(),
    rate: FxRate? = null,
    onBack: () -> Unit = {},
    onOpenPaywall: () -> Unit = {},
    onLoadHistory: (String, String, Period, List<Float>) -> Unit = { _, _, _, _ -> },
    onOpenUrl: (String) -> Unit = {},
    onOpenStory: (NewsStory) -> Unit = {},
    onCreateAlert: (FxRate) -> Unit = {},
) {
    var period by remember { mutableStateOf(Period.OneMonth) }
    val selected = rate ?: liveState.favorites.firstOrNull { it.code == "EUR" } ?: FavoriteRates.first()
    val activeForPair = alertsState.alerts.count { it.enabled && it.base == liveState.baseCurrency && it.quote == selected.code }
    val alertAccess = subscriptionState.featureAccess()
    val alertLabel = if (alertAccess.hasUnlimitedAlerts) {
        "${alertsState.activeCount} active"
    } else {
        "${alertsState.activeCount}/${alertAccess.alertLimit} active"
    }
    val fallbackSeries = if (selected.code == liveState.favorites.firstOrNull()?.code) liveState.detailSeries else selected.sparkline
    val detailMatches = detailState.base == liveState.baseCurrency && detailState.quote == selected.code && detailState.period == period
    val hasLoadedPeriodData = detailMatches && detailState.points.isNotEmpty()
    val isLoadingNewPeriod = detailMatches && detailState.isLoading && !hasLoadedPeriodData
    val chartData = remember(detailState.series, hasLoadedPeriodData, fallbackSeries, period) {
        if (hasLoadedPeriodData) detailState.series else fallbackSeries.seriesForPeriod(period)
    }
    var visibleChartData by remember(liveState.baseCurrency, selected.code) { mutableStateOf(chartData) }
    LaunchedEffect(chartData, isLoadingNewPeriod) {
        if (!isLoadingNewPeriod) {
            visibleChartData = chartData
        }
    }
    val stats = remember(visibleChartData) { visibleChartData.toDetailStats() }
    val effectivePremium = subscriptionState.isPremium || !subscriptionReady
    val periodIsPro = period == Period.OneYear || period == Period.All
    val historyCaption = if (detailMatches && detailState.points.isNotEmpty()) {
        "${detailState.provider} · ${detailState.points.size} pts · ${localizedRuntimeLabel(detailState.updatedLabel)}"
    } else {
        ui("cached preview")
    }
    LaunchedEffect(liveState.baseCurrency, selected.code, period, fallbackSeries, effectivePremium) {
        if (effectivePremium || !periodIsPro) {
            onLoadHistory(liveState.baseCurrency, selected.code, period, fallbackSeries)
        }
    }
    val relatedStories = remember(newsState.stories, selected.code) {
        newsState.stories.filter { story ->
            story.tag == selected.code || story.moves.any { it.first == selected.code }
        }
    }
    ScreenScaffold {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            BackNavButton(label = null, onClick = onBack)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Pill(if (activeForPair > 0) "🔔 $activeForPair ${ui("alert")}" else "★ ${ui("Watching")}")
                Pill(if (effectivePremium) ui("Pro") else ui("Free"))
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FlagDot(selected.glyph, selected.kind, size = 36.dp)
            Column {
                Text("${liveState.baseCurrency} / ${selected.code}", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                Text(localizedCurrencyName(selected.name), style = FxTheme.typography.caption, color = FxTheme.colors.textFaint)
            }
        }
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(formatRate(selected.rate), style = FxTheme.typography.numberXL, color = FxTheme.colors.text)
            Text(formatChange(selected.change24h), style = FxTheme.typography.numberBody, color = if (selected.change24h >= 0) FxTheme.colors.up else FxTheme.colors.down, modifier = Modifier.padding(bottom = 7.dp))
        }
        Text("${selected.caption?.let { ui(it) } ?: ui("mid-market")} · ${localizedRuntimeLabel(liveState.updatedLabel)}", style = FxTheme.typography.captionMono, color = FxTheme.colors.textDim)
        BentoCard(Modifier.testTag("detail_history_card")) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Eyebrow(if (detailState.isLoading && detailMatches) ui("LOADING HISTORY") else "${ui("HISTORY")} · ${period.label}")
                    Text(
                        historyCaption,
                        style = FxTheme.typography.captionMono,
                        color = FxTheme.colors.textFaint,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (isLoadingNewPeriod) {
                    DetailChartLoadingOverlay(visibleChartData, Modifier.fillMaxWidth().height(188.dp))
                } else {
                    PriceChart(visibleChartData, Modifier.fillMaxWidth().height(188.dp))
                }
                SegmentedPeriods(
                    period,
                    { next ->
                        if (!effectivePremium && (next == Period.OneYear || next == Period.All)) {
                            onOpenPaywall()
                        } else {
                            period = next
                        }
                    },
                    Modifier.fillMaxWidth(),
                )
                if (detailMatches && detailState.errorMessage != null) {
                    Text(ui("History unavailable · using cached preview"), style = FxTheme.typography.caption, color = FxTheme.colors.down)
                }
            }
        }
        if (periodIsPro && !effectivePremium) {
            ProUpsellCard(
                title = ui("Unlock long-range history"),
                subtitle = ui("Pro adds 1Y and all-time detail, full event context and deeper market overlays."),
                modifier = Modifier.testTag("detail_history_upsell"),
                onClick = onOpenPaywall,
            )
        }
        SectionLabel("${ui("STATISTICS")} · ${period.label}")
        BentoCard(Modifier.testTag("detail_statistics")) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                KeyValueRow(ui("Open"), formatRate(stats.open))
                KeyValueRow(ui("High"), formatRate(stats.high))
                KeyValueRow(ui("Low"), formatRate(stats.low))
                KeyValueRow(ui("Range"), "${formatRate(stats.low)} - ${formatRate(stats.high)}")
                KeyValueRow(ui("Volatility"), "${formatRate(stats.volatilityPct)}%")
                KeyValueRow(ui("Average"), formatRate(stats.average))
            }
        }
        SectionLabel(ui("RELATED NEWS"), right = if (newsState.isLoading) ui("Loading") else if (effectivePremium) ui("Live") else ui("Preview"))
        if (relatedStories.isEmpty()) {
            EmptyDetailSection(
                title = if (newsState.isLoading) ui("Loading related news") else ui("No related news"),
                subtitle = if (newsState.isLoading) {
                    "${ui("Fetching market headlines")} ${selected.code}"
                } else {
                    "${ui("No live headlines are currently tied to")} ${selected.code}."
                },
            )
        } else {
            relatedStories.take(if (effectivePremium) relatedStories.size else 2).forEach { story ->
                StoryCard(story, modifier = Modifier.testTag("detail_story_${story.safeTestTagKey()}"), onClick = { onOpenStory(story) })
            }
        }
        SectionLabel(ui("EVENTS · ANNOTATED"), right = if (effectivePremium) ui("Derived") else ui("Preview"))
        if (relatedStories.isEmpty()) {
            EmptyDetailSection(
                title = ui("No annotated events"),
                subtitle = "${ui("Events will appear here when stories include")} ${selected.code}.",
            )
        } else {
            BentoCard(padding = 0.dp) {
                Column {
                    relatedStories.take(if (effectivePremium) relatedStories.size else 2).forEach { story ->
                        DetailEventRow(story, modifier = Modifier.testTag("detail_event_${story.safeTestTagKey()}"), onOpenUrl = onOpenUrl)
                    }
                }
            }
        }
        GhostIconButton(
            icon = MoreFeatureIcon.Alerts,
            text = if (activeForPair > 0) "${ui("Add another alert")} ${selected.code} · $alertLabel" else "${ui("Alert me above")} ${formatRate(selected.rate * 1.01)} · $alertLabel",
            modifier = Modifier.fillMaxWidth().testTag("detail_alert_cta"),
            onClick = { onCreateAlert(selected) },
        )
    }
}

@Composable
private fun DetailChartLoadingPlaceholder(modifier: Modifier = Modifier) {
    val colors = FxTheme.colors
    Canvas(modifier = modifier) {
        val padX = 8.dp.toPx()
        val padTop = 16.dp.toPx()
        val padBottom = 18.dp.toPx()
        val chartH = size.height - padTop - padBottom
        repeat(4) { i ->
            val y = padTop + chartH * (i / 3f)
            var x = padX
            while (x < size.width - padX) {
                drawLine(
                    colors.border.copy(alpha = 0.72f),
                    Offset(x, y),
                    Offset((x + 4.dp.toPx()).coerceAtMost(size.width - padX), y),
                    strokeWidth = 1f,
                )
                x += 8.dp.toPx()
            }
        }
    }
}

@Composable
private fun DetailChartLoadingOverlay(data: List<Float>, modifier: Modifier = Modifier) {
    val colors = FxTheme.colors
    val transition = rememberInfiniteTransition()
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(1_100), repeatMode = RepeatMode.Restart),
    )
    Box(modifier) {
        PriceChart(data, Modifier.matchParentSize().alpha(0.46f))
        Canvas(Modifier.matchParentSize()) {
            val x = size.width * progress
            drawLine(
                colors.accent.copy(alpha = 0.48f),
                Offset(x, 16.dp.toPx()),
                Offset(x, size.height - 18.dp.toPx()),
                strokeWidth = 2.dp.toPx(),
            )
        }
    }
}

@Composable
private fun DetailEventRow(story: NewsStory, modifier: Modifier = Modifier, onOpenUrl: (String) -> Unit) {
    Row(
        modifier
            .fillMaxWidth()
            .clickable(enabled = story.sourceUrl.isNotBlank()) { onOpenUrl(story.sourceUrl) }
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(story.age, style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint, modifier = Modifier.width(58.dp))
        Pill(story.tag, variant = PillVariant.Accent)
        Text(story.title, style = FxTheme.typography.caption, color = FxTheme.colors.text, modifier = Modifier.weight(1f))
        if (story.sourceUrl.isNotBlank()) {
            Text("→", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
        }
    }
}

@Composable
private fun EmptyDetailSection(title: String, subtitle: String) {
    BentoCard(padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text(subtitle, style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
        }
    }
}

@Composable
fun CompareScreen(
    liveState: LiveRatesState,
    subscriptionState: SubscriptionState,
    selectedCurrencyCodes: List<String> = emptyList(),
    onCurrencyCodesChange: (List<String>) -> Unit = {},
    onOpenPaywall: () -> Unit,
    onOpenDetail: (FxRate) -> Unit,
) {
    val access = subscriptionState.featureAccess()
    var sortMode by remember { mutableStateOf(CompareSortMode.Movers) }
    var showCurrencyPicker by remember { mutableStateOf(false) }
    val availableRates = remember(liveState.baseCurrency, liveState.favorites, liveState.compare, liveState.converter, liveState.allFiat, liveState.crypto, subscriptionState.isPremium) {
        liveState.compareAvailableRates(subscriptionState.isPremium)
    }
    val selectedCodes = remember(liveState.baseCurrency, selectedCurrencyCodes, availableRates, access.compareLimit) {
        compareTargetCodes(selectedCurrencyCodes, availableRates, liveState.baseCurrency, access.compareLimit)
    }
    val compareRates = remember(selectedCodes, availableRates, sortMode) {
        val byCode = availableRates.associateBy { it.code }
        selectedCodes.mapNotNull { byCode[it] }.sortedForCompare(sortMode)
    }
    val bestRate = compareRates.maxByOrNull { it.change24h }
    val weakestRate = compareRates.minByOrNull { it.change24h }
    val averageAbsMove = if (compareRates.isEmpty()) 0.0 else compareRates.sumOf { kotlin.math.abs(it.change24h) } / compareRates.size
    val momentumSpread = if (bestRate != null && weakestRate != null) bestRate.change24h - weakestRate.change24h else 0.0
    val cryptoCount = compareRates.count { it.kind == CurrencyKind.Crypto }
    if (showCurrencyPicker) {
        CurrencyListPickerSheet(
            title = ui("Edit comparison"),
            lockedSubtitle = ui("Pro unlocks more comparison currencies"),
            currencies = availableRates.filterNot { it.code == liveState.baseCurrency },
            selectedCodes = selectedCodes,
            limit = access.compareLimit,
            isPremium = subscriptionState.isPremium,
            onDismiss = { showCurrencyPicker = false },
            onOpenPaywall = {
                showCurrencyPicker = false
                onOpenPaywall()
            },
            onApply = { codes ->
                showCurrencyPicker = false
                onCurrencyCodesChange(codes)
            },
        )
    }
    ScreenScaffold {
        ScreenHeader(
            ui("Compare"),
            sub = "${liveState.baseCurrency} ${ui("BASE")}",
            subtitle = "${compareRates.size} ${ui("currencies")} · ${ui(sortMode.label).lowercase()} · ${localizedRuntimeLabel(liveState.updatedLabel)}",
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CompareSortMode.entries.forEach { mode ->
                Pill(
                    ui(mode.label),
                    variant = if (mode == sortMode) PillVariant.Accent else PillVariant.Ghost,
                    modifier = Modifier
                        .testTag("compare_sort_${mode.name}")
                        .clickable { sortMode = mode },
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile(
                ui("STRONGEST"),
                bestRate?.code ?: "--",
                bestRate?.let { formatChange(it.change24h) } ?: ui("No data"),
                Modifier.weight(1f).height(76.dp),
            )
            MetricTile(
                ui("WEAKEST"),
                weakestRate?.code ?: "--",
                weakestRate?.let { formatChange(it.change24h) } ?: ui("No data"),
                Modifier.weight(1f).height(76.dp),
            )
        }
        BentoCard(Modifier.fillMaxWidth().testTag("compare_board"), padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Eyebrow(ui("COMPARE BOARD"))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetricTile(ui("Average move"), formatPercentValue(averageAbsMove) + "%", ui(sortMode.label), Modifier.weight(1f).height(76.dp))
                    MetricTile(ui("Momentum spread"), formatPercentValue(momentumSpread) + "%", "${bestRate?.code ?: "--"} / ${weakestRate?.code ?: "--"}", Modifier.weight(1f).height(76.dp))
                }
                KeyValueRow(ui("Asset mix"), "${compareRates.size} ${ui("currencies")} · $cryptoCount ${ui("crypto")}")
            }
        }
        if (compareRates.isEmpty()) {
            EmptyDetailSection(
                title = ui("No comparison currencies"),
                subtitle = "${ui("The saved list is unavailable for")} ${liveState.baseCurrency}. ${ui("Edit the comparison set to choose active currencies.")}",
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                compareRates.chunked(2).forEach { rowRates ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowRates.forEach { rate ->
                            CompareTile(
                                rate = rate,
                                baseCurrency = liveState.baseCurrency,
                                onOpenDetail = onOpenDetail,
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (rowRates.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            GhostButton("≡  ${ui("Edit comparison")}", Modifier.weight(1f).testTag("compare_edit_button"), onClick = { showCurrencyPicker = true })
            GhostButton("↗  ${ui("Open strongest")}", Modifier.weight(1f).testTag("compare_open_strongest"), onClick = { bestRate?.let(onOpenDetail) })
        }
        if (!subscriptionState.isPremium) {
            ProUpsellCard(
                title = ui("Compare every tracked currency"),
                subtitle = "${ui("Free compares")} ${access.compareLimit}; ${ui("Pro unlocks the full board and advanced overlays.")}",
                onClick = onOpenPaywall,
            )
        }
        if (compareRates.isNotEmpty()) {
            BentoCard(modifier = Modifier.testTag("compare_overlay"), padding = 12.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Eyebrow(ui("OVERLAY · 1M"))
                    OverlayChart(compareRates.take(4))
                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                        compareRates.take(4).forEachIndexed { index, rate ->
                            LegendDot(rate.code, compareOverlayColor(index, rate.kind))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompareTile(rate: FxRate, baseCurrency: String, onOpenDetail: (FxRate) -> Unit, modifier: Modifier = Modifier) {
    BentoTile(
        modifier = modifier
            .testTag("compare_tile_${rate.code}")
            .clickable { onOpenDetail(rate) },
        padding = 10.dp,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FlagDot(rate.glyph, rate.kind, 24.dp)
                    Text(rate.code, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                }
                Text(formatChange(rate.change24h), style = FxTheme.typography.captionMono, color = if (rate.change24h >= 0) FxTheme.colors.up else FxTheme.colors.down)
            }
            Text(formatRate(rate.rate), style = FxTheme.typography.numberL, color = FxTheme.colors.text)
            Text("${ui("per 1")} $baseCurrency", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
            SparkLine(rate.sparkline, Modifier.fillMaxWidth().height(30.dp))
        }
    }
}

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
) {
    val access = subscriptionState.featureAccess()
    val travelRates = remember(liveState.baseCurrency, liveState.favorites, liveState.compare, liveState.converter, liveState.allFiat) {
        liveState.portfolioRates().filterNot { it.code == liveState.baseCurrency }
    }
    val destinationLimit = if (access.canUseAdvancedTraveler) 12 else 8
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
    var budgetText by remember { mutableStateOf(if (budgetBase > 0.0) formatMoneyValue(budgetBase) else "") }
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
            subtitle = if (liveState.isLive) "${ui("Live")} ${liveState.baseCurrency} ${ui("rates")} · ${localizedRuntimeLabel(liveState.updatedLabel)}" else "${ui("Offline snapshot")} · ${liveState.baseCurrency} ${ui("base")}",
        )
        BentoCard(Modifier.fillMaxWidth().height(156.dp).testTag("traveler_hero"), padding = 14.dp) {
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
                    title = ui("More destinations"),
                    subtitle = if (access.canUseAdvancedTraveler) {
                        "${ui("Search")} ${travelRates.size} ${ui("supported live currencies")}"
                    } else {
                        "${ui("Free shows")} ${visibleDestinations.size}; ${ui("Pro unlocks every supported currency")}"
                    },
                    selected = false,
                    actionLabel = ui("more +"),
                    modifier = Modifier.testTag("traveler_more_destinations"),
                    onClick = {
                        if (access.canUseAdvancedTraveler) showDestinationPicker = true else onOpenPaywall()
                    },
                )
                if (!access.canUseAdvancedTraveler && travelRates.size > visibleDestinations.size) {
                    Text(ui("Free keeps the destination picker focused on the most common travel currencies."), style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
                }
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
        if (!access.canUseAdvancedTraveler) {
            ProUpsellCard(
                title = ui("Unlock full traveler mode"),
                subtitle = ui("Pro adds complete cheat sheets, offline context and more local money tips."),
                onClick = onOpenPaywall,
            )
        }
        SectionLabel(ui("LOCAL ETIQUETTE"))
        Row(Modifier.testTag("traveler_local_etiquette"), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile(ui("TIPPING"), destination.tipping, ui(destination.tippingNote), Modifier.weight(1f))
            MetricTile(ui("TAX"), ui(destination.tax), ui(destination.taxNote), Modifier.weight(1f))
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
        SectionLabel(ui("LOCAL PRICE GUIDE"), right = ui("Estimates"))
        BentoCard(Modifier.testTag("traveler_price_guide"), padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                destination.priceGuide.forEach { item ->
                    val basePrice = item.localAmount / selectedRate.rate
                    KeyValueRow(ui(item.label), "${destination.symbol}${formatMoneyValue(item.localAmount)} · ${liveState.baseCurrency} ${formatMoneyValue(basePrice)}")
                }
            }
        }
    }
}

private data class TravelerDestination(
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

private data class TravelerPriceGuide(
    val label: String,
    val localAmount: Double,
)

private fun travelerDestination(code: String): TravelerDestination =
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

@Composable
fun MoreScreen(
    subscriptionState: SubscriptionState,
    alertsCount: Int,
    watchlistCount: Int,
    onOpenAlerts: () -> Unit,
    onOpenWatchlist: () -> Unit,
    onOpenTraveler: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenNews: () -> Unit,
    onOpenPaywall: () -> Unit,
) {
    ScreenScaffold {
        ScreenHeader(ui("More"), sub = ui("TOOLS"), subtitle = ui("Travel, preferences and account"))
        BentoCard(padding = 8.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                MoreRow(
                    icon = MoreFeatureIcon.Traveler,
                    title = ui("Traveler"),
                    subtitle = ui("Local cheat sheets and offline rates"),
                    onClick = onOpenTraveler,
                )
                MoreRow(
                    icon = MoreFeatureIcon.News,
                    title = ui("News"),
                    subtitle = ui("Market stream and sentiment"),
                    onClick = onOpenNews,
                )
                MoreRow(
                    icon = MoreFeatureIcon.Alerts,
                    title = ui("Alerts"),
                    subtitle = "$alertsCount ${ui("active")} · ${ui("price targets and breakouts")}",
                    onClick = onOpenAlerts,
                )
                MoreRow(
                    icon = MoreFeatureIcon.Watchlist,
                    title = ui("Watchlist"),
                    subtitle = "$watchlistCount ${ui("currencies")} · ${ui("custom tracking")}",
                    onClick = onOpenWatchlist,
                )
                MoreRow(
                    icon = MoreFeatureIcon.Settings,
                    title = ui("Settings"),
                    subtitle = ui("Theme mode, base currency and version"),
                    onClick = onOpenSettings,
                )
                MoreRow(
                    icon = MoreFeatureIcon.Pro,
                    title = if (subscriptionState.isPremium) ui("FX/ Pro active") else ui("Upgrade to Pro"),
                    subtitle = subscriptionState.localizedProStatusLabel(),
                    onClick = onOpenPaywall,
                )
            }
        }
        SectionLabel(ui("COMING NEXT"))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricTile(ui("WIDGETS"), ui("Next"), ui("home screen and watch glance"), Modifier.weight(1f))
            MetricTile("PRO", if (subscriptionState.isPremium) ui("Active") else ui("Ready"), ui("monthly plan controls"), Modifier.weight(1f))
        }
    }
}

@Composable
fun AlertsScreen(
    liveState: LiveRatesState,
    alertsState: AlertsState,
    subscriptionState: SubscriptionState = SubscriptionState(isPremium = false),
    onBack: (() -> Unit)? = null,
    onOpenPaywall: () -> Unit = {},
    onCreateAlert: (FxRate) -> Unit = {},
    onCreateManualAlert: (FxRate, AlertDirection, Double, AlertKind) -> Unit = { _, _, _, _ -> },
    onResumeAlert: (String) -> Unit = {},
    onToggleAlert: (String) -> Unit = {},
    onDeleteAlert: (String) -> Unit = {},
    onTestAlert: (PriceAlert) -> Unit = {},
) {
    val access = subscriptionState.featureAccess()
    val canCreate = canCreateAlert(subscriptionState, alertsState.alerts.size)
	    val limitLabel = if (access.hasUnlimitedAlerts) ui("Unlimited") else "${alertsState.alerts.size}/${access.alertLimit}"
    val alertRates = remember(
        liveState.baseCurrency,
        liveState.favorites,
        liveState.compare,
        liveState.converter,
        liveState.allFiat,
        liveState.crypto,
        subscriptionState.isPremium,
    ) {
        liveState.alertRates(subscriptionState.isPremium)
    }
    val currentRatesByCode = remember(liveState.baseCurrency, alertRates) {
        alertRates.associateBy { it.code }
    }
    val smartSuggestions = remember(liveState.baseCurrency, alertRates, subscriptionState.isPremium) {
        smartAlertSuggestions(alertRates, subscriptionState.isPremium)
    }
    var selectedRateCode by remember(liveState.baseCurrency) { mutableStateOf(alertRates.firstOrNull()?.code ?: "EUR") }
    val selectedRate = alertRates.firstOrNull { it.code == selectedRateCode } ?: alertRates.firstOrNull() ?: FavoriteRates.first()
    val visibleAlertRates = remember(alertRates, selectedRate.code, subscriptionState.isPremium) {
        compactCurrencyChoices(alertRates, selectedRate.code, if (subscriptionState.isPremium) 8 else 4)
    }
    var showAlertCurrencyPicker by remember { mutableStateOf(false) }
    var selectedKind by remember { mutableStateOf(AlertKind.Target) }
    var selectedDirection by remember { mutableStateOf(AlertDirection.Above) }
    var targetText by remember(selectedRate.code, selectedDirection, selectedKind) {
        mutableStateOf(defaultAlertInput(selectedRate, selectedDirection, selectedKind))
    }
    val targetValue = parseAmountInput(targetText)
    val selectedDailyChange = selectedRate.change24h
    val matchingCustomAlert = alertsState.alerts.findMatchingAlert(
        baseCurrency = liveState.baseCurrency,
        quote = selectedRate.code,
        target = targetValue,
        direction = selectedDirection,
        kind = selectedKind,
    )
    val canCreateOrUpdate = canCreate || matchingCustomAlert != null
	    var customAlertFeedback by remember { mutableStateOf<String?>(null) }
    var customAlertError by remember { mutableStateOf<String?>(null) }
	    val existingAlertReactivatedCopy = ui("Existing alert reactivated")
	    val alertCreatedCopy = ui("alert created")
    val invalidTargetCopy = ui("Enter a target above 0")
	    LaunchedEffect(liveState.baseCurrency, selectedRate.code, selectedDirection, selectedKind, targetText) {
	        customAlertFeedback = null
        customAlertError = null
	    }
    if (showAlertCurrencyPicker) {
        CurrencyPickerSheet(
            title = ui("Choose alert pair"),
            subtitle = "${alertRates.size} ${ui("currencies")} · ${liveState.baseCurrency} ${ui("base")}",
            currencies = alertRates,
            selectedCode = selectedRate.code,
            onDismiss = { showAlertCurrencyPicker = false },
            onSelect = { code ->
                showAlertCurrencyPicker = false
                selectedRateCode = code
                alertRates.firstOrNull { it.code == code }?.let { rate ->
                    targetText = defaultAlertInput(rate, selectedDirection, selectedKind)
                }
            },
        )
    }
    ScreenScaffold {
        if (onBack != null) {
	            BackNavButton(label = ui("More"), onClick = onBack)
        }
	        ScreenHeader(ui("Alerts"), sub = ui("PRICE TARGETS"), subtitle = "$limitLabel ${ui("alerts")} · ${liveState.baseCurrency} ${ui("base")}")

        BentoCard(Modifier.fillMaxWidth().heightIn(min = 144.dp), padding = 14.dp) {
            GridBg(Modifier.matchParentSize().alpha(0.12f))
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
	                    Eyebrow(if (subscriptionState.isPremium) "FX/ PRO" else "FX/ FREE")
	                    Pill("${alertsState.activeCount} ${ui("active")}", variant = if (alertsState.activeCount > 0) PillVariant.Up else PillVariant.Ghost)
                }
	                Text(ui("Watch breakouts without watching charts."), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                Text(
	                    ui("Android checks every 15 min when online. iOS saves alerts now; push delivery is next."),
                    style = FxTheme.typography.caption,
                    color = FxTheme.colors.textDim,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        SectionLabel(ui("SMART ALERTS"), right = if (subscriptionState.isPremium) "FX/ PRO" else ui("Preview"))
        BentoCard(padding = 8.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (smartSuggestions.isEmpty()) {
                    Text(
                        ui("No smart alert signals yet"),
                        modifier = Modifier.fillMaxWidth().testTag("alert_smart_empty").padding(horizontal = 12.dp, vertical = 10.dp),
                        style = FxTheme.typography.captionMono,
                        color = FxTheme.colors.textFaint,
                    )
                } else {
                    smartSuggestions.forEach { suggestion ->
                        val existingSmartAlert = alertsState.alerts.findMatchingAlert(
                            baseCurrency = liveState.baseCurrency,
                            quote = suggestion.rate.code,
                            target = suggestion.target,
                            direction = suggestion.direction,
                            kind = suggestion.kind,
                        )
                        val canUseSuggestion = existingSmartAlert != null || canCreate
                        SmartAlertRow(
                            baseCurrency = liveState.baseCurrency,
                            suggestion = suggestion,
                            state = when {
                                existingSmartAlert?.enabled == true -> QuickAlertState.Active
                                existingSmartAlert != null -> QuickAlertState.Paused
                                canCreate -> QuickAlertState.Create
                                else -> QuickAlertState.Locked
                            },
                            enabled = canUseSuggestion,
                            onCreate = {
                                if (existingSmartAlert != null) {
                                    onResumeAlert(existingSmartAlert.id)
                                } else {
                                    onCreateManualAlert(suggestion.rate, suggestion.direction, suggestion.target, suggestion.kind)
                                }
                            },
                            onLocked = onOpenPaywall,
                        )
                    }
                }
            }
        }

        SectionLabel(ui("CUSTOM ALERT"))
        BentoCard(padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
	                Eyebrow("${liveState.baseCurrency} ${ui("PAIR")}")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    visibleAlertRates.chunked(2).forEach { rowRates ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowRates.forEach { rate ->
	                                AlertCurrencyChoice(
	                                    rate = rate,
	                                    selected = rate.code == selectedRate.code,
	                                    modifier = Modifier.clickable {
	                                        selectedRateCode = rate.code
	                                        targetText = defaultAlertInput(rate, selectedDirection, selectedKind)
	                                    }.weight(1f),
	                                )
                            }
                            if (rowRates.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
                GhostButton(
                    text = "≡  ${ui("Choose alert pair")}",
                    modifier = Modifier.fillMaxWidth().testTag("alert_choose_pair"),
                    onClick = { showAlertCurrencyPicker = true },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AlertKind.entries.forEach { kind ->
                        Pill(
	                            text = ui(kind.label),
	                            variant = if (kind == selectedKind) PillVariant.Accent else PillVariant.Ghost,
	                            modifier = Modifier
                                    .testTag("alert_kind_${kind.name}")
                                    .clickable {
	                                selectedKind = kind
	                                targetText = defaultAlertInput(selectedRate, selectedDirection, kind)
	                            },
	                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AlertDirection.entries.forEach { direction ->
                        Pill(
		                            text = ui(direction.label(selectedKind)),
	                            variant = if (direction == selectedDirection) PillVariant.Accent else PillVariant.Ghost,
	                            modifier = Modifier
                                    .testTag("alert_direction_${direction.name}")
                                    .clickable {
	                                selectedDirection = direction
	                                targetText = defaultAlertInput(selectedRate, direction, selectedKind)
	                            },
	                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    alertPresets.forEach { preset ->
                        Pill(
	                            text = preset.label,
	                            variant = PillVariant.Ghost,
	                            modifier = Modifier
                                    .testTag("alert_preset_${preset.label}")
                                    .clickable {
	                                selectedDirection = if (preset.percent >= 0.0) AlertDirection.Above else AlertDirection.Below
	                                targetText = if (selectedKind == AlertKind.Target) {
                                    formatRate(selectedRate.rate * (1.0 + preset.percent / 100.0))
                                } else {
                                    formatPercentValue(kotlin.math.abs(preset.percent))
                                }
                            },
                        )
                    }
                }
                AlertTargetField(
                    value = targetText,
                    onValueChange = { raw ->
                        targetText = raw.filter { it.isDigit() || it == '.' || it == ',' }.take(12)
                    },
                    pair = "${liveState.baseCurrency}/${selectedRate.code}",
		                    label = if (selectedKind == AlertKind.Target) ui("Target rate") else ui("Daily move %"),
	                )
	                PrimaryButton(
                    text = when {
	                        matchingCustomAlert?.enabled == true -> ui("Keep existing alert active")
	                        matchingCustomAlert != null -> ui("Reactivate existing alert")
	                        canCreate -> "${ui("Create")} ${ui(selectedDirection.label(selectedKind)).lowercase()} ${ui("alert")}"
		                        else -> ui("Unlock custom alerts")
	                    },
                    modifier = Modifier.fillMaxWidth().testTag("alert_create_button"),
	                    onClick = {
	                        if (!canCreateOrUpdate) {
	                            onOpenPaywall()
	                        } else if (targetValue > 0.0) {
	                            onCreateManualAlert(selectedRate, selectedDirection, targetValue, selectedKind)
	                            customAlertFeedback = if (matchingCustomAlert != null) {
		                                "$existingAlertReactivatedCopy ${liveState.baseCurrency}/${selectedRate.code}."
		                            } else {
			                                "${liveState.baseCurrency}/${selectedRate.code} $alertCreatedCopy."
		                            }
                                customAlertError = null
	                        } else {
                                customAlertError = invalidTargetCopy
	                        }
	                    },
	                )
                customAlertError?.let { error ->
                    Text(
                        error,
                        modifier = Modifier.testTag("alert_target_error"),
                        style = FxTheme.typography.captionMono,
                        color = FxTheme.colors.down,
                    )
                }
	                customAlertFeedback?.let { feedback ->
	                    Text(
	                        feedback,
                            modifier = Modifier.testTag("alert_feedback"),
	                        style = FxTheme.typography.captionMono,
	                        color = FxTheme.colors.accent,
                    )
                }
                Text(
                    localizedAlertSummaryLine(selectedKind, selectedRate, selectedDirection, targetValue, selectedDailyChange),
                    style = FxTheme.typography.captionMono,
                    color = FxTheme.colors.textFaint,
                )
            }
        }

	        SectionLabel(ui("QUICK CREATE"))
        BentoCard(padding = 8.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                liveState.favorites.take(4).forEach { rate ->
                    val quickAlert = alertsState.alerts.findQuickAlert(liveState.baseCurrency, rate)
                    val canCreateQuick = quickAlert != null || canCreate
                    AlertQuickRow(
                        baseCurrency = liveState.baseCurrency,
                        rate = rate,
                        state = when {
                            quickAlert?.enabled == true -> QuickAlertState.Active
                            quickAlert != null -> QuickAlertState.Paused
                            canCreate -> QuickAlertState.Create
                            else -> QuickAlertState.Locked
                        },
                        enabled = canCreateQuick,
                        onCreate = {
                            if (quickAlert != null) {
                                onResumeAlert(quickAlert.id)
                            } else {
                                onCreateAlert(rate)
                            }
                        },
                        onLocked = onOpenPaywall,
                    )
                }
            }
        }

        if (!canCreate) {
            ProUpsellCard(
	                title = ui("Create unlimited alerts"),
	                subtitle = "${ui("Free includes")} ${access.alertLimit}; ${ui("Pro unlocks every pair, range and breakout alert.")}",
                onClick = onOpenPaywall,
            )
        }

	        SectionLabel(ui("ACTIVE ALERTS"))
        if (alertsState.alerts.isEmpty()) {
            BentoCard(padding = 14.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
	                    Eyebrow(ui("NO ALERTS YET"))
	                    Text(ui("Create one from a favorite currency or from any detail screen."), style = FxTheme.typography.body, color = FxTheme.colors.textDim)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                alertsState.alerts.forEach { alert ->
                    val currentRate = currentRatesByCode[alert.quote]?.rate.takeIf { alert.base == liveState.baseCurrency }
                    AlertCard(
                        alert = alert,
                        currentRate = currentRate,
                        currentChangePct = currentRatesByCode[alert.quote]?.change24h.takeIf { alert.base == liveState.baseCurrency },
                        onToggle = onToggleAlert,
                        onDelete = onDeleteAlert,
                        onTest = onTestAlert,
                    )
                }
            }
        }
    }
}

@Composable
fun WatchlistScreen(
    liveState: LiveRatesState,
    watchlistState: WatchlistState,
    subscriptionState: SubscriptionState = SubscriptionState(isPremium = false),
    onBack: (() -> Unit)? = null,
    onOpenPaywall: () -> Unit = {},
    onToggleCurrency: (String) -> Unit = {},
    onSetHolding: (String, Double) -> Unit = { _, _ -> },
    onSetHoldingCost: (String, Double) -> Unit = { _, _ -> },
    onRecordTransaction: (String, PortfolioTransactionType, Double, Double) -> Unit = { _, _, _, _ -> },
    onImportPortfolioCsv: (String) -> PortfolioCsvImportResult = { watchlistState.watchlist.importPortfolioCsv(it) },
    onOpenDetail: (FxRate) -> Unit = {},
) {
    val access = subscriptionState.featureAccess()
    val allRates = remember(liveState.baseCurrency, liveState.favorites, liveState.compare, liveState.converter, liveState.crypto, subscriptionState.isPremium) {
        liveState.portfolioRates(subscriptionState.isPremium)
    }
	    val limitLabel = if (access.hasUnlimitedWatchlistCurrencies) ui("Unlimited") else "${watchlistState.watchlist.codes.size}/${access.watchlistCurrencyLimit}"
	    val holdings = remember(liveState.baseCurrency, allRates, watchlistState.watchlist) {
	        watchlistState.watchlist.codes.mapNotNull { code ->
	            val rate = allRates.firstOrNull { it.code == code } ?: return@mapNotNull null
	            PortfolioHolding(
	                rate = rate,
	                amount = watchlistState.watchlist.holdings[rate.code] ?: 0.0,
	                averageCostBase = watchlistState.watchlist.holdingCosts[rate.code] ?: 0.0,
	            )
	        }
	    }
    val valuedHoldings = holdings.filter { it.amount > 0.0 }
    val portfolioValue = valuedHoldings.sumOf { it.baseValue }
    val portfolioDailyChange = valuedHoldings.sumOf { it.dailyChangeInBase }
    val portfolioCostBasis = valuedHoldings.sumOf { it.costBasisBase }
    val portfolioUnrealizedPnl = valuedHoldings.sumOf { it.unrealizedPnlBase }
    val portfolioRealizedPnl = watchlistState.watchlist.transactions.sumOf { it.realizedPnlBase }
    val fiatValue = valuedHoldings.filter { it.rate.kind == CurrencyKind.Fiat }.sumOf { it.baseValue }
    val cryptoValue = valuedHoldings.filter { it.rate.kind == CurrencyKind.Crypto }.sumOf { it.baseValue }
    val largestHolding = valuedHoldings.maxByOrNull { it.baseValue }
    val portfolioSeries = remember(valuedHoldings) { valuedHoldings.portfolioValueSeries() }
    val nonZeroHoldings = holdings.count { it.amount > 0.0 }
    ScreenScaffold {
        if (onBack != null) {
	            BackNavButton(label = ui("More"), onClick = onBack)
        }
	        ScreenHeader(ui("Watchlist"), sub = ui("CUSTOM TRACKING"), subtitle = "$limitLabel ${ui("currencies")} · ${liveState.baseCurrency} ${ui("base")}")

        BentoCard(Modifier.fillMaxWidth().heightIn(min = 148.dp).testTag("watchlist_summary"), padding = 14.dp) {
            GridBg(Modifier.matchParentSize().alpha(0.12f))
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Eyebrow(if (subscriptionState.isPremium) "FX/ PRO" else "FX/ FREE")
	                    Pill("${holdings.size} ${ui("tracked")}", variant = if (holdings.isNotEmpty()) PillVariant.Accent else PillVariant.Ghost)
                }
	                Text(ui("Tracked currencies"), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                if (nonZeroHoldings == 0) {
	                    BigValueText("${holdings.size}", " ${ui("tracked")}")
                    Text(
	                        ui("Add amounts below to value your portfolio."),
                        style = FxTheme.typography.caption,
                        color = FxTheme.colors.textDim,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                } else {
                    BigValueText("${liveState.baseCurrency} ${formatMoneyValue(portfolioValue)}")
                    Text(
	                        "${formatPortfolioChange(portfolioDailyChange, liveState.baseCurrency)} ${ui("today")} · $nonZeroHoldings ${ui("holdings valued")}",
                        style = FxTheme.typography.caption,
                        color = if (portfolioDailyChange >= 0.0) FxTheme.colors.up else FxTheme.colors.down,
                    )
                    if (portfolioSeries.size >= 2) {
                        SparkLine(
                            portfolioSeries,
                            Modifier.fillMaxWidth().height(38.dp).testTag("watchlist_portfolio_chart"),
                            color = if ((portfolioSeries.last() - portfolioSeries.first()) >= 0f) FxTheme.colors.up else FxTheme.colors.down,
                            showLastDot = true,
                        )
                    }
                }
            }
        }

        if (subscriptionState.isPremium && nonZeroHoldings > 0) {
            SectionLabel(ui("PORTFOLIO INSIGHTS"))
            BentoCard(Modifier.testTag("watchlist_portfolio_insights"), padding = 12.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    KeyValueRow(
                        ui("Unrealized P&L"),
                        formatSignedMoney(portfolioUnrealizedPnl, liveState.baseCurrency),
                        portfolioPnlPercentLabel(portfolioUnrealizedPnl, portfolioCostBasis),
                        modifier = Modifier.testTag("watchlist_unrealized_pnl"),
                    )
                    KeyValueRow(
                        ui("Realized P&L"),
                        formatSignedMoney(portfolioRealizedPnl, liveState.baseCurrency),
                        "${watchlistState.watchlist.transactions.size} ${ui("transactions")}",
                        modifier = Modifier.testTag("watchlist_realized_pnl"),
                    )
                    KeyValueRow(
                        ui("Total P&L"),
                        formatSignedMoney(portfolioUnrealizedPnl + portfolioRealizedPnl, liveState.baseCurrency),
                        ui("realized + unrealized"),
                        modifier = Modifier.testTag("watchlist_total_pnl"),
                    )
                    KeyValueRow(
                        ui("Cost basis"),
                        "${liveState.baseCurrency} ${formatMoneyValue(portfolioCostBasis)}",
                        ui("average cost per asset"),
                        modifier = Modifier.testTag("watchlist_cost_basis"),
                    )
                    KeyValueRow(
                        ui("Allocation"),
                        "${ui("Fiat")} ${allocationLabel(fiatValue, portfolioValue)} · ${ui("Crypto")} ${allocationLabel(cryptoValue, portfolioValue)}",
                        modifier = Modifier.testTag("watchlist_allocation"),
                    )
                    KeyValueRow(
                        ui("Largest position"),
                        largestHolding?.rate?.code ?: "—",
                        largestHolding?.weightLabel(portfolioValue),
                        modifier = Modifier.testTag("watchlist_largest_position"),
                    )
                    if (portfolioSeries.size >= 2) {
                        KeyValueRow(
                            ui("Chart range"),
                            formatPortfolioSignedPercent(portfolioSeries.changePercent()),
                            ui("estimated from tracked assets"),
                            modifier = Modifier.testTag("watchlist_chart_range"),
                        )
                    }
                }
            }
        }

        if (subscriptionState.isPremium && holdings.isNotEmpty()) {
            SectionLabel(ui("TRANSACTION HISTORY"))
            PortfolioTransactionsCard(
                baseCurrency = liveState.baseCurrency,
                holdings = holdings,
                transactions = watchlistState.watchlist.transactions,
                onRecordTransaction = onRecordTransaction,
            )
        }

        if (subscriptionState.isPremium) {
            SectionLabel(ui("IMPORT / EXPORT"))
            PortfolioImportExportCard(
                watchlist = watchlistState.watchlist,
                onImportPortfolioCsv = onImportPortfolioCsv,
            )
        }

	        SectionLabel(ui("PORTFOLIO HOLDINGS"))
        if (holdings.isEmpty()) {
            BentoCard(padding = 14.dp) {
	                Text(ui("Choose currencies below to start tracking."), style = FxTheme.typography.body, color = FxTheme.colors.textDim)
            }
        } else {
            if (nonZeroHoldings == 0) {
                BentoCard(padding = 12.dp) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
	                        Eyebrow(ui("HOW IT WORKS"))
                        Text(
	                            ui("Watchlist follows rates. Portfolio value appears after you enter how much you hold."),
                            style = FxTheme.typography.caption,
                            color = FxTheme.colors.textDim,
                        )
                    }
                }
            }
            BentoCard(padding = 8.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    holdings.forEach { holding ->
                        PortfolioHoldingRow(
                            baseCurrency = liveState.baseCurrency,
                            holding = holding,
                            portfolioValue = portfolioValue,
                            canEditCostBasis = subscriptionState.isPremium,
                            onAmountChange = { amount -> onSetHolding(holding.rate.code, amount) },
                            onCostChange = { averageCost -> onSetHoldingCost(holding.rate.code, averageCost) },
                            onOpenDetail = { onOpenDetail(holding.rate) },
                        )
                    }
                }
            }
        }

	        SectionLabel(ui("ADD OR REMOVE"))
        BentoCard(padding = 8.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                allRates.forEach { rate ->
                    WatchlistCurrencyRow(
                        rate = rate,
                        selected = rate.code in watchlistState.watchlist.codes,
                        locked = rate.code !in watchlistState.watchlist.codes &&
                            !access.hasUnlimitedWatchlistCurrencies &&
                            watchlistState.watchlist.codes.size >= access.watchlistCurrencyLimit,
                        amount = watchlistState.watchlist.holdings[rate.code] ?: 0.0,
                        onToggle = { onToggleCurrency(rate.code) },
                    )
                }
            }
        }

        if (!access.hasUnlimitedWatchlistCurrencies && watchlistState.watchlist.codes.size >= access.watchlistCurrencyLimit) {
            ProUpsellCard(
	                title = ui("Track unlimited currencies"),
	                subtitle = "${ui("Free includes")} ${access.watchlistCurrencyLimit}; ${ui("Pro unlocks bigger watchlists across rates, alerts and portfolio tracking.")}",
                onClick = onOpenPaywall,
            )
        }
    }
}

@Composable
private fun PortfolioTransactionsCard(
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
private fun PortfolioImportExportCard(
    watchlist: com.fxalways.app.data.Watchlist,
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

@Composable
private fun CsvTextBox(
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
                Text(placeholder, style = FxTheme.typography.captionMono, color = FxTheme.colors.textGhost)
            }
            innerTextField()
        },
    )
}

@Composable
private fun TransactionChip(label: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(FxTheme.shapes.field)
            .background(if (selected) FxTheme.colors.accentSoft else FxTheme.colors.surface2)
            .border(1.dp, if (selected) FxTheme.colors.accent else FxTheme.colors.border, FxTheme.shapes.field)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = FxTheme.typography.captionMono, color = if (selected) FxTheme.colors.accent else FxTheme.colors.textDim)
    }
}

@Composable
private fun TransactionInputField(value: String, placeholder: String, modifier: Modifier = Modifier, onValueChange: (String) -> Unit) {
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
                Text(
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
private fun PortfolioTransactionRow(baseCurrency: String, transaction: PortfolioTransaction) {
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
            Text(
                "${ui(transaction.type.name)} ${transaction.code} ${formatMoneyValue(transaction.amount)}",
                style = FxTheme.typography.bodyStrong,
                color = FxTheme.colors.text,
            )
            Text(
                "${baseCurrency} ${formatMoneyValue(transaction.priceBase)} · ${localizedShortAgeLabel(transaction.createdAtMillis)}",
                style = FxTheme.typography.captionMono,
                color = FxTheme.colors.textFaint,
            )
        }
        Text(
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
private fun PortfolioHoldingRow(
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
            Text(
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
            Text(
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
                        Text(
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
                Text(
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
                            Text(
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
private fun WatchlistCurrencyRow(
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
            Text(rate.code, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text(
	                if (amount > 0.0) "${formatRate(amount)} ${ui("held")} · ${localizedCurrencyName(rate.name)}" else localizedCurrencyName(rate.name),
                style = FxTheme.typography.caption,
                color = FxTheme.colors.textFaint,
            )
        }
        Text(formatRate(rate.rate), style = FxTheme.typography.numberBody, color = FxTheme.colors.textDim)
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
private fun AlertTargetField(
    value: String,
    onValueChange: (String) -> Unit,
    pair: String,
    label: String,
) {
    val focusManager = LocalFocusManager.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2)
            .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("$label · $pair", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = FxTheme.typography.numberL.copy(color = FxTheme.colors.text),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.testTag("alert_target_input"),
                decorationBox = { innerTextField ->
                    if (value.isBlank()) {
                        Text("0.0000", style = FxTheme.typography.numberL, color = FxTheme.colors.textGhost)
                    }
                    innerTextField()
                },
            )
        }
    }
}

@Composable
private fun AlertCurrencyChoice(
    rate: FxRate,
    selected: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .testTag("alert_currency_${rate.code}")
            .heightIn(min = 54.dp)
            .clip(FxTheme.shapes.field)
            .background(if (selected) FxTheme.colors.accentSoft else FxTheme.colors.surface2)
            .border(
                1.dp,
                if (selected) FxTheme.colors.accentLine else FxTheme.colors.border,
                FxTheme.shapes.field,
            )
            .padding(horizontal = 10.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlagDot(rate.glyph, rate.kind, 26.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(rate.code, style = FxTheme.typography.bodyStrong, color = if (selected) FxTheme.colors.accent else FxTheme.colors.text)
            Text(
                localizedCurrencyName(rate.name),
                style = FxTheme.typography.caption,
                color = FxTheme.colors.textFaint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (selected) {
            Text("✓", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.accent)
        }
    }
}

@Composable
private fun AlertQuickRow(
    baseCurrency: String,
    rate: FxRate,
    state: QuickAlertState,
    enabled: Boolean,
    onCreate: () -> Unit,
    onLocked: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("alert_quick_${rate.code}")
            .clip(FxTheme.shapes.field)
            .clickable(onClick = if (enabled) onCreate else onLocked)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlagDot(rate.glyph, rate.kind, 28.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("$baseCurrency / ${rate.code}", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
	            Text("${ui("Above")} ${formatRate(rate.rate * 1.01)} · ${ui("current")} ${formatRate(rate.rate)}", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
        }
        Pill(ui(state.label), variant = state.variant)
    }
}

@Composable
private fun SmartAlertRow(
    baseCurrency: String,
    suggestion: SmartAlertSuggestion,
    state: QuickAlertState,
    enabled: Boolean,
    onCreate: () -> Unit,
    onLocked: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("alert_smart_${suggestion.rate.code}")
            .clip(FxTheme.shapes.field)
            .clickable(onClick = if (enabled) onCreate else onLocked)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        FlagDot(suggestion.rate.glyph, suggestion.rate.kind, 30.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                "$baseCurrency / ${suggestion.rate.code}",
                style = FxTheme.typography.bodyStrong,
                color = FxTheme.colors.text,
            )
            Text(
                ui(suggestion.title),
                style = FxTheme.typography.captionMono,
                color = FxTheme.colors.accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "${ui(suggestion.direction.label(suggestion.kind))} ${formatRate(suggestion.target)} · ${ui(suggestion.subtitle)}",
                style = FxTheme.typography.caption,
                color = FxTheme.colors.textFaint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Pill(ui(state.label), variant = state.variant)
            Text(suggestion.strengthLabel, style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
        }
    }
}

@Composable
private fun AlertCard(
    alert: PriceAlert,
    currentRate: Double?,
    currentChangePct: Double?,
    onToggle: (String) -> Unit,
    onDelete: (String) -> Unit,
    onTest: (PriceAlert) -> Unit,
) {
    val isHit = alert.isHit(currentRate, currentChangePct)
    BentoCard(modifier = Modifier.testTag("alert_card_${alert.id}"), padding = 12.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                FlagDot(if (alert.kind == AlertKind.Target) "◎" else "%", CurrencyKind.Fiat, 32.dp)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text("${alert.base} / ${alert.quote}", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                    Text(
	                        "${ui(alert.direction.label(alert.kind))} ${alert.targetLabel()} · ${localizedAlertStatusLabel(alert, currentRate, currentChangePct)}",
                        style = FxTheme.typography.captionMono,
                        color = if (isHit) FxTheme.colors.up else FxTheme.colors.textFaint,
                    )
                }
	                Pill(if (alert.enabled) ui("on") else ui("paused"), variant = if (alert.enabled) PillVariant.Up else PillVariant.Ghost)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                MetricTile(
	                    if (alert.kind == AlertKind.Target) ui("CURRENT") else ui("24H MOVE"),
                    if (alert.kind == AlertKind.Target) currentRate?.let(::formatRate) ?: "--" else currentChangePct?.let(::formatSignedPercent) ?: "--",
                    localizedAlertDistanceLabel(alert, currentRate, currentChangePct),
                    Modifier.weight(1f).height(72.dp),
                )
                MetricTile(
	                    ui("LAST HIT"),
	                    alert.lastTriggeredAtMillis?.let { localizedShortAgeLabel(it) } ?: ui("Never"),
	                    if (alert.enabled) ui("monitoring") else ui("paused"),
                    Modifier.weight(1f).height(72.dp),
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                Text(
		                    if (alert.enabled) ui("pause") else ui("resume"),
	                    style = FxTheme.typography.captionMono,
	                    color = FxTheme.colors.textDim,
	                    modifier = Modifier
                            .testTag("alert_toggle_${alert.id}")
                            .clickable { onToggle(alert.id) },
	                )
                Spacer(Modifier.width(14.dp))
                Text(
		                    ui("test"),
	                    style = FxTheme.typography.captionMono,
	                    color = FxTheme.colors.accent,
	                    modifier = Modifier
                            .testTag("alert_test_${alert.id}")
                            .clickable { onTest(alert) },
	                )
	                Spacer(Modifier.width(14.dp))
	                Text("×", style = FxTheme.typography.titleL, color = FxTheme.colors.textFaint, modifier = Modifier
                        .testTag("alert_delete_${alert.id}")
                        .clickable { onDelete(alert.id) })
            }
        }
    }
}

@Composable
private fun MoreRow(
    icon: MoreFeatureIcon,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FxTheme.shapes.field)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(FxTheme.shapes.icon)
                .background(FxTheme.colors.accentSoft),
            contentAlignment = Alignment.Center,
        ) {
            MoreFeatureIconView(icon)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text(subtitle, style = FxTheme.typography.caption, color = FxTheme.colors.textFaint)
        }
        Text("→", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
    }
}

private enum class MoreFeatureIcon {
    Traveler,
    News,
    Alerts,
    Watchlist,
    Settings,
    Pro,
}

@Composable
private fun MoreFeatureIconView(icon: MoreFeatureIcon) {
    val accent = FxTheme.colors.accent
    Canvas(Modifier.size(30.dp)) {
        val lineWidth = 2.2.dp.toPx()
        val stroke = Stroke(width = 2.4.dp.toPx(), cap = StrokeCap.Round)
        val thinStroke = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
        val w = size.width
        val h = size.height
        fun iconLine(startX: Float, startY: Float, endX: Float, endY: Float, strokeWidth: Float = lineWidth) {
            drawLine(
                color = accent,
                start = Offset(w * startX, h * startY),
                end = Offset(w * endX, h * endY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }

        when (icon) {
            MoreFeatureIcon.Traveler -> {
                iconLine(0.34f, 0.22f, 0.66f, 0.22f)
                iconLine(0.34f, 0.22f, 0.34f, 0.32f)
                iconLine(0.66f, 0.22f, 0.66f, 0.32f)
                drawRoundRect(
                    color = accent,
                    topLeft = Offset(w * 0.22f, h * 0.32f),
                    size = Size(w * 0.56f, h * 0.46f),
                    cornerRadius = CornerRadius(w * 0.08f, w * 0.08f),
                    style = stroke,
                )
                iconLine(0.36f, 0.44f, 0.36f, 0.66f, thinStroke.width)
                iconLine(0.64f, 0.44f, 0.64f, 0.66f, thinStroke.width)
            }
            MoreFeatureIcon.News -> {
                drawRoundRect(
                    color = accent,
                    topLeft = Offset(w * 0.24f, h * 0.16f),
                    size = Size(w * 0.52f, h * 0.68f),
                    cornerRadius = CornerRadius(w * 0.05f, w * 0.05f),
                    style = stroke,
                )
                iconLine(0.34f, 0.36f, 0.66f, 0.36f, thinStroke.width)
                iconLine(0.34f, 0.50f, 0.66f, 0.50f, thinStroke.width)
                iconLine(0.34f, 0.64f, 0.56f, 0.64f, thinStroke.width)
            }
            MoreFeatureIcon.Alerts -> {
                val bell = Path().apply {
                    moveTo(w * 0.31f, h * 0.60f)
                    quadraticTo(w * 0.32f, h * 0.34f, w * 0.50f, h * 0.30f)
                    quadraticTo(w * 0.68f, h * 0.34f, w * 0.69f, h * 0.60f)
                    lineTo(w * 0.76f, h * 0.70f)
                    lineTo(w * 0.24f, h * 0.70f)
                    close()
                }
                drawPath(bell, accent, style = stroke)
                iconLine(0.45f, 0.22f, 0.55f, 0.22f)
                drawArc(accent, 15f, 150f, false, Offset(w * 0.42f, h * 0.68f), Size(w * 0.16f, h * 0.16f), style = thinStroke)
            }
            MoreFeatureIcon.Watchlist -> {
                drawCircle(accent, radius = w * 0.30f, center = Offset(w * 0.50f, h * 0.50f), style = stroke)
                val chart = Path().apply {
                    moveTo(w * 0.30f, h * 0.58f)
                    lineTo(w * 0.43f, h * 0.46f)
                    lineTo(w * 0.52f, h * 0.54f)
                    lineTo(w * 0.70f, h * 0.36f)
                }
                drawPath(chart, accent, style = thinStroke)
                drawCircle(accent, radius = w * 0.035f, center = Offset(w * 0.70f, h * 0.36f))
            }
            MoreFeatureIcon.Settings -> {
                drawCircle(accent, radius = w * 0.17f, center = Offset(w * 0.50f, h * 0.50f), style = stroke)
                listOf(
                    Offset(w * 0.50f, h * 0.18f) to Offset(w * 0.50f, h * 0.28f),
                    Offset(w * 0.50f, h * 0.72f) to Offset(w * 0.50f, h * 0.82f),
                    Offset(w * 0.18f, h * 0.50f) to Offset(w * 0.28f, h * 0.50f),
                    Offset(w * 0.72f, h * 0.50f) to Offset(w * 0.82f, h * 0.50f),
                    Offset(w * 0.28f, h * 0.28f) to Offset(w * 0.35f, h * 0.35f),
                    Offset(w * 0.65f, h * 0.65f) to Offset(w * 0.72f, h * 0.72f),
                    Offset(w * 0.72f, h * 0.28f) to Offset(w * 0.65f, h * 0.35f),
                    Offset(w * 0.35f, h * 0.65f) to Offset(w * 0.28f, h * 0.72f),
                ).forEach { (start, end) ->
                    drawLine(accent, start, end, strokeWidth = lineWidth, cap = StrokeCap.Round)
                }
            }
            MoreFeatureIcon.Pro -> {
                drawCircle(accent, radius = w * 0.18f, center = Offset(w * 0.36f, h * 0.50f), style = stroke)
                drawCircle(accent, radius = w * 0.18f, center = Offset(w * 0.64f, h * 0.50f), style = stroke)
                iconLine(0.44f, 0.38f, 0.56f, 0.62f)
                iconLine(0.44f, 0.62f, 0.56f, 0.38f)
            }
        }
    }
}

@Composable
fun NewsScreen(
    newsState: NewsUiState = NewsUiState(),
    subscriptionState: SubscriptionState = SubscriptionState(isPremium = false),
    onRefresh: () -> Unit = {},
    onRegionSelected: (String) -> Unit = {},
    onCurrencySelected: (String) -> Unit = {},
    onOpenStory: (NewsStory) -> Unit = {},
    onOpenPaywall: () -> Unit = {},
) {
    val access = subscriptionState.featureAccess()
    var query by remember { mutableStateOf("") }
    var selectedTopic by remember { mutableStateOf("ALL") }
    val normalizedQuery = query.trim()
    val topicOptions = remember(newsState.stories) {
        (listOf("ALL") + newsState.stories.flatMap { story -> story.topics }.filter { it.isNotBlank() })
            .distinct()
            .take(8)
    }
    val filteredStories = remember(newsState.stories, query, newsState.selectedCurrency, selectedTopic) {
        newsState.stories.filter { story ->
            val matchesQuery = normalizedQuery.isBlank() ||
                story.title.contains(normalizedQuery, ignoreCase = true) ||
                story.summary.contains(normalizedQuery, ignoreCase = true) ||
                story.tag.contains(normalizedQuery, ignoreCase = true) ||
                story.topics.any { it.contains(normalizedQuery, ignoreCase = true) } ||
                story.moves.any { it.first.contains(normalizedQuery, ignoreCase = true) }
            val matchesCurrency = newsState.selectedCurrency.isBlank() ||
                newsState.selectedCurrency == "USD" ||
                story.moves.any { it.first == newsState.selectedCurrency } ||
                story.tag == newsState.selectedCurrency
            val matchesTopic = selectedTopic == "ALL" || story.topics.any { it == selectedTopic }
            matchesQuery && matchesCurrency && matchesTopic
        }
    }
    val visibleStories = filteredStories.take(access.newsStoryLimit.cap(filteredStories.size))
    val regionOptions = listOf("US", "AU", "GB", "EU", "BR", "MX", "JP")
    val currencyOptions = (newsState.trackedCurrencies + listOf("USD", "EUR", "GBP", "JPY", "AUD", "BTC")).distinct()
    val emptyCopy = newsEmptyCopy(
        hasBackendStories = newsState.stories.isNotEmpty(),
        hasQuery = normalizedQuery.isNotBlank(),
        topic = selectedTopic,
    )
    ScreenScaffold {
        ScreenHeader(
	            ui("News"),
	            sub = if (access.canUseAdvancedNews) ui("MARKET STREAM") else ui("MARKET PREVIEW"),
	            subtitle = "${newsState.provider} · ${newsState.region} · ${newsState.selectedCurrency} ${ui("focus")} · ${localizedRuntimeLabel(newsState.refreshedLabel)}",
            right = {
                Text(
                    if (newsState.isLoading) "…" else "↻",
                    style = FxTheme.typography.numberL,
                    color = if (newsState.isLoading) FxTheme.colors.accent else FxTheme.colors.textDim,
                    modifier = Modifier.clickable(enabled = !newsState.isLoading, onClick = onRefresh),
                )
            },
        )
        BentoCard(padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
	                    Eyebrow(ui("SENTIMENT"))
                    if (newsState.isLoading) {
	                        Eyebrow(ui("REFRESHING"), color = FxTheme.colors.accent)
                    }
                }
                SentimentBar(newsState.bullish, newsState.neutral, newsState.bearish)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
	                    LegendDot("${ui("BULLISH")} ${newsState.bullish}%", FxTheme.colors.up)
	                    LegendDot("${ui("NEUTRAL")} ${newsState.neutral}%", FxTheme.colors.textGhost)
	                    LegendDot("${ui("BEARISH")} ${newsState.bearish}%", FxTheme.colors.down)
                }
	                KeyValueRow(ui("Feed"), "${newsState.language.uppercase()} · ${newsState.trackedCurrencies.joinToString(", ")}")
	                KeyValueRow(ui("Updated"), "${newsState.provider} · ${localizedRuntimeLabel(newsState.refreshedLabel)}")
            }
        }
        BentoCard(padding = 10.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                NewsSearchField(query = query, onQueryChange = { query = it })
                NewsFilterRow(
	                    label = ui("REGION"),
                    options = regionOptions,
                    selected = newsState.region,
                    enabled = access.canUseAdvancedNews,
                    onSelect = { region ->
                        if (access.canUseAdvancedNews) onRegionSelected(region) else onOpenPaywall()
                    },
                )
                NewsFilterRow(
	                    label = ui("CURRENCY"),
                    options = currencyOptions,
                    selected = newsState.selectedCurrency,
                    enabled = access.canUseAdvancedNews,
                    onSelect = { code ->
                        if (access.canUseAdvancedNews) onCurrencySelected(code) else onOpenPaywall()
                    },
                )
                NewsFilterRow(
	                    label = ui("TOPIC"),
                    options = topicOptions,
                    selected = selectedTopic,
                    enabled = access.canUseAdvancedNews,
                    onSelect = { topic ->
                        if (access.canUseAdvancedNews || topic == selectedTopic) {
                            selectedTopic = topic
                        } else {
                            onOpenPaywall()
                        }
                    },
                )
            }
        }
	        SectionLabel("${ui("RECENT LINES")} · ${filteredStories.size}")
        if (visibleStories.isEmpty()) {
            BentoCard(modifier = Modifier.fillMaxWidth(), padding = 12.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
	                    Text(ui(emptyCopy.first), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
	                    Text(ui(emptyCopy.second), style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
                    if (newsState.isLoading) {
	                        Text(ui("Refreshing market stream…"), style = FxTheme.typography.captionMono, color = FxTheme.colors.accent)
                    }
                }
            }
        }
        visibleStories.forEach { story ->
            StoryCard(story, onClick = { onOpenStory(story) })
        }
        if (!access.canUseAdvancedNews || visibleStories.size < filteredStories.size) {
            ProUpsellCard(
	                title = ui("Personalize the market stream"),
                subtitle = if (visibleStories.size < filteredStories.size) {
	                    "${ui("Showing")} ${visibleStories.size}/${filteredStories.size} ${ui("stories")}. ${ui("Pro unlocks the full regional stream.")}"
                } else {
	                    ui("Pro unlocks more stories and filters by region, currencies and topics.")
                },
                onClick = onOpenPaywall,
            )
        }
    }
}

@Composable
fun NewsDetailScreen(
    story: NewsStory?,
    onBack: () -> Unit = {},
    onOpenUrl: (String) -> Unit = {},
) {
    val selected = story ?: NewsStory(
        tag = "FX",
        impact = "MED",
        age = "Now",
	        title = ui("Market update"),
	        summary = ui("Latest currency market context."),
        moves = emptyList(),
        source = "FX Always",
        sourceUrl = "",
    )
    val impactColor = if (selected.impact.startsWith("HIGH")) FxTheme.colors.down else FxTheme.colors.accent
    ScreenScaffold {
	        BackNavButton(label = ui("News"), onClick = onBack)
        ScreenHeader(
	            ui("News detail"),
            sub = "${selected.tag} · ${selected.impact}",
	            subtitle = "${selected.source.ifBlank { ui("Market source") }} · ${selected.age}",
        )
        BentoCard(padding = 14.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Pill(selected.tag, variant = PillVariant.Accent)
                    Eyebrow(selected.impact, color = impactColor)
                }
                Text(selected.title, style = FxTheme.typography.titleL, color = FxTheme.colors.text)
                Text(selected.summary, style = FxTheme.typography.body, color = FxTheme.colors.textDim)
            }
        }
	        SectionLabel(ui("MARKET MOVES"))
        BentoCard(padding = 12.dp) {
            if (selected.moves.isEmpty()) {
	                Text(ui("No direct currency move was detected for this story."), style = FxTheme.typography.caption, color = FxTheme.colors.textFaint)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    selected.moves.forEach { (code, change) ->
                        KeyValueRow(code, formatChange(change))
                    }
                }
            }
        }
	        SectionLabel(ui("SOURCE"))
        BentoCard(padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
	                KeyValueRow(ui("Publisher"), selected.source.ifBlank { ui("Market source") })
	                KeyValueRow(ui("Published"), selected.age)
                if (selected.sourceUrl.isNotBlank()) {
	                    GhostButton(ui("Open original source"), onClick = { onOpenUrl(selected.sourceUrl) })
                } else {
	                    Text(ui("This item is generated from the fallback market brief, so there is no external article link."), style = FxTheme.typography.caption, color = FxTheme.colors.textFaint)
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    appLanguage: String,
    baseCurrency: String,
    userProfile: UserProfile = UserProfile.Traveler,
    availableBaseCurrencies: List<FxRate> = SettingsBaseCurrencies,
    backupState: UserBackupState,
    backupSyncing: Boolean,
    lastSyncedAtMillis: Long?,
    subscriptionState: SubscriptionState,
    onBack: (() -> Unit)? = null,
    onOpenPaywall: () -> Unit,
    onOpenUrl: (String) -> Unit,
    onRestorePurchase: () -> Unit,
    onSyncNow: () -> Unit,
    onLinkGoogle: () -> Unit,
    onSignOut: () -> Unit,
    onDevPremiumChange: (Boolean) -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onLanguageChange: (String) -> Unit,
    onBaseCurrencyChange: (String) -> Unit,
    onUserProfileChange: (UserProfile) -> Unit = {},
) {
    val copy = settingsCopy(appLanguage)
    val activeLanguage = SupportedLanguages.firstOrNull { it.code == appLanguage }
        ?: SupportedLanguages.first()
    val access = subscriptionState.featureAccess()
    val fullBaseCurrencies = availableBaseCurrencies.ifEmpty { SettingsBaseCurrencies }
    val canUseAllBaseCurrencies = access.hasUnlimitedBaseCurrencies
    val baseCurrencyLimit = if (canUseAllBaseCurrencies) 12 else access.baseCurrencyLimit.cap(fullBaseCurrencies.size)
    val baseCurrencies = remember(fullBaseCurrencies, baseCurrency, baseCurrencyLimit) {
        compactCurrencyChoices(fullBaseCurrencies, baseCurrency, baseCurrencyLimit)
    }
    var showBaseCurrencyPicker by remember { mutableStateOf(false) }
    if (showBaseCurrencyPicker) {
        CurrencyPickerSheet(
	            title = ui("Choose base currency"),
	            subtitle = "${fullBaseCurrencies.size} ${ui("supported live currencies")}",
            currencies = fullBaseCurrencies,
            selectedCode = baseCurrency,
            onDismiss = { showBaseCurrencyPicker = false },
            onSelect = { code ->
                showBaseCurrencyPicker = false
                onBaseCurrencyChange(code)
            },
        )
    }
    ScreenScaffold {
        if (onBack != null) {
            BackNavButton(label = copy.more, onClick = onBack)
        }
        ScreenHeader(copy.title, sub = copy.sub, subtitle = "${copy.activeLanguage}: ${activeLanguage.label} · ${copy.deviceLanguage}: ${DeviceLocale.language.uppercase()}")

        SectionLabel(copy.backup)
        BentoCard(padding = 8.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                AccountBackupCard(
                    backupState = backupState,
                    lastSyncedAtMillis = lastSyncedAtMillis,
                    backupSyncing = backupSyncing,
                    modifier = Modifier.testTag("settings_backup_card"),
                    onClick = onSyncNow,
                )
                SettingChoiceRow(
                    title = copy.syncNow,
                    subtitle = copy.syncNowSubtitle,
                    selected = false,
                    actionLabel = if (backupSyncing) copy.syncing else copy.sync,
                    modifier = Modifier.testTag("settings_sync_now"),
                    onClick = onSyncNow,
                )
                if (backupState.isAnonymous) {
                    val providerLabel = when (PlatformConfig.platform) {
                        Platform.Android -> "Google"
                        Platform.Ios -> "Apple"
                    }
                    val deviceLabel = when (PlatformConfig.platform) {
                        Platform.Android -> "Android phone"
                        Platform.Ios -> "iPhone"
                    }
                    SettingChoiceRow(
                        title = "${copy.signInWith} $providerLabel",
                        subtitle = "${copy.signInSubtitle} $deviceLabel",
                        selected = false,
                        actionLabel = copy.connect,
                        modifier = Modifier.testTag("settings_link_account"),
                        onClick = onLinkGoogle,
                    )
                } else {
                    SettingChoiceRow(
                        title = copy.signOut,
                        subtitle = copy.signOutSubtitle,
                        selected = false,
                        actionLabel = copy.signOutAction,
                        modifier = Modifier.testTag("settings_sign_out"),
                        onClick = onSignOut,
                    )
                }
            }
        }

        SectionLabel(copy.subscription)
        BentoCard(padding = 8.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SettingChoiceRow(
                    title = if (subscriptionState.isPremium) ui("FX/ Pro active") else ui("FX/ Free"),
                    subtitle = subscriptionState.statusMessage?.let { localizedSubscriptionMessage(it) } ?: subscriptionState.localizedProStatusLabel(),
                    selected = subscriptionState.isPremium,
                    actionLabel = if (subscriptionState.isPremium) copy.view else copy.upgrade,
                    modifier = Modifier.testTag("settings_subscription"),
                    onClick = onOpenPaywall,
                )
                SettingChoiceRow(
                    title = copy.restorePurchase,
                    subtitle = copy.restorePurchaseSubtitle,
                    selected = false,
                    actionLabel = copy.restore,
                    modifier = Modifier.testTag("settings_restore_purchase"),
                    onClick = onRestorePurchase,
                )
                SettingChoiceRow(
                    title = copy.manageSubscription,
                    subtitle = copy.manageSubscriptionSubtitle,
                    selected = false,
                    actionLabel = copy.open,
                    modifier = Modifier.testTag("settings_manage_subscription"),
                    onClick = { onOpenUrl(subscriptionManagementUrl()) },
                )
            }
        }

        SectionLabel(copy.notifications)
        BentoCard(padding = 8.dp) {
            SettingChoiceRow(
                title = copy.priceAlertNotifications,
                subtitle = ui(NotificationPermissionStatus.subtitle),
                selected = false,
                actionLabel = ui(NotificationPermissionStatus.actionLabel),
                modifier = Modifier.testTag("settings_notifications"),
                onClick = {},
            )
        }

        SectionLabel(ui("Profile"))
        BentoCard(padding = 8.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                UserProfile.entries.forEach { profile ->
                    val profileCopy = profile.copy()
                    SettingChoiceRow(
                        title = ui(profileCopy.label),
                        subtitle = ui(profileCopy.subtitle),
                        selected = userProfile == profile,
                        modifier = Modifier.testTag("settings_profile_${profile.name}"),
                        onClick = { onUserProfileChange(profile) },
                    )
                }
            }
        }

        SectionLabel(copy.themeMode)
        BentoCard(padding = 8.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ThemeMode.entries.forEach { mode ->
                    SettingChoiceRow(
	                        title = ui(mode.label),
	                        subtitle = ui(mode.subtitle),
                        selected = themeMode == mode,
                        modifier = Modifier.testTag("settings_theme_${mode.name}"),
                        onClick = { onThemeModeChange(mode) },
                    )
                }
            }
        }

        SectionLabel(copy.language)
        BentoCard(padding = 8.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SettingChoiceRow(
                    title = copy.activeLanguage,
                    subtitle = "${activeLanguage.label} · ${copy.languageApplied}",
                    selected = true,
                    actionLabel = appLanguage.uppercase(),
                    onClick = {},
                )
                SupportedLanguages.forEach { language ->
                    SettingChoiceRow(
                        title = language.label,
                        subtitle = if (language.code == DeviceLocale.language) copy.deviceLanguage else language.code.uppercase(),
                        selected = appLanguage == language.code,
                        modifier = Modifier.testTag("settings_language_${language.code}"),
                        onClick = { onLanguageChange(language.code) },
                    )
                }
            }
        }

        SectionLabel(copy.baseCurrency)
        BentoCard(padding = 8.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                baseCurrencies.forEach { currency ->
                    SettingChoiceRow(
                        title = "${currency.glyph}  ${currency.code}",
                        subtitle = localizedCurrencyName(currency.name),
                        selected = baseCurrency == currency.code,
                        modifier = Modifier.testTag("settings_base_${currency.code}"),
                        onClick = { onBaseCurrencyChange(currency.code) },
                    )
                }
                SettingChoiceRow(
                    title = copy.moreCurrencies,
                    subtitle = if (canUseAllBaseCurrencies) {
                        "${copy.search} ${fullBaseCurrencies.size}"
                    } else {
                        "${copy.freeIncludes} ${baseCurrencies.size}; Pro ${copy.unlocks} ${fullBaseCurrencies.size}"
                    },
                    selected = false,
	                    actionLabel = ui("more +"),
                    modifier = Modifier.testTag("settings_more_base_currencies"),
                    onClick = {
                        if (canUseAllBaseCurrencies) showBaseCurrencyPicker = true else onOpenPaywall()
                    },
                )
            }
        }
        if (!canUseAllBaseCurrencies && baseCurrencies.size < fullBaseCurrencies.size) {
            ProUpsellCard(
                title = copy.unlockAllBaseCurrencies,
                subtitle = "${copy.freeIncludes} ${baseCurrencies.size}; Pro ${copy.unlocks} ${fullBaseCurrencies.size} ${copy.supportedBaseCurrencies}.",
                onClick = onOpenPaywall,
            )
        }

        if (PlatformConfig.isDebug) {
	            SectionLabel(ui("DEV"))
            BentoCard(padding = 8.dp) {
                SettingChoiceRow(
	                    title = "${ui("Simulate")} ${if (subscriptionState.isPremium) ui("Free") else ui("Pro")}",
	                    subtitle = ui("Debug-only local gate override"),
                    selected = subscriptionState.isPremium,
	                    actionLabel = if (subscriptionState.isPremium) ui("set free") else ui("set pro"),
                    modifier = Modifier.testTag("settings_dev_premium"),
                    onClick = { onDevPremiumChange(!subscriptionState.isPremium) },
                )
            }
        }

        SectionLabel(copy.legal)
        BentoCard(padding = 8.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SettingChoiceRow(
                    title = copy.privacyPolicy,
                    subtitle = copy.privacyPolicySubtitle,
                    selected = false,
                    actionLabel = copy.open,
                    modifier = Modifier.testTag("settings_privacy_policy"),
                    onClick = { onOpenUrl(privacyPolicyUrl(appLanguage)) },
                )
                SettingChoiceRow(
                    title = copy.termsOfUse,
                    subtitle = copy.termsOfUseSubtitle,
                    selected = false,
                    actionLabel = copy.open,
                    modifier = Modifier.testTag("settings_terms_of_use"),
                    onClick = { onOpenUrl(termsOfUseUrl(appLanguage)) },
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
	            "${ui("Version")} ${PlatformConfig.versionName}",
            style = FxTheme.typography.captionMono,
            color = FxTheme.colors.textFaint,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AccountBackupCard(
    backupState: UserBackupState,
    lastSyncedAtMillis: Long?,
    backupSyncing: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val signedIn = backupState.isAvailable && !backupState.isAnonymous
    val title = if (signedIn) {
	        "${ui("Signed in with")} ${backupState.providerLabel ?: ui("account")}"
    } else {
	        ui(backupState.title)
    }
    val identity = when {
        signedIn && backupState.email != null -> backupState.email
        signedIn && backupState.displayName != null -> backupState.displayName
        else -> backupState.localizedSubtitle(lastSyncedAtMillis)
    }
    val initial = when {
        signedIn && !backupState.displayName.isNullOrBlank() -> backupState.displayName.first().uppercaseChar().toString()
        signedIn && !backupState.email.isNullOrBlank() -> backupState.email.first().uppercaseChar().toString()
        else -> "G"
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(FxTheme.shapes.field)
            .background(if (signedIn) FxTheme.colors.accentSoft else Color.Transparent)
            .border(1.dp, if (signedIn) FxTheme.colors.accentLine else FxTheme.colors.border, FxTheme.shapes.field)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(if (signedIn) FxTheme.colors.accent else FxTheme.colors.surface2),
            contentAlignment = Alignment.Center,
        ) {
            Text(initial, style = FxTheme.typography.bodyStrong, color = if (signedIn) FxTheme.colors.bg else FxTheme.colors.text)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text(identity, style = FxTheme.typography.caption, color = FxTheme.colors.textFaint)
            if (signedIn) {
                Text(formatLastSyncedLocalized(lastSyncedAtMillis), style = FxTheme.typography.captionMono, color = FxTheme.colors.accent)
            }
            if (backupState.errorMessage != null) {
                Text(backupState.errorMessage, style = FxTheme.typography.captionMono, color = FxTheme.colors.down)
            }
        }
        Pill(
	            if (backupSyncing) ui("syncing") else if (signedIn) backupState.providerLabel ?: ui("account") else ui(backupState.actionLabel),
            variant = if (signedIn || backupState.isAvailable) PillVariant.Accent else PillVariant.Ghost,
        )
    }
}

@Composable
private fun SettingChoiceRow(
    title: String,
    subtitle: String,
    selected: Boolean,
	    actionLabel: String = if (selected) ui("active") else ui("select"),
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(FxTheme.shapes.field)
            .background(if (selected) FxTheme.colors.accentSoft else Color.Transparent)
            .border(if (selected) 1.dp else 0.dp, if (selected) FxTheme.colors.accentLine else Color.Transparent, FxTheme.shapes.field)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(title, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text(subtitle, style = FxTheme.typography.caption, color = FxTheme.colors.textFaint)
        }
        Pill(actionLabel, variant = if (selected) PillVariant.Accent else PillVariant.Ghost)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyPickerSheet(
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyListPickerSheet(
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
                    .height(430.dp)
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

private const val DefaultPickerVisibleLimit = 20

private val PopularCurrencyCodes = listOf("USD", "EUR", "GBP", "JPY", "AUD", "CAD", "CHF", "CNY", "BRL", "MXN", "NZD", "SGD")

@Composable
private fun assetKindLabel(currency: FxRate): String =
    when {
        currency.kind == CurrencyKind.Crypto && currency.code in StablecoinCodes -> ui("Stablecoin")
        currency.kind == CurrencyKind.Crypto -> ui("Crypto")
        else -> ui("Fiat")
    }

private fun compactCurrencyChoices(
    currencies: List<FxRate>,
    selectedCode: String,
    limit: Int,
): List<FxRate> {
    val distinct = currencies.distinctBy { it.code }
    val byCode = distinct.associateBy { it.code }
    val selected = byCode[selectedCode]
    val popular = PopularCurrencyCodes.mapNotNull { byCode[it] }
    return (listOfNotNull(selected) + popular.filterNot { it.code == selectedCode })
        .take(limit)
        .ifEmpty { distinct.take(limit) }
}

private val ThemeMode.label: String
    get() = when (this) {
        ThemeMode.System -> "System"
        ThemeMode.Light -> "Light"
        ThemeMode.Dark -> "Dark"
    }

private val ThemeMode.subtitle: String
    get() = when (this) {
        ThemeMode.System -> "Follow device appearance"
        ThemeMode.Light -> "Use the bright interface"
        ThemeMode.Dark -> "Use the dark trading interface"
    }

private val UserBackupState.title: String
    get() = when {
        isAvailable && isAnonymous -> "Guest backup active"
        isAvailable -> "${providerLabel ?: "Account"} backup active"
        else -> "Backup unavailable"
    }

private fun UserBackupState.subtitle(lastSyncedAtMillis: Long?): String {
    val syncLabel = lastSyncedAtMillis?.let { " · ${formatLastSynced(it)}" }.orEmpty()
    val base = when {
        isAvailable && uid?.startsWith("ios-anon-") == true && isAnonymous -> "Local iOS guest ${uid.takeLast(8)}"
        isAvailable && uid != null && isAnonymous -> "Firebase guest ${uid.take(8)}"
        isAvailable && uid != null -> "Restores on any signed-in device"
        isAvailable -> "Preferences, alerts and watchlist sync to Firebase"
        else -> "Firebase Auth has not started on this platform"
    }
    return if (errorMessage != null) "$base · $errorMessage" else "$base$syncLabel"
}

@Composable
private fun UserBackupState.localizedSubtitle(lastSyncedAtMillis: Long?): String {
    val syncLabel = lastSyncedAtMillis?.let { " · ${formatLastSyncedLocalized(it)}" }.orEmpty()
    val base = when {
        isAvailable && uid?.startsWith("ios-anon-") == true && isAnonymous -> "${ui("Local iOS guest")} ${uid.takeLast(8)}"
        isAvailable && uid != null && isAnonymous -> "${ui("Firebase guest")} ${uid.take(8)}"
        isAvailable && uid != null -> ui("Restores on any signed-in device")
        isAvailable -> ui("Preferences, alerts and watchlist sync to Firebase")
        else -> ui("Firebase Auth has not started on this platform")
    }
    return if (errorMessage != null) "$base · $errorMessage" else "$base$syncLabel"
}

private val UserBackupState.actionLabel: String
    get() = if (isAvailable) "active" else "offline"

private fun formatLastSynced(millis: Long): String {
    val elapsedSeconds = ((Clock.System.now().toEpochMilliseconds() - millis) / 1000).coerceAtLeast(0)
    return when {
        elapsedSeconds < 15 -> "synced just now"
        elapsedSeconds < 60 -> "synced ${elapsedSeconds}s ago"
        elapsedSeconds < 3600 -> "synced ${elapsedSeconds / 60}m ago"
        elapsedSeconds < 86_400 -> "synced ${elapsedSeconds / 3600}h ago"
        else -> "synced ${elapsedSeconds / 86_400}d ago"
    }
}

private fun formatLastSynced(millis: Long?): String =
    millis?.let(::formatLastSynced) ?: "Sync pending"

@Composable
private fun formatLastSyncedLocalized(millis: Long): String {
    val elapsedSeconds = ((Clock.System.now().toEpochMilliseconds() - millis) / 1000).coerceAtLeast(0)
    return when {
        elapsedSeconds < 15 -> ui("synced just now")
        elapsedSeconds < 60 -> "${ui("synced")} ${elapsedSeconds}s ${ui("ago")}"
        elapsedSeconds < 3600 -> "${ui("synced")} ${elapsedSeconds / 60}m ${ui("ago")}"
        elapsedSeconds < 86_400 -> "${ui("synced")} ${elapsedSeconds / 3600}h ${ui("ago")}"
        else -> "${ui("synced")} ${elapsedSeconds / 86_400}d ${ui("ago")}"
    }
}

@Composable
private fun formatLastSyncedLocalized(millis: Long?): String =
    millis?.let { formatLastSyncedLocalized(it) } ?: ui("Sync pending")

@Composable
private fun localizedRuntimeLabel(label: String): String =
    when {
        label == "cached · mock" -> "${ui("cached")} · mock"
        label == "Auto-refresh off" -> ui("Auto-refresh off")
        label.startsWith("Auto-refresh every ") -> "${ui("Auto-refresh every")} ${label.substringAfter("every ").substringBefore(" min")} ${ui("min")}"
        label == "loading" -> ui("loading")
        label == "updated just now" -> ui("updated just now")
        label.startsWith("updated ") && label.endsWith("m ago") -> "${ui("updated")} ${label.removePrefix("updated ").removeSuffix("m ago")}m ${ui("ago")}"
        label.startsWith("updated ") -> "${ui("updated")} ${label.removePrefix("updated ")}"
        label.startsWith("refreshed ") -> "${ui("refreshed")} ${label.removePrefix("refreshed ")}"
        else -> ui(label)
    }

@Composable
private fun localizedCurrencyName(name: String): String = ui(name)

@Composable
private fun localizedRate(rate: FxRate): FxRate =
    rate.copy(
        name = localizedCurrencyName(rate.name),
        caption = if (rate.caption == "cached · mock") localizedRuntimeLabel(rate.caption) else ui(rate.caption),
    )

@Composable
private fun localizedSubscriptionMessage(message: String): String =
    when {
        message.startsWith("No RevenueCat package is configured for ") -> {
            val plan = message.removePrefix("No RevenueCat package is configured for ").removeSuffix(".")
            "${ui("No RevenueCat package is configured for")} ${ui(plan)}."
        }
        message.startsWith("Pro active") -> message.replace("Pro active", ui("Pro active"))
        message == "RevenueCat key missing. Add REVENUECAT_API_KEY to enable live purchases." -> ui("RevenueCat key missing. Add REVENUECAT_API_KEY to enable live purchases.")
        message == "RevenueCat key missing. Add REVENUECAT_API_KEY before testing purchases." -> ui("RevenueCat key missing. Add REVENUECAT_API_KEY before testing purchases.")
        message == "RevenueCat key missing. Restore is not connected yet." -> ui("RevenueCat key missing. Restore is not connected yet.")
        message == "RevenueCat unavailable." -> ui("RevenueCat unavailable.")
        message == "No offering packages are configured in RevenueCat." -> ui("No offering packages are configured in RevenueCat.")
        message == "Purchase did not complete." -> ui("Purchase did not complete.")
        message == "Restore failed." -> ui("Restore failed.")
        message == "Dev override only affects local debug gating." -> ui("Dev override only affects local debug gating.")
        else -> ui(message)
    }

private val AlertKind.label: String
    get() = when (this) {
        AlertKind.Target -> "Target"
        AlertKind.DailyChange -> "Daily move"
    }

private fun AlertDirection.label(kind: AlertKind): String =
    when (kind) {
        AlertKind.Target -> when (this) {
            AlertDirection.Above -> "Above"
            AlertDirection.Below -> "Below"
        }
        AlertKind.DailyChange -> when (this) {
            AlertDirection.Above -> "Up"
            AlertDirection.Below -> "Down"
        }
    }

private enum class QuickAlertState(
    val label: String,
    val variant: PillVariant,
) {
    Create("create", PillVariant.Ghost),
    Active("active", PillVariant.Up),
    Paused("resume", PillVariant.Ghost),
    Locked("pro", PillVariant.Accent),
}

private data class SmartAlertSuggestion(
    val rate: FxRate,
    val title: String,
    val subtitle: String,
    val target: Double,
    val direction: AlertDirection,
    val kind: AlertKind = AlertKind.Target,
    val strength: Double,
) {
    val strengthLabel: String
        get() = "${(strength * 100.0).toInt()}%"
}

private fun smartAlertSuggestions(rates: List<FxRate>, isPremium: Boolean): List<SmartAlertSuggestion> =
    rates
        .mapNotNull(::smartAlertSuggestion)
        .sortedWith(compareByDescending<SmartAlertSuggestion> { it.strength }.thenBy { it.rate.code })
        .take(if (isPremium) 4 else 2)

private fun smartAlertSuggestion(rate: FxRate): SmartAlertSuggestion? {
    val points = (rate.sparkline + rate.rate.toFloat())
        .filter { it.isFinite() && it > 0f }
        .map { it.toDouble() }
    if (points.size < 3) return null
    val low = points.minOrNull() ?: return null
    val high = points.maxOrNull() ?: return null
    val range = high - low
    if (range <= 0.0) return null
    val position = ((rate.rate - low) / range).coerceIn(0.0, 1.0)
    return when {
        position >= 0.74 -> SmartAlertSuggestion(
            rate = rate,
            title = "Near recent high",
            subtitle = "30d range signal",
            target = rate.rate * 1.002,
            direction = AlertDirection.Above,
            strength = position,
        )
        position <= 0.26 -> SmartAlertSuggestion(
            rate = rate,
            title = "Near recent low",
            subtitle = "30d range signal",
            target = rate.rate * 0.998,
            direction = AlertDirection.Below,
            strength = 1.0 - position,
        )
        else -> null
    }
}

private data class AlertPreset(
    val label: String,
    val percent: Double,
)

private val alertPresets = listOf(
    AlertPreset("-1%", -1.0),
    AlertPreset("-0.5%", -0.5),
    AlertPreset("+0.5%", 0.5),
    AlertPreset("+1%", 1.0),
)

private fun List<PriceAlert>.findQuickAlert(baseCurrency: String, rate: FxRate): PriceAlert? {
    val target = quickAlertTarget(rate)
    return findMatchingAlert(
        baseCurrency = baseCurrency,
        quote = rate.code,
        target = target,
        direction = AlertDirection.Above,
        kind = AlertKind.Target,
    )
}

private fun quickAlertTarget(rate: FxRate): Double =
    rate.rate * 1.01

private fun List<PriceAlert>.findMatchingAlert(
    baseCurrency: String,
    quote: String,
    target: Double,
    direction: AlertDirection,
    kind: AlertKind,
): PriceAlert? =
    firstOrNull {
        it.matchesDefinition(
            base = baseCurrency,
            quote = quote,
            target = target,
            direction = direction,
            kind = kind,
        )
    }

private fun PriceAlert.isHit(currentRate: Double?, currentChangePct: Double?): Boolean =
    when (kind) {
        AlertKind.Target -> {
            if (currentRate == null) false else when (direction) {
                AlertDirection.Above -> currentRate >= target
                AlertDirection.Below -> currentRate <= target
            }
        }
        AlertKind.DailyChange -> {
            if (currentChangePct == null) false else when (direction) {
                AlertDirection.Above -> currentChangePct >= target
                AlertDirection.Below -> currentChangePct <= -target
            }
        }
    }

private fun PriceAlert.statusLabel(currentRate: Double?, currentChangePct: Double?): String =
    when {
        kind == AlertKind.Target && currentRate == null -> "waiting for ${base} live rate"
        kind == AlertKind.DailyChange && currentChangePct == null -> "waiting for 24h change"
        isHit(currentRate, currentChangePct) -> "target hit"
        kind == AlertKind.Target && currentRate != null -> "${distancePercent(currentRate)}% away"
        kind == AlertKind.DailyChange && currentChangePct != null -> "${dailyChangeDistancePercent(currentChangePct)} pts away"
        else -> "waiting"
    }

private fun PriceAlert.distanceLabel(currentRate: Double?, currentChangePct: Double?): String =
    when {
        kind == AlertKind.Target && currentRate == null -> "base changed"
        kind == AlertKind.DailyChange && currentChangePct == null -> "waiting"
        isHit(currentRate, currentChangePct) -> "target reached"
        kind == AlertKind.Target && currentRate != null -> "${distancePercent(currentRate)}% to target"
        kind == AlertKind.DailyChange && currentChangePct != null -> "${dailyChangeDistancePercent(currentChangePct)} pts to move"
        else -> "waiting"
    }

@Composable
private fun localizedAlertStatusLabel(alert: PriceAlert, currentRate: Double?, currentChangePct: Double?): String =
    when {
        alert.kind == AlertKind.Target && currentRate == null -> "${ui("waiting for live rate")} · ${alert.base}"
        alert.kind == AlertKind.DailyChange && currentChangePct == null -> ui("waiting for 24h change")
        alert.isHit(currentRate, currentChangePct) -> ui("target hit")
        alert.kind == AlertKind.Target && currentRate != null -> "${alert.distancePercent(currentRate)}% ${ui("away")}"
        alert.kind == AlertKind.DailyChange && currentChangePct != null -> "${alert.dailyChangeDistancePercent(currentChangePct)} ${ui("pts away")}"
        else -> ui("waiting")
    }

@Composable
private fun localizedAlertDistanceLabel(alert: PriceAlert, currentRate: Double?, currentChangePct: Double?): String =
    when {
        alert.kind == AlertKind.Target && currentRate == null -> ui("base changed")
        alert.kind == AlertKind.DailyChange && currentChangePct == null -> ui("waiting")
        alert.isHit(currentRate, currentChangePct) -> ui("target reached")
        alert.kind == AlertKind.Target && currentRate != null -> "${alert.distancePercent(currentRate)}% ${ui("to target")}"
        alert.kind == AlertKind.DailyChange && currentChangePct != null -> "${alert.dailyChangeDistancePercent(currentChangePct)} ${ui("pts to move")}"
        else -> ui("waiting")
    }

private fun PriceAlert.targetLabel(): String =
    when (kind) {
        AlertKind.Target -> formatRate(target)
        AlertKind.DailyChange -> "${formatPercentValue(target)}%"
    }

private fun PriceAlert.dailyChangeDistancePercent(currentChangePct: Double): String {
    val threshold = if (direction == AlertDirection.Above) target else -target
    val distance = kotlin.math.abs(threshold - currentChangePct).coerceAtLeast(0.0)
    return if (distance < 0.1) "<0.1" else formatPercentValue(distance)
}

private fun PriceAlert.distancePercent(currentRate: Double): String {
    val distance = when (direction) {
        AlertDirection.Above -> (target - currentRate) / currentRate
        AlertDirection.Below -> (currentRate - target) / currentRate
    }.coerceAtLeast(0.0) * 100.0
    return if (distance < 0.1) "<0.1" else ((distance * 10).toInt() / 10.0).toString()
}

private fun defaultAlertInput(rate: FxRate, direction: AlertDirection, kind: AlertKind): String =
    when (kind) {
        AlertKind.Target -> {
            val multiplier = if (direction == AlertDirection.Above) 1.01 else 0.99
            formatRate(rate.rate * multiplier)
        }
        AlertKind.DailyChange -> "1.0"
    }

private fun alertSummaryLine(
    kind: AlertKind,
    rate: FxRate,
    direction: AlertDirection,
    targetValue: Double,
    currentChangePct: Double,
): String =
    when (kind) {
        AlertKind.Target -> "Current ${formatRate(rate.rate)} · target ${if (targetValue > 0.0) formatRate(targetValue) else "--"}"
        AlertKind.DailyChange -> {
            val threshold = if (targetValue > 0.0) "${direction.label(kind).lowercase()} ${formatPercentValue(targetValue)}%" else "--"
            "24h ${formatSignedPercent(currentChangePct)} · alert at $threshold"
        }
    }

@Composable
private fun localizedAlertSummaryLine(
    kind: AlertKind,
    rate: FxRate,
    direction: AlertDirection,
    targetValue: Double,
    currentChangePct: Double,
): String =
    when (kind) {
        AlertKind.Target -> "${ui("Current")} ${formatRate(rate.rate)} · ${ui("target")} ${if (targetValue > 0.0) formatRate(targetValue) else "--"}"
        AlertKind.DailyChange -> {
            val threshold = if (targetValue > 0.0) {
                "${ui(direction.label(kind)).lowercase()} ${formatPercentValue(targetValue)}%"
            } else {
                "--"
            }
            "24h ${formatSignedPercent(currentChangePct)} · ${ui("alert at")} $threshold"
        }
    }

private fun formatPercentValue(value: Double): String =
    ((value * 10.0).toInt() / 10.0).toString()

private fun formatSignedPercent(value: Double): String {
    val sign = if (value >= 0.0) "+" else "-"
    return "$sign${formatPercentValue(kotlin.math.abs(value))}%"
}

private fun shortAgeLabel(millis: Long): String {
    val elapsedSeconds = ((Clock.System.now().toEpochMilliseconds() - millis) / 1000).coerceAtLeast(0)
    return when {
        elapsedSeconds < 60 -> "Now"
        elapsedSeconds < 3600 -> "${elapsedSeconds / 60}m ago"
        elapsedSeconds < 86_400 -> "${elapsedSeconds / 3600}h ago"
        else -> "${elapsedSeconds / 86_400}d ago"
    }
}

@Composable
private fun localizedShortAgeLabel(millis: Long): String {
    val elapsedSeconds = ((Clock.System.now().toEpochMilliseconds() - millis) / 1000).coerceAtLeast(0)
    return when {
        elapsedSeconds < 60 -> ui("Now")
        elapsedSeconds < 3600 -> "${elapsedSeconds / 60}m ${ui("ago")}"
        elapsedSeconds < 86_400 -> "${elapsedSeconds / 3600}h ${ui("ago")}"
        else -> "${elapsedSeconds / 86_400}d ${ui("ago")}"
    }
}

private fun LiveRatesState.alertRates(isPremium: Boolean): List<FxRate> =
    (favorites + compare + converter + allFiat + availableCryptoRates(isPremium))
        .filterNot { it.code == baseCurrency }
        .distinctBy { it.code }
        .sortedWith(compareByDescending<FxRate> { it.code in PopularCurrencyCodes || it.code in DefaultCryptoCodes }.thenBy { it.code })

private fun LiveRatesState.portfolioRates(isPremium: Boolean = false): List<FxRate> =
    (converter + favorites + compare + allFiat + availableCryptoRates(isPremium))
        .distinctBy { it.code }
        .sortedWith(compareByDescending<FxRate> { it.code == baseCurrency || it.code in DefaultCryptoCodes }.thenBy { it.code })

private fun LiveRatesState.defaultCryptoRates(): List<FxRate> {
    val byCode = crypto.associateBy { it.code }
    return DefaultCryptoCodes.mapNotNull { byCode[it] }
}

private fun LiveRatesState.visibleDashboardCryptoRates(isPremium: Boolean, trackedCurrencyCodes: List<String>): List<FxRate> {
    val byCode = crypto.associateBy { it.code }
    val trackedCrypto = if (isPremium) {
        trackedCurrencyCodes
            .filter { it !in DefaultCryptoCodes }
            .mapNotNull { byCode[it] }
    } else {
        emptyList()
    }
    return (defaultCryptoRates() + trackedCrypto).distinctBy { it.code }
}

private fun LiveRatesState.availableCryptoRates(isPremium: Boolean): List<FxRate> =
    if (isPremium) {
        crypto
    } else {
        defaultCryptoRates()
    }

private fun LiveRatesState.converterAvailableRates(isPremium: Boolean): List<FxRate> =
    (allFiat + favorites + compare + converter + availableCryptoRates(isPremium))
        .distinctBy { it.code }
        .sortedWith(compareByDescending<FxRate> { it.code in PopularCurrencyCodes || it.code in DefaultCryptoCodes }.thenBy { it.code })

private fun LiveRatesState.compareAvailableRates(isPremium: Boolean): List<FxRate> =
    (compare + favorites + converter + allFiat + availableCryptoRates(isPremium))
        .filterNot { it.code == baseCurrency }
        .distinctBy { it.code }
        .sortedWith(compareByDescending<FxRate> { it.code in PopularCurrencyCodes || it.code in DefaultCryptoCodes }.thenBy { it.code })

private fun converterTargetCodes(
    selectedCurrencyCodes: List<String>,
    availableRates: List<FxRate>,
    baseCurrency: String,
    limit: Int,
): List<String> {
    val availableCodes = availableRates.map { it.code }.toSet()
    val selected = selectedCurrencyCodes
        .filter { it != baseCurrency && it in availableCodes }
        .distinct()
    val defaults = PopularCurrencyCodes
        .filter { it != baseCurrency && it in availableCodes && it !in selected }
    val targetLimit = limit.cap(availableRates.size).coerceAtLeast(1)
    return (selected + defaults)
        .take(targetLimit)
        .ifEmpty {
            availableRates
                .map { it.code }
                .filter { it != baseCurrency }
                .take(targetLimit)
        }
}

private fun compareTargetCodes(
    selectedCurrencyCodes: List<String>,
    availableRates: List<FxRate>,
    baseCurrency: String,
    limit: Int,
): List<String> =
    converterTargetCodes(
        selectedCurrencyCodes = selectedCurrencyCodes,
        availableRates = availableRates,
        baseCurrency = baseCurrency,
        limit = limit,
    )

private enum class CompareSortMode(val label: String) {
    Movers("Movers"),
    Strongest("Strongest"),
    Weakest("Weakest"),
}

private fun List<FxRate>.sortedForCompare(sortMode: CompareSortMode): List<FxRate> =
    when (sortMode) {
        CompareSortMode.Movers -> sortedByDescending { kotlin.math.abs(it.change24h) }
        CompareSortMode.Strongest -> sortedByDescending { it.change24h }
        CompareSortMode.Weakest -> sortedBy { it.change24h }
    }

private data class PortfolioHolding(
    val rate: FxRate,
    val amount: Double,
    val averageCostBase: Double,
) {
    val baseValue: Double = amountInBase(rate, amount)
    val dailyChangeInBase: Double = if (rate.rate == 0.0) 0.0 else baseValue * rate.change24h / 100.0
    val hasCostBasis: Boolean = averageCostBase > 0.0 && amount > 0.0
    val costBasisBase: Double = if (hasCostBasis) averageCostBase * amount else 0.0
    val unrealizedPnlBase: Double = if (hasCostBasis) baseValue - costBasisBase else 0.0
}

private fun amountInBase(rate: FxRate, amount: Double): Double =
    if (rate.rate == 0.0) 0.0 else amount / rate.rate

private fun parseAmountInput(value: String): Double {
    val normalized = if (value.count { it == ',' } == 1 && '.' !in value) {
        value.replace(',', '.')
    } else {
        value.replace(",", "")
    }
    return normalized.toDoubleOrNull() ?: 0.0
}

private fun PortfolioHolding.weightLabel(portfolioValue: Double): String =
    if (portfolioValue <= 0.0 || baseValue <= 0.0) {
        "0%"
    } else {
        "${((baseValue / portfolioValue) * 100.0).toInt()}%"
    }

private fun PortfolioHolding.dailyChangeLabel(baseCurrency: String): String {
    val sign = if (dailyChangeInBase >= 0.0) "+" else "-"
    return "$sign$baseCurrency ${formatMoneyValue(kotlin.math.abs(dailyChangeInBase))} today"
}

private fun List<PortfolioHolding>.portfolioValueSeries(): List<Float> {
    if (isEmpty()) return emptyList()
    val pointCount = minOf(24, map { it.rate.sparkline.size }.filter { it > 0 }.minOrNull() ?: return emptyList())
    return List(pointCount) { index ->
        sumOf { holding ->
            val point = holding.rate.sparkline.getOrNull(index)?.toDouble() ?: holding.rate.rate
            if (point <= 0.0) 0.0 else holding.amount / point
        }.toFloat()
    }
}

private fun List<Float>.changePercent(): Double =
    if (size < 2 || first() == 0f) 0.0 else (last() - first()) / first() * 100.0

private fun formatSignedMoney(change: Double, baseCurrency: String): String {
    val sign = if (change >= 0.0) "+" else "-"
    return "$sign$baseCurrency ${formatMoneyValue(kotlin.math.abs(change))}"
}

private fun formatPortfolioSignedPercent(change: Double): String {
    val sign = if (change >= 0.0) "+" else "-"
    return "$sign${formatRate(kotlin.math.abs(change))}%"
}

private fun portfolioPnlPercentLabel(pnl: Double, costBasis: Double): String =
    if (costBasis <= 0.0) {
        "Add average cost"
    } else {
        val sign = if (pnl >= 0.0) "+" else "-"
        "$sign${formatRate(kotlin.math.abs(pnl / costBasis * 100.0))}%"
    }

private fun allocationLabel(value: Double, portfolioValue: Double): String =
    if (portfolioValue <= 0.0 || value <= 0.0) "0%" else "${((value / portfolioValue) * 100.0).toInt()}%"

private fun formatPortfolioChange(change: Double, baseCurrency: String): String {
    val sign = if (change >= 0.0) "+" else "-"
    return "$sign$baseCurrency ${formatMoneyValue(kotlin.math.abs(change))}"
}

private fun formatMoneyValue(value: Double): String =
    when {
        value == 0.0 -> "0.00"
        kotlin.math.abs(value) < 0.01 -> "<0.01"
        else -> formatRate(value)
    }

private fun buildUserBackupSnapshot(
    themeMode: ThemeMode,
    language: String,
    baseCurrency: String,
    travelerCurrency: String,
    travelerBudgetBase: Double,
    converterCurrencyCodes: List<String>,
    compareCurrencyCodes: List<String>,
    userProfile: UserProfile,
    alertsState: AlertsState,
    watchlistState: WatchlistState,
): UserBackupSnapshot =
    UserBackupSnapshot(
        updatedAtMillis = Clock.System.now().toEpochMilliseconds(),
        settings = BackupSettings(
            themeMode = themeMode.name,
            language = language,
            baseCurrency = baseCurrency,
            travelerCurrency = travelerCurrency,
            travelerBudgetBase = travelerBudgetBase,
            converterCurrencyCodes = converterCurrencyCodes,
            compareCurrencyCodes = compareCurrencyCodes,
            userProfile = userProfile.name,
        ),
        alerts = alertsState.alerts,
        watchlist = watchlistState.watchlist,
    )

private fun applyUserBackupSnapshot(
    snapshot: UserBackupSnapshot,
    alertsStore: AlertsStore,
    watchlistStore: WatchlistStore,
    liveStore: LiveRatesStore,
    onConverterCurrencyCodes: (List<String>) -> Unit,
    onCompareCurrencyCodes: (List<String>) -> Unit,
    onTravelerCurrency: (String) -> Unit,
    onTravelerBudgetBase: (Double) -> Unit,
    onUserProfile: (UserProfile) -> Unit,
    onLanguage: (String) -> Unit,
): ThemeMode {
    val theme = ThemeMode.entries.firstOrNull { it.name == snapshot.settings.themeMode } ?: ThemeMode.System
    val language = snapshot.settings.language.ifBlank { DeviceLocale.language }
    val profile = UserProfile.entries.firstOrNull { it.name == snapshot.settings.userProfile } ?: UserProfile.Traveler
    AppSettingsPrefs.setThemeMode(theme)
    AppSettingsPrefs.setLanguage(language)
    AppSettingsPrefs.setBaseCurrency(snapshot.settings.baseCurrency)
    AppSettingsPrefs.setTravelerCurrency(snapshot.settings.travelerCurrency)
    AppSettingsPrefs.setTravelerBudgetBase(snapshot.settings.travelerBudgetBase)
    AppSettingsPrefs.setConverterCurrencyCodes(snapshot.settings.converterCurrencyCodes)
    AppSettingsPrefs.setCompareCurrencyCodes(snapshot.settings.compareCurrencyCodes)
    AppSettingsPrefs.setUserProfile(profile)
    liveStore.setBaseCurrency(snapshot.settings.baseCurrency)
    onLanguage(language)
    onConverterCurrencyCodes(snapshot.settings.converterCurrencyCodes)
    onCompareCurrencyCodes(snapshot.settings.compareCurrencyCodes)
    onTravelerCurrency(snapshot.settings.travelerCurrency)
    onTravelerBudgetBase(snapshot.settings.travelerBudgetBase)
    onUserProfile(profile)
    alertsStore.replaceAll(snapshot.alerts)
    watchlistStore.replaceFromBackup(snapshot.watchlist)
    return theme
}

private fun canCreateAlert(subscriptionState: SubscriptionState, currentCount: Int): Boolean {
    val access = subscriptionState.featureAccess()
    return access.hasUnlimitedAlerts || currentCount < access.alertLimit
}

private data class DetailStats(
    val open: Double,
    val high: Double,
    val low: Double,
    val average: Double,
    val volatilityPct: Double,
)

private val Period.label: String
    get() = when (this) {
        Period.OneDay -> "1D"
        Period.OneWeek -> "1W"
        Period.OneMonth -> "1M"
        Period.OneYear -> "1Y"
        Period.All -> "ALL"
    }

private fun List<Float>.seriesForPeriod(period: Period): List<Float> {
    val source = if (isEmpty()) DetailSeries else this
    val points = when (period) {
        Period.OneDay -> 6
        Period.OneWeek -> 8
        Period.OneMonth -> 18
        Period.OneYear -> source.size
        Period.All -> source.size
    }
    return source.takeLast(points.coerceAtMost(source.size)).ifEmpty { DetailSeries }
}

private fun List<Float>.toDetailStats(): DetailStats {
    val source = if (isEmpty()) DetailSeries else this
    val values = source.map { it.toDouble() }
    val average = values.average().takeIf { !it.isNaN() } ?: 0.0
    val high = values.maxOrNull() ?: 0.0
    val low = values.minOrNull() ?: 0.0
    val volatility = if (average == 0.0) 0.0 else ((high - low) / average) * 100.0
    return DetailStats(
        open = values.firstOrNull() ?: 0.0,
        high = high,
        low = low,
        average = average,
        volatilityPct = volatility,
    )
}

@Composable
private fun BackNavButton(label: String?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(FxTheme.shapes.field)
            .clickable(onClick = onClick)
            .padding(end = 12.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text("←", style = FxTheme.typography.numberL.copy(fontSize = 34.sp), color = FxTheme.colors.text)
        if (label != null) {
            Text(label, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.textDim)
        }
    }
}

@Composable
private fun StoryCard(story: NewsStory, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    BentoCard(
        padding = 12.dp,
        modifier = modifier.clickable(onClick = onClick),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Pill(story.tag, variant = PillVariant.Ghost)
                    Eyebrow(ui(story.impact), color = if (story.impact.startsWith("HIGH")) FxTheme.colors.down else FxTheme.colors.accent)
                }
                Text(story.age, style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
            }
            Text(story.title, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text(story.summary, style = FxTheme.typography.body, color = FxTheme.colors.textDim)
            if (story.source.isNotBlank()) {
                Text(
                    if (story.sourceUrl.isNotBlank()) "${story.source} · ${ui("tap for details")}" else story.source,
                    style = FxTheme.typography.captionMono,
                    color = if (story.sourceUrl.isNotBlank()) FxTheme.colors.accent else FxTheme.colors.textFaint,
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(ui("MOVES"))
                story.moves.forEach { (code, change) ->
                    Pill("$code ${formatChange(change)}", variant = if (change >= 0) PillVariant.Up else PillVariant.Down)
                }
            }
        }
    }
}

@Composable
private fun NewsSearchField(query: String, onQueryChange: (String) -> Unit) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        textStyle = FxTheme.typography.body.copy(color = FxTheme.colors.text),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(FxTheme.shapes.field)
                    .background(FxTheme.colors.surface2)
                    .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
                    .padding(horizontal = 12.dp, vertical = 11.dp),
            ) {
                if (query.isBlank()) {
                    Text(ui("Search headlines, tags or currencies"), style = FxTheme.typography.caption, color = FxTheme.colors.textGhost)
                }
                innerTextField()
            }
        },
    )
}

@Composable
private fun NewsFilterRow(
    label: String,
    options: List<String>,
    selected: String,
    enabled: Boolean,
    onSelect: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Eyebrow(label)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEach { option ->
                Box(
                    modifier = Modifier
                        .clip(FxTheme.shapes.pill)
                        .background(if (selected == option) FxTheme.colors.accentSoft else Color.Transparent)
                        .border(
                            1.dp,
                            if (selected == option) FxTheme.colors.accentLine else FxTheme.colors.border,
                            FxTheme.shapes.pill,
                        )
                        .clickable { onSelect(option) }
                        .padding(horizontal = 11.dp, vertical = 7.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        if (enabled || selected == option) option else "$option Pro",
                        style = FxTheme.typography.captionMono,
                        color = if (selected == option) FxTheme.colors.accent else FxTheme.colors.textDim,
                    )
                }
            }
        }
    }
}

private fun newsEmptyCopy(
    hasBackendStories: Boolean,
    hasQuery: Boolean,
    topic: String,
): Pair<String, String> =
    when {
        !hasBackendStories -> "No market stories yet" to "No live market stories have arrived yet."
        hasQuery -> "No search matches" to "No live stories match this search."
        topic != "ALL" -> "No topic stories" to "Try a broader filter or refresh the feed."
        else -> "No currency stories" to "Try a broader filter or refresh the feed."
    }

private fun privacyPolicyUrl(language: String): String = legalDocumentUrl("privacy", language)

private fun termsOfUseUrl(language: String): String = legalDocumentUrl("terms", language)

private fun legalDocumentUrl(doc: String, language: String): String {
    val normalizedLanguage = language
        .substringBefore("-")
        .substringBefore("_")
        .lowercase()
        .ifBlank { "en" }
    return "https://fxalways.com/legal?doc=$doc&lang=$normalizedLanguage"
}

private fun subscriptionManagementUrl(): String =
    when (PlatformConfig.platform) {
        Platform.Android -> "https://play.google.com/store/account/subscriptions"
        Platform.Ios -> "https://apps.apple.com/account/subscriptions"
    }

private data class SettingsCopy(
    val title: String,
    val sub: String,
    val more: String,
    val backup: String,
    val syncNow: String,
    val syncNowSubtitle: String,
    val syncing: String,
    val sync: String,
    val signInWith: String,
    val signInSubtitle: String,
    val connect: String,
    val signOut: String,
    val signOutSubtitle: String,
    val signOutAction: String,
    val subscription: String,
    val view: String,
    val upgrade: String,
    val restorePurchase: String,
    val restorePurchaseSubtitle: String,
    val restore: String,
    val manageSubscription: String,
    val manageSubscriptionSubtitle: String,
    val open: String,
    val notifications: String,
    val priceAlertNotifications: String,
    val themeMode: String,
    val language: String,
    val activeLanguage: String,
    val deviceLanguage: String,
    val languageApplied: String,
    val baseCurrency: String,
    val moreCurrencies: String,
    val search: String,
    val freeIncludes: String,
    val unlocks: String,
    val unlockAllBaseCurrencies: String,
    val supportedBaseCurrencies: String,
    val legal: String,
    val privacyPolicy: String,
    val privacyPolicySubtitle: String,
    val termsOfUse: String,
    val termsOfUseSubtitle: String,
)

private fun settingsCopy(language: String): SettingsCopy =
    when (language.lowercase()) {
        "es" -> SettingsCopy(
            title = "Ajustes",
            sub = "PREFERENCIAS",
            more = "Más",
            backup = "BACKUP",
            syncNow = "Sincronizar ahora",
            syncNowSubtitle = "Guarda ajustes, alertas y watchlist en Firebase",
            syncing = "sincronizando",
            sync = "sincronizar",
            signInWith = "Iniciar sesión con",
            signInSubtitle = "Mantén el mismo backup y restáuralo en un nuevo",
            connect = "conectar",
            signOut = "Cerrar sesión",
            signOutSubtitle = "Mantén los datos locales y continúa con backup invitado",
            signOutAction = "salir",
            subscription = "SUSCRIPCIÓN",
            view = "ver",
            upgrade = "pro",
            restorePurchase = "Restaurar compra",
            restorePurchaseSubtitle = "Recupera una suscripción existente de Play/App Store",
            restore = "restaurar",
            manageSubscription = "Gestionar suscripción",
            manageSubscriptionSubtitle = "Abre el centro de suscripciones de la tienda",
            open = "abrir",
            notifications = "NOTIFICACIONES",
            priceAlertNotifications = "Notificaciones de alertas",
            themeMode = "TEMA",
            language = "IDIOMA",
            activeLanguage = "Idioma activo",
            deviceLanguage = "Idioma del dispositivo",
            languageApplied = "aplicado",
            baseCurrency = "MONEDA BASE",
            moreCurrencies = "Más monedas",
            search = "Buscar monedas soportadas",
            freeIncludes = "Free incluye",
            unlocks = "desbloquea",
            unlockAllBaseCurrencies = "Desbloquear todas las monedas base",
            supportedBaseCurrencies = "monedas base soportadas",
            legal = "LEGAL",
            privacyPolicy = "Política de privacidad",
            privacyPolicySubtitle = "Cómo FX Always maneja cuenta, rates y datos",
            termsOfUse = "Términos de uso",
            termsOfUseSubtitle = "Suscripción, disclaimers y uso aceptable",
        )
        "pt" -> SettingsCopy(
            title = "Ajustes",
            sub = "PREFERÊNCIAS",
            more = "Mais",
            backup = "BACKUP",
            syncNow = "Sincronizar agora",
            syncNowSubtitle = "Salva ajustes, alertas e watchlist no Firebase",
            syncing = "sincronizando",
            sync = "sincronizar",
            signInWith = "Entrar com",
            signInSubtitle = "Mantenha o mesmo backup e restaure em um novo",
            connect = "conectar",
            signOut = "Sair",
            signOutSubtitle = "Mantém dados locais e continua com backup convidado",
            signOutAction = "sair",
            subscription = "ASSINATURA",
            view = "ver",
            upgrade = "pro",
            restorePurchase = "Restaurar compra",
            restorePurchaseSubtitle = "Recupera uma assinatura existente da Play/App Store",
            restore = "restaurar",
            manageSubscription = "Gerenciar assinatura",
            manageSubscriptionSubtitle = "Abre o centro de assinaturas da loja",
            open = "abrir",
            notifications = "NOTIFICAÇÕES",
            priceAlertNotifications = "Notificações de alertas",
            themeMode = "TEMA",
            language = "IDIOMA",
            activeLanguage = "Idioma ativo",
            deviceLanguage = "Idioma do dispositivo",
            languageApplied = "aplicado",
            baseCurrency = "MOEDA BASE",
            moreCurrencies = "Mais moedas",
            search = "Buscar moedas suportadas",
            freeIncludes = "Free inclui",
            unlocks = "desbloqueia",
            unlockAllBaseCurrencies = "Desbloquear todas as moedas base",
            supportedBaseCurrencies = "moedas base suportadas",
            legal = "LEGAL",
            privacyPolicy = "Política de privacidade",
            privacyPolicySubtitle = "Como FX Always lida com conta, rates e dados",
            termsOfUse = "Termos de uso",
            termsOfUseSubtitle = "Assinatura, disclaimers e uso aceitável",
        )
        else -> {
            fun t(key: String): String = localizedUiText(language, key)
            SettingsCopy(
                title = t("Settings"),
                sub = t("APP PREFERENCES"),
                more = t("More"),
                backup = t("BACKUP"),
                syncNow = t("Sync now"),
                syncNowSubtitle = t("Push the latest settings, alerts and watchlist to Firebase"),
                syncing = t("syncing"),
                sync = t("sync"),
                signInWith = t("Sign in with"),
                signInSubtitle = t("Keep the same backup and restore it on a new"),
                connect = t("connect"),
                signOut = t("Sign out"),
                signOutSubtitle = t("Keep local data and continue with a new guest backup"),
                signOutAction = t("sign out"),
                subscription = t("SUBSCRIPTION"),
                view = t("view"),
                upgrade = t("upgrade"),
                restorePurchase = t("Restore purchase"),
                restorePurchaseSubtitle = t("Recover an existing Play/App Store subscription"),
                restore = t("restore"),
                manageSubscription = t("Manage subscription"),
                manageSubscriptionSubtitle = t("Open the store subscription center for billing changes"),
                open = t("open"),
                notifications = t("NOTIFICATIONS"),
                priceAlertNotifications = t("Price alert notifications"),
                themeMode = t("THEME MODE"),
                language = t("LANGUAGE"),
                activeLanguage = t("Active language"),
                deviceLanguage = t("Device language"),
                languageApplied = t("applied"),
                baseCurrency = t("BASE CURRENCY"),
                moreCurrencies = t("More currencies"),
                search = t("Search supported base currencies"),
                freeIncludes = t("Free includes"),
                unlocks = t("unlocks"),
                unlockAllBaseCurrencies = t("Unlock all base currencies"),
                supportedBaseCurrencies = t("supported base currencies"),
                legal = t("LEGAL"),
                privacyPolicy = t("Privacy Policy"),
                privacyPolicySubtitle = t("How FX Always handles account, rates and analytics data"),
                termsOfUse = t("Terms of Use"),
                termsOfUseSubtitle = t("Subscription terms, disclaimers and acceptable use"),
            )
        }
    }

@Composable
private fun ProUpsellCard(title: String, subtitle: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    BentoCard(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, FxTheme.colors.accentLine, FxTheme.shapes.card)
            .clickable(onClick = onClick),
        padding = 12.dp,
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            FlagDot("∞", CurrencyKind.Crypto, 34.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Eyebrow("FX/ PRO", color = FxTheme.colors.accent)
                Text(title, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                Text(subtitle, style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
            }
            Text("→", style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
        }
    }
}

private fun NewsStory.safeTestTagKey(): String =
    title
        .filter { it.isLetterOrDigit() }
        .take(18)
        .ifBlank { tag }

@Composable
fun PaywallScreen(
    subscriptionState: SubscriptionState = SubscriptionState(isPremium = false),
    actionInProgress: Boolean = false,
    userProfile: UserProfile = UserProfile.Traveler,
    appLanguage: String = LocalAppLanguage.current,
    onClose: () -> Unit = {},
    onStart: (SubscriptionPlanKind) -> Unit = {},
    onRestore: () -> Unit = {},
    onOpenUrl: (String) -> Unit = {},
) {
    var selectedKind by remember { mutableStateOf(SubscriptionPlanKind.Monthly) }
    val selectedPlan = subscriptionState.plans.firstOrNull { it.kind == selectedKind && it.isAvailable }
        ?: subscriptionState.plans.firstOrNull { it.isAvailable }
        ?: subscriptionState.plans.first()
    val profileCopy = userProfile.copy()
    val profilePreset = userProfile.preset()
    LaunchedEffect(selectedPlan.kind) {
        selectedKind = selectedPlan.kind
    }

    ScreenScaffold {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Text("×", style = FxTheme.typography.titleL, color = FxTheme.colors.textDim, modifier = Modifier.testTag("paywall_close").clickable(onClick = onClose))
        }
        Eyebrow("FX/ PRO", color = FxTheme.colors.accent)
	        Text(ui("The full picture.\nMore rates. More context."), style = FxTheme.typography.display, color = FxTheme.colors.text)
        Text(
	            ui("Unlimited alerts, deeper history, expanded comparisons, traveler tools and watchlists on one membership."),
            style = FxTheme.typography.body,
            color = FxTheme.colors.textDim,
        )
        Text(
            ui("Built for people who move money, travel, track currencies or need alerts before rates move away."),
            style = FxTheme.typography.caption,
            color = FxTheme.colors.textFaint,
        )
        BentoCard(Modifier.testTag("paywall_profile_offer"), padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Eyebrow("${ui("FOR YOU")} · ${ui(profileCopy.label)}", color = FxTheme.colors.accent)
                Text(ui(profileCopy.title), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                Text(ui(profileCopy.proFocus), style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
                KeyValueRow(ui("Suggested pair"), profilePreset.suggestedPair, profilePreset.suggestedProvider)
                KeyValueRow(ui("Suggested alert"), ui(profilePreset.suggestedAlert), ui(profilePreset.suggestedHolding))
            }
        }
        if (subscriptionState.isPremium) {
            ProActiveCard(subscriptionState = subscriptionState)
        }
        SectionLabel(ui("PRO UNLOCKS"))
        BentoCard(Modifier.testTag("paywall_benefits"), padding = 12.dp) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
	                BenefitRow("FX", ui("Fresh market rates"), ui("Backend-backed mid-market rates with automatic refresh."))
	                BenefitRow("AL", ui("Unlimited alerts"), ui("Price, range, daily and weekly targets."))
	                BenefitRow("TR", ui("Traveler mode"), ui("Auto-location, cheat sheets and offline rates."))
	                BenefitRow("%", ui("Fee comparison"), ui("Expanded provider estimates by amount and currency pair."))
	                BenefitRow("WL", ui("Bigger watchlists"), ui("Track more currencies across converter, compare and portfolio."))
	                BenefitRow("1Y", ui("Long-range history"), ui("Unlock 1Y and all-time detail views where history is available."))
            }
        }
        SectionLabel(ui("FREE VS PRO"))
        BentoCard(Modifier.testTag("paywall_comparison"), padding = 12.dp) {
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PaywallComparisonRow("alerts", ui("Custom alerts"), ui("1 active alert"), ui("Unlimited pairs + ranges"))
                PaywallComparisonRow("compare", ui("Compare board"), ui("4 currencies"), ui("Every tracked currency"))
                PaywallComparisonRow("crypto", ui("Crypto catalog"), ui("BTC, ETH, USDT, USDC"), ui("Search and add up to 200 crypto assets"))
                PaywallComparisonRow("traveler", ui("Traveler"), ui("Focused destinations"), ui("All destinations + full cheat sheet"))
                PaywallComparisonRow("watchlist", ui("Watchlist"), ui("4 tracked currencies"), ui("Unlimited portfolio tracking"))
                PaywallComparisonRow("news", ui("News"), ui("Top stories only"), ui("Full regional stream"))
                PaywallComparisonRow("history", ui("History"), ui("30 days"), ui("1Y + all-time where available"))
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            subscriptionState.plans.forEach { plan ->
                PlanOption(
                    plan = plan,
                    selected = plan.kind == selectedPlan.kind,
                    modifier = Modifier.testTag("paywall_plan_${plan.kind.name}"),
                    onSelect = {
                        if (plan.isAvailable) {
                            selectedKind = plan.kind
                        }
                    },
                )
            }
        }
        BentoCard(Modifier.border(1.dp, FxTheme.colors.accentLine, FxTheme.shapes.card).testTag("paywall_selected_plan"), padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    selectedPlan.badge?.let { Pill(ui(it), variant = PillVariant.Accent) }
                }
                Text(ui(selectedPlan.title), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                BigValueText(selectedPlan.priceLabel, ui(selectedPlan.cadenceLabel))
                Text(
	                    ui("Billed through Google Play on Android and App Store on iOS."),
                    style = FxTheme.typography.caption,
                    color = FxTheme.colors.textDim,
                )
            }
        }
        subscriptionState.statusMessage?.let {
            Text(
                localizedSubscriptionMessage(it),
                modifier = Modifier.testTag("paywall_status_message"),
                style = FxTheme.typography.captionMono,
                color = FxTheme.colors.down,
            )
        }
        PrimaryButton(
            when {
	                actionInProgress -> ui("Processing...")
	                subscriptionState.isPremium -> ui("Continue")
	                !subscriptionState.canPurchase -> ui("Purchases unavailable")
	                else -> ui("Start FX/ Pro")
            },
            onClick = {
                if (actionInProgress) {
                    return@PrimaryButton
                } else if (subscriptionState.isPremium) {
                    onClose()
                } else if (subscriptionState.canPurchase) {
                    onStart(selectedPlan.kind)
                }
            },
            modifier = Modifier.fillMaxWidth().testTag("paywall_start_button"),
        )
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            Text(
	                ui("Restore purchase"),
                style = FxTheme.typography.captionMono,
                color = if (actionInProgress) FxTheme.colors.textGhost else FxTheme.colors.textFaint,
                modifier = Modifier.testTag("paywall_restore").clickable(enabled = !actionInProgress, onClick = onRestore),
            )
            Text("  ·  ", style = FxTheme.typography.captionMono, color = FxTheme.colors.textGhost)
            Text(
                ui("Terms"),
                style = FxTheme.typography.captionMono,
                color = FxTheme.colors.textFaint,
                modifier = Modifier.testTag("paywall_terms").clickable { onOpenUrl(termsOfUseUrl(appLanguage)) },
            )
            Text("  ·  ", style = FxTheme.typography.captionMono, color = FxTheme.colors.textGhost)
            Text(
                ui("Privacy"),
                style = FxTheme.typography.captionMono,
                color = FxTheme.colors.textFaint,
                modifier = Modifier.testTag("paywall_privacy").clickable { onOpenUrl(privacyPolicyUrl(appLanguage)) },
            )
        }
    }
}

@Composable
private fun PaywallComparisonRow(id: String, feature: String, freeValue: String, proValue: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("paywall_feature_$id")
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2.copy(alpha = 0.54f))
            .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
            .padding(horizontal = 10.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(0.92f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(feature, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text(ui("Free"), style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
            Text(freeValue, style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp), horizontalAlignment = Alignment.End) {
            Text(ui("Pro unlock"), style = FxTheme.typography.captionMono, color = FxTheme.colors.accent)
            Text(proValue, style = FxTheme.typography.caption, color = FxTheme.colors.text)
        }
    }
}

@Composable
private fun ProActiveCard(subscriptionState: SubscriptionState) {
    BentoCard(Modifier.border(1.dp, FxTheme.colors.accentLine, FxTheme.shapes.card).testTag("paywall_active_card"), padding = 12.dp) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FlagDot("✓", CurrencyKind.Fiat, 34.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
	                Eyebrow(ui("ACTIVE"), color = FxTheme.colors.accent)
	                Text(ui("FX/ Pro is active"), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                Text(subscriptionState.localizedProStatusLabel(), style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
            }
        }
    }
}

@Composable
private fun PlanOption(
    plan: SubscriptionPlan,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onSelect: () -> Unit,
) {
    val borderColor = if (selected) FxTheme.colors.accentLine else FxTheme.colors.border
    val contentAlpha = if (plan.isAvailable) 1f else 0.46f
    BentoCard(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, FxTheme.shapes.card)
            .alpha(contentAlpha)
            .clickable(onClick = onSelect),
        padding = 12.dp,
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            FlagDot(planGlyph(plan.kind), CurrencyKind.Fiat, 40.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(ui(plan.title), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                    plan.badge?.let { Pill(ui(it), variant = PillVariant.Accent) }
                }
                Text(ui(plan.cadenceLabel), style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(plan.priceLabel, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                Text(
	                    if (plan.isAvailable) ui("Available") else ui("Not configured"),
                    style = FxTheme.typography.captionMono,
                    color = FxTheme.colors.textFaint,
                )
            }
        }
    }
}

private fun planGlyph(kind: SubscriptionPlanKind): String =
    when (kind) {
        SubscriptionPlanKind.Monthly -> "1M"
        SubscriptionPlanKind.Yearly -> "1Y"
        SubscriptionPlanKind.Lifetime -> "∞"
    }

private fun SubscriptionState.proStatusLabel(): String =
    if (isPremium) {
        activePlanLabel?.let { "Active plan: $it · Entitlement $entitlementId" }
            ?: "Entitlement $entitlementId is active"
    } else {
        "Alerts, extended history and unlimited watchlists"
    }

@Composable
private fun SubscriptionState.localizedProStatusLabel(): String =
    if (isPremium) {
        activePlanLabel?.let { "${ui("Active plan")}: $it · $entitlementId" }
            ?: "${ui("Entitlement is active")} · $entitlementId"
    } else {
        ui("Alerts, extended history and unlimited watchlists")
    }

@Composable
fun OfflineScreen(
    liveState: LiveRatesState = LiveRatesState(),
    onRefresh: () -> Unit = {},
) {
    val primaryRate = liveState.favorites.firstOrNull()
        ?: liveState.converter.firstOrNull { it.code != liveState.baseCurrency }
        ?: liveState.compare.firstOrNull()
    ScreenScaffold {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LiveDot(color = FxTheme.colors.down)
	            Eyebrow(ui("OFFLINE"), color = FxTheme.colors.down)
	            Text(localizedRuntimeLabel(liveState.updatedLabel), style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
        }
	        ScreenHeader(
            ui("No connection"),
            subtitle = if (liveState.isOfflineCache) {
                ui("Showing rates from your last sync")
            } else {
                ui("Connect once to save rates for offline use")
            },
        )
        if (primaryRate != null) {
            BentoCard {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
	                Eyebrow("${ui("LAST KNOWN")} · ${liveState.baseCurrency} → ${primaryRate.code}", color = FxTheme.colors.down)
                    Text(formatRate(primaryRate.rate), style = FxTheme.typography.numberXL, color = FxTheme.colors.textDim)
                    Text(localizedRuntimeLabel(liveState.updatedLabel), style = FxTheme.typography.captionMono, color = FxTheme.colors.textFaint)
                }
            }
        } else {
            BentoCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Eyebrow(ui("LAST KNOWN"), color = FxTheme.colors.down)
                    Text(ui("No saved rates yet"), style = FxTheme.typography.bodyStrong, color = FxTheme.colors.textDim)
                }
            }
        }
	        PrimaryButton("↻  ${ui("Retry connection")}", onClick = onRefresh)
	        SectionLabel(ui("CACHED FAVORITES"))
        BentoCard(padding = 0.dp) {
            Column {
                liveState.favorites.take(4).forEach { CurrencyRow(localizedRate(it), dense = true, enabled = false) }
            }
        }
        Text("╌╌╌  ${ui("saved locally")}  ╌╌╌", style = FxTheme.typography.captionMono, color = FxTheme.colors.textGhost, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    }
}

private data class OnboardingStep(
    val tag: String,
    val title: String,
    val body: String,
    val glyph: String,
    val signal: String,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onComplete: (UserProfile) -> Unit = {}) {
    val localCurrency = remember { DeviceLocale.currencyCode }
    val localRegion = remember { DeviceLocale.region.uppercase() }
    val localLanguage = remember { DeviceLocale.language.uppercase() }
    var selectedProfile by remember { mutableStateOf(UserProfile.Traveler) }
	    val steps = listOf(
            OnboardingStep(
	                tag = ui("STEP 01 · LIVE RATES"),
	                title = ui("Fresh rates.\nAlways ready."),
	                body = ui("The app starts with your local base currency and keeps rates refreshed from the backend."),
                glyph = "⌖",
	                signal = "${ui("Local base")} · $localCurrency",
            ),
            OnboardingStep(
	                tag = ui("STEP 02 · FEES THAT MATTER"),
	                title = ui("See the cost\nbefore you send."),
	                body = ui("Compare estimated provider fees by amount and currency pair, then unlock deeper comparisons with Pro."),
                glyph = "⬢",
	                signal = "${ui("Converter")} · ${ui("fees")} · Pro",
            ),
            OnboardingStep(
	                tag = ui("STEP 03 · TRAVEL READY"),
	                title = ui("Your wallet\nfollows the map."),
	                body = ui("Auto-detect local currency on landing. Offline-safe last rates. Per-country tipping built in."),
                glyph = "◐",
	                signal = "${ui("Region")} · $localRegion",
            ),
            OnboardingStep(
	                tag = ui("STEP 04 · BACKUP"),
	                title = ui("Start private.\nRestore later."),
	                body = ui("A guest backup is created silently. You can connect Google on Android or Apple on iOS when you want portability."),
                glyph = "∞",
	                signal = "${ui("Language")} · $localLanguage",
            ),
	    )
    val pagerState = rememberPagerState(pageCount = { steps.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FxTheme.colors.bg),
    ) {
        GridBg(Modifier.matchParentSize().alpha(0.10f), radialMask = false)
        GridBg(Modifier.matchParentSize().alpha(0.30f))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding()
                .padding(horizontal = 18.dp, vertical = 18.dp),
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("FX/", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Pill(localCurrency, variant = PillVariant.Ghost)
                    Text(
                        ui("Skip"),
                        style = FxTheme.typography.caption,
                        color = FxTheme.colors.textDim,
                        modifier = Modifier
                            .clip(FxTheme.shapes.field)
                            .clickable(onClick = { onComplete(selectedProfile) })
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                key = { it },
            ) { page ->
                OnboardingPage(step = steps[page])
            }

            OnboardingProfilePicker(
                selectedProfile = selectedProfile,
                onProfileSelected = { selectedProfile = it },
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    steps.indices.forEach { dot ->
                        val width by animateDpAsState(
                            targetValue = if (dot == pagerState.currentPage) 22.dp else 6.dp,
                            animationSpec = tween(durationMillis = 200),
                            label = "onboarding-dot",
                        )
                        Box(
                            Modifier
                                .size(width = width, height = 6.dp)
                                .background(
                                    color = if (dot == pagerState.currentPage) FxTheme.colors.accent else FxTheme.colors.textGhost,
                                    shape = CircleShape,
                                ),
                        )
                    }
                }
                PrimaryButton(
	                    text = if (pagerState.currentPage == steps.lastIndex) ui("Get started") else ui("Next  →"),
                    modifier = Modifier.width(if (pagerState.currentPage == steps.lastIndex) 154.dp else 126.dp),
                ) {
                    if (pagerState.currentPage == steps.lastIndex) {
                        onComplete(selectedProfile)
                    } else {
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingProfilePicker(
    selectedProfile: UserProfile,
    onProfileSelected: (UserProfile) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("onboarding_profile_picker")
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Eyebrow(ui("Choose your focus"), color = FxTheme.colors.accent)
            Pill(ui(selectedProfile.copy().label), variant = PillVariant.Accent)
        }
        val rows = listOf(
            listOf(UserProfile.Traveler, UserProfile.CryptoHolder, UserProfile.Remittances),
            listOf(UserProfile.Freelancer, UserProfile.Savings),
        )
        rows.forEach { rowProfiles ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowProfiles.forEach { profile ->
                    val copy = profile.copy()
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("onboarding_profile_${profile.name}")
                            .clip(FxTheme.shapes.field)
                            .background(if (selectedProfile == profile) FxTheme.colors.accentSoft else FxTheme.colors.surface2)
                            .border(
                                if (selectedProfile == profile) 1.dp else 0.dp,
                                if (selectedProfile == profile) FxTheme.colors.accentLine else Color.Transparent,
                                FxTheme.shapes.field,
                            )
                            .clickable { onProfileSelected(profile) }
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            ui(copy.label),
                            style = FxTheme.typography.caption,
                            color = if (selectedProfile == profile) FxTheme.colors.accent else FxTheme.colors.textDim,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                if (rowProfiles.size < 3) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun OnboardingPage(step: OnboardingStep) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start,
    ) {
        Spacer(Modifier.weight(0.18f))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            OnboardingGlyph(step.glyph)
        }
        Spacer(Modifier.weight(0.18f))
        Eyebrow(step.tag, color = FxTheme.colors.accent)
        Spacer(Modifier.height(12.dp))
        Text(step.title, style = FxTheme.typography.titleXL, color = FxTheme.colors.text)
        Spacer(Modifier.height(18.dp))
        Text(step.body, style = FxTheme.typography.body, color = FxTheme.colors.textDim)
        Spacer(Modifier.height(18.dp))
        OnboardingSignal(step.signal)
        Spacer(Modifier.weight(0.22f))
    }
}

@Composable
private fun OnboardingSignal(text: String) {
    Row(
        modifier = Modifier
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2)
            .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LiveDot(Modifier.size(8.dp))
        Text(text, style = FxTheme.typography.captionMono, color = FxTheme.colors.textDim)
    }
}

@Composable
private fun OnboardingGlyph(glyph: String) {
    val transition = rememberInfiniteTransition(label = "onboarding-glyph")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000),
            repeatMode = RepeatMode.Restart,
        ),
        label = "onboarding-glyph-rotation",
    )
    Box(
        modifier = Modifier.size(300.dp),
        contentAlignment = Alignment.Center,
    ) {
        GridBg(Modifier.fillMaxSize().alpha(0.36f))
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .border(1.dp, FxTheme.colors.accentLine, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(156.dp)
                    .border(1.dp, FxTheme.colors.accentLine, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    glyph,
                    style = FxTheme.typography.display.copy(fontSize = 86.sp),
                    color = FxTheme.colors.accent,
                    modifier = Modifier.graphicsLayer { rotationZ = rotation },
                )
            }
        }
    }
}

@Composable
private fun PrimaryButton(text: String, modifier: Modifier = Modifier.fillMaxWidth(), onClick: () -> Unit = {}) {
    Box(
        modifier
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.accent)
            .clickable(onClick = onClick)
            .padding(vertical = 15.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.bg)
    }
}

@Composable
private fun GhostButton(text: String, modifier: Modifier = Modifier.fillMaxWidth(), onClick: () -> Unit = {}) {
    Box(
        modifier
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2)
            .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp, horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
    }
}

@Composable
private fun GhostIconButton(
    icon: MoreFeatureIcon,
    text: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    onClick: () -> Unit = {},
) {
    Row(
        modifier
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2)
            .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MoreFeatureIconView(icon)
        Spacer(Modifier.width(8.dp))
        Text(text, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
    }
}

@Composable
private fun BenefitRow(glyph: String, title: String, body: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2.copy(alpha = 0.62f))
            .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(FxTheme.colors.accentSoft)
                .border(1.dp, FxTheme.colors.accentLine, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(glyph, style = FxTheme.typography.captionMono, color = FxTheme.colors.accent, textAlign = TextAlign.Center)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(title, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
            Text(body, style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
        }
    }
}

@Composable
private fun LegendDot(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(8.dp).background(color, FxTheme.shapes.chip))
        Text(label, style = FxTheme.typography.captionMono, color = FxTheme.colors.textDim)
    }
}

@Composable
private fun SentimentBar(
    bullish: Int = 46,
    neutral: Int = 20,
    bearish: Int = 34,
) {
    Row(Modifier.fillMaxWidth().height(10.dp).clip(FxTheme.shapes.pill)) {
        Box(Modifier.weight(bullish.coerceAtLeast(1).toFloat()).background(FxTheme.colors.up))
        Box(Modifier.weight(neutral.coerceAtLeast(1).toFloat()).background(FxTheme.colors.textGhost))
        Box(Modifier.weight(bearish.coerceAtLeast(1).toFloat()).background(FxTheme.colors.down))
    }
}

@Composable
private fun OverlayChart(rates: List<FxRate>) {
    val border = FxTheme.colors.border
    val series = rates.map { rate -> rate.sparkline.normalizedPercentSeries() }
    val colors = rates.mapIndexed { index, rate -> compareOverlayColor(index, rate.kind) }
    Canvas(Modifier.fillMaxWidth().height(130.dp)) {
        repeat(5) { i ->
            val y = size.height * (i / 4f)
            drawLine(border, Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
        }
        series.forEachIndexed { seriesIndex, values ->
            val min = values.minOrNull() ?: return@forEachIndexed
            val max = values.maxOrNull() ?: return@forEachIndexed
            val range = (max - min).coerceAtLeast(1e-9f)
            val path = Path()
            values.forEachIndexed { index, value ->
                val denominator = values.lastIndex.coerceAtLeast(1)
                val point = Offset(
                    x = (index.toFloat() / denominator) * size.width,
                    y = (1f - (value - min) / range) * size.height,
                )
                if (index == 0) path.moveTo(point.x, point.y) else path.lineTo(point.x, point.y)
            }
            drawPath(path, colors[seriesIndex], style = Stroke(width = 1.5f))
        }
    }
}

private fun List<Float>.normalizedPercentSeries(): List<Float> {
    val first = firstOrNull()?.takeIf { kotlin.math.abs(it) > 0.0000001f } ?: return this
    return map { ((it - first) / first) * 100f }
}

@Composable
private fun compareOverlayColor(index: Int, kind: CurrencyKind?): Color {
    val colors = listOf(FxTheme.colors.accent, FxTheme.colors.up, FxTheme.colors.down, FxTheme.colors.textDim)
    return if (kind == CurrencyKind.Crypto) FxTheme.colors.crypto else colors[index % colors.size]
}
