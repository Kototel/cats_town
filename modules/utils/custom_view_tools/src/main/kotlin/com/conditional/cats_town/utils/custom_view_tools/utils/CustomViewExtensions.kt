/**
 * Набор инструментов для облегчения работы с кастомными view.
 */
package com.conditional.cats_town.utils.custom_view_tools.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.graphics.Canvas
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowInsets
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.FloatRange
import androidx.annotation.IntRange
import androidx.annotation.Px
import androidx.annotation.StringRes
import androidx.core.graphics.withTranslation
import androidx.core.view.ViewCompat
import androidx.core.view.doOnLayout
import androidx.core.view.updateLayoutParams
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * Безопасный вызов [View.requestLayout].
 * Включает в себя [View.invalidate], тк вызов [View.requestLayout]
 * не гарантирует вызов [View.draw] в рамках жизненного цикла [View].
 */
fun View.safeRequestLayout() {
    requestLayout()
    invalidate()
}

/**
 * Разместить View на координате [x] [y] (левый верхний угол View) с рассчитанными размерами в [View.onMeasure].
 */
fun View.layout(x: Int, y: Int) {
    layout(x, y, x + measuredWidth, y + measuredHeight)
}

/**
 * Безопасно выполнить действие, обращая внимание на текущую видимость View:
 * если [View.getVisibility] == [View.GONE] - действие не будет выполнено.
 * Данный подход необходим для предотвращения лишних measure и опеределений высоты View,
 * в случае, если она полностью скрыта.
 *
 * @see safeMeasuredWidth
 * @see safeMeasuredHeight
 * @see safeMeasure
 * @see safeLayout
 */
inline fun <T> View.safeVisibility(action: () -> T): T? =
    if (visibility != View.GONE) action() else null

/**
 * Безопасно получить измеренную ширину View [View.getMeasuredWidth] c учетом ее текущей видимости.
 * @see safeVisibility
 */
inline val View.safeMeasuredWidth: Int
    get() = safeVisibility { measuredWidth } ?: 0

/**
 * Безопасно получить измеренную высоту View [View.getMeasuredHeight] c учетом ее текущей видимости.
 * @see safeVisibility
 */
inline val View.safeMeasuredHeight: Int
    get() = safeVisibility { measuredHeight } ?: 0


/**
 * Безопасно получить измеренную ширину StaticLayout [Layout.getWidth].
 */
val StaticLayout?.safeWidth: Int
    get() = this?.width ?: 0

/**
 * Безопасно получить измеренную высоту StaticLayout [Layout.getHeight].
 */
val StaticLayout?.safeHeight: Int
    get() = this?.height ?: 0

/**
 * Безопасно измерить View [View.measure] c учетом ее текущей видимости.
 * @see safeVisibility
 */
fun View.safeMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    safeVisibility { measure(widthMeasureSpec, heightMeasureSpec) }
}

/**
 * Безопасно разместить View [View.layout] c учетом ее текущей видимости.
 * @see safeVisibility
 */
fun View.safeLayout(left: Int, top: Int) {
    safeVisibility { layout(left, top) }
        ?: layout(left, top, left, top)
}

/**
 * Нарисовать StaticLayout на канвасе с сохранением состояния
 */
fun StaticLayout.drawWithSave(canvas: Canvas, x: Float = 0.0f, y: Float = 0.0f) {
    canvas.withTranslation(x, y) { draw(this) }
}

/**
 * Нарисовать StaticLayout на канвасе с сохранением состояния
 */
fun StaticLayout.drawWithSave(canvas: Canvas, x: Int = 0, y: Int = 0) {
    drawWithSave(canvas, x.toFloat(), y.toFloat())
}

/**
 * Получить значение в пикселях по переданному значению в dp,
 * округленное до целого числа (по правилам округления).
 */
@Px
fun Resources.dp(@FloatRange(from = 0.0) value: Float): Int =
    (displayMetrics.density * value).mathRoundToInt()

/**
 * Получить значение в пикселях по переданному значению в dp,
 * округленное до целого числа (по правилам округления).
 */
