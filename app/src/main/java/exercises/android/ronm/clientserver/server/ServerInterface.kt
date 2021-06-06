package exercises.android.ronm.clientserver.server

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface ServerInterface {

    data class TokenResponse(var data: String)
    data class User(public var username: String, public var pretty_name: String, public var image_url: String)
    data class UserResponse(var data: User)

    @GET("/users/{username}/token") //todo  https://hujipostpc2019.pythonanywhere.com/users/<username goes here>/token/
    fun getToken(@Path("username") username: String) : Call<TokenResponse>

    @GET("/user/") // probably, todo https://hujipostpc2019.pythonanywhere.com/user/
    fun getUserInfo(@Header("Authorization: token ") token : String) : Call<UserResponse>



}