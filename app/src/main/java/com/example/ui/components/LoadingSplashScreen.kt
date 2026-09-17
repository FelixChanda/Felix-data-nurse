package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import kotlinx.coroutines.delay

@Composable
fun LoadingSplashScreen(
    onFinished: () -> Unit
) {
    val progress = remember { Animatable(0f) }
    var statusText by remember { mutableStateOf("Initializing Database...") }

    LaunchedEffect(Unit) {
        delay(150)
        statusText = "Loading Curriculum & Syllabus..."
        progress.animateTo(
            targetValue = 0.35f,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        )
        delay(100)
        statusText = "Syncing Clinical Guidelines & Past Papers..."
        progress.animateTo(
            targetValue = 0.72f,
            animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
        )
        delay(100)
        statusText = "Starting DATANURSE Engine..."
        progress.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        )
        delay(150)
        onFinished()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Blurred Campus Background Image
        Image(
            painter = painterResource(id = R.drawable.img_campus_bg),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = 16.dp),
            contentScale = ContentScale.Crop
        )

        // Dark Atmospheric Gradient Tint Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF030D1A).copy(alpha = 0.88f),
                            Color(0xFF07192C).copy(alpha = 0.92f),
                            Color(0xFF020912).copy(alpha = 0.96f)
                        )
                    )
                )
        )

        // Centered Branding and Progress
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Rounded App Campus Icon with Glowing Cyan Border
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .border(2.5.dp, Color(0xFF00E5FF), RoundedCornerShape(22.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_campus_icon),
                    contentDescription = "DATANURSE Icon",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // Official Database Badge
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFF064E3B).copy(alpha = 0.75f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "OFFICIAL CLINICAL DATABASE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF34D399),
                        letterSpacing = 0.8.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Brand Title: DATA-NURSE
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "DATA",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "–NURSE",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF00E5FF),
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle: by Chanda Felix™
            Text(
                text = "by  Chanda  Felix™",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFFFD600),
                letterSpacing = 0.4.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tagline
            Text(
                text = "powered by Ba Sacheal and Apostle Sikate and Ba Edwa",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Description
            Text(
                text = "Comprehensive Academic Library, Core Exam Papers, Nursing Modules & Clinical Guidelines",
                fontSize = 12.sp,
                color = Color(0xFFCBD5E1),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp,
                modifier = Modifier.widthIn(max = 320.dp)
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Sleek Rounded Gradient Progress Bar
            Box(
                modifier = Modifier
                    .width(280.dp)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction = progress.value)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF06B6D4), Color(0xFF10B981), Color(0xFFF59E0B))
                            )
                        )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress status and percentage
            Row(
                modifier = Modifier.width(280.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = statusText,
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = "${(progress.value * 100).toInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF38BDF8)
                )
            }
        }
    }
}
