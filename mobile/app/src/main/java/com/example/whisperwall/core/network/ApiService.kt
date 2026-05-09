package com.example.whisperwall.core.network

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body body: JsonObject): Response<JsonObject>

    @POST("auth/register")
    suspend fun register(@Body body: JsonObject): Response<JsonObject>

    @GET("users/me")
    suspend fun getMe(): Response<JsonObject>

    @PUT("users/me")
    suspend fun updateMe(@Body body: JsonObject): Response<JsonObject>

    @PUT("users/me/password")
    suspend fun updatePassword(@Body body: JsonObject): Response<JsonObject>

    @GET("confessions/public")
    suspend fun getPublicConfessions(@Query("page") page: Int, @Query("size") size: Int): Response<JsonObject>

    @GET("confessions/user/{id}")
    suspend fun getMyConfessions(@Path("id") userId: String, @Query("page") page: Int, @Query("size") size: Int): Response<JsonObject>

    @POST("confessions")
    suspend fun createConfession(@Body body: JsonObject): Response<JsonObject>

    @DELETE("confessions/{id}")
    suspend fun deleteConfession(@Path("id") id: Long): Response<JsonObject>

    @GET("reactions/confession/{id}")
    suspend fun getReactions(@Path("id") confessionId: Long): Response<JsonArray>

    @POST("reactions/confession/{id}")
    suspend fun likeConfession(@Path("id") confessionId: Long, @Body body: JsonObject): Response<JsonObject>

    @DELETE("reactions/confession/{id}")
    suspend fun unlikeConfession(@Path("id") confessionId: Long): Response<JsonObject>

    @POST("reports/confession/{id}")
    suspend fun reportConfession(@Path("id") confessionId: Long, @Body body: JsonObject): Response<JsonObject>

    @GET("admin/reports")
    suspend fun getAdminReports(
        @Query("status") status: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<JsonObject>

    @POST("admin/reports/{id}/dismiss")
    suspend fun dismissReport(@Path("id") reportId: Long): Response<JsonObject>

    @POST("admin/reports/{id}/remove-confession")
    suspend fun removeConfession(@Path("id") reportId: Long): Response<JsonObject>

    @POST("moderator/restriction-requests")
    suspend fun postModeratorRestrictionRequest(@Body body: JsonObject): Response<JsonObject>

    @GET("admin/stats/usage")
    suspend fun getAdminUsageStats(): Response<JsonObject>

    @POST("admin/users")
    suspend fun createAdminUser(@Body body: JsonObject): Response<JsonObject>

    @DELETE("admin/users/{id}")
    suspend fun deleteAdminUser(@Path("id") userId: Long): Response<JsonObject>

    @POST("admin/users/{id}/restrict")
    suspend fun restrictUser(
        @Path("id") userId: Long,
        @Body body: JsonObject
    ): Response<JsonObject>

    @DELETE("admin/users/{id}/restrict")
    suspend fun unrestrictUser(@Path("id") userId: Long): Response<JsonObject>

    @DELETE("admin/posts/{id}")
    suspend fun deleteAdminPost(@Path("id") postId: Long): Response<JsonObject>

    @PUT("admin/posts/{id}")
    suspend fun updateAdminPost(@Path("id") postId: Long, @Body body: JsonObject): Response<JsonObject>
}
