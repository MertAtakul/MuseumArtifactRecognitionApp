package com.example.artifactrecognitionapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static android.content.ContentValues.TAG;

import java.io.IOException;


    public class GalleryFragment extends Fragment {

        private ImageView selectedImageView;
        private TextView predictionResult;
        private TextView detailsText;

        public Button signInButton,signUpButton;

        public ImageView logo;

        public GalleryFragment() {
            // Required empty public constructor
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_gallery, container, false);

            selectedImageView = view.findViewById(R.id.imageView);
            predictionResult = view.findViewById(R.id.prediction_result);
            detailsText = view.findViewById(R.id.details);


            Bundle args = getArguments();
            if (args != null) {
                String artifactName = args.getString("artifact_name");
                predictionResult.setText(artifactName);
                String artifactDetails = args.getString("artifact_details");
                detailsText.setText(artifactDetails);

                if (args.containsKey("image_uri")) {
                    Uri imageUri = args.getParcelable("image_uri");
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                        selectedImageView.setImageBitmap(bitmap);
                        ((MainActivity) getActivity()).signInButton.setVisibility(View.GONE);
                        ((MainActivity) getActivity()).signUpButton.setVisibility(View.GONE);
                        ((MainActivity) getActivity()).mLogo.setVisibility(View.GONE);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else if (args.containsKey("image_bitmap")) {
                    Bitmap bitmap = args.getParcelable("image_bitmap");
                    selectedImageView.setImageBitmap(bitmap);
                    ((MainActivity) getActivity()).signInButton.setVisibility(View.GONE);
                    ((MainActivity) getActivity()).signUpButton.setVisibility(View.GONE);
                    ((MainActivity) getActivity()).mLogo.setVisibility(View.GONE);
                }
            }

            return view;
        }
    }

