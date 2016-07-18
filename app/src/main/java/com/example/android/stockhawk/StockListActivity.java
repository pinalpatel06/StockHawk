package com.example.android.stockhawk;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.android.stockhawk.data.QuoteColumns;
import com.example.android.stockhawk.data.QuoteProvider;
import com.example.android.stockhawk.rest.Utils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * StockListActivity is Main Activity contains StockListFragment
 * @author Pinal
 */
public class StockListActivity extends AppCompatActivity {
    private final String PRTAG = "PFTAG";
    private StockListActivityFragment mStockListActivityFragment;
    private StockDetailActivityFragment detailActivityFragment;
    private MaterialDialog mNewStockDialog;
    private boolean twoPane=false;
    private final String DETAILFRAGMENT_TAG = "DFTAG";
    private final String EXTRA_ADD_DIALOG_OPENED = "EXTRA_ADD_DIALOG_OPENED";

    @Bind(R.id.coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_list);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        if(savedInstanceState == null){
            mStockListActivityFragment = new StockListActivityFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.container, mStockListActivityFragment, PRTAG).commit();

        }else{
            mStockListActivityFragment =(StockListActivityFragment) getSupportFragmentManager().findFragmentByTag(PRTAG);
        }

        if(findViewById(R.id.detail_container)!=null){
            twoPane = true;
            if (savedInstanceState == null) {

                detailActivityFragment = new StockDetailActivityFragment();
                getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.detail_container, detailActivityFragment, DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            twoPane = false;
        }
    }

    /**
     * showDialogForAddingNewStock is launch new dialog to add new stock
     * if such stock already exist then display stock already exist
     */
    @OnClick(R.id.fab)
    public void showDialogForAddingNewStock(){
        if(Utils.isNetworkAvailable(getApplicationContext())){
            mNewStockDialog = new MaterialDialog.Builder(this).title(R.string.add_symbol)
                    .inputType(InputType.TYPE_CLASS_TEXT)
                    .autoDismiss(true)
                    .negativeText(R.string.disagree)
                    .input(R.string.input_hint,R.string.input_prefill,false,
                            new MaterialDialog.InputCallback(){
                                @Override
                                public void onInput(@NonNull MaterialDialog dialog , CharSequence input){
                                    mStockListActivityFragment.addStock(input.toString());
                                }
                            }).build();
            mNewStockDialog.show();
        }else{
            Snackbar.make(mCoordinatorLayout,getString(R.string.no_internet_connectivity),
                    Snackbar.LENGTH_LONG).setAction(R.string.try_again, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDialogForAddingNewStock();
                }
            }).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_stock_list, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
