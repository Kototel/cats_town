package com.conditional.cats_town.custom_view_tools.utils

import android.R.attr.theme
import android.content.Context
import android.content.res.Resources.NotFoundException
import android.content.res.TypedArray
import android.os.Bundle
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.View
import androidx.annotation.AttrRes
import androidx.annotation.CheckResult
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.annotation.IntegerRes
import androidx.annotation.StyleRes
import androidx.annotation.StyleableRes
import androidx.annotation.VisibleForTesting
import androidx.core.content.res.ResourcesCompat.ID_NULL
import androidx.core.content.res.use
import androidx.fragment.app.Fragment

/**
 * Ключ, по которому укладывается тема в аргументы фрагмента
 *
 * @see themeRes
 */
const val THEME_CONTEXT_BUILDER_FRAGMENT_THEME = "THEME_CONTEXT_BUILDER_FRAGMENT_THEME"

/**
 * Позволяет решить типовую задачу для наших реализаций view: обеспечить наличие атрибутов темизации в контексте с
 * учётом приоритетов.
 *
 * Атрибуты полученной темы разворачиваются _плоским списком_ для view как, если бы они были применены через атрибут
 * [theme]. Это позволяет получить доступ к атрибутам в xml без необходимости применять тему на корневой элемент
 * (невозможно в случай использования корневого тега `<merge>`)
 *
 * @param primaryThemeResolver функция для приоритетного получения стиля
 *
 * @author ma.kolpakov
 */
class ThemeContextBuilder @JvmOverloads constructor(
    private val context: Context,
    @AttrRes private val defStyleAttr: Int = ID_NULL,
    @StyleRes private val defaultStyle: Int = ID_NULL,
    private var primaryThemeResolver: (() -> Int?)? = null,
    private val fallbackThemeResolver: () -> Int? = {
        context.findInApplicationTheme(defStyleAttr)
    }
) {

    // не заменять на constructor reference, чтобы не ломать отображение в xml
    @VisibleForTesting
    internal var themeContextFactory: (base: Context, styleRes: Int) -> Context = { base, context ->
        ContextThemeWrapper(base, context)
    }

    /**
     * Конструктор для получения контекста с темой для [View]
     *
     * Порядок получения темы:
     * 1. тема из атрибута [theme] в xml
     * 2. тема из атрибута [defStyleAttr] в [context]
     * 3. тема по умолчанию [defaultStyle]
     */
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int = ID_NULL,
        @StyleRes defaultStyle: Int = ID_NULL,
        fallbackThemeResolver: () -> Int? = { context.findInApplicationTheme(defStyleAttr) }
    ) : this(context, defStyleAttr, defaultStyle, primaryThemeResolver = {
        context.getThemeAttributeValue(attrs)
    }, fallbackThemeResolver = fallbackThemeResolver)

    /**
     * Конструктор для получения контекста с темой для [Fragment]
     *
     * Порядок получения темы:
     * 1. тема из аргументов фрагмента [fragment] по ключу [THEME_CONTEXT_BUILDER_FRAGMENT_THEME]
     * 2. тема из атрибута [defStyleAttr] в [context]
     * 3. тема по умолчанию [defaultStyle]
     *
     * @see themeRes
     */
    @JvmOverloads
    constructor(
        context: Context,
        fragment: Fragment,
        @AttrRes defStyleAttr: Int = ID_NULL,
        @StyleRes defaultStyle: Int = ID_NULL
    ) : this(context, fragment.arguments, THEME_CONTEXT_BUILDER_FRAGMENT_THEME, defStyleAttr, defaultStyle)

    /**
     * Конструктор для получения контекста с темой для [Fragment]
     *
     * Порядок получения темы:
     * 1. тема из аргументов [arguments] по ключу [argumentKey]
     * 2. тема из атрибута [defStyleAttr] в [context]
     * 3. тема по умолчанию [defaultStyle]
     *
     * @see themeRes
     */
    @JvmOverloads
    constructor(
        context: Context,
        arguments: Bundle?,
        argumentKey: String,
        @AttrRes defStyleAttr: Int = ID_NULL,
        @StyleRes defaultStyle: Int = ID_NULL
    ) : this(context, defStyleAttr, defaultStyle, primaryThemeResolver = {
        arguments?.getInt(argumentKey, ID_NULL).takeIf { it != ID_NULL }
    })

    /**
     * Возвращает Context с применённой темой.
     */
    @CheckResult
    fun build(): Context =
        themeContextFactory.invoke(context, buildThemeRes())

    /**
     * Возвращает ресурс темы.
     */
    @CheckResult
    @StyleRes
    fun buildThemeRes(): Int =
        primaryThemeResolver?.invoke()
            ?: context.getDataFromAttrOrNull(defStyleAttr)
            ?: fallbackThemeResolver.invoke()
            ?: defaultStyle
}

