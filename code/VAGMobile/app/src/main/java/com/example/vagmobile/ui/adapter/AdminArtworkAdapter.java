package com.example.vagmobile.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.vagmobile.R;
import com.example.vagmobile.model.Artwork;

import java.util.List;

public class AdminArtworkAdapter extends RecyclerView.Adapter<AdminArtworkAdapter.ViewHolder> {

    private List<Artwork> artworkList;
    private ArtworkActionListener actionListener;

    public interface ArtworkActionListener {
        void onApprove(Artwork artwork);
        void onReject(Artwork artwork);
    }

    public AdminArtworkAdapter(List<Artwork> artworkList, ArtworkActionListener actionListener) {
        this.artworkList = artworkList;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_artwork, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Artwork artwork = artworkList.get(position);

        holder.tvTitle.setText(artwork.getTitle());
        holder.tvDescription.setText(artwork.getDescription());
        holder.tvStatus.setText("Status: " + artwork.getStatus());
        
        if (artwork.getUser() != null) {
            holder.tvArtist.setText("By: " + artwork.getUser().getUsername());
        }

        // Загрузка изображения
        if (artwork.getImagePath() != null && !artwork.getImagePath().isEmpty()) {
            String imagePath = artwork.getImagePath();
            if (imagePath.startsWith("/")) {
                imagePath = imagePath.substring(1);
            }
            String imageUrl = "http://192.168.0.51:8080/vag/uploads/" + imagePath;
            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(holder.ivArtwork);
        }

        // Показываем кнопки в зависимости от статуса
        if ("PENDING".equals(artwork.getStatus())) {
            holder.btnApprove.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.VISIBLE);
        } else if ("APPROVED".equals(artwork.getStatus())) {
            holder.btnApprove.setVisibility(View.GONE);
            holder.btnReject.setVisibility(View.VISIBLE);
        } else if ("REJECTED".equals(artwork.getStatus())) {
            holder.btnApprove.setVisibility(View.VISIBLE);
            holder.btnReject.setVisibility(View.GONE);
        }

        holder.btnApprove.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onApprove(artwork);
            }
        });

        holder.btnReject.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onReject(artwork);
            }
        });
    }

    @Override
    public int getItemCount() {
        return artworkList != null ? artworkList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivArtwork;
        TextView tvTitle, tvDescription, tvStatus, tvArtist;
        Button btnApprove, btnReject;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivArtwork = itemView.findViewById(R.id.ivArtwork);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            btnApprove = itemView.findViewById(R.id.btnApprove);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }
}

