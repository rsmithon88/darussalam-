package com.example.settings

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class MadrassaGeneralSettings(
    val madrassaName: String = "দারুস সালাম মাদরাসা",
    val tagline: String = "দ্বীনি শিক্ষার নির্ভরযোগ্য প্রতিষ্ঠান",
    val district: String = "ঢাকা",
    val address: String = "মেইন রোড, ঢাকা",
    val phone: String = "01700000000",
    val email: String = "darussalammadrasha7@gmail.com",
    val academicYear: String = "২০২৬"
)

data class AdminSecuritySettings(
    val adminEmail: String = "admin@darussalam.com",
    val adminPhone: String = "01700000000",
    val pinCode: String = "1234"
)

data class SmsSettingsData(
    val gatewayName: String = "Texbee (Android App Gateway)",
    val apiKey: String = "txb_live_key_987654321",
    val senderId: String = "DARUSSALAM",
    val texbeeDeviceId: String = "device_android_001",
    val texbeeSimSlot: String = "SIM 1",
    val texbeeServerUrl: String = "https://api.texbee.com/v1/sms/send",
    val isEnabled: Boolean = true
)

data class OneSignalSettingsData(
    val appId: String = "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX",
    val restApiKey: String = "MzA2YjE5Nzkt...",
    val isPushEnabled: Boolean = true
)

data class NotificationSettingsData(
    val notifyOnStudentAbsence: Boolean = true,
    val notifyOnTeacherAbsence: Boolean = true,
    val notifyExamResults: Boolean = true
)

class SettingsManager private constructor() {

    private val _generalSettings = MutableStateFlow(MadrassaGeneralSettings())
    val generalSettings: StateFlow<MadrassaGeneralSettings> = _generalSettings.asStateFlow()

    private val _securitySettings = MutableStateFlow(AdminSecuritySettings())
    val securitySettings: StateFlow<AdminSecuritySettings> = _securitySettings.asStateFlow()

    private val _smsSettings = MutableStateFlow(SmsSettingsData())
    val smsSettings: StateFlow<SmsSettingsData> = _smsSettings.asStateFlow()

    private val _oneSignalSettings = MutableStateFlow(OneSignalSettingsData())
    val oneSignalSettings: StateFlow<OneSignalSettingsData> = _oneSignalSettings.asStateFlow()

    private val _notificationSettings = MutableStateFlow(NotificationSettingsData())
    val notificationSettings: StateFlow<NotificationSettingsData> = _notificationSettings.asStateFlow()

    private val _designations = MutableStateFlow(
        listOf("প্রধান শিক্ষক", "সহকারী শিক্ষক", "মুহাদ্দিস", "হাফেজ", "হিসাবরক্ষক", "খাদেম")
    )
    val designations: StateFlow<List<String>> = _designations.asStateFlow()

    private val _feeTypes = MutableStateFlow(
        listOf("মাসিক বেতন", "ভর্তি ফি", "পরীক্ষার ফি", "আবাসিক ফি", "বই পত্র ফি")
    )
    val feeTypes: StateFlow<List<String>> = _feeTypes.asStateFlow()

    fun updateGeneralSettings(newSettings: MadrassaGeneralSettings) {
        _generalSettings.value = newSettings
    }

    fun updateSecuritySettings(newSettings: AdminSecuritySettings) {
        _securitySettings.value = newSettings
    }

    fun updateSmsSettings(newSettings: SmsSettingsData) {
        _smsSettings.value = newSettings
    }

    fun updateOneSignalSettings(newSettings: OneSignalSettingsData) {
        _oneSignalSettings.value = newSettings
    }

    fun updateNotificationSettings(newSettings: NotificationSettingsData) {
        _notificationSettings.value = newSettings
    }

    fun addDesignation(designation: String) {
        if (designation.isNotBlank() && !_designations.value.contains(designation)) {
            _designations.value = _designations.value + designation
        }
    }

    fun removeDesignation(designation: String) {
        _designations.value = _designations.value - designation
    }

    fun addFeeType(feeType: String) {
        if (feeType.isNotBlank() && !_feeTypes.value.contains(feeType)) {
            _feeTypes.value = _feeTypes.value + feeType
        }
    }

    fun removeFeeType(feeType: String) {
        _feeTypes.value = _feeTypes.value - feeType
    }

    companion object {
        val instance: SettingsManager by lazy { SettingsManager() }
    }
}
