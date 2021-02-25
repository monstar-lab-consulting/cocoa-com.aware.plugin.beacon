package com.aware.plugin.beacon;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;

import com.aware.Aware;
import com.aware.ui.AppCompatPreferenceActivity;

public class Settings extends AppCompatPreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    //Plugin settings in XML @xml/preferences_conversations
    public static final String STATUS_PLUGIN_BEACON = "status_plugin_beacon";
    public static final String PLUGIN_SCAN_BACKGROUND_PERIOD = "plugin_scan_background_period";
    public static final String PLUGIN_SCAN_BETWEEN_BACKGROUND_PERIOD = "plugin_scan_between_background_period";
    public static final String PLUGIN_SCAN_BETWEEN_FOREGROUND_PERIOD = "plugin_scan_between_foreground_period";
    public static final String PLUGIN_BEACON_UDID = "plugin_beacon_udid";
    public static final String PLUGIN_BEACON_MAJOR = "plugin_beacon_major";
    public static final String PLUGIN_BEACON_MINOR = "plugin_beacon_minor";
    public static final String PLUGIN_BEACON_LAYOUT = "plugin_beacon_layout";

    public static final String SCAN_BACKGROUND_PERIOD_DEFAULT = "1100";
    public static final String SCAN_BETWEEN_BACKGROUND_PERIOD_DEFAULT = "0";
    public static final String SCAN_BETWEEN_FOREGROUND_PERIOD_DEFAULT = "2000";
    public static final String BEACON_UDID_DEFAULT = "b9407f30-f5f8-466e-aff9-25556b57fe6d";
    public static final String BEACON_LAYOUT_DEFAULT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";

    //Plugin settings UI elements
    private static CheckBoxPreference status;
    private static EditTextPreference scanBackgroundPeriod, scanBetweenBackgroundPeriod, scanBetweenForegroundPeriod, udid, layout, major, minor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences_beacon);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status = (CheckBoxPreference) findPreference(STATUS_PLUGIN_BEACON);

        if (Aware.getSetting(this, STATUS_PLUGIN_BEACON).length() == 0)
            Aware.setSetting(this, STATUS_PLUGIN_BEACON, true); //as soon as we install the plugin, it is activated, so default here

        status.setChecked(Aware.getSetting(this, STATUS_PLUGIN_BEACON).equals("true"));

        scanBackgroundPeriod = (EditTextPreference) findPreference(PLUGIN_SCAN_BACKGROUND_PERIOD);
        if (Aware.getSetting(this, PLUGIN_SCAN_BACKGROUND_PERIOD).length() == 0) {
            Aware.setSetting(this, PLUGIN_SCAN_BACKGROUND_PERIOD, SCAN_BACKGROUND_PERIOD_DEFAULT);
        }
        scanBackgroundPeriod.setText(Aware.getSetting(this, PLUGIN_SCAN_BACKGROUND_PERIOD));

        scanBetweenBackgroundPeriod = (EditTextPreference) findPreference(PLUGIN_SCAN_BETWEEN_BACKGROUND_PERIOD);
        if (Aware.getSetting(this, PLUGIN_SCAN_BETWEEN_BACKGROUND_PERIOD).length() == 0) {
            Aware.setSetting(this, PLUGIN_SCAN_BETWEEN_BACKGROUND_PERIOD, SCAN_BETWEEN_BACKGROUND_PERIOD_DEFAULT);
        }
        scanBetweenBackgroundPeriod.setText(Aware.getSetting(this, PLUGIN_SCAN_BETWEEN_BACKGROUND_PERIOD));

        scanBetweenForegroundPeriod = (EditTextPreference) findPreference(PLUGIN_SCAN_BETWEEN_FOREGROUND_PERIOD);
        if (Aware.getSetting(this, PLUGIN_SCAN_BETWEEN_FOREGROUND_PERIOD).length() == 0) {
            Aware.setSetting(this, PLUGIN_SCAN_BETWEEN_FOREGROUND_PERIOD, SCAN_BETWEEN_FOREGROUND_PERIOD_DEFAULT);
        }
        scanBetweenForegroundPeriod.setText(Aware.getSetting(this, PLUGIN_SCAN_BETWEEN_FOREGROUND_PERIOD));

        udid = (EditTextPreference) findPreference(PLUGIN_BEACON_UDID);
        if (Aware.getSetting(this, PLUGIN_BEACON_UDID).length() == 0) {
            Aware.setSetting(this, PLUGIN_BEACON_UDID, BEACON_UDID_DEFAULT);
        }
        udid.setText(Aware.getSetting(this, PLUGIN_BEACON_UDID));

        layout = (EditTextPreference) findPreference(PLUGIN_BEACON_LAYOUT);
        if (Aware.getSetting(this, PLUGIN_BEACON_LAYOUT).length() == 0) {
            Aware.setSetting(this, PLUGIN_BEACON_LAYOUT, BEACON_LAYOUT_DEFAULT);
        }
        layout.setText(Aware.getSetting(this, PLUGIN_BEACON_LAYOUT));

        major = (EditTextPreference) findPreference(PLUGIN_BEACON_MAJOR);
        if (Aware.getSetting(this, PLUGIN_BEACON_MAJOR).length() == 0) {
            Aware.setSetting(this, PLUGIN_BEACON_MAJOR, 0);
        }
        major.setText(Aware.getSetting(this, PLUGIN_BEACON_MAJOR));

        minor = (EditTextPreference) findPreference(PLUGIN_BEACON_MINOR);
        if (Aware.getSetting(this, PLUGIN_BEACON_MINOR).length() == 0) {
            Aware.setSetting(this, PLUGIN_BEACON_MINOR, 0);
        }
        minor.setText(Aware.getSetting(this, PLUGIN_BEACON_MINOR));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference setting = findPreference(key);

        if( setting.getKey().equals(STATUS_PLUGIN_BEACON) ) {
            Aware.setSetting(this, key, sharedPreferences.getBoolean(key, false));
            status.setChecked(sharedPreferences.getBoolean(key, false));
        }

        if( setting.getKey().equals(PLUGIN_SCAN_BACKGROUND_PERIOD) ) {
            Aware.setSetting(this, key, sharedPreferences.getString(key, SCAN_BACKGROUND_PERIOD_DEFAULT));
            scanBackgroundPeriod.setText(String.valueOf(sharedPreferences.getString(key, SCAN_BACKGROUND_PERIOD_DEFAULT)));
        }

        if( setting.getKey().equals(PLUGIN_SCAN_BETWEEN_BACKGROUND_PERIOD) ) {
            Aware.setSetting(this, key, sharedPreferences.getString(key, SCAN_BETWEEN_BACKGROUND_PERIOD_DEFAULT));
            scanBetweenBackgroundPeriod.setText(String.valueOf(sharedPreferences.getString(key, SCAN_BETWEEN_BACKGROUND_PERIOD_DEFAULT)));
        }

        if( setting.getKey().equals(PLUGIN_SCAN_BETWEEN_FOREGROUND_PERIOD) ) {
            Aware.setSetting(this, key, sharedPreferences.getString(key, SCAN_BETWEEN_FOREGROUND_PERIOD_DEFAULT));
            scanBetweenForegroundPeriod.setText(String.valueOf(sharedPreferences.getString(key, SCAN_BETWEEN_FOREGROUND_PERIOD_DEFAULT)));
        }

        if( setting.getKey().equals(PLUGIN_BEACON_UDID) ) {
            Aware.setSetting(this, key, sharedPreferences.getString(key, BEACON_UDID_DEFAULT));
            udid.setText(sharedPreferences.getString(key, BEACON_UDID_DEFAULT));
        }

        if( setting.getKey().equals(PLUGIN_BEACON_LAYOUT) ) {
            Aware.setSetting(this, key, sharedPreferences.getString(key, BEACON_LAYOUT_DEFAULT));
            layout.setText(sharedPreferences.getString(key, BEACON_LAYOUT_DEFAULT));
        }

        if( setting.getKey().equals(PLUGIN_BEACON_MAJOR) ) {
            Aware.setSetting(this, key, sharedPreferences.getString(key, "0"));
            major.setText(sharedPreferences.getString(key, "0"));
        }

        if( setting.getKey().equals(PLUGIN_BEACON_MINOR) ) {
            Aware.setSetting(this, key, sharedPreferences.getString(key, "0"));
            minor.setText(sharedPreferences.getString(key, "0"));
        }

        if (Aware.getSetting(this, STATUS_PLUGIN_BEACON).equals("true")) {
            Aware.startPlugin(getApplicationContext(), Plugin.PLUGIN_NAME);
        } else {
            Aware.stopPlugin(getApplicationContext(), Plugin.PLUGIN_NAME);
        }
    }
}
