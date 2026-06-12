package com.magicregan.prankcaller.ui.screens.detail

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.magicregan.prankcaller.ui.components.PhoneNumberInput
import com.magicregan.prankcaller.ui.theme.CardBackground
import com.magicregan.prankcaller.ui.theme.Crimson
import com.magicregan.prankcaller.ui.theme.CrimsonDark
import com.magicregan.prankcaller.ui.theme.DarkBackground
import com.magicregan.prankcaller.ui.theme.TextPrimary
import com.magicregan.prankcaller.ui.theme.TextSecondary

@Composable
fun PrankDetailScreen(
    onBackClick: () -> Unit,
    onPlayPreview: (Int) -> Unit,
    onStartCall: (Int) -> Unit,
    viewModel: PrankDetailViewModel = hiltViewModel()
) {
    val prank by viewModel.prank.collectAsState()
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val countryCode by viewModel.countryCode.collectAsState()
    val recordingConsent by viewModel.recordingConsent.collectAsState()
    val credits by viewModel.credits.collectAsState()
    val context = LocalContext.current

    val callPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            when (val result = viewModel.startPrankCall(context)) {
                is PrankDetailViewModel.CallResult.Success ->
                    prank?.let { onStartCall(it.id) }
                is PrankDetailViewModel.CallResult.NoCredits ->
                    Toast.makeText(context, "Not enough credits!", Toast.LENGTH_SHORT).show()
                is PrankDetailViewModel.CallResult.PermissionDenied ->
                    Toast.makeText(context, "Phone call permission denied", Toast.LENGTH_SHORT).show()
                is PrankDetailViewModel.CallResult.InvalidInput ->
                    Toast.makeText(context, "Invalid phone number", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Phone call permission required", Toast.LENGTH_SHORT).show()
        }
    }

    // Contacts picker
    val contactPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        uri?.let {
            val cursor = context.contentResolver.query(
                Uri.withAppendedPath(it, ContactsContract.Contacts.Entity.CONTENT_DIRECTORY),
                null, null, null, null
            )
            cursor?.use { c ->
                while (c.moveToNext()) {
                    val phoneIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    if (phoneIndex >= 0) {
                        val phone = c.getString(phoneIndex)
                        if (phone != null) {
                            val cleaned = phone.replace("[^0-9+]".toRegex(), "")
                            if (cleaned.startsWith("+")) {
                                val digits = cleaned.substring(1)
                                viewModel.onPhoneNumberChange(digits)
                            } else {
                                viewModel.onPhoneNumberChange(cleaned)
                            }
                            break
                        }
                    }
                }
            }
        }
    }

    val contactPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            contactPickerLauncher.launch(null)
        } else {
            Toast.makeText(context, "Contacts permission required", Toast.LENGTH_SHORT).show()
        }
    }

    val currentPrank = prank ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Top bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CrimsonDark)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 4.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Prank Details",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "$credits 🎟️",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Prank image with play overlay - responsive aspect ratio
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = currentPrank.largeImage,
                    contentDescription = currentPrank.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Surface(
                    shape = CircleShape,
                    color = Crimson.copy(alpha = 0.9f),
                    modifier = Modifier
                        .size(56.dp)
                        .align(Alignment.Center)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        IconButton(onClick = { onPlayPreview(currentPrank.id) }) {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = "Play preview",
                                tint = TextPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = currentPrank.name,
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = currentPrank.longDescription,
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Stats row
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.ThumbUp,
                        contentDescription = "Likes",
                        tint = Crimson,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatCount(currentPrank.thumbsUp),
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.ThumbDown,
                        contentDescription = "Dislikes",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatCount(currentPrank.thumbsDown),
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Call,
                        contentDescription = "Calls sent",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${formatCount(currentPrank.callsSent)} sent",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
                IconButton(onClick = {
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, "Check out this prank: ${currentPrank.name} on Prank Caller!")
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Prank"))
                }) {
                    Icon(
                        imageVector = Icons.Filled.Share,
                        contentDescription = "Share",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Tags
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (currentPrank.hasPrankAI) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Crimson
                    ) {
                        Text(
                            text = "🤖 PRANK AI",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
                if (currentPrank.isDynamic) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = CardBackground
                    ) {
                        Text(
                            text = "CUSTOMIZABLE",
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Phone number section
            Text(
                text = "Friend's Number",
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            PhoneNumberInput(
                phoneNumber = phoneNumber,
                onPhoneNumberChange = viewModel::onPhoneNumberChange,
                countryCode = countryCode,
                onCountryCodeChange = viewModel::onCountryCodeChange,
                onContactsClick = {
                    contactPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Recording consent
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = recordingConsent,
                    onCheckedChange = viewModel::onRecordingConsentChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = Crimson,
                        uncheckedColor = TextSecondary,
                        checkmarkColor = TextPrimary
                    )
                )
                Text(
                    text = "I agree to record this call with the knowledge that the person I am calling consents to being recorded.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Start call button
            Button(
                onClick = {
                    if (viewModel.canStartCall()) {
                        callPermissionLauncher.launch(Manifest.permission.CALL_PHONE)
                    } else if (!recordingConsent) {
                        Toast.makeText(context, "Please agree to recording consent", Toast.LENGTH_SHORT).show()
                    } else if (phoneNumber.length < 7) {
                        Toast.makeText(context, "Enter a valid phone number", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Not enough credits!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Crimson,
                    contentColor = TextPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Call,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Start Prank Call",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "1 credit will be used for this call",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun formatCount(count: Int): String {
    return when {
        count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
        count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
        else -> count.toString()
    }
}
