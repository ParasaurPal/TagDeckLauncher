package com.example.data.system

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.TagDeckApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class CloudBackupWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val app = applicationContext as? TagDeckApplication ?: return@withContext Result.failure()
        val settings = app.preferencesRepository.getSettingsOnce()
        if (!settings.cloudBackupEnabled) return@withContext Result.success()
        val treeUri = settings.cloudBackupTreeUri?.let(Uri::parse)
            ?: return@withContext Result.failure()
        val tree = DocumentFile.fromTreeUri(applicationContext, treeUri)
            ?: return@withContext Result.failure()
        val json = runCatching { app.launcherRepository.exportFullBackupJson(includeEmbeddedImages = true) }
            .getOrElse { return@withContext Result.retry() }
        runCatching {
            tree.findFile(FILE_NAME)?.delete()
            val file = tree.createFile("application/json", FILE_NAME)
                ?: error("Cloud provider could not create the backup file")
            applicationContext.contentResolver.openOutputStream(file.uri, "wt")?.use {
                it.write(json.toByteArray(Charsets.UTF_8))
                it.flush()
            } ?: error("Cloud provider did not open the backup file")
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() },
        )
    }

    companion object {
        const val UNIQUE_WORK_NAME = "tagdeck_periodic_cloud_backup"
        const val FILE_NAME = "TagDeck-AutoBackup.json"
    }
}

object CloudBackupScheduler {
    fun schedule(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<CloudBackupWorker>(12, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            CloudBackupWorker.UNIQUE_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
        val initialBackup = OneTimeWorkRequestBuilder<CloudBackupWorker>()
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            "tagdeck_initial_cloud_backup",
            ExistingWorkPolicy.KEEP,
            initialBackup,
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(CloudBackupWorker.UNIQUE_WORK_NAME)
    }
}
