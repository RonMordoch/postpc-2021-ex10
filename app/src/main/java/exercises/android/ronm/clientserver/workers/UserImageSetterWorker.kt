package exercises.android.ronm.clientserver.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.google.gson.Gson
import exercises.android.ronm.clientserver.server.BASE_URL
import exercises.android.ronm.clientserver.server.ServerHolder
import exercises.android.ronm.clientserver.server.ServerInterface

const val KEY_INPUT_USER_IMG = "key_input_user_img"

class UserImageSetterWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val imgUrl = inputData.getString(KEY_INPUT_USER_IMG) ?: return Result.failure()
        val token = inputData.getString(KEY_INPUT_TOKEN) ?: return Result.failure()
        val request = ServerInterface.SetUserImageRequest(imgUrl)
        val server = ServerHolder.serverInterface
        val response = server.setUserImage("token $token", request).execute()
        if (!response.isSuccessful) {
            return Result.failure()
        }
        val result = response.body() ?: return Result.failure()
        result.data.image_url = BASE_URL + result.data.image_url // return the full image url
        return Result.success(workDataOf(KEY_OUTPUT_USER_INFO to Gson().toJson(result.data)))
    }
}