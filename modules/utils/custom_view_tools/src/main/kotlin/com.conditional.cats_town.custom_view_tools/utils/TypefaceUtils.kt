package com.conditional.cats_town.custom_view_tools.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Typeface
import android.os.Build
import androidx.annotation.FontRes
import androidx.core.content.res.ResourcesCompat

fun Context.getFont(@FontRes fontRes: Int): Typeface? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        try {
            resources.getFont(fontRes)
        } catch (exception: Resources.NotFoundException) {
            ResourcesCompat.getFont(this, fontRes)
        }
    } else {
        ResourcesCompat.getFont(this, fontRes)
    }
}