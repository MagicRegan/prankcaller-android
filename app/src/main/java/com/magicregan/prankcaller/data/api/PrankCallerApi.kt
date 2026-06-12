package com.magicregan.prankcaller.data.api

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrankCallerApi @Inject constructor() {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val auth = FirebaseAuth.getInstance()

    val isLoggedIn: Boolean
        get() = auth.currentUser != null

    val userEmail: String?
        get() = auth.currentUser?.email

    val userId: String?
        get() = auth.currentUser?.uid

    suspend fun login(email: String, password: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val token = result.user?.getIdToken(false)?.await()?.token
                if (token != null) {
                    loginToApi(token)
                    Result.success(token)
                } else {
                    Result.failure(Exception("Failed to get auth token"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun register(email: String, password: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val token = result.user?.getIdToken(false)?.await()?.token
                if (token != null) {
                    loginToApi(token)
                    Result.success(token)
                } else {
                    Result.failure(Exception("Failed to get auth token"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun logout() {
        auth.signOut()
    }

    private suspend fun getToken(): String? {
        return auth.currentUser?.getIdToken(false)?.await()?.token
    }

    private suspend fun loginToApi(token: String) {
        withContext(Dispatchers.IO) {
            try {
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
                val uid = auth.currentUser?.uid ?: ""

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
