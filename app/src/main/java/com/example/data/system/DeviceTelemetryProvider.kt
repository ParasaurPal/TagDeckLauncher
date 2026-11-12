package com.example.data.system

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioManager
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.view.KeyEvent
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.roundToInt

data class BatteryTelemetry(
    val levelPercent: Int,
    val isCharging: Boolean,
    val temperatureC: Float,
)

data class StorageRamTelemetry(
    val usedStorageGb: Float,
    val totalStorageGb: Float,
    val availableRamGb: Float,
    val totalRamGb: Float,
)

object DeviceTelemetryProvider {

    fun observeBattery(context: Context): Flow<BatteryTelemetry> = callbackFlow {
        fun readIntent(intent: Intent?): BatteryTelemetry {
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100)?.coerceAtLeast(1) ?: 100
            val pct = if (level >= 0) ((level * 100f) / scale).roundToInt() else 100
            val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
            val tempTenths = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 250) ?: 250
            return BatteryTelemetry(pct, charging, tempTenths / 10f)
        }

        val initial = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        trySend(readIntent(initial))

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                trySend(readIntent(intent))
            }
        }
        try {
            context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        } catch (_: Exception) {
        }
        awaitClose {
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {
            }
        }
    }

    fun readStorageAndRam(context: Context): StorageRamTelemetry {
        val stat = try {
            StatFs(Environment.getDataDirectory().path)
        } catch (_: Exception) {
            null
        }
        val totalBytes = stat?.totalBytes ?: 1L
        val availBytes = stat?.availableBytes ?: 0L
        val usedGb = (totalBytes - availBytes) / (1024f * 1024f * 1024f)
        val totalGb = totalBytes / (1024f * 1024f * 1024f)

        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        try {
            am?.getMemoryInfo(memInfo)
        } catch (_: Exception) {
        }
        val availRamGb = memInfo.availMem / (1024f * 1024f * 1024f)
        val totalRamGb = memInfo.totalMem / (1024f * 1024f * 1024f)
        return StorageRamTelemetry(usedGb, totalGb, availRamGb, totalRamGb)
    }

    fun observeCompassAzimuth(context: Context): Flow<Float> = callbackFlow {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (sensorManager == null || rotationSensor == null) {
            trySend(0f)
            awaitClose {}
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            private val rotMatrix = FloatArray(9)
            private val orientation = FloatArray(3)
            override fun onSensorChanged(event: SensorEvent) {
                SensorManager.getRotationMatrixFromVector(rotMatrix, event.values)
                SensorManager.getOrientation(rotMatrix, orientation)
                val azimuthDeg = ((Math.toDegrees(orientation[0].toDouble()) + 360.0) % 360.0).toFloat()
                trySend(azimuthDeg)
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        try {
            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } catch (_: Exception) {
        }

        awaitClose {
            try {
                sensorManager.unregisterListener(listener)
            } catch (_: Exception) {
            }
        }
    }

    fun sendMediaKey(context: Context, keyCode: Int) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
            val now = SystemClock.uptimeMillis()
            audioManager.dispatchMediaKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_DOWN, keyCode, 0))
            audioManager.dispatchMediaKeyEvent(KeyEvent(now, now, KeyEvent.ACTION_UP, keyCode, 0))
        } catch (_: Exception) {
        }
    }
}
