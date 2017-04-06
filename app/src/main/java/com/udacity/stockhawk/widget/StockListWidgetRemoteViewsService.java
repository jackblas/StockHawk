package com.udacity.stockhawk.widget;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.Utils;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;
import com.udacity.stockhawk.sync.QuoteSyncJob;

/**
 * Created by Jacek on 3/19/2017.
 */

public class StockListWidgetRemoteViewsService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {

            private Cursor cursor = null;

            @Override
            public void onCreate() {

            }

            @Override
            public void onDataSetChanged() {


                if (cursor != null) {
                    cursor.close();
                }
                // This method is called by the app hosting the widget (e.g., the launcher)
                // However, our ContentProvider is not exported so it doesn't have access to the
                // data. Therefore we need to clear (and finally restore) the calling identity so
                // that calls use our process and permission
                final long identityToken = Binder.clearCallingIdentity();

                // Get data from the ContentProvider
                cursor = getContentResolver().query(
                    Contract.Quote.URI,
                    Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                    null,
                    null,
                    Contract.Quote.COLUMN_SYMBOL);

                Binder.restoreCallingIdentity(identityToken);

            }

            @Override
            public void onDestroy() {

                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }

            }

            @Override
            public int getCount() {
                return cursor == null ? 0 : cursor.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {

                if (position == AdapterView.INVALID_POSITION ||
                        cursor == null || !cursor.moveToPosition(position)) {
                    return null;
                }
                RemoteViews views = new RemoteViews(getPackageName(),
                        R.layout.widget_stock_list_item);

                // Extract data from the Cursor
                String symbol = cursor.getString(Contract.Quote.POSITION_SYMBOL);
                float price = cursor.getFloat(Contract.Quote.POSITION_PRICE);

                float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
                float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

                int status = PrefUtils.getServerStatus(getApplicationContext());

                String change = Utils.dollarFormatWithPlus.format(rawAbsoluteChange);
                String percentage = Utils.percentageFormat.format(percentageChange / 100);

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

                // Set a fill-intent, which will be used to fill in the pending intent template
                // that is set on the collection view in WidgetProvider.
                Intent fillInIntent = new Intent();
                views.setOnClickFillInIntent(R.id.widget_list_item, fillInIntent);

                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                //JB ?
                return new RemoteViews(getPackageName(), R.layout.widget_stock_list_item);
                //return null;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {

                if (cursor.moveToPosition(position))
                    return cursor.getLong(Contract.Quote.POSITION_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {

                return true;
                //return false;
            }
        };
    }
}
