package com.example.android.stockhawk.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by veeral on 06/07/2016.
 */
public class StockWidgetService extends RemoteViewsService{
    @Override
    public RemoteViewsService.RemoteViewsFactory onGetViewFactory(Intent intent){
        return new StockWidgetFactory(getApplicationContext(),intent);
    }

}
