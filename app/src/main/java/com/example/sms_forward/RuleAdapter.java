package com.example.sms_forward;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RuleAdapter extends RecyclerView.Adapter<RuleAdapter.RuleViewHolder> {
    private List<SMSRule.Rule> rules = new ArrayList<>();
    private final OnDeleteListener deleteListener;

    public interface OnDeleteListener {
        void onDelete(int position);
    }

    public RuleAdapter(OnDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public void setRules(List<SMSRule.Rule> rules) {
        this.rules = new ArrayList<>(rules);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rule, parent, false);
        return new RuleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RuleViewHolder holder, int position) {
        try {
            SMSRule.Rule rule = rules.get(position);
            if (rule != null) {
                String pattern = rule.getPattern();
                holder.tvPattern.setText(pattern != null ? pattern : "");
                
                String type = rule.isRegex() ? "正则表达式" : "关键词";
                holder.tvType.setText(type);
                
                holder.btnDelete.setOnClickListener(v -> {
                    if (deleteListener != null) {
                        deleteListener.onDelete(holder.getAdapterPosition());
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return rules != null ? rules.size() : 0;
    }

    static class RuleViewHolder extends RecyclerView.ViewHolder {
        TextView tvPattern;
        TextView tvType;
        ImageButton btnDelete;

        RuleViewHolder(View itemView) {
            super(itemView);
            tvPattern = itemView.findViewById(R.id.tvPattern);
            tvType = itemView.findViewById(R.id.tvType);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}