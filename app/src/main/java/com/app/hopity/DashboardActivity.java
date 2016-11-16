package com.app.hopity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.app.hopity.adapters.FragmentPageAdapter;
import com.app.hopity.appdata.GlobalSharedPrefs;
import com.app.hopity.fragments.BroadcastListFragment;
import com.app.hopity.fragments.MapFragment;
import com.app.hopity.fragments.MyProfileFragment;
import com.app.hopity.fragments.PeopleFragment;
import com.app.hopity.streamActivities.StreamBroadcastActivity;
import com.gigamole.library.NavigationTabBar;
import com.glidebitmappool.GlideBitmapPool;

import net.lateralview.simplerestclienthandler.RestClientManager;

import java.util.ArrayList;

import de.quist.app.errorreporter.ExceptionReporter;
import ua.com.crosp.solutions.library.prettytoast.PrettyToast;

public class DashboardActivity extends AppCompatActivity {

    private static final int PERMISSION_ID_CAMERA_AND_MIC_AND_LOC = 0;
    private static final String mTabColorString = "#78af5f";
    public static FragmentManager fragmentManager;
    private static FragmentPageAdapter pageAdapter;
    protected OnBackPressedListener onBackPressedListener;
    private String BroadcastListGlobal, BroadcastMapGlobal, People, MyProfile;
    private boolean doubleBackToExitPressedOnce = false;
    private String TAG = "Dashboard Activity";
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ExceptionReporter.register(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        new GlobalSharedPrefs(this);
        RestClientManager.initialize(getApplicationContext()).enableDebugLog(true);
        fragmentManager = getSupportFragmentManager();
        // Initialization
        FloatingActionButton fabStartStreaming = (FloatingActionButton) findViewById(R.id.fabStartStreaming);
        if (fabStartStreaming != null) {
            fabStartStreaming.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent intent = null;
                    if (ActivityCompat.checkSelfPermission(DashboardActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(DashboardActivity.this, android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(DashboardActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                            ActivityCompat.checkSelfPermission(DashboardActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(DashboardActivity.this,
                                new String[]{
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                        android.Manifest.permission.RECORD_AUDIO,
                                        android.Manifest.permission.CAMERA,
                                        android.Manifest.permission.ACCESS_FINE_LOCATION},
                                PERMISSION_ID_CAMERA_AND_MIC_AND_LOC);
                        return;
                    } else {
                        intent = new Intent(DashboardActivity.this, StreamBroadcastActivity.class);
                    }
                    if (intent == null) {
                        // Wait for permission result
                        return;
                    }
                    startActivity(intent);
                }
            });
        }

        BroadcastListGlobal = "Global Broadcasts";
        BroadcastMapGlobal = "Map Broadcasts";
        People = "Trending People";
        MyProfile = "My Profile";

        mViewPager = (ViewPager) findViewById(R.id.viewPagerDashboard);
        setupViewPager(mViewPager);
        mViewPager.setOffscreenPageLimit(3);
        initUI();
    }

    public void setupViewPager(ViewPager viewPager) {
        pageAdapter = new FragmentPageAdapter(getApplicationContext(), getSupportFragmentManager());
        pageAdapter.addFragment(BroadcastListFragment.newInstance(), BroadcastListGlobal, R.drawable.tab_broadcast);
        pageAdapter.addFragment(MapFragment.newInstance(), BroadcastMapGlobal, R.drawable.tab_map);
        pageAdapter.addFragment(PeopleFragment.newInstance(), People, R.drawable.tab_people);
        pageAdapter.addFragment(MyProfileFragment.newInstance(), MyProfile, R.drawable.tab_profile);
        viewPager.setAdapter(pageAdapter);
    }

    private void initUI() {

        final NavigationTabBar navigationTabBar = (NavigationTabBar) findViewById(R.id.viewTabBar);
        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();
        models.add(new NavigationTabBar.Model(
                ContextCompat.getDrawable(this, R.drawable.tab_broadcast), Color.parseColor(mTabColorString), BroadcastListGlobal));
        models.add(new NavigationTabBar.Model(
                ContextCompat.getDrawable(this, R.drawable.tab_map), Color.parseColor(mTabColorString), BroadcastMapGlobal));
        models.add(new NavigationTabBar.Model(
                ContextCompat.getDrawable(this, R.drawable.tab_people), Color.parseColor(mTabColorString), People));
        models.add(new NavigationTabBar.Model(
                ContextCompat.getDrawable(this, R.drawable.tab_profile), Color.parseColor(mTabColorString), MyProfile));
        navigationTabBar.setModels(models);
        navigationTabBar.setViewPager(mViewPager, 0);

        navigationTabBar.post(new Runnable() {
            @Override
            public void run() {
                final View bgNavigationTabBar = findViewById(R.id.bgViewTabBar);
                bgNavigationTabBar.getLayoutParams().height = (int) navigationTabBar.getBarHeight();
                bgNavigationTabBar.requestLayout();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_ID_CAMERA_AND_MIC_AND_LOC && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[2] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[3] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(DashboardActivity.this, StreamBroadcastActivity.class);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onBackPressed() {

        if (onBackPressedListener != null)
            onBackPressedListener.doBack();

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        PrettyToast.showDim(getApplicationContext(), "Please click back again to exit");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 1000);
    }

    public void setOnBackPressedListener(OnBackPressedListener onBackPressedListener) {
        this.onBackPressedListener = onBackPressedListener;
    }

    @Override
    protected void onDestroy() {
        onBackPressedListener = null;
        super.onDestroy();
        System.runFinalization();
        Runtime.getRuntime().gc();
        GlideBitmapPool.clearMemory();
        System.gc();
    }

    @Override
    protected void onPause() {
        System.gc();
        GlideBitmapPool.clearMemory();
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        android.support.v4.app.Fragment fragment = DashboardActivity.pageAdapter.getCurrentFragment();
        fragment.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    public interface OnBackPressedListener {
        void doBack();
    }
}