package com.my.dictionary;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.mediation.admob.AdMobAdapter;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.my.dictionary.data.AppConfig;
import com.my.dictionary.data.DatabaseManager;
import com.my.dictionary.data.DatabaseUserManager;
import com.my.dictionary.data.GDPR;
import com.my.dictionary.data.GlobalVariable;
import com.my.dictionary.data.Tools;
import com.my.dictionary.fragment.FragmentAddNew;
import com.my.dictionary.fragment.FragmentFavorites;
import com.my.dictionary.fragment.FragmentMyWords;
import com.my.dictionary.fragment.FragmentTranslate;
import com.my.dictionary.theme.ActionBarColoring;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class ActivityMain extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener {

    //for ads
    private InterstitialAd mInterstitialAd;
    private AdView mAdView;

    private Toolbar toolbar;
    private static DatabaseManager db = null;
    private static GlobalVariable global = null;
    private View parent_view;
    private DatabaseUserManager db_user = null;

    //BottomNavigation
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        parent_view = findViewById(android.R.id.content);

        global = (GlobalVariable) getApplication();
        db = new DatabaseManager(this);
        db_user = new DatabaseUserManager(this);

        setupToolbar();
        setupDrawerMenu();
//        onNavigationSelected(R.id.nav_translate, getString(R.string.str_nav_translate));
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.bottom_translate);

        hideKeyboard();

        GDPR.updateConsentStatus(this);
        initBannerAds();
        initInterstitialAds();

    }

    private void hideBottomNav() {
        bottomNavigationView.setVisibility(View.GONE);
        bottomNavigationView = null;
    }

    private void showBottomNav() {
        Intent i = new Intent(getApplicationContext(), ActivityMain.class);
        startActivity(i);
        overridePendingTransition(0, R.anim.slide_out_left);
        finish();
    }

    private void setupToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setTitle(null);
        new ActionBarColoring(this).getColor(actionBar);
        // for system bar in lollipop
        Tools.systemBarLolipop(this);
    }

    private void setupDrawerMenu() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        updateDrawerCounter(navigationView);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                updateDrawerCounter(navigationView);
                hideKeyboard();
                super.onDrawerOpened(drawerView);
            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                if (showInterstitial()) return false;
                return onNavigationSelected(item.getItemId(), item.getTitle().toString());
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (bottomNavigationView == null) {
            showBottomNav();
        } else {
            if (!drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.openDrawer(GravityCompat.START);
            } else {
                doExitApp();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.nav_webview) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onNavigationSelected(int id, String title) {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        Fragment fragment = null;
        toolbar.setTitle(title);

        if (id == R.id.nav_add) {
            hideBottomNav();
            fragment = new FragmentAddNew();
        } else if (id == R.id.nav_my_word) {
            hideBottomNav();
            toolbar.setTitle(null);
            fragment = new FragmentMyWords();
        } else if (id == R.id.nav_more_app) {
            Tools.directLinkToBrowser(this, getString(R.string.more_app_url));
        } else if (id == R.id.nav_setting) {
            Intent i = new Intent(getApplicationContext(), ActivitySetting.class);
            startActivity(i);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        } else if (id == R.id.nav_webview) {
            Intent intent = new Intent(getApplicationContext(), ActivityWebview.class);
            intent.putExtra("dataInt", 1);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        } else if (id == R.id.nav_webview_two) {
            Intent intent = new Intent(getApplicationContext(), ActivityWebview.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            finish();
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_content, fragment);
            fragmentTransaction.commit();
        }
        return true;
    }

    public static DatabaseManager getDbManager() {
        return db;
    }

    public static GlobalVariable getGlobalVariable() {
        return global;
    }

    private void initBannerAds() {
        if (!AppConfig.ENABLE_MAIN_BANNER) return;
        mAdView = (AdView) findViewById(R.id.ad_view);
        AdRequest adRequest = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, GDPR.getBundleAd(this)).build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                mAdView.setVisibility(View.VISIBLE);
                super.onAdLoaded();
            }
        });
    }

    private void initInterstitialAds() {
        if (!AppConfig.ENABLE_MAIN_INTERSTITIAL) return;
        //prepare ads
        AdRequest adRequest2 = new AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter.class, GDPR.getBundleAd(this)).build();
        // Create the InterstitialAd and set the adUnitId.
        InterstitialAd.load(this, getString(R.string.interstitial_ad_unit_id), adRequest2, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                mInterstitialAd = interstitialAd;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                mInterstitialAd = null;
            }
        });
    }

    /** show ads */
    public boolean showInterstitial() {
        if (mInterstitialAd == null) return false;
        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
            @Override
            public void onAdDismissedFullScreenContent() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        initInterstitialAds();
                    }
                }, 1000 * AppConfig.DELAY_NEXT_INTERSTITIAL);
            }

            @Override
            public void onAdFailedToShowFullScreenContent(AdError adError) {
            }

            @Override
            public void onAdShowedFullScreenContent() {
                mInterstitialAd = null;
            }
        });
        mInterstitialAd.show(this);
        return true;
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void updateDrawerCounter(NavigationView navView) {
        int count = global.getFavorites();
        TextView view = (TextView) navView.getMenu().findItem(R.id.nav_favorites).getActionView();
        view.setText(String.valueOf(count));

        int count_my_word = db_user.getMyWordSize();
        TextView view_myword = (TextView) navView.getMenu().findItem(R.id.nav_my_word).getActionView();
        view_myword.setText(String.valueOf(count_my_word));
    }

    private long exitTime = 0;

    public void doExitApp() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
//            Snackbar.make(parent_view, "Press again to exit", Snackbar.LENGTH_SHORT).show();
            Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show();
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    @Override
    public void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        super.onPause();
    }

    @Override
    public void onResume() {

        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        FragmentTranslate fragmentTranslate = new FragmentTranslate();
        FragmentFavorites fragmentFavorites = new FragmentFavorites();

        switch (item.getItemId()) {
            case R.id.bottom_translate:
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_content, fragmentTranslate).commit();
                return true;
            case R.id.bottom_favorites:
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_content, fragmentFavorites).commit();
                return true;
        }
        return false;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}