@Px
fun Resources.dp(@IntRange(from = 0) value: Int): Int =
    dp(value.toFloat())

/**
 * Получить значение в пикселях по переданному значению в dp,
 * округленное до целого числа (по правилам округления).
 */
@Px
fun Resources.dpF(@IntRange(from = 0) value: Int): Float =
    dp(value.toFloat()).toFloat()

/**
 * Получить значение в пикселях по переданному значению в sp,
 * округленное до целого числа (по правилам округления).
 */
@Px
fun Resources.sp(@FloatRange(from = 0.0) value: Float): Int =
    (displayMetrics.scaledDensity * value).mathRoundToInt()

/**
 * Получить значение в пикселях по переданному значению в sp,
 * округленное до целого числа (по правилам округления).
 */
@Px
fun Resources.sp(@IntRange(from = 0) value: Int): Int =
    sp(value.toFloat())

/**
 * Получить значение в пикселях по переданному значению в dp,
 * округленное до целого числа (по правилам округления).
 */
@Px
fun View.dp(@FloatRange(from = 0.0) value: Float): Int =
    resources.dp(value)

/**
 * Получить значение в пикселях по переданному значению в dp,
 * округленное до целого числа (по правилам округления).
 */
@Px
fun View.dp(@IntRange(from = 0) value: Int): Int =
    dp(value.toFloat())

/**
 * Получить значение в пикселях по переданному значению в sp,
 * округленное до целого числа (по правилам округления).
 */
@Px
fun View.sp(@FloatRange(from = 0.0) value: Float): Int =
    resources.sp(value)

/**
 * Получить значение в пикселях по переданному значению в sp,
 * округленное до целого числа (по правилам округления).
 */
@Px
fun View.sp(@IntRange(from = 0) value: Int): Int =
    sp(value.toFloat())

/**
 * Получить ширину текста для данного [TextPaint].
 *
 * @param text текст, для которого высчитывается ширина.
 * @param start индекс символа, с которого начнется измерение. (включительно)
 * @param end индекс символа, на котором закончится измерение. (не включительно, +1 к индексу символа)
 * @param byLayout измерение по api [Layout]. Тяжелее, но позволяет получить реальную ширину для spannable текста
 * со спанами размера.
 */
@Px
fun TextPaint.getTextWidth(
    text: CharSequence,
    start: Int = 0,
    end: Int = text.length,
    byLayout: Boolean = false
): Int =
    if (byLayout) {
        ceil(Layout.getDesiredWidth(text, start, end, this)).toInt()
    } else {
        measureText(text, start, end).toInt()
    }

/**
 * Получить ширину текста и индекс последнего символа для данного [TextPaint] с ограничением в [maxWidth].
 * Метод позволяет не производить лишних измерений текста, если он не влезает в ограничение [maxWidth].
 *
 * @param text текст, для которого высчитывается ширина.
 * @param maxWidth максимально допустимое пространство для текста.
 * @param byLayout измерение по api [Layout]. Тяжелее, но позволяет получить реальную ширину для spannable текста
 * со спанами размера.
 *
 * @return пара - ширина текста, но не больше [maxWidth], и индекс последнего символа во время ручного измерения.
 * Важно: индекс последнего символа может находиться за пределами [maxWidth], его необходимо использовать
 * исключительно для построения статичной текстовой разметки для оптимизации измерений, чтобы не гонять
 * layout по тексту за пределами видимости.
 * Пример: текст на 200 символов, в layout максимум может отобразиться 50, передача индекса в 50
 * при построении разметки почти в 3 раза ускоряет построение за счет исключения обработки лишнего текста.
 */
