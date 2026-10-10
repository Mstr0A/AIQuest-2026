package com.a0.daleelak

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.a0.daleelak.app.DaleelakApp
import com.a0.daleelak.app.DaleelakViewModel
import com.a0.daleelak.data.LocalOperationStore
import com.a0.daleelak.ui.theme.DaleelakTheme
import com.a0.daleelak.ui.UiPreferencesProvider

class MainActivity : ComponentActivity() {
    private lateinit var model: DaleelakViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        model = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                require(modelClass.isAssignableFrom(DaleelakViewModel::class.java))
                @Suppress("UNCHECKED_CAST")
                return DaleelakViewModel(LocalOperationStore(applicationContext), voiceContext = applicationContext) as T
            }
        })[DaleelakViewModel::class.java]
        setContent {
            UiPreferencesProvider {
                DaleelakTheme {
                    DaleelakApp(model)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        model.onVoiceForegroundChanged(true)
    }

    override fun onStop() {
        model.onVoiceForegroundChanged(false)
        super.onStop()
    }
}
