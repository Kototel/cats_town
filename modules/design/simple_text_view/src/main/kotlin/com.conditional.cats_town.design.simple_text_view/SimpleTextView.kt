package com.conditional.cats_town.design.simple_text_view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.os.Build
import android.text.Layout
import android.text.Layout.Alignment
import android.text.Spannable
import android.text.TextPaint
import android.text.TextUtils.TruncateAt
import android.text.method.TransformationMethod
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import androidx.annotation.IntRange
import androidx.annotation.Px
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.content.withStyledAttributes
import androidx.core.text.clearSpans
import androidx.core.view.isGone
import org.apache.commons.lang3.StringUtils.EMPTY
import org.json.JSONObject
import com.conditional.cats_town.design.simple_text_view.R
import com.conditional.cats_town.design.simple_text_view.utils.AllCapsTransformationMethod
import com.conditional.cats_town.design.simple_text_view.utils.SimpleTextViewObtainHelper
import com.conditional.cats_town.utils.custom_view_tools.TextLayout
import com.conditional.cats_town.utils.custom_view_tools.utils.sp
import com.conditional.cats_town.utils.custom_view_tools.TextLayoutConfig
import com.conditional.cats_town.utils.custom_view_tools.utils.MeasureSpecUtils.measureDirection
import com.conditional.cats_town.utils.custom_view_tools.utils.SimpleTextPaint
import com.conditional.cats_town.utils.custom_view_tools.utils.TextHighlights
import com.conditional.cats_town.utils.custom_view_tools.utils.getTextWidth
import com.conditional.cats_town.utils.custom_view_tools.utils.safeRequestLayout

/**
 * Компонент для отображения текста.
 *
 * Является оптимизированным аналогом [TextView] с сокращенным набором функционала [SimpleTextViewApi]
 * и атрибутов [R.styleable.SimpleTextView].
 * Компонент может расширяться, поэтому
 * если Вам не хватает какого-то API для вашей интеграции - обратитесь к ответственному за компонент.
 * Приветствуются предложения по переносу в компонент полезных или частоиспользуемых расширений для [TextView],
 * а также заказы на реализацию нового API, которого не хватало в нативном компоненте из коробки.
 */
open class SimpleTextView : View, SimpleTextViewApi {

