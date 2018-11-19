package com.trixiesoft.mybooks.utils

import androidx.annotation.NonNull
import androidx.fragment.app.Fragment

@Suppress("UNCHECKED_CAST")
fun <T> getParent(@NonNull fragment: Fragment, @NonNull parentClass: Class<T>): T? {
    val parentFragment = fragment.parentFragment
    if (parentClass.isInstance(parentFragment)) {
        //Checked by runtime methods
        return parentFragment as T
    } else if (parentClass.isInstance(fragment.activity)) {
        //Checked by runtime methods
        return fragment.activity as T
    }
    return null
}

fun Fragment.getParent(): Any? {
    val parentFragment = parentFragment
    if (parentFragment != null) {
        //Checked by runtime methods
        return parentFragment
    }
    return activity
}
