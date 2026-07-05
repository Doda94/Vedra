package hr.doda.vedra

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import hr.doda.vedra.ui.theme.VedraTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Preview
fun App() {
    VedraTheme {
        Scaffold(
            topBar = { TopAppBar(title = { Text("Vedra") }) },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Zagreb", style = MaterialTheme.typography.displayMedium)
                Text("23°C", style = MaterialTheme.typography.headlineLarge)
                Text("Feels like 21°C", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Partly cloudy with light showers expected this afternoon.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text("Updated just now", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}