package dev.wiisportsresorts.clockwidgets

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.util.Log
import java.util.*

abstract class ClockWidget : AppWidgetProvider() {
    protected fun log(message: String) {
        Log.v(this::class.java.simpleName, message)
    }

    companion object {
        const val CLOCK_TICK = "dev.wiisportsresorts.clockwidgets.CLOCK_TICK"
        const val CLOCK_CLICK = "dev.wiisportsresorts.clockwidgets.CLOCK_CLICK"
    }

    private fun getAlarmManager(context: Context): AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private var nextTick: PendingIntent? = null
    private fun nextTickTime(): Long {
        val currentMinute = System.currentTimeMillis() / 1000L / 60L
        return (currentMinute + 1L) * 60L * 1000L
    }

    private fun makeTickIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            0,
            Intent(CLOCK_TICK).setClass(context, this::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

    // Android Studio incorrectly reporting that SET_EXACT_ALARM is needed,
    // while in fact we are using USE_EXACT_ALARM.
    @SuppressLint("MissingPermission")
    private fun queueNextTick(context: Context): PendingIntent {
        log("queueNextTick ${System.currentTimeMillis()} -> ${nextTickTime()}")
        val manager = getAlarmManager(context)
        if (nextTick != null) {
            manager.cancel(nextTick)
        }
        nextTick = makeTickIntent(context)
        manager.setExact(AlarmManager.RTC, nextTickTime(), nextTick)
        log(nextTick.toString())
        return nextTick!!
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)

        updateAllClocks(context)
        queueNextTick(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)

        if (nextTick != null) {
            getAlarmManager(context).cancel(nextTick)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        updateAllClocks(context)
        queueNextTick(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        val action = intent.action
        if (action == null || action.startsWith("android.appwidget.action.APPWIDGET_")) {
            // handled by this::class
            return
        }

        log("onReceive $action")

        when (action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                updateAllClocks(context)
                queueNextTick(context)
            }
            CLOCK_TICK -> {
                updateAllClocks(context)
                queueNextTick(context)
            }
            CLOCK_CLICK -> {
                val alarmIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
                alarmIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(alarmIntent)
            }
            else -> {
                log("unhandled intent: $action")
            }
        }
    }

    private fun updateAllClocks(context: Context) {
        log("updateAllClocks")
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val thisAppWidget = ComponentName(context.packageName, javaClass.name)
        val ids = appWidgetManager.getAppWidgetIds(thisAppWidget)
        ids.forEach { updateWidget(context, appWidgetManager, it, Calendar.getInstance()) }
    }

    protected fun makeShowAlarmsIntent(context: Context): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            0,
            Intent(CLOCK_CLICK).setClass(context, this::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    abstract fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        cal: Calendar
    )
}




