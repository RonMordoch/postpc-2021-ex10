package exercises.android.ronm.clientserver.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.doOnTextChanged
import androidx.work.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.gson.Gson
import exercises.android.ronm.clientserver.ClientServerApp
import exercises.android.ronm.clientserver.R
import exercises.android.ronm.clientserver.server.ServerInterface
import exercises.android.ronm.clientserver.workers.*

class MainActivity : AppCompatActivity() {

    private lateinit var editTextUsername: EditText
    private lateinit var fabGetToken: FloatingActionButton
    private lateinit var progressIndicator : CircularProgressIndicator
    private lateinit var imageViewUserImage : ImageView
    private lateinit var textViewUserName : TextView
    private lateinit var textViewPrettyName : TextView
    private val workManager: WorkManager = WorkManager.getInstance(application)
    private lateinit var appContext : ClientServerApp


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // get appContext
        appContext =  applicationContext as ClientServerApp

        // find all views
        editTextUsername = findViewById(R.id.editTextUsername)
        fabGetToken = findViewById(R.id.fabGetToken)
        progressIndicator = findViewById(R.id.progressIndicator)
        imageViewUserImage = findViewById(R.id.imageViewUserImage)
        textViewUserName = findViewById(R.id.textViewUsername)
        textViewPrettyName = findViewById(R.id.textViewPrettyName)

        // init view according to saved token
        if (appContext.token == ""){
            editTextUsername.visibility = View.VISIBLE
            fabGetToken.visibility = View.VISIBLE
            progressIndicator.visibility = View.INVISIBLE
            imageViewUserImage.visibility = View.INVISIBLE
            textViewUserName.visibility = View.INVISIBLE
            textViewPrettyName.visibility = View.INVISIBLE
        }
        else
        {
            // todo
        }

        // disable button and set listener to editText for valid username
        fabGetToken.isEnabled = false
        editTextUsername.doOnTextChanged { inputText, _, _, _ ->
            if (inputText != null) {
                fabGetToken.isEnabled = inputText.all { it.isLetterOrDigit() }
            }
        }

        // set on-click listener for button
        fabGetToken.setOnClickListener {
            startTokenGetterWorker()
        }
    }

    private fun startTokenGetterWorker() {
        val username = editTextUsername.text.toString()
        val inputData = workDataOf(KEY_INPUT_USERNAME to username)
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val workRequest = OneTimeWorkRequestBuilder<UserTokenGetterWorker>().setInputData(inputData).setConstraints(constraints).build()
        workManager.enqueue(workRequest)
        // set live-data observer for result
        workManager.getWorkInfoByIdLiveData(workRequest.id).observe(this, { workInfo ->
            if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                appContext.token = workInfo.outputData.getString(KEY_OUTPUT_TOKEN).toString()
                // todo maybe incorporate live data to simplify the following call to next worker, or maybe chain workers
                // start the user info worker
                startInfoGetterWorker()
            }
        })
    }

    private fun startInfoGetterWorker(){
        if (appContext.token == ""){
            return // safety check for unexpected calls
        }
        val inputData = workDataOf(KEY_INPUT_TOKEN to appContext.token)
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        var workRequest = OneTimeWorkRequestBuilder<UserInfoGetterWorker>().setInputData(inputData).setConstraints(constraints).build()
        workManager.enqueue(workRequest)
        // set live-data observer for result
        workManager.getWorkInfoByIdLiveData(workRequest.id).observe(this, {workInfo ->
            if (workInfo.state == WorkInfo.State.SUCCEEDED){
                val userInfoJson = workInfo.outputData.getString(KEY_OUTPUT_USER_INFO)
                val userInfo = Gson().fromJson(userInfoJson, ServerInterface.User::class.java)
                setUserInfoViews(userInfo)
            }
        })
    }

    private fun setUserInfoViews(userInfo : ServerInterface.User){
        textViewUserName.visibility = View.VISIBLE
        textViewPrettyName.visibility = View.VISIBLE
        textViewUserName.text = userInfo.username
        textViewPrettyName.text = userInfo.pretty_name

    }
}