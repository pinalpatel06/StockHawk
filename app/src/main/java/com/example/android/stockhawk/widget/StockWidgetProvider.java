package com.example.android.stockhawk.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.android.stockhawk.R;

/**
 * Created by veeral on 06/07/2016.
 */
public class StockWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context,AppWidgetManager appWidgetManager,int[] appWidgetIds){
        super.onUpdate(context,appWidgetManager,appWidgetIds);
        for(int i:appWidgetIds){
            updateWidget(context,appWidgetManager,i);
        }

    }
    void updateWidget(Context context,AppWidgetManager appWidgetManager,int appWidgetId){
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_stock);
        setList(remoteViews,context,appWidgetId);
        appWidgetManager.updateAppWidget(appWidgetId,remoteViews);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.stock_list);
    }
    void setList(RemoteViews remoteViews,Context context, int appWidgetId){
        Intent adapter =new Intent(context,StockWidgetService.class);
        adapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,appWidgetId);
        remoteViews.setRemoteAdapter(R.id.stock_list,adapter);
    }
}
