package exercises.android.ronm.clientserver.models

data class User(var username: String?, var pretty_name: String?, var image_url: String?)
{

    fun getDisplayName() : String{
        return if (pretty_name != null && pretty_name != "") {
            pretty_name.toString()
        } else {
            username.toString()
        }
    }

}
