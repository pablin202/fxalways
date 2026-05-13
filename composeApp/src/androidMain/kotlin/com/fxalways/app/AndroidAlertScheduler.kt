package com.fxalways.app

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object AndroidAlertScheduler {
    private const val PERIODIC_WORK_NAME = "fx_always_price_alerts"
    private const val STARTUP_WORK_NAME = "fx_always_price_alerts_startup"

    fun schedule(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val periodic = PeriodicWorkRequestBuilder<PriceAlertWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        val startup = OneTimeWorkRequestBuilder<PriceAlertWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodic,
        )
        WorkManager.getInstance(context).enqueueUniqueWork(
            STARTUP_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            startup,
        )
    }
}
