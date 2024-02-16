package com.example.artifactrecognitionapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.Rotate;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class ArtHistoryAdapter extends ArrayAdapter<ArtHistoryItem> {
    private Context context;
    private List<ArtHistoryItem> artHistoryItems;

    public ArtHistoryAdapter(@NonNull Context context, List<ArtHistoryItem> list) {
        super(context, 0, list);
        this.context = context;
        this.artHistoryItems = list;
    }

    @NonNull
    // eski getview kodu
    /*@Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.art_history_item, parent, false);
        }

        ArtHistoryItem currentItem = getItem(position);
        TextView artifactNameTextView = convertView.findViewById(R.id.artifact_name_text_view);
        ImageView artifactImageView = convertView.findViewById(R.id.artifact_image_view);

        if (currentItem != null) {
            artifactNameTextView.setText(currentItem.getArtifactName());

            Bitmap imageBitmap = currentItem.getImageBitmap();
            if (imageBitmap != null) {
                artifactImageView.setImageBitmap(imageBitmap);
            } else {
                Log.d("ArtHistoryAdapter", "Image Bitmap is null for position: " + position);
            }
        } else {
            Log.d("ArtHistoryAdapter", "Current item is null for position: " + position);
        }

        return convertView;
    }*/


    //glide kütüphanesi ile gösterme
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = convertView;

        if (listItem == null) {
            listItem = LayoutInflater.from(context).inflate(R.layout.art_history_item, parent, false);
        }

        ArtHistoryItem currentItem = getItem(position);

        TextView artifactNameTextView = listItem.findViewById(R.id.artifact_name_text_view);
        ImageView artifactImageView = listItem.findViewById(R.id.artifact_image_view);

        artifactNameTextView.setText(currentItem.getArtifactName());

        Glide.with(context)
                .load(Uri.parse(currentItem.getImageUri()))
                .placeholder(R.drawable.sample_image) // Eğer varsa, bir yer tutucu görüntü atayın.
                .error(R.drawable.error_image) // Eğer varsa, bir hata görüntüsü atayın.
                .into(artifactImageView);

        return listItem;
    }

}


