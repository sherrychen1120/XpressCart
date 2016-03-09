package com.xcart16.xpresscart;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.xcart16.xpresscart.itemclass.Item;

import java.util.List;

/**
 * Created by Jason on 3/8/2016.
 */
public class ShoppingItemAdapter extends ArrayAdapter<Item> {

    private Context context;
    private List<Item> items;

    public ShoppingItemAdapter(Context context, List<Item> items) {
        super(context, R.layout.shopping_list_item, items);
        this.context = context;
        this.items = items;
    }


}
