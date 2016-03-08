package com.xcart16.xpresscart;

import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

/**
 * Created by Jason on 3/7/2016.
 */
public class BarcodeTrackerFactory implements MultiProcessor.Factory<Barcode> {

    private CallBack callback;

    private BarcodeTrackerFactory(){

    }

    public BarcodeTrackerFactory(CallBack callback) {
        this.callback = callback;
    }

    @Override
    public Tracker<Barcode> create(Barcode barcode) {
        return new BarcodeTracker(callback);
    }
}