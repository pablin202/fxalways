package com.fxalways.app.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.theme.FxTheme
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.Text as MlText
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import java.util.concurrent.atomic.AtomicBoolean

@Composable
actual fun PriceOcrScannerAction(
    scanLabel: String,
    readingLabel: String,
    detectedLabel: String,
    unavailableLabel: String,
    liveTitleLabel: String,
    liveHintLabel: String,
    useDetectedLabel: String,
    closeLabel: String,
    currentCurrencyLabel: String,
    switchingCurrencyLabel: String,
    targetCurrency: String,
    modifier: Modifier,
    onPriceDetected: (amount: String, currencyCode: String?) -> Unit,
) {
    val context = LocalContext.current
    var scannerOpen by remember { mutableStateOf(false) }
    var feedback by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) scannerOpen = true else feedback = unavailableLabel
    }

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        MinimalButton(
            text = scanLabel,
            modifier = Modifier.fillMaxWidth().testTag("price_scanner_scan_button"),
            onClick = {
                feedback = null
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    scannerOpen = true
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
        )
        Text(
            feedback.orEmpty(),
            style = FxTheme.typography.caption,
            color = if (feedback == unavailableLabel) FxTheme.colors.down else FxTheme.colors.textDim,
            modifier = Modifier.testTag("price_scanner_ocr_status"),
        )
    }

    if (scannerOpen) {
        LivePriceScannerDialog(
            targetCurrency = targetCurrency,
            readingLabel = readingLabel,
            detectedLabel = detectedLabel,
            unavailableLabel = unavailableLabel,
            liveTitleLabel = liveTitleLabel,
            liveHintLabel = liveHintLabel,
            useDetectedLabel = useDetectedLabel,
            closeLabel = closeLabel,
            currentCurrencyLabel = currentCurrencyLabel,
            switchingCurrencyLabel = switchingCurrencyLabel,
            onDismiss = { scannerOpen = false },
            onUsePrice = { detection ->
                scannerOpen = false
                feedback = detection.feedback(detectedLabel, targetCurrency)
                onPriceDetected(detection.amountText, detection.currencyCode)
            },
        )
    }
}

@Composable
private fun LivePriceScannerDialog(
    targetCurrency: String,
    readingLabel: String,
    detectedLabel: String,
    unavailableLabel: String,
    liveTitleLabel: String,
    liveHintLabel: String,
    useDetectedLabel: String,
    closeLabel: String,
    currentCurrencyLabel: String,
    switchingCurrencyLabel: String,
    onDismiss: () -> Unit,
    onUsePrice: (PriceDetection) -> Unit,
) {
    var detection by remember { mutableStateOf<PriceDetection?>(null) }
    var status by remember { mutableStateOf(readingLabel) }
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            executor.shutdown()
            runCatching { ProcessCameraProvider.getInstance(context).get().unbindAll() }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        BentoCard(Modifier.fillMaxWidth(), padding = 12.dp) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(liveTitleLabel, style = FxTheme.typography.bodyStrong, color = FxTheme.colors.text)
                        Text(targetCurrency, style = FxTheme.typography.captionMono, color = FxTheme.colors.textDim)
                    }
                    CloseButton(closeLabel, onDismiss)
                }
                Text(liveHintLabel, style = FxTheme.typography.caption, color = FxTheme.colors.textDim)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .clip(FxTheme.shapes.card)
                        .background(FxTheme.colors.surface2)
                        .testTag("price_scanner_live_preview"),
                    contentAlignment = Alignment.Center,
                ) {
                    AndroidView(
                        factory = { ctx ->
                            PreviewView(ctx).also { previewView ->
                                previewView.scaleType = PreviewView.ScaleType.FILL_CENTER
                                bindLiveOcrCamera(
                                    context = ctx,
                                    lifecycleOwner = lifecycleOwner,
                                    previewView = previewView,
                                    executor = executor,
                                    onDetection = {
                                        detection = it
                                        status = it?.feedback(detectedLabel, targetCurrency) ?: unavailableLabel
                                    },
                                )
                            }
                        },
                        modifier = Modifier.matchParentSize(),
                    )
                    Box(
                        Modifier
                            .fillMaxWidth(0.82f)
                            .height(96.dp)
                            .border(1.dp, FxTheme.colors.accent, FxTheme.shapes.field)
                            .background(FxTheme.colors.surface2.copy(alpha = 0.08f)),
                    )
                }
                Text(status, style = FxTheme.typography.caption, color = if (detection == null) FxTheme.colors.textDim else FxTheme.colors.accent)
                detection?.let { current ->
                    if (current.currencyCode != null && current.currencyCode != targetCurrency) {
                        Text(
                            "$currentCurrencyLabel: $targetCurrency. $switchingCurrencyLabel.",
                            style = FxTheme.typography.caption,
                            color = FxTheme.colors.textDim,
                        )
                    }
                }
                ConfirmButton(
                    text = useDetectedLabel,
                    enabled = detection != null,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { detection?.let(onUsePrice) },
                )
            }
        }
    }
}

