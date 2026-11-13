package com.example.data.system

import android.app.Activity
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast

/**
 * A small, non-exported gate that asks Android to authenticate with the device's
 * existing screen lock before launching a protected app. TagDeck never stores
 * or handles the user's PIN, password, or biometric data.
 */
class AppLockGateActivity : Activity() {

    companion object {
        private const val EXTRA_PACKAGE = "com.tagdeck.launcher.extra.LOCKED_PACKAGE"
        private const val EXTRA_COMPONENT = "com.tagdeck.launcher.extra.LOCKED_COMPONENT"
        private const val REQUEST_CONFIRM_DEVICE = 7412

        fun launch(context: Context, packageName: String, componentName: String? = null) {
            context.startActivity(
                Intent(context, AppLockGateActivity::class.java)
                    .putExtra(EXTRA_PACKAGE, packageName)
                    .putExtra(EXTRA_COMPONENT, componentName)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val keyguard = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        val isSecure = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            keyguard?.isDeviceSecure == true
        } else {
            @Suppress("DEPRECATION")
            keyguard?.isKeyguardSecure == true
        }

        if (!isSecure) {
            Toast.makeText(
                this,
                "Set a secure screen lock before using App Lock.",
                Toast.LENGTH_LONG
            ).show()
            runCatching { startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS)) }
            finish()
            return
        }

        val confirmation = keyguard?.createConfirmDeviceCredentialIntent(
            "Unlock protected app",
            "Authenticate to open this app"
        )
        if (confirmation == null) {
            Toast.makeText(this, "Device authentication is unavailable.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        @Suppress("DEPRECATION")
        startActivityForResult(confirmation, REQUEST_CONFIRM_DEVICE)
    }

    @Deprecated("Uses the platform device-credential confirmation activity")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CONFIRM_DEVICE && resultCode == RESULT_OK) {
            val packageName = intent.getStringExtra(EXTRA_PACKAGE)
            val componentName = intent.getStringExtra(EXTRA_COMPONENT)
            if (!packageName.isNullOrBlank()) {
                val launchedFromProfile = runCatching {
                    if (componentName.isNullOrBlank()) false else {
                        val serial = if (componentName.startsWith("tdprofile:")) {
                            componentName.substringAfter("tdprofile:").substringBefore("|").toLongOrNull()
                        } else null
                        val flattened = if (componentName.startsWith("tdprofile:")) componentName.substringAfter("|") else componentName
                        val component = android.content.ComponentName.unflattenFromString(flattened)
                        val user = if (serial != null) {
                            (getSystemService(Context.USER_SERVICE) as? android.os.UserManager)?.getUserForSerialNumber(serial)
                        } else android.os.Process.myUserHandle()
                        val launcherApps = getSystemService(Context.LAUNCHER_APPS_SERVICE) as? android.content.pm.LauncherApps
                        if (component != null && user != null && launcherApps != null) {
                            launcherApps.startMainActivity(component, user, null, null)
                            true
                        } else false
                    }
                }.getOrDefault(false)
                if (!launchedFromProfile) {
                    val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        runCatching { startActivity(launchIntent) }
                    }
                }
            }
        }
        finish()
    }
}
