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

import android.os.Parcel;
import android.text.format.DateUtils;

import com.aware.plugin.beacon.R;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Identifier;

/**
 * Created by vitas on 09/12/2015.
 */
public class DetectedBeacon extends Beacon implements IManagedBeacon {
    public static final Creator<DetectedBeacon> CREATOR =
            new Creator<DetectedBeacon>() {
                @Override
                public DetectedBeacon createFromParcel(Parcel in) {
                    Beacon b = Beacon.CREATOR.createFromParcel(in);
                    DetectedBeacon dbeacon = new DetectedBeacon(b);
                    dbeacon.mLastSeen = in.readLong();
                    return dbeacon;
                }

                @Override
                public DetectedBeacon[] newArray(int size) {
                    return new DetectedBeacon[size];
                }
            };
    protected long mLastSeen;

    public DetectedBeacon(Beacon paramBeacon) {
        super(paramBeacon);
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        super.writeToParcel(parcel, flags);
        parcel.writeLong(mLastSeen);
    }


    @Override
    public long getTimeLastSeen() {
        return this.mLastSeen;
    }

    public void setTimeLastSeen(long lastSeen) {
        this.mLastSeen = lastSeen;
    }

    @Override
    public boolean equalTo(IManagedBeacon target) {
        return getId().equals(target.getId());
    }

    public Identifier getId2() {
        return super.getId2();
    }

    public Identifier getId3() {
        return super.getId3();
    }

    @Override
    public String getId() {
        return getUUID() + ";" + getMajor() + ";" + getMinor() + ";FF:FF:FF:FF:FF:FF"; // ";" + getBluetoothAddress();
    }

    @Override
    public int getType() {
        return getBeaconTypeCode();
    }

    @Override
    public String getUUID() {
        return getId1().toString();
    }

    @Override
    public String getMajor() {
        return getId2().toString();
    }

    @Override
    public String getMinor() {
        return getId3().toString();
    }

    @Override
    public String toString() {
        return "UUID: " + getUUID() + ", Major: " + getMajor() + ", Minor: " + getMinor() + "\n" + "RSSI: " + getRssi() + " dBm, TX: " + getTxPower() + " dBm\n" + "Distance: " + BeaconUtil.getRoundedDistance(getDistance()) + "m";
    }

    public String getSeenSince() {
        return DateUtils.getRelativeTimeSpanString(getTimeLastSeen(), System.currentTimeMillis(), 0L).toString();
    }

    public boolean isLostBeacon() {
        return ((System.currentTimeMillis() - getTimeLastSeen()) / 1000L > 5L);
    }

    public String getProximity() {
        if (isLostBeacon()) {
            return getSeenSince();
        }
        return BeaconUtil.getProximityResourceId(BeaconUtil.getProximity(getDistance()));
    }

    public int getProximityColor() {
        if (isLostBeacon()) {
            return R.color.hn_orange_dark;
        }
        return android.R.color.tab_indicator_text;
    }
}
