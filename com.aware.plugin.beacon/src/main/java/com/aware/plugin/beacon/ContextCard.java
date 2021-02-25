package com.aware.plugin.beacon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.aware.utils.IContextCard;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Created by at-trinhnguyen2
 */
public class ContextCard implements IContextCard {

    private TextView tvBeaconInfo;
    private BeaconUpdater beaconUpdater = new BeaconUpdater();

    /**
     * Constructor for Stream reflection
     */
    public ContextCard() {
    }

    public View getContextCard(final Context context) {
        View card = LayoutInflater.from(context).inflate(R.layout.beacon_layout, null);

        tvBeaconInfo = card.findViewById(R.id.tvBeaconInfo);
        tvBeaconInfo.setText("Scan Beacon ...");

        Plugin.setBeaconObserver(new Plugin.AWAREBeaconObserver() {
            @Override
            public void onScanBeacon(List<Beacon> data) {
                context.sendBroadcast(new Intent("BEACON").putParcelableArrayListExtra("data", (ArrayList<? extends Parcelable>) data));
            }
        });

        IntentFilter filter = new IntentFilter("BEACON");
        context.registerReceiver(beaconUpdater, filter);

        return card;
    }

    public class BeaconUpdater extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<Beacon> beacons = intent.getParcelableArrayListExtra("data");
            final StringBuilder message = new StringBuilder();
            message.append("Found ").append(beacons.size()).append(" beacon").append(beacons.size() <= 1 ? "" : "s").append(":").append("\n");
            message.append("\n");
            beacons.forEach(new Consumer<Beacon>() {
                @Override
                public void accept(Beacon beacon) {
                    message.append(beacon.getId1()).append("\n");
                    message.append("Major: ").append(beacon.getId2()).append("  Minor: ").append(beacon.getId3()).append("\n");
                    message.append("RSSI: ").append(beacon.getRssi()).append("  TxPower: ").append(beacon.getTxPower()).append("\n");
                    message.append("Distance: ").append(String.format("%.1f", beacon.getDistance())).append("m").append("\n");
                    message.append("\n");
                }
            });
            tvBeaconInfo.setText(message);
        }
    }
}

