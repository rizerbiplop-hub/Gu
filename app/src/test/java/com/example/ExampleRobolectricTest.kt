package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.engine.DeviceActionEngine
import com.example.model.DeviceAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("RIVON", appName)
    }

    @Test
    fun `test action parsing for app opening`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val engine = DeviceActionEngine(context)

        val action1 = engine.parseActionFromInput("open Instagram", null)
        assertNotNull(action1)
        assertTrue(action1 is DeviceAction.LaunchApp)

        // Nepali / Hinglish phrasing
        val action2 = engine.parseActionFromInput("Instagram khol", null)
        assertNotNull(action2)
        assertTrue(action2 is DeviceAction.LaunchApp)
    }

    @Test
    fun `test action parsing for volume and alarm`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val engine = DeviceActionEngine(context)

        val volAction = engine.parseActionFromInput("set volume to 50 percent", null)
        assertNotNull(volAction)
        assertTrue(volAction is DeviceAction.AdjustVolume)
        assertEquals(50, (volAction as DeviceAction.AdjustVolume).percentage)

        val alarmAction = engine.parseActionFromInput("set alarm for 6:30 am", null)
        assertNotNull(alarmAction)
        assertTrue(alarmAction is DeviceAction.SetAlarm)
        assertEquals(6, (alarmAction as DeviceAction.SetAlarm).hour)
        assertEquals(30, (alarmAction as DeviceAction.SetAlarm).minute)
    }

    @Test
    fun `test action parsing for flashlight and battery`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val engine = DeviceActionEngine(context)

        val flashAction = engine.parseActionFromInput("turn on flashlight", null)
        assertNotNull(flashAction)
        assertTrue(flashAction is DeviceAction.ToggleFlashlight)
        assertTrue((flashAction as DeviceAction.ToggleFlashlight).enabled)

        val battAction = engine.parseActionFromInput("check battery percentage", null)
        assertNotNull(battAction)
        assertTrue(battAction is DeviceAction.CheckBattery)
    }

    @Test
    fun `test action parsing for math and time`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val engine = DeviceActionEngine(context)

        val timeAction = engine.parseActionFromInput("what time is it now", null)
        assertNotNull(timeAction)
        assertTrue(timeAction is DeviceAction.GetDeviceTime)

        val mathAction = engine.parseActionFromInput("calculate 25 + 75", null)
        assertNotNull(mathAction)
        assertTrue(mathAction is DeviceAction.CalculateMath)
        assertEquals("100", (mathAction as DeviceAction.CalculateMath).result)
    }
}
