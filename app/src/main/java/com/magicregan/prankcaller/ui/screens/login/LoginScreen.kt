package com.magicregan.prankcaller.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.magicregan.prankcaller.ui.theme.Crimson
import com.magicregan.prankcaller.ui.theme.CrimsonDark
import com.magicregan.prankcaller.ui.theme.DarkBackground
import com.magicregan.prankcaller.ui.theme.CardBackground
import com.magicregan.prankcaller.ui.theme.TextPrimary
import com.magicregan.prankcaller.ui.theme.TextSecondary

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit = {},
    viewModel: LoginViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isRegisterMode by remember { mutableStateOf(true) }

    val uiState by viewModel.uiState.collectAsState()

    if (uiState is LoginUiState.Success) {
        onLoginSuccess()
        return
    }

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
                text = if (isRegisterMode) "Create Account" else "Sign In",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = if (isRegisterMode)
                    "Create a free account to start making prank calls"
                else
                    "Sign in to your account",
                color = TextSecondary,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            val textFieldColors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = Crimson,
                unfocusedBorderColor = TextSecondary.copy(alpha = 0.5f),
                focusedLabelColor = Crimson,
                unfocusedLabelColor = TextSecondary,
                cursorColor = Crimson,
                focusedContainerColor = CardBackground,
                unfocusedContainerColor = CardBackground
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                leadingIcon = {
                    Icon(Icons.Default.Email, "Email", tint = TextSecondary)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                colors = textFieldColors,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, "Password", tint = TextSecondary)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            "Toggle password visibility",
                            tint = TextSecondary
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                colors = textFieldColors,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState is LoginUiState.Error) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = (uiState as LoginUiState.Error).message,
                    color = Color.Red,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (isRegisterMode) {
                        viewModel.register(email, password)
                    } else {
                        viewModel.login(email, password)
                    }
                },
                enabled = email.isNotBlank() && password.length >= 6 && uiState !is LoginUiState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = Crimson),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (uiState is LoginUiState.Loading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isRegisterMode) "Create Account" else "Sign In",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (isRegisterMode) "Already have an account? " else "Don't have an account? ",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                Text(
                    text = if (isRegisterMode) "Sign In" else "Create Account",
                    color = Crimson,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        isRegisterMode = !isRegisterMode
                        viewModel.clearError()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "You'll get 5 free credits on sign up!",
                color = TextSecondary.copy(alpha = 0.7f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
