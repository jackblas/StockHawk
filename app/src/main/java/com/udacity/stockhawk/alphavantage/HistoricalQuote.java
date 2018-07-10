package com.udacity.stockhawk.alphavantage;

import java.math.BigDecimal;
import java.util.Calendar;

public class HistoricalQuote {
    private Calendar date;
    private BigDecimal closePrice;

    public HistoricalQuote(Calendar date, BigDecimal closePrice){
        this.closePrice = closePrice;
        this.date = date;
    }

    public Calendar getDate() {
        return date;
    }
    public BigDecimal getClose() {
        return closePrice;
    }
}
