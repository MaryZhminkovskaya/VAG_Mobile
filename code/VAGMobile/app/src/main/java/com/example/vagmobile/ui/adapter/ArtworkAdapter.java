package com.example.vagmobile.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.vagmobile.R;
import com.example.vagmobile.model.Artwork;
import java.util.List;

public class ArtworkAdapter extends RecyclerView.Adapter<ArtworkAdapter.ArtworkViewHolder> {
    private List<Artwork> artworkList;
    private OnArtworkClickListener onArtworkClickListener;

    public ArtworkAdapter(List<Artwork> artworkList, OnArtworkClickListener onArtworkClickListener) {
        this.artworkList = artworkList;
        this.onArtworkClickListener = onArtworkClickListener;
    }

    @NonNull
    @Override
    public ArtworkViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_artwork, parent, false);
        return new ArtworkViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArtworkViewHolder holder, int position) {
        Artwork artwork = artworkList.get(position);
        holder.bind(artwork);

        holder.itemView.setOnClickListener(v -> {
            if (onArtworkClickListener != null) {
                onArtworkClickListener.onArtworkClick(artwork);
            }
        });
    }

    @Override
    public int getItemCount() {
        return artworkList != null ? artworkList.size() : 0;
    }

    public void updateData(List<Artwork> newArtworkList) {
        this.artworkList = newArtworkList;
        notifyDataSetChanged();
    }

    static class ArtworkViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivArtwork;
        private TextView tvTitle, tvArtist, tvLikes, tvCategories;

        public ArtworkViewHolder(@NonNull View itemView) {
            super(itemView);
            ivArtwork = itemView.findViewById(R.id.ivArtwork);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvArtist = itemView.findViewById(R.id.tvArtist);
            tvLikes = itemView.findViewById(R.id.tvLikes);
            tvCategories = itemView.findViewById(R.id.tvCategories); // Добавлено для категорий
        }

        public void bind(Artwork artwork) {
            tvTitle.setText(artwork.getTitle());
            tvLikes.setText(String.valueOf(artwork.getLikes()));

            if (artwork.hasCategories()) {
                tvCategories.setText(artwork.getCategoriesString());
                tvCategories.setVisibility(View.VISIBLE);
            } else {
                tvCategories.setVisibility(View.GONE);
            }

            if (artwork.getUser() != null && artwork.getUser().getUsername() != null) {
                tvArtist.setText(artwork.getUser().getUsername());
            } else {
                tvArtist.setText("Неизвестный художник");
            }

            if (artwork.getImagePath() != null && !artwork.getImagePath().isEmpty()) {
                String imagePath = artwork.getImagePath();
                if (imagePath.startsWith("/")) {
                    imagePath = imagePath.substring(1);
                }
                String imageUrl = "http://192.168.0.51:8080/vag/uploads/" + imagePath;
                System.out.println("ArtworkAdapter: Loading image from URL: " + imageUrl);
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_placeholder)
                        .error(R.drawable.ic_error)
                        .into(ivArtwork);
            } else {
                System.out.println("ArtworkAdapter: ImagePath is null or empty for artwork: " + artwork.getTitle());
            }
        }
    }

    public interface OnArtworkClickListener {
        void onArtworkClick(Artwork artwork);
    }
}