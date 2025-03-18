package com.example.rompe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ScoreAdapter extends RecyclerView.Adapter<ScoreAdapter.ViewHolder> {

    private List<Score> scores;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPosition;
        TextView tvName;
        TextView tvTime;
        TextView tvMoves;

        public ViewHolder(View itemView) {
            super(itemView);
            tvPosition = itemView.findViewById(R.id.tvPosition);
            tvName = itemView.findViewById(R.id.tvName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvMoves = itemView.findViewById(R.id.tvMoves);
        }
    }

    public ScoreAdapter(List<Score> scores) {
        this.scores = scores;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_score, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Score score = scores.get(position);
        holder.tvPosition.setText(String.format("%d°", position + 1));
        holder.tvName.setText(score.getName());
        holder.tvTime.setText(String.format("%02d:%02d",
                score.getTime() / 60, score.getTime() % 60));
        holder.tvMoves.setText(String.format("%d movimientos", score.getMoves()));
    }

    @Override
    public int getItemCount() {
        return scores.size();
    }

    public void updateData(List<Score> newScores) {
        scores = newScores;
        notifyDataSetChanged();
    }
}