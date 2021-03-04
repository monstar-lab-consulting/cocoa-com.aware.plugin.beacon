package com.aware.plugin.beacon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.aware.plugin.beacon.adapter.DetectedBeaconAdapter;
import com.aware.utils.IContextCard;

import org.altbeacon.beacon.Beacon;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by at-trinhnguyen2
 */
public class ContextCard implements IContextCard {

    private final BeaconUpdater beaconUpdater = new BeaconUpdater();
    private final DetectedBeaconAdapter mBeaconsAdapter = new DetectedBeaconAdapter();

    /**
     * Constructor for Stream reflection
     */
    public ContextCard() {
    }

    public View getContextCard(final Context context) {
        View card = LayoutInflater.from(context).inflate(R.layout.beacon_layout, null);

        RecyclerView recyclerView = card.findViewById(R.id.recyclerView);
        recyclerView.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(mBeaconsAdapter);

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
            mBeaconsAdapter.insertBeacons(beacons);
        }
    }
}

