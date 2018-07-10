package com.udacity.stockhawk.remote;

public class RemoteConfig {

    private static final String LOG_TAG = "RemoteConfig";

    public static final String BASE_URL_STRING = "https://www.alphavantage.co/query";

    // API Parameters
    public static final String FUNCTION_PARAM = "function";
    public static final String SYMBOL_PARAM = "symbol";
    public static final String APIKEY_PARAM = "apikey";

    //API Parameters Values
    public static final String DAILY_QUERY_STRING = "TIME_SERIES_DAILY";
    public static final String WEEKLY_QUERY_STRING = "TIME_SERIES_WEEKLY";


    // https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=MSFT&apikey=demo
    // https://www.alphavantage.co/query?function=TIME_SERIES_WEEKLY&symbol=MSFT&apikey=demo

}
