package com.example.artifactrecognitionapp;

import android.graphics.Bitmap;

public class ArtHistoryItem {
    private String artifactName;
    private String imageUri;
    private Bitmap imageBitmap;

    public ArtHistoryItem(String artifactName, String imageUri) {
        this.artifactName = artifactName;
        this.imageUri = imageUri;
    }

    public ArtHistoryItem(String artifactName) {
        this.artifactName = artifactName;
    }

    public String getArtifactName() {
        return artifactName;
    }

    public void setArtifactName(String artifactName) {
        this.artifactName = artifactName;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
    }
}
