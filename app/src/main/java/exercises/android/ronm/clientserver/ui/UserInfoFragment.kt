package exercises.android.ronm.clientserver.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.work.*
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.gson.Gson
import exercises.android.ronm.clientserver.ClientServerApp
import exercises.android.ronm.clientserver.R
import exercises.android.ronm.clientserver.server.ServerInterface
import exercises.android.ronm.clientserver.workers.KEY_INPUT_TOKEN
import exercises.android.ronm.clientserver.workers.KEY_OUTPUT_USER_INFO
import exercises.android.ronm.clientserver.workers.UserInfoGetterWorker


class UserInfoFragment : Fragment(R.layout.fragment_user_info) {

    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var imageViewUserImage: ImageView
    private lateinit var textViewUserName: TextView
    private lateinit var textViewPrettyName: TextView
    private lateinit var appContext: ClientServerApp
    private lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get appContext
        appContext = activity?.applicationContext as ClientServerApp
        // init nav controller
        navController = (activity?.supportFragmentManager?.findFragmentById(R.id.navHostFragment) as NavHostFragment).navController
        // find all views
        progressIndicator = view.findViewById(R.id.progressUserInfo)
        imageViewUserImage = view.findViewById(R.id.imageViewUserImage)
        textViewUserName = view.findViewById(R.id.textViewUsername)
        textViewPrettyName = view.findViewById(R.id.textViewPrettyName)
        // hide all views but progress-indicator
        hideUserInfoViews()
        // start info getter worker and update views upon success
        startInfoGetterWorker()


    }


    private fun startInfoGetterWorker() {
        if (appContext.token == "") {
            return // safety check for unexpected calls
        }
        val workManager = activity?.application?.let { WorkManager.getInstance(it) }
        val inputData = workDataOf(KEY_INPUT_TOKEN to appContext.token)
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val workRequest = OneTimeWorkRequestBuilder<UserInfoGetterWorker>().setInputData(inputData).setConstraints(constraints).build()
        workManager?.enqueue(workRequest)
        // set live-data observer for result
        workManager?.getWorkInfoByIdLiveData(workRequest.id)?.observe(viewLifecycleOwner, { workInfo ->
            if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                val userInfoJson = workInfo.outputData.getString(KEY_OUTPUT_USER_INFO)
                val userInfo = Gson().fromJson(userInfoJson, ServerInterface.User::class.java)
                showUserInfoViews(userInfo)
            }
        })
    }


    private fun showUserInfoViews(userInfo: ServerInterface.User) {
        progressIndicator.visibility = View.INVISIBLE
        imageViewUserImage.visibility = View.VISIBLE
        textViewUserName.visibility = View.VISIBLE
        textViewPrettyName.visibility = View.VISIBLE
        // load the user info into views
        textViewUserName.text = userInfo.username
        textViewPrettyName.text = userInfo.pretty_name
    }

    private fun hideUserInfoViews() {
        progressIndicator.visibility = View.VISIBLE
        imageViewUserImage.visibility = View.INVISIBLE
        textViewUserName.visibility = View.INVISIBLE
        textViewPrettyName.visibility = View.INVISIBLE
    }
}