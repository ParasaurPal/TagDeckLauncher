package com.example

import com.example.data.system.QuickMathEvaluator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QuickMathEvaluatorTest {
    @Test
    fun respectsOperatorPrecedenceAndParentheses() {
        assertEquals("14", QuickMathEvaluator.evaluate("2 + 3 * 4"))
        assertEquals("20", QuickMathEvaluator.evaluate("(2 + 3) * 4"))
    }

    @Test
    fun supportsDivisionModuloAndUnicodeOperators() {
        assertEquals("2.5", QuickMathEvaluator.evaluate("5 ÷ 2"))
        assertEquals("1", QuickMathEvaluator.evaluate("10 % 3"))
        assertEquals("9", QuickMathEvaluator.evaluate("7 × 2 − 5"))
    }

    @Test
    fun rejectsInvalidExpressionsAndDivisionByZero() {
        assertNull(QuickMathEvaluator.evaluate("hello"))
        assertNull(QuickMathEvaluator.evaluate("-5"))
        assertNull(QuickMathEvaluator.evaluate("5 / 0"))
        assertNull(QuickMathEvaluator.evaluate("2 +"))
    }
}
