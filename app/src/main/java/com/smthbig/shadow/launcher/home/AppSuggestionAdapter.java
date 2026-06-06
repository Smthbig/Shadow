package com.smthbig.shadow.launcher.home;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smthbig.shadow.R;

import java.util.List;

public class AppSuggestionAdapter extends RecyclerView.Adapter<AppSuggestionAdapter.ViewHolder> {

    public interface OnAppClick {
        void onClick(String pkg);
    }

    private final List<ResolveInfo> apps;
    private final OnAppClick listener;
    private final PackageManager pm;

    public AppSuggestionAdapter(android.content.Context context,
                                 List<ResolveInfo> apps,
                                 OnAppClick listener) {
        this.apps = apps;
        this.listener = listener;
        this.pm = context.getPackageManager();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app_suggestion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ResolveInfo app = apps.get(position);
        holder.icon.setImageDrawable(app.loadIcon(pm));
        holder.label.setText(app.loadLabel(pm));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(app.activityInfo.packageName);
            }
        });
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView label;

        ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.app_icon);
            label = itemView.findViewById(R.id.app_label);
        }
    }
}