package com.xcart16.xpresscart;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.xcart16.xpresscart.itemclass.Item;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Jason on 4/10/2016.
 */
public class ShoppingListAdapter extends ArrayAdapter<Item> {

    private Context context;
    private List<Item> items;
    private HashMap<Item, Boolean> map;

    public ShoppingListAdapter(Context context, List<Item> items) {
        super(context, R.layout.shopping_cart_item, items);
        this.context = context;
        this.items = items;
        map = new HashMap<>();
        for (Item item : items) {
            map.put(item, false);
        }
    }

    @Override
    public View getView (int position, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.shopping_list_item, parent, false);
        }
        final Item item = getItem(position);
        TextView tv = (TextView) view.findViewById(R.id.shopping_list_name);
        tv.setText(item.getName());
        CheckBox checkbox = (CheckBox) view.findViewById(R.id.shopping_list_checkbox);
        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                map.put(item, isChecked);
            }
        });
        checkbox.setChecked(map.get(item));
        return view;
    }
}
