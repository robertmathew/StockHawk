package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.model.Point;
import com.db.chart.view.AxisController;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DetailStockActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "DetailStockActivity";
    private Cursor quoteData;
    private String quoteID, quoteName, quoteSymbol;
    private static final int CURSOR_LOADER_ID = 1;
    private TextView tvPrice, tvChangePercent, tvOpen, tvPreviousClose, tvDayLow, tvDayHigh,
            tvYearLow, tvYearHigh;
    Toolbar toolbar;
    ArrayList<String> labels;
    ArrayList<Float> values;
    int rangeMin, rangeMax;
    int labelMin, labelMax;


    //private final String[] mLabels = {"Jan", "Feb", "", "", "", "", "", "", ""};
    //private final float[][] mValues = {{0f, 2f, 1.4f, 4.f, 3.5f, 4.3f, 2f, 4f, 6.f},{1.5f, 2.5f, 1.5f, 5f, 4f, 5f, 4.3f, 2.1f, 1.4f}};

    String[] mLabels;
    float[] mValues;

    LineChartView quoteChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_stock);

        toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        quoteID = getIntent().getStringExtra("id");
        quoteName = getIntent().getStringExtra("name");

        getSupportActionBar().setTitle(quoteName);

        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
        Log.d(TAG, "ID: " + quoteID);


        tvPrice = (TextView) findViewById(R.id.tvPrice);
        tvChangePercent = (TextView) findViewById(R.id.tvChangePercent);
        //tvChange = (TextView) findViewById(R.id.tvChange);
        tvOpen = (TextView) findViewById(R.id.tvOpen);
        tvPreviousClose = (TextView) findViewById(R.id.tvPrevClose);
        tvDayLow = (TextView) findViewById(R.id.tvDayLow);
        tvDayHigh = (TextView) findViewById(R.id.tvDayHigh);
        tvYearLow = (TextView) findViewById(R.id.tvYearLow);
        tvYearHigh = (TextView) findViewById(R.id.tvYearHigh);
        quoteChart = (LineChartView) findViewById(R.id.linechart);

        //Chart

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // This narrows the return to only the stocks that are most current.
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns.SYMBOL, QuoteColumns.NAME, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.DAYLOW,
                        QuoteColumns.DAYHIGH, QuoteColumns.YEARLOW, QuoteColumns.YEARHIGH,
                        QuoteColumns.OPEN, QuoteColumns.PREVIOUSCLOSE, QuoteColumns.ISUP,
                        QuoteColumns.CREATED, QuoteColumns.ISCURRENT},
                QuoteColumns._ID + " = ?",
                new String[]{quoteID},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        quoteData = data;
        Log.d(TAG, DatabaseUtils.dumpCursorToString(data));
        quoteData.moveToFirst();
        quoteSymbol = quoteData.getString(quoteData.getColumnIndex(QuoteColumns.SYMBOL));
        tvPrice.setText(quoteData.getString(quoteData.getColumnIndex(QuoteColumns.BIDPRICE)));
        tvChangePercent.setText(quoteData.getString(quoteData.getColumnIndex(QuoteColumns.PERCENT_CHANGE)));
        int sdk = Build.VERSION.SDK_INT;
        if (quoteData.getInt(quoteData.getColumnIndex("is_up")) == 1) {
            tvChangePercent.setTextColor(getResources().getColor(R.color.high_green));
        } else {
            tvChangePercent.setTextColor(getResources().getColor(R.color.low_red));
        }

        tvOpen.setText(quoteData.getString(quoteData.getColumnIndex(QuoteColumns.OPEN)));
        tvPreviousClose.setText(quoteData.getString(quoteData.getColumnIndex(QuoteColumns.PREVIOUSCLOSE)));
        tvDayLow.setText(quoteData.getString(quoteData.getColumnIndex(QuoteColumns.DAYLOW)));
        tvDayHigh.setText(quoteData.getString(quoteData.getColumnIndex(QuoteColumns.DAYHIGH)));
        tvYearLow.setText(quoteData.getString(quoteData.getColumnIndex(QuoteColumns.YEARLOW)));
        tvYearHigh.setText(quoteData.getString(quoteData.getColumnIndex(QuoteColumns.YEARHIGH)));
        downloadFinanceChart();
        //Chart

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    private void downloadFinanceChart() {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://chartapi.finance.yahoo.com/instrument/1.0/" + quoteSymbol + "/chartdata;type=close;range=1d/json")
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.code() == 200) {
                    try {
                        // Trim response string
                        String result = response.body().string();
                        if (result.startsWith("finance_charts_json_callback( ")) {
                            result = result.substring(29, result.length() - 2);
                        }

                        // Parse JSON
                        JSONObject object = new JSONObject(result);

                        //Label value
                        /*JSONArray jsonLabels = object.getJSONArray("labels");
                        for (int i = 0; i < jsonLabels.length(); i++) {

                        }*/

                        //LABEL MIN/MAX value
                        /*JSONObject jsonLabel = object.getJSONObject("Timestamp");
                        labelMin = jsonLabel.getInt("min");
                        labelMax = jsonLabel.getInt("max");*/

                        //Range MIN/MAX value
                        JSONObject range = object.getJSONObject("ranges");
                        JSONObject closeItem = range.getJSONObject("close");
                        rangeMin = closeItem.getInt("min");
                        rangeMax = closeItem.getInt("max");

                        //Series values
                        labels = new ArrayList<>();
                        values = new ArrayList<>();
                        JSONArray series = object.getJSONArray("series");
                        for (int i = 0; i < series.length(); i++) {
                            JSONObject seriesItem = series.getJSONObject(i);

                            int unixTime = seriesItem.getInt("Timestamp");
                            long timestamp = unixTime * 1000;  // msec

                            Date d = new Date(timestamp);
                            SimpleDateFormat localDateFormat = new SimpleDateFormat("HH:mm");
                            String time = localDateFormat.format(d);
                            labels.add(time);
                            values.add(Float.parseFloat(seriesItem.getString("close")));
                        }
                        onDownloadCompleted();

                    } catch (Exception e) {
                        Log.d(TAG, "onFailure: Failed");
                        e.printStackTrace();
                    }
                } else {
                    Log.d(TAG, "onFailure: Failed");
                }
            }

            private void onDownloadCompleted() {
                DetailStockActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mLabels = new String[labels.size()];
                        mValues = new float[values.size()];
                        labels.toArray(mLabels);
                        int i = 0;
                        for (Float f : values) {
                            mValues[i++] = (f != null ? f : Float.NaN);
                        }

                        LineSet dataset = new LineSet(mLabels, mValues);
                        dataset.setColor(Color.parseColor("#53c1bd")).setThickness(3);
                        for (int j = 0; j < mLabels.length; j+=5) {
                            Point point = (Point) dataset.getEntry(j);
                        }
                        quoteChart.addData(dataset);

                        Paint gridPaint = new Paint();
                        gridPaint.setColor(Color.parseColor("#000000"));
                        gridPaint.setStyle(Paint.Style.STROKE);
                        gridPaint.setAntiAlias(true);
                        gridPaint.setStrokeWidth(Tools.fromDpToPx(0));

                            quoteChart.setBorderSpacing(1)
                                    .setXLabels(AxisController.LabelPosition.NONE)
                                    .setYLabels(AxisController.LabelPosition.OUTSIDE)
                                    .setXAxis(false)
                                    .setYAxis(false)
                                    .setGrid(ChartView.GridType.FULL, gridPaint)
                                .setAxisBorderValues(rangeMin - 1, rangeMax + 1)
                                .setBorderSpacing(Tools.fromDpToPx(1));

                        quoteChart.show();
                    }
                });
            }

                @Override
                public void onFailure (Request request, IOException e){
                    Log.d(TAG, "onFailure: Failed");
                }
            }

            );
        }

    }
