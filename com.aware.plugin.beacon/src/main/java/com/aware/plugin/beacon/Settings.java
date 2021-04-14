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

    //Plugin settings in XML @xml/preferences_beacon
    public static final String STATUS_PLUGIN_BEACON = "status_plugin_beacon";
    public static final String PLUGIN_SCAN_PERIOD = "plugin_scan_period";
    public static final String PLUGIN_SCAN_BETWEEN_PERIOD = "plugin_scan_between_period";
    public static final String PLUGIN_BEACON_UUID = "plugin_beacon_uuid";
    public static final String PLUGIN_BEACON_MAJOR = "plugin_beacon_major";
    public static final String PLUGIN_BEACON_MINOR = "plugin_beacon_minor";
    public static final String PLUGIN_BEACON_LAYOUT = "plugin_beacon_layout";
    public static final String PLUGIN_BEACON_WORKING_TIME = "plugin_beacon_working_time";
    public static final String PLUGIN_NUMBER_CONTACT_ALERT = "plugin_number_contact_alert";
    public static final String PLUGIN_GET_POSITIVES_INTERVAL = "plugin_get_positives_interval";
    public static final String PLUGIN_CONTACT_CONTINUATION_INTERVAL = "plugin_contact_continuation_interval";
    public static final String PLUGIN_CONTACT_DENSITY_INTERVAL = "plugin_contact_density_interval";
    public static final String PLUGIN_DATE_CREATED = "plugin_date_created";

    public static final String SCAN_PERIOD_DEFAULT = "1100";
    public static final String SCAN_BETWEEN_PERIOD_DEFAULT = "2000";
    public static final String BEACON_UUID_DEFAULT = "";
    public static final String BEACON_MAJOR_DEFAULT = "";
    public static final String BEACON_MINOR_DEFAULT = "";
    public static final String BEACON_LAYOUT_DEFAULT = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24";
    public static final String NUMBER_CONTACT_ALERT_DEFAULT = "15";
    public static final String GET_POSITIVES_INTERVAL_DEFAULT = "600000";
    public static final String CONTACT_CONTINUATION_INTERVAL_DEFAULT = "300000";
    public static final String CONTACT_DENSITY_INTERVAL_DEFAULT = "600000";
    public static final String CONTACT_DATE_CREATED_DEFAULT = "2021-01-01 12:00:00";

    //Plugin settings UI elements
    private static CheckBoxPreference status;
    private static EditTextPreference scanPeriod, scanBetweenPeriod, uuid, layout, major, minor, workingTime, noContactAlert, getPositivesInterval, contactContinuationInterval, contactDensityInterval, dateCreated;

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

        scanPeriod = (EditTextPreference) findPreference(PLUGIN_SCAN_PERIOD);
        if (Aware.getSetting(this, PLUGIN_SCAN_PERIOD).length() == 0) {
            Aware.setSetting(this, PLUGIN_SCAN_PERIOD, SCAN_PERIOD_DEFAULT);
        }
        scanPeriod.setText(Aware.getSetting(this, PLUGIN_SCAN_PERIOD));

        scanBetweenPeriod = (EditTextPreference) findPreference(PLUGIN_SCAN_BETWEEN_PERIOD);
        if (Aware.getSetting(this, PLUGIN_SCAN_BETWEEN_PERIOD).length() == 0) {
            Aware.setSetting(this, PLUGIN_SCAN_BETWEEN_PERIOD, SCAN_BETWEEN_PERIOD_DEFAULT);
        }
        scanBetweenPeriod.setText(Aware.getSetting(this, PLUGIN_SCAN_BETWEEN_PERIOD));

        uuid = (EditTextPreference) findPreference(PLUGIN_BEACON_UUID);
        if (Aware.getSetting(this, PLUGIN_BEACON_UUID).length() == 0) {
            Aware.setSetting(this, PLUGIN_BEACON_UUID, BEACON_UUID_DEFAULT);
        }
        uuid.setText(Aware.getSetting(this, PLUGIN_BEACON_UUID));

        layout = (EditTextPreference) findPreference(PLUGIN_BEACON_LAYOUT);
        if (Aware.getSetting(this, PLUGIN_BEACON_LAYOUT).length() == 0) {
            Aware.setSetting(this, PLUGIN_BEACON_LAYOUT, BEACON_LAYOUT_DEFAULT);
        }
        layout.setText(Aware.getSetting(this, PLUGIN_BEACON_LAYOUT));

        major = (EditTextPreference) findPreference(PLUGIN_BEACON_MAJOR);
        if (Aware.getSetting(this, PLUGIN_BEACON_MAJOR).length() == 0) {
            Aware.setSetting(this, PLUGIN_BEACON_MAJOR, BEACON_MAJOR_DEFAULT);
        }
        major.setText(Aware.getSetting(this, PLUGIN_BEACON_MAJOR));

        minor = (EditTextPreference) findPreference(PLUGIN_BEACON_MINOR);
        if (Aware.getSetting(this, PLUGIN_BEACON_MINOR).length() == 0) {
            Aware.setSetting(this, PLUGIN_BEACON_MINOR, BEACON_MINOR_DEFAULT);
        }
        minor.setText(Aware.getSetting(this, PLUGIN_BEACON_MINOR));

        workingTime = (EditTextPreference) findPreference(PLUGIN_BEACON_WORKING_TIME);
        if (Aware.getSetting(this, PLUGIN_BEACON_WORKING_TIME).length() == 0) {
            Aware.setSetting(this, PLUGIN_BEACON_WORKING_TIME, getString(R.string.default_working_time_json));
        }
        workingTime.setText(Aware.getSetting(this, PLUGIN_BEACON_WORKING_TIME));

        noContactAlert = (EditTextPreference) findPreference(PLUGIN_NUMBER_CONTACT_ALERT);
        if (Aware.getSetting(this, PLUGIN_NUMBER_CONTACT_ALERT).length() == 0) {
            Aware.setSetting(this, PLUGIN_NUMBER_CONTACT_ALERT, NUMBER_CONTACT_ALERT_DEFAULT);
        }
        noContactAlert.setText(Aware.getSetting(this, PLUGIN_NUMBER_CONTACT_ALERT));

        getPositivesInterval = (EditTextPreference) findPreference(PLUGIN_GET_POSITIVES_INTERVAL);
        if (Aware.getSetting(this, PLUGIN_GET_POSITIVES_INTERVAL).length() == 0) {
            Aware.setSetting(this, PLUGIN_GET_POSITIVES_INTERVAL, GET_POSITIVES_INTERVAL_DEFAULT);
        }
        getPositivesInterval.setText(Aware.getSetting(this, PLUGIN_GET_POSITIVES_INTERVAL));

        contactContinuationInterval = (EditTextPreference) findPreference(PLUGIN_CONTACT_CONTINUATION_INTERVAL);
        if (Aware.getSetting(this, PLUGIN_CONTACT_CONTINUATION_INTERVAL).length() == 0) {
            Aware.setSetting(this, PLUGIN_CONTACT_CONTINUATION_INTERVAL, CONTACT_CONTINUATION_INTERVAL_DEFAULT);
        }
        contactContinuationInterval.setText(Aware.getSetting(this, PLUGIN_CONTACT_CONTINUATION_INTERVAL));

        contactDensityInterval = (EditTextPreference) findPreference(PLUGIN_CONTACT_DENSITY_INTERVAL);
        if (Aware.getSetting(this, PLUGIN_CONTACT_DENSITY_INTERVAL).length() == 0) {
            Aware.setSetting(this, PLUGIN_CONTACT_DENSITY_INTERVAL, CONTACT_DENSITY_INTERVAL_DEFAULT);
        }
        contactDensityInterval.setText(Aware.getSetting(this, PLUGIN_CONTACT_DENSITY_INTERVAL));

        dateCreated = (EditTextPreference) findPreference(PLUGIN_DATE_CREATED);
        if (Aware.getSetting(this, PLUGIN_DATE_CREATED).length() == 0) {
            Aware.setSetting(this, PLUGIN_DATE_CREATED, CONTACT_DATE_CREATED_DEFAULT);
        }
        dateCreated.setText(Aware.getSetting(this, PLUGIN_DATE_CREATED));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference setting = findPreference(key);

        if (setting.getKey().equals(STATUS_PLUGIN_BEACON)) {
            Aware.setSetting(this, key, sharedPreferences.getBoolean(key, false));
            status.setChecked(sharedPreferences.getBoolean(key, false));
        }

        if (setting.getKey().equals(PLUGIN_SCAN_PERIOD)) {
            Aware.setSetting(this, key, sharedPreferences.getString(key, SCAN_PERIOD_DEFAULT));
            scanPeriod.setText(String.valueOf(sharedPreferences.getString(key, SCAN_PERIOD_DEFAULT)));
        }

        if (setting.getKey().equals(PLUGIN_SCAN_BETWEEN_PERIOD)) {
            Aware.setSetting(this, key, sharedPreferences.getString(key, SCAN_BETWEEN_PERIOD_DEFAULT));
            scanBetweenPeriod.setText(String.valueOf(sharedPreferences.getString(key, SCAN_BETWEEN_PERIOD_DEFAULT)));
        }

        if (setting.getKey().equals(PLUGIN_BEACON_UUID)) {
            Aware.setSetting(this, key, sharedPreferences.getString(key, BEACON_UUID_DEFAULT));
            uuid.setText(sharedPreferences.getString(key, BEACON_UUID_DEFAULT));
        }

        if (setting.getKey().equals(PLUGIN_BEACON_LAYOUT)) {
            Aware.setSetting(this, key, sharedPreferences.getString(key, BEACON_LAYOUT_DEFAULT));
            layout.setText(sharedPreferences.getString(key, BEACON_LAYOUT_DEFAULT));
        }

        if (setting.getKey().equals(PLUGIN_BEACON_MAJOR)) {
            Aware.setSetting(this, key, sharedPreferences.getString(key, Settings.BEACON_MAJOR_DEFAULT));
            major.setText(sharedPreferences.getString(key, BEACON_MAJOR_DEFAULT));
        }

        if (setting.getKey().equals(PLUGIN_BEACON_MINOR)) {
            Aware.setSetting(this, key, sharedPreferences.getString(key, Settings.BEACON_MINOR_DEFAULT));
            minor.setText(sharedPreferences.getString(key, BEACON_MINOR_DEFAULT));
        }

        if (Aware.getSetting(this, STATUS_PLUGIN_BEACON).equals("true")) {
            Aware.startPlugin(getApplicationContext(), Plugin.PLUGIN_NAME);
        } else {
            Aware.stopPlugin(getApplicationContext(), Plugin.PLUGIN_NAME);
        }
    }
}
