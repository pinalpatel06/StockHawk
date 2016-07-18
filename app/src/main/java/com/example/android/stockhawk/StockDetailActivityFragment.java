package com.example.android.stockhawk;

import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TabHost;
import android.widget.TextView;

import com.example.android.stockhawk.data.HistoricalQuoteColumns;
import com.example.android.stockhawk.data.QuoteColumns;
import com.example.android.stockhawk.data.QuoteProvider;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * A placeholder fragment containing details of selected quotes
 * also display chart based on historic values
 */
public class StockDetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, TabHost.OnTabChangeListener{

    public static String LOG_TAG = StockDetailActivityFragment.class.getSimpleName();
    public static final String ARGS_SYMBOL = "ARGS_SYMBOL";
    public static final String EXTRA_CURRENT_TAB = "EXTRA_CURRENT_TAB";
    private static final int CURSOR_LOADER_ID = 1;
    private static final int CURSOR_LOADER_ID_FOR_FETCH_ALL_DATA=3;
    private static final int CURSOR_LOADER_ID_FOR_LINE_CHART = 2;

    private String mSymbol;
    private String mSelectedTab;

    @Bind(R.id.stock_name)
    TextView mNameView;
    @Bind(R.id.stock_symbol)
    TextView mSymbolView;
    @Bind(R.id.stock_bidprice)
    TextView mEbitdaView;
    @Bind(R.id.stock_change)
    TextView mChange;
    @Bind(R.id.tabHost)
    TabHost mTabHost;
    @Bind(android.R.id.tabcontent)
    View mTabContent;
    @Bind(R.id.stockChart)
    LineChartView mChart;

    public StockDetailActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();

        if(args!=null) {
            if (getArguments().containsKey(ARGS_SYMBOL)) {
                mSymbol = getArguments().getString(ARGS_SYMBOL);
            }
        }

