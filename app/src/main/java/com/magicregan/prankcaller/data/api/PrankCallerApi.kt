package com.magicregan.prankcaller.data.api

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrankCallerApi @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val prefs: SharedPreferences =
        context.getSharedPreferences("prank_auth", Context.MODE_PRIVATE)

    private var cachedIdToken: String?
        get() = prefs.getString(KEY_ID_TOKEN, null)
        set(value) { prefs.edit().putString(KEY_ID_TOKEN, value).apply() }

    private var cachedRefreshToken: String?
        get() = prefs.getString(KEY_REFRESH_TOKEN, null)
        set(value) { prefs.edit().putString(KEY_REFRESH_TOKEN, value).apply() }

    private var cachedEmail: String?
        get() = prefs.getString(KEY_EMAIL, null)
        set(value) { prefs.edit().putString(KEY_EMAIL, value).apply() }

    private var cachedUserId: String?
        get() = prefs.getString(KEY_USER_ID, null)
        set(value) { prefs.edit().putString(KEY_USER_ID, value).apply() }

    val isLoggedIn: Boolean
        get() = cachedIdToken != null

    val userEmail: String?
        get() = cachedEmail

    val userId: String?
        get() = cachedUserId

    fun saveAuthTokens(idToken: String, refreshToken: String, email: String, uid: String) {
        cachedIdToken = idToken
        cachedRefreshToken = refreshToken
        cachedEmail = email
        cachedUserId = uid
    }

    fun logout() {
        cachedIdToken = null
        cachedRefreshToken = null
        cachedEmail = null
        cachedUserId = null
        prefs.edit().clear().apply()
    }

    private suspend fun refreshIdToken(): String? {
        return withContext(Dispatchers.IO) {
            try {
                val refresh = cachedRefreshToken ?: return@withContext null
                val body = "grant_type=refresh_token&refresh_token=$refresh"
                    .toRequestBody("application/x-www-form-urlencoded".toMediaType())

                val request = Request.Builder()
                    .url("$TOKEN_BASE/v1/token?key=$FIREBASE_API_KEY")
                    .addHeader("Referer", REFERER)
                    .addHeader("Origin", ORIGIN)
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""
                val responseJson = JSONObject(responseBody)

                if (responseJson.has("id_token")) {
                    val newIdToken = responseJson.getString("id_token")
                    val newRefreshToken = responseJson.getString("refresh_token")
                    cachedIdToken = newIdToken
                    cachedRefreshToken = newRefreshToken
                    newIdToken
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }
        }
    }

    private suspend fun getToken(): String? {
        return cachedIdToken ?: refreshIdToken()
    }

    suspend fun loginToApi() {
        withContext(Dispatchers.IO) {
            try {
                val token = getToken() ?: return@withContext
                val body = FormBody.Builder()
                    .add("source", "Android")
                    .build()
                val request = Request.Builder()
                    .url("${API_BASE}v2/login")
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .post(body)
                    .build()
                client.newCall(request).execute().close()
            } catch (_: Exception) {
                // Non-critical
            }
        }
    }

    suspend fun getUserDetail(): Result<UserDetail> {
        return withContext(Dispatchers.IO) {
            try {
                val token = getToken() ?: return@withContext Result.failure(Exception("Not logged in"))
                val request = Request.Builder()
                    .url("${API_BASE}v2/getUserDetail")
                    .addHeader("Authorization", "Bearer $token")
                    .get()
                    .build()
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: ""
                val json = JSONObject(body)
                if (json.optString("status") == "success") {
                    val data = json.optJSONObject("data")
                    val minutes = data?.optInt("minutes", 0) ?: 0
                    val freeCalls = data?.optInt("freeCalls", 0) ?: 0
                    val isSubscription = data?.optString("isSubscription", "false") ?: "false"
                    Result.success(UserDetail(minutes, freeCalls, isSubscription == "true"))
                } else {
                    Result.failure(Exception(json.optString("message", "Unknown error")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun startCall(
        prankId: Int,
        callTo: String,
        callFrom: String,
        recordCall: Boolean = false
    ): Result<CallResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = getToken() ?: return@withContext Result.failure(Exception("Not logged in"))
                val uid = cachedUserId ?: ""

                val body = FormBody.Builder()
                    .add("user_id", uid)
                    .add("source", "Android")
                    .add("prankId", prankId.toString())
                    .add("prankAI", "0")
                    .add("country", "US")
                    .add("callTo", callTo.replace("[- )(]".toRegex(), ""))
                    .add("callFrom", callFrom.replace("[- )(]".toRegex(), ""))
                    .add("listenLive", "1")
                    .add("recordCall", if (recordCall) "1" else "0")
                    .add("prankedRecording", "0")
                    .build()

                val request = Request.Builder()
                    .url("${API_BASE}v2/call")
                    .addHeader("Authorization", "Bearer $token")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""
                val json = JSONObject(responseBody)

                if (json.optString("status") == "success") {
                    val sidToken = json.optString("sidToken", "")
                    Result.success(CallResponse(true, sidToken, null))
                } else {
                    val error = json.optString("message", "Call failed")
                    Result.failure(Exception(error))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getCallStatus(sidToken: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val token = getToken() ?: return@withContext Result.failure(Exception("Not logged in"))
                val body = FormBody.Builder()
                    .add("sidToken", sidToken)
                    .build()
                val request = Request.Builder()
                    .url("${STATUS_BASE}status")
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""
                val json = JSONObject(responseBody)
                Result.success(json.optString("callStatus", "unknown"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun endCall(sidToken: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val token = getToken() ?: return@withContext Result.failure(Exception("Not logged in"))
                val body = FormBody.Builder()
                    .add("sidToken", sidToken)
                    .build()
                val request = Request.Builder()
                    .url("${STATUS_BASE}end-call")
                    .addHeader("Authorization", "Bearer $token")
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build()
                client.newCall(request).execute().close()
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    companion object {
        private const val API_BASE = "https://apiv4.prankcaller.io/"
        private const val STATUS_BASE = "https://aibot.prankcaller.io/"
        private const val TOKEN_BASE = "https://securetoken.googleapis.com"
        private const val FIREBASE_API_KEY = "AIzaSyCavu7eOOpfQeXXEPCmSAePSzE877B182E"
        private const val REFERER = "https://prankcaller.io/"
        private const val ORIGIN = "https://prankcaller.io"
        private const val KEY_ID_TOKEN = "id_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_EMAIL = "email"
        private const val KEY_USER_ID = "user_id"
    }
}

data class UserDetail(
    val minutes: Int,
    val freeCalls: Int,
    val isSubscription: Boolean
)

data class CallResponse(
    val success: Boolean,
    val sidToken: String,
    val error: String?
)
