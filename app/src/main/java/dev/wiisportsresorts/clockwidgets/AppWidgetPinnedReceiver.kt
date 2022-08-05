package dev.wiisportsresorts.clockwidgets

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AppWidgetPinnedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Toast.makeText(
            context,
            "Widget pinned. Go to home screen.",
            Toast.LENGTH_SHORT
        ).show()
    }
}