package com.xcart16.xpresscart.itemclass;

import com.google.android.gms.vision.barcode.Barcode;

/**
 * Created by Jason on 3/7/2016.
 */
public class BarcodeResult implements Result {

    public Barcode barcode;

    public BarcodeResult (Barcode barcode) {
        this.barcode = barcode;
    }
}
