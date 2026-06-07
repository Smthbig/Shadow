package com.smthbig.shadow.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textview.MaterialTextView;
import com.smthbig.shadow.R;
import com.smthbig.shadow.theme.ThemeMode;

public class ThemeAdapter extends BaseAdapter {

    private final Context context;
    private final String[] names;
    private final String[] values;
    private String selected;

    private final OnThemeClick listener;

    public interface OnThemeClick {
        void onClick(String value);
    }

    public ThemeAdapter(Context context,
                        String[] names,
                        String[] values,
                        String selected,
                        OnThemeClick listener) {

        this.context = context;
        this.names = names;
        this.values = values;
        this.selected = selected;
        this.listener = listener;
    }

    @Override
    public int getCount() {
        return names.length;
    }

    @Override
    public Object getItem(int i) {
        return values[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context)
                    .inflate(R.layout.item_theme_option, parent, false);

            holder = new ViewHolder();
            holder.root = convertView.findViewById(R.id.root);
            holder.title = convertView.findViewById(R.id.title);
            holder.subtitle = convertView.findViewById(R.id.subtitle);
            holder.radio = convertView.findViewById(R.id.radio);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String value = values[i];
        boolean isSelected = value.equals(selected);

        /* ---------- TEXT ---------- */

        holder.title.setText(names[i]);
        holder.subtitle.setText(getSubtitle(value));

        /* ---------- RADIO ---------- */

        holder.radio.setChecked(isSelected);

        /* ---------- VISUAL STATE ---------- */

        if (isSelected) {
            holder.root.setStrokeWidth((int) (2 * context.getResources().getDisplayMetrics().density));
            holder.root.setStrokeColor(context.getColor(R.color.md_primary));
        } else {
            holder.root.setStrokeWidth((int) (1 * context.getResources().getDisplayMetrics().density));
            holder.root.setStrokeColor(context.getColor(R.color.md_outline));
        }

        /* ---------- CLICK ---------- */

        convertView.setOnClickListener(v -> {
            if (!value.equals(selected)) {
                selected = value;
                notifyDataSetChanged();

                if (listener != null) {
                    listener.onClick(value);
                }
            }
        });

        return convertView;
    }

    /* ---------- VIEW HOLDER ---------- */

    static class ViewHolder {
        MaterialCardView root;
        MaterialTextView title;
        MaterialTextView subtitle;
        MaterialRadioButton radio;
    }

    /* ---------- SUBTITLE ---------- */

    private String getSubtitle(String value) {

        switch (value) {

            case ThemeMode.SYSTEM:
                return "Follows device theme";

            case ThemeMode.LIGHT:
                return "Always light mode";

            case ThemeMode.DARK:
                return "Always dark mode";

            default:
                return "";
        }
    }
}