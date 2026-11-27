package com.example

import com.example.ui.components.T9Search
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class T9SearchTest {
    @Test fun encodesClassicKeypadLetters() {
        assertEquals("843", T9Search.encode("THE"))
        assertEquals("5663", T9Search.encode("MNO"))
        assertEquals("43556", T9Search.encode("Hello"))
    }

    @Test fun ignoresSeparatorsAndPreservesDigits() {
        assertEquals("4355623", T9Search.encode("Hello-23"))
    }

    @Test fun matchesNumericQueryAgainstAnyCandidate() {
        assertTrue(T9Search.matches("435", "Hello World", "Other"))
        assertTrue(T9Search.matches("23", "App 23"))
        assertFalse(T9Search.matches("999", "Hello"))
        assertFalse(T9Search.matches("", "Hello"))
    }
}
