package com.conditional.cats_town.utils.custom_view_tools.utils.animation

import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange

/**
 * Утилиты для анимации цветов.
 */
object ColorAnimationUtils {

    /**
     * Получить цвет для отрисовки анимации изменения цвета.
     * @param fromColor начальный цвет.
     * @param toColor конечный цвет.
     * @param fraction доля от продолжительности анимации.
     */
    @ColorInt
    fun getAnimatedColor(
        @ColorInt fromColor: Int,
        @ColorInt toColor: Int,
        @FloatRange(from = 0.0, to = 1.0) fraction: Float
    ): Int =
        when (fraction) {
            0f -> fromColor
            1f -> toColor
            else -> Color.rgb(
                (Color.red(toColor) * fraction + Color.red(fromColor) * (1f - fraction)).toInt(),
                (Color.green(toColor) * fraction + Color.green(fromColor) * (1f - fraction)).toInt(),
                (Color.blue(toColor) * fraction + Color.blue(fromColor) * (1f - fraction)).toInt()
            )
        }
}