package com.aware.plugin.beacon;

import android.Manifest;
import android.accounts.Account;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SyncRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.aware.Aware;
import com.aware.Aware_Preferences;
import com.aware.Bluetooth;
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
    private static final String ACTION_AWARE_ENABLE_BT = "ACTION_AWARE_ENABLE_BT";
    public static boolean IN_SCAN = false;
    public static ContextProducer contextProducer;
    private static AWAREBeaconObserver beaconObserver;
    private static NotificationManager notificationManager = null;
    private static Intent enableBluetoothIntent = null;
    private static BluetoothAdapter bluetoothAdapter;
    private final BluetoothBroadcaster bluetoothMonitor = new BluetoothBroadcaster();
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
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        bluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(bluetoothMonitor, filter);
        enableBluetoothIntent = new Intent(this, Bluetooth.class);
        enableBluetoothIntent.putExtra("action", ACTION_AWARE_ENABLE_BT);

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

            if (intent != null && intent.hasExtra("action") && intent.getStringExtra("action").equalsIgnoreCase(ACTION_AWARE_ENABLE_BT)) {
                Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                enableBT.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(enableBT);
            }
            if (bluetoothAdapter == null) {
                if (Aware.DEBUG) Log.w(TAG, "No bluetooth is detected on this device");
                stopSelf();
            } else {
                if (!bluetoothAdapter.isEnabled()) {
                    notifyMissingBluetooth(getApplicationContext(), false);
                }
            }

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
        unregisterReceiver(bluetoothMonitor);
        if (bluetoothAdapter != null) {
            notificationManager.cancel(123);
        }
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

    private void notifyMissingBluetooth(Context c, boolean dismiss) {
        if (!dismiss) {
            //Remind the user that we need Bluetooth on for data collection
            NotificationCompat.Builder builder = new NotificationCompat.Builder(c, Aware.AWARE_NOTIFICATION_CHANNEL_GENERAL)
                    .setSmallIcon(com.aware.R.drawable.ic_stat_aware_accessibility)
                    .setContentTitle(getApplicationContext().getResources().getString(R.string.bluetooth_needed_notification_title))
                    .setContentText(getApplicationContext().getResources().getString(R.string.bluetooth_needed_notification_text))
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setOnlyAlertOnce(true)
                    .setAutoCancel(true)
                    .setContentIntent(PendingIntent.getService(c, 123, enableBluetoothIntent, PendingIntent.FLAG_UPDATE_CURRENT));

            builder = Aware.setNotificationProperties(builder, Aware.AWARE_NOTIFICATION_IMPORTANCE_GENERAL);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                builder.setChannelId(Aware.AWARE_NOTIFICATION_CHANNEL_GENERAL);

            try {
                notificationManager.notify(123, builder.build());
            } catch (NullPointerException e) {
                if (Aware.DEBUG) Log.d(Aware.TAG, "Notification exception: " + e);
            }
        } else {
            try {
                notificationManager.cancel(123);
            } catch (NullPointerException e) {
                if (Aware.DEBUG) Log.d(Aware.TAG, "Notification exception: " + e);
            }
        }
    }

    public class BluetoothBroadcaster extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                        == BluetoothAdapter.STATE_OFF) {
                    notifyMissingBluetooth(context.getApplicationContext(), false);

                } else if (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                        == BluetoothAdapter.STATE_ON) {
                    notifyMissingBluetooth(context.getApplicationContext(), true);
                }
            }
        }
    }
}
