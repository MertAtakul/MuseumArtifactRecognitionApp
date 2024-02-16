package com.example.artifactrecognitionapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;



public class ProfileFragment extends Fragment {

    public Button logoutButton;
    public TextView userHistoryText;
    private ListView artHistoryListView;
    private UserDatabaseHelper dbHelper;
    private int userId;
    private boolean hideViews = false;

    public ProfileFragment(boolean hideViews) {
        this.hideViews = hideViews;
    }

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView welcomeTextView = view.findViewById(R.id.welcomeTextView);
        logoutButton = view.findViewById(R.id.logoutButton);
        artHistoryListView = view.findViewById(R.id.art_history_list);
        userHistoryText = view.findViewById(R.id.historyText);

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        String userName = sharedPreferences.getString("name", null);
        String userSurname = sharedPreferences.getString("surname", null);
        userId = sharedPreferences.getInt("user_id", -1);

        dbHelper = new UserDatabaseHelper(requireContext());

        if (userName != null && userSurname != null) {
            welcomeTextView.setText("Hoşgeldiniz, " + userName + " " + userSurname);
            ((MainActivity) getActivity()).signInButton.setVisibility(View.GONE);
            ((MainActivity) getActivity()).signUpButton.setVisibility(View.GONE);
            ((MainActivity) getActivity()).mLogo.setVisibility(View.GONE);
        } else {
            welcomeTextView.setText("Kullanıcı adı ve soyadı alınamadı.");
        }

        if (hideViews) {
            logoutButton.setVisibility(View.GONE);
            welcomeTextView.setVisibility(View.GONE);
        } else {
            logoutButton.setVisibility(View.VISIBLE);
            welcomeTextView.setVisibility(View.VISIBLE);
        }

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).signOut();
                }
            }
        });

        Log.d("ProfileFragment", "onViewCreated called");
        loadArtHistory();
    }

    private void loadArtHistory() {
        ArrayList<ArtHistoryItem> artHistoryItems = new ArrayList<>();
        Cursor cursor = dbHelper.getArtHistoryByUserId(userId);
        Log.d("ProfileFragment", "getArtHistoryByUserId called with userId: " + userId);

        int predictionColumnIndex = cursor.getColumnIndex(UserDatabaseHelper.COLUMN_PREDICTION);
        int imageUriColumnIndex = cursor.getColumnIndex(UserDatabaseHelper.COLUMN_IMAGE_URI);

        while (cursor.moveToNext()) {
            if (predictionColumnIndex != -1 && imageUriColumnIndex != -1) {
                String artifactName = cursor.getString(predictionColumnIndex);
                String imageUri = cursor.getString(imageUriColumnIndex);
                ArtHistoryItem item = new ArtHistoryItem(artifactName, imageUri);
                artHistoryItems.add(item);
                Log.d("ProfileFragment", "Adding item: " + artifactName + ", " + imageUri);
            }
        }

        cursor.close();

        userHistoryText.setVisibility(View.VISIBLE);

        ArtHistoryAdapter adapter = new ArtHistoryAdapter(requireContext(), artHistoryItems);
        artHistoryListView.setAdapter(adapter);
    }

}