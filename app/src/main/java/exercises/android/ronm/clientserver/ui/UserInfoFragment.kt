package exercises.android.ronm.clientserver.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.work.*
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.gson.Gson
import exercises.android.ronm.clientserver.ClientServerApp
import exercises.android.ronm.clientserver.R
import exercises.android.ronm.clientserver.viewmodels.UserInfoViewModel
import exercises.android.ronm.clientserver.server.ServerInterface
import exercises.android.ronm.clientserver.workers.*


class UserInfoFragment : Fragment(R.layout.fragment_user_info) {

    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var imageViewUserImage: ImageView
    private lateinit var textViewPrettyName : TextView
    private lateinit var fabEditUserInfo : FloatingActionButton
    private lateinit var appContext: ClientServerApp
    private val userInfoViewModel : UserInfoViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get application context
        appContext = activity?.applicationContext as ClientServerApp
        // find all views
        progressIndicator = view.findViewById(R.id.progressUserInfo)
        imageViewUserImage = view.findViewById(R.id.imageViewUserImage)
        textViewPrettyName = view.findViewById(R.id.textViewPrettyName)
        fabEditUserInfo = view.findViewById(R.id.fabEditUserInfo)
        // hide all views but progress-indicator
        hideUserInfoViews()
        // start info getter worker and update views upon success
        startInfoGetterWorker()

        // set on-click listener for fab to enable pretty-name editing
        fabEditUserInfo.setOnClickListener {
            fabEditUserInfo.hide()
            findNavController().navigate(R.id.action_userInfoFragment_to_editUserInfoFragment)
        }
        // set an observer for user info live data for UI updates
        userInfoViewModel.userInfoLiveData.observe(viewLifecycleOwner, { userInfo ->
            // load the user info into views
            textViewPrettyName.text = getString(R.string.user_welcome_msg, userInfoViewModel.displayName)
            if (userInfoViewModel.fullImgUrl != "") {
                Glide.with(this).load(userInfoViewModel.fullImgUrl).into(imageViewUserImage)
            }
        })
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
                userInfoViewModel.setUserInfo(Gson().fromJson(userInfoJson, ServerInterface.User::class.java))
                showUserInfoViews()
            }
        })
    }

    private fun showUserInfoViews() {
        progressIndicator.visibility = View.INVISIBLE
        imageViewUserImage.visibility = View.VISIBLE
        textViewPrettyName.visibility = View.VISIBLE
        fabEditUserInfo.visibility = View.VISIBLE

    }

    private fun hideUserInfoViews() {
        progressIndicator.visibility = View.VISIBLE
        imageViewUserImage.visibility = View.INVISIBLE
        textViewPrettyName.visibility = View.INVISIBLE
        fabEditUserInfo.visibility = View.INVISIBLE
    }

}