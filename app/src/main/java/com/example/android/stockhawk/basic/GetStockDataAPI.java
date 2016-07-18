package com.example.android.stockhawk.basic;

import com.example.android.stockhawk.R;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
/**
 * Created by veeral on 02/07/2016.
 */
public interface GetStockDataAPI {

   String BASE_URL = "https://query.yahooapis.com";

    @GET("/v1/public/yql?" +
                "format=json&diagnostics=true&" +
                "env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=")
        Call<ResponseQuotes> getStocks(@Query("q") String query);

        @GET("/v1/public/yql?" +
                "format=json&diagnostics=true&" +
                "env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=")
        Call<ResponseQuote> getStock(@Query("q") String query);

        @GET("/v1/public/yql?" +
                "format=json&diagnostics=true&" +
                "env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys&callback=")
        Call<ResponseHistoricalQuoteData> getStockHistoricalData(@Query("q") String query);

}
