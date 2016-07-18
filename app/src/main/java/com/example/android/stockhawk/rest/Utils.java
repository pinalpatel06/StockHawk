package com.example.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.android.stockhawk.basic.Quote;
import com.example.android.stockhawk.basic.ResponseHistoricalQuoteData;
import com.example.android.stockhawk.data.HistoricalQuoteColumns;
import com.example.android.stockhawk.data.QuoteColumns;
import com.example.android.stockhawk.data.QuoteProvider;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sam_chordas on 10/8/15.
 */
public class Utils {

  private static String LOG_TAG = Utils.class.getSimpleName();

  public static boolean showPercent = true;

/*  public static ArrayList quoteJsonToContentVals(String JSON){
    ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
    JSONObject jsonObject = null;
    JSONArray resultsArray = null;
    try{
      jsonObject = new JSONObject(JSON);
      if (jsonObject != null && jsonObject.length() != 0){
        jsonObject = jsonObject.getJSONObject("query");
        int count = Integer.parseInt(jsonObject.getString("count"));
        if (count == 1){
          jsonObject = jsonObject.getJSONObject("results")
              .getJSONObject("quote");
          batchOperations.add(buildBatchOperation(jsonObject));
        } else{
          resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

          if (resultsArray != null && resultsArray.length() != 0){
            for (int i = 0; i < resultsArray.length(); i++){
              jsonObject = resultsArray.getJSONObject(i);
              batchOperations.add(buildBatchOperation(jsonObject));
            }
          }
        }
      }
    } catch (JSONException e){
      Log.e(LOG_TAG, "String to JSON failed: " + e);
    }
    return batchOperations;
  }
*/
  public static String truncateBidPrice(String bidPrice){
    bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
    return bidPrice;
  }

  public static String truncateChange(String change, boolean isPercentChange){
    String weight = change.substring(0,1);
    String ampersand = "";
    if (isPercentChange){
      ampersand = change.substring(change.length() - 1, change.length());
      change = change.substring(0, change.length() - 1);
    }
    change = change.substring(1, change.length());
    double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
    change = String.format("%.2f", round);
    StringBuffer changeBuffer = new StringBuffer(change);
    changeBuffer.insert(0, weight);
    changeBuffer.append(ampersand);
    change = changeBuffer.toString();
    return change;
  }

  /*public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject){
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
            QuoteProvider.Quotes.CONTENT_URI);
    Log.d(LOG_TAG,jsonObject.toString());
    try {
      String change = jsonObject.getString("Change");
      builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
      builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
      builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
              jsonObject.getString("ChangeinPercent"), true));
      builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
      builder.withValue(QuoteColumns.ISCURRENT, 1);

      if (change.charAt(0) == '-'){
        builder.withValue(QuoteColumns.ISUP, 0);
      }else{
        builder.withValue(QuoteColumns.ISUP, 1);
      }
      builder.withValue(QuoteColumns.NAME,jsonObject.getString("Name"));

    } catch (JSONException e){
      e.printStackTrace();
    }
    return builder.build();
  }
*/
  public static ContentProviderOperation buildBatchOperation(Quote quote){
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(QuoteProvider.Quotes.CONTENT_URI);
    String change = quote.getChange();
    builder.withValue(QuoteColumns.SYMBOL,quote.getSymbol());
    builder.withValue(QuoteColumns.BIDPRICE,truncateBidPrice(quote.getBid()));
    builder.withValue(QuoteColumns.PERCENT_CHANGE,truncateChange(quote.getChangeInPercent(), true));
    builder.withValue(QuoteColumns.CHANGE,truncateChange(change,false));
    builder.withValue(QuoteColumns.ISCURRENT,1);
    if (change.charAt(0) == '-') {
      builder.withValue(QuoteColumns.ISUP, 0);
    } else {
      builder.withValue(QuoteColumns.ISUP, 1);
    }
    builder.withValue(QuoteColumns.NAME, quote.getName());
    return builder.build();
  }
  public static ContentProviderOperation buildBatchOperation(ResponseHistoricalQuoteData.StockQuote quote) {
    ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
            QuoteProvider.HistoricalQuotes.CONTENT_URI);
    builder.withValue(HistoricalQuoteColumns.SYMBOL, quote.getSymbol());
    builder.withValue(HistoricalQuoteColumns.BIDPRICE, quote.getOpen());
    builder.withValue(HistoricalQuoteColumns.DATE, quote.getDate());
    return builder.build();
  }

  static public boolean isNetworkAvailable(Context c) {
    ConnectivityManager cm =
            (ConnectivityManager)c.getSystemService(Context.CONNECTIVITY_SERVICE);

    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    return activeNetwork != null &&
            activeNetwork.isConnectedOrConnecting();
  }
}
