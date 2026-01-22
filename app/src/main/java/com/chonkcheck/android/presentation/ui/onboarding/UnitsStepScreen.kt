package com.chonkcheck.android.presentation.ui.onboarding

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.chonkcheck.android.domain.model.HeightUnit
import com.chonkcheck.android.domain.model.WeightUnit
import com.chonkcheck.android.presentation.ui.components.ChonkButton
import com.chonkcheck.android.ui.theme.ChonkCheckTheme

@Composable
fun UnitsStepScreen(
    weightUnit: WeightUnit,
    heightUnit: HeightUnit,
    onWeightUnitChange: (WeightUnit) -> Unit,
    onHeightUnitChange: (HeightUnit) -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Welcome! Let's get you sorted.",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Quick setup—less than a minute. Promise.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "Weight",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UnitOptionCard(
                title = "Kilograms",
                subtitle = "kg",
                isSelected = weightUnit == WeightUnit.KG,
                onClick = { onWeightUnitChange(WeightUnit.KG) },
                modifier = Modifier.weight(1f)
            )
            UnitOptionCard(
                title = "Pounds",
                subtitle = "lb",
                isSelected = weightUnit == WeightUnit.LB,
                onClick = { onWeightUnitChange(WeightUnit.LB) },
                modifier = Modifier.weight(1f)
            )
            UnitOptionCard(
                title = "Stones",
                subtitle = "st/lb",
                isSelected = weightUnit == WeightUnit.ST,
                onClick = { onWeightUnitChange(WeightUnit.ST) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Height",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            UnitOptionCard(
                title = "Centimeters",
                subtitle = "cm",
                isSelected = heightUnit == HeightUnit.CM,
                onClick = { onHeightUnitChange(HeightUnit.CM) },
                modifier = Modifier.weight(1f)
            )
            UnitOptionCard(
                title = "Feet & Inches",
                subtitle = "ft/in",
                isSelected = heightUnit == HeightUnit.FT,
                onClick = { onHeightUnitChange(HeightUnit.FT) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        ChonkButton(
            text = "Start \u2192",
            onClick = onContinue
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun UnitOptionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                textAlign = TextAlign.Center,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun UnitsStepScreenPreview() {
    ChonkCheckTheme {
        UnitsStepScreen(
            weightUnit = WeightUnit.KG,
            heightUnit = HeightUnit.CM,
            onWeightUnitChange = {},
            onHeightUnitChange = {},
            onContinue = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UnitsStepScreenStonesPreview() {
    ChonkCheckTheme {
        UnitsStepScreen(
            weightUnit = WeightUnit.ST,
            heightUnit = HeightUnit.FT,
            onWeightUnitChange = {},
            onHeightUnitChange = {},
            onContinue = {}
        )
    }
}
