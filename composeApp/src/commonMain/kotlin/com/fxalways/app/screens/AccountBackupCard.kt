package com.fxalways.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fxalways.app.UserBackupState
import com.fxalways.designsystem.components.Pill
import com.fxalways.designsystem.components.PillVariant
import com.fxalways.designsystem.theme.FxTheme
import kotlinx.datetime.Clock

@Composable
internal fun AccountBackupCard(
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
                Text(userFriendlyNetworkError(backupState.errorMessage), style = FxTheme.typography.captionMono, color = FxTheme.colors.down)
            }
        }
        Pill(
            if (backupSyncing) ui("syncing") else if (signedIn) backupState.providerLabel ?: ui("account") else ui(backupState.actionLabel),
            variant = if (signedIn || backupState.isAvailable) PillVariant.Accent else PillVariant.Ghost,
        )
    }
}

private val UserBackupState.title: String
    get() = when {
        isAvailable && isAnonymous -> "Guest backup active"
        isAvailable -> "${providerLabel ?: "Account"} backup active"
        else -> "Backup unavailable"
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
    return "$base$syncLabel"
}

private val UserBackupState.actionLabel: String
    get() = if (isAvailable) "active" else "offline"

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
internal fun userFriendlyNetworkError(message: String?): String {
    if (message.isNullOrBlank()) {
        return ui("Please check your connection and try again.")
    }
    return when {
        message.contains("network", ignoreCase = true) ||
            message.contains("timeout", ignoreCase = true) ||
            message.contains("interrupted", ignoreCase = true) ||
            message.contains("unreachable", ignoreCase = true) -> ui("Please check your connection and try again.")
        message.contains("RevenueCat", ignoreCase = true) -> ui("Purchases are temporarily unavailable. Try again later.")
        else -> message
    }
}
