package com.conditional.cats_town.design.simple_text_view.utils

import android.graphics.Rect
import android.text.method.TransformationMethod
import android.view.View

/**
 * Класс для применения трансформации всех заглавных букв для текста.
 * Нет возможности использовать нативный класс из-за прямой завязки на TextView.
 */
internal class AllCapsTransformationMethod : TransformationMethod {

    override fun getTransformation(source: CharSequence, view: View): CharSequence =
        source.toString().uppercase()

    override fun onFocusChanged(
        view: View,
        sourceText: CharSequence,
        focused: Boolean,
        direction: Int,
        previouslyFocusedRect: Rect
    ) = Unit
}