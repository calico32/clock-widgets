@file:OptIn(ExperimentalMaterial3Api::class)

package dev.wiisportsresorts.clockwidgets

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val widgetManager = AppWidgetManager.getInstance(this)
        val widgetProviders = widgetManager.getInstalledProvidersForPackage(packageName, null)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(),
            ) {
                Scaffold {
                    LazyColumn(
                        contentPadding = it,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (!widgetManager.isRequestPinAppWidgetSupported) {
                            item { PinUnavailableBanner() }
                        }

                        items(widgetProviders) { provider ->
                            WidgetInfoCard(provider)
                        }
                    }
                }
            }
        }
    }
}


/**
 * Extension method to request the launcher to pin the given AppWidgetProviderInfo
 *
 * Note: the optional success callback to retrieve if the widget was placed might be unreliable
 * depending on the default launcher implementation. Also, it does not callback if user cancels the
 * request.
 */
private fun AppWidgetProviderInfo.pin(context: Context) {
    val successCallback = PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, AppWidgetPinnedReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    AppWidgetManager.getInstance(context).requestPinAppWidget(provider, null, successCallback)
}

@Composable
private fun PinUnavailableBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.error)
            .padding(16.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Error,
            contentDescription = "warning",
            tint = MaterialTheme.colorScheme.onError,
            modifier = Modifier.padding(end = 12.dp)
        )
        Text(
            text = "Pinning is not supported in the default launcher!",
            modifier = Modifier
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.onError
        )
    }
}

@Composable
private fun AppInfoText() {
    Text(
        text = stringResource(id = R.string.app_name),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
}


/**
 * Display the app widget info from the provider.
 *
 * This class contains all the info we provide via the XML meta-data for each provider.
 */
@SuppressLint("ResourceType")
@Composable
private fun WidgetInfoCard(providerInfo: AppWidgetProviderInfo) {
    val context = LocalContext.current
    val label = providerInfo.loadLabel(context.packageManager)
    val description = providerInfo.loadDescription(context)

    Card(
        modifier = Modifier.padding(16.dp),
        onClick = {
            providerInfo.pin(context)
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "${providerInfo.targetCellWidth} x ${providerInfo.targetCellHeight}",
                style = MaterialTheme.typography.bodyMedium
            )
            if (!description.isNullOrBlank()) {
                Text(
                    text = description.toString(),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            AndroidView(
                modifier = Modifier.padding(top = 12.dp),
                factory = {
                    View.inflate(it, providerInfo.previewLayout, null)
                },
            )
        }
    }
}
