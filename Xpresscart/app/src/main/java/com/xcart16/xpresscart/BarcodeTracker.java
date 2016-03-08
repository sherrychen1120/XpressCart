package com.xcart16.xpresscart;

import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

/**
 * Created by Jason on 3/7/2016.
 */
public class BarcodeTracker extends Tracker<Barcode> {

    private CallBack callback;

    private BarcodeTracker() {

    }

    public BarcodeTracker(CallBack callback) {
        this.callback = callback;
    }
    @Override
    public void onUpdate(Detector.Detections<Barcode> detectionResults, Barcode barcode) {
        // Access detected barcode values
        if (barcode != null) {
            callback.callBack(MainActivity.BARCODE_TRACKER_ID, new BarcodeResult(barcode));
        } else if (detectionResults.getDetectedItems().size() > 0) {
            callback.callBack(MainActivity.BARCODE_TRACKER_ID, new BarcodeResult(detectionResults.getDetectedItems().valueAt(0)));
        }
    }
}