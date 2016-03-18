package com.xcart16.xpresscart.itemclass;

import android.graphics.Bitmap;

import com.google.android.gms.vision.barcode.Barcode;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Jason on 3/8/2016.
 */
public class Item {
    public String barcode;
    private String name;
    private double weight;
    private double price;
    private String discount;
    private Set<Item> suggestion;
    private String location;
    private Bitmap bitmap;

    public Item (String name, String barcode, double weight, double price) {
        this.barcode = barcode;
        this.name = name;
        this.weight = weight;
        this.price = price;
        suggestion = new HashSet<>();
    }

    public double getPrice() {
        return price;
    }

    public double getWeight() {
        return weight;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getName() {
        return name;
    }

    public String getDisplayPrice() {
        NumberFormat formatter = new DecimalFormat("#0.00");
        return new StringBuilder().append("$").append(formatter.format(price)).toString();
    }

    public String getDiscountedName() {
        if (discount == null) {
            return getName();
        }
        return new StringBuilder().append(name).append(" ").append(discount).toString();
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public void addSuggestion(Item item) {
        suggestion.add(item);
    }

    public Set<Item> getSuggestion() {
        return suggestion;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }
}
