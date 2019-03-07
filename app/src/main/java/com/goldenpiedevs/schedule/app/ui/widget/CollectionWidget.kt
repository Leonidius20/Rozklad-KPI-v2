package com.goldenpiedevs.schedule.app.ui.widget

import android.annotation.TargetApi
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.RemoteViews

import com.goldenpiedevs.schedule.app.R

class CollectionWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {

        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                                     appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.collection_widget)
            //        views.setTextViewText(R.id.appwidget_text, widgetText);

            // Set up the collection
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                setRemoteAdapter(context, views)
            else {
                setRemoteAdapterV11(context, views)
            }
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        /**
         * Sets the remote adapter used to fill in the list items
         *
         * @param views RemoteViews to set the RemoteAdapter
         */
        @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
        private fun setRemoteAdapter(context: Context, views: RemoteViews) {
            views.setRemoteAdapter(R.id.widget_list,
                    Intent(context, WidgetService::class.java))
        }

        /**
         * Sets the remote adapter used to fill in the list items
         *
         * @param views RemoteViews to set the RemoteAdapter
         */
        private fun setRemoteAdapterV11(context: Context, views: RemoteViews) {
            views.setRemoteAdapter(0, R.id.widget_list,
                    Intent(context, WidgetService::class.java))
        }
    }
}