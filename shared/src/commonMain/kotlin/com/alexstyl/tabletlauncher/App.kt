package com.alexstyl.tabletlauncher

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.composables.ui.theme.ComposablesTheme

@Composable
fun App() {
  ComposablesTheme { MaterialTheme { HomeScreen(modifier = Modifier.fillMaxSize()) } }
}

@Preview
@Composable
fun AppPreview() {
  App()
}
