package com.fxalways.app

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ReviewPromptPolicyTest {
    @Test
    fun neverPromptsInFirstSession() {
        assertFalse(ReviewPromptPolicy.shouldPrompt("alert_created", sessionCount = 1, alreadyShown = false))
        assertFalse(ReviewPromptPolicy.shouldPrompt("provider_compare_viewed", sessionCount = 0, alreadyShown = false))
    }

    @Test
    fun promptsOnceAtValueMomentFromSecondSession() {
        assertTrue(ReviewPromptPolicy.shouldPrompt("alert_created", sessionCount = 2, alreadyShown = false))
        assertTrue(ReviewPromptPolicy.shouldPrompt("provider_compare_viewed", sessionCount = 7, alreadyShown = false))
        assertFalse(ReviewPromptPolicy.shouldPrompt("alert_created", sessionCount = 2, alreadyShown = true))
    }

    @Test
    fun ignoresNonValueMoments() {
        assertFalse(ReviewPromptPolicy.shouldPrompt("tab_selected", sessionCount = 5, alreadyShown = false))
    }
}
