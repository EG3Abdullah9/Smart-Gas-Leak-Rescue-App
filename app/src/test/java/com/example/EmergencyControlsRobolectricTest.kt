package com.example

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.i18n.AppLanguage
import com.example.i18n.AppStrings
import com.example.ui.GasRescueUiState
import com.example.ui.GasRescueViewModel
import com.example.ui.SmsSendMode
import com.example.ui.components.EmergencyDangerResponseCard
import com.example.ui.components.EmergencyDispatchDialog
import com.example.ui.components.PackedHardwareCommandPad
import com.example.ui.theme.GasRescueTheme
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class EmergencyControlsRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `viewmodel toggles fan valve alarm and dispatch dialog independently in manual mode`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = GasRescueViewModel(app)
        vm.setSmsSendMode(SmsSendMode.MANUAL)

        // Crossing 350 PPM triggers emergency state, leaving manual actions ready to toggle
        vm.updateGasPPM(500, pushToChart = false)
        assertTrue(vm.uiState.value.isEmergency)
        assertFalse(vm.uiState.value.isFanActive)
        assertFalse(vm.uiState.value.isValveShutOff)
        assertFalse(vm.uiState.value.isBuzzerActive)

        vm.toggleFanOverride()
        assertTrue(vm.uiState.value.isFanActive)

        vm.toggleGasValve()
        assertTrue(vm.uiState.value.isValveShutOff)

        vm.toggleBuzzerOverride()
        assertTrue(vm.uiState.value.isBuzzerActive)

        vm.openEmergencyDispatchDialog()
        assertTrue(vm.uiState.value.showEmergencyDispatchDialog)
        vm.closeEmergencyDispatchDialog()
        assertFalse(vm.uiState.value.showEmergencyDispatchDialog)

        vm.activateNormalPreset()
        assertFalse(vm.uiState.value.isEmergency)
        assertFalse(vm.uiState.value.isFanActive)
        assertFalse(vm.uiState.value.isValveShutOff)
        assertFalse(vm.uiState.value.isBuzzerActive)
    }

    @Test
    fun `danger response card shows all four emergency buttons when danger detected`() {
        val strings = AppStrings.forLang(AppLanguage.EN)
        val uiState = GasRescueUiState(
            gasPPM = 650,
            isEmergency = true,
            isFanActive = false,
            isValveShutOff = false,
            isBuzzerActive = false
        )
        var fanClicked = false
        var valveClicked = false
        var alarmClicked = false
        var smsWaClicked = false

        composeRule.setContent {
            GasRescueTheme(darkTheme = true) {
                EmergencyDangerResponseCard(
                    uiState = uiState,
                    strings = strings,
                    onActivateFan = { fanClicked = true },
                    onToggleValve = { valveClicked = true },
                    onTriggerAlarm = { alarmClicked = true },
                    onOpenSmsWhatsappDispatch = { smsWaClicked = true }
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("danger_btn_fan").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("danger_btn_valve").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("danger_btn_alarm").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("danger_btn_sms_wa").assertIsDisplayed().performClick()

        assertTrue(fanClicked)
        assertTrue(valveClicked)
        assertTrue(alarmClicked)
        assertTrue(smsWaClicked)
    }

    @Test
    fun `packed command pad and dispatch dialog display interactive controls and saved number`() {
        val strings = AppStrings.forLang(AppLanguage.EN)
        var selectedPreset = 0
        var fullRescueTriggered = false
        val phone = "+201098765432"

        composeRule.setContent {
            GasRescueTheme(darkTheme = true) {
                PackedHardwareCommandPad(
                    uiState = GasRescueUiState(gasPPM = 120),
                    strings = strings,
                    onSelectGasPreset = { selectedPreset = it },
                    onToggleFan = {},
                    onToggleValve = {},
                    onTogglePower = {},
                    onToggleBuzzer = {},
                    onFullRescueTest = { fullRescueTriggered = true },
                    onResetAll = {}
                )
                EmergencyDispatchDialog(
                    phone = phone,
                    alertMessage = "GAS LEAK 650 PPM",
                    strings = strings,
                    onDismiss = {}
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("preset_650").assertIsDisplayed().performClick()
        composeRule.onNodeWithTag("pad_btn_full_rescue").assertIsDisplayed().performClick()
        composeRule.onNodeWithText(phone).assertIsDisplayed()
        composeRule.onNodeWithTag("dispatch_sms_button").assertIsDisplayed()
        composeRule.onNodeWithTag("dispatch_whatsapp_button").assertIsDisplayed()

        assertTrue(selectedPreset == 650)
        assertTrue(fullRescueTriggered)
    }
}
