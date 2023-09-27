package com.example.nasaimageofdaysearch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.net.Uri;
import android.widget.TextView;
import android.Manifest;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Retrieve image details from Image class and displays list of images using recycler view
 * Utilize ImageDao class to delete images from database
 *
 */
public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ImageViewHolder> {
    private List<Image> images;
    private Context context;

    public ImageListAdapter(List<Image> images, Context context) {
        this.images = images != null ? images : new ArrayList<>();
        this.context = context;
    }

    // Updates the data in the adapter and refresh.
    public void updateData(List<Image> newImages) {
        this.images = newImages != null ? newImages : new ArrayList<>();
        notifyDataSetChanged();
    }

    // Creates and returns a new ViewHolder for displaying an image item in the list.
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

        holder.deleteImage.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            Image imageToDelete = images.get(currentPosition);
            new DeleteImageTask(currentPosition).execute(imageToDelete);
        });


    }
    // handle deletion of an image from storage and/or database.
    private class DeleteImageTask extends AsyncTask<Image, Void, Boolean> {
        int position;

        DeleteImageTask(int position) {
            this.position = position;
        }

        @Override
        protected Boolean doInBackground(Image... images) {
            String imagePath = images[0].getImagePath();

            if (imagePath.startsWith("content://")) {
                return deleteImageFromMediaStore(imagePath);
            } else {
                if (deleteFileFromStorage(imagePath)) {
                    NasaImageDatabase db = NasaImageDatabase.getInstance(context);
                    db.imageDAO().deleteImage(images[0]);
                    List<Image> allImages = db.imageDAO().getAllImages();

                    return true;
                } else {
                    return false;
                }
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                images.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, images.size());

                Toast.makeText(context, "Image deleted successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to delete the image. Please try again.", Toast.LENGTH_SHORT).show();
            }
        }

        private boolean deleteImageFromMediaStore(String imagePath) {
            Uri imageUri = Uri.parse(imagePath);
            int deletedRows = context.getContentResolver().delete(imageUri, null, null);
            return deletedRows > 0;
        }

        private boolean deleteFileFromStorage(String path) {
            if (path != null && !path.isEmpty()) {
                File file = new File(path);
                return file.delete();
            }
            return false;
        }

    }

    // Returns the number of  images that the adapter will display.
    @Override
    public int getItemCount() {
        return images.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView imageDate;
        TextView deleteImage;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageDate = itemView.findViewById(R.id.imageDate);
            imageView = itemView.findViewById(R.id.imageItem);
            deleteImage = itemView.findViewById(R.id.deleteImage);
        }

    }
}
