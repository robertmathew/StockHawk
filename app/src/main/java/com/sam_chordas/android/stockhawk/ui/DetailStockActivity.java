package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

public class DetailStockActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "DetailStockActivity";
    private Cursor quoteData;
    private String quoteID, quoteName;
    private static final int CURSOR_LOADER_ID = 1;
    private TextView tvPrice, tvChangePercent, tvOpen, tvPreviousClose, tvDayLow, tvDayHigh,
            tvYearLow, tvYearHigh;
    Toolbar toolbar;

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
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }
}
