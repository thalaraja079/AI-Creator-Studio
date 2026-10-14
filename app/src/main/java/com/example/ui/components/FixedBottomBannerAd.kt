package com.example.ui.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun FixedBottomBannerAd(
    modifier: Modifier = Modifier
) {
    var isAdLoaded by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color(0xFF0F172A))
            .border(
                width = 0.5.dp,
                brush = Brush.horizontalGradient(
                    listOf(
                        Color(0xFF8B5CF6).copy(alpha = 0.3f),
                        Color(0xFF3B82F6).copy(alpha = 0.3f)
                    )
                ),
                shape = RoundedCornerShape(0.dp)
            )
            .testTag("fixed_bottom_banner_ad"),
        contentAlignment = Alignment.Center
    ) {
        // Fallback UI shown when in emulator, offline, or while loading
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .border(
                        0.5.dp,
                        Color(0xFFF59E0B).copy(alpha = 0.5f),
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "AD",
                    color = Color(0xFFF59E0B),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.Campaign,
                contentDescription = null,
                tint = Color(0xFF3B82F6),
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "Sponsored Creator Ad • Google AdMob Test Banner",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }

        // Live Google Mobile Ads AdView
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            factory = { context ->
                val frameLayout = FrameLayout(context).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }

                try {
                    val adView = AdView(context).apply {
                        setAdSize(AdSize.BANNER)
                        adUnitId = "ca-app-pub-3940256099942544/6300978111" // Official Test Banner Unit ID
                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                isAdLoaded = true
                            }

                            override fun onAdFailedToLoad(error: LoadAdError) {
                                isAdLoaded = false
                            }
                        }
                    }
                    frameLayout.addView(adView)
                    adView.loadAd(AdRequest.Builder().build())
                } catch (_: Exception) {
                    // Safe fallback if Play Services is missing or disabled
                }

                frameLayout
            }
        )
    }
}
