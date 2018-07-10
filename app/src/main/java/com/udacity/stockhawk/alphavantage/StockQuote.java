package com.udacity.stockhawk.alphavantage;

import java.math.BigDecimal;

public class StockQuote {

    private String symbol;
    private BigDecimal price;
    private BigDecimal lastDayClose;

    public StockQuote(String symbol) {
        this.symbol = symbol;
    }

    public StockQuote(String symbol, BigDecimal price, BigDecimal lastDayClose) {
        this.symbol = symbol;
        this.price = price;
        this.lastDayClose = lastDayClose;
    }

    public BigDecimal getPrice() {
        return price;
    }
    public BigDecimal getChange() {
        return price.subtract(lastDayClose);
    }
    public BigDecimal getChangeInPercent() {
        BigDecimal changeFraction = getChange().divide(lastDayClose, 6, BigDecimal.ROUND_HALF_UP);
        return changeFraction.multiply(new BigDecimal(100.00));
    }
}
