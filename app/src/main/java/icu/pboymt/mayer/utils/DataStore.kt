package icu.pboymt.mayer.utils

import android.content.Context
import androidx.annotation.Keep
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore

const val DATASTORE_NAME = "mayer_datastore"

@Keep
data class PrefKey<T>(
    val key: String,
    val default: T,
    val title: String,
    val description: String? = null,
) {
    val prefKey
        get() = when (default) {
            is Boolean -> booleanPreferencesKey(key)
            is Int -> intPreferencesKey(key)
            is Long -> longPreferencesKey(key)
            is Float -> floatPreferencesKey(key)
            is Double -> doublePreferencesKey(key)
            is String -> stringPreferencesKey(key)
            is Set<*> -> stringSetPreferencesKey(key)
            else -> throw IllegalArgumentException("Unsupported type")
        }
}

@Keep
object PrefKeys {
    object Script {
        object All {
            val StartApp = PrefKey(
                key = "mayer.pref.script.all.start_app",
                default = true,
                title = "自动启动应用"
            )
            val StartAppPackageName = PrefKey(
                key = "mayer.pref.script.all.start_app_package_name",
                default = "com.leiting.wf",
                title = "应用包名"
            )
            val StartWfBilibili = PrefKey(
                key = "mayer.pref.script.all.start_wf_bilibili",
                default = false,
                title = "启动 B 服客户端"
            )
        }
    }
}

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = DATASTORE_NAME)

