package com.fxalways.app.screens.onboarding

import com.fxalways.app.screens.*
import com.fxalways.app.screens.profile.copy
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fxalways.app.DeviceLocale
import com.fxalways.app.UserProfile
import com.fxalways.designsystem.components.Eyebrow
import com.fxalways.designsystem.components.GridBg
import com.fxalways.designsystem.components.Pill
import com.fxalways.designsystem.components.PillVariant
import com.fxalways.designsystem.theme.FxTheme
import kotlinx.coroutines.launch

private data class OnboardingStep(
    val tag: String,
    val title: String,
    val body: String,
    val glyph: String,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onComplete: (UserProfile) -> Unit = {}) {
    val localCurrency = remember { DeviceLocale.currencyCode }
    var selectedProfile by remember { mutableStateOf(UserProfile.Traveler) }
    val steps = listOf(
        OnboardingStep(
            tag = ui("STEP 01 · LIVE RATES"),
            title = ui("Fresh rates.\nAlways ready."),
            body = ui("The app starts with your local base currency and keeps rates refreshed from the backend."),
            glyph = "⌖",
        ),
        OnboardingStep(
            tag = ui("STEP 02 · FEES THAT MATTER"),
            title = ui("See the cost\nbefore you send."),
            body = ui("Compare estimated provider fees by amount and currency pair, then unlock deeper comparisons with Pro."),
            glyph = "⬢",
        ),
        OnboardingStep(
            tag = ui("STEP 03 · TRAVEL READY"),
            title = ui("Your wallet\nfollows the map."),
            body = ui("Auto-detect local currency on landing. Offline-safe last rates. Per-country tipping built in."),
            glyph = "◐",
        ),
        OnboardingStep(
            tag = ui("STEP 04 · BACKUP"),
            title = ui("Start private.\nRestore later."),
            body = ui("A guest backup is created silently. You can connect Google on Android or Apple on iOS when you want portability."),
            glyph = "∞",
        ),
    )
    val pagerState = rememberPagerState(pageCount = { steps.size })
    val scope = rememberCoroutineScope()

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(FxTheme.colors.bg),
    ) {
        val compactHeight = maxHeight < 720.dp
        GridBg(Modifier.matchParentSize().alpha(0.10f), radialMask = false)
        GridBg(Modifier.matchParentSize().alpha(0.30f))
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding()
                .padding(horizontal = 18.dp, vertical = if (compactHeight) 12.dp else 18.dp),
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
                OnboardingPage(step = steps[page], compactHeight = compactHeight)
            }

            OnboardingProfilePicker(
                selectedProfile = selectedProfile,
                onProfileSelected = { selectedProfile = it },
                compactHeight = compactHeight,
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
    compactHeight: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(FxTheme.colors.bg)
            .testTag("onboarding_profile_picker")
            .padding(top = if (compactHeight) 6.dp else 8.dp)
            .padding(bottom = if (compactHeight) 8.dp else 12.dp),
        verticalArrangement = Arrangement.spacedBy(if (compactHeight) 6.dp else 8.dp),
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
                            .height(if (compactHeight) 40.dp else 44.dp)
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
private fun OnboardingPage(step: OnboardingStep, compactHeight: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.Start,
    ) {
        Spacer(Modifier.height(if (compactHeight) 12.dp else 34.dp))
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            OnboardingGlyph(step.glyph, size = if (compactHeight) 168.dp else 246.dp)
        }
        Spacer(Modifier.height(if (compactHeight) 14.dp else 26.dp))
        Eyebrow(step.tag, color = FxTheme.colors.accent)
        Spacer(Modifier.height(if (compactHeight) 8.dp else 10.dp))
        Text(step.title, style = FxTheme.typography.titleXL, color = FxTheme.colors.text)
        Spacer(Modifier.height(if (compactHeight) 10.dp else 14.dp))
        Text(
            step.body,
            style = FxTheme.typography.body,
            color = FxTheme.colors.textDim,
            maxLines = if (compactHeight) 2 else 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun OnboardingGlyph(glyph: String, size: Dp) {
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
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        GridBg(Modifier.fillMaxSize().alpha(0.36f))
        Box(
            modifier = Modifier
                .size(size * 0.68f)
                .clip(CircleShape)
                .border(1.dp, FxTheme.colors.accentLine, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(size * 0.54f)
                    .border(1.dp, FxTheme.colors.accentLine, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    glyph,
                    style = FxTheme.typography.display.copy(fontSize = if (size < 200.dp) 52.sp else 72.sp),
                    color = FxTheme.colors.accent,
                    modifier = Modifier.graphicsLayer { rotationZ = rotation },
                )
            }
        }
    }
}
