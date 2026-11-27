package com.example.data.system

import kotlin.math.abs

object QuickMathEvaluator {

  fun evaluate(input: String): String? {
    val sanitized =
      input
        .trim()
        .replace('×', '*')
        .replace('÷', '/')
        .replace('−', '-')
        .replace(" ", "")

    if (sanitized.isEmpty()) return null
    val hasOperator = sanitized.any { it in charArrayOf('+', '-', '*', '/', '%') }
    val hasDigit = sanitized.any { it.isDigit() }
    if (!hasOperator || !hasDigit) return null

    // Prevent single leading negative numbers like "-5" from showing as math
    if (sanitized.startsWith("-") && sanitized.drop(1).none { it in charArrayOf('+', '-', '*', '/', '%') }) {
      return null
    }

    return runCatching {
      val parser = Parser(sanitized)
      val value = parser.parseExpression()
      if (!parser.isAtEnd() || value.isNaN() || value.isInfinite()) return null
      formatNumber(value)
    }
      .getOrNull()
  }

  private fun formatNumber(value: Double): String {
    val rounded = value.toLong()
    return if (abs(value - rounded) < 1e-9) {
      rounded.toString()
    } else {
      String.format("%.4f", value).trimEnd('0').trimEnd('.')
    }
  }

  private class Parser(private val src: String) {
    private var pos = 0

    fun isAtEnd(): Boolean = pos >= src.length

    fun parseExpression(): Double {
      var left = parseTerm()
      while (!isAtEnd()) {
        when (src[pos]) {
          '+' -> {
            pos++
            left += parseTerm()
          }
          '-' -> {
            pos++
            left -= parseTerm()
          }
          else -> break
        }
      }
      return left
    }

    private fun parseTerm(): Double {
      var left = parseFactor()
      while (!isAtEnd()) {
        when (src[pos]) {
          '*' -> {
            pos++
            left *= parseFactor()
          }
          '/' -> {
            pos++
            val denom = parseFactor()
            if (abs(denom) < 1e-12) throw ArithmeticException("Division by zero")
            left /= denom
          }
          '%' -> {
            pos++
            val denom = parseFactor()
            if (abs(denom) < 1e-12) throw ArithmeticException("Modulo by zero")
            left %= denom
          }
          else -> break
        }
      }
      return left
    }

    private fun parseFactor(): Double {
      if (isAtEnd()) throw IllegalArgumentException("Unexpected end")
      return when (src[pos]) {
        '+' -> {
          pos++
          parseFactor()
        }
        '-' -> {
          pos++
          -parseFactor()
        }
        '(' -> {
          pos++
          val inside = parseExpression()
          if (isAtEnd() || src[pos] != ')') throw IllegalArgumentException("Missing )")
          pos++
          inside
        }
        else -> parseNumber()
      }
    }

    private fun parseNumber(): Double {
      val start = pos
      while (!isAtEnd() && (src[pos].isDigit() || src[pos] == '.')) {
        pos++
      }
      if (start == pos) throw IllegalArgumentException("Expected number")
      return src.substring(start, pos).toDouble()
    }
  }
}
