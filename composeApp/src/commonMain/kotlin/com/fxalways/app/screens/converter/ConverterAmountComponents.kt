package com.fxalways.app.screens.converter

import com.fxalways.app.screens.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fxalways.designsystem.components.BentoCard
import com.fxalways.designsystem.components.Eyebrow
import com.fxalways.designsystem.components.FxRate
import com.fxalways.designsystem.components.Pill
import com.fxalways.designsystem.components.PillVariant
import com.fxalways.designsystem.theme.FxTheme

@Composable
internal fun ConverterAmountCard(
    sourceRate: FxRate,
    targetRate: FxRate,
    amountText: String,
    amountValue: Double,
    amountFocused: Boolean,
    onAmountChange: (String) -> Unit,
    onAmountFocusChange: (Boolean) -> Unit,
    onDone: () -> Unit,
) {
    BentoCard(padding = 14.dp) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(ui("YOU SEND"))
                Pill(sourceRate.code, variant = PillVariant.Accent)
            }
            BasicTextField(
                value = amountText,
                onValueChange = onAmountChange,
                singleLine = true,
                textStyle = FxTheme.typography.numberXL.copy(color = FxTheme.colors.text, fontSize = 38.sp, lineHeight = 40.sp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onDone() }),
                modifier = Modifier
                    .testTag("converter_amount_input")
                    .fillMaxWidth()
                    .clip(FxTheme.shapes.field)
                    .background(if (amountFocused) FxTheme.colors.accentSoft else FxTheme.colors.surface2)
                    .border(1.dp, if (amountFocused) FxTheme.colors.accentLine else FxTheme.colors.border, FxTheme.shapes.field)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
                    .onFocusChanged { onAmountFocusChange(it.isFocused) },
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
}

@Composable
internal fun ConverterRateListCard(
    rates: List<FxRate>,
    sourceRate: FxRate,
    targetRate: FxRate,
    amountValue: Double,
    onTargetSelected: (FxRate) -> Unit,
) {
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
                            onTargetSelected(rate)
                        }
                    },
                )
            }
        }
    }
}

@Composable
internal fun ConverterActionsRow(
    onReverse: () -> Unit,
    onEditList: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        GhostButton(
            "⇄  ${ui("Reverse")}",
            Modifier.weight(1f),
            onClick = onReverse,
        )
        GhostButton(
            "≡  ${ui("Edit list")}",
            Modifier.weight(1f).testTag("converter_edit_list"),
            onClick = onEditList,
        )
    }
}
