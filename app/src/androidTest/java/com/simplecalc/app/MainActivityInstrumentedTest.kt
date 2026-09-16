package com.simplecalc.app

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented smoke test: launches the real Activity on a device/emulator,
 * taps buttons exactly as a user would, and asserts the on-screen result.
 * This exercises the full UI wiring on top of the already unit-tested engine.
 *
 * Run with: ./gradlew connectedDebugAndroidTest (requires a device or emulator).
 */
@RunWith(AndroidJUnit4::class)
class MainActivityInstrumentedTest {

    @Test
    fun launchingApp_showsZeroOnDisplay() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.textResult)).check(matches(withText("0")))
        }
    }

    @Test
    fun tappingSevenPlusThreeEquals_showsTen() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.btn7)).perform(click())
            onView(withId(R.id.btnAdd)).perform(click())
            onView(withId(R.id.btn3)).perform(click())
            onView(withId(R.id.btnEquals)).perform(click())
            onView(withId(R.id.textResult)).check(matches(withText("10")))
        }
    }

    @Test
    fun tappingClear_resetsDisplayToZero() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.btn5)).perform(click())
            onView(withId(R.id.btnClear)).perform(click())
            onView(withId(R.id.textResult)).check(matches(withText("0")))
        }
    }

    @Test
    fun dividingByZero_showsErrorWithoutCrashing() {
        ActivityScenario.launch(MainActivity::class.java).use {
            onView(withId(R.id.btn9)).perform(click())
            onView(withId(R.id.btnDivide)).perform(click())
            onView(withId(R.id.btn0)).perform(click())
            onView(withId(R.id.btnEquals)).perform(click())
            onView(withId(R.id.textResult)).check(matches(withText(CalculatorEngine.ERROR_TEXT)))
        }
    }
}