fun TextPaint.getTextWidth(
    text: CharSequence,
    maxWidth: Int,
    byLayout: Boolean = false,
    checkMultiLines: Boolean = true
): Pair<Int, Int> {
    if (maxWidth <= 0) return 0 to 0
    val (correctText, length) = correctTextAndLength(text, maxWidth, byLayout, checkMultiLines)
    return if (length > MANUAL_TEXT_MEASURE_LENGTH && !byLayout) {
        val step = MANUAL_TEXT_MEASURE_SYMBOLS_STEP
        val steps = ceil(length / step.toFloat()).toInt()
        var sumWidth = 0f
        var startIndex = 0
        var lastIndex = 0

        for (i in 1..steps) {
            lastIndex = (i * step).coerceAtMost(length)
            sumWidth += getTextWidth(correctText, startIndex, lastIndex)
            if (sumWidth >= maxWidth) {
                return maxWidth to lastIndex
            } else {
                startIndex = lastIndex
            }
        }

        sumWidth.toInt() to lastIndex
    } else {
        val textWidth = this@getTextWidth.getTextWidth(correctText, byLayout = byLayout).coerceAtMost(maxWidth)
        textWidth to length
    }
}

private fun TextPaint.correctTextAndLength(
    text: CharSequence,
    maxWidth: Int,
    byLayout: Boolean = false,
    checkMultiLines: Boolean
): Pair<CharSequence, Int> {
    var longestString: CharSequence = ""
    val longestStrings = mutableListOf<Pair<String, Int>>()
    if (checkMultiLines && text.contains("\n")) {
        val strings = text.split("\n")
        strings.forEach {
            if (it.length >= longestString.length) {
                if (it.length > longestString.length) longestStrings.clear()
                longestStrings.add(it to it.length)
                longestString = it
            }
        }
        var lastWidth = 0
        if (longestStrings.size > 1) {
            longestStrings.forEach {
                val width = getTextWidth(
                    text = it.first,
                    maxWidth = maxWidth,
                    byLayout = byLayout,
                    checkMultiLines = false
                ).first
                if (width > lastWidth) {
                    lastWidth = width
                    longestString = it.first
                }
            }
        }
    } else {
        longestString = text
    }
    return longestString to longestString.length
}

/**
 * Получить высоту одной строчки текста для данного [TextPaint].
 */
@get:Px
val TextPaint.textHeight: Int
    get() = ceil(fontMetrics.descent - fontMetrics.ascent).toInt()

/**
 * Метод для правильного математического округления дробных чисел по модулю.
 * [Math.round] округляет отрицательные половинчатые числа к бОльшему значению, а не по модулю
 * (например, round(-1.5) == -1 и round(1.5) == 2),
 * и логика этого метода разнится с округлением значений из ресурсов.
 * http://proglang.su/java/numbers-round
 */
internal fun Float.mathRoundToInt(): Int =
    abs(this).roundToInt().let { result ->
        if (this >= 0) result
        else result * -1
    }

/**
 * Performs the given action when this view is next laid out.
 *
 * @see doOnLayout
 */
@Deprecated(message = "Используйте ktx extensions в View.kt")
inline fun View.doOnNextLayout(crossinline action: (view: View) -> Unit) {
    addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
        override fun onLayoutChange(
            view: View,
            left: Int,
            top: Int,
            right: Int,
            bottom: Int,
            oldLeft: Int,
            oldTop: Int,
            oldRight: Int,
            oldBottom: Int
        ) {
            view.removeOnLayoutChangeListener(this)
            action(view)
        }
    })
}

/**
 * Performs the given action when this view is laid out. If the view has been laid out and it
 * has not requested a layout, the action will be performed straight away, otherwise the
 * action will be performed after the view is next laid out.
 *
 * @see doOnNextLayout
 */
@Deprecated(message = "Используйте ktx extensions в View.kt")
inline fun View.doOnLayout(crossinline action: (view: View) -> Unit) {
    if (ViewCompat.isLaidOut(this) && !isLayoutRequested) {
        action(this)
    } else {
        doOnNextLayout {
            action(it)
        }
    }
}

/**
 * Выполняет [action] при первом вызове onGlobalLayout, при котором [skipWhile] вернёт false (по умолчанию просто при
 * первом вызове)
 *
 * @return установленный [ViewTreeObserver.OnGlobalLayoutListener]
 */
inline fun View.doOnNextGlobalLayout(
    crossinline skipWhile: () -> Boolean = { false },
    crossinline action: () -> Unit
): ViewTreeObserver.OnGlobalLayoutListener {
    val listener = object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (skipWhile()) return
            viewTreeObserver.removeOnGlobalLayoutListener(this)
            action()
        }
    }
    return listener.also { viewTreeObserver.addOnGlobalLayoutListener(it) }
}

