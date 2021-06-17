package exercises.android.ronm.clientserver.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.EditText
import androidx.annotation.VisibleForTesting
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import exercises.android.ronm.clientserver.R
import exercises.android.ronm.clientserver.viewmodels.UserInfoViewModel

class LandingFragment : Fragment(R.layout.fragment_landing) {

    private lateinit var editTextUsername: EditText
    private lateinit var fabGetToken: FloatingActionButton
    private lateinit var progressUserToken: CircularProgressIndicator

    private val userInfoViewModel: UserInfoViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (userInfoViewModel.tokenLiveData.value != "") {
            // navigate to next fragment
            findNavController().navigate(R.id.action_landingFragment_to_userInfoFragment)
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
                fabGetToken.isEnabled = userInfoViewModel.isUsernameValid(inputText.toString())
            }
        }

        // set on-click listener for button
        fabGetToken.setOnClickListener {
            fabGetToken.hide()
            progressUserToken.visibility = View.VISIBLE
            val username = editTextUsername.text.toString()
            userInfoViewModel.startTokenGetterWorker(username)
        }

        userInfoViewModel.tokenLiveData.observe(viewLifecycleOwner, { token ->
            if (token != ""){
                findNavController().navigate(R.id.action_landingFragment_to_userInfoFragment)
            }
        })
    }


}