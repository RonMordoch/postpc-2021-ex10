package exercises.android.ronm.clientserver.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import exercises.android.ronm.clientserver.R
import exercises.android.ronm.clientserver.viewmodels.UserInfoViewModel
import exercises.android.ronm.clientserver.server.BASE_URL

class EditUserInfoFragment : Fragment(R.layout.fragment_edit_user_info) {

    private lateinit var editTextPrettyName: EditText
    private lateinit var fabFinishEdit: FloatingActionButton
    private lateinit var imageViewCrab: ImageView
    private lateinit var imageViewUnicorn: ImageView
    private lateinit var imageViewAlien: ImageView
    private lateinit var imageViewRobot: ImageView
    private lateinit var imageViewOctopus: ImageView
    private lateinit var imageViewFrog: ImageView
    private lateinit var imageViewUserImage: ImageView
    private lateinit var imagesHashMap: HashMap<ImageView, String>
    private val userInfoViewModel: UserInfoViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // get application context
        val appContext = activity?.applicationContext
        // find all views
        editTextPrettyName = view.findViewById(R.id.editTextPrettyName)
        fabFinishEdit = view.findViewById(R.id.fabFinishEdit)
        imageViewCrab = view.findViewById(R.id.imageViewCrab)
        imageViewUnicorn = view.findViewById(R.id.imageViewUnicorn)
        imageViewAlien = view.findViewById(R.id.imageViewAlien)
        imageViewRobot = view.findViewById(R.id.imageViewRobot)
        imageViewOctopus = view.findViewById(R.id.imageViewOctopus)
        imageViewFrog = view.findViewById(R.id.imageViewFrog)
        // load all images
        initImageViews()
        // expand fab upon entering fragment
        fabFinishEdit.show()
        // init display name
        editTextPrettyName.setText(userInfoViewModel.userLiveData.value?.getDisplayName())
        fabFinishEdit.setOnClickListener {
            fabFinishEditOnClick()
        }

        userInfoViewModel.isEditSuccessful.observe(viewLifecycleOwner, { isEditSuccessful ->
            when (isEditSuccessful) {
                true -> {
                    Toast.makeText(appContext, TOAST_SUCCESS, Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_editUserInfoFragment_to_userInfoFragment) // navigate forward upon success
                }
                false -> {
                    fabFinishEdit.isEnabled = true // re-enable button
                    Toast.makeText(appContext, TOAST_FAIL, Toast.LENGTH_SHORT).show()
                }
                else -> {
                    // null value, do nothing
                }
            }
        })
    }

    private fun initImageViews() {
        imagesHashMap = hashMapOf(
            imageViewCrab to crabImgUrl,
            imageViewUnicorn to unicornImgUrl,
            imageViewAlien to alienImgUrl,
            imageViewRobot to robotImgUrl,
            imageViewOctopus to octopusImgUrl,
            imageViewFrog to frogImgUrl
        )
        imagesHashMap.forEach { (imageView, imgUrl) ->
            // load image
            Glide.with(this).load(imgUrl).into(imageView)
            // set on click listener for every image
            imageView.setOnClickListener {
                imageViewUserImage.setBackgroundResource(EMPTY_BACKGROUND)
                imageViewUserImage = imageView
                imageViewUserImage.setBackgroundResource(R.drawable.image_border)
            }
            // find the image-view with the user's current image
            if (imgUrl == userInfoViewModel.userLiveData.value?.image_url) {
                imageViewUserImage = imageView
            }
        }
        // mark the user's current image
        imageViewUserImage.setBackgroundResource(R.drawable.image_border)
    }

    private fun fabFinishEditOnClick() {
        fabFinishEdit.isEnabled = false
        val prettyName = editTextPrettyName.text.toString()
        val imgUrl = imagesHashMap[imageViewUserImage]
        if (imgUrl != null) {
            userInfoViewModel.startUserInfoSetterWorkers(prettyName, imgUrl)
        }

    }

    companion object {
        private const val crabImgUrl = "$BASE_URL/images/crab.png"
        private const val unicornImgUrl = "$BASE_URL/images/unicorn.png"
        private const val alienImgUrl = "$BASE_URL/images/alien.png"
        private const val robotImgUrl = "$BASE_URL/images/robot.png"
        private const val octopusImgUrl = "$BASE_URL/images/octopus.png"
        private const val frogImgUrl = "$BASE_URL/images/frog.png"
        private const val EMPTY_BACKGROUND = 0
        private const val TOAST_SUCCESS = "Success!"
        private const val TOAST_FAIL = "Please try again!"
    }


}