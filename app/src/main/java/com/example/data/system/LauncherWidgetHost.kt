package com.example.data.system

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle

data class InstalledWidgetProvider(
    val info: AppWidgetProviderInfo,
    val label: String,
    val packageName: String,
    val minWidthDp: Int,
    val minHeightDp: Int,
)

class LauncherWidgetHost(private val context: Context) {

    companion object {
        const val HOST_ID = 1024
    }

    val appWidgetManager: AppWidgetManager? = try {
        AppWidgetManager.getInstance(context)
    } catch (_: Exception) {
        null
    }

    val appWidgetHost: AppWidgetHost? = try {
        AppWidgetHost(context.applicationContext, HOST_ID)
    } catch (_: Exception) {
        null
    }

    fun startListening() {
        try {
            appWidgetHost?.startListening()
        } catch (_: Exception) {
        }
    }

    fun stopListening() {
        try {
            appWidgetHost?.stopListening()
        } catch (_: Exception) {
        }
    }

    fun getInstalledProviders(): List<InstalledWidgetProvider> {
        val manager = appWidgetManager ?: return emptyList()
        val pm = context.packageManager
        return try {
            manager.installedProviders.map { info ->
                InstalledWidgetProvider(
                    info = info,
                    label = info.loadLabel(pm).orEmpty().ifBlank { info.provider.shortClassName },
                    packageName = info.provider.packageName,
                    minWidthDp = info.minWidth.coerceAtLeast(110),
                    minHeightDp = info.minHeight.coerceAtLeast(110),
                )
            }.sortedBy { it.label.lowercase() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun allocateAppWidgetId(): Int? {
        return try {
            appWidgetHost?.allocateAppWidgetId()
        } catch (_: Exception) {
            null
        }
    }

    fun deleteAppWidgetId(appWidgetId: Int) {
        try {
            appWidgetHost?.deleteAppWidgetId(appWidgetId)
        } catch (_: Exception) {
        }
    }

    fun bindIfAllowed(appWidgetId: Int, provider: ComponentName): Boolean {
        return try {
            appWidgetManager?.bindAppWidgetIdIfAllowed(appWidgetId, provider) == true
        } catch (_: Exception) {
            false
        }
    }

    fun createBindIntent(appWidgetId: Int, provider: ComponentName): Intent {
        return Intent(AppWidgetManager.ACTION_APPWIDGET_BIND).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider)
        }
    }

    fun getProviderInfo(appWidgetId: Int): AppWidgetProviderInfo? {
        return try {
            appWidgetManager?.getAppWidgetInfo(appWidgetId)
        } catch (_: Exception) {
            null
        }
    }

    fun createHostView(contextForView: Context, appWidgetId: Int): AppWidgetHostView? {
        val info = getProviderInfo(appWidgetId) ?: return null
        return try {
            appWidgetHost?.createView(contextForView, appWidgetId, info)?.apply {
                setAppWidget(appWidgetId, info)
            }
        } catch (_: Exception) {
            null
        }
    }

    fun updateWidgetSize(appWidgetId: Int, widthDp: Int, heightDp: Int) {
        try {
            val opts = Bundle().apply {
                putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, widthDp)
                putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, widthDp)
                putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, heightDp)
                putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, heightDp)
            }
            appWidgetManager?.updateAppWidgetOptions(appWidgetId, opts)
        } catch (_: Exception) {
        }
    }
}
