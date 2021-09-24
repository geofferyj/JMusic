package com.geofferyj.jmusic.utils

import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import androidx.core.view.*
import androidx.databinding.BindingAdapter
import coil.load

@BindingAdapter("android:src")
fun setImage(imageView: ImageView, src: Int) {
    imageView.load(src)
}


@BindingAdapter("android:src")
fun setImage(imageView: ImageView, src: String?) {
    imageView.load(src)
}

@BindingAdapter("app:behavior_peekHeight")
fun peekHeight(view: View, height: Int) {

}

@BindingAdapter("android:visibility")
fun isVisible(view: View, visible: Boolean) {
    view.visibility = if (visible) View.VISIBLE else View.GONE
}

@BindingAdapter("android:visibility")
fun isVisible(view: View, visible: Int) {
    view.visibility = if (visible > 0) View.VISIBLE else View.GONE
}

@BindingAdapter("android:visibility")
fun isVisible(view: View, visible: String?) {
    view.visibility = if (!visible.isNullOrBlank()) View.VISIBLE else View.GONE
}


@BindingAdapter(
    "paddingLeftSystemWindowInsets",
    "paddingTopSystemWindowInsets",
    "paddingRightSystemWindowInsets",
    "paddingBottomSystemWindowInsets",
    "paddingImeSystemWindowInsets",
    requireAll = false
)
fun applySystemWindows(
    view: View,
    applyLeft: Boolean,
    applyTop: Boolean,
    applyRight: Boolean,
    applyBottom: Boolean,
    applyIme: Boolean
) {
    view.doOnApplyWindowInsets { v, insets, padding ->
        val left = if (applyLeft) insets.getInsets(WindowInsetsCompat.Type.systemBars()).left else 0
        val top = if (applyTop) insets.getInsets(WindowInsetsCompat.Type.systemBars()).top else 0
        val right =
            if (applyRight) insets.getInsets(WindowInsetsCompat.Type.systemBars()).right else 0
        val bottom =
            if (applyBottom) insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom else 0
        val ime = if (applyIme) insets.getInsets(WindowInsetsCompat.Type.ime()).bottom else 0

        v.setPadding(
            padding.left + left,
            padding.top + top,
            padding.right + right,
            padding.bottom + if (ime != 0) ime else bottom
        )
    }
}


fun View.doOnApplyWindowInsets(f: (View, WindowInsetsCompat, InitialPadding) -> Unit) {
    // Create a snapshot of the view's padding state
    val initialPadding = recordInitialPaddingForView(this)
    // Set an actual OnApplyWindowInsetsListener which proxies to the given
    // lambda, also passing in the original padding state
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        f(v, insets, initialPadding)
        // Always return the insets, so that children can also use them
        insets
    }
    // request some insets
    requestApplyInsetsWhenAttached()
}

@BindingAdapter(
    "marginLeftSystemWindowInsets",
    "marginTopSystemWindowInsets",
    "marginRightSystemWindowInsets",
    "marginBottomSystemWindowInsets",
    "marginImeSystemWindowInsets",
    requireAll = false
)
fun applySystemWindowsMargins(
    view: View,
    applyLeft: Boolean,
    applyTop: Boolean,
    applyRight: Boolean,
    applyBottom: Boolean,
    applyIme: Boolean
) {
    view.doOnApplyWindowInsetsMargin { v, insets, padding ->
        val left = if (applyLeft) insets.getInsets(WindowInsetsCompat.Type.systemBars()).left else 0
        val top = if (applyTop) insets.getInsets(WindowInsetsCompat.Type.systemBars()).top else 0
        val right =
            if (applyRight) insets.getInsets(WindowInsetsCompat.Type.systemBars()).right else 0
        val bottom =
            if (applyBottom) insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom else 0
        val ime = if (applyIme) insets.getInsets(WindowInsetsCompat.Type.ime()).bottom else 0

        v.marginRight
        v.setMargins(
            padding.left + left,
            padding.top + top,
            padding.right + right,
            padding.bottom + if (ime != 0) ime else bottom
        )
    }
}

fun View.doOnApplyWindowInsetsMargin(f: (View, WindowInsetsCompat, InitialPadding) -> Unit) {
    // Create a snapshot of the view's padding state
    val initialPadding = recordInitialMarginForView(this)
    // Set an actual OnApplyWindowInsetsListener which proxies to the given
    // lambda, also passing in the original padding state
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        f(v, insets, initialPadding)
        // Always return the insets, so that children can also use them
        insets
    }
    // request some insets
    requestApplyInsetsWhenAttached()
}

fun View.requestApplyInsetsWhenAttached() {
    if (isAttachedToWindow) {
        // We're already attached, just request as normal
        requestApplyInsets()
    } else {
        // We're not attached to the hierarchy, add a listener to
        // request when we are
        addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                v.removeOnAttachStateChangeListener(this)
                v.requestApplyInsets()
            }

            override fun onViewDetachedFromWindow(v: View) {}
        })
    }
}

data class InitialPadding(
    val left: Int, val top: Int,
    val right: Int, val bottom: Int
)

private fun recordInitialPaddingForView(view: View) = InitialPadding(
    view.paddingLeft, view.paddingTop, view.paddingRight, view.paddingBottom
)

private fun recordInitialMarginForView(view: View) = InitialPadding(
    view.marginLeft, view.marginTop, view.marginRight, view.marginBottom
)

fun View.setMargins(l: Int, t: Int, r: Int, b: Int) {
    if (layoutParams is MarginLayoutParams) {
        val p = layoutParams as MarginLayoutParams
        p.setMargins(l, t, r, b)
        requestLayout()
    }
}