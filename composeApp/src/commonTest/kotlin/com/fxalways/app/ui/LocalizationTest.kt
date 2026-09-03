package com.fxalways.app.ui

import kotlin.test.Test
import kotlin.test.assertEquals

class LocalizationTest {
    @Test
    fun selectorOffersOnlyLaunchLanguages() {
        assertEquals(listOf("en", "es", "pt", "hi"), SupportedLanguages.map { it.code })
    }

    @Test
    fun unsupportedLanguagesFallBackToEnglish() {
        assertEquals("en", supportedLanguageOrDefault("fr"))
        assertEquals("en", supportedLanguageOrDefault("zh-Hans"))
        assertEquals("en", supportedLanguageOrDefault(null))
        assertEquals("pt", supportedLanguageOrDefault("pt-BR"))
        assertEquals("es", supportedLanguageOrDefault("ES_AR"))
        assertEquals("hi", supportedLanguageOrDefault("hi"))
    }
}