/**
 * Расширение для установки аргумента темы во фрагмент
 *
 * @see ThemeContextBuilder
 * @see THEME_CONTEXT_BUILDER_FRAGMENT_THEME
 */
@get:StyleRes
@setparam:StyleRes
var Fragment.themeRes: Int
    get() = arguments?.themeRes ?: ID_NULL
    set(value) {
        requireArguments().themeRes = value
    }

/**
 * @see Fragment.themeRes
 */
@get:StyleRes
@setparam:StyleRes
var Bundle.themeRes: Int
    get() = getInt(THEME_CONTEXT_BUILDER_FRAGMENT_THEME, ID_NULL)
    set(value) {
        putInt(THEME_CONTEXT_BUILDER_FRAGMENT_THEME, value)
    }

/**
 * Get color from current theme
 *
 * @param attrColor Color attribute from custom attributes
 * @return Id 'color' res for current theme
 */
@ColorRes
fun Context.getThemeColor(@AttrRes attrColor: Int) = getResIdForCurrentTheme(attrColor)

/**
 * Get dimension from current theme
 *
 * @param attrDimension Dimension attribute from custom attributes
 * @return Id 'dimension' res for current theme
 */
@DimenRes
fun Context.getThemeDimension(@AttrRes attrDimension: Int) = getResIdForCurrentTheme(attrDimension)

/**
 * Get drawable from current theme
 *
 * @param attrDrawable Drawable attribute from custom attributes
 * @return Id 'drawable' res for current theme
 */
@DrawableRes
fun Context.getThemeDrawable(@AttrRes attrDrawable: Int) = getResIdForCurrentTheme(attrDrawable)

/**
 * Get integer from current theme
 *
 * @param attrInt Integer attribute from custom attributes
 * @return Id 'integer' res for current theme
 */
@Suppress("unused")
@IntegerRes
fun Context.getThemeInteger(@AttrRes attrInt: Int) = getResDataForCurrentTheme(attrInt)

fun Context.getThemeBoolean(attrBoolean: Int) = getResBooleanForCurrentTheme(attrBoolean)

/**
 * Получить цвет по аттрибута из текущей темы
 */
@ColorInt
fun Context.getThemeColorInt(@AttrRes attrColor: Int) = getResDataForCurrentTheme(attrColor)

/**
 * Получить идентификатор шрифта из аттрибута из текущей темы
 */
@FontRes
fun Context.getFontFromTheme(@AttrRes attrFont: Int) = getResIdForCurrentTheme(attrFont)

/**
 * Get custom attr from current theme, if him not initialized
 * returned null value
 *
 * @param attr Custom attribute for check
 * @return Id res for current theme or null
 */
@JvmOverloads
fun Context.getDataFromAttrOrNull(@AttrRes attr: Int, resolveRefs: Boolean = true): Int? {
    return TypedValue().takeIf { theme.resolveAttribute(attr, it, resolveRefs) }?.data
}

/**
 * Возвращает размер из темы по идентификатору атрибута [attr]
 *
 * @throws NotFoundException если [attr] не найден в теме
 */
@JvmOverloads
fun Context.getDimen(
    @AttrRes attr: Int,
    out: TypedValue = TypedValue(),
    resolveRefs: Boolean = true
): Float = if (theme.resolveAttribute(attr, out, resolveRefs))
    out.getDimension(resources.displayMetrics)
else
    throw NotFoundException("Unable to get dimen for attr ${resources.getResourceEntryName(attr)}")

/**
 * Возвращает размер из темы по идентификатору атрибута [attr]
 *
 * @throws NotFoundException если [attr] не найден в теме
 */
@JvmOverloads
fun Context.getDimenPx(
    @AttrRes attr: Int,
    out: TypedValue = TypedValue(),
    resolveRefs: Boolean = true
): Int = if (theme.resolveAttribute(attr, out, resolveRefs))
    TypedValue.complexToDimensionPixelSize(out.data, resources.displayMetrics)
else
    throw NotFoundException("Unable to get dimen for attr ${resources.getResourceEntryName(attr)}")
/**
 * Применяет аттрибуты к текущей теме из другой темы, полученной из переданного аттрибута.
 * [force] по-умолчанию не переопределяет аттрибуты из переданной темы, если они объявлены в текущей
 * Если получить тему из аттрибута не удалось - применяет аттрибуты из темы, переданной по умолчанию
 */
