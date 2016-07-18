package com.example.android.stockhawk.basic;


import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.Streams;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
/**
 * Created by veeral on 02/07/2016.
 */
public class ResponseHistoricalQuoteData {

    @SerializedName("query")
    private Results mResults;

    public List<StockQuote> getHistoricData() {
        List<StockQuote> result = new ArrayList<>();
        if (mResults.getQuote() != null) {
            List<StockQuote> quotes = mResults.getQuote().getStockQuotes();
            for (StockQuote quote : quotes) {
                result.add(quote);
            }
        }
        return result;
    }

    @SuppressWarnings("unused")
    public class Results {

        @SerializedName("count")
        private String mCount;

        @SerializedName("results")
        private StockQuotes mQuote;

        public StockQuotes getQuote() {
            return mQuote;
        }
    }

    public class StockQuotes {

        @SerializedName("quote")
        private List<StockQuote> mStockQuotes = new ArrayList<>();

        public List<StockQuote> getStockQuotes() {
            return mStockQuotes;
        }
    }

    public class StockQuote {

        @SerializedName("Symbol")
        private String mSymbol;

        @SerializedName("Date")
        private String mDate;

        @SerializedName("Low")
        private String mLow;

        @SerializedName("High")
        private String mHigh;

        @SerializedName("Open")
        private String mOpen;

        public String getSymbol() {
            return mSymbol;
        }

        public String getDate() {
            return mDate;
        }

        public String getOpen() {
            return mOpen;
        }
    }
}
