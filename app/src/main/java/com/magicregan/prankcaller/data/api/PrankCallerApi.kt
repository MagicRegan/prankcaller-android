package com.magicregan.prankcaller.data.api

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    private var cachedToken: String?
        get() = prefs.getString(KEY_TOKEN, null)
        set(value) { prefs.edit().putString(KEY_TOKEN, value).apply() }

    private var cachedEmail: String?
        get() = prefs.getString(KEY_EMAIL, null)
        set(value) { prefs.edit().putString(KEY_EMAIL, value).apply() }

    private var cachedUserId: String?
        get() = prefs.getString(KEY_USER_ID, null)
        set(value) { prefs.edit().putString(KEY_USER_ID, value).apply() }

    val isLoggedIn: Boolean
        get() = cachedToken != null

    val userEmail: String?
        get() = cachedEmail

    val userId: String?
        get() = cachedUserId

    fun logout() {
        cachedToken = null
        cachedEmail = null
        cachedUserId = null
        prefs.edit().clear().apply()
    }

    suspend fun register(email: String, password: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                }
                val body = json.toString()
                    .toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url("${API_BASE}api/register")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""
                val responseJson = JSONObject(responseBody)

                if (responseJson.optString("status") == "success") {
                    val data = responseJson.getJSONObject("data")
                    val token = data.getString("token")
                    val userId = data.getString("userId")
                    val respEmail = data.getString("email")
                    val credits = data.getInt("credits")

                    cachedToken = token
                    cachedEmail = respEmail
                    cachedUserId = userId

                    Result.success(AuthResponse(token, userId, respEmail, credits))
                } else {
                    val msg = responseJson.optString("message", "Registration failed")
                    Result.failure(Exception(msg))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Connection error: ${e.message}"))
            }
        }
    }

    suspend fun login(email: String, password: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val json = JSONObject().apply {
                    put("email", email)
                    put("password", password)
                }
                val body = json.toString()
                    .toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url("${API_BASE}api/login")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""
                val responseJson = JSONObject(responseBody)

                if (responseJson.optString("status") == "success") {
                    val data = responseJson.getJSONObject("data")
                    val token = data.getString("token")
                    val userId = data.getString("userId")
                    val respEmail = data.getString("email")
                    val credits = data.getInt("credits")

                    cachedToken = token
                    cachedEmail = respEmail
                    cachedUserId = userId

                    Result.success(AuthResponse(token, userId, respEmail, credits))
                } else {
                    val msg = responseJson.optString("message", "Login failed")
                    Result.failure(Exception(msg))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Connection error: ${e.message}"))
            }
        }
    }

    suspend fun getUserDetail(): Result<UserDetail> {
        return withContext(Dispatchers.IO) {
            try {
                val token = cachedToken ?: return@withContext Result.failure(Exception("Not logged in"))
                val request = Request.Builder()
                    .url("${API_BASE}api/user")
                    .addHeader("Authorization", "Bearer $token")
                    .get()
                    .build()
                val response = client.newCall(request).execute()
                val body = response.body?.string() ?: ""
                val json = JSONObject(body)
                if (json.optString("status") == "success") {
                    val data = json.getJSONObject("data")
                    val credits = data.optInt("credits", 0)
                    Result.success(UserDetail(credits, 0, false))
                } else {
                    Result.failure(Exception(json.optString("message", "Unknown error")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun addCredits(amount: Int): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val token = cachedToken ?: return@withContext Result.failure(Exception("Not logged in"))
                val json = JSONObject().apply { put("amount", amount) }
                val body = json.toString().toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url("${API_BASE}api/credits/add")
                    .addHeader("Authorization", "Bearer $token")
                    .post(body)
                    .build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""
                val responseJson = JSONObject(responseBody)
                if (responseJson.optString("status") == "success") {
                    val credits = responseJson.getJSONObject("data").getInt("credits")
                    Result.success(credits)
                } else {
                    Result.failure(Exception(responseJson.optString("message", "Failed")))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun startCall(
        prankId: Int,
        callTo: String,
        countryCode: String = "",
        recordCall: Boolean = false
    ): Result<CallResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val token = cachedToken ?: return@withContext Result.failure(Exception("Not logged in"))

                val json = JSONObject().apply {
                    put("prankId", prankId)
                    put("callTo", callTo.replace("[\\s()-]".toRegex(), ""))
                    put("countryCode", countryCode)
                }
                val body = json.toString().toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url("${API_BASE}api/call")
                    .addHeader("Authorization", "Bearer $token")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""
                val responseJson = JSONObject(responseBody)

                if (responseJson.optString("status") == "success") {
                    val data = responseJson.getJSONObject("data")
                    val callId = data.getString("callId")
                    val message = data.optString("message", "Call initiated!")
                    val creditsRemaining = data.optInt("creditsRemaining", -1)
                    Result.success(CallResponse(true, callId, message, creditsRemaining))
                } else {
                    val error = responseJson.optString("message", "Call failed")
                    Result.failure(Exception(error))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Connection error: ${e.message}"))
            }
        }
    }

    suspend fun getCallStatus(callId: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val token = cachedToken ?: return@withContext Result.failure(Exception("Not logged in"))
                val request = Request.Builder()
                    .url("${API_BASE}api/call/$callId/status")
                    .addHeader("Authorization", "Bearer $token")
                    .get()
                    .build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""
                val json = JSONObject(responseBody)
                val status = json.optJSONObject("data")?.optString("status", "unknown") ?: "unknown"
                Result.success(status)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getCallHistory(): Result<List<CallRecord>> {
        return withContext(Dispatchers.IO) {
            try {
                val token = cachedToken ?: return@withContext Result.failure(Exception("Not logged in"))
                val request = Request.Builder()
                    .url("${API_BASE}api/calls")
                    .addHeader("Authorization", "Bearer $token")
                    .get()
                    .build()
                val response = client.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""
                val json = JSONObject(responseBody)
                if (json.optString("status") == "success") {
                    val arr = json.getJSONArray("data")
                    val records = mutableListOf<CallRecord>()
                    for (i in 0 until arr.length()) {
                        val item = arr.getJSONObject(i)
                        records.add(
                            CallRecord(
                                id = item.getString("id"),
                                prankName = item.optString("prank_name", "Unknown"),
                                callTo = item.optString("call_to", ""),
                                status = item.optString("status", "unknown"),
                                createdAt = item.optString("created_at", "")
                            )
                        )
                    }
                    Result.success(records)
                } else {
                    Result.failure(Exception("Failed to load call history"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    companion object {
        private const val API_BASE = "https://prankcaller-android.onrender.com/"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_EMAIL = "email"
        private const val KEY_USER_ID = "user_id"
    }
}

data class AuthResponse(
    val token: String,
    val userId: String,
    val email: String,
    val credits: Int
)

data class UserDetail(
    val minutes: Int,
    val freeCalls: Int,
    val isSubscription: Boolean
)

data class CallResponse(
    val success: Boolean,
    val callId: String,
    val message: String?,
    val creditsRemaining: Int
)

data class CallRecord(
    val id: String,
    val prankName: String,
    val callTo: String,
    val status: String,
    val createdAt: String
)
