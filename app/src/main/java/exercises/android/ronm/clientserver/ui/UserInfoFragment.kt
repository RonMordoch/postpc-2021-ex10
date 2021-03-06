package exercises.android.ronm.clientserver.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import exercises.android.ronm.clientserver.R
import exercises.android.ronm.clientserver.viewmodels.UserInfoViewModel



class UserInfoFragment : Fragment(R.layout.fragment_user_info) {

    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var imageViewUserImage: ImageView
    private lateinit var textViewPrettyName: TextView
    private lateinit var fabEditUserInfo: FloatingActionButton
    private val userInfoViewModel: UserInfoViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // find all views
        progressIndicator = view.findViewById(R.id.progressUserInfo)
        imageViewUserImage = view.findViewById(R.id.imageViewUserImage)
        textViewPrettyName = view.findViewById(R.id.textViewPrettyName)
        fabEditUserInfo = view.findViewById(R.id.fabEditUserInfo)
        // hide all views but progress-indicator
        hideUserInfoViews()
        // set on-click listener for fab to enable pretty-name editing
        fabEditUserInfo.setOnClickListener {
            fabEditUserInfo.hide()
            findNavController().navigate(R.id.action_userInfoFragment_to_editUserInfoFragment)
        }
        userInfoViewModel.isEditSuccessful.value = null // reset edit status when not in edit screen
        userInfoViewModel.userLiveData.observe(viewLifecycleOwner, {user ->
            textViewPrettyName.text = getString(R.string.user_welcome_msg, user.getDisplayName())
            if (user.image_url != ""){
                Glide.with(this).load(user.image_url).into(imageViewUserImage)
                showUserInfoViews() // show views only when we have the data for them
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