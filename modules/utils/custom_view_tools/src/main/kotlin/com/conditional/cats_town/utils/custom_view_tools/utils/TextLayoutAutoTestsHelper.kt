package com.conditional.cats_town.utils.custom_view_tools.utils

import android.annotation.SuppressLint
import android.content.res.Resources
import android.view.View
import android.view.View.AccessibilityDelegate
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.content.res.ResourcesCompat.ID_NULL
import androidx.core.view.ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES
import org.json.JSONArray
import org.json.JSONObject
import com.conditional.cats_town.utils.custom_view_tools.TextLayout

/**
 * Вспомогательный класс для автотестов кастомных [View], использующих компонент текстовой разметки [TextLayout].
 *
 * [TextLayout] не является [View] компонентом и не отображается в иерархии [View], например, в Layout Inspector.
 * Данный класс позволяет при включенном режиме "специальных возможностей" передавать полезную информацию состояния
 * текстовых разметок из списка [layoutSet] в системные вызовы [AccessibilityDelegate.onInitializeAccessibilityNodeInfo]
 * в качестве JSON текста в [AccessibilityNodeInfo], которая доступна для автотестирования,
 * как дополнительная информация о [View].
 *
 * Способ подключения:
 * ```
 * class ExampleCustomView(context: Context) : View(context) {
 *
 *     private val textLayout1 = TextLayout()
 *     private val textLayout2 = TextLayout()
 *     // Для примера опционального поля
 *     private var textLayout3: TextLayout? = null
 *
 *     private val autoTestsHelper = TextLayoutAutoTestsHelper(textLayout1, textLayout2){
 *     // Пример доп текста
 *          "some text"
 *     }
 *
 *     // Для примера nullable разметок:
 *     fun setData(data: Data) {
 *         textLayout3 = if (data.needAddLayout) {
 *              // Если по какой-то причине существует необходимость отложенного создания TextLayout,
 *              // то новые разметки можно добавить через методы add/addAll.
 *              TextLayout().also(autoTestsHelper::add)
 *         } else {
 *              // При необходимости удалить старые разметки можно через методы remove/clear.
 *              textLayout3?.let(autoTestsHelper::remove)
 *              null
 *         }
 *     }
 * }
 * ```
 *
 * @param view вью, в которой находятся текстовые разметки [TextLayout].
 * @property layoutSet набор текстовых разметок, которые использует view.
 * @property requireFullInfo признак необходимости полной информации о разметке. По умолчанию для автотестов
 * предоставляется только id и текст в формате JSON [{"textLayoutId1":"text"}, {"textLayoutId2":"text"}].
 * @property customInfoText дополнительная информация, которую можно добавить к сформированному JSON
 * (добавляется еще одно поле {"customInformation":"some text"}).
 *
 * В качестве полной информации для каждого [TextLayout] в JSON передаются следующие параметры:
 * - [TextLayout.id] идентификатор
 * - [TextLayout.text] текст
 * - [TextLayout.textPaint] цвет и размер текста из краски
 * - [TextLayout.width] ширина
 * - [TextLayout.height] высота
 * - [TextLayout.left] левая координата относительно [View]
 * - [TextLayout.top] верхняя координата относительно [View]
 * - [TextLayout.right] правая координата относительно [View]
 * - [TextLayout.bottom] нижняя координата относительно [View]
 * - [TextLayout.isVisible] признак видимости
 * - [TextLayout.lineCount] количество строк
 * - [TextLayout.maxLines] максимально допустимое количество строк
 * - [TextLayout.paddingStart] левый отступ
 * - [TextLayout.paddingTop] верхний отступ
 * - [TextLayout.paddingEnd] правый отступ
 * - [TextLayout.paddingBottom] нижний отступ
 * - [TextLayout.isEnabled] состояния доступности
 * - [TextLayout.isPressed] состояние нажатости
 * - [TextLayout.isSelected] состояние выбранности
 */
