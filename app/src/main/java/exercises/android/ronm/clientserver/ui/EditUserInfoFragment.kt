package exercises.android.ronm.clientserver.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.work.*
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import exercises.android.ronm.clientserver.ClientServerApp
import exercises.android.ronm.clientserver.R
import exercises.android.ronm.clientserver.viewmodels.UserInfoViewModel
import exercises.android.ronm.clientserver.server.BASE_URL
import exercises.android.ronm.clientserver.server.ServerInterface
import exercises.android.ronm.clientserver.workers.*

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
    private lateinit var appContext: ClientServerApp
    private lateinit var imagesHashMap: HashMap<ImageView, String>
    private val userInfoViewModel: UserInfoViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // get application context
        appContext = activity?.applicationContext as ClientServerApp
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
        editTextPrettyName.setText(userInfoViewModel.displayName)
        fabFinishEdit.setOnClickListener {
            fabFinishEditOnClick()
        }
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
            Glide.with(this).load(BASE_URL + imgUrl).into(imageView)
            // set on click listener for every image
            imageView.setOnClickListener {
                imageViewUserImage.setBackgroundResource(EMPTY_BACKGROUND)
                imageViewUserImage = imageView
                imageViewUserImage.setBackgroundResource(R.drawable.image_border)
            }
            // find the image-view with the user's current image
            if (imgUrl == userInfoViewModel.userInfoLiveData.value?.image_url) {
                imageViewUserImage = imageView
            }
        }
        // mark the user's current image
        imageViewUserImage.setBackgroundResource(R.drawable.image_border)
    }

    private fun fabFinishEditOnClick() {
        fabFinishEdit.isEnabled = false
        startPrettyNameSetterWorker()
        startUserImageSetterWorker()
    }

    private fun startPrettyNameSetterWorker() {
        if (appContext.token == "") {
            return // safety check for unexpected calls
        }
        val workManager = activity?.application?.let { WorkManager.getInstance(it) }
        val prettyName = editTextPrettyName.text.toString()
        val inputData = workDataOf(KEY_INPUT_TOKEN to appContext.token, KEY_INPUT_PRETTY_NAME to prettyName)
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val workRequest =
            OneTimeWorkRequestBuilder<PrettyNameSetterWorker>().setInputData(inputData).setConstraints(constraints).build()
        workManager?.enqueue(workRequest)
        // set live-data observer for result
        workManager?.getWorkInfoByIdLiveData(workRequest.id)?.observe(viewLifecycleOwner, { workInfo ->
            if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                val userInfoJson = workInfo.outputData.getString(KEY_OUTPUT_USER_INFO)
                userInfoViewModel.setUserInfo(Gson().fromJson(userInfoJson, ServerInterface.User::class.java))
                Toast.makeText(appContext, TOAST_SUCCESS, Toast.LENGTH_SHORT).show()
            } else if (workInfo.state == WorkInfo.State.FAILED) {
                fabFinishEdit.isEnabled = true // re-enable button
                Toast.makeText(appContext, TOAST_FAIL, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun startUserImageSetterWorker() {
        if (appContext.token == "") {
            return // safety check for unexpected calls
        }
        val workManager = activity?.application?.let { WorkManager.getInstance(it) }
        val newImgUrl = imagesHashMap[imageViewUserImage]
        val inputData = workDataOf(KEY_INPUT_TOKEN to appContext.token, KEY_INPUT_USER_IMG to newImgUrl)
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val workRequest =
            OneTimeWorkRequestBuilder<UserImageSetterWorker>().setInputData(inputData).setConstraints(constraints).build()
        workManager?.enqueue(workRequest)
        // set live-data observer for result
        workManager?.getWorkInfoByIdLiveData(workRequest.id)?.observe(viewLifecycleOwner, { workInfo ->
            if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                val userInfoJson = workInfo.outputData.getString(KEY_OUTPUT_USER_INFO)
                userInfoViewModel.setUserInfo(Gson().fromJson(userInfoJson, ServerInterface.User::class.java))
                Toast.makeText(appContext, TOAST_SUCCESS, Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_editUserInfoFragment_to_userInfoFragment) // navigate forward upon success
            } else if (workInfo.state == WorkInfo.State.FAILED) {
                fabFinishEdit.isEnabled = true // re-enable button
                Toast.makeText(appContext, TOAST_FAIL, Toast.LENGTH_SHORT).show()
            }
        })
    }


    companion object {
        private const val crabImgUrl = "/images/crab.png"
        private const val unicornImgUrl = "/images/unicorn.png"
        private const val alienImgUrl = "/images/alien.png"
        private const val robotImgUrl = "/images/robot.png"
        private const val octopusImgUrl = "/images/octopus.png"
        private const val frogImgUrl = "/images/frog.png"
        private const val EMPTY_BACKGROUND = 0
        private const val TOAST_SUCCESS = "Success!"
        private const val TOAST_FAIL = "Please try again!"
    }


}