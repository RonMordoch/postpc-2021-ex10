package exercises.android.ronm.clientserver.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import exercises.android.ronm.clientserver.server.BASE_URL
import exercises.android.ronm.clientserver.server.ServerInterface

class UserInfoViewModel : ViewModel() {

    var displayNameLiveData = MutableLiveData("")
    var imgUrlLiveData = MutableLiveData("")


    fun setUserInfo(userInfo: ServerInterface.User) {
        // first update display name and image url so observer's will see the updated fields
        displayNameLiveData.value = if (userInfo.pretty_name != null && userInfo.pretty_name != "") {
            userInfo.pretty_name.toString()
        } else {
            userInfo.username.toString()
        }
        if (userInfo.image_url != null && userInfo.image_url != "") {
            imgUrlLiveData.value = BASE_URL + userInfo.image_url.toString()
        }
    }
}