/*
 *
 *  Copyright (c) 2015 SameBits UG. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.aware.plugin.beacon.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.aware.plugin.beacon.R;

import org.altbeacon.beacon.Beacon;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by vitas on 09/12/2015.
 */
public class DetectedBeaconAdapter extends BeaconAdapter<DetectedBeaconAdapter.ViewHolder> {

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_beacon_detected, parent, false);
        return new DetectedBeaconAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        DetectedBeacon beacon = (DetectedBeacon) getItem(position);
        holder.tvUuid.setText(beacon.getUUID());
        holder.tvMajor.setText(beacon.getMajor());
        holder.tvMinor.setText(beacon.getMinor());
        holder.tvName.setText((beacon.getBluetoothName() == null || beacon.getBluetoothName().isEmpty()) ? beacon.getBluetoothAddress() :
                beacon.getBluetoothName());
        holder.tvRssi.setText(String.format("%d", beacon.getRssi()));
        holder.tvTxPower.setText(String.format("%d", beacon.getTxPower()));
        holder.tvDistance.setText(String.format("%.1f", beacon.getDistance()));
        holder.tvTimeAgo.setText(beacon.getProximity());
        int color = holder.tvDistance.getContext().getColor(beacon.getProximityColor());
        holder.tvTimeAgo.setTextColor(color);
        holder.tvDistance.setTextColor(color);
        holder.tvDistanceUnit.setTextColor(color);
    }

    public void insertBeacons(Collection<Beacon> beacons) {
        Iterator<Beacon> iterator = beacons.iterator();
        while (iterator.hasNext()) {
            DetectedBeacon dBeacon = new DetectedBeacon(iterator.next());
            dBeacon.setTimeLastSeen(System.currentTimeMillis());
            this.mBeacons.put(dBeacon.getId(), dBeacon);
        }
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvUuid;
        TextView tvMajor;
        TextView tvMinor;
        TextView tvRssi;
        TextView tvName;
        TextView tvTxPower;
        TextView tvDistance;
        TextView tvDistanceUnit;
        TextView tvTimeAgo;

        ViewHolder(View itemView) {
            super(itemView);
            tvUuid = itemView.findViewById(R.id.tvUuid);
            tvMajor = itemView.findViewById(R.id.beacon_item_id2_value);
            tvMinor = itemView.findViewById(R.id.beacon_item_id3_value);
            tvRssi = itemView.findViewById(R.id.beacon_item_rssi_value);
            tvName = itemView.findViewById(R.id.beacon_item_id1_value);
            tvTxPower = itemView.findViewById(R.id.beacon_item_tx_value);
            tvDistance = itemView.findViewById(R.id.beacon_item_distance_value);
            tvDistanceUnit = itemView.findViewById(R.id.beacon_item_distance_unit);
            tvTimeAgo = itemView.findViewById(R.id.beacon_item_proximity);
        }
    }
}