class TextLayoutAutoTestsHelper(
    view: View,
    private val layoutSet: MutableSet<TextLayout> = mutableSetOf(),
    private val requireFullInfo: Boolean = false,
    private val customInfoText: (() -> String)? = null
) : AccessibilityDelegate() {

    constructor(view: View, vararg layouts: TextLayout, customInfo: (() -> String)? = null) : this(
        view,
        layouts.toMutableSet(),
        customInfoText = customInfo
    )

    init {
        view.accessibilityDelegate = this
        view.importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
    }

    /**
     * Список текстовых разметок хелпера, информация по которым собирается [AccessibilityDelegate].
     */
    val layouts: List<TextLayout>
        get() = layoutSet.toList()

    /**
     * Добавить в обработку тестовую разметку [layout].
     */
    fun add(layout: TextLayout) {
        layoutSet.add(layout)
    }

    /**
     * Добавить в обработку список тестовых разметок [layouts].
     */
    fun addAll(layouts: List<TextLayout>) {
        layoutSet.addAll(layouts)
    }

    /**
     * Добавить в обработку перечень тестовых разметок [layouts].
     */
    fun addAll(vararg layouts: TextLayout) {
        layoutSet.addAll(layouts.toList())
    }

    /**
     * Удалить из обработки текстовую разметку [layout].
     */
    fun remove(layout: TextLayout) {
        layoutSet.remove(layout)
    }

    /**
     * Очистить список обрабатываемых текстовых разметок.
     */
    fun clear() {
        layoutSet.clear()
    }

    override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(host, info)
        val (accessibilityDescription, layoutsInfo) = description(host)
        info.apply {
            contentDescription = accessibilityDescription
            text = layoutsInfo
        }
    }

    /**
     * Попытка исправить проблему, когда данные для тестов не подставляются. Заполняем так же [event],
     * как в примере https://developer.android.com/guide/topics/ui/accessibility/custom-views.html
     */
    override fun onInitializeAccessibilityEvent(host: View, event: AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(host, event)
        val (accessibilityDescription, layoutsInfo) = description(host)
        event.apply {
            contentDescription = accessibilityDescription
            text.add(layoutsInfo)
        }
    }

    @SuppressLint("DefaultLocale")
    private fun description(host: View): Pair<StringBuilder, String> {
        val resources = host.resources
        val accessibilityDescription = StringBuilder()
        val layoutsInfo = if (requireFullInfo) {
            val layoutInfoJsonArray = JSONArray()
            layoutSet.forEachIndexed { index, layout ->
                accessibilityDescription.appendLine(layout.text)
                layout.toFullInfoJson(resources, index).also(layoutInfoJsonArray::put)
            }
            customInfoText?.let {
                layoutInfoJsonArray.put(
                    JSONObject().apply {
                        put(CUSTOM_INFO_KEY, it.invoke())
                    }
                )
            }
            layoutInfoJsonArray.toString()
        } else {
            val layoutInfoMap = mutableMapOf<String, CharSequence>()
            layoutSet.forEachIndexed { index, layout ->
                accessibilityDescription.appendLine(layout.text)
                layoutInfoMap[layout.getIdName(resources, index)] = layout.text.toString()
            }
            customInfoText?.let {
                layoutInfoMap[CUSTOM_INFO_KEY] = it.invoke()
            }
            JSONObject(layoutInfoMap as Map<*, *>).toString()
        }
        return Pair(accessibilityDescription, layoutsInfo)
    }

    private fun TextLayout.getIdName(resources: Resources, index: Int): String =
        id.takeIf { it != ID_NULL }
            ?.let(resources::getResourceEntryName)
            ?: NO_ID_VALUE.format(index)

    private fun TextLayout.toFullInfoJson(resources: Resources, index: Int): JSONObject =
        JSONObject().apply {
            val color = String.format(
                COLOR_HEX_STRING_FORMAT,
                textPaint.color and 0xFFFFFF
            ).uppercase()

            put(ID_KEY, getIdName(resources, index))
            put(TEXT_KEY, text)
            put(TEXT_SIZE_KEY, textPaint.textSize)
            put(TEXT_COLOR_KEY, color)
            put(WIDTH_KEY, width)
            put(HEIGHT_KEY, height)
            put(LEFT_KEY, left)
            put(TOP_KEY, top)
            put(RIGHT_KEY, right)
            put(BOTTOM_KEY, bottom)
            put(IS_VISIBLE_KEY, isVisible)
            put(LINE_COUNT_KEY, lineCount)
            put(MAX_LINES_KEY, maxLines)
            put(PADDING_START_KEY, paddingStart)
            put(PADDING_TOP_KEY, paddingTop)
            put(PADDING_END_KEY, paddingEnd)
            put(PADDING_BOTTOM_KEY, paddingBottom)
            put(IS_ENABLED_KEY, isEnabled)
            put(IS_PRESSED_KEY, isPressed)
            put(IS_SELECTED_KEY, isSelected)
        }
}

