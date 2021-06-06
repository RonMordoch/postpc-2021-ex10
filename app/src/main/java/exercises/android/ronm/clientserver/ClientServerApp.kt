package exercises.android.ronm.clientserver

import android.app.Application

class ClientServerApp : Application() {

    var token: String = ""
        set(value) {
            field = value
            // save to SP
            val sp = getSharedPreferences(SP_NAME_TOKEN, MODE_PRIVATE)
            sp.edit().putString(SP_KEY_TOKEN, value).apply()
        }

    override fun onCreate() {
        super.onCreate()
        val sp = getSharedPreferences(SP_NAME_TOKEN, MODE_PRIVATE)
        token = sp.getString(SP_KEY_TOKEN, "").toString()
    }

    companion object {
        private const val SP_NAME_TOKEN = "sp_name_token"
        private const val SP_KEY_TOKEN = "sp_key_token"
    }
}