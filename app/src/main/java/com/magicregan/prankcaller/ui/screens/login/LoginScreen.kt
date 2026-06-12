package com.magicregan.prankcaller.ui.screens.login

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.magicregan.prankcaller.ui.theme.Crimson
import com.magicregan.prankcaller.ui.theme.CrimsonDark
import com.magicregan.prankcaller.ui.theme.DarkBackground
import com.magicregan.prankcaller.ui.theme.TextPrimary

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel()
) {
    var isLoading by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CrimsonDark)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                text = "Sign In",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.databaseEnabled = true
                        settings.setSupportMultipleWindows(false)

                        CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                        addJavascriptInterface(
                            TokenBridge { idToken, refreshToken, email, uid ->
                                viewModel.onTokenReceived(idToken, refreshToken, email, uid)
                                onLoginSuccess()
                            },
                            "AndroidBridge"
                        )

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                isLoading = true
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                                injectTokenExtractor(view)
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val url = request?.url?.toString() ?: return false
                                if (url.startsWith("https://prankcaller.io") ||
                                    url.startsWith("https://accounts.google.com") ||
                                    url.contains("googleapis.com") ||
                                    url.contains("firebaseapp.com") ||
                                    url.contains("gstatic.com") ||
                                    url.contains("google.com")
                                ) {
                                    return false
                                }
                                return false
                            }
                        }

                        webChromeClient = WebChromeClient()

                        loadUrl("https://prankcaller.io/")
                    }
                }
            )

            if (isLoading) {
                CircularProgressIndicator(
                    color = Crimson,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp)
                )
            }
        }
    }
}

private fun injectTokenExtractor(webView: WebView?) {
    webView?.evaluateJavascript(
        """
        (function() {
            // Check if we already set up the listener
            if (window._prankTokenListenerSet) return;
            window._prankTokenListenerSet = true;
            
            // Poll for Firebase auth state
            function checkAuth() {
                try {
                    // Try to access Firebase auth
                    if (typeof firebase !== 'undefined' && firebase.auth) {
                        var user = firebase.auth().currentUser;
                        if (user) {
                            user.getIdToken().then(function(idToken) {
                                // Get the refresh token from the internal state
                                var refreshToken = user.refreshToken || '';
                                var email = user.email || '';
                                var uid = user.uid || '';
                                if (idToken && typeof AndroidBridge !== 'undefined') {
                                    AndroidBridge.onToken(idToken, refreshToken, email, uid);
                                }
                            });
                            return;
                        }
                        
                        // Set up auth state listener
                        firebase.auth().onAuthStateChanged(function(user) {
                            if (user) {
                                user.getIdToken().then(function(idToken) {
                                    var refreshToken = user.refreshToken || '';
                                    var email = user.email || '';
                                    var uid = user.uid || '';
                                    if (typeof AndroidBridge !== 'undefined') {
                                        AndroidBridge.onToken(idToken, refreshToken, email, uid);
                                    }
                                });
                            }
                        });
                    } else {
                        // Firebase not loaded yet, retry
                        setTimeout(checkAuth, 1000);
                    }
                } catch(e) {
                    setTimeout(checkAuth, 1000);
                }
            }
            
            // Also check IndexedDB for stored tokens
            function checkStoredToken() {
                try {
                    var request = indexedDB.open('firebaseLocalStorageDb');
                    request.onsuccess = function(event) {
                        var db = event.target.result;
                        if (!db.objectStoreNames.contains('firebaseLocalStorage')) {
                            setTimeout(checkStoredToken, 2000);
                            return;
                        }
                        var tx = db.transaction('firebaseLocalStorage', 'readonly');
                        var store = tx.objectStore('firebaseLocalStorage');
                        var getAllReq = store.getAll();
                        getAllReq.onsuccess = function() {
                            var results = getAllReq.result;
                            for (var i = 0; i < results.length; i++) {
                                var item = results[i];
                                if (item && item.value && item.value.stsTokenManager) {
                                    var token = item.value.stsTokenManager.accessToken;
                                    var refresh = item.value.stsTokenManager.refreshToken || '';
                                    var email = item.value.email || '';
                                    var uid = item.value.uid || '';
                                    if (token && typeof AndroidBridge !== 'undefined') {
                                        AndroidBridge.onToken(token, refresh, email, uid);
                                        return;
                                    }
                                }
                            }
                            setTimeout(checkStoredToken, 2000);
                        };
                    };
                    request.onerror = function() {
                        setTimeout(checkStoredToken, 2000);
                    };
                } catch(e) {
                    setTimeout(checkStoredToken, 2000);
                }
            }
            
            checkAuth();
            checkStoredToken();
        })();
        """.trimIndent(),
        null
    )
}

class TokenBridge(
    private val onToken: (idToken: String, refreshToken: String, email: String, uid: String) -> Unit
) {
    private var tokenSent = false

    @JavascriptInterface
    fun onToken(idToken: String, refreshToken: String, email: String, uid: String) {
        if (!tokenSent && idToken.isNotEmpty()) {
            tokenSent = true
            onToken.invoke(idToken, refreshToken, email, uid)
        }
    }
}
