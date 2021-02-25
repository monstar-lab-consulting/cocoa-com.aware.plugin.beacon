package com.aware.plugin.beacon;

import com.aware.utils.Aware_Plugin;

import org.altbeacon.beacon.BeaconConsumer;

public class Plugin extends Aware_Plugin implements BeaconConsumer {

    public static final String PLUGIN_NAME = "com.aware.plugin.beacon";

    @Override
    public void onBeaconServiceConnect() {

    }
}
