package com.simplecalc.app

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * Single-screen calculator UI. All arithmetic logic lives in [CalculatorEngine];
 * this Activity only wires button clicks to the engine and refreshes the display.
 * Keeping the two separated is what makes the logic unit-testable without any
 * emulator or device (see app/src/test/.../CalculatorEngineTest.kt).
 */
class MainActivity : AppCompatActivity() {

    private val engine = CalculatorEngine()

    private lateinit var resultDisplay: TextView
    private lateinit var expressionDisplay: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        resultDisplay = findViewById(R.id.textResult)
        expressionDisplay = findViewById(R.id.textExpression)

        setupDigitButtons()
        setupOperatorButtons()
        setupFunctionButtons()

        if (savedInstanceState != null) {
            // Restore whatever was on screen; the engine itself resets per-process,
            // which is acceptable for a simple, lightweight calculator.
            resultDisplay.text = savedInstanceState.getString(STATE_RESULT, "0")
            expressionDisplay.text = savedInstanceState.getString(STATE_EXPRESSION, "")
        } else {
            refreshDisplay()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_RESULT, resultDisplay.text.toString())
        outState.putString(STATE_EXPRESSION, expressionDisplay.text.toString())
    }

    private fun setupDigitButtons() {
        val digitButtonIds = intArrayOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        )
        for (id in digitButtonIds) {
            findViewById<Button>(id).setOnClickListener { view ->
                performHapticClick(view)
                val digitChar = (view as Button).text.toString().first()
                engine.inputDigit(digitChar)
                refreshDisplay()
            }
        }

        findViewById<Button>(R.id.btnDecimal).setOnClickListener { view ->
            performHapticClick(view)
            engine.inputDecimalPoint()
            refreshDisplay()
        }
    }

    private fun setupOperatorButtons() {
        findViewById<Button>(R.id.btnAdd).setOnClickListener { view ->
            performHapticClick(view)
            engine.inputOperator(Operator.ADD)
            refreshDisplay()
        }
        findViewById<Button>(R.id.btnSubtract).setOnClickListener { view ->
            performHapticClick(view)
            engine.inputOperator(Operator.SUBTRACT)
            refreshDisplay()
        }
        findViewById<Button>(R.id.btnMultiply).setOnClickListener { view ->
            performHapticClick(view)
            engine.inputOperator(Operator.MULTIPLY)
            refreshDisplay()
        }
        findViewById<Button>(R.id.btnDivide).setOnClickListener { view ->
            performHapticClick(view)
            engine.inputOperator(Operator.DIVIDE)
            refreshDisplay()
        }
        findViewById<Button>(R.id.btnEquals).setOnClickListener { view ->
            performHapticClick(view)
            engine.calculateEquals()
            refreshDisplay()
        }
    }

    private fun setupFunctionButtons() {
        findViewById<Button>(R.id.btnClear).setOnClickListener { view ->
            performHapticClick(view)
            engine.clear()
            refreshDisplay()
        }
        findViewById<Button>(R.id.btnBackspace).setOnClickListener { view ->
            performHapticClick(view)
            engine.backspace()
            refreshDisplay()
        }
        findViewById<Button>(R.id.btnPercent).setOnClickListener { view ->
            performHapticClick(view)
            engine.inputPercent()
            refreshDisplay()
        }
        findViewById<Button>(R.id.btnSign).setOnClickListener { view ->
            performHapticClick(view)
            engine.toggleSign()
            refreshDisplay()
        }
    }

    private fun refreshDisplay() {
        resultDisplay.text = engine.currentInput
        expressionDisplay.text = engine.expression
    }

    private fun performHapticClick(view: android.view.View) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    companion object {
        private const val STATE_RESULT = "state_result"
        private const val STATE_EXPRESSION = "state_expression"
    }
}
