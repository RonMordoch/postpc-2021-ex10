package exercises.android.ronm.clientserver.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.work.*
import com.google.gson.Gson
import exercises.android.ronm.clientserver.models.User
import exercises.android.ronm.clientserver.workers.*

class UserInfoViewModel(application: Application) : AndroidViewModel(application) {

    val tokenLiveData = MutableLiveData("")
    val isEditSuccessful = MutableLiveData<Boolean?>(null)
    val userLiveData = MutableLiveData(User("", "", ""))
    private val workManager = WorkManager.getInstance(application)
    private val sp = application.getSharedPreferences(SP_NAME_TOKEN, Application.MODE_PRIVATE)

    init {
        tokenLiveData.value = sp.getString(SP_KEY_TOKEN, "")
        if (tokenLiveData.value != "") {
            startInfoGetterWorker()
        }
    }

    fun isUsernameValid(username: String): Boolean {
        return username.all { it.isLetterOrDigit() } && username != ""
    }

    fun startTokenGetterWorker(username: String) {
        val inputData = workDataOf(KEY_INPUT_USERNAME to username)
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val workRequest = OneTimeWorkRequestBuilder<UserTokenGetterWorker>().setInputData(inputData).setConstraints(constraints).build()
        workManager.enqueue(workRequest)
        val workInfoLiveData = workManager.getWorkInfoByIdLiveData(workRequest.id)
        workInfoLiveData.observeForever(object : Observer<WorkInfo> {
            override fun onChanged(workInfo: WorkInfo?) {
                if (workInfo == null) {
                    return
                }
                if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                    val token = workInfo.outputData.getString(KEY_OUTPUT_TOKEN).toString()
                    tokenLiveData.value = token
                    sp.edit().putString(SP_KEY_TOKEN, token).apply() // save token to shared preferences
                    startInfoGetterWorker()
                    workInfoLiveData.removeObserver(this)
                }
            }
        })
    }


    private fun startInfoGetterWorker() {
        val inputData = workDataOf(KEY_INPUT_TOKEN to tokenLiveData.value)
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val workRequest = OneTimeWorkRequestBuilder<UserInfoGetterWorker>().setInputData(inputData).setConstraints(constraints).build()
        workManager.enqueue(workRequest)
        val workInfoLiveData = workManager.getWorkInfoByIdLiveData(workRequest.id)
        workInfoLiveData.observeForever(object : Observer<WorkInfo> {
            override fun onChanged(workInfo: WorkInfo?) {
                if (workInfo == null) {
                    return
                }
                if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                    val userInfoJson = workInfo.outputData.getString(KEY_OUTPUT_USER_INFO)
                    val user = Gson().fromJson(userInfoJson, User::class.java)
                    userLiveData.value = user
                    workInfoLiveData.removeObserver(this)
                }
            }
        })
    }


    fun startUserInfoSetterWorkers(prettyName: String, imgUrl: String) {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val prettyNameInputData = workDataOf(KEY_INPUT_TOKEN to tokenLiveData.value, KEY_INPUT_PRETTY_NAME to prettyName)
        val prettyNameWorkRequest =
            OneTimeWorkRequestBuilder<PrettyNameSetterWorker>().setInputData(prettyNameInputData).setConstraints(constraints).build()
        val imgInputData = workDataOf(KEY_INPUT_TOKEN to tokenLiveData.value, KEY_INPUT_USER_IMG to imgUrl)
        val imgWorkRequest =
            OneTimeWorkRequestBuilder<UserImageSetterWorker>().setInputData(imgInputData).setConstraints(constraints).build()
        workManager.beginWith(prettyNameWorkRequest).then(imgWorkRequest).enqueue()
        val workInfoLiveData = workManager.getWorkInfoByIdLiveData(imgWorkRequest.id)
        workInfoLiveData.observeForever(object : Observer<WorkInfo> {
            override fun onChanged(workInfo: WorkInfo?) {
                if (workInfo == null) {
                    return
                }
                if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                    val userInfoJson = workInfo.outputData.getString(KEY_OUTPUT_USER_INFO)
                    val user = Gson().fromJson(userInfoJson, User::class.java)
                    userLiveData.value = user
                    isEditSuccessful.value = true
                    workInfoLiveData.removeObserver(this)
                } else if (workInfo.state == WorkInfo.State.FAILED) {
                    isEditSuccessful.value = false
                    workInfoLiveData.removeObserver(this)
                }
            }

        })
    }


    companion object {
        private const val SP_NAME_TOKEN = "sp_name_token"
        private const val SP_KEY_TOKEN = "sp_key_token"
    }
}



