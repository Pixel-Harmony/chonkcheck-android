package com.chonkcheck.android.presentation.ui.settings.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chonkcheck.android.ui.theme.ChonkCheckTheme

@Composable
fun PrivacyDataSection(
    onExportData: () -> Unit,
    onPrivacyPolicy: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = onExportData,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Export My Data")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onPrivacyPolicy,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Privacy Policy")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Download all your data in JSON format or view our privacy policy.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PrivacyDataSectionPreview() {
    ChonkCheckTheme {
        Surface {
            PrivacyDataSection(
                onExportData = {},
                onPrivacyPolicy = {}
            )
        }
    }
}
