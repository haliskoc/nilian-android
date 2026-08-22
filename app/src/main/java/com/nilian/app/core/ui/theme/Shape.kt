package com.nilian.app.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val NilianShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp), // Standard 16.dp cards
    large = RoundedCornerShape(24.dp),  // Hero cards, dialogs, bottom sheets (24.dp)
    extraLarge = RoundedCornerShape(32.dp)
)

// Specific component shapes
val PillShape = RoundedCornerShape(50)
val CardShapeMedium = RoundedCornerShape(16.dp)
val CardShapeLarge = RoundedCornerShape(24.dp)
val DialogShape = RoundedCornerShape(28.dp)
val BottomSheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
