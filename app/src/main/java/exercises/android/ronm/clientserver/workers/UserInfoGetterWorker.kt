package exercises.android.ronm.clientserver.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import exercises.android.ronm.clientserver.server.BASE_URL
import exercises.android.ronm.clientserver.server.ServerHolder

const val KEY_INPUT_TOKEN = "key_input_token"
const val KEY_OUTPUT_USER_INFO = "key_output_user_info"

class UserInfoGetterWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        // if token is null from input data, we have an error, return failure
        val token = inputData.getString(KEY_INPUT_TOKEN) ?: return Result.failure()
        val server = ServerHolder.serverInterface
        val response = server.getUserInfo("token $token").execute()
        if (!response.isSuccessful) {
            return Result.failure()
        }
        val result = response.body() ?: return Result.failure()
        return Result.success(workDataOf(KEY_OUTPUT_USER_INFO to Gson().toJson(result.data)))
    }
}