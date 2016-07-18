package com.example.android.stockhawk.basic;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by veeral on 05/07/2016.
 */
@SuppressWarnings("unused")
public class ResponseQuotes {
    @SerializedName("query")
    private Results mResults;

    public List<Quote> getStockQuotes() {
        List<Quote> result = new ArrayList<>();
        List<Quote> quotes = mResults.getQuote().getQuotes();
        for (Quote stockQuote : quotes) {
            if (stockQuote.getBid() != null && stockQuote.getChangeInPercent() != null
                    && stockQuote.getChange() != null) {
                result.add(stockQuote);
            }
        }
        return result;
    }

    @SuppressWarnings("unused")
    public class Results {

        @SerializedName("count")
        private String mCount;

        @SerializedName("results")
        private Quotes mQuote;

        public Quotes getQuote() {
            return mQuote;
        }
    }

    public class Quotes {

        @SerializedName("quote")
        private List<Quote> mStockQuotes = new ArrayList<>();

        public List<Quote> getQuotes() {
            return mStockQuotes;
        }
    }
}