/**
 * Performs the given action when the view tree is about to be drawn.
 */
@Deprecated(message = "Используйте ktx extensions в View.kt")
inline fun <T : View> T.doOnPreDraw(crossinline action: (view: T) -> Unit) {
    val vto = viewTreeObserver
    vto.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
        override fun onPreDraw(): Boolean {
            action(this@doOnPreDraw)
            when {
                vto.isAlive -> vto.removeOnPreDrawListener(this)
                else -> viewTreeObserver.removeOnPreDrawListener(this)
            }
            return true
        }
    })
}

/**
 * Выполняет [action] при отсоединении [View] от окна
 */
@Suppress("unused")
inline fun View.doOnDetachedFromWindow(crossinline action: (view: View) -> Unit) {
    addOnAttachStateChangeListener(
        object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(view: View) = Unit

            override fun onViewDetachedFromWindow(v: View) {
                removeOnAttachStateChangeListener(this)
                action(this@doOnDetachedFromWindow)
            }
        }
    )
}

/**
 * Выполняет [action] при присоединении [View] к окну
 */
@Suppress("unused")
inline fun <T : View> T.doOnAttachToWindow(crossinline action: (view: T) -> Unit) {
    addOnAttachStateChangeListener(
        object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(view: View) {
                removeOnAttachStateChangeListener(this)
                action(this@doOnAttachToWindow)
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        }
    )
}

/**
 * Sends [AccessibilityEvent] of type [AccessibilityEvent.TYPE_ANNOUNCEMENT].
 *
 * @see View.announceForAccessibility
 */
@Suppress("unused")
@Deprecated(message = "Используйте ktx extensions в View.kt")
inline fun View.announceForAccessibility(@StringRes resource: Int) {
    val announcement = resources.getString(resource)
    announceForAccessibility(announcement)
}

/**
 * Класс, содержащий внутренние отступы для view.
 */
data class ViewPaddings(val left: Int, val top: Int, val right: Int, val bottom: Int)

/**
 * Класс, содержащий отступы для view.
 */
data class ViewMargins(val left: Int, val start: Int, val top: Int, val right: Int, val end: Int, val bottom: Int)

/**
 * Возвращает внутренние отступы для view в виде объекта [ViewPaddings]
 */
fun View.getPaddings(): ViewPaddings = ViewPaddings(paddingLeft, paddingTop, paddingRight, paddingBottom)

/**
 * Возвращает отступы для view в виде объекта [ViewMargins]
 */
fun View.getMargins(): ViewMargins =
    (layoutParams as ViewGroup.MarginLayoutParams).let { marginLayoutParams ->
        ViewMargins(
            marginLayoutParams.leftMargin,
            marginLayoutParams.marginStart,
            marginLayoutParams.topMargin,
            marginLayoutParams.rightMargin,
            marginLayoutParams.marginEnd,
            marginLayoutParams.bottomMargin
        )
    }

/**
 * Updates this view's padding. This version of the method allows using named parameters
 * to just set one or more axes.
 *
 * @see View.setPadding
 */
@Deprecated(message = "Используйте ktx extensions в View.kt")
inline fun View.updatePadding(
    @Px left: Int = paddingLeft,
    @Px top: Int = paddingTop,
    @Px right: Int = paddingRight,
    @Px bottom: Int = paddingBottom
) {
    setPadding(left, top, right, bottom)
}

/**
 * Sets the view's padding. This version of the method sets all axes to the provided size.
 *
 * @see View.setPadding
 */
@Deprecated(message = "Используйте ktx extensions в View.kt")
inline fun View.setPadding(@Px size: Int) {
    setPadding(size, size, size, size)
}

/**
 * Returns true when this view's visibility is [View.VISIBLE], false otherwise.
 *
 * ```
 * if (view.isVisible) {
 *     // Behavior...
 * }
 * ```
 *
 * Setting this property to true sets the visibility to [View.VISIBLE], false to [View.GONE].
 *
 * ```
 * view.isVisible = true
 * ```
 */
