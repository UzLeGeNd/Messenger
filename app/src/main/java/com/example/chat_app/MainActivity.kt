package com.example.chat_app

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.navigation.compose.rememberNavController
import com.example.chat_app.Navigation.AppNavigation
import com.example.chat_app.ui.theme.Chat_AppTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Chat_AppTheme {
                val navController = rememberNavController()
                AppNavigation(navController = navController)
            }
        }
    }
}