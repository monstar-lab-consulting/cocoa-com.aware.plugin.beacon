package com.aware.plugin.beacon;

import android.Manifest;
import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SyncRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.utils.Aware_Plugin;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;

import java.util.Collection;
import java.util.List;

/**
 * Created by at-trinhnguyen2
 */
public class Plugin extends Aware_Plugin implements BeaconConsumer {

    public static final String PLUGIN_NAME = "com.aware.plugin.beacon";

    public static final String ACTION_AWARE_PLUGIN_SCAN_START = "ACTION_AWARE_PLUGIN_SCAN_START";
    public static final String ACTION_AWARE_PLUGIN_SCAN_STOP = "ACTION_AWARE_PLUGIN_SCAN_STOP";
    public static boolean IN_SCAN = false;
    public static ContextProducer contextProducer;
    private static AWAREBeaconObserver beaconObserver;
    private BeaconManager beaconManager;

    public static AWAREBeaconObserver getSensorObserver() {
        return beaconObserver;
    }

    public static void setBeaconObserver(AWAREBeaconObserver observer) {
        beaconObserver = observer;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AUTHORITY = Provider.getAuthority(this);
        TAG = "AWARE::Beacon";

        //Any active plugin/sensor shares its overall context using broadcasts
        CONTEXT_PRODUCER = new ContextProducer() {
            @Override
            public void onContext() {
                if (DEBUG) {
                    Log.d(TAG, "In scan: " + IN_SCAN);
                }
                if (IN_SCAN) {
                    sendBroadcast(new Intent(ACTION_AWARE_PLUGIN_SCAN_START));
                } else {
                    sendBroadcast(new Intent(ACTION_AWARE_PLUGIN_SCAN_STOP));
                }
            }
        };
        contextProducer = CONTEXT_PRODUCER;
        //Add permissions you need (Support for Android M) e.g.,
        REQUIRED_PERMISSIONS.add(Manifest.permission.BLUETOOTH);
        REQUIRED_PERMISSIONS.add(Manifest.permission.BLUETOOTH_ADMIN);
        REQUIRED_PERMISSIONS.add(Manifest.permission.ACCESS_FINE_LOCATION);
        REQUIRED_PERMISSIONS.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            REQUIRED_PERMISSIONS.add(Manifest.permission.FOREGROUND_SERVICE);
        }
        new BackgroundPowerSaver(this);
        setDefaultSettings();
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().clear();
        beaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout(Aware.getSetting(getApplicationContext(), Settings.PLUGIN_BEACON_LAYOUT)));
        beaconManager.setEnableScheduledScanJobs(false);
        beaconManager.setBackgroundScanPeriod(Long.parseLong(Aware.getSetting(getApplicationContext(), Settings.PLUGIN_SCAN_BACKGROUND_PERIOD)));
        beaconManager.setBackgroundBetweenScanPeriod(Long.parseLong(Aware.getSetting(getApplicationContext(), Settings.PLUGIN_SCAN_BETWEEN_BACKGROUND_PERIOD)));
        beaconManager.setForegroundBetweenScanPeriod(Long.parseLong(Aware.getSetting(getApplicationContext(), Settings.PLUGIN_SCAN_BETWEEN_FOREGROUND_PERIOD)));
        beaconManager.bind(this);
    }

    //This function gets called every 5 minutes by AWARE to make sure this plugin is still running.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if (PERMISSIONS_OK) {

            //Check if the user has toggled the debug messages
            DEBUG = Aware.getSetting(this, Aware_Preferences.DEBUG_FLAG).equals("true");

            if (Aware.getSetting(getApplicationContext(), Settings.STATUS_PLUGIN_BEACON).length() == 0) {
                Aware.setSetting(getApplicationContext(), Settings.STATUS_PLUGIN_BEACON, true);
            } else {
                if (Aware.getSetting(getApplicationContext(), Settings.STATUS_PLUGIN_BEACON).equalsIgnoreCase("false")) {
                    Aware.stopPlugin(getApplicationContext(), getPackageName());
                    return START_STICKY;
                }
            }
            setDefaultSettings();
            if (Aware.isStudy(this)) {
                Account awareAccount = Aware.getAWAREAccount(getApplicationContext());
                String authority = Provider.getAuthority(getApplicationContext());
                long frequency = Long.parseLong(Aware.getSetting(this, Aware_Preferences.FREQUENCY_WEBSERVICE)) * 60;

                ContentResolver.setIsSyncable(awareAccount, authority, 1);
                ContentResolver.setSyncAutomatically(awareAccount, authority, true);
                SyncRequest request = new SyncRequest.Builder()
                        .syncPeriodic(frequency, frequency / 3)
                        .setSyncAdapter(awareAccount, authority)
                        .setExtras(new Bundle()).build();
                ContentResolver.requestSync(request);
            }
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
        ContentResolver.setSyncAutomatically(Aware.getAWAREAccount(this), Provider.getAuthority(this), false);
        ContentResolver.removePeriodicSync(
                Aware.getAWAREAccount(this),
                Provider.getAuthority(this),
                Bundle.EMPTY
        );

        Aware.setSetting(this, Settings.STATUS_PLUGIN_BEACON, false);
    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.removeAllRangeNotifiers();
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (DEBUG)
                    Log.d(TAG, "Found " + beacons.size() + " beacon" + (beacons.size() <= 1 ? "" : "s") + ":");
                if (Plugin.getSensorObserver() != null)
                    Plugin.getSensorObserver().onScanBeacon((List<Beacon>) beacons);

                if (beacons.size() > 0) {
                    for (Beacon b : beacons) {
                        ContentValues beaconInfo = new ContentValues();
                        beaconInfo.put(Provider.BeaconData.TIMESTAMP, System.currentTimeMillis());
                        beaconInfo.put(Provider.BeaconData.DEVICE_ID, Aware.getSetting(getApplicationContext(), Aware_Preferences.DEVICE_ID));
                        beaconInfo.put(Provider.BeaconData.UUID, b.getId1().toString());
                        beaconInfo.put(Provider.BeaconData.MAJOR, b.getId2().toString());
                        beaconInfo.put(Provider.BeaconData.MINOR, b.getId3().toString());
                        beaconInfo.put(Provider.BeaconData.RSSI, b.getRssi());
                        beaconInfo.put(Provider.BeaconData.TX_POWER, b.getTxPower());
                        if (DEBUG) Log.d(TAG, String.valueOf(beaconInfo));
                        getApplicationContext().getContentResolver().insert(Provider.BeaconData.CONTENT_URI, beaconInfo);
                    }
                }
            }
        });
        try {
            String major = Aware.getSetting(getApplicationContext(), Settings.PLUGIN_BEACON_MAJOR);
            String minor = Aware.getSetting(getApplicationContext(), Settings.PLUGIN_BEACON_MINOR);
            Identifier majorIdentifier;
            Identifier minorIdentifier;
            if (major.equals("0")) {
                majorIdentifier = null;
            } else {
                majorIdentifier = Identifier.parse(major);
            }
            if (minor.equals("0")) {
                minorIdentifier = null;
            } else {
                minorIdentifier = Identifier.parse(minor);
            }
            beaconManager.startRangingBeaconsInRegion(new Region("backgroundRegion", Identifier.parse(Aware.getSetting(getApplicationContext(), Settings.PLUGIN_BEACON_UDID)),
                    majorIdentifier,
                    minorIdentifier));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void setDefaultSettings() {
        if (Aware.getSetting(this, Settings.PLUGIN_SCAN_BACKGROUND_PERIOD).length() == 0)
            Aware.setSetting(this, Settings.PLUGIN_SCAN_BACKGROUND_PERIOD, Settings.SCAN_BACKGROUND_PERIOD_DEFAULT);

        if (Aware.getSetting(this, Settings.PLUGIN_SCAN_BETWEEN_BACKGROUND_PERIOD).length() == 0)
            Aware.setSetting(this, Settings.PLUGIN_SCAN_BETWEEN_BACKGROUND_PERIOD, Settings.SCAN_BETWEEN_BACKGROUND_PERIOD_DEFAULT);

        if (Aware.getSetting(this, Settings.PLUGIN_SCAN_BETWEEN_FOREGROUND_PERIOD).length() == 0)
            Aware.setSetting(this, Settings.PLUGIN_SCAN_BETWEEN_FOREGROUND_PERIOD, Settings.SCAN_BETWEEN_FOREGROUND_PERIOD_DEFAULT);

        if (Aware.getSetting(this, Settings.PLUGIN_BEACON_UDID).length() == 0)
            Aware.setSetting(this, Settings.PLUGIN_BEACON_UDID, Settings.BEACON_UDID_DEFAULT);

        if (Aware.getSetting(this, Settings.PLUGIN_BEACON_MAJOR).length() == 0)
            Aware.setSetting(this, Settings.PLUGIN_BEACON_MAJOR, 0);

        if (Aware.getSetting(this, Settings.PLUGIN_BEACON_MINOR).length() == 0)
            Aware.setSetting(this, Settings.PLUGIN_BEACON_MINOR, 0);

        if (Aware.getSetting(this, Settings.PLUGIN_BEACON_LAYOUT).length() == 0)
            Aware.setSetting(this, Settings.PLUGIN_BEACON_LAYOUT, Settings.BEACON_LAYOUT_DEFAULT);
    }

    public interface AWAREBeaconObserver {
        void onScanBeacon(List<Beacon> data);
    }
}
