package com.conditional.cats_town.utils.custom_view_tools.utils

import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

/**
 * Стандартная задержка клика
 */
const val STANDART_CLICK_DELAY = 300L

/**
 * Увеличенная задержка клика
 */
const val LONG_CLICK_DELAY = 1000L

/**
 * Блокирует отрабатывание OnClickListener вью после повторного нажатия на неё.
 * Разблокирует после вызова метода жизненного цикла onResume().
 * Такую проверку имеет смысл вешать на кнопки, открывающие другой фрагмент/активити по нажатию на них.
 *
 * @param onClickAction действие, которое необходимо выполнить при клике на вью
 *
 * @return слушатель кликов по вью
 */
@Suppress("unused")
fun Fragment.preventViewFromDoubleClick(onClickAction: (View) -> Unit) =
    preventViewFromDoubleClickWithDelayAndLifecycle(0, onClickAction)

/**
 * Блокирует отрабатывание OnClickListener вью после повторного нажатия на неё
 * Разблокирует через переданное количество миллисекунд
 *
 * @param delay задержка, после которой OnClickListener будет снова срабатывать при клике на вью
 * @param onClickAction Runnable, который необходимо выполнить при клике на вью
 *
 * @return слушатель кликов по вью
 */
fun preventDoubleClick(delay: Long = STANDART_CLICK_DELAY, onClickAction: Runnable): View.OnClickListener =
    DoubleClickPreventer(delay, object : (View) -> Unit {
        override fun invoke(p1: View) = onClickAction.run()
    })

/**
 * Блокирует отрабатывание OnClickListener вью после повторного нажатия на неё
 * Разблокирует через переданное количество миллисекунд
 *
 * @param delay задержка, после которой OnClickListener будет снова срабатывать при клике на вью
 * @param onClickAction действие, которое необходимо выполнить при клике на вью
 *
 * @return слушатель кликов по вью
 */
fun preventViewFromDoubleClickWithDelay(
    delay: Long = STANDART_CLICK_DELAY,
    onClickAction: (View) -> Unit
): View.OnClickListener =
    DoubleClickPreventer(delay, onClickAction)

/**
 * Блокирует отрабатывание OnClickListener вью после повторного нажатия на неё.
 * Разблокирует через переданное количество миллисекунд или после вызова метода жизненного цикла onResume().
 *
 * @param delay задержка, после которой OnClickListener будет снова срабатывать при клике на вью
 * @param onClickAction действие, которое необходимо выполнить при клике на вью
 *
 * @return слушатель кликов по вью
 */
fun Fragment.preventViewFromDoubleClickWithDelayAndLifecycle(
    delay: Long = STANDART_CLICK_DELAY,
    onClickAction: (View) -> Unit
): View.OnClickListener =
    DoubleClickPreventer(delay, onClickAction).apply { lifecycle.addObserver(this) }

/**
 * Класс для предотвращения двойных кликов по [View]
 *
 * @property delay задержка, после которой OnClickListener будет снова срабатывать при клике на вью
 * @property onClickAction действие, которое необходимо выполнить при клике на вью
 */
private class DoubleClickPreventer(
    private val delay: Long,
    private val onClickAction: (View) -> Unit
) : View.OnClickListener, LifecycleObserver {

    private var allowed: Boolean = true

    override fun onClick(view: View) {
        if (!allowed) return

        allowed = false
        onClickAction(view)
        if (delay != 0L) {
            view.postDelayed({ unblock() }, delay)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun unblock() {
        allowed = true
    }
}