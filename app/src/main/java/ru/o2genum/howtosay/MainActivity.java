package ru.o2genum.howtosay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.quinny898.library.persistentsearch.SearchBox;

import ru.o2genum.howtosay.api.dto.forvo.Pronunciation;


public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, BillingProcessor.IBillingHandler  {

    SharedPreferences mSharedPreferences;
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    private SearchableFragment mCurrentFragment;
    private SearchableFragment mPronunciationsFragment;
    private SearchableFragment mHistoryFragment;
    private SearchBox mSearchBox;
    private AdView mAdView;
    private RelativeLayout mAdContainer;

    private int mSearchBoxHeight;

    private BillingProcessor bp;

    public SearchBox getSearchBox() {
        return mSearchBox;
    }

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSharedPreferences = getSharedPreferences("PREF", MODE_PRIVATE);

        bp = new BillingProcessor(this, getString(R.string.license_key), this);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mSearchBox = (SearchBox) findViewById(R.id.search_box);
        mSearchBox.setLogoTextColor(getResources().getColor(R.color.secondary_text));
        mSearchBox.setLogoText("Search here");
        mSearchBox.enableVoiceRecognition(this);

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout), mSearchBox);
    }

    @Override
    public void onNavigationDrawerItemSelected(String tag) {
        if (tag.equals("ads_off")) {
            if (!bp.isPurchased("no_ads_mode_2")) {
                bp.purchase(this, "no_ads_mode_2");
            }
            return;
        }

        if (tag.equals("rate_app")) {
            setAlreadyRated(true);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse("market://details?id=ru.o2genum.howtosay"));
            startActivity(intent);
            return;
        }

        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        SearchableFragment mCurrentFragment = null;
        if (mPronunciationsFragment == null)
            mPronunciationsFragment = (SearchableFragment) fragmentManager.findFragmentByTag(PronunciationsFragment.class.getName());
        if (mHistoryFragment == null)
            mHistoryFragment = (SearchableFragment) fragmentManager.findFragmentByTag(HistoryFragment.class.getName());
        if (tag.equals("pronunciations")) {
            if (mPronunciationsFragment == null) {
                mPronunciationsFragment = PronunciationsFragment.newInstance();
                mPronunciationsFragment.setRetainInstance(true);
            }
        mCurrentFragment = mPronunciationsFragment;
        } else if (tag.equals("history")) {
                if (mHistoryFragment == null) {
                    mHistoryFragment = HistoryFragment.newInstance();
                    mHistoryFragment.setRetainInstance(true);
                }
                mCurrentFragment = mHistoryFragment;
        }
        fragmentManager.beginTransaction()
                .replace(R.id.container, mCurrentFragment, mCurrentFragment.getClass().getName())
                .commit();
    }

    public void onHistoryClicked(String word) {
        FragmentManager fragmentManager = getSupportFragmentManager();

        mPronunciationsFragment = PronunciationsFragment.newInstance();
        ((PronunciationsFragment) mPronunciationsFragment).setWord(word);
        mPronunciationsFragment.setRetainInstance(true);
        mCurrentFragment = mPronunciationsFragment;

        fragmentManager.beginTransaction()
                .replace(R.id.container, mCurrentFragment, mCurrentFragment.getClass().getName())
                .commit();
        mNavigationDrawerFragment.setSelected(0);
    }

    public void loadAd() {
        mAdView = (AdView) findViewById(R.id.adView);
        mAdContainer = (RelativeLayout) findViewById(R.id.ad_container);

        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mAdView.setVisibility(View.VISIBLE);
            }
        });
        mAdView.loadAd(adRequest);
    }

    public void hideAd() {
        mAdView = (AdView) findViewById(R.id.adView);
        mAdContainer = (RelativeLayout) findViewById(R.id.ad_container);

        mAdView.setVisibility(View.GONE);
    }

    public int getPlaysCounter() {
        return mSharedPreferences.getInt("PLAYS_COUNTER", 0);
    }

    public void incrementPlaysCounter() {
        mSharedPreferences.edit().putInt("PLAYS_COUNTER", getPlaysCounter() + 1).commit();
    }

    public boolean isAlreadyRated() {
        return mSharedPreferences.getBoolean("ALREADY_RATED", false);
    }

    public void setAlreadyRated(boolean rated) {
        mSharedPreferences.edit().putBoolean("ALREADY_RATED", rated).commit();
    }

    public void showRateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("App feedback").setMessage("Thank you for using this app.\nWould you like to rate it?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                setAlreadyRated(true);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=ru.o2genum.howtosay"));
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Not yet", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onProductPurchased(String s, TransactionDetails transactionDetails) {
        if (transactionDetails.productId.equals("no_ads_mode_2")) {
            hideAd();
            mNavigationDrawerFragment.removeItem(2);
        }
    }

    @Override
    public void onPurchaseHistoryRestored() {
        if (bp.isPurchased("no_ads_mode_2")) {
            hideAd();
            mNavigationDrawerFragment.removeItem(2);
        }
    }

    @Override
    public void onBillingError(int i, Throwable throwable) {
    }

    @Override // Ready for purchases and checking
    public void onBillingInitialized() {
        if (!bp.isPurchased("no_ads_mode_2")) {
            loadAd();
        } else {
            mNavigationDrawerFragment.removeItem(2);
        }
        bp.loadOwnedPurchasesFromGoogle();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!bp.handleActivityResult(requestCode, resultCode, data))
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        if (bp != null)
            bp.release();

        super.onDestroy();
    }
}
