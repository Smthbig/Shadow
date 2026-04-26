package com.smthbig.shadow.launcher.apps;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
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

        ViewHolder holder;

        if (v == null) {
            v = LayoutInflater.from(context)
                    .inflate(R.layout.item_app_limit, parent, false);
            holder = new ViewHolder();
            holder.icon = v.findViewById(R.id.app_icon);
            holder.name = v.findViewById(R.id.app_name);
            holder.limit = v.findViewById(R.id.app_limit);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        AppItem item = list.get(i);

        holder.name.setText(item.label);
        
        try {
            PackageManager pm = context.getPackageManager();
            Drawable icon = pm.getApplicationIcon(item.packageName);
            holder.icon.setImageDrawable(icon);
            holder.icon.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            holder.icon.setVisibility(View.GONE);
        }

        String text;

        if (item.limitMs == -1) {
            text = "Unlimited";
        } else if (item.limitMs == 0) {
            text = "No limit";
        } else {
            text = TimeUnit.MILLISECONDS.toMinutes(item.limitMs) + " min";
        }

        holder.limit.setText(text);

        return v;
    }

    private static class ViewHolder {
        ImageView icon;
        TextView name;
        TextView limit;
    }
}
