package com.xcart16.xpresscart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.xcart16.xpresscart.itemclass.Item;

import java.util.List;

/**
 * Created by Jason on 3/8/2016.
 */
public class ShoppingCartAdapter extends ArrayAdapter<Item> {

    private Context context;
    private List<Item> items;

    public ShoppingCartAdapter(Context context, List<Item> items) {
        super(context, R.layout.shopping_cart_item, items);
        this.context = context;
        this.items = items;
    }

    @Override
    public View getView (int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.shopping_cart_item, parent, false);
        }
        Item item = getItem(position);
        ((TextView) view.findViewById(R.id.shopping_item_name)).setText(item.getName());
        ((TextView) view.findViewById(R.id.shopping_item_price)).setText(item.getDisplayPrice());
        if (item.getCustomQuantity() != null) {
            TextView quant = (TextView) view.findViewById(R.id.shopping_quant);
            quant.setVisibility(View.VISIBLE);
            quant.setText(item.getCustomQuantity());
        } else {
            view.findViewById(R.id.shopping_quant).setVisibility(View.GONE);
        }
        return view;
    }

    public List<Item> getList() {
        return items;
    }
}