    /**
     * Базовый конструктор.
     */
    @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        @AttrRes defStyleAttr: Int = R.attr.simpleTextViewTheme,
        @StyleRes defStyleRes: Int = 0
    ) : super(context, attrs, defStyleAttr, defStyleRes) {
        obtainAttrs(attrs, defStyleAttr, defStyleRes)
    }

    /**
     * Облегченный и самый быстрый конструктор для программного создания [SimpleTextView]
     * без темизации с dsl настройкой [config].
     */
    constructor(
        context: Context,
        config: SimpleTextViewConfig?
    ) : super(context) {
        applyConfig(config)
    }

    /**
     * Конструктор для программного создания [SimpleTextView] с темизированным контекстом по стилю [styleRes].
     * Настройка [config] будет применена поверх атрибутов из стиля.
     */
    constructor(
        context: Context,
        styleRes: Int,
        config: SimpleTextViewConfig? = null
    ) : super(ContextThemeWrapper(context, styleRes)) {
        obtainAttrs()
        applyConfig(config)
    }

    /**
     * Конструктор для тестирования делегации [TextLayout].
     */
    internal constructor(
        context: Context,
        textLayout: TextLayout
    ) : super(context) {
        this.textLayout = textLayout
    }

    private var textLayout: TextLayout = TextLayout {
        paint.textSize = sp(DEFAULT_TEXT_SIZE_SP).toFloat()
        maxLines = DEFAULT_MAX_LINES
        minLines = DEFAULT_MIN_LINES
        ellipsize = null
    }.apply {
        makeClickable(this@SimpleTextView)
    }
    private var isInitialized: Boolean? = true
    private val layoutTouchRect = Rect()
    private val descriptionProvider: DescriptionProvider =
        if (BuildConfig.DEBUG) DebugDescriptionProvider()
        else ReleaseDescriptionProvider()

    private val simpleTextPaint: SimpleTextPaint
        get() = textLayout.textPaint as SimpleTextPaint

    override var text: CharSequence?
        get() = textLayout.text
        set(value) {
            val isChanged = configure {
                val transformedText = transformationMethod?.getTransformation(value, this@SimpleTextView)
                text = transformedText ?: value ?: EMPTY
            }
            if (isChanged) restartForeground()
        }

    @get:Px
    override var textSize: Float
        get() = textLayout.textPaint.textSize
        set(value) {
            configure { paint.textSize = value }
        }

    @get:ColorInt
    override val textColor: Int
        get() = textLayout.textPaint.color

    override val textColors: ColorStateList
        get() = textLayout.colorStateList
            ?: ColorStateList.valueOf(textLayout.textPaint.color)

    override var linkTextColor: Int
        get() = linkTextColors?.defaultColor ?: textColor
        set(value) {
            linkTextColors = ColorStateList.valueOf(value)
        }

    override var linkTextColors: ColorStateList? = null
        set(value) {
            field = value
            updateColors()
        }

    override var allCaps: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            transformationMethod = if (value) AllCapsTransformationMethod() else null
        }

    override var isSingleLine: Boolean
        get() = textLayout.isSingleLine
        set(value) {
            configure {
                isSingleLine = value
                maxLines = if (value) SINGLE_LINE else DEFAULT_MAX_LINES
                minLines = DEFAULT_MIN_LINES
            }
        }

    override var lines: Int?
        get() = if (maxLines == minLines) maxLines else null
        set(value) {
            configure {
                maxLines = value ?: DEFAULT_MAX_LINES
                minLines = value ?: DEFAULT_MIN_LINES
            }
        }

    override var maxLines: Int?
        get() = textLayout.maxLines
        set(value) {
            configure { maxLines = value ?: DEFAULT_MAX_LINES }
        }

    override var minLines: Int?
        get() = textLayout.minLines
        set(value) {
            configure { minLines = value ?: DEFAULT_MIN_LINES }
        }

    override val lineCount: Int
        get() = textLayout.lineCount

    override var maxWidth: Int? = null
        set(value) {
            field = value?.coerceAtLeast(0)
            configure {
                maxWidth = field?.let { it - paddingStart - paddingEnd }
                    ?.coerceAtLeast(0)
            }
        }

    override var minWidth: Int? = 0
        set(value) {
            field = value?.coerceAtLeast(0) ?: 0
            configure {
                minWidth = field?.let { it - paddingStart - paddingEnd }
                    ?.coerceAtLeast(0)
                    ?: 0
            }
        }

    override var maxHeight: Int? = null
        set(value) {
            field = value?.coerceAtLeast(0)
            configure {
                maxHeight = field?.let { it - paddingTop - paddingBottom }
                    ?.coerceAtLeast(0)
            }
        }

    override var minHeight: Int? = 0
        set(value) {
            field = value?.coerceAtLeast(0) ?: 0
            configure {
                minHeight = field?.let { it - paddingTop - paddingBottom }
                    ?.coerceAtLeast(0)
                    ?: 0
            }
        }

    override var maxLength: Int?
        get() = textLayout.maxLength
        set(value) {
            configure { maxLength = value ?: Int.MAX_VALUE }
        }

    override var gravity: Int = Gravity.NO_GRAVITY
        set(value) {
            if (field == value) return
            field = value
            textLayout.configure {
                alignment = getLayoutAlignment()
                verticalGravity = value
            }
            if (!isGone && isAttachedToWindow) {
                internalLayout()
                invalidate()
            } else {
                safeRequestLayout()
            }
        }

    override var typeface: Typeface?
        get() = paint.typeface
        set(value) {
            configure { paint.typeface = value }
        }

    override var ellipsize: TruncateAt?
        get() = textLayout.ellipsize
        set(value) {
            configure { ellipsize = value }
        }

    override val ellipsizedWidth: Int
        get() = textLayout.ellipsizedWidth

    override var includeFontPadding: Boolean
        get() = textLayout.includeFontPad
        set(value) {
            configure { includeFontPad = value }
        }

    override val paint: TextPaint
        get() = textLayout.textPaint

    override var paintFlags: Int
        get() = paint.flags
        set(value) {
            configure { paint.flags = value }
        }

    override var transformationMethod: TransformationMethod? = null
        set(value) {
            val isChanged = field != value
            field = value
            if (isChanged && value != null) {
                val currentText = text
                if (currentText is Spannable) currentText.clearSpans()
                configure { text = value.getTransformation(currentText, this@SimpleTextView) }
            }
        }

    @get:androidx.annotation.IntRange(from = 0, to = 2)
    override var breakStrategy: Int
        get() = textLayout.breakStrategy
        set(value) {
            configure { breakStrategy = value.coerceAtLeast(0) }
        }

    @get:IntRange(from = 0, to = 2)
    override var hyphenationFrequency: Int
        get() = textLayout.hyphenationFrequency
        set(value) {
            configure { hyphenationFrequency = value.coerceAtLeast(0) }
        }

    @AutoSizeTextType
    override var autoSizeTextType: Int = AUTO_SIZE_TEXT_TYPE_NONE
        set(value) {
            field = value
            textLayout.isAutoTextSizeMode = value != AUTO_SIZE_TEXT_TYPE_NONE
        }

    @get:Px
    override var autoSizeMaxTextSize: Int
        get() = textLayout.autoSizeMaxTextSize
        set(value) {
            val isChanged = textLayout.autoSizeMaxTextSize != value
            textLayout.autoSizeMaxTextSize = value
            if (isChanged) safeRequestLayout()
        }

    @get:Px
    override var autoSizeMinTextSize: Int
        get() = textLayout.autoSizeMinTextSize
        set(value) {
            val isChanged = textLayout.autoSizeMinTextSize != value
            textLayout.autoSizeMinTextSize = value
            if (isChanged) safeRequestLayout()
        }

    @get:Px
    override var autoSizeStepGranularity: Int
        get() = textLayout.autoSizeStepGranularity
        set(value) {
            val isChanged = textLayout.autoSizeStepGranularity != value
            textLayout.autoSizeStepGranularity = value
            if (isChanged) safeRequestLayout()
        }

    @get:Px
    override var maxTextSize: Int
        get() = simpleTextPaint.maxTextSize
        set(value) {
            val textSize = simpleTextPaint.textSize
            simpleTextPaint.maxTextSize = value
            if (textSize != simpleTextPaint.textSize) safeRequestLayout()
        }

    @get:Px
    override var minTextSize: Int
        get() = simpleTextPaint.minTextSize
        set(value) {
            val textSize = simpleTextPaint.textSize
            simpleTextPaint.minTextSize = value
            if (textSize != simpleTextPaint.textSize) safeRequestLayout()
        }

    override val layout: Layout
        get() = textLayout.layout

    /**
     * Установить ширину view в px.
     * @see TextView.setWidth
     */
    @get:JvmName("getViewWidth")
    var width: Int
        get() = super.getWidth()
        set(value) {
            minWidth = value
            maxWidth = value
        }

    /**
     * Установить высоту view в px.
     * @see TextView.setHeight
     */
    @get:JvmName("getViewHeight")
    var height: Int
        get() = super.getHeight()
        set(value) {
            minHeight = value
            maxHeight = value
        }

    init {
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
    }

    override fun setText(@StringRes stringRes: Int) {
        text = if (stringRes != 0) {
            resources.getString(stringRes)
        } else {
            EMPTY
        }
    }

    override fun setTextWithHighlights(text: CharSequence?, highlights: TextHighlights?) {
        configure {
            this.text = text ?: EMPTY
            this.highlights = highlights
        }
    }

    override fun setTextSize(unit: Int, size: Float) {
        val newTextSize = TypedValue.applyDimension(unit, size, resources.displayMetrics)
        textSize = newTextSize
    }

    override fun setTextColor(@ColorInt color: Int) {
        setTextColor(ColorStateList.valueOf(color))
    }

    override fun setTextColor(colorStateList: ColorStateList?) {
        textLayout.colorStateList = colorStateList
        invalidate()
    }

    override fun setTextAppearance(style: Int) {
        setTextAppearance(context, style)
    }

    override fun setTextAppearance(context: Context, @StyleRes style: Int) {
        val textAppearance = SimpleTextViewObtainHelper.getTextAppearance(context, typeface, style)
        var shouldLayout = false
        var shouldInvalidate = false

        textLayout.configure {
            if (textAppearance.textSize != null) {
                this.paint.textSize = textAppearance.textSize
                shouldLayout = true
            }
            if (textAppearance.color != null) {
                this.paint.color = textAppearance.color
                shouldInvalidate = true
            }
            if (textAppearance.typeface != null) {
                this.paint.typeface = textAppearance.typeface
                shouldLayout = true
            }
        }
        if (textAppearance.colorStateList != null) {
            textLayout.colorStateList = textAppearance.colorStateList
        }
        if (textAppearance.linkColorStateList != null) {
            this@SimpleTextView.linkTextColors = textAppearance.linkColorStateList
        }
        if (textAppearance.allCaps == true) {
            this@SimpleTextView.allCaps = true
        }
        when {
            isGone -> Unit
            shouldLayout -> safeRequestLayout()
            shouldInvalidate -> invalidate()
        }
    }

    override fun setLineSpacing(spacingAdd: Float, spacingMulti: Float) {
        configure {
            this.spacingAdd = spacingAdd
            this.spacingMulti = spacingMulti
        }
    }

    override fun setTypeface(typeface: Typeface?, style: Int) {
        if (style > 0) {
            this.typeface = if (typeface == null) {
                Typeface.defaultFromStyle(style)
            } else {
                Typeface.create(typeface, style)
            }
            val typefaceStyle = this.typeface?.style ?: 0
            val need = style and typefaceStyle.inv()
            paint.isFakeBoldText = need and Typeface.BOLD != 0
            paint.textSkewX = if (need and Typeface.ITALIC != 0) ITALIC_STYLE_PAINT_SKEW else 0f
        } else {
            paint.isFakeBoldText = false
            paint.textSkewX = 0f
            this.typeface = typeface
        }
    }

    override fun measureText(text: CharSequence?): Float {
        val resultText = text ?: this.text ?: return 0f
        return paint.getTextWidth(resultText, 0, resultText.length, byLayout = text is Spannable).toFloat()
    }

    override fun getEllipsisCount(line: Int): Int =
        textLayout.getEllipsisCount(line)

    override fun getHighlightColor(): Int =
        textLayout.highlights?.highlightColor ?: -1

    override fun setTextAlignment(textAlignment: Int) {
        super.setTextAlignment(textAlignment)
        configure { alignment = getLayoutAlignment() }
    }

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        textLayout.isEnabled = enabled
    }

    override fun isEnabled(): Boolean =
        textLayout.isEnabled || super.isEnabled()

    override fun dispatchSetSelected(selected: Boolean) {
        textLayout.isSelected = selected
    }

    override fun isSelected(): Boolean =
        textLayout.isSelected || super.isSelected()

    override fun setPressed(pressed: Boolean) {
        super.setPressed(pressed && isClickable)
    }

    override fun dispatchSetPressed(pressed: Boolean) {
        textLayout.isPressed = pressed
    }

    override fun isPressed(): Boolean =
        textLayout.isPressed || super.isPressed()

    override fun dispatchSetActivated(activated: Boolean) {
        textLayout.isActivated = activated
    }

    override fun isActivated(): Boolean =
        textLayout.isActivated || super.isActivated()

    override fun isHorizontalFadingEdgeEnabled(): Boolean =
        textLayout.requiresFadingEdge

    override fun setHorizontalFadingEdgeEnabled(horizontalFadingEdgeEnabled: Boolean) {
        val isChanged = textLayout.requiresFadingEdge != horizontalFadingEdgeEnabled
        textLayout.requiresFadingEdge = horizontalFadingEdgeEnabled
        if (isChanged && textLayout.fadeEdgeSize > 0) safeRequestLayout()
    }

    override fun getHorizontalFadingEdgeLength(): Int =
        textLayout.fadeEdgeSize

    override fun setFadingEdgeLength(length: Int) {
        val rangedValue = length.coerceAtLeast(0)
        val isChanged = textLayout.fadeEdgeSize != rangedValue
        textLayout.fadeEdgeSize = rangedValue
        if (isChanged && textLayout.requiresFadingEdge) safeRequestLayout()
    }

    override fun onRtlPropertiesChanged(layoutDirection: Int) {
        if (isInitialized != true) return
        textLayout.onRtlPropertiesChanged(layoutDirection, textDirection)
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        updateColors()
    }

    override fun getBaseline(): Int {
        val layoutBaseLine = textLayout.safeLayoutBaseLine
        return if (layoutBaseLine != -1) {
            getLayoutTop() + layoutBaseLine
        } else {
            layoutBaseLine
        }
    }

    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        super.setPadding(left, top, right, bottom)
        refreshTextRestrictions()
    }

    override fun setPaddingRelative(start: Int, top: Int, end: Int, bottom: Int) {
        super.setPaddingRelative(start, top, end, bottom)
        refreshTextRestrictions()
    }

    private fun refreshTextRestrictions() {
        configure {
            minWidth = this@SimpleTextView.minWidth
                ?.let { it - paddingStart - paddingEnd }
                ?.coerceAtLeast(0)
                ?: 0
            minHeight = this@SimpleTextView.minHeight
                ?.let { it - paddingTop - paddingBottom }
                ?.coerceAtLeast(0)
                ?: 0
            maxWidth = this@SimpleTextView.maxWidth
                ?.let { it - paddingStart - paddingEnd }
                ?.coerceAtLeast(0)
            maxHeight = this@SimpleTextView.maxHeight
                ?.let { it - paddingTop - paddingBottom }
                ?.coerceAtLeast(0)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val layoutTouch = if (isEnabled) textLayout.onTouch(this, event) else false
        val superTouch = super.onTouchEvent(event)
        return layoutTouch || superTouch
    }

    override fun post(action: Runnable?): Boolean =
        // Эффективное средство для ускорения момента обработки клика на слабых девайсах.
        if (action?.javaClass?.simpleName == PERFORM_CLICK_RUNNABLE_NAME) {
            false
        } else {
            super.post(action)
        }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (autoSizeTextType != AUTO_SIZE_TEXT_TYPE_NONE) {
            configureLayoutForAutoSize(widthMeasureSpec, heightMeasureSpec)
        }
        val width = measureDirection(widthMeasureSpec) { availableWidth ->
            getInternalSuggestedMinimumWidth(availableWidth)
        }
        val horizontalPadding = paddingStart + paddingEnd
        textLayout.buildLayout(width - horizontalPadding)
        val height = measureDirection(heightMeasureSpec) {
            suggestedMinimumHeight
        }
        setMeasuredDimension(width, height)
    }

    override fun getSuggestedMinimumWidth(): Int =
        getInternalSuggestedMinimumWidth()

    private fun getInternalSuggestedMinimumWidth(availableWidth: Int? = null): Int {
        val horizontalPadding = paddingStart + paddingEnd
        val availableTextWidth = availableWidth?.let { it - horizontalPadding }
        return (horizontalPadding + textLayout.getPrecomputedWidth(availableTextWidth))
            .coerceAtLeast(super.getSuggestedMinimumWidth())
            .coerceAtLeast(minWidth ?: 0)
            .coerceAtMost(maxWidth ?: Int.MAX_VALUE)
    }

    override fun getSuggestedMinimumHeight(): Int =
        (paddingTop + paddingBottom + textLayout.height)
            .coerceAtLeast(super.getSuggestedMinimumHeight())
            .coerceAtLeast(minHeight ?: 0)
            .coerceAtMost(maxHeight ?: Int.MAX_VALUE)

    private fun configureLayoutForAutoSize(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        textLayout.isAutoSizeForAvailableSpace = MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY &&
            MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.EXACTLY
        val verticalPadding = paddingTop + paddingBottom
        val availableLayoutHeight = measureDirection(heightMeasureSpec) {
            Int.MAX_VALUE.coerceAtMost(maxHeight ?: Int.MAX_VALUE)
        } - verticalPadding
        textLayout.autoSizeAvailableHeight = availableLayoutHeight.coerceAtLeast(0)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        layoutTouchRect.set(0, 0, w, h)
        textLayout.setStaticTouchRect(layoutTouchRect)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        internalLayout()
        invalidate()
    }

    private fun internalLayout() {
        textLayout.layout(paddingLeft, getLayoutTop())
    }

    override fun onDraw(canvas: Canvas) {
        textLayout.draw(canvas)
    }

    private fun obtainAttrs(
        attrs: AttributeSet? = null,
        @AttrRes defStyleAttr: Int = 0,
        @StyleRes defStyleRes: Int = 0
    ) {
        context.withStyledAttributes(attrs, R.styleable.SimpleTextView, defStyleAttr, defStyleRes) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveAttributeDataForStyleable(
                    context,
                    R.styleable.SimpleTextView,
                    attrs,
                    this,
                    defStyleAttr,
                    defStyleRes
                )
            }
            val textAppearance = getResourceId(R.styleable.SimpleTextView_android_textAppearance, NO_RESOURCE)
                .takeIf { it != NO_RESOURCE }
            val textAppearanceData = if (textAppearance != null) {
                SimpleTextViewObtainHelper.getTextAppearance(context, typeface, textAppearance)
            } else {
                null
            }
            val text = getText(R.styleable.SimpleTextView_android_text) ?: EMPTY
            val textSize = getDimensionPixelSize(R.styleable.SimpleTextView_android_textSize, NO_RESOURCE)
                .takeIf { it != NO_RESOURCE }
                ?: textAppearanceData?.textSize
            val colorStateList = getColorStateList(R.styleable.SimpleTextView_android_textColor)
            val resultColorStateList = colorStateList ?: textAppearanceData?.colorStateList
            val color = colorStateList?.defaultColor
                ?: getColor(R.styleable.SimpleTextView_android_textColor, NO_RESOURCE)
                    .takeIf { it != NO_RESOURCE }
                ?: getResourceId(R.styleable.SimpleTextView_android_textColor, NO_RESOURCE)
                    .takeIf { it != NO_RESOURCE }
                    ?.let { ContextCompat.getColor(context, it) }
                ?: textAppearanceData?.colorStateList?.defaultColor
                ?: textAppearanceData?.color
                ?: Color.BLACK
            val linkColorStateList = getColorStateList(R.styleable.SimpleTextView_android_textColorLink)
                ?: textAppearanceData?.linkColorStateList
            val fontFamily = getResourceId(R.styleable.SimpleTextView_android_fontFamily, NO_RESOURCE)
                .takeIf { it != NO_RESOURCE }
            val textStyle = getInt(R.styleable.SimpleTextView_android_textStyle, NO_RESOURCE)
                .takeIf { it != NO_RESOURCE }
            val typeface = if (fontFamily == null && textStyle == null) {
                textAppearanceData?.typeface ?: paint.typeface
            } else {
                SimpleTextViewObtainHelper.getTypeface(
                    context,
                    textAppearanceData?.typeface,
                    fontFamily,
                    textStyle
                )
            }
            val includeFontPadding = getBoolean(R.styleable.SimpleTextView_android_includeFontPadding, true)
            val allCaps = getBoolean(R.styleable.SimpleTextView_android_textAllCaps, false)
            val gravity = getInt(R.styleable.SimpleTextView_android_gravity, NO_RESOURCE)
                .takeIf { it != NO_RESOURCE }
            val ellipsize = getInt(R.styleable.SimpleTextView_android_ellipsize, NO_RESOURCE)
                .takeIf { it != NO_RESOURCE }
            val truncateAt = ellipsize?.let {
                when (ellipsize) {
                    ELLIPSIZE_NONE -> null
                    ELLIPSIZE_END -> TruncateAt.END
                    ELLIPSIZE_START -> TruncateAt.START
                    ELLIPSIZE_MIDDLE -> TruncateAt.MIDDLE
                    ELLIPSIZE_MARQUEE -> TruncateAt.MARQUEE
                    else -> null
                }
            }
            val breakStrategy = getInt(R.styleable.SimpleTextView_android_breakStrategy, 0)
            val hyphenationFrequency = getInt(R.styleable.SimpleTextView_android_hyphenationFrequency, 0)
            val isEnabled = getBoolean(R.styleable.SimpleTextView_android_enabled, isEnabled)
            val lines = getInt(R.styleable.SimpleTextView_android_lines, NO_RESOURCE)
                .takeIf { it != NO_RESOURCE }
            val maxLines = getInt(R.styleable.SimpleTextView_android_maxLines, NO_RESOURCE)
                .takeIf { it != NO_RESOURCE }
                ?: DEFAULT_MAX_LINES
            val minLines = getInt(R.styleable.SimpleTextView_android_minLines, NO_RESOURCE)
                .takeIf { it != NO_RESOURCE }
                ?: DEFAULT_MIN_LINES
            val isSingleLine = getBoolean(R.styleable.SimpleTextView_android_singleLine, false)
            val maxLength = getInt(R.styleable.SimpleTextView_android_maxLength, Int.MAX_VALUE)
            val minWidth = getDimensionPixelSize(R.styleable.SimpleTextView_android_minWidth, NO_RESOURCE)
                .takeIf { it != NO_RESOURCE }
            val maxWidth = getDimensionPixelSize(R.styleable.SimpleTextView_android_maxWidth, NO_RESOURCE)
                .takeIf { it != NO_RESOURCE }
            val minHeight = getDimensionPixelSize(R.styleable.SimpleTextView_android_minHeight, NO_RESOURCE)
                .takeIf { it != NO_RESOURCE }
            val maxHeight = getDimensionPixelSize(R.styleable.SimpleTextView_android_maxHeight, NO_RESOURCE)
                .takeIf { it != NO_RESOURCE }

            val requiresFadingEdge = getInt(
                R.styleable.SimpleTextView_android_requiresFadingEdge,
                FADING_EDGE_NONE
            ) and FADING_EDGE_HORIZONTAL == FADING_EDGE_HORIZONTAL
            val fadingEdgeLength = getDimensionPixelSize(R.styleable.SimpleTextView_android_fadingEdgeLength, 0)

            val autoSizeTextType = getInt(
                R.styleable.SimpleTextView_SimpleTextView_autoSizeTextType,
                AUTO_SIZE_TEXT_TYPE_NONE
            )
            val autoSizeMaxTextSize = getDimensionPixelSize(
                R.styleable.SimpleTextView_SimpleTextView_autoSizeMaxTextSize,
                autoSizeMaxTextSize
            )
            val autoSizeMinTextSize = getDimensionPixelSize(
                R.styleable.SimpleTextView_SimpleTextView_autoSizeMinTextSize,
                autoSizeMinTextSize
            )
            val autoSizeStepGranularity = getDimensionPixelSize(
                R.styleable.SimpleTextView_SimpleTextView_autoSizeStepGranularity,
                autoSizeStepGranularity
            )

            val maxTextSize = getDimensionPixelSize(R.styleable.SimpleTextView_SimpleTextView_maxTextSize, NO_RESOURCE)
                .takeIf { it != NO_RESOURCE }
            val minTextSize = getDimensionPixelSize(R.styleable.SimpleTextView_SimpleTextView_minTextSize, NO_RESOURCE)
                .takeIf { it != NO_RESOURCE }

            textLayout.configure {
                this.text = text
                this.paint.also { paint ->
                    paint.textSize = (textSize ?: sp(DEFAULT_TEXT_SIZE_SP)).toFloat()
                    paint.color = color
                    paint.typeface = typeface
                }
                this.includeFontPad = includeFontPadding
                this.breakStrategy = breakStrategy
                this.hyphenationFrequency = hyphenationFrequency
                this.ellipsize = if (isSingleLine && ellipsize == null) TruncateAt.END else truncateAt
                this.maxLines = if (isSingleLine) SINGLE_LINE else lines ?: maxLines
                this.minLines = if (isSingleLine) SINGLE_LINE else lines ?: minLines
                this.isSingleLine = isSingleLine
                this.maxLength = maxLength
            }
            textLayout.also {
                it.colorStateList = resultColorStateList
                it.requiresFadingEdge = requiresFadingEdge
                it.fadeEdgeSize = fadingEdgeLength
            }
            this@SimpleTextView.also {
                it.linkTextColors = linkColorStateList
                it.isEnabled = isEnabled
                it.gravity = gravity ?: Gravity.NO_GRAVITY
                it.allCaps = allCaps
                it.autoSizeTextType = autoSizeTextType
                it.autoSizeMaxTextSize = autoSizeMaxTextSize
                it.autoSizeMinTextSize = autoSizeMinTextSize
                it.autoSizeStepGranularity = autoSizeStepGranularity
                if (minWidth != null) it.minWidth = minWidth
                if (maxWidth != null) it.maxWidth = maxWidth
                if (minHeight != null) it.minHeight = minHeight
                if (maxHeight != null) it.maxHeight = maxHeight
                if (maxTextSize != null) it.maxTextSize = maxTextSize
                if (minTextSize != null) it.minTextSize = minTextSize
            }
        }
    }

    private fun updateColors() {
        linkTextColors?.getColorForState(drawableState, linkTextColor)?.let { linkColor ->
            if (linkColor != textLayout.textPaint.linkColor) {
                textLayout.textPaint.linkColor = linkColor
                invalidate()
            }
        }
    }

    private fun getLayoutAlignment(): Alignment =
        when (textAlignment) {
            TEXT_ALIGNMENT_GRAVITY -> {
                when (gravity and Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) {
                    Gravity.CENTER_HORIZONTAL -> Alignment.ALIGN_CENTER
                    Gravity.RIGHT,
                    Gravity.END -> Alignment.ALIGN_OPPOSITE
                    else -> Alignment.ALIGN_NORMAL
                }
            }
            TEXT_ALIGNMENT_TEXT_START,
            TEXT_ALIGNMENT_VIEW_START -> Alignment.ALIGN_NORMAL
            TEXT_ALIGNMENT_TEXT_END,
            TEXT_ALIGNMENT_VIEW_END -> Alignment.ALIGN_OPPOSITE
            TEXT_ALIGNMENT_CENTER -> Alignment.ALIGN_CENTER
            else -> Alignment.ALIGN_NORMAL
        }

    private fun getLayoutTop(): Int =
        when (gravity and Gravity.VERTICAL_GRAVITY_MASK) {
            Gravity.BOTTOM -> {
                measuredHeight - paddingBottom - textLayout.height
            }
            Gravity.CENTER, Gravity.CENTER_VERTICAL -> {
                paddingTop + (measuredHeight - paddingTop - paddingBottom - textLayout.height) / 2
            }
            else -> paddingTop
        }

    private fun configure(config: TextLayoutConfig): Boolean =
        textLayout.configure(config).also { isChanged ->
            if (!isGone && isChanged) safeRequestLayout()
        }

    private fun applyConfig(config: SimpleTextViewConfig?) {
        config?.invoke(this)
    }

    private fun restartForeground() {
        foreground?.setVisible(false, true)
    }

    override fun setVerticalFadingEdgeEnabled(verticalFadingEdgeEnabled: Boolean) = Unit
    override fun getVerticalFadingEdgeLength(): Int = 0

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.text = text
    }

    override fun onPopulateAccessibilityEvent(event: AccessibilityEvent) {
        super.onPopulateAccessibilityEvent(event)
        if (!text.isNullOrEmpty()) {
            event.text.add(text)
        }
    }

    @SuppressLint("GetContentDescriptionOverride")
    override fun getContentDescription(): CharSequence {
        val contentDescription = super.getContentDescription()
        if (contentDescription.isNullOrBlank()) {
            return descriptionProvider.getContentDescription()
        }
        return contentDescription
    }

    private inner class ReleaseDescriptionProvider : DescriptionProvider {
        override fun getContentDescription(): CharSequence =
            text ?: EMPTY
    }

    private inner class DebugDescriptionProvider : DescriptionProvider {
        override fun getContentDescription(): CharSequence =
            JSONObject().apply {
                put(DESCRIPTION_TEXT_KEY, text)
                put(DESCRIPTION_TEXT_SIZE_KEY, textSize)
                put(
                    DESCRIPTION_TEXT_COLOR_KEY,
                    String.format(COLOR_HEX_STRING_FORMAT, paint.color and 0xFFFFFF).uppercase()
                )
                put(DESCRIPTION_ELLIPSIZE_KEY, ellipsize?.toString() ?: NONE_VALUE)
                if (maxLines != DEFAULT_MAX_LINES) put(DESCRIPTION_MAX_LINES_KEY, maxLines)
                if (minLines != DEFAULT_MIN_LINES) put(DESCRIPTION_MIN_LINES_KEY, minLines)
            }.toString()
    }
}

