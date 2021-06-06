package exercises.android.ronm.clientserver.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class UserInfoWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        TODO("Not yet implemented")
    }
}