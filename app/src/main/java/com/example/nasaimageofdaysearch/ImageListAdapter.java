package com.example.nasaimageofdaysearch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.net.Uri;
import android.widget.TextView;
import java.io.InputStream;
import java.io.IOException;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ImageViewHolder> {
    private List<Image> images;
    private Context context;
    public ImageListAdapter(List<Image> images, Context context) {
        this.images = images != null ? images : new ArrayList<>();
        this.context = context;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_item, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Image currentImage = images.get(position);
        Log.d("ImageListAdapter", "Binding Image with ID: " + currentImage.getId() + ", Date: " + currentImage.getDate());

        if (currentImage != null && currentImage.getImagePath() != null) {
            String imagePath = currentImage.getImagePath();
            Log.d("ImageListAdapter", "Image Path: " + imagePath);

            Bitmap bitmap = null;
            if (imagePath.startsWith("content://")) {
                try {
                    InputStream is = context.getContentResolver().openInputStream(Uri.parse(imagePath));
                    bitmap = BitmapFactory.decodeStream(is);
                    if (is != null) is.close();
                } catch (IOException e) {
                    Log.e("ImageListAdapter", "Error reading image from content URI: " + e.getMessage());
                }
            } else {
                bitmap = BitmapFactory.decodeFile(imagePath);
            }
            if (bitmap != null) {
                holder.imageView.setImageBitmap(bitmap);
            } else {
                Log.e("ImageListAdapter", "Failed to decode bitmap from path: " + imagePath);
                holder.imageView.setVisibility(View.GONE);
            }
        } else {
            Log.e("ImageListAdapter", "Image or ImagePath is null");
            holder.imageView.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(v -> {
        });
        holder.imageDate.setText(currentImage.getDate());
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView imageDate;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageDate = itemView.findViewById(R.id.imageDate);
            imageView = itemView.findViewById(R.id.imageItem);
        }

    }
}
