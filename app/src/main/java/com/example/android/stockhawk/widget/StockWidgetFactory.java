package com.example.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.android.stockhawk.R;
import com.example.android.stockhawk.data.QuoteColumns;
import com.example.android.stockhawk.data.QuoteProvider;

/**
 * Created by veeral on 06/07/2016.
 */
public class StockWidgetFactory implements RemoteViewsService.RemoteViewsFactory {

    private Cursor mCursor;
    private Context mContext;
    int mWidgetId;

    public StockWidgetFactory(Context context,Intent intent){
        mContext = context;
        mWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID);
    }
    @Override
    public int getCount(){
        return mCursor.getCount();
    }
    @Override
    public long getItemId(int position){
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public RemoteViews getLoadingView(){
        return null;
    }

    @Override
    public RemoteViews getViewAt(int position){
        RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_stock_list);
        if(mCursor.moveToPosition(position)){
            remoteViews.setTextViewText(R.id.stock_symbol,mCursor.getString(mCursor.getColumnIndex(QuoteColumns.SYMBOL)));
            remoteViews.setTextViewText(R.id.bid_price,mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE)));
            remoteViews.setTextViewText(R.id.stock_change,mCursor.getString(mCursor.getColumnIndex(QuoteColumns.CHANGE)));

        }
        return remoteViews;
    }
    @Override
    public int getViewTypeCount(){
        return 1;
    }
    @Override
    public void onCreate(){
    }

    @Override
    public void onDataSetChanged() {
        if(mCursor!=null){
            mCursor.close();
        }
        mCursor = mContext.getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID,QuoteColumns.SYMBOL,QuoteColumns.BIDPRICE,
                QuoteColumns.PERCENT_CHANGE,QuoteColumns.CHANGE,QuoteColumns.ISUP},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
    }

    @Override
    public void onDestroy() {
        if(mCursor!=null){
            mCursor.close();
        }
    }


}