/** Ключ идентификатора разметки */
private const val ID_KEY = "id"

/** Ключ текста разметки */
private const val TEXT_KEY = "text"

/** Ключ размера текста разметки */
private const val TEXT_SIZE_KEY = "textSize"

/** Ключ цвета текста разметки */
private const val TEXT_COLOR_KEY = "textColor"

/** Ключ ширини разметки */
private const val WIDTH_KEY = "width"

/** Ключ высоты разметки */
private const val HEIGHT_KEY = "height"

/** Ключ левой позиции разметки относительно [View] */
private const val LEFT_KEY = "left"

/** Ключ верхней позиции разметки относительно [View] */
private const val TOP_KEY = "top"

/** Ключ правой позиции разметки относительно [View] */
private const val RIGHT_KEY = "right"

/** Ключ нижней позиции разметки относительно [View] */
private const val BOTTOM_KEY = "bottom"

/** Ключ признака видимости разметки */
private const val IS_VISIBLE_KEY = "isVisible"

/** Ключ количества строк разметки */
private const val LINE_COUNT_KEY = "lineCount"

/** Ключ максимально допустимого количества строк разметки */
private const val MAX_LINES_KEY = "maxLines"

/** Ключ левого отступа разметки */
private const val PADDING_START_KEY = "paddingStart"

/** Ключ верхнего отсутпа разметки */
private const val PADDING_TOP_KEY = "paddingTop"

/** Ключ правого отсутпа разметки */
private const val PADDING_END_KEY = "paddingEnd"

/** Ключ нижнего отсутпа разметки */
private const val PADDING_BOTTOM_KEY = "paddingBottom"

/** Ключ состояния доступности разметки */
private const val IS_ENABLED_KEY = "isEnabled"

/** Ключ состояния нажатости разметки */
private const val IS_PRESSED_KEY = "isPressed"

/** Ключ состояния выбранности разметки */
private const val IS_SELECTED_KEY = "isSelected"

/** Ключ поля, содержащего кастомный текст, задающийся в конструторе */
private const val CUSTOM_INFO_KEY = "customInformation"

/** Значение пустого идентификатора, согласовано с автотестерами **/
private const val NO_ID_VALUE = "id_%s"
private const val COLOR_HEX_STRING_FORMAT = "#%06x"

/**
 * Добавление вспомогательного класса для автотестов для кастомных [View]
 */
fun View.addTextLayoutAccessibilityDelegateIfAutoTests(isAutoTestLaunch: Boolean, vararg layouts: TextLayout) {
    if (isAutoTestLaunch) TextLayoutAutoTestsHelper(this, *layouts)
}

/**
 * Создать делегат доступа для автостетов
 */
fun View.createAccessibilityDelegate(layouts: Map<Int, () -> CharSequence>): AccessibilityDelegate =
    object : AccessibilityDelegate() {
        override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfo) {
            super.onInitializeAccessibilityNodeInfo(host, info)
            val description = mutableMapOf<String, String>()
            layouts.forEach { (layoutId, getter) ->
                getter().let { text ->
                    if (text.isNotBlank()) {
                        description[context.resources.getResourceEntryName(layoutId)] = text.toString()
                    }
                }
            }
            info.text = JSONObject(description as Map<*, *>).toString()
        }
    }