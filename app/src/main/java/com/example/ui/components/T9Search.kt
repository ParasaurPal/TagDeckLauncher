package com.example.ui.components

/**
 * Converts text to classic telephone keypad digits and matches numeric queries.
 * Letters use the standard 2–9 mapping; punctuation and whitespace are ignored.
 */
object T9Search {
    private val digitByChar = mapOf(
        'a' to '2', 'b' to '2', 'c' to '2',
        'd' to '3', 'e' to '3', 'f' to '3',
        'g' to '4', 'h' to '4', 'i' to '4',
        'j' to '5', 'k' to '5', 'l' to '5',
        'm' to '6', 'n' to '6', 'o' to '6',
        'p' to '7', 'q' to '7', 'r' to '7', 's' to '7',
        't' to '8', 'u' to '8', 'v' to '8',
        'w' to '9', 'x' to '9', 'y' to '9', 'z' to '9',
    )

    fun encode(value: String): String = buildString {
        value.lowercase().forEach { char ->
            when {
                char in '0'..'9' -> append(char)
                digitByChar[char] != null -> append(digitByChar.getValue(char))
            }
        }
    }

    fun matches(numericQuery: String, vararg candidates: String): Boolean {
        val query = numericQuery.filter { it.isDigit() }
        if (query.isEmpty()) return false
        return candidates.any { candidate ->
            encode(candidate).contains(query)
        }
    }
}
