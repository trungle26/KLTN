package com.trungld.viberide.presentation.screens.shared.components.cards

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trungld.viberide.presentation.theme.VibeRideTheme

@Composable
fun CardContainer(
    modifier: Modifier = Modifier,
    color: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp), // Rounded corners like in the image
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent // We'll use a gradient background
        ),

        ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to color,  // Top edge kicks off with start color
                                0.5f to color,    // At 20%, we hit the end color
                                1.0f to Color.Black          // Bottom fades to black
                            ),
                        ),
                        alpha = 0.2f
                    )
                }
        ) {
            content()
        }
    }
}

@Preview
@Composable
private fun CardContainerPreview() {
    VibeRideTheme {
        CardContainer(modifier = Modifier.size(100.dp), color = Color.Red) {
            // Your content goes here
        }

    }
}