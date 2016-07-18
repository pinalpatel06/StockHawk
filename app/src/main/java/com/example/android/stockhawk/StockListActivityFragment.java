package com.example.android.stockhawk;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.content.CursorLoader;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.LoaderManager;
import android.content.Intent;
import android.database.Cursor;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.example.android.stockhawk.data.QuoteColumns;
import com.example.android.stockhawk.data.QuoteProvider;
import com.example.android.stockhawk.rest.QuoteCursorAdapter;
import com.example.android.stockhawk.rest.RecyclerViewItemClickListener;
import com.example.android.stockhawk.rest.Utils;
import com.example.android.stockhawk.service.StockIntentService;
import com.example.android.stockhawk.service.StockTaskService;
import com.example.android.stockhawk.touch_helper.SimpleItemTouchHelperCallback;
import com.google.android.gms.gcm.GcmNetworkManager;
import com.google.android.gms.gcm.PeriodicTask;
import com.google.android.gms.gcm.Task;


import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A placeholder fragment containing a stock quotes price in unit & in Percentage
 */
public class StockListActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,RecyclerViewItemClickListener.OnItemClickListener{

    public static final int CHANGE_UNITS_DOLLARS = 0;
    public static final int CHANGE_UNITS_PERCENTAGES = 1;
    private static final int CURSOR_LOADER_ID = 0;
    private final String EXTRA_CHANGE_UNITS = "EXTRA_CHANGE_UNITS";
    private final String EXTRA_ADD_DIALOG_OPENED = "EXTRA_ADD_DIALOG_OPENED";
    private int mChangeUnits = CHANGE_UNITS_DOLLARS;
    private QuoteCursorAdapter mAdapter;

    @Bind(R.id.stock_list)
    RecyclerView mRecyclerView;
    @Bind(R.id.empty_state_no_connection)
    View mEmptyStateNoConnection;
    @Bind(R.id.empty_state_no_stock)
    View mEmptyStateNoStocks;

    @Bind(R.id.relative_layout)
    RelativeLayout mRelativeLayout;

