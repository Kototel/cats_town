package com.conditional.cats_town.utils.custom_view_tools.utils

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IntRange
import com.conditional.cats_town.utils.custom_view_tools.TextLayout

/**
 * Менеджер для упрощения работы с обработкой касаний по нескольким текстовым разметкам [TextLayout] в кастомной [View].
 *
 * @property view вью, в которой находятся текстовые разметки [TextLayout].
 * @property layoutList список текстовых разметок, участвующих в обработке касаний.
 * @property isSynchronizedClicks способ обработки нажатий при котором, отрабатывает один клик на все разметки, но важно
 * учесть, что тачи будут обрабатываться только при нажатии на первую разметку, чтобы реализовать
 * нажатие на остальные разметки, необходимо использовать [TextLayout.setStaticTouchRect].
 *
 * Может облегчить жизнь в сценариях использования нескольких [TextLayout] в рамках одной [View],
 * если необходим следующий функционал разметки:
 * - [TextLayout.setOnClickListener]
 * - [TextLayout.setOnLongClickListener]
 * - [TextLayout.colorStateList]
 *
 * Менеджер включает кликабельность текстовых разметок из списка [layoutList],
 * а также делегирует события касаний между [TextLayout] с соблюдением приоритетов порядка в списке.
 *
 * Логика делегирования [MotionEvent] в [TextLayout] аналогична обработке событий касания во [ViewGroup] -
 * если в позиции касания границы разметок из [layoutList] пересекаются,
 * то приоритет обработки касания будет иметь тот [TextLayout],
 * который находится ближе к концу списка, т.е. условно находится поверх других [TextLayout].
 *
 * Способ подключения:
 * ```
 * class ExampleCustomView(context: Context) : View(context) {
 *
 *     private val textLayout1 = TextLayout()
 *     private val textLayout2 = TextLayout()
 *
 *     private val touchManager = TextLayoutTouchManager(textLayout1, textLayout2)
 *
 *     override fun onTouchEvent(event: MotionEvent): Boolean =
 *         touchManager.onTouch(this, event) || super.onTouchEvent(event)
 * }
 * ```
 */
class TextLayoutTouchManager(
    private val view: View,
    private val isSynchronizedClicks: Boolean = false,
    private val layoutList: MutableList<TextLayout> = mutableListOf()
) : View.OnTouchListener {

    constructor(
        view: View,
        isSynchronizedClicks: Boolean,
        vararg layouts: TextLayout
    ) : this(view, isSynchronizedClicks, layouts.toMutableList())

    constructor(
        view: View,
        vararg layouts: TextLayout
    ) : this(view, false, layouts.toMutableList())

    init {
        makeLayoutsClickable()
    }

    /**
     * Список текстовых разметок менеджера.
     */
    val layouts: List<TextLayout>
        get() = layoutList

    /**
     * Текстовая разметка, которая последняя обрабатывала событие касания.
     */
    private var lastTouchedLayout: TextLayout? = null

    /**
     * Добавить тестовую разметку [layout] в менеджер на позицию [index].
     *
     * [index] по-умолчанию помещает [layout] на вершину обработки касаний [layoutList].
     */
    fun add(
        layout: TextLayout,
        @IntRange(from = -1) index: Int = -1
    ) {
        layoutList.apply {
            layout.makeClickable(view)
            remove(layout)
            add(if (index < 0) size else index, layout)
        }
    }

    /**
     * Добавить список тестовых разметок [layouts] в менеджер с позиции [index].
     *
     * [index] по-умолчанию помещает [layouts] на вершину обработки касаний [layoutList].
     */
    fun addAll(
        layouts: List<TextLayout>,
        @IntRange(from = -1) index: Int = -1
    ) {
        layoutList.apply {
            removeAll(layouts)
            addAll(if (index < 0) size else index, layouts)
            makeLayoutsClickable()
        }
    }

    /**
     * Добавить перечень тестовых разметок [layouts] в менеджер с позиции [index].
     *
     * [index] по-умолчанию помещает [layouts] на вершину обработки касаний [layoutList].
     */
    fun addAll(
        vararg layouts: TextLayout,
        @IntRange(from = -1) index: Int = -1
    ) {
        layoutList.apply {
            removeAll(layouts)
            addAll(if (index < 0) size else index, layouts.toList())
            makeLayoutsClickable()
        }
    }

    /**
     * Удалить текстовую разметку [layout] из менеджера.
     */
    fun remove(layout: TextLayout) {
        layoutList.remove(layout)
    }

    /**
     * Очистить менеджер от обрабатываемых текстовых разметок.
     */
    fun clear() {
        layoutList.clear()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (!v.isEnabled) return false
        return if (!isSynchronizedClicks) {
            invokeClickWithPriority(v, event)
        } else {
            invokeBroadcastClick(event, v)
        }
    }

    /**
     * Выполнить клик и оповестить все [TextLayout] в списке
     */
    private fun invokeBroadcastClick(event: MotionEvent, v: View): Boolean {
        layoutList.forEach {
            it.drawableStateHelper.checkPressedState(event.action, true)
        }
        return layoutList.firstOrNull { it.onTouch(v, event) } != null
    }

    /**
     * Выполнить клик на "верхний" [TextLayout] (последний в списке), при этом отменив действие клика на предыдущий
     */
    private fun invokeClickWithPriority(v: View, event: MotionEvent) = layoutList.findLast {
        it.onTouch(v, event)
    }
        .also { touchedLayout ->
            lastTouchedLayout?.takeIf { it !== touchedLayout }
                ?.onTouchCanceled()
            lastTouchedLayout = touchedLayout
        } != null

    private fun makeLayoutsClickable() {
        layoutList.forEach { it.makeClickable(view) }
    }
}