package com.example

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.SvgDecoder
import com.example.data.db.LauncherDatabase
import com.example.data.prefs.LauncherPreferencesRepository
import com.example.data.repository.LauncherRepository
import com.example.data.system.AppDiscoveryManager
import com.example.data.system.ImageStorageManager
import com.example.data.system.LauncherWidgetHost
import com.example.data.system.CloudBackupScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TagDeckApplication : Application(), ImageLoaderFactory {

    lateinit var database: LauncherDatabase
        private set
    lateinit var preferencesRepository: LauncherPreferencesRepository
        private set
    lateinit var appDiscoveryManager: AppDiscoveryManager
        private set
    lateinit var imageStorageManager: ImageStorageManager
        private set
    lateinit var widgetHost: LauncherWidgetHost
        private set
    lateinit var launcherRepository: LauncherRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = LauncherDatabase.getInstance(this)
        preferencesRepository = LauncherPreferencesRepository(this)
        appDiscoveryManager = AppDiscoveryManager(this)
        imageStorageManager = ImageStorageManager(this)
        widgetHost = LauncherWidgetHost(this)
        launcherRepository = LauncherRepository(
            dao = database.launcherDao(),
            appDiscovery = appDiscoveryManager,
            prefsRepo = preferencesRepository,
            imageStorage = imageStorageManager,
        )
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            val settings = preferencesRepository.getSettingsOnce()
            if (settings.cloudBackupEnabled && !settings.cloudBackupTreeUri.isNullOrBlank()) {
                CloudBackupScheduler.schedule(this@TagDeckApplication)
            }
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(SvgDecoder.Factory())
            }
            .crossfade(true)
            .build()
    }
}
