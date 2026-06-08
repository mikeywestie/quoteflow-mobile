package com.mikeywestie.quoteflow.util

import java.text.NumberFormat
import java.util.Locale

fun Double.toRand(): String {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
    return format.format(this)
}
