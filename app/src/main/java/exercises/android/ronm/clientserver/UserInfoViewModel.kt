package exercises.android.ronm.clientserver

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import exercises.android.ronm.clientserver.server.ServerInterface

class UserInfoViewModel : ViewModel() {

    var userInfoLiveData = MutableLiveData<ServerInterface.User>()
    var displayName: String = ""

    init {
        userInfoLiveData.value = ServerInterface.User("", "", "")
    }

    fun setUserInfo(userInfo: ServerInterface.User){
        // first update displayName so observer's will see the updated name
        displayName = if (userInfo.pretty_name != null && userInfo.pretty_name != "") {
            userInfo.pretty_name.toString()
        } else {
            userInfo.username.toString()
        }
        userInfoLiveData.value = userInfo
    }
}