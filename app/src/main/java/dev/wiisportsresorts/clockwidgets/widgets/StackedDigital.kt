package dev.wiisportsresorts.clockwidgets.widgets

import android.appwidget.AppWidgetManager
import android.content.Context
import android.text.format.DateFormat
import android.widget.RemoteViews
import dev.wiisportsresorts.clockwidgets.ClockWidget
import dev.wiisportsresorts.clockwidgets.R
import java.util.*

class StackedDigital : ClockWidget() {
    override fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        cal: Calendar
    ) {
        log("updateWidget")

        val use24Hour = DateFormat.is24HourFormat(context)

        // TODO: locale-aware date formatting
        val dateString =
            String.format(
                "%s, %s %d",
                cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()),
                cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()),
                cal.get(Calendar.DAY_OF_MONTH)
            )

        // supports 12hr and 24hr time formats
        val hourString = when (val hour = cal.get(Calendar.HOUR_OF_DAY)) {
            0 -> if (use24Hour) "00" else "12"
            12 -> "12"
            else -> String.format("%02d", if (use24Hour) hour else hour % 12)
        }

        val minuteString = String.format("%02d", cal.get(Calendar.MINUTE))

        val views = RemoteViews(context.packageName, R.layout.stacked_digital).apply {
            setTextViewText(R.id.dateText, dateString)
            setTextViewText(R.id.hourText, hourString)
            setTextViewText(R.id.minuteText, minuteString)
            setOnClickPendingIntent(R.id.widgetRoot, makeShowAlarmsIntent(context))
        }
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}



