package com.smthbig.shadow.launcher;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.smthbig.shadow.R;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class AppLimitAdapter extends BaseAdapter {

    private final Context context;
    private final List<AppItem> list;

    public AppLimitAdapter(Context context, List<AppItem> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() { return list.size(); }

    @Override
    public Object getItem(int i) { return list.get(i); }

    @Override
    public long getItemId(int i) { return i; }

    @Override
    public View getView(int i, View v, ViewGroup parent) {

        if (v == null) {
            v = LayoutInflater.from(context)
                    .inflate(R.layout.item_app_limit, parent, false);
        }

        AppItem item = list.get(i);

        TextView name = v.findViewById(R.id.app_name);
        TextView limit = v.findViewById(R.id.app_limit);

        name.setText(item.label);

        String text;

        if (item.limitMs == -1) {
            text = "Unlimited";
        } else if (item.limitMs == 0) {
            text = "No limit";
        } else {
            text = TimeUnit.MILLISECONDS.toMinutes(item.limitMs) + " min";
        }

        limit.setText(text);

        return v;
    }
}