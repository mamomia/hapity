package com.app.hopity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.SwitchPreference;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.View;

/**
 * Created by Mushi on 2/24/2016.
 */
public class AppSettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    private String TAG = "AppSettingsActivity";
    private AppCompatDelegate mDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getDelegate().installViewFactory();
        getDelegate().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarAppSetting);
        toolbar.setNavigationIcon(R.drawable.img_back_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        setSupportActionBar(toolbar);
        addPreferencesFromResource(R.xml.pref_app_settings);

        // init settings
        initSettings();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getDelegate().onPostCreate(savedInstanceState);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        getDelegate().setContentView(layoutResID);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        getDelegate().onPostResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        getDelegate().onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getDelegate().onDestroy();
    }

    private void setSupportActionBar(@Nullable Toolbar toolbar) {
        getDelegate().setSupportActionBar(toolbar);
    }

    private AppCompatDelegate getDelegate() {
        if (mDelegate == null) {
            mDelegate = AppCompatDelegate.create(this, null);
        }
        return mDelegate;
    }

    private void initSettings() {
        SwitchPreference sensitive_setting = (SwitchPreference) findPreference("is_sensitive_settings");
        sensitive_setting.setChecked(sensitive_setting.isChecked());

        SwitchPreference inform_setting = (SwitchPreference) findPreference("inform_me_settings");
        inform_setting.setChecked(inform_setting.isChecked());

        SwitchPreference del_broadcast_setting = (SwitchPreference) findPreference("delete_broadcast_settings");
        del_broadcast_setting.setChecked(del_broadcast_setting.isChecked());
    }

    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updateSummary(key);
    }

    private void updateSummary(String key) {
        if (key.equals("is_sensitive_settings")) {

            SwitchPreference sensitive_setting = (SwitchPreference) findPreference("is_sensitive_settings");
            sensitive_setting.setChecked(sensitive_setting.isChecked());

        } else if (key.equals("inform_me_settings")) {

            SwitchPreference inform_setting = (SwitchPreference) findPreference("inform_me_settings");
            inform_setting.setChecked(inform_setting.isChecked());

        } else if (key.equals("delete_broadcast_settings")) {

            SwitchPreference del_broadcast_setting = (SwitchPreference) findPreference("delete_broadcast_settings");
            del_broadcast_setting.setChecked(del_broadcast_setting.isChecked());

        }
    }
}