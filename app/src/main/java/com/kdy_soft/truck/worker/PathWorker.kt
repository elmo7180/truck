package com.kdy_soft.truck.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class PathWorker(context: Context, params: WorkerParameters) : Worker(context, params) {

    override fun doWork(): Result {
        return Result.success()
    }
    companion object{
        const val WORKER_UNIQUE_NAME = "path_worker"
    }
}