    public StockListActivityFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_stock_list, container, false);
        ButterKnife.bind(this, rootView);

        if (savedInstanceState == null) {
            // The intent service is for executing immediate pulls from the Yahoo API
            // GCMTaskService can only schedule tasks, they cannot execute immediately
            Intent stackServiceIntent = new Intent(getActivity(),StockIntentService.class);
            // Run the initialize task service so that some stocks appear upon an empty database
            stackServiceIntent.putExtra(StockIntentService.EXTRA_TAG, StockIntentService.ACTION_INIT);
            if (Utils.isNetworkAvailable(getContext())) {
                getActivity().startService(stackServiceIntent);
            } else {
                Snackbar.make(mRelativeLayout, getString(R.string.no_internet_connectivity),
                        Snackbar.LENGTH_LONG).show();
            }
        } else {
            mChangeUnits = savedInstanceState.getInt(EXTRA_CHANGE_UNITS);
            if (savedInstanceState.getBoolean(EXTRA_ADD_DIALOG_OPENED, false)) {
                //showDialogForAddingStock();
            }
        }

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(getContext(), this));
        mAdapter = new QuoteCursorAdapter(getContext(), null, mChangeUnits);
        mRecyclerView.setAdapter(mAdapter);

        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(mAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

        // Create a periodic task to pull stocks once every hour after the app has been opened.
        // This is so Widget data stays up to date.
                PeriodicTask periodicTask = new PeriodicTask.Builder()
                .setService(StockTaskService.class)
                .setPeriod(/* 1h */ 60 * 5)
                .setFlex(/* 10s */ 10)
                .setTag(StockTaskService.TAG_PERIODIC)
                .setRequiredNetwork(Task.NETWORK_STATE_CONNECTED)
                .setRequiresCharging(false)
                .build();

        // Schedule task with tag "periodic." This ensure that only the stocks present in the DB
        // are updated.
        GcmNetworkManager.getInstance(getActivity()).schedule(periodicTask);
        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public void onCreateOptionsMenu(Menu menu,MenuInflater inflater){
        inflater.inflate(R.menu.menu_stock_list_fragment,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if(item.getItemId() == R.id.action_change_unit){
            if(mChangeUnits==CHANGE_UNITS_DOLLARS){
                // this is for changing stock changes from percent value to dollar value
                Utils.showPercent = !Utils.showPercent;
                getActivity().getContentResolver().notifyChange(QuoteProvider.Quotes.CONTENT_URI, null);
            }else{
                mChangeUnits=CHANGE_UNITS_DOLLARS;
            }
          //  mAdapter.setChangeUnits(mChangeUnits);
            //mAdapter.notifyDataSetChanged();



        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        mEmptyStateNoConnection.setVisibility(View.GONE);
        mEmptyStateNoStocks.setVisibility(View.GONE);
        //   mProgressBar.setVisibility(View.VISIBLE);
        return new CursorLoader(getActivity(), QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                        QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP, QuoteColumns.NAME},
                QuoteColumns.ISCURRENT + " = ?",
                new String[]{"1"},
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //    mProgressBar.setVisibility(View.GONE);
        mAdapter.swapCursor(data);

        if (mAdapter.getItemCount() == 0) {
            if (!Utils.isNetworkAvailable(getContext())) {
                mEmptyStateNoConnection.setVisibility(View.VISIBLE);
            } else {
                mEmptyStateNoStocks.setVisibility(View.VISIBLE);
            }
        } else {
            mEmptyStateNoConnection.setVisibility(View.GONE);
            mEmptyStateNoStocks.setVisibility(View.GONE);
        }

        if (!Utils.isNetworkAvailable(getContext())) {
            Snackbar.make(mRelativeLayout, getString(R.string.offline),
                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.try_again, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    restartLoader();
                }
            }).show();
        }

    }
    private void restartLoader(){
        getLoaderManager().restartLoader(CURSOR_LOADER_ID, null,this);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

   @Override
    public void onItemClick(View v, int position) {
          if(getActivity().findViewById(R.id.detail_container)!=null){
            Bundle arguments = new Bundle();
            arguments.putString(StockDetailActivityFragment.ARGS_SYMBOL, mAdapter.getSymbol(position));
            StockDetailActivityFragment fragment = new StockDetailActivityFragment();
            fragment.setArguments(arguments);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detail_container, fragment)
                    .commit();
        } else {
            Context context = v.getContext();
            Intent intent = new Intent(context, StockDetailActivity.class);
            intent.putExtra(StockDetailActivityFragment.ARGS_SYMBOL, mAdapter.getSymbol(position));
            context.startActivity(intent);
        }
    }

    /**
     * addStock() fetch requested stock from yahoo.finance & stored it in database.
     * @param stockQuote - pass requested stock to method in string type
     */
   public void addStock(final String stockQuote){

        new AsyncTask<Void,Void,Boolean>(){

            @Override
            protected Boolean doInBackground(Void... params){
                Cursor cursor = getActivity().getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI,
                        new String[]{QuoteColumns.SYMBOL},
                        QuoteColumns.SYMBOL + "=?",
                        new String[]{stockQuote},
                        null);
                if(cursor!=null){
                    cursor.close();
                    return cursor.getCount()!=0;
                }
                return Boolean.FALSE;
            }

            @Override
            protected  void onPostExecute(Boolean stockAvailable){
                if(stockAvailable){
                    Snackbar.make(mRelativeLayout,R.string.stock_already_saved,Snackbar.LENGTH_LONG).show();
                }else{
                    Intent stockIntentService = new Intent(getActivity(),StockIntentService.class);
                    stockIntentService.putExtra(StockIntentService.EXTRA_TAG, StockIntentService.ACTION_ADD);
                    stockIntentService.putExtra(StockIntentService.EXTRA_SYMBOL, stockQuote);
                    if (Utils.isNetworkAvailable(getContext())) {
                        getActivity().startService(stockIntentService);
                    } else {
                        Snackbar.make(mRelativeLayout, getString(R.string.no_internet_connectivity),
                                Snackbar.LENGTH_LONG).show();
                    }
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

}