private interface DescriptionProvider {
    fun getContentDescription(): CharSequence
}

/**
 * Настройка параметров [SimpleTextView].
 */
typealias SimpleTextViewConfig = SimpleTextView.() -> Unit

const val AUTO_SIZE_TEXT_TYPE_NONE = 0
const val AUTO_SIZE_TEXT_TYPE_UNIFORM = 1

/**
 * Тип режима работы автоматического определения размера текста.
 * [AUTO_SIZE_TEXT_TYPE_NONE] - выключено.
 * [AUTO_SIZE_TEXT_TYPE_UNIFORM] - включено.
 */
@IntDef(value = [AUTO_SIZE_TEXT_TYPE_NONE, AUTO_SIZE_TEXT_TYPE_UNIFORM])
@Retention(AnnotationRetention.SOURCE)
annotation class AutoSizeTextType

private const val ELLIPSIZE_NONE = 0
private const val ELLIPSIZE_START = 1
private const val ELLIPSIZE_MIDDLE = 2
private const val ELLIPSIZE_END = 3
private const val ELLIPSIZE_MARQUEE = 4
private const val NO_RESOURCE = -1
private const val SINGLE_LINE = 1
private const val DEFAULT_MIN_LINES = 1
private const val DEFAULT_MAX_LINES = Int.MAX_VALUE
private const val FADING_EDGE_NONE = 0x00000000
private const val FADING_EDGE_HORIZONTAL = 0x00001000
private const val ITALIC_STYLE_PAINT_SKEW = -0.25f
private const val PERFORM_CLICK_RUNNABLE_NAME = "PerformClick"
private const val DEFAULT_TEXT_SIZE_SP = 14

private const val DESCRIPTION_TEXT_KEY = "text"
private const val DESCRIPTION_TEXT_SIZE_KEY = "text_size"
private const val DESCRIPTION_TEXT_COLOR_KEY = "text_color"
private const val DESCRIPTION_MAX_LINES_KEY = "max_lines"
private const val DESCRIPTION_MIN_LINES_KEY = "min_lines"
private const val DESCRIPTION_ELLIPSIZE_KEY = "ellipsize"
private const val NONE_VALUE = "none"
private const val COLOR_HEX_STRING_FORMAT = "#%06x"