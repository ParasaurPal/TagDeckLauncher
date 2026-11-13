package com.example.data.system

import android.app.role.RoleManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Process
import android.os.UserHandle
import android.provider.Settings
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

data class DiscoveredApp(
    val packageName: String,
    val componentName: String,
    val label: String,
    val installTimeMillis: Long = 0L,
)

class AppDiscoveryManager(val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private val launcherApps: LauncherApps? =
        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as? LauncherApps

    private val iconCache = ConcurrentHashMap<String, ImageBitmap>()
    val iconPackManager: IconPackManager = IconPackManager(context)

    suspend fun discoverLaunchableApps(): List<DiscoveredApp> = withContext(Dispatchers.IO) {
        val ownPackage = context.packageName
        val results = LinkedHashMap<String, DiscoveredApp>()

        val activities = try {
            launcherApps?.getActivityList(null, Process.myUserHandle()).orEmpty()
        } catch (_: Exception) {
            emptyList()
        }

        if (activities.isNotEmpty()) {
            for (info in activities) {
                val pkg = info.applicationInfo.packageName
                if (pkg == ownPackage) continue
                if (!results.containsKey(pkg)) {
                    val label = info.label?.toString()?.takeIf { it.isNotBlank() } ?: pkg
                    val comp = info.componentName.flattenToString()
                    val installTime = getInstallTime(pkg)
                    results[pkg] = DiscoveredApp(pkg, comp, label, installTime)
                    try {
                        val drawable = info.getIcon(0)
                        drawableToBitmap(drawable)?.asImageBitmap()?.let {
                            iconCache[pkg] = it
                        }
                    } catch (_: Exception) {
                    }
                }
            }
        } else {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.queryIntentActivities(
                        intent,
                        PackageManager.ResolveInfoFlags.of(0L)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    packageManager.queryIntentActivities(intent, 0)
                }
            } catch (_: Exception) {
                emptyList()
            }
            for (ri in resolveInfos) {
                val pkg = ri.activityInfo?.packageName ?: continue
                if (pkg == ownPackage) continue
                if (!results.containsKey(pkg)) {
                    val label = ri.loadLabel(packageManager)?.toString()?.takeIf { it.isNotBlank() } ?: pkg
                    val comp = ComponentName(pkg, ri.activityInfo.name).flattenToString()
                    results[pkg] = DiscoveredApp(pkg, comp, label, getInstallTime(pkg))
                    try {
                        val drawable = ri.loadIcon(packageManager)
                        drawableToBitmap(drawable)?.asImageBitmap()?.let {
                            iconCache[pkg] = it
                        }
                    } catch (_: Exception) {
                    }
                }
            }
        }
        results.values.sortedBy { it.label.lowercase() }
    }

    private fun getInstallTime(packageName: String): Long {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0L)).firstInstallTime
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, 0).firstInstallTime
            }
        } catch (_: Exception) {
            0L
        }
    }

    fun getCachedIcon(packageName: String): ImageBitmap? {
        iconCache[packageName]?.let { return it }
        return try {
            val drawable = packageManager.getApplicationIcon(packageName)
            drawableToBitmap(drawable)?.asImageBitmap()?.also { iconCache[packageName] = it }
        } catch (_: Exception) {
            null
        }
    }

    fun invalidatePackageIcon(packageName: String) {
        if (packageName == "batch") {
            iconCache.clear()
            iconPackManager.clearAllCaches()
        } else {
            iconCache.remove(packageName)
            iconPackManager.invalidatePackage(packageName)
        }
    }

    fun launchApp(packageName: String, componentName: String? = null): Boolean {
        return try {
            if (componentName != null && componentName.isNotBlank() && launcherApps != null) {
                val comp = ComponentName.unflattenFromString(componentName)
                if (comp != null) {
                    launcherApps.startMainActivity(comp, Process.myUserHandle(), null, null)
                    return true
                }
            }
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            try {
                val fallback = packageManager.getLaunchIntentForPackage(packageName)
                if (fallback != null) {
                    fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(fallback)
                    true
                } else false
            } catch (_: Exception) {
                false
            }
        }
    }

    fun openAppInfo(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun requestUninstallApp(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun openUrl(url: String) {
        try {
            val normalized = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(normalized)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun openSystemSettings(action: String = Settings.ACTION_SETTINGS) {
        try {
            val intent = Intent(action).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            context.startActivity(intent)
        } catch (_: Exception) {
        }
    }

    fun isDefaultLauncher(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                return roleManager.isRoleHeld(RoleManager.ROLE_HOME)
            }
        }
        val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        val res = packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY)
        return res?.activityInfo?.packageName == context.packageName
    }

    fun buildRequestDefaultLauncherIntent(): Intent {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val roleManager = context.getSystemService(RoleManager::class.java)
            if (roleManager != null && roleManager.isRoleAvailable(RoleManager.ROLE_HOME) && !roleManager.isRoleHeld(RoleManager.ROLE_HOME)) {
                return roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME)
            }
        }
        return Intent(Settings.ACTION_HOME_SETTINGS)
    }

    fun observePackageChanges(): Flow<String> = callbackFlow {
        val launcherCallback = object : LauncherApps.Callback() {
            override fun onPackageRemoved(packageName: String, user: UserHandle) {
                invalidatePackageIcon(packageName)
                trySend(packageName)
            }

            override fun onPackageAdded(packageName: String, user: UserHandle) {
                invalidatePackageIcon(packageName)
                trySend(packageName)
            }

            override fun onPackageChanged(packageName: String, user: UserHandle) {
                invalidatePackageIcon(packageName)
                trySend(packageName)
            }

            override fun onPackagesAvailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) {
                packageNames.forEach { invalidatePackageIcon(it) }
                trySend("batch")
            }

            override fun onPackagesUnavailable(packageNames: Array<out String>, user: UserHandle, replacing: Boolean) {
                packageNames.forEach { invalidatePackageIcon(it) }
                trySend("batch")
            }
        }

        try {
            launcherApps?.registerCallback(launcherCallback)
        } catch (_: Exception) {
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val pkg = intent?.data?.schemeSpecificPart ?: "batch"
                invalidatePackageIcon(pkg)
                trySend(pkg)
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
            } else {
                @Suppress("UnspecifiedRegisterReceiverFlag")
                context.registerReceiver(receiver, filter)
            }
        } catch (_: Exception) {
        }

        awaitClose {
            try {
                launcherApps?.unregisterCallback(launcherCallback)
            } catch (_: Exception) {
            }
            try {
                context.unregisterReceiver(receiver)
            } catch (_: Exception) {
            }
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap? {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 144
        val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 144
        val targetW = width.coerceAtMost(192)
        val targetH = height.coerceAtMost(192)
        return try {
            val bitmap = Bitmap.createBitmap(targetW, targetH, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        } catch (_: Exception) {
            null
        }
    }
}