@Deprecated(message = "Используйте ktx extensions в View.kt")
inline var View.isVisible: Boolean
    get() = visibility == View.VISIBLE
    set(value) {
        visibility = if (value) View.VISIBLE else View.GONE
    }

/**
 * Returns true when this view's visibility is [View.INVISIBLE], false otherwise.
 *
 * ```
 * if (view.isInvisible) {
 *     // Behavior...
 * }
 * ```
 *
 * Setting this property to true sets the visibility to [View.INVISIBLE], false to [View.VISIBLE].
 *
 * ```
 * view.isInvisible = true
 * ```
 */
@Deprecated(message = "Используйте ktx extensions в View.kt")
inline var View.isInvisible: Boolean
    get() = visibility == View.INVISIBLE
    set(value) {
        visibility = if (value) View.INVISIBLE else View.VISIBLE
    }

/**
 * Returns true when this view's visibility is [View.GONE], false otherwise.
 *
 * ```
 * if (view.isGone) {
 *     // Behavior...
 * }
 * ```
 *
 * Setting this property to true sets the visibility to [View.GONE], false to [View.VISIBLE].
 *
 * ```
 * view.isGone = true
 * ```
 */
@Deprecated(message = "Используйте ktx extensions в View.kt")
inline var View.isGone: Boolean
    get() = visibility == View.GONE
    set(value) {
        visibility = if (value) View.GONE else View.VISIBLE
    }

/**
 * Executes [block] with the View's layoutParams and reassigns the layoutParams with the
 * updated version.
 *
 * @see View.getLayoutParams
 * @see View.setLayoutParams
 **/
@Deprecated(message = "Используйте ktx extensions в View.kt")
inline fun View.updateLayoutParams(block: ViewGroup.LayoutParams.() -> Unit) {
    updateLayoutParams<ViewGroup.LayoutParams>(block)
}

/**
 * Executes [block] with a typed version of the View's layoutParams and reassigns the
 * layoutParams with the updated version.
 *
 * @see View.getLayoutParams
 * @see View.setLayoutParams
 **/
@JvmName("updateLayoutParamsTyped")
@Deprecated(message = "Используйте ktx extensions в View.kt")
inline fun <reified T : ViewGroup.LayoutParams> View.updateLayoutParams(block: T.() -> Unit) {
    val params = layoutParams as T
    block(params)
    layoutParams = params
}

/** @SelfDocumented */
@JvmName("doOnApplyWindowInsetsPaddings")
inline fun View.doOnApplyWindowInsets(
    crossinline applyWindowInsets: (View, WindowInsets, ViewPaddings) -> Unit
) {
    val initialPaddings = getPaddings()
    setOnApplyWindowInsetsListener { view, insets ->
        applyWindowInsets(view, insets, initialPaddings)
        insets
    }
    requestApplyInsetsWhenAttached()
}

/** @SelfDocumented */
@JvmName("doOnApplyWindowInsetsMargins")
inline fun View.doOnApplyWindowInsets(
    crossinline applyWindowInsets: (View, WindowInsets, ViewMargins) -> Unit
) {
    val initialMargins = getMargins()
    setOnApplyWindowInsetsListener { view, insets ->
        applyWindowInsets(view, insets, initialMargins)
        insets
    }
    requestApplyInsetsWhenAttached()
}

/** @SelfDocumented */
fun View.applyWindowInsets(windowInsets: WindowInsets, viewPaddings: ViewPaddings) {
    setPadding(
        viewPaddings.left + windowInsets.systemWindowInsetLeft,
        viewPaddings.top + windowInsets.systemWindowInsetTop,
        viewPaddings.right + windowInsets.systemWindowInsetRight,
        viewPaddings.bottom + windowInsets.systemWindowInsetBottom
    )
}

