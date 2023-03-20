package icu.pboymt.mayer.runner

import androidx.annotation.Keep

@Keep
annotation class Step(val templates: Array<String>)

@Keep
annotation class Setting(val key: String)

@Keep
annotation class Script(
    val name: String,
    val uuid: String,
    val description: String,
    val author: String
)