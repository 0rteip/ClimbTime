package com.example.climbtime.ui.graph

import android.graphics.Paint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Arrangement.Top
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.climbtime.ui.component.smallDP

@Composable
fun BarGraphCompose(
    data: Map<String, Int>,
    height: Dp,
    barWidth: Dp,
    barColor: Color,
    horizontalLineColor: Color,
    barArrangement: Arrangement.Horizontal,
    modifier: Modifier = Modifier,
) {
    val graphBarData = data.values.map { it.toFloat() / data.values.max() }
    val xAxisScaleData = data.keys.toList()
    val barValues = data.values.toList()

    val barData by remember {
        mutableStateOf(barValues)
    }

    // getting screen width
    val width = LocalConfiguration.current.screenWidthDp.dp

    // bottom height of the X-Axis Scale
    val xAxisScaleHeight = 40.dp

    val yAxisScaleSpacing by remember {
        mutableFloatStateOf(100f)
    }

    val yAxisTextWidth by remember {
        mutableStateOf(100.dp)
    }

    // bar shape
    val barShape = RoundedCornerShape(topStart = smallDP, topEnd = smallDP)

    val density = LocalDensity.current
    // y-axis scale text paint
    val c = MaterialTheme.colorScheme.onBackground
    val textPaint = remember(density) {
        Paint().apply {
            color = c.hashCode()
            textAlign = Paint.Align.CENTER
            textSize = density.run { 14.sp.toPx() }
        }
    }

    // height of horizontal line over x-axis scale
    val horizontalLineHeight = smallDP

    val maxNumberOfElements = barData.max()

    Box(
        modifier = modifier
            .wrapContentSize()
            .padding(0.dp),
        contentAlignment = Alignment.TopStart
    ) {

        // y-axis scale
        Column(
            modifier = Modifier
                .padding(top = xAxisScaleHeight, end = 3.dp)
                .height(height)
                .fillMaxWidth(),
            horizontalAlignment = CenterHorizontally
        ) {
            Canvas(
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .fillMaxSize()
            ) {
                // Y-Axis Scale Text
                (0..maxNumberOfElements).forEach { i ->
                    drawContext.canvas.nativeCanvas.apply {
                        drawText(
                            if (barData.contains(i)) {
                                i.toString()
                            } else {
                                ""
                            },
                            100f / maxNumberOfElements,
                            size.height - yAxisScaleSpacing - i * size.height / maxNumberOfElements,
                            textPaint
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier.wrapContentSize(),
            contentAlignment = Alignment.CenterStart
        ) {

            // Graph with Bar Graph and X-Axis Scale
            Box(
                modifier = Modifier
                    .padding(start = 50.dp)
                    .width(width - yAxisTextWidth)
                    .height(height + xAxisScaleHeight), contentAlignment = BottomCenter
            ) {

                Row(
                    modifier = Modifier.width(width - yAxisTextWidth),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = barArrangement
                ) {

                    // Graph
                    graphBarData.forEachIndexed { index, value ->

                        var animationTriggered by remember {
                            mutableStateOf(false)
                        }
                        val graphBarHeight by animateFloatAsState(
                            targetValue = if (animationTriggered) value else 0f,
                            animationSpec = tween(
                                durationMillis = 1000, delayMillis = 0
                            ),
                            label = ""
                        )
                        LaunchedEffect(key1 = true) {
                            animationTriggered = true
                        }

                        Column(
                            modifier = Modifier.fillMaxHeight(),
                            verticalArrangement = Top,
                            horizontalAlignment = CenterHorizontally
                        ) {
                            // Each Graph
                            Box(
                                modifier = Modifier
                                    .padding(bottom = 5.dp)
                                    .clip(barShape)
                                    .width(barWidth)
                                    .height(height - 5.dp)
                                    .background(Color.Transparent), contentAlignment = BottomCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(barShape)
                                        .fillMaxWidth()
                                        .fillMaxHeight(graphBarHeight)
                                        .background(barColor)
                                )
                            }

                            // scale x-axis and bottom part of graph
                            Column(
                                modifier = Modifier
                                    .height(xAxisScaleHeight)
                                    .wrapContentSize(),
                                verticalArrangement = Top,
                                horizontalAlignment = CenterHorizontally
                            ) {
                                // scale x-axis
                                Text(
                                    text = xAxisScaleData[index],
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
                // horizontal line on x-axis below the graph
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent),
                    horizontalAlignment = CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .padding(bottom = xAxisScaleHeight)
                            .clip(RoundedCornerShape(2.dp))
                            .fillMaxWidth()
                            .height(horizontalLineHeight)
                            .background(horizontalLineColor)
                    )
                }
            }
        }
    }
}