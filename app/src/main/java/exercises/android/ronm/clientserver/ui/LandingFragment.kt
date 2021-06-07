package exercises.android.ronm.clientserver.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.EditText
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.work.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import exercises.android.ronm.clientserver.ClientServerApp
import exercises.android.ronm.clientserver.R
import exercises.android.ronm.clientserver.UserInfoViewModel
import exercises.android.ronm.clientserver.workers.KEY_INPUT_USERNAME
import exercises.android.ronm.clientserver.workers.KEY_OUTPUT_TOKEN
import exercises.android.ronm.clientserver.workers.UserTokenGetterWorker

class LandingFragment : Fragment(R.layout.fragment_landing) {

    private lateinit var editTextUsername: EditText
    private lateinit var fabGetToken: FloatingActionButton
    private lateinit var progressUserToken: CircularProgressIndicator
    private lateinit var appContext: ClientServerApp
    private lateinit var navController: NavController

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get appContext
        appContext = activity?.applicationContext as ClientServerApp
        // init nav controller
        navController = (activity?.supportFragmentManager?.findFragmentById(R.id.navHostFragment) as NavHostFragment).navController
        if (appContext.token != "") {
            // navigate to next fragment
            navController.navigate(R.id.action_landingFragment_to_userInfoFragment)
        }
        // else find and init views
        editTextUsername = view.findViewById(R.id.editTextUsername)
        fabGetToken = view.findViewById(R.id.fabGetToken)
        progressUserToken = view.findViewById(R.id.progressUserToken)
        editTextUsername.visibility = View.VISIBLE
        fabGetToken.visibility = View.VISIBLE

        progressUserToken.visibility = View.INVISIBLE

        // disable button and set listener to editText for valid username
        fabGetToken.isEnabled = false
        editTextUsername.doOnTextChanged { inputText, _, _, _ ->
            if (inputText != null) {
                fabGetToken.isEnabled = inputText.all { it.isLetterOrDigit() }
            }
        }

        // set on-click listener for button
        fabGetToken.setOnClickListener {
            fabGetToken.hide()
            startTokenGetterWorker()
        }
    }

    private fun startTokenGetterWorker() {
        val workManager = activity?.application?.let { WorkManager.getInstance(it) }
        val username = editTextUsername.text.toString()
        val inputData = workDataOf(KEY_INPUT_USERNAME to username)
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val workRequest = OneTimeWorkRequestBuilder<UserTokenGetterWorker>().setInputData(inputData).setConstraints(constraints).build()
        workManager?.enqueue(workRequest)
        // set live-data observer for result
        workManager?.getWorkInfoByIdLiveData(workRequest.id)?.observe(viewLifecycleOwner, { workInfo ->
            if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                appContext.token = workInfo.outputData.getString(KEY_OUTPUT_TOKEN).toString()
                // navigate to next fragment
                navController.navigate(R.id.action_landingFragment_to_userInfoFragment)
            }
        })
    }

}