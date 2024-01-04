package com.conditional.cats_town.custom_view_tools.utils.animation

import android.graphics.PointF
import android.view.animation.Interpolator
import kotlin.math.abs

/**
 * Интерполятор на основе CSS функции cubic-bezier для создания функции интерполяции по 2ум заданным точкам,
 * что позволяет наглядно, легко и быстро настроить любую кастомную анимацию.
 *
 * Интерактивный сайт для визуализации интерполяции на основе заданных точек: cubic-bezier.com/
 * Подробнее: https://habr.com/ru/post/220715/
 */
class CubicBezierInterpolator(
    private var start: PointF,
    private var end: PointF
) : Interpolator {

    constructor(
        startX: Float,
        startY: Float,
        endX: Float,
        endY: Float
    ) : this(PointF(startX, startY), PointF(endX, endY))

    companion object {

        /**
         * Очень гладкий замедляющийся интерполятор.
         */
        val superSmoothDecelerateInterpolator: CubicBezierInterpolator
            get() = CubicBezierInterpolator(
                SUPER_SMOOTH_DECELERATE_START_X,
                SUPER_SMOOTH_DECELERATE_START_Y,
                SUPER_SMOOTH_DECELERATE_END_X,
                SUPER_SMOOTH_DECELERATE_END_Y
            )
    }

    private var a = PointF()
    private var b = PointF()
    private var c = PointF()

    override fun getInterpolation(time: Float): Float =
        getBezierCoordinateY(getXForTime(time))

    private fun getBezierCoordinateY(time: Float): Float {
        c.y = 3 * start.y
        b.y = 3 * (end.y - start.y) - c.y
        a.y = 1 - c.y - b.y
        return time * (c.y + time * (b.y + time * a.y))
    }

    private fun getXForTime(time: Float): Float {
        var x = time
        var z: Float
        for (i in 1..13) {
            z = getBezierCoordinateX(x) - time
            if (abs(z) < 1e-3) break
            x -= z / getXDerivative(x)
        }
        return x
    }

    private fun getXDerivative(t: Float): Float =
        c.x + t * (2 * b.x + 3 * a.x * t)

    private fun getBezierCoordinateX(time: Float): Float {
        c.x = 3 * start.x
        b.x = 3 * (end.x - start.x) - c.x
        a.x = 1 - c.x - b.x
        return time * (c.x + time * (b.x + time * a.x))
    }
}

private const val SUPER_SMOOTH_DECELERATE_START_X = 0.2f
private const val SUPER_SMOOTH_DECELERATE_START_Y = 0.01f
private const val SUPER_SMOOTH_DECELERATE_END_X = 0.28f
private const val SUPER_SMOOTH_DECELERATE_END_Y = 0.91f