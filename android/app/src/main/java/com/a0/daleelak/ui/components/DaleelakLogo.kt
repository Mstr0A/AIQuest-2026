package com.a0.daleelak.ui.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.a0.daleelak.R

/** Shared supplied SVG artwork, rendered as a native Android vector. */
@Composable
fun DaleelakLogo(modifier: Modifier = Modifier) {
    Image(painter = painterResource(R.drawable.daleelak_logo),
        contentDescription = null, modifier = modifier)
}
