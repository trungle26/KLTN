package com.trungld.viberide.ui.screens.shared.utils

import android.graphics.PointF
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import com.google.mlkit.vision.common.PointF3D

fun adjustPoint(point: PointF, imageWidth: Int, imageHeight: Int, screenWidth: Int, screenHeight: Int): PointF {
    val x = point.x / imageWidth * screenWidth
    val y = point.y / imageHeight * screenHeight
    return PointF(x, y)
}

fun adjustSize(size: Size, imageWidth: Int, imageHeight: Int, screenWidth: Int, screenHeight: Int): Size {
    val width = size.width / imageWidth * screenWidth
    val height = size.height / imageHeight * screenHeight
    return Size(width, height)
}

/**
 * Maps a point (in face coordinates) from the provided faceBoundingBox to the given targetRect.
 */
fun mapFacePointToTarget(
    faceBoundingBox: Rect,
    point: PointF3D,
    targetRect: Rect
): Offset {
    val normX = (point.x - faceBoundingBox.left) / faceBoundingBox.width
    val normY = (point.y - faceBoundingBox.top) / faceBoundingBox.height
    val targetX = targetRect.left + normX * targetRect.width
    val targetY = targetRect.top + normY * targetRect.height
    return Offset(targetX, targetY)
}