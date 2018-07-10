package com.udacity.stockhawk.alphavantage;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.udacity.stockhawk.BuildConfig;
import com.udacity.stockhawk.remote.RemoteConfig;
import com.udacity.stockhawk.remote.RemoteEndpoint;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

/*


 */

public class Stock {

    private static final String LOG_TAG = "Stock";
    private String symbol;
    private String name;
    private StockQuote quote;
    private List<HistoricalQuote> history;

    //final int YEARS_OF_HISTORY = 1;
    final int WEEKS_OF_HISTORY = 52;


    public Stock(String symbol, Context context)
            throws IOException {
        this.symbol=symbol;
        // Since AV data does't include stock's name,
        // set name to stock symbol
        name = symbol;

        quote = loadQuote(context);
        history = loadHistory(context);

    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public StockQuote getQuote() {
        return quote;
    }

    public List<HistoricalQuote> getHistory(Calendar from, Calendar to, Interval interval) {
        return history;
    }


    private StockQuote  loadQuote(Context context)
            throws IOException {
        Log.v(LOG_TAG, "In the loadQuote():: symbol=" + symbol);

        StockQuote stockQuote=null;

        try {

            String dailyPricesJsonStr;
            if(BuildConfig.AV_API_KEY.equals("demo")) {
                dailyPricesJsonStr = getJsonStringFromAssets(RemoteConfig.DAILY_QUERY_STRING, context);
            } else {
                dailyPricesJsonStr = getJsonStringFromAPI(RemoteConfig.DAILY_QUERY_STRING);
            }

            Log.v(LOG_TAG, "In the loadQuote():: dailyPricesJsonStr=" + dailyPricesJsonStr);

            // quotePrices[0] - current price
            // quotePrices[1] - last day close
            String[] quotePrices = getQuotePrices(dailyPricesJsonStr);

            Log.d(LOG_TAG, "In the getQuote():: current price=" + quotePrices[0]);
            Log.d(LOG_TAG, "In the getQuote():: last day close=" + quotePrices[1]);

            stockQuote = new StockQuote(symbol,
                    new BigDecimal(quotePrices[0]),
                    new BigDecimal(quotePrices[1]));

        } catch (JSONException e) {
            Log.e(LOG_TAG,"ERROR parsing Json for " + symbol + " DAILY_QUERY: ",e);
            throw new IOException(e);
        }

        return stockQuote;

    }


    private List<HistoricalQuote> loadHistory(Context context)
            throws IOException {

        List<HistoricalQuote> stockHistory = new ArrayList<>();

        String weeklyPricesJsonStr;
        //String weeklyPricesJsonStr = getJsonStringFromAssets(RemoteConfig.WEEKLY_QUERY_STRING, context);
        if(BuildConfig.AV_API_KEY.equals("demo")) {
            weeklyPricesJsonStr = getJsonStringFromAssets(RemoteConfig.WEEKLY_QUERY_STRING, context);
        } else {
            weeklyPricesJsonStr = getJsonStringFromAPI(RemoteConfig.WEEKLY_QUERY_STRING);
        }

        Log.v(LOG_TAG, "In the loadHistory():: weeklyPricesJsonStr=" + weeklyPricesJsonStr);

        try {
            JSONObject jsonObject = new JSONObject(weeklyPricesJsonStr);

            JSONObject weeklySeriesObj = jsonObject.getJSONObject("Weekly Time Series");
            Iterator<String> keysItr = weeklySeriesObj.keys();

            int i = 0;
            while (keysItr.hasNext() && i < WEEKS_OF_HISTORY) {
                String key = keysItr.next();

                JSONObject dayObj = (JSONObject) weeklySeriesObj.get(key);
                String close = dayObj.getString("4. close");

                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Calendar date = Calendar.getInstance();

                date.setTime(dateFormat.parse(key));

                HistoricalQuote historicalQuote = new HistoricalQuote(date, new BigDecimal(close));
                stockHistory.add(historicalQuote);
                i++;

            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return stockHistory;
    }

    private String getJsonStringFromAssets(String function, Context context){

        String json = null;
        String fileName = symbol + "_" + function + ".json";
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return json;
    }

    private String getJsonStringFromAPI(String function)
            throws IOException {

        String jsonString = null;

        Uri builtUri;

        builtUri = Uri.parse(RemoteConfig.BASE_URL_STRING).buildUpon()
                .appendQueryParameter(RemoteConfig.FUNCTION_PARAM, function)
                .appendQueryParameter(RemoteConfig.SYMBOL_PARAM, symbol)
                .appendQueryParameter(RemoteConfig.APIKEY_PARAM, BuildConfig.AV_API_KEY)
                .build();

        URL url = new URL(builtUri.toString());
        Log.d(LOG_TAG, "In the getJsonStringFromAPI():: url=" + url.toString());

        jsonString = RemoteEndpoint.fetchPlainText(url);
        return jsonString;
    }


    private String[] getQuotePrices(String jsonString)
            throws JSONException {


        String[] prices = new String[2];

        JSONObject jsonObject = new JSONObject(jsonString);
        JSONObject dailySeriesObj = jsonObject.getJSONObject("Time Series (Daily)");

        Iterator<String> keysItr = dailySeriesObj.keys();

        int i = 0;
        while (keysItr.hasNext() && i <2) {
            String key = keysItr.next();

            JSONObject dayObj = (JSONObject) dailySeriesObj.get(key);
            String close = dayObj.getString("4. close");

            prices[i]=close;
            i++;

        }

        return prices;
    }

}
