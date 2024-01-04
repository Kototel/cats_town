# Компонент текста SimpleTextView

#### Описание
Модуль содержит реализацию SimpleTextView - компонент для отображения текста на базе компонента текстовой разметки TextLayout,
созданный для ускорения создания и обновления интерфейсной части текста.
- SimpleTextView - компонент
- SimpleTextViewApi - функционал компонента

#### Отображение SimpleTextView

Компонент можно отобразить двумя способами:
1) Добавление SimpleTextView в xml разметку:
```xml
<com.conditional.cats_town.design.simple_text_view.SimpleTextView
    android:id="@+id/simple_text_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>
```
2) Программно, путем создания view.
```kotlin
val simpleTextView1 = SimpleTextView(context, attrs, R.attr.yourAttr, R.style.yourStyle)
val simpleTextView2 = SimpleTextView(context, simpleTextViewConfig)
val simpleTextView3 = SimpleTextView(context, R.style.yourStyle, simpleTextViewConfig)
```

##### Стилизация
Стандартная тема компонента SimpleTextView: `SimpleTextViewDefaultTheme`.
Атрибут для установки темы компонента SimpleTextView: `simpleTextViewTheme`(src/main/res/values/attrs.xml)
здесь же можно найти атрибуты стилизации.

Способы применения стилей и темы аналогичны стандартному View.
Дополнительно предоставлен конструктор, который позволяет программно создать SimpleTextView по стилю.
```kotlin
val simpleTextView3 = SimpleTextView(context, R.style.yourStyle, simpleTextViewConfig)
```

##### Описание особенностей работы
SimpleTextView - оптимизированный вариант TextView, поэтому в нем отсутствуют некоторые возможности
оригинального компонента.
Компонент может расширяться, поэтому если вам 
не хватает какого-то API для вашей интеграции - обратитесь к ответственному за компонент.
**Не могут быть поддержаны**
-`MovementMethod`
-`Функционал скролла внутри View`

**Особенности установки значений некоторых атрибутов**
- app:text принимает @StringRes
- Вместо app:textStyle используем app:textAppearance
- Доступ через layout.paint:  
  - было: letterSpacing = -0.05f
  - стало: layout.paint.letterSpacing = -0.05f
**Для выделения текста цветом использовать TextHighlights**
```kotlin
val textHighlights = TextHighlights(
  highlightedRanges.map { HighlightSpan(it.first, it.last) },
  resources.getColor(R.color.text_search_highlight_color)
)
setTextWithHighlights(text, textHighlights)
```