/** @SelfDocumented */
fun View.applyWindowInsets(windowInsets: WindowInsets, viewMargins: ViewMargins) {
    updateLayoutParams<ViewGroup.MarginLayoutParams> {
        leftMargin = viewMargins.left + windowInsets.systemWindowInsetLeft
        marginStart = viewMargins.start + windowInsets.systemWindowInsetLeft
        topMargin = viewMargins.top + windowInsets.systemWindowInsetTop
        rightMargin = viewMargins.right + windowInsets.systemWindowInsetRight
        marginEnd = viewMargins.end + windowInsets.systemWindowInsetRight
        bottomMargin = viewMargins.bottom + windowInsets.systemWindowInsetBottom
    }
}

/** @SelfDocumented */
fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) {
        requestApplyInsets()
    } else {
        addOnAttachStateChangeListener(
            object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(view: View) {
                    view.removeOnAttachStateChangeListener(this)
                    view.requestApplyInsets()
                }

                override fun onViewDetachedFromWindow(v: View) = Unit
            }
        )
    }
}

/** @SelfDocumented */
@Suppress("unused")
fun View.applyWidth(width: Int) {
    layoutParams = layoutParams.apply { this.width = width }
}

/** @SelfDocumented */
fun View.applyHeight(height: Int) {
    layoutParams = layoutParams.apply { this.height = height }
}

/** @SelfDocumented */
@Suppress("unused")
fun View.setHorizontalPadding(horizontalPadding: Int) {
    setPadding(
        horizontalPadding,
        paddingTop,
        horizontalPadding,
        paddingBottom
    )
}

/**
 * Получение Activity в любой View
 */
tailrec fun View.getActivity(viewContext: Context = context): Activity =
    when (viewContext) {
        is Activity -> viewContext
        is ContextWrapper -> getActivity(viewContext.baseContext)
        else -> throw IllegalArgumentException("Can not find Activity by viewContext")
    }

/**
 * Выполняет указанный блок кода, если view видима.
 */
inline fun <T : View> T.runOnVisible(block: T.() -> Unit) {
    if (visibility != View.GONE) {
        block()
    }
}

/**
 * Возвращает ширину view с учётом отступов.
 */
fun View.getFullMeasuredWidth(): Int {
    runOnVisible {
        val layoutParams = (layoutParams as ViewGroup.MarginLayoutParams)
        return this.measuredWidth + layoutParams.marginStart + layoutParams.marginEnd
    }
    return 0
}

/**
 * Возвращает высоту view с учётом отступов.
 */
fun View.getFullMeasuredHeight(): Int {
    runOnVisible {
        val layoutParams = (layoutParams as ViewGroup.MarginLayoutParams)
        return this.measuredHeight + layoutParams.topMargin + layoutParams.bottomMargin
    }
    return 0
}

/**
 * Возвращает ширину view с учётом отступов.
 */
@get:JvmName("getFullMeasureWidthNullable")
val View?.fullMeasuredWidth
    get() = this?.getFullMeasuredWidth() ?: 0
/**
 * Обновляет отступ сверху
 *
 * @return true если значение изменилось и выполнен [View.requestLayout]
 */
fun View.updateTopMargin(@Px margin: Int): Boolean =
    with(layoutParams as ViewGroup.MarginLayoutParams) {
        if (topMargin != margin) {
            topMargin = margin
            requestLayout()
            true
        } else {
            false
        }
    }

/**
 * Обновляет отступ снизу
 *
 * @return true если значение изменилось и выполнен [View.requestLayout]
 */
fun View.updateBottomMargin(@Px margin: Int): Boolean =
    with(layoutParams as ViewGroup.MarginLayoutParams) {
        if (bottomMargin != margin) {
            bottomMargin = margin
            requestLayout()
            true
        } else {
            false
        }
    }

/**
 * Обновляет отступ слева
 *
 * @return true если значение изменилось и выполнен [View.requestLayout]
 */
fun View.updateLeftMargin(@Px margin: Int): Boolean =
    with(layoutParams as ViewGroup.MarginLayoutParams) {
        if (leftMargin != margin) {
            leftMargin = margin
            requestLayout()
            true
        } else {
            false
        }
    }

/**
 * Обновляет отступ справа
 *
 * @return true если значение изменилось и выполнен [View.requestLayout]
 */
