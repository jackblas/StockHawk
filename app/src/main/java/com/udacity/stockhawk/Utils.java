package com.udacity.stockhawk;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

/**
 * Created by Jacek on 3/18/2017.
 */

public class Utils {

    public static final DecimalFormat dollarFormat;
    public static final DecimalFormat dollarFormatWithPlus;
    public static final DecimalFormat percentageFormat;

    /* *******************
     JB-Issue 02: Localization
     Localize currency and numbers
     */

    static {

        Locale localeDefault = Locale.getDefault();
        Currency usDollar = Currency.getInstance(Locale.US);

        //Get dollar symbol in default locale:
        String localSymbol = usDollar.getSymbol(localeDefault);
        //Format currency for default locale (how it is displayed)
        dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(localeDefault);
        //Ser currency to USD (what is displayed)
        dollarFormat.setCurrency(usDollar);

        dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(localeDefault);
        dollarFormatWithPlus.setCurrency(usDollar);

        //Add other Locales where currency symbol is displayed in front of the number
        if (localeDefault.equals(Locale.US) || localeDefault.equals(Locale.CHINA)) {
            dollarFormatWithPlus.setPositivePrefix("+" + localSymbol);
        } else {
            dollarFormatWithPlus.setPositivePrefix("+");
        }


        percentageFormat = (DecimalFormat) NumberFormat.getPercentInstance(localeDefault);
        percentageFormat.setMaximumFractionDigits(2);
        percentageFormat.setMinimumFractionDigits(2);
        percentageFormat.setPositivePrefix("+");

    }


}