        if(savedInstanceState==null){
            mSelectedTab = getString(R.string.stock_detail_tab1);
        }else{
            mSelectedTab=savedInstanceState.getString(EXTRA_CURRENT_TAB);
        }
        if(mSymbol==null){
            getLoaderManager().initLoader(CURSOR_LOADER_ID_FOR_FETCH_ALL_DATA,null,this);
            getLoaderManager().initLoader(CURSOR_LOADER_ID_FOR_LINE_CHART,null,this);
        }else {
            getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);
            getLoaderManager().initLoader(CURSOR_LOADER_ID_FOR_LINE_CHART,null,this);
        }

    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putString(EXTRA_CURRENT_TAB, mSelectedTab);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_stock_detail, container, false);
        ButterKnife.bind(this, rootView);

        //Set Stock Title on Actionbar
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if(actionBar!=null){
            try {
                actionBar.setTitle(mSymbol);
            }catch (NullPointerException e){
                Log.e(LOG_TAG, "Error " + e);
            }
        }

        setUpTab();
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(mSymbol==null){
             if(id==CURSOR_LOADER_ID_FOR_FETCH_ALL_DATA){
                return new CursorLoader(getActivity(), QuoteProvider.Quotes.CONTENT_URI,
                        new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                                QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP,
                                QuoteColumns.NAME},
                        null,null, null);

            }
        }
        if (id == CURSOR_LOADER_ID) {
            return new CursorLoader(getContext(), QuoteProvider.Quotes.CONTENT_URI,
                    new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                            QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP,
                            QuoteColumns.NAME},
                    QuoteColumns.SYMBOL + " = \"" + mSymbol + "\"",
                    null, null);
        }else if (id == CURSOR_LOADER_ID_FOR_LINE_CHART) {
            String sortOrder = QuoteColumns._ID + " ASC LIMIT 5";
            if (mSelectedTab.equals(getString(R.string.stock_detail_tab2))) {
                sortOrder = QuoteColumns._ID + " ASC LIMIT 14";
            } else if (mSelectedTab.equals(getString(R.string.stock_detail_tab3))) {
                sortOrder = QuoteColumns._ID + " ASC";
            }
           return new CursorLoader(getContext(), QuoteProvider.HistoricalQuotes.CONTENT_URI,
                    new String[]{HistoricalQuoteColumns._ID, HistoricalQuoteColumns.SYMBOL,
                            HistoricalQuoteColumns.BIDPRICE, HistoricalQuoteColumns.DATE},
                    HistoricalQuoteColumns.SYMBOL + " = \"" + mSymbol + "\"",
                    null, sortOrder);
        } else {
            throw new IllegalStateException();
        }

    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if(loader.getId()==CURSOR_LOADER_ID_FOR_FETCH_ALL_DATA && data!=null && data.moveToFirst()){
            mSymbol=data.getString(data.getColumnIndex(QuoteColumns.SYMBOL));
            String name = data.getString(data.getColumnIndex(QuoteColumns.NAME));
            mNameView.setText(name);


            String symbol = data.getString(data.getColumnIndex(QuoteColumns.SYMBOL));
            mSymbolView.setText(getString(R.string.stock_detail_tab_header, symbol));

            String ebitda = data.getString(data.getColumnIndex(QuoteColumns.BIDPRICE));
            mEbitdaView.setText(ebitda);


            String change = data.getString(data.getColumnIndex(QuoteColumns.CHANGE));
            String percentChange = data.getString(data.getColumnIndex(QuoteColumns.PERCENT_CHANGE));
            String mixedChange = change + " (" + percentChange + ")";
            mChange.setText(mixedChange);
            getLoaderManager().restartLoader(CURSOR_LOADER_ID_FOR_LINE_CHART, null, this);
        }
        if (loader.getId() == CURSOR_LOADER_ID && data != null && data.moveToFirst()) {

            String name = data.getString(data.getColumnIndex(QuoteColumns.NAME));
            mNameView.setText(name);


            String symbol = data.getString(data.getColumnIndex(QuoteColumns.SYMBOL));
            mSymbolView.setText(getString(R.string.stock_detail_tab_header, symbol));

            String ebitda = data.getString(data.getColumnIndex(QuoteColumns.BIDPRICE));
            mEbitdaView.setText(ebitda);


            String change = data.getString(data.getColumnIndex(QuoteColumns.CHANGE));
            String percentChange = data.getString(data.getColumnIndex(QuoteColumns.PERCENT_CHANGE));
            String mixedChange = change + " (" + percentChange + ")";
            mChange.setText(mixedChange);

        } else if (loader.getId() == CURSOR_LOADER_ID_FOR_LINE_CHART && data != null &&
                data.moveToFirst()) {
                updateChart(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // Nothing to do
    }

    /**
     * setUpTab - set TabView on Fragment placeholder. it contains three tab for different interval
     */
    private void setUpTab(){
        mTabHost.setup();
        TabHost.TabSpec tabSpec;
        tabSpec = mTabHost.newTabSpec(getString(R.string.stock_detail_tab1));
        tabSpec.setIndicator(getString(R.string.stock_detail_tab1));
        tabSpec.setContent(android.R.id.tabcontent);
        mTabHost.addTab(tabSpec);

        tabSpec = mTabHost.newTabSpec(getString(R.string.stock_detail_tab2));
        tabSpec.setIndicator(getString(R.string.stock_detail_tab2));
        tabSpec.setContent(android.R.id.tabcontent);
        mTabHost.addTab(tabSpec);

        tabSpec = mTabHost.newTabSpec(getString(R.string.stock_detail_tab3));
        tabSpec.setIndicator(getString(R.string.stock_detail_tab3));
        tabSpec.setContent(android.R.id.tabcontent);
        mTabHost.addTab(tabSpec);

        mTabHost.setOnTabChangedListener(this);

        if (mSelectedTab.equals(getString(R.string.stock_detail_tab2))) {
            mTabHost.setCurrentTab(1);
        }else if(mSelectedTab.equals(R.string.stock_detail_tab3)){
            mTabHost.setCurrentTab(2);
        }else{
            mTabHost.setCurrentTab(0);
        }
    }
    @Override
    public void onTabChanged(String tabId) {
        mSelectedTab = tabId;
        getLoaderManager().restartLoader(CURSOR_LOADER_ID_FOR_LINE_CHART, null, this);
    }

    /**
     * updateChart draw a line chart based on historic value
     * @param data - give historic data for selected quotes
     */
    private void updateChart(Cursor data){
        List<AxisValue> axisValuesX = new ArrayList<>();
        List<PointValue> pointValues = new ArrayList<>();
        String date,bidPrice;
        int x;
        int counter = -1;
        do {
            counter++;
            date = data.getString(data.getColumnIndex(HistoricalQuoteColumns.DATE));
            bidPrice = data.getString(data.getColumnIndex(HistoricalQuoteColumns.BIDPRICE));
            x = data.getCount()-counter-1;

            PointValue pointValue = new PointValue(x,Float.valueOf(bidPrice));
            pointValue.setLabel(date);
            pointValues.add(pointValue);

            if(counter!=0 && counter%(data.getCount()/3)==0){
                AxisValue axisValue = new AxisValue(x);
                axisValue.setLabel(date);
                axisValuesX.add(axisValue);
            }
        }while (data.moveToNext());

        Line line = new Line(pointValues).setColor(Color.BLUE).setCubic(false);
        List<Line> lines = new ArrayList<>();
        lines.add(line);
        LineChartData lineChartData = new LineChartData();
        lineChartData.setLines(lines);

        //X axis
        Axis xAxis = new Axis(axisValuesX);
        xAxis.setHasLines(true);
        xAxis.setMaxLabelChars(4);
        lineChartData.setAxisXBottom(xAxis);

        //Y axis

        Axis yAxis = new Axis();
        yAxis.setAutoGenerated(true);
        yAxis.setHasLines(true);
        yAxis.setMaxLabelChars(4);
        lineChartData.setAxisYLeft(yAxis);

        //update chart data
        // Update chart with new data.
        mChart.setInteractive(false);
        mChart.setLineChartData(lineChartData);

        // Show chart
        mChart.setVisibility(View.VISIBLE);
        mTabContent.setVisibility(View.VISIBLE);
    }
}
