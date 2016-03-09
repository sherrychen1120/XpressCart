package com.xcart16.xpresscart.itemclass;

import com.google.android.gms.vision.barcode.Barcode;

/**
 * Created by Jason on 3/8/2016.
 */
public class Item {
    public Barcode barcode;
    private String name;
    private double weight;
    private double price;

    public Item (String name, Barcode barcode, double weight, double price) {
        this.barcode = barcode;
        this.name = name;
        this.weight = weight;
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

    public double getWeight() {
        return weight;
    }

    public String getName() {
        return name;
    }
}
