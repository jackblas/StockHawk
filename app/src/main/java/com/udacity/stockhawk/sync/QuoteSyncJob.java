package com.udacity.stockhawk.sync;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;


public final class QuoteSyncJob {

    private static final int ONE_OFF_ID = 2;
    public static final String ACTION_DATA_UPDATED = "com.udacity.stockhawk.ACTION_DATA_UPDATED";
    private static final int PERIOD = 300000;
    //For testing:
    //private static final int PERIOD = 60000;
    private static final int INITIAL_BACKOFF = 10000;
    private static final int PERIODIC_ID = 1;
    private static final int YEARS_OF_HISTORY = 2;

    public static final int STATUS_STOCK_SYMBOL_VALID = 0;
    public static final int STATUS_STOCK_SYMBOL_INVALID = 1;

    public static final int STATUS_SERVER_OK = 0;
    public static final int STATUS_SERVER_DOWN = 1;
    public static final int STATUS_NO_CONNECTION = 2;

    private QuoteSyncJob() {
    }

    static void getQuotes(Context context) {

        Timber.d("Running sync job");

        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
        from.add(Calendar.YEAR, -YEARS_OF_HISTORY);

        String symbol;
        try {

            Set<String> stockPref = PrefUtils.getStocks(context);
            Set<String> stockCopy = new HashSet<>();
            stockCopy.addAll(stockPref);
            String[] stockArray = stockPref.toArray(new String[stockPref.size()]);

            Timber.d(stockCopy.toString());

            if (stockArray.length == 0) {
                return;
            }

            Map<String, Stock> quotes = YahooFinance.get(stockArray);
            Iterator<String> iterator = stockCopy.iterator();

            Timber.d(quotes.toString());

            ArrayList<ContentValues> quoteCVs = new ArrayList<>();

            PrefUtils.setStockSymbolStatus(context, STATUS_STOCK_SYMBOL_VALID);

            while (iterator.hasNext()) {
                symbol = iterator.next();

                Stock stock = quotes.get(symbol);
                StockQuote quote = stock.getQuote();
                //JB-Issue 05: Handle invalid stock symbol.
                //If stock symbol is invalid, YF returns quote
                //with null values.
                //If priceObj is null, assume invalid stock symbol
                //and remove stock from preferences.
                BigDecimal priceObj = quote.getPrice();
                float price;
                if(priceObj != null) {
                    //Valid stock symbol
                    price = priceObj.floatValue();
                } else {
                    //Invalid stock symbol:
                    PrefUtils.removeStock(context, symbol);
                    PrefUtils.setStockSymbolStatus(context, STATUS_STOCK_SYMBOL_INVALID);
                    continue;
                }
                //float price = quote.getPrice().floatValue();
                float change = quote.getChange().floatValue();
                float percentChange = quote.getChangeInPercent().floatValue();
                String name = stock.getName();
                Timber.d(name);
                ///Log.d("JACK", "name: " + name);

                // WARNING! Don't request historical data for a stock that doesn't exist!
                // The request will hang forever X_x
                List<HistoricalQuote> history = stock.getHistory(from, to, Interval.WEEKLY);

                StringBuilder historyBuilder = new StringBuilder();

                for (HistoricalQuote it : history) {
                    historyBuilder.append(it.getDate().getTimeInMillis());
                    historyBuilder.append(", ");
                    historyBuilder.append(it.getClose());
                    historyBuilder.append("\n");
                    ///Log.d("JACK", "history: " + it.getDate() + ", " + it.getClose());
                }

                ///Log.d("JACK", "history: " + historyBuilder.toString());

                ContentValues quoteCV = new ContentValues();
                quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
                quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
                quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);

                quoteCV.put(Contract.Quote.COLUMN_HISTORY, historyBuilder.toString());
                quoteCV.put(Contract.Quote.COLUMN_NAME, name);

                quoteCVs.add(quoteCV);

            }

            context.getContentResolver()
                    .bulkInsert(
                            Contract.Quote.URI,
                            quoteCVs.toArray(new ContentValues[quoteCVs.size()]));

            // Update widgets
            Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
            context.sendBroadcast(dataUpdatedIntent);

            PrefUtils.setServerStatus(context,STATUS_SERVER_OK);

        } catch (IOException exception) {
            Timber.e(exception, "Error fetching stock quotes");
            if(networkUp(context)) {
                Timber.e("STATUS_SERVER_DOWN");
                PrefUtils.setServerStatus(context,STATUS_SERVER_DOWN);
            } else {
                Timber.e("STATUS_NO_CONNECTION");
                PrefUtils.setServerStatus(context,STATUS_NO_CONNECTION);
            }
        }

    }

    private static void schedulePeriodic(Context context) {
        Timber.d("Scheduling a periodic task");

        JobInfo.Builder builder = new JobInfo.Builder(PERIODIC_ID, new ComponentName(context, QuoteJobService.class));

        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(PERIOD)
                .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        scheduler.schedule(builder.build());
    }


    public static synchronized void initialize(final Context context) {

        schedulePeriodic(context);
        // JB No need to call here - called in onRefresh()! - make sure initilialize is called first
        // before first syncImmediately
        //syncImmediately(context);

    }

    public static synchronized void syncImmediately(Context context) {

        if(networkUp(context)){

            Intent nowIntent = new Intent(context, QuoteIntentService.class);
            context.startService(nowIntent);
            //Set status: connected
            //Don not set here.If connected, status should be OK or server_down
            //PrefUtils.setServerStatus(context,STATUS_CONNECTED);

        } else {
            //Set status: no connection
            PrefUtils.setServerStatus(context,STATUS_NO_CONNECTION);
        }

        /*
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            Intent nowIntent = new Intent(context, QuoteIntentService.class);
            context.startService(nowIntent);
        } else {


            JobInfo.Builder builder = new JobInfo.Builder(ONE_OFF_ID, new ComponentName(context, QuoteJobService.class));


            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

            scheduler.schedule(builder.build());


        }
        */
    }

    private static boolean networkUp(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }
}
