package com.udacity.stockhawk.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.Utils;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Jacek on 3/21/2017.
 */

@SuppressWarnings("deprecation")
public class DetailsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = DetailsActivity.class.getSimpleName();

    private static final int STOCK_LOADER = 0;

    private TextView mSymbolTextView;
    private TextView mPriceTextView;
    private TextView mChangeTextView;
    private TextView mNameTextView;
    private TextView mRangeTextView;

    private Uri mUri;

    // MPChartAndroid custom formatting class
    class MyValueFormatter implements IValueFormatter {

        private final DecimalFormat mFormat;

        public MyValueFormatter() {
            mFormat = new DecimalFormat("###,###,##0.00");
        }

        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
            // write your logic here
            return "$" + mFormat.format(value);
        }
    }

    // MPChartAndroid custom formatting class
    public class MyXAxisValueFormatter implements IAxisValueFormatter {

        private final String[] mValues;

        public MyXAxisValueFormatter(String[] values) {
            this.mValues = values;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            // "value" represents the position of the label on the axis (x or y)
            //return mValues[(int) value];
            Date date = new Date(Float.valueOf(value).longValue());
            DateFormat dateFormatter;

            dateFormatter = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());

            return dateFormatter.format(date);

        }

        /** this is only needed if numbers are returned, else return 0 */

        public int getDecimalDigits() { return 0; }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        // Get Uri from the bundle
        super.onCreate(savedInstanceState);
        mUri = getIntent().getData();

        //Set Content View
        setContentView(R.layout.activity_details);
        mSymbolTextView = (TextView) findViewById(R.id.details_symbol);
        mPriceTextView = (TextView) findViewById(R.id.details_price);
        mChangeTextView = (TextView) findViewById(R.id.details_change);
        mNameTextView = (TextView) findViewById(R.id.details_name);
        mRangeTextView = (TextView) findViewById(R.id.details_range);

        // Initialize loader
        getSupportLoaderManager().initLoader(STOCK_LOADER, null, this);

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this,
                mUri,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                null, null, Contract.Quote.COLUMN_SYMBOL);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {


        try {
            if (cursor.moveToFirst()) {

                float rawAbsoluteChange = cursor.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
                float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

                mNameTextView.setText(cursor.getString(Contract.Quote.POSITION_NAME));
                //mSymbolTextView.setText(cursor.getString(Contract.Quote.POSITION_SYMBOL));
                mPriceTextView.setText(Utils.dollarFormat.format(cursor.getFloat(Contract.Quote.POSITION_PRICE)));

                String symbol = cursor.getString(Contract.Quote.POSITION_SYMBOL);
                mSymbolTextView.setText(getResources().getString(R.string.symbol_string,symbol));


                if (PrefUtils.getDisplayMode(getApplicationContext())
                        .equals(getApplicationContext().getString(R.string.pref_display_mode_absolute_key))) {
                    mChangeTextView.setText(Utils.dollarFormatWithPlus.format(rawAbsoluteChange));
                } else {
                    mChangeTextView.setText(Utils.percentageFormat.format(percentageChange/100));
                }

                if (rawAbsoluteChange > 0) {
                    mChangeTextView.setBackgroundResource(R.drawable.percent_change_pill_green);
                } else {
                    mChangeTextView.setBackgroundResource(R.drawable.percent_change_pill_red);
                }

                setPriceHistoryGraph(cursor);

            }
            //setPriceHistoryGraph(cursor);

        } catch  (Exception e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();

        }

    }

    private void setPriceHistoryGraph(Cursor cursor) {
        // Dataset entries
        ArrayList<Entry> entries = new ArrayList<>();
        //Strings for X axis
        ArrayList<String> dates = new ArrayList<>();

        // History data:
        String[] historyArray = cursor.getString(Contract.Quote.POSITION_HISTORY).split("\n");
        float largest=0;
        float smallest=0;
        //Read the array from last to first
        for(int i=0; i < historyArray.length;i++) {
            String historyEntry = historyArray[(historyArray.length - 1) - i];
            String[] history = historyEntry.split(",");

            dates.add(history[0]);
            entries.add(new Entry(Float.parseFloat(history[0]), Float.parseFloat(history[1])));

            if (i == 0) {
                largest = Float.parseFloat(history[1]);
                smallest = Float.parseFloat(history[1]);
            } else if((Float.parseFloat(history[1]) > largest)) {
                largest = Float.parseFloat(history[1]);
            } else  if((Float.parseFloat(history[1]) < smallest)){
                smallest = Float.parseFloat(history[1]);
            }

        }

        // Create graph dataset:
        LineDataSet dataset = new LineDataSet(entries,getString(R.string.dataset_label));

        // Format dataset
        dataset.setValueFormatter(new MyValueFormatter());
        dataset.setColor(getResources().getColor(R.color.colorAccent));
        dataset.setValueTextColor(getResources().getColor(R.color.colorWhite));
        dataset.setDrawCircles(false);
        dataset.setDrawFilled(true);
        dataset.setFillColor(getResources().getColor(R.color.colorWhite));

        // Create chart:
        final LineChart chart = (LineChart) findViewById(R.id.details_chart);

        // Create and format X axis:
        XAxis xAxis = chart.getXAxis();
        xAxis.setTextColor(getResources().getColor(R.color.colorWhite));

        String [] dateArray = dates.toArray(new String[dates.size()]);
        xAxis.setValueFormatter(new MyXAxisValueFormatter(dateArray));

        // Create and format Y axis
        YAxis lAxis = chart.getAxisLeft();
        YAxis rAxis = chart.getAxisRight();
        lAxis.setTextColor(getResources().getColor(R.color.colorWhite));
        rAxis.setTextColor(getResources().getColor(R.color.colorWhite));

        // Format graph legend
        Legend legend = chart.getLegend();
        legend.setTextColor(getResources().getColor(R.color.colorWhite));

        // Format graph description
        Description description = new Description();
        //description.setText(getString(R.string.chart_description));
        DecimalFormat df = new DecimalFormat("###,###,##0.00");
        description.setText(getResources().getString(R.string.range_string,df.format(smallest),df.format(largest)));
        description.setTextColor(getResources().getColor(R.color.colorWhite));
        description.setTextSize(16f);

        // Set onTouch listener:
        chart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                Entry e = chart.getEntryByTouchPoint(motionEvent.getX(),motionEvent.getY());

                float x = e.getX();
                float y = e.getY();

                Date date = new Date(Float.valueOf(x).longValue());
                DateFormat dateFormatter;
                dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault());

                DecimalFormat decFormat = new DecimalFormat("###,###,##0.00");
                //decFormat.format(y);

                int action = motionEvent.getActionMasked();

                if(action == MotionEvent.ACTION_UP)

                    Toast.makeText(getApplicationContext(),"" + dateFormatter.format(date) +" $" +decFormat.format(y),Toast.LENGTH_LONG).show();

                return false;
            }
        });


        // Display data in the chart:
        LineData data = new LineData(dataset);
        chart.setDescription(description);
        //chart.setBackgroundColor(getResources().getColor(R.color.colorWhite));
        chart.setData(data);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


}
