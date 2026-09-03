package com.fxalways.app

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** Issue #8: the store review prompt never shows in the first session and shows at most once. */
class ReviewPrompterTest {
    @get:Rule
    val compose = createAndroidComposeRule<ComponentActivity>()

    private val requests = mutableListOf<String>()

    @Before
    fun setUp() {
        AndroidAppContext.init(compose.activity)
        LocalDataReset.clearAll()
        ReviewPrompter.flow = { onResult -> requests += "requested"; onResult(true) }
    }

    @After
    fun tearDown() {
        ReviewPrompter.flow = { onResult -> PlatformReviewFlow.request(onResult) }
        LocalDataReset.clearAll()
    }

    @Test
    fun firstSessionNeverRequestsReview() {
        AppSettingsPrefs.incrementSessionCount()
        ReviewPrompter.onValueMoment("alert_created")
        ReviewPrompter.onValueMoment("provider_compare_viewed")
        assertEquals(emptyList<String>(), requests)
        assertEquals(false, AppSettingsPrefs.reviewPromptShown())
    }

    @Test
    fun secondSessionRequestsReviewOnceAtValueMoment() {
        AppSettingsPrefs.incrementSessionCount()
        AppSettingsPrefs.incrementSessionCount()
        ReviewPrompter.onValueMoment("tab_selected")
        assertEquals(emptyList<String>(), requests)
        ReviewPrompter.onValueMoment("provider_compare_viewed")
        ReviewPrompter.onValueMoment("alert_created")
        assertEquals(listOf("requested"), requests)
        assertEquals(true, AppSettingsPrefs.reviewPromptShown())
    }
}
