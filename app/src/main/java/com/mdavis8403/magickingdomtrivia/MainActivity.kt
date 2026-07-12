package com.mdavis8403.magickingdomtrivia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mdavis8403.magickingdomtrivia.ui.TriviaApp
import com.mdavis8403.magickingdomtrivia.ui.TriviaViewModel
import com.mdavis8403.magickingdomtrivia.ui.theme.MagicKingdomTriviaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MagicKingdomTriviaTheme {
                TriviaApp(viewModel = viewModel<TriviaViewModel>())
            }
        }
    }
}

