package icu.pboymt.mayer.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import icu.pboymt.mayer.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceStatusCard(
    title: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    ElevatedCard(
        onClick = onClick,
        enabled = true,
        modifier = Modifier
            .wrapContentHeight(unbounded = true)
            .fillMaxWidth(),
        colors = CardDefaults.elevatedCardColors(
            containerColor = run {
                if (enabled) MaterialTheme.colorScheme.secondaryContainer
                else MaterialTheme.colorScheme.errorContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
//            Column(
//                modifier = Modifier
//                    .width(75.dp)
//                    .fillMaxHeight(),
//                verticalArrangement = Arrangement.Center,
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
            // use Material3 icon
            // use alert icon if not enabled
            Icon(
                imageVector = if (enabled) Icons.Default.Check else Icons.Default.Warning,
                contentDescription = "Accessibility service status",
//                    tint = if (enabled) Color.Green else Color.Red,
                modifier = Modifier.height(40.dp)
            )
//            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 24.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = if (enabled) {
                        stringResource(R.string.status_enabled)
                    } else {
                        stringResource(R.string.status_disabled)
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}