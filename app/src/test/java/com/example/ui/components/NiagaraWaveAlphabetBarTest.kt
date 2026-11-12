package com.example.ui.components

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NiagaraWaveAlphabetBarTest {

    @Test
    fun appIndexLetterUsesAlphabetAndGroupsSymbolsUnderHash() {
        assertEquals("A", appIndexLetter(" Alpha"))
        assertEquals("Z", appIndexLetter("zebra"))
        assertEquals("#", appIndexLetter(" 123 games"))
        assertEquals("#", appIndexLetter(""))
    }

    @Test
    fun nearestActiveAlphabetLetterSkipsEmptyInitials() {
        val active = setOf("A", "D", "M", "#")
        assertEquals("D", nearestActiveAlphabetLetter(2, active))
        assertEquals("M", nearestActiveAlphabetLetter(10, active))
        assertEquals("A", nearestActiveAlphabetLetter(-10, active))
        assertEquals("#", nearestActiveAlphabetLetter(26, active))
    }

    @Test
    fun nearestActiveAlphabetLetterReturnsNullForEmptyCollection() {
        assertNull(nearestActiveAlphabetLetter(12, emptySet()))
    }
}
