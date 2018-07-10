package com.udacity.stockhawk.alphavantage;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * AlphaVantage retrieves stock quotes including historical data.
 * <p>
 *
 * @author Jack Blaszkowski
 *
 */

public class AlphaVantage {

    private static final String LOG_TAG = "AlphaVantage";


    /**
     * Sends a quotes request to AlphaVantage API.
     * This request returns a Map object that links the symbols to
     * their respective Stock objects. Each Stock object has
     * StockQuote and HistoricalQuote fields.
     *
     * @param  stockSymbols  the symbols of the stocks for which you want to retrieve information
     * @return               a Map that links the symbols to their respective Stock objects
     * @throws IOException  when there's a connection problem
     * 
     */
    public static Map<String, Stock> get(String[] stockSymbols, Context context)
        throws IOException {

        Map<String, Stock> stocksMap = new HashMap<String, Stock>();

        for (String symbol: stockSymbols) {
            Log.d(LOG_TAG, "In the get():: symbol=" + symbol);

            Stock stock = new Stock(symbol, context);

            // Add Stock object to the Map
            stocksMap.put(symbol, stock);
            
        }

        return stocksMap;
    }

}
