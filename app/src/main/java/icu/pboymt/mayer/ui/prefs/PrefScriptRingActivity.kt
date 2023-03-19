package icu.pboymt.mayer.ui.prefs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.alorma.compose.settings.storage.datastore.rememberPreferenceDataStoreBooleanSettingState
import com.alorma.compose.settings.storage.datastore.rememberPreferenceDataStoreIntSettingState
import com.alorma.compose.settings.ui.SettingsList
import com.alorma.compose.settings.ui.SettingsListDropdown
import com.alorma.compose.settings.ui.SettingsSlider
import com.alorma.compose.settings.ui.SettingsSwitch
import icu.pboymt.mayer.scripts.RingScript
import icu.pboymt.mayer.ui.component.TabTitle
import icu.pboymt.mayer.ui.theme.MayerTheme
import icu.pboymt.mayer.utils.dataStore

class PrefScriptRingActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MayerTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(topBar = {
                        TabTitle(title = "脚本设置")
                    }) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .padding(innerPadding)
                                .verticalScroll(rememberScrollState())
                        ) {
                            val fightCountState = RingScript.Prefs.FightCount.let {
                                rememberPreferenceDataStoreIntSettingState(
                                    key = it.key,
                                    defaultValue = it.default,
                                    dataStore = dataStore
                                )
                            }
                            val autoContinueState = RingScript.Prefs.AutoContinue.let {
                                rememberPreferenceDataStoreBooleanSettingState(
                                    key = it.key,
                                    defaultValue = it.default,
                                    dataStore = dataStore
                                )
                            }
                            val useStaminaState = RingScript.Prefs.UseStamina.let {
                                rememberPreferenceDataStoreBooleanSettingState(
                                    key = it.key,
                                    defaultValue = it.default,
                                    dataStore = dataStore
                                )
                            }
                            val staminaCountState = RingScript.Prefs.StaminaCount.let {
                                rememberPreferenceDataStoreIntSettingState(
                                    key = it.key,
                                    defaultValue = it.default,
                                    dataStore = dataStore
                                )
                            }
                            val staminaAllowStoneState = RingScript.Prefs.StaminaAllowStone.let {
                                rememberPreferenceDataStoreBooleanSettingState(
                                    key = it.key,
                                    defaultValue = it.default,
                                    dataStore = dataStore
                                )
                            }
                            SettingsListDropdown(
                                state = fightCountState,
                                title = RingScript.Prefs.FightCount.title.let { { Text(it) } },
                                items = List(100) { it.toString() },
                                subtitle = RingScript.Prefs.FightCount.description?.let { { Text(it) } }
                            )
                            SettingsSwitch(
                                state = autoContinueState,
                                title = RingScript.Prefs.AutoContinue.title.let { { Text(it) } },
                                subtitle = RingScript.Prefs.AutoContinue.description?.let { { Text(it) } }
                            )
                            SettingsSwitch(
                                state = useStaminaState,
                                title = RingScript.Prefs.UseStamina.title.let { { Text(it) } },
                                subtitle = RingScript.Prefs.UseStamina.description?.let { { Text(it) } }
                            )
                            SettingsListDropdown(
                                state = staminaCountState,
                                title = RingScript.Prefs.StaminaCount.title.let { { Text(it) } },
                                items = List(100) { it.toString() },
                                subtitle = RingScript.Prefs.StaminaCount.description?.let { { Text(it) } }
                            )
                            SettingsSwitch(
                                state = staminaAllowStoneState,
                                title = RingScript.Prefs.StaminaAllowStone.title.let { { Text(it) } },
                                subtitle = RingScript.Prefs.StaminaAllowStone.description?.let { { Text(it) } }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting3(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview4() {
    MayerTheme {
        Greeting3("Android")
    }
}