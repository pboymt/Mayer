buildscript {
    extra.apply {
        set("compose_version", "1.3.3")
        set("defaultMinSdkVersion", 30)
    }
}// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.0.0" apply false
    id("com.android.library") version "8.0.0" apply false
    kotlin("android") version "1.7.0" apply false
}