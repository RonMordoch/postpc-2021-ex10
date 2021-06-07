package exercises.android.ronm.clientserver.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.work.*
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import exercises.android.ronm.clientserver.ClientServerApp
import exercises.android.ronm.clientserver.R
import exercises.android.ronm.clientserver.UserInfoViewModel
import exercises.android.ronm.clientserver.server.BASE_URL
import exercises.android.ronm.clientserver.server.ServerInterface
import exercises.android.ronm.clientserver.workers.KEY_INPUT_PRETTY_NAME
import exercises.android.ronm.clientserver.workers.KEY_INPUT_TOKEN
import exercises.android.ronm.clientserver.workers.KEY_OUTPUT_USER_INFO
import exercises.android.ronm.clientserver.workers.UserPrettyNameSetterWorker

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
        val imageViewList = mutableListOf(imageViewCrab, imageViewUnicorn, imageViewAlien, imageViewRobot, imageViewOctopus, imageViewFrog)
        imageViewList.zip(imageUrlList) { imageView, imgUrl ->
            Glide.with(this).load(imgUrl).into(imageView)
            // find the image-view with the user's current image
            if (imgUrl == userInfoViewModel.userInfoLiveData.value?.image_url){
                imageViewUserImage = imageView
            }
        }
        imageViewList.forEach { imageView ->
            imageView.setOnClickListener {
                imageViewUserImage.setBackgroundResource(0)
                imageViewUserImage = imageView
                imageViewUserImage.setBackgroundResource(R.drawable.image_border)


            }
        }
        // mark the user's current image
        imageViewUserImage.setBackgroundResource(R.drawable.image_border)
        // expand fab upon entering fragment
        fabFinishEdit.show()
        // init display name
        editTextPrettyName.setText(userInfoViewModel.displayName)
        fabFinishEdit.setOnClickListener {
            startInfoSetterWorker()
        }

    }

    private fun startInfoSetterWorker() {
        if (appContext.token == "") {
            return // safety check for unexpected calls
        }
        val workManager = activity?.application?.let { WorkManager.getInstance(it) }
        val prettyName = editTextPrettyName.text.toString()
        val inputData = workDataOf(KEY_INPUT_TOKEN to appContext.token, KEY_INPUT_PRETTY_NAME to prettyName)
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val workRequest =
            OneTimeWorkRequestBuilder<UserPrettyNameSetterWorker>().setInputData(inputData).setConstraints(constraints).build()
        workManager?.enqueue(workRequest)
        // set live-data observer for result
        workManager?.getWorkInfoByIdLiveData(workRequest.id)?.observe(viewLifecycleOwner, { workInfo ->
            if (workInfo.state == WorkInfo.State.SUCCEEDED) {
                val userInfoJson = workInfo.outputData.getString(KEY_OUTPUT_USER_INFO)
                userInfoViewModel.setUserInfo(Gson().fromJson(userInfoJson, ServerInterface.User::class.java))
                Toast.makeText(appContext, "Success!", Toast.LENGTH_SHORT).show()
            } else if (workInfo.state == WorkInfo.State.FAILED) {
                Toast.makeText(appContext, "Please try again!", Toast.LENGTH_SHORT).show()

            }
        })
    }


    companion object {
        private const val crabImgUrl = "$BASE_URL/images/crab.png"
        private const val unicornImgUrl = "$BASE_URL/images/unicorn.png"
        private const val alienImgUrl = "$BASE_URL/images/alien.png"
        private const val robotImgUrl = "$BASE_URL/images/robot.png"
        private const val octopusImgUrl = "$BASE_URL/images/octopus.png"
        private const val frogImgUrl = "$BASE_URL/images/frog.png"
        private val imageUrlList = mutableListOf(crabImgUrl, unicornImgUrl, alienImgUrl, robotImgUrl, octopusImgUrl, frogImgUrl)

    }


}