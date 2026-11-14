package com.example.data.system

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DeviceControlsManager {

    @Volatile
    private var torchEnabledState: Boolean = false

    fun isTorchEnabled(): Boolean = torchEnabledState

    fun toggleTorch(context: Context): Pair<Boolean, String> {
        return try {
            val cm = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
                ?: return false to "Camera torch unavailable"
            val cameraId = cm.cameraIdList.firstOrNull { id ->
                val chars = cm.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true &&
                    chars.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
            } ?: cm.cameraIdList.firstOrNull { id ->
                cm.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: return false to "No flashlight hardware found"

            val next = !torchEnabledState
            cm.setTorchMode(cameraId, next)
            torchEnabledState = next
            true to if (next) "Flashlight ON" else "Flashlight OFF"
        } catch (_: SecurityException) {
            false to "Flashlight permission denied by device"
        } catch (_: Exception) {
            false to "Unable to toggle flashlight"
        }
    }

    fun cycleRingerMode(context: Context): String {
        return try {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                ?: return "Audio unavailable"
            val next = when (am.ringerMode) {
                AudioManager.RINGER_MODE_NORMAL -> AudioManager.RINGER_MODE_VIBRATE
                AudioManager.RINGER_MODE_VIBRATE -> AudioManager.RINGER_MODE_SILENT
                else -> AudioManager.RINGER_MODE_NORMAL
            }
            am.ringerMode = next
            when (next) {
                AudioManager.RINGER_MODE_NORMAL -> "Ringer: Sound"
                AudioManager.RINGER_MODE_VIBRATE -> "Ringer: Vibrate"
                else -> "Ringer: Silent"
            }
        } catch (_: SecurityException) {
            openSystemPanel(context, "VOLUME_PANEL")
            "Opened Sound panel (Do Not Disturb access managed by Android)"
        } catch (_: Exception) {
            openSystemPanel(context, "VOLUME_PANEL")
            "Opened Sound panel"
        }
    }

    fun getRingerLabel(context: Context): String {
        return try {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return "Sound"
            when (am.ringerMode) {
                AudioManager.RINGER_MODE_VIBRATE -> "Vibrate"
                AudioManager.RINGER_MODE_SILENT -> "Silent"
                else -> "Sound"
            }
        } catch (_: Exception) {
            "Sound"
        }
    }

    fun openSystemPanel(context: Context, panelType: String): Boolean {
        val intent = when (panelType) {
            "WIFI_PANEL" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
            } else {
                Intent(Settings.ACTION_WIFI_SETTINGS)
            }
            "BLUETOOTH_PANEL" -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            "VOLUME_PANEL" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Intent(Settings.Panel.ACTION_VOLUME)
            } else {
                Intent(Settings.ACTION_SOUND_SETTINGS)
            }
            "DISPLAY_SETTINGS" -> Intent(Settings.ACTION_DISPLAY_SETTINGS)
            else -> Intent(Settings.ACTION_SETTINGS)
        }.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            try {
                context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                true
            } catch (_: Exception) {
                false
            }
        }
    }

    fun readNetworkSummary(context: Context): Pair<String, String> {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
                ?: return "Offline" to "None"
            val network = cm.activeNetwork ?: return "Offline" to "None"
            val caps = cm.getNetworkCapabilities(network) ?: return "Offline" to "None"
            when {
                caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Connected" to "Wi-Fi"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile" to "Cellular"
                caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Wired" to "Ethernet"
                else -> "Online" to "Connected"
            }
        } catch (_: Exception) {
            "Unknown" to "Unknown"
        }
    }

    fun readNextAlarmText(context: Context): String {
        return try {
            val am = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return "None"
            val next = am.nextAlarmClock ?: return "None"
            SimpleDateFormat("EEE h:mm a", Locale.getDefault()).format(Date(next.triggerTime))
        } catch (_: Exception) {
            "None"
        }
    }
}
