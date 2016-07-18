package com.example.android.stockhawk.basic;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by veeral on 05/07/2016.
 */
public class ResponseQuote {
    @SerializedName("query")
    private Result mResult;

    public List<Quote> getStockQuotes() {
        List<Quote> result = new ArrayList<>();
        if (mResult != null && mResult.getQuote() != null) {
            Quote quote = mResult.getQuote().getStockQuote();
            if (quote.getBid() != null && quote.getChangeInPercent() != null
                    && quote.getChange() != null) {
                result.add(quote);
            }
        }
        return result;
    }

    public class Result {

        @SerializedName("count")
        private int mCount;

        @SerializedName("results")
        private StockQuote mQuote;

        public StockQuote getQuote() {
            return mQuote;
        }
    }

    public class StockQuote {

        @SerializedName("quote")
        private Quote mStockQuote;

        public Quote getStockQuote() {
            return mStockQuote;
        }
    }
}
