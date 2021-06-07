package exercises.android.ronm.clientserver.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.work.*
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.gson.Gson
import exercises.android.ronm.clientserver.ClientServerApp
import exercises.android.ronm.clientserver.R
import exercises.android.ronm.clientserver.server.ServerInterface
import exercises.android.ronm.clientserver.workers.*


class UserInfoFragment : Fragment(R.layout.fragment_user_info) {

    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var imageViewUserImage: ImageView
    private lateinit var editTextPrettyName: EditText
    private lateinit var textViewPrettyName : TextView
    private lateinit var fabStartEdit : FloatingActionButton
    private lateinit var fabFinishEdit : FloatingActionButton
    private lateinit var appContext: ClientServerApp
    private lateinit var navController: NavController
    private lateinit var userInfo : ServerInterface.User

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get appContext
        appContext = activity?.applicationContext as ClientServerApp
        // init nav controller
        navController = (activity?.supportFragmentManager?.findFragmentById(R.id.navHostFragment) as NavHostFragment).navController
        // find all views
        progressIndicator = view.findViewById(R.id.progressUserInfo)
        imageViewUserImage = view.findViewById(R.id.imageViewUserImage)
        editTextPrettyName = view.findViewById(R.id.editTextPrettyName)
        textViewPrettyName = view.findViewById(R.id.textViewPrettyName)
        fabStartEdit = view.findViewById(R.id.fabStartEditPrettyName)
        fabFinishEdit = view.findViewById(R.id.fabFinishEditPrettyName)
        // hide all views but progress-indicator
        hideUserInfoViews()
        // start info getter worker and update views upon success
        startInfoGetterWorker()

        // set on-click listener for fab to enable pretty-name editing
        fabStartEdit.setOnClickListener {
            startEdit()
        }

        fabFinishEdit.setOnClickListener {
            finishEdit()
        }
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
                userInfo = Gson().fromJson(userInfoJson, ServerInterface.User::class.java)
                showUserInfoViews()
            }
        })
    }

    private fun startInfoSetterWorker(){
        if (appContext.token == "") {
            return // safety check for unexpected calls
        }
        val workManager = activity?.application?.let { WorkManager.getInstance(it) }
        val prettyName = editTextPrettyName.text.toString()
        val inputData = workDataOf(KEY_INPUT_TOKEN to appContext.token, KEY_INPUT_PRETTY_NAME to prettyName)
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val workRequest = OneTimeWorkRequestBuilder<UserPrettyNameSetterWorker>().setInputData(inputData).setConstraints(constraints).build()
        workManager?.enqueue(workRequest)
        // set live-data observer for result
        workManager?.getWorkInfoByIdLiveData(workRequest.id)?.observe(viewLifecycleOwner, { workInfo ->
            if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                val userInfoJson = workInfo.outputData.getString(KEY_OUTPUT_USER_INFO)
                userInfo = Gson().fromJson(userInfoJson, ServerInterface.User::class.java)
                showUserInfoViews()
            }
        })
    }


    private fun showUserInfoViews() {
        // load the user info into views
        val displayName = if (userInfo.pretty_name == null || userInfo.pretty_name == "") userInfo.username else userInfo.pretty_name
        textViewPrettyName.text = getString(R.string.user_welcome_msg,displayName)
        if (userInfo.image_url != "") {
            Glide.with(this).load(userInfo.image_url).into(imageViewUserImage)
        }
        // enable views visibility only after image loaded
        progressIndicator.visibility = View.INVISIBLE
        imageViewUserImage.visibility = View.VISIBLE
        textViewPrettyName.visibility = View.VISIBLE
        editTextPrettyName.visibility = View.INVISIBLE
        fabStartEdit.visibility = View.VISIBLE
        fabStartEdit.isEnabled = true

    }

    private fun hideUserInfoViews() {
        progressIndicator.visibility = View.VISIBLE
        imageViewUserImage.visibility = View.INVISIBLE
        textViewPrettyName.visibility = View.INVISIBLE
        editTextPrettyName.visibility = View.INVISIBLE
        fabStartEdit.visibility = View.INVISIBLE
        fabFinishEdit.visibility = View.INVISIBLE
    }

    private fun startEdit(){
        fadeOutAnimation(fabStartEdit)
        fadeInAnimation(fabFinishEdit)
        textViewPrettyName.visibility = View.INVISIBLE
        editTextPrettyName.visibility = View.VISIBLE
        editTextPrettyName.setText(if (userInfo.pretty_name == "") userInfo.username else userInfo.pretty_name)
    }

    private fun finishEdit(){
        fadeOutAnimation(fabFinishEdit)
        fadeInAnimation(fabStartEdit)
        textViewPrettyName.visibility = View.VISIBLE
        editTextPrettyName.visibility = View.INVISIBLE
        fabStartEdit.isEnabled = false // disable until response
        startInfoSetterWorker()
    }

    private fun fadeInAnimation(view: View) {
        view.visibility = View.VISIBLE
        view.alpha = 0f
        view.animate()
            .alpha(1f)
            .setDuration(200L)
            .start()
    }

    private fun fadeOutAnimation(view: View) {
        view.animate()
            .alpha(0f)
            .setStartDelay(100L)
            .setDuration(200L)
            .withEndAction { view.visibility = View.INVISIBLE }.start()
    }
}