fun View.updateRightMargin(@Px margin: Int): Boolean =
    with(layoutParams as ViewGroup.MarginLayoutParams) {
        if (rightMargin != margin) {
            rightMargin = margin
            requestLayout()
            true
        } else {
            false
        }
    }

//region Padding
/**@SelfDocumented*/
fun View.setVerticalPadding(top: Int, bottom: Int = top) {
    setPadding(paddingLeft, top, paddingRight, bottom)
}

/**@SelfDocumented*/
fun View.setTopPadding(padding: Int) {
    setVerticalPadding(padding, paddingBottom)
}

/**@SelfDocumented*/
fun View.setBottomPadding(padding: Int) {
    setVerticalPadding(paddingTop, padding)
}

/**@SelfDocumented*/
fun View.setHorizontalPadding(left: Int, right: Int = left) {
    setPadding(left, paddingTop, right, paddingBottom)
}

/**@SelfDocumented*/
fun View.setLeftPadding(padding: Int) {
    setHorizontalPadding(padding, paddingRight)
}

/**@SelfDocumented*/
fun View.setRightPadding(padding: Int) {
    setHorizontalPadding(paddingLeft, padding)
}
//endregion

//region Margin
/**@SelfDocumented*/
fun View.setMargins(left: Int, top: Int, right: Int, bottom: Int) {
    marginParams().setMargins(left, top, right, bottom)
}

/**@SelfDocumented*/
fun View.setVerticalMargin(top: Int, bottom: Int) {
    with(marginParams()) {
        setMargins(leftMargin, top, rightMargin, bottom)
        layoutParams = this
    }
}

/**@SelfDocumented*/
fun View.setTopMargin(margin: Int) {
    setVerticalMargin(margin, marginParams().bottomMargin)
}

/**@SelfDocumented*/
fun View.setBottomMargin(margin: Int) {
    setVerticalMargin(marginParams().topMargin, margin)
}

/**@SelfDocumented*/
fun View.setHorizontalMargin(left: Int, right: Int) {
    with(marginParams()) {
        setMargins(left, topMargin, right, bottomMargin)
        layoutParams = this
    }
}

/**@SelfDocumented*/
fun View.setLeftMargin(margin: Int) {
    setHorizontalMargin(margin, marginParams().rightMargin)
}

/**@SelfDocumented*/
fun View.setRightMargin(margin: Int) {
    setHorizontalMargin(marginParams().leftMargin, margin)
}

/**@SelfDocumented*/
internal fun View.marginParams() = layoutParams as ViewGroup.MarginLayoutParams
//endregion

/**
 * Показывает view плавно
 * [duration] - длительность (по-умолчанию 150 мс)
 */
fun View.show(duration: Long = 150) =
    animateVisibility(true, duration)

/**
 * Скрывает view плавно
 * [duration] - длительность (по-умолчанию 150 мс)
 */
fun View.hide(duration: Long = 150) =
    animateVisibility(false, duration)

private fun View.animateVisibility(toShow: Boolean, duration: Long) {
    toShow && visibility == View.VISIBLE && return
    !toShow && visibility != View.VISIBLE && return
    val toVisibility = if (toShow) View.VISIBLE else View.GONE
    alpha = if (toShow) 0f else 1f
    visibility = toVisibility
    animate()
        .alpha(if (toShow) 1f else 0f)
        .setDuration(duration)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator, isReverse: Boolean) {
                visibility = toVisibility
            }
        })
}

/**
 * Вешает кликлистенер с задержкой на view
 */
fun View.preventDoubleClickListener(delay: Long = STANDART_CLICK_DELAY, action: (view: View) -> Unit) {
    setOnClickListener(preventDoubleClick(delay) { action.invoke(this) })
}

/**
 * Выполняет [action] после полного завершения layout фазы у View.
 */
inline fun View.postOnLayout(crossinline action: (view: View) -> Unit) {
    doOnLayout {
        if (it.isInLayout) {
            it.post {
                action(it)
            }
        } else {
            action(it)
        }
    }
}

private const val MANUAL_TEXT_MEASURE_LENGTH = 20
private const val MANUAL_TEXT_MEASURE_SYMBOLS_STEP = 10