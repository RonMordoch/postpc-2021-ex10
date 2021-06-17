package exercises.android.ronm.clientserver.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import exercises.android.ronm.clientserver.server.ServerHolder

const val KEY_INPUT_USERNAME = "key_input_username"
const val KEY_OUTPUT_TOKEN = "key_output_token"

class UserTokenGetterWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        // if username is null from input data, we have an error, return failure
        val username = inputData.getString(KEY_INPUT_USERNAME) ?: return Result.failure()
        val server = ServerHolder.serverInterface
        try {
            val response = server.getToken(username).execute()
            if (!response.isSuccessful) {
                return Result.failure()
            }
            val result = response.body() ?: return Result.failure()
            return Result.success(workDataOf(KEY_OUTPUT_TOKEN to result.data))
        } catch (exception: Exception) {
            return Result.retry() // retry work if any error happened when communicating with server
        }
    }
}