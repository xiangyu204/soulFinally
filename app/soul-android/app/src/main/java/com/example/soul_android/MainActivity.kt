package com.example.soul_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.soul_android.data.LanguageState
import com.example.soul_android.data.LocalMessageStore
import com.example.soul_android.data.ThemeState
import com.example.soul_android.ui.components.SoulHazeHost
import com.example.soul_android.ui.theme.SOUL_AndroidTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        LanguageState.init(this)
        ThemeState.init(this)
        LocalMessageStore.init(this)

        setContent {
            SOUL_AndroidTheme {
                SoulHazeHost {
                    SOULApp()
                }
            }
        }
    }
}
