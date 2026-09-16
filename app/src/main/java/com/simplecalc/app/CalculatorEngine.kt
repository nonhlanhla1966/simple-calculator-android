package com.simplecalc.app

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.abs

/**
 * Supported binary arithmetic operators.
 */
enum class Operator(val symbol: String) {
    ADD("+"),
    SUBTRACT("\u2212"), // minus sign
    MULTIPLY("\u00D7"), // multiplication sign
    DIVIDE("\u00F7")    // division sign
}

/**
 * Pure Kotlin calculator engine. Contains ALL calculator logic and no Android
 * framework dependencies whatsoever, so it can be unit tested on a plain JVM
 * (fast, no emulator/device required) and reused unchanged inside the Android app.
 *
 * The engine implements the standard "immediate execution" calculator model used
 * by nearly all pocket/phone calculators: numbers and one pending operator are
 * tracked at a time, and pressing an operator or "=" evaluates the pending step.
 */
class CalculatorEngine {

    companion object {
        private const val MAX_INPUT_DIGITS = 15
        private const val MAX_MAGNITUDE = 1e15
        const val ERROR_TEXT = "Error"
        const val ERROR_DIV_ZERO = "Cannot divide by zero"
    }

    /** What is currently shown on the main display line. */
    var currentInput: String = "0"
        private set

    /** Small preview line shown above the main display, e.g. "12 + 5 =". */
    var expression: String = ""
        private set

    /** True if the display currently shows an error state. */
    var isError: Boolean = false
        private set

    private var storedValue: Double? = null
    private var pendingOperator: Operator? = null
    private var startFreshInput: Boolean = true
    private var lastResultShown: Boolean = false

    /** Digit button 0-9 pressed. */
    fun inputDigit(digit: Char) {
        require(digit in '0'..'9') { "inputDigit expects a character '0'..'9'" }
        if (isError) clear()

        if (startFreshInput) {
            currentInput = if (digit == '0') "0" else digit.toString()
            startFreshInput = false
        } else if (currentInput == "0") {
            currentInput = digit.toString()
        } else if (digitCount(currentInput) < MAX_INPUT_DIGITS) {
            currentInput += digit
        }
        lastResultShown = false
    }

    /** Decimal point button (.) pressed. */
    fun inputDecimalPoint() {
        if (isError) clear()
        if (startFreshInput) {
            currentInput = "0."
            startFreshInput = false
        } else if (!currentInput.contains('.')) {
            currentInput += "."
        }
        lastResultShown = false
    }

    /** +/- sign toggle pressed; supports negative number entry. */
    fun toggleSign() {
        if (isError) return
        currentInput = when {
            currentInput == "0" -> "0"
            currentInput.startsWith("-") -> currentInput.substring(1)
            else -> "-$currentInput"
        }
        startFreshInput = false
        lastResultShown = false
    }

    /**
     * Percent button pressed.
     * - If there is a pending operator (e.g. "200 +"), % treats the current number
     *   as a percentage OF the stored value: 200 + 10% -> 10% of 200 -> 20, so "=" gives 220.
     * - Otherwise it simply divides the current number by 100.
     */
    fun inputPercent() {
        if (isError) return
        val current = currentInput.toDoubleOrNull() ?: return
        val result = if (pendingOperator != null && storedValue != null) {
            storedValue!! * (current / 100.0)
        } else {
            current / 100.0
        }
        currentInput = formatNumber(result)
        startFreshInput = true
        lastResultShown = false
    }

    /** One of + − × ÷ pressed. */
    fun inputOperator(op: Operator) {
        if (isError) return
        val current = currentInput.toDoubleOrNull() ?: return

        if (pendingOperator != null && !startFreshInput) {
            // Chain calculation: evaluate the previous step first, e.g. 5 + 3 + -> 8, then continue.
            val result = compute(storedValue ?: 0.0, current, pendingOperator!!)
            if (result == null) {
                setDivideByZeroError()
                return
            }
            storedValue = result
            currentInput = formatNumber(result)
        } else {
            storedValue = current
        }

        pendingOperator = op
        expression = "${formatNumber(storedValue ?: 0.0)} ${op.symbol}"
        startFreshInput = true
        lastResultShown = false
    }

    /** "=" pressed. */
    fun calculateEquals() {
        if (isError) return
        val op = pendingOperator
        val first = storedValue
        if (op == null || first == null) return // nothing pending; ignore
        val second = currentInput.toDoubleOrNull() ?: return

        val result = compute(first, second, op)
        if (result == null) {
            setDivideByZeroError()
            return
        }

        expression = "${formatNumber(first)} ${op.symbol} ${formatNumber(second)} ="
        currentInput = formatNumber(result)
        storedValue = null
        pendingOperator = null
        startFreshInput = true
        lastResultShown = true
    }

    /** Backspace / delete button pressed: removes the last digit, or cancels a pending operator. */
    fun backspace() {
        if (isError) {
            clear()
            return
        }
        if (startFreshInput) {
            // Nothing has been typed since the last operator/result; backspace cancels
            // the pending operator and returns to editing the stored value, if any.
            if (pendingOperator != null && !lastResultShown) {
                pendingOperator = null
                expression = ""
                currentInput = formatNumber(storedValue ?: 0.0)
                storedValue = null
                startFreshInput = false
            }
            return
        }
        currentInput = when {
            currentInput.length <= 1 -> "0"
            currentInput.length == 2 && currentInput.startsWith("-") -> "0"
            else -> currentInput.dropLast(1)
        }
        if (currentInput == "-") currentInput = "0"
        if (currentInput == "0") startFreshInput = true
    }

    /** AC pressed: fully resets the calculator to its initial state. */
    fun clear() {
        currentInput = "0"
        expression = ""
        storedValue = null
        pendingOperator = null
        startFreshInput = true
        lastResultShown = false
        isError = false
    }

    // ---- internal helpers -------------------------------------------------

    private fun setDivideByZeroError() {
        isError = true
        currentInput = ERROR_TEXT
        storedValue = null
        pendingOperator = null
        startFreshInput = true
        lastResultShown = false
    }

    private fun compute(a: Double, b: Double, op: Operator): Double? = when (op) {
        Operator.ADD -> a + b
        Operator.SUBTRACT -> a - b
        Operator.MULTIPLY -> a * b
        Operator.DIVIDE -> if (b == 0.0) null else a / b
    }

    private fun digitCount(s: String): Int = s.count { it.isDigit() }

    /**
     * Formats a Double for display: avoids binary floating point artifacts
     * (e.g. 0.1 + 0.2 must show "0.3", not "0.30000000000000004"), strips
     * unnecessary trailing zeros, and guards against overflow/NaN/Infinity.
     */
    private fun formatNumber(value: Double): String {
        if (value.isNaN() || value.isInfinite()) return ERROR_TEXT
        if (abs(value) > MAX_MAGNITUDE) return ERROR_TEXT

        val rounded = BigDecimal.valueOf(value)
            .setScale(9, RoundingMode.HALF_UP)
            .stripTrailingZeros()
        val plain = rounded.toPlainString()
        return if (plain == "-0") "0" else plain
    }
}
