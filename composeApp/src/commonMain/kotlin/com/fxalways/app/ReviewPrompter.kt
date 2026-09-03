package com.fxalways.app

import com.fxalways.observability.Observability

/**
 * When to ask for a store review (issue #8): after the first value moment (first alert created or
 * first provider comparison viewed), only from the second session on, and never more than once.
 */
object ReviewPromptPolicy {
    const val MIN_SESSIONS = 2
    val VALUE_MOMENTS = setOf("alert_created", "provider_compare_viewed")

    fun shouldPrompt(trigger: String, sessionCount: Int, alreadyShown: Boolean): Boolean =
        !alreadyShown && trigger in VALUE_MOMENTS && sessionCount >= MIN_SESSIONS
}

/** Platform hook that shows the store review flow. Android uses Play In-App Review; iOS is a no-op for now. */
expect object PlatformReviewFlow {
    fun request(onResult: (shown: Boolean) -> Unit)
}

object ReviewPrompter {
    private var sessionCounted = false

    /** Tests can swap the store flow for a recorder. */
    var flow: ((onResult: (Boolean) -> Unit) -> Unit) = { onResult -> PlatformReviewFlow.request(onResult) }

    /** Counts one session per process start. */
    fun onAppStart() {
        if (sessionCounted) return
        sessionCounted = true
        AppSettingsPrefs.incrementSessionCount()
    }

    /** Call at a value moment; the policy decides whether the store prompt is requested. */
    fun onValueMoment(trigger: String) {
        if (!ReviewPromptPolicy.shouldPrompt(trigger, AppSettingsPrefs.sessionCount(), AppSettingsPrefs.reviewPromptShown())) return
        AppSettingsPrefs.setReviewPromptShown()
        flow { shown ->
            Observability.event("review_prompt_shown", mapOf("trigger" to trigger, "shown" to shown.toString()))
        }
    }
}