@Composable
private fun CloseButton(label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(36.dp)
            .clip(FxTheme.shapes.field)
            .background(FxTheme.colors.surface2)
            .border(1.dp, FxTheme.colors.border, FxTheme.shapes.field)
            .clickable(onClick = onClick)
            .testTag("price_scanner_close_button"),
        contentAlignment = Alignment.Center,
    ) {
        Text("X", style = FxTheme.typography.bodyStrong, color = FxTheme.colors.textDim)
    }
}

@Composable
private fun ConfirmButton(text: String, enabled: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .height(48.dp)
            .clip(FxTheme.shapes.field)
            .background(if (enabled) FxTheme.colors.accent else FxTheme.colors.surface2)
            .border(1.dp, if (enabled) FxTheme.colors.accentLine else FxTheme.colors.border, FxTheme.shapes.field)
            .alpha(if (enabled) 1f else 0.54f)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = FxTheme.typography.bodyStrong,
            color = if (enabled) FxTheme.colors.bg else FxTheme.colors.textDim,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun MinimalButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
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

private fun bindLiveOcrCamera(
    context: Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    executor: ExecutorService,
    onDetection: (PriceDetection?) -> Unit,
) {
    val providerFuture = ProcessCameraProvider.getInstance(context)
    providerFuture.addListener(
        {
            val provider = runCatching { providerFuture.get() }.getOrNull() ?: return@addListener
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val analyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(executor, LivePriceAnalyzer(onDetection))
            }
            provider.unbindAll()
            runCatching {
                provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analyzer)
            }
        },
        ContextCompat.getMainExecutor(context),
    )
}

private class LivePriceAnalyzer(
    private val onDetection: (PriceDetection?) -> Unit,
) : ImageAnalysis.Analyzer {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val processing = AtomicBoolean(false)
    private var lastRun = 0L
    private var stableDetection: PriceDetection? = null
    private var candidateDetection: PriceDetection? = null
    private var candidateHits = 0
    private var lastCandidateAt = 0L

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val now = System.currentTimeMillis()
        val mediaImage = imageProxy.image
        if (mediaImage == null || processing.get() || now - lastRun < 650L) {
            imageProxy.close()
            return
        }
        processing.set(true)
        lastRun = now
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        recognizer.process(image)
            .addOnSuccessListener { text ->
                val detection = extractBestPrice(text)
                detection?.let { observeCandidate(it, now) }?.let { stable ->
                    if (stable != stableDetection) {
                        stableDetection = stable
                        onDetection(stable)
                    }
                }
            }
            .addOnFailureListener { onDetection(stableDetection) }
            .addOnCompleteListener {
                processing.set(false)
                imageProxy.close()
            }
    }

    private fun observeCandidate(detection: PriceDetection, now: Long): PriceDetection? {
        val previous = candidateDetection
        val sameCandidate = previous != null && previous.stableKey == detection.stableKey
        candidateDetection = detection
        candidateHits = if (sameCandidate && now - lastCandidateAt < 1_800L) candidateHits + 1 else 1
        lastCandidateAt = now
        return when {
            candidateHits >= 2 -> detection
            stableDetection == null -> null
            stableDetection?.currencyCode == detection.currencyCode &&
                kotlin.math.abs((stableDetection?.amount ?: 0.0) - detection.amount) < 0.01 -> stableDetection
            else -> null
        }
    }
}

private data class PriceDetection(
    val amount: Double,
    val amountText: String,
    val currencyCode: String?,
    val score: Int,
) {
    val stableKey: String
        get() = "${currencyCode.orEmpty()}:$amountText"

    fun feedback(detectedLabel: String, targetCurrency: String): String {
        val currency = currencyCode ?: targetCurrency
        return "$detectedLabel $currency $amountText"
    }
}

