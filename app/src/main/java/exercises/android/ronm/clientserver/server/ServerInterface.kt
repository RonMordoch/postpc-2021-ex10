package exercises.android.ronm.clientserver.server

import retrofit2.Call
import retrofit2.http.*

interface ServerInterface {

    data class TokenResponse(var data: String)
    data class User(var username: String?, var pretty_name: String?, var image_url: String?)
    data class UserResponse(var data: User)
    data class SetUserPrettyNameRequest(var pretty_name: String?)
    data class SetUserImageRequest(var image_url: String?)

    @GET("/users/{username}/token/")
    fun getToken(@Path("username") username: String) : Call<TokenResponse>

    @GET("/user/")
    fun getUserInfo(@Header("Authorization") token : String) : Call<UserResponse>

    @POST("/user/edit/")
    @Headers("Content-Type: application/json")
    fun setUserPrettyName(@Header("Authorization") token : String, @Body request : SetUserPrettyNameRequest) : Call<UserResponse>

    @POST("/user/edit/")
    @Headers("Content-Type: application/json")
    fun setUserImage(@Header("Authorization") token : String, @Body request : SetUserImageRequest) : Call<UserResponse>
}