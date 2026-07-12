package com.mdavis8403.magickingdomtrivia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mdavis8403.magickingdomtrivia.ui.TriviaApp
import com.mdavis8403.magickingdomtrivia.ui.TriviaViewModel
import com.mdavis8403.magickingdomtrivia.ui.theme.MagicKingdomTriviaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        setContent {
            MagicKingdomTriviaTheme {
                TriviaApp(viewModel = viewModel<TriviaViewModel>())
            }
        }
    }
}