private fun extractBestPrice(text: MlText): PriceDetection? {
    val rawLines = text.textBlocks.flatMap { block -> block.lines.map { it.text } }
    val elementLines = text.textBlocks.flatMap { block ->
        block.lines.map { line -> line.elements.joinToString(" ") { it.text } }
    }
    val searchable = (listOf(text.text, text.text.replace('\n', ' ')) + rawLines + elementLines)
        .map { it.normalizeOcrPriceText() }
        .filter { it.isNotBlank() }
        .distinct()

    return searchable
        .flatMap { it.priceCandidates() }
        .filter { it.amount > 0.0 && it.amount < 1_000_000.0 }
        .maxWithOrNull(compareBy<PriceDetection> { it.score }.thenBy { it.amount })
        ?.let { it.copy(amountText = it.amount.formatPrice()) }
}

private fun String.priceCandidates(): List<PriceDetection> {
    val currency = """([$€£¥]|USD|EUR|GBP|JPY|AUD|CAD|CHF|CNY|MXN|BRL|ARS)"""
    val digit = """[0-9OILS|]"""
    val number = """$digit{1,3}(?:[.,\s]$digit{3})+(?:[.,]$digit{1,2})?|$digit{1,6}(?:[.,]\s?$digit{1,2})?"""
    val candidates = mutableListOf<PriceDetection>()
    Regex("""(?i)$currency\s*($number)""").findAll(this).forEach { match ->
        val code = match.groupValues.getOrNull(1).orEmpty().toCurrencyCode()
        match.groupValues.getOrNull(2)?.toPriceNumber()?.let { candidates += PriceDetection(it, it.formatPrice(), code, 6) }
    }
    Regex("""(?i)($number)\s*$currency""").findAll(this).forEach { match ->
        val code = match.groupValues.getOrNull(2).orEmpty().toCurrencyCode()
        match.groupValues.getOrNull(1)?.toPriceNumber()?.let { candidates += PriceDetection(it, it.formatPrice(), code, 6) }
    }
    Regex("""(?<!\d)([0-9]{1,5})\s+([0-9]{2})(?!\d)""").findAll(this).forEach { match ->
        val major = match.groupValues.getOrNull(1).orEmpty().toDoubleOrNull()
        val cents = match.groupValues.getOrNull(2).orEmpty().toDoubleOrNull()
        if (major != null && cents != null) {
            val value = major + cents / 100.0
            candidates += PriceDetection(value, value.formatPrice(), null, 4)
        }
    }
    Regex("""(?<!\d)($number)(?!\d)""").findAll(this).forEach { match ->
        val raw = match.groupValues.getOrNull(1).orEmpty()
        raw.toPriceNumber()?.let { value ->
            val hasDecimal = raw.contains('.') || raw.contains(',')
            candidates += PriceDetection(value, value.formatPrice(), null, if (hasDecimal) 3 else 1)
        }
    }
    return candidates
}

private fun String.toCurrencyCode(): String? =
    when (uppercase(Locale.US)) {
        "$", "USD" -> "USD"
        "€", "EUR" -> "EUR"
        "£", "GBP" -> "GBP"
        "¥", "JPY" -> "JPY"
        "AUD" -> "AUD"
        "CAD" -> "CAD"
        "CHF" -> "CHF"
        "CNY" -> "CNY"
        "MXN" -> "MXN"
        "BRL" -> "BRL"
        "ARS" -> "ARS"
        else -> null
    }

private fun String.normalizeOcrPriceText(): String =
    uppercase(Locale.US)
        .replace(Regex("""\s+"""), " ")
        .trim()

private fun String.toPriceNumber(): Double? {
    val compact = replace(" ", "")
    val decimalSeparator = listOf(compact.lastIndexOf('.'), compact.lastIndexOf(',')).maxOrNull()
    val normalized = buildString {
        compact.forEachIndexed { index, char ->
            when {
                char.isDigit() -> append(char)
                char == 'O' -> append('0')
                char == 'I' || char == 'L' || char == '|' -> append('1')
                char == 'S' -> append('5')
                index == decimalSeparator -> append('.')
            }
        }
    }
    return normalized.toDoubleOrNull()
}

private fun Double.formatPrice(): String {
    val symbols = DecimalFormatSymbols(Locale.US)
    return DecimalFormat("0.##", symbols).format(this)
}
