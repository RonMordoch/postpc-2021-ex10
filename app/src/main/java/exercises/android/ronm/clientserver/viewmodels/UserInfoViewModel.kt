package exercises.android.ronm.clientserver.viewmodels

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import exercises.android.ronm.clientserver.server.BASE_URL
import exercises.android.ronm.clientserver.server.ServerInterface

class UserInfoViewModel : ViewModel() {

    var userInfoLiveData = MutableLiveData<ServerInterface.User>()
    var displayName: String = ""
    var fullImgUrl: String = ""

    init {
        userInfoLiveData.value = ServerInterface.User("", "", "")
    }

    fun setUserInfo(userInfo: ServerInterface.User) {
        // first update display name and image url so observer's will see the updated fields
        displayName = if (userInfo.pretty_name != null && userInfo.pretty_name != "") {
            userInfo.pretty_name.toString()
        } else {
            userInfo.username.toString()
        }
        if (userInfo.image_url != null && userInfo.image_url != "") {
            fullImgUrl = BASE_URL + userInfo.image_url.toString()
        }
        userInfoLiveData.value = userInfo
    }
}