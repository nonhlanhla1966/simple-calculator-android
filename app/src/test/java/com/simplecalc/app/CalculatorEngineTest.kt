package com.simplecalc.app

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Pure JVM unit tests for [CalculatorEngine]. No Android framework required,
 * so these run instantly with plain JUnit (`./gradlew testDebugUnitTest`).
 */
class CalculatorEngineTest {

    private lateinit var calc: CalculatorEngine

    @Before
    fun setUp() {
        calc = CalculatorEngine()
    }

    private fun type(text: String) {
        for (c in text) {
            when (c) {
                in '0'..'9' -> calc.inputDigit(c)
                '.' -> calc.inputDecimalPoint()
                '+' -> calc.inputOperator(Operator.ADD)
                '-' -> calc.inputOperator(Operator.SUBTRACT)
                '*' -> calc.inputOperator(Operator.MULTIPLY)
                '/' -> calc.inputOperator(Operator.DIVIDE)
                '=' -> calc.calculateEquals()
                '%' -> calc.inputPercent()
                else -> error("Unsupported token '$c' in test helper")
            }
        }
    }

    // ---------- basic arithmetic ----------

    @Test
    fun addition_returnsCorrectSum() {
        type("12+8=")
        assertEquals("20", calc.currentInput)
    }

    @Test
    fun subtraction_returnsCorrectDifference() {
        type("50-15=")
        assertEquals("35", calc.currentInput)
    }

    @Test
    fun subtraction_canProduceNegativeResult() {
        type("4-9=")
        assertEquals("-5", calc.currentInput)
    }

    @Test
    fun multiplication_returnsCorrectProduct() {
        type("6*7=")
        assertEquals("42", calc.currentInput)
    }

    @Test
    fun division_returnsCorrectQuotient() {
        type("81/9=")
        assertEquals("9", calc.currentInput)
    }

    @Test
    fun division_returnsAccurateDecimalResult() {
        type("10/4=")
        assertEquals("2.5", calc.currentInput)
    }

    // ---------- decimals ----------

    @Test
    fun decimalInput_isPreservedInDisplay() {
        type("3.14")
        assertEquals("3.14", calc.currentInput)
    }

    @Test
    fun decimalArithmetic_addsAccurately_avoidingFloatingPointArtifacts() {
        type("0.1+0.2=")
        // Naive Double math gives 0.30000000000000004; engine must clean this up.
        assertEquals("0.3", calc.currentInput)
    }

    @Test
    fun decimalPoint_cannotBeEnteredTwiceInSameNumber() {
        calc.inputDigit('1')
        calc.inputDecimalPoint()
        calc.inputDigit('5')
        calc.inputDecimalPoint() // should be ignored, already has one
        calc.inputDigit('5')
        assertEquals("1.55", calc.currentInput)
    }

    @Test
    fun decimalMultiplication_isAccurate() {
        type("2.5*4=")
        assertEquals("10", calc.currentInput)
    }

    // ---------- negative numbers ----------

    @Test
    fun toggleSign_makesPositiveNumberNegative() {
        calc.inputDigit('7')
        calc.toggleSign()
        assertEquals("-7", calc.currentInput)
    }

    @Test
    fun toggleSign_twiceReturnsToPositive() {
        calc.inputDigit('7')
        calc.toggleSign()
        calc.toggleSign()
        assertEquals("7", calc.currentInput)
    }

    @Test
    fun negativeNumber_participatesCorrectlyInAddition() {
        calc.inputDigit('5')
        calc.toggleSign() // -5
        calc.inputOperator(Operator.ADD)
        calc.inputDigit('3')
        calc.calculateEquals()
        assertEquals("-2", calc.currentInput)
    }

    @Test
    fun multiplyingTwoNegatives_returnsPositive() {
        calc.inputDigit('4')
        calc.toggleSign()
        calc.inputOperator(Operator.MULTIPLY)
        calc.inputDigit('5')
        calc.toggleSign()
        calc.calculateEquals()
        assertEquals("20", calc.currentInput)
    }

    // ---------- percentage ----------

    @Test
    fun percent_ofStandaloneNumber_divivdesBy100() {
        calc.inputDigit('5')
        calc.inputDigit('0')
        calc.inputPercent()
        assertEquals("0.5", calc.currentInput)
    }

    @Test
    fun percent_afterOperator_isRelativeToFirstOperand() {
        // 200 + 10% -> 10% of 200 = 20 -> 200 + 20 = 220
        type("200+10")
        calc.inputPercent()
        assertEquals("20", calc.currentInput)
        calc.calculateEquals()
        assertEquals("220", calc.currentInput)
    }

    @Test
    fun percent_discountExample_subtraction() {
        // 80 - 25% -> 25% of 80 = 20 -> 80 - 20 = 60
        type("80-25")
        calc.inputPercent()
        calc.calculateEquals()
        assertEquals("60", calc.currentInput)
    }

    // ---------- division by zero ----------

    @Test
    fun divisionByZero_showsErrorInsteadOfCrashing() {
        type("5/0=")
        assertEquals(CalculatorEngine.ERROR_TEXT, calc.currentInput)
        assertTrue(calc.isError)
    }

    @Test
    fun afterDivideByZeroError_furtherDigitEntryRecoversCalculator() {
        type("5/0=")
        assertTrue(calc.isError)
        calc.inputDigit('9')
        assertEquals("9", calc.currentInput)
        assertTrue(!calc.isError)
    }

    @Test
    fun zeroDividedByNonZero_isZeroNotError() {
        type("0/5=")
        assertEquals("0", calc.currentInput)
        assertTrue(!calc.isError)
    }

    // ---------- AC (clear) ----------

    @Test
    fun clear_resetsDisplayToZero() {
        type("123+45")
        calc.clear()
        assertEquals("0", calc.currentInput)
    }

    @Test
    fun clear_resetsPendingOperatorAndExpression() {
        type("9*")
        calc.clear()
        assertEquals("", calc.expression)
        // A stray "=" after AC should have no pending operation to act on.
        calc.inputDigit('3')
        calc.calculateEquals()
        assertEquals("3", calc.currentInput)
    }

    @Test
    fun clear_afterErrorFullyRecovers() {
        type("1/0=")
        calc.clear()
        assertTrue(!calc.isError)
        assertEquals("0", calc.currentInput)
        type("2+2=")
        assertEquals("4", calc.currentInput)
    }

    // ---------- backspace ----------

    @Test
    fun backspace_removesLastDigit() {
        type("123")
        calc.backspace()
        assertEquals("12", calc.currentInput)
    }

    @Test
    fun backspace_onSingleDigit_returnsToZero() {
        calc.inputDigit('7')
        calc.backspace()
        assertEquals("0", calc.currentInput)
    }

    @Test
    fun backspace_onNegativeSingleDigit_returnsToZero() {
        calc.inputDigit('7')
        calc.toggleSign() // -7
        calc.backspace()
        assertEquals("0", calc.currentInput)
    }

    @Test
    fun backspace_removesDecimalPoint() {
        type("12.")
        calc.backspace()
        assertEquals("12", calc.currentInput)
    }

    @Test
    fun backspace_immediatelyAfterOperator_cancelsOperator() {
        type("12+")
        calc.backspace()
        assertEquals("12", calc.currentInput)
        assertEquals("", calc.expression)
        // Calculator should now behave as if no operator was pressed.
        calc.inputOperator(Operator.MULTIPLY)
        calc.inputDigit('3')
        calc.calculateEquals()
        assertEquals("36", calc.currentInput)
    }

    // ---------- continuous / chained calculations ----------

    @Test
    fun chainedOperators_evaluateLeftToRightImmediately() {
        // 5 + 3 + 2 = -> (5+3)=8, then 8+2=10
        type("5+3+2=")
        assertEquals("10", calc.currentInput)
    }

    @Test
    fun newCalculationAfterEquals_startsFromPreviousResult() {
        type("6+4=")
        assertEquals("10", calc.currentInput)
        calc.inputOperator(Operator.MULTIPLY)
        calc.inputDigit('5')
        calc.calculateEquals()
        assertEquals("50", calc.currentInput)
    }

    @Test
    fun typingNewNumberAfterEquals_startsFreshCalculation() {
        type("6+4=")
        assertEquals("10", calc.currentInput)
        calc.inputDigit('9')
        assertEquals("9", calc.currentInput)
    }

    @Test
    fun leadingZeros_areCollapsedCorrectly() {
        calc.inputDigit('0')
        calc.inputDigit('0')
        calc.inputDigit('5')
        assertEquals("5", calc.currentInput)
    }

    @Test
    fun largeResult_doesNotCrashAndReportsError() {
        type("999999999999")
        calc.inputOperator(Operator.MULTIPLY)
        type("999999999999")
        calc.calculateEquals()
        assertTrue(calc.isError || calc.currentInput.isNotEmpty())
    }
}
