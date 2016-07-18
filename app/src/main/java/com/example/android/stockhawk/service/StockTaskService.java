package com.example.android.stockhawk.service;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.RemoteException;
import android.util.Log;

import com.example.android.stockhawk.basic.GetStockDataAPI;
import com.example.android.stockhawk.basic.Quote;
import com.example.android.stockhawk.basic.ResponseHistoricalQuoteData;
import com.example.android.stockhawk.basic.ResponseQuote;
import com.example.android.stockhawk.basic.ResponseQuotes;
import com.example.android.stockhawk.data.HistoricalQuoteColumns;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.GcmTaskService;
import com.google.android.gms.gcm.TaskParams;
import com.example.android.stockhawk.data.QuoteColumns;
import com.example.android.stockhawk.data.QuoteProvider;
import com.example.android.stockhawk.rest.Utils;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
//import com.squareup.okhttp.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.Response;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by sam_chordas on 9/30/15.
 * The GCMTask service is primarily for periodic tasks. However, OnRunTask can be called directly
 * and is used for the initialization and adding task as well.
 */
public class StockTaskService extends GcmTaskService{
  private String LOG_TAG = StockTaskService.class.getSimpleName();

  private OkHttpClient client = new OkHttpClient();
  private Context mContext;
  private StringBuilder mStoredSymbols = new StringBuilder();
  private boolean isUpdate;
  private final static String INIT_QUOTES = "\"YHOO\",\"AAPL\",\"GOOG\",\"MSFT\"";
  public final static String TAG_PERIODIC = "periodic";

  public StockTaskService(){}

  public StockTaskService(Context context){
    mContext = context;
  }

    @Override
    public int onRunTask(TaskParams params){
        if(mContext == null){
            return GcmNetworkManager.RESULT_FAILURE;
        }
        try{
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(GetStockDataAPI.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            GetStockDataAPI service = retrofit.create(GetStockDataAPI.class);
            String query="select *from yahoo.finance.quotes where symbol in ("
                    + buildUrl(params)
                    +")";


            if(params.getTag().equals(StockIntentService.ACTION_INIT)){
                Call<ResponseQuotes> responseQuotesCall = service.getStocks(query);
                Response<ResponseQuotes> quotesResponse = responseQuotesCall.execute();
                ResponseQuotes responseQuotes = quotesResponse.body();
                if(responseQuotes!=null)
                saveQuotesToDB(responseQuotes.getStockQuotes());
            }else{
                Call<ResponseQuote> responseQuoteCall = service.getStock(query);
                Response<ResponseQuote> quoteResponse = responseQuoteCall.execute();
                ResponseQuote responseQuote = quoteResponse.body();
                saveQuotesToDB(responseQuote.getStockQuotes());
            }
            return GcmNetworkManager.RESULT_SUCCESS;
        }catch (IOException | RemoteException | OperationApplicationException e){
            Log.e(LOG_TAG, e.getMessage(),e);
            return GcmNetworkManager.RESULT_FAILURE;
        }
    }


    private String buildUrl(TaskParams params)throws  UnsupportedEncodingException{
        ContentResolver contentResolver = mContext.getContentResolver();
        if(params.getTag().equals(StockIntentService.ACTION_INIT) || params.getTag().equals(TAG_PERIODIC)){
            isUpdate=true;
            Cursor cursor = contentResolver.query(QuoteProvider.Quotes.CONTENT_URI,
                    new String[]{"Distinct " + QuoteColumns.SYMBOL},
                    null,null,null);

            if(cursor!=null && cursor.getCount()==0 || cursor==null){
                return INIT_QUOTES;
            }else {
                DatabaseUtils.dumpCursor(cursor);
                cursor.moveToFirst();
                for(int i=0;i<cursor.getCount();i++){
                    mStoredSymbols.append("\"");
                    mStoredSymbols.append(cursor.getString(cursor.getColumnIndex(QuoteColumns.SYMBOL)));
                    mStoredSymbols.append("\",");
                    cursor.moveToNext();
                }
                mStoredSymbols.replace(mStoredSymbols.length()-1,mStoredSymbols.length(),"");
                return mStoredSymbols.toString();
            }
        }else if(params.getTag().equals(StockIntentService.ACTION_ADD)) {
            isUpdate=false;
            String stockInput = params.getExtras().getString(StockIntentService.EXTRA_SYMBOL);
            return "\""+ stockInput + "\"";
        }else{
            throw new IllegalStateException("Action not specified");
        }
    }
    private void saveQuotesToDB(List<Quote> quotes) throws RemoteException,OperationApplicationException{
        ContentResolver contentResolver = mContext.getContentResolver();
        ArrayList<ContentProviderOperation> batchOperation = new ArrayList<>();
        for(Quote quote: quotes){
            batchOperation.add(Utils.buildBatchOperation(quote));
        }
        if(isUpdate){
            ContentValues contentValues = new ContentValues();
            contentValues.put(QuoteColumns.ISCURRENT,0);
            contentResolver.update(QuoteProvider.Quotes.CONTENT_URI,contentValues,null,null);
        }
        contentResolver.applyBatch(QuoteProvider.AUTHORITY,batchOperation);

        for(Quote quote:quotes){
            try {
                fetchHistoricalQuoteData(quote);
            }catch (IOException | RemoteException | OperationApplicationException e){
                Log.e(LOG_TAG,e.getMessage(),e);
            }
        }
    }

    private void fetchHistoricalQuoteData(Quote quote) throws IOException,RemoteException,OperationApplicationException{

      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
      Date currentDate = new Date();

      Calendar calendarEnd = Calendar.getInstance();
      calendarEnd.setTime(currentDate);
      calendarEnd.add(Calendar.DATE, 0);


      Calendar calendarStart = Calendar.getInstance();
      calendarStart.setTime(currentDate);
      calendarStart.add(Calendar.MONTH, -1);

      String startDate = dateFormat.format(calendarStart.getTime());
      String endDate = dateFormat.format(calendarEnd.getTime());

      String query = "select * from yahoo.finance.historicaldata where symbol=\""+
            quote.getSymbol()+
            "\" and startDate=\"" + startDate + "\" and endDate=\"" + endDate + "\"";

      Retrofit retrofit = new Retrofit.Builder()
              .baseUrl(GetStockDataAPI.BASE_URL)
              .addConverterFactory(GsonConverterFactory.create())
              .build();
      GetStockDataAPI stockDataAPI = retrofit.create(GetStockDataAPI.class);
      Call<ResponseHistoricalQuoteData> call = stockDataAPI.getStockHistoricalData(query);
      Response<ResponseHistoricalQuoteData> response;

      response = call.execute();
      ResponseHistoricalQuoteData responseHistoricalQuoteData = response.body();

      if(responseHistoricalQuoteData!=null){
          saveHistoricDataToDB(responseHistoricalQuoteData.getHistoricData());
      }
  }
    private void saveHistoricDataToDB(List<ResponseHistoricalQuoteData.StockQuote> quoteList) throws RemoteException,OperationApplicationException{
        ContentResolver contentResolver = mContext.getContentResolver();
        ArrayList<ContentProviderOperation> batchOperation = new ArrayList<>();
        for(ResponseHistoricalQuoteData.StockQuote quote:quoteList){
            contentResolver.delete(QuoteProvider.HistoricalQuotes.CONTENT_URI, HistoricalQuoteColumns.SYMBOL + "=\""+ quote.getSymbol() + "\"",null);
            batchOperation.add(Utils.buildBatchOperation(quote));
        }
        contentResolver.applyBatch(QuoteProvider.AUTHORITY,batchOperation);
    }
}
