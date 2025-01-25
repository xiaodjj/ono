package moe.ono.util

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.forEach
import com.afollestad.materialdialogs.MaterialDialog

fun ViewGroup.findViewByText(text: String, contains: Boolean = false) =
    this.findViewByCondition {
        it.javaClass == TextView::class.java && if (!contains) (it as TextView).text == text else (it as TextView).text.contains(
            text
        )
    }

fun ViewGroup.findViewByType(clazz: Class<*>) =
    this.findViewByCondition {
        it.javaClass.isAssignableFrom(clazz)
    }

fun ViewGroup.findViewByCondition(condition: (view: View) -> Boolean): View? {
    this.forEach {
        if (condition(it))
            return it
        val ret = if (it is ViewGroup) {
            it.findViewByCondition(condition)
        } else null
        ret?.let {
            return ret
        }
    }
    return null
}

@Suppress("unused")
fun MaterialDialog.ignoreResult() {
    // do nothing here, just ignore the result to make the lint happy
}
