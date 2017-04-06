package com.udacity.stockhawk.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.Utils;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.ui.MainActivity;

/**
 * Created by Jacek on 3/18/2017.
 * IntentService which handles updating My Stock widgets with the latest data
 */

public class MyStockWidgetIntentService extends IntentService {

    public MyStockWidgetIntentService() {
        super("MyStockWidgetIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        // Retrieve all of the MyStock widget ids: these are the widgets we need to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                MyStockWidgetProvider.class));

        // Get data from the ContentProvider
        Cursor cursor = getContentResolver().query(
                Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null,
                null,
                Contract.Quote.COLUMN_SYMBOL);

        if (cursor == null) {
            return;
        }
        if (!cursor.moveToFirst()) {
            cursor.close();
            return;
        }

        // Extract data from the Cursor

        String symbol = cursor.getString(Contract.Quote.POSITION_SYMBOL);
        float price = cursor.getFloat(Contract.Quote.POSITION_PRICE);

        float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
        float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

        int status = PrefUtils.getServerStatus(getApplicationContext());

        String change = Utils.dollarFormatWithPlus.format(rawAbsoluteChange);
        String percentage = Utils.percentageFormat.format(percentageChange / 100);

        cursor.close();

        // Perform this loop procedure for each widget
        for (int appWidgetId : appWidgetIds) {
            int layoutId = R.layout.widget_my_stock;

            RemoteViews views = new RemoteViews(getPackageName(), layoutId);

            // Add the data to the RemoteViews
            views.setTextViewText(R.id.widget_symbol,symbol);
            views.setTextViewText(R.id.widget_price, Utils.dollarFormat.format(price));

            if (status != QuoteSyncJob.STATUS_SERVER_OK) {

                views.setInt(R.id.widget_change,"setBackgroundResource",R.drawable.percent_change_pill_grey);

            } else {

                if (rawAbsoluteChange > 0) {
                    views.setInt(R.id.widget_change,"setBackgroundResource",R.drawable.percent_change_pill_green);
                } else {
                    views.setInt(R.id.widget_change,"setBackgroundResource",R.drawable.percent_change_pill_red);
                }
            }

            if (PrefUtils.getDisplayMode(getApplicationContext())
                    .equals(getApplicationContext().getString(R.string.pref_display_mode_absolute_key))) {
                views.setTextViewText(R.id.widget_change,change);
            } else {
                views.setTextViewText(R.id.widget_change,percentage);
            }

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);

        }

    }

}
