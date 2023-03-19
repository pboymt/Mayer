package icu.pboymt.mayer.runner

annotation class Step(val templates: Array<String>)

annotation class Setting(val key: String)

annotation class Script(
    val name: String,
    val uuid: String,
    val description: String,
    val author: String
)