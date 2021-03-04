package com.aware.plugin.beacon.adapter;/*
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

import java.text.DecimalFormat;

/**
 * Created by vitas on 09/12/2015.
 */
public final class BeaconUtil {

    public static IManagedBeacon.ProximityType getProximity(double paramDouble) {
        if (paramDouble <= 0.5D) {
            return IManagedBeacon.ProximityType.IMMEDIATE;
        }
        if ((paramDouble > 0.5D) && (paramDouble <= 3.0D)) {
            return IManagedBeacon.ProximityType.NEAR;
        }
        return IManagedBeacon.ProximityType.FAR;
    }

    public static String getProximityResourceId(IManagedBeacon.ProximityType proximityType) {
        if (proximityType == IManagedBeacon.ProximityType.IMMEDIATE) {
            return "Immediate";
        }
        if (proximityType == IManagedBeacon.ProximityType.NEAR) {
            return "Near";
        }
        return "Far";
    }

    public static boolean isInProximity(IManagedBeacon.ProximityType proximityType, double paramDouble) {
        return getProximity(paramDouble) == proximityType;
    }

    public static double getRoundedDistance(double distance) {
        return Math.ceil(distance * 100.0D) / 100.0D;
    }

    public static String getRoundedDistanceString(double distance) {
        return new DecimalFormat("##0.00").format(getRoundedDistance(distance));
    }
}
