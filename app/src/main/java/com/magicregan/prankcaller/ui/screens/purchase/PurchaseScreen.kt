package com.magicregan.prankcaller.ui.screens.purchase

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.magicregan.prankcaller.ui.theme.CardBackground
import com.magicregan.prankcaller.ui.theme.CardBorder
import com.magicregan.prankcaller.ui.theme.Crimson
import com.magicregan.prankcaller.ui.theme.CrimsonDark
import com.magicregan.prankcaller.ui.theme.DarkBackground
import com.magicregan.prankcaller.ui.theme.GoldCredits
import com.magicregan.prankcaller.ui.theme.TextPrimary
import com.magicregan.prankcaller.ui.theme.TextSecondary

data class CreditPackage(
    val credits: Int,
    val price: String,
    val popular: Boolean = false
)

@Composable
fun PurchaseScreen(
    viewModel: PurchaseViewModel = hiltViewModel()
) {
    val credits by viewModel.credits.collectAsState()
    val context = LocalContext.current

    val packages = listOf(
        CreditPackage(20, "$6.99"),
        CreditPackage(40, "$10.99", popular = true),
        CreditPackage(120, "$26.99"),
        CreditPackage(160, "$32.99")
    )

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
                .padding(horizontal = 12.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Get Credits",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                modifier = Modifier.align(Alignment.Center)
            )
            IconButton(
                onClick = { viewModel.syncCreditsFromApi() },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = "Sync Credits",
                    tint = TextPrimary
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                // Current credits display
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBackground),
                    border = BorderStroke(1.dp, GoldCredits.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = GoldCredits,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Your Credits",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "$credits",
                            color = GoldCredits,
                            fontWeight = FontWeight.Bold,
                            fontSize = 42.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "1 credit = 1 prank call",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Buy Credits",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            items(packages) { pkg ->
                CreditPackageCard(
                    creditPackage = pkg,
                    onBuyClick = { viewModel.purchaseCredits(context, pkg) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))

                // Watch ad for free credits
                OutlinedButton(
                    onClick = { viewModel.addFreeCredits() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, GoldCredits.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldCredits)
                ) {
                    Icon(Icons.Filled.Star, null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Get 5 Free Credits",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tap the refresh icon above to sync credits after purchasing.\nCredits persist between app sessions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun CreditPackageCard(
    creditPackage: CreditPackage,
    onBuyClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        border = if (creditPackage.popular) BorderStroke(1.5.dp, Crimson) else BorderStroke(1.dp, CardBorder.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${creditPackage.credits} Credits",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    if (creditPackage.popular) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "POPULAR",
                            color = Crimson,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
                Text(
                    text = "${"%.2f".format(creditPackage.price.removePrefix("$").toDouble() / creditPackage.credits)}/call",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = onBuyClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Crimson,
                    contentColor = TextPrimary
                ),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = creditPackage.price,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}