fun Context.mergeAttrsWithCurrentTheme(attr: Int, defaultTheme: Int, force: Boolean = false) {
    val merging = getDataFromAttrOrNull(attr) ?: defaultTheme
    theme.applyStyle(merging, force)
}

/**
 * Ищет дочернюю тему с именем аттрибута [themeNameAttr] внутри текущей темы и внутри темы приложения.
 * В случае нахождения аттрибута с указанным именем - применяет содержимое найденного аттрибута к текущей теме,
 * иначе - применяет к текущей теме резервный стиль [reserveTheme], если он задан.
 * ВАЖНО - переписывает ранее определенные аттрибуты
 */
fun Context.obtainThemeAttrsAndMerge(themeNameAttr: Int, @StyleRes reserveTheme: Int = ID_NULL) {
    val themeToApply = getDataFromAttrOrNull(themeNameAttr)
        ?: run {
            // Ищем аттрибут внутри темы приложения. Если находим - мержим аттрибуты в текущую тему и возвращаем
            // идентификатор ресурса целевой дочерней темы для применения ее аттрибутов к текущей
            val id = getResIdForThemeByThemeId(themeNameAttr, applicationInfo.theme)
            when {
                id != ID_NULL -> {
                    theme.applyStyle(applicationInfo.theme, true)
                    id
                }
                reserveTheme != ID_NULL -> reserveTheme
                else -> return
            }
        }
    theme.applyStyle(themeToApply, true)
}

/**
 * Получить ссылку на цвет, содержащийся в другой теме.
 * Необходимо для случаев, когда в текущей теме нет необходимых атрибутов, и известен идентификатор темы, в которой эти атрибуты есть.
 *
 * @param attrColor Атрибут цвета.
 * @param themeRes Идентификатор темы, в которой содержится атрибут [attrColor]
 */
fun Context.getThemeColorByThemeId(@AttrRes attrColor: Int, @IntegerRes themeRes: Int): Int =
    getResIdForThemeByThemeId(attrColor, themeRes)

/**
 * Получить цвет, содержащийся в другой теме.
 * Необходимо для случаев, когда в текущей теме нет необходимых атрибутов, и известен идентификатор
 * темы, в которой эти атрибуты есть.
 *
 * @param attrColor Атрибут цвета.
 * @param themeRes Идентификатор темы, в которой содержится атрибут [attrColor]
 */
fun Context.getThemeColorIntByThemeId(@AttrRes attrColor: Int, @IntegerRes themeRes: Int): Int {
    val themeForResolve = resources.newTheme()
    themeForResolve.applyStyle(themeRes, true)

    val typedValue = TypedValue()
    themeForResolve.resolveAttribute(attrColor, typedValue, true)
    return typedValue.data
}

private fun Context.getResIdForCurrentTheme(attr: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attr, typedValue, true)
    return typedValue.resourceId
}

private fun Context.getResDataForCurrentTheme(attr: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data
}

private fun Context.getResBooleanForCurrentTheme(attr: Int): Boolean {
    val typedValue = TypedValue()
    theme.resolveAttribute(attr, typedValue, true)
    return typedValue.data != 0
}

private fun Context.getResIdForThemeByThemeId(@AttrRes attr: Int, @IntegerRes themeRes: Int): Int {
    val themeForResolve = resources.newTheme()
    themeForResolve.applyStyle(themeRes, true)

    val typedValue = TypedValue()
    themeForResolve.resolveAttribute(attr, typedValue, true)
    return typedValue.resourceId
}

/**
 * Получение значения из enum атрибута
 */
fun <ENUM_ATTR : Enum<*>?> TypedArray.loadEnum(
    @StyleableRes valueAttr: Int,
    default: ENUM_ATTR,
    vararg values: ENUM_ATTR
): ENUM_ATTR {
    val valueCode = getInteger(valueAttr, ID_NULL)
    val ordinal = valueCode - 1
    return when {
        valueCode == ID_NULL -> default
        ordinal in values.indices -> values[ordinal]
        else -> error("Unexpected value code $valueCode")
    }
}

@StyleRes
private fun Context.findInApplicationTheme(@AttrRes attr: Int) =
    ContextThemeWrapper(this, applicationInfo.theme).getDataFromAttrOrNull(attr)

@StyleRes
private fun Context.getThemeAttributeValue(attrs: AttributeSet?): Int? = attrs?.let { attrSet ->
    obtainStyledAttributes(attrSet, intArrayOf(android.R.attr.theme)).use { arr ->
        arr.getResourceId(0, ID_NULL)
    }.takeIf { it != ID_NULL }
}