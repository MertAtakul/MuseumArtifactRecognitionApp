package com.example.artifactrecognitionapp;

import static android.view.View.GONE;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

import com.google.gson.annotations.SerializedName;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import com.example.artifactrecognitionapp.UserDatabaseHelper;


// notlar : main xmlde frame layout idsi camera_containerdı

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_GALLERY_IMAGE = 2;
    private static final int PERMISSION_REQUEST_CODE = 200;

    private ApiService apiService;

    private UserDatabaseHelper userDatabaseHelper;              // db
    private SharedPreferences sharedPreferences;        // db

    public Button signInButton,signUpButton;

    public ImageView mLogo;

    private boolean isLoggedIn = false;




    // her iki cihazda da test etmek için servera bağlanma url methodu
    private String getBaseUrl() {
        String baseUrl;
        if (Build.FINGERPRINT.contains("generic") || Build.FINGERPRINT.contains("sdk_gphone")) {
            // Emulator
            baseUrl = "http://10.0.2.2:5000/";
        } else {
            // Real device
            baseUrl = "http://192.168.0.15:5000";     // http://127.0.0.1:5000/
        }
        return baseUrl;
    }

    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQUEST_GALLERY_IMAGE);
    }


    // Dönen tahmin sonucu küçük harfli bir şekilde geldiği için her kelimenin baş harfini büyük harf yapan method
    public static String capitalizeWords(String input) {
        String[] words = input.split("\\s+");
        StringBuilder capitalized = new StringBuilder();

        for (String word : words) {
            if (word.length() > 0) {
                capitalized.append(word.substring(0, 1).toUpperCase());
                capitalized.append(word.substring(1).toLowerCase());
                capitalized.append(" ");
            }
        }

        return capitalized.toString().trim();
    }

    private String getArtifactDetailsByName(String artifactName) {
        String details = "";
        try {
            InputStream is = getAssets().open("artifacts.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, "UTF-8");
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("artifacts");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject artifactObject = jsonArray.getJSONObject(i);
                String name = artifactObject.getString("name");
                String normalizedArtifactName = normalizeName(artifactName);
                String normalizedName = normalizeName(name);
                if (normalizedName.equalsIgnoreCase(normalizedArtifactName)) {
                    details = artifactObject.getString("details");
                    break;
                }
            }

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return details;
    }

    private String normalizeName(String name) {
        return name.trim().toLowerCase();
    }

    public void signInUser(String username, String password) {
        int userId = userDatabaseHelper.getUserId(username, password);

        if (userId != -1) {
            String[] columns = {
                    UserDatabaseHelper.COLUMN_NAME,
                    UserDatabaseHelper.COLUMN_SURNAME
            };
            SQLiteDatabase db = userDatabaseHelper.getReadableDatabase();
            String selection = UserDatabaseHelper.COLUMN_ID + " = ?";
            String[] selectionArgs = {String.valueOf(userId)};
            Cursor cursor = db.query(UserDatabaseHelper.TABLE_USERS, columns, selection, selectionArgs, null, null, null);

            if (cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(UserDatabaseHelper.COLUMN_NAME);
                int surnameIndex = cursor.getColumnIndex(UserDatabaseHelper.COLUMN_SURNAME);
                String name = cursor.getString(nameIndex);
                String surname = cursor.getString(surnameIndex);
                isLoggedIn = true;

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isLoggedIn", true);
                editor.putInt("user_id", userId); // Kullanıcı ID'sini SharedPreferences'a kaydet
                editor.putString("name", name); // Kullanıcı adını ve soyadını SharedPreferences'a kaydet
                editor.putString("surname", surname);
                editor.apply();

                saveCurrentUserId(userId); // Kullanıcının ID'sini kaydet

                Bundle bundle = new Bundle();
                bundle.putString("userName", name);
                bundle.putString("userSurname", surname);
                ProfileFragment profileFragment = new ProfileFragment();
                profileFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, profileFragment).commit();
                mLogo.setVisibility(GONE);
            }
            cursor.close();
        } else {
            Toast.makeText(this, "Giriş başarısız. Lütfen bilgilerinizi kontrol edin.", Toast.LENGTH_SHORT).show();
        }
    }

    public void signUpUser(String username, String password, String name, String surname) {
        SQLiteDatabase db = userDatabaseHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UserDatabaseHelper.COLUMN_USERNAME, username);
        values.put(UserDatabaseHelper.COLUMN_PASSWORD, password);
        values.put(UserDatabaseHelper.COLUMN_NAME, name);
        values.put(UserDatabaseHelper.COLUMN_SURNAME, surname);

        long newRowId = db.insert(UserDatabaseHelper.TABLE_USERS, null, values);
        isLoggedIn = true;

        if (newRowId != -1) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLoggedIn", true);
            editor.putString("name", name);
            editor.putString("surname", surname);
            editor.putInt("user_id", (int) newRowId); // Kullanıcı ID'sini SharedPreferences'a kaydet
            editor.apply();

            saveCurrentUserId((int) newRowId); // Kullanıcının ID'sini kaydet

            Toast.makeText(this, "Üyelik işleminiz başarıyla tamamlandı.", Toast.LENGTH_SHORT).show();

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
            mLogo.setVisibility(GONE);
        } else {
            Toast.makeText(this, "Kayıt başarısız. Lütfen tekrar deneyin.", Toast.LENGTH_SHORT).show();
        }
    }
    private void showSignInDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_sign_in, null);
        builder.setView(dialogView);

        final EditText emailEditText = dialogView.findViewById(R.id.emailEditText);
        final EditText passwordEditText = dialogView.findViewById(R.id.passwordEditText);
        Button signInButton = dialogView.findViewById(R.id.signInButton);

        AlertDialog dialog = builder.create();

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                signInUser(email, password);
                dialog.dismiss();
            }
        });

        dialog.show();
    }
    private void showSignUpDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_sign_up, null);
        builder.setView(dialogView);

        final EditText emailEditText = dialogView.findViewById(R.id.emailEditText);
        final EditText nameEditText = dialogView.findViewById(R.id.nameEditText);
        final EditText surnameEditText = dialogView.findViewById(R.id.surnameEditText);
        final EditText passwordEditText = dialogView.findViewById(R.id.passwordEditText);
        Button signUpButton = dialogView.findViewById(R.id.signUpButton);

        AlertDialog dialog = builder.create();

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEditText.getText().toString();
                String name = nameEditText.getText().toString();
                String surname = surnameEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                signUpUser(email, password, name, surname);
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void signOut() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", false);
        editor.remove("name"); // Kullanıcı adını ve soyadını kaldır
        editor.remove("surname");
        editor.apply();

        isLoggedIn = false;

        // MainActivity'i yeniden başlat
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Logo imageview oluşturma
        mLogo = new ImageView(this);
        mLogo.setImageResource(R.drawable.applogo);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(600, 350);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        layoutParams.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics()); // 20dp üst boşluk ekler
        mLogo.setLayoutParams(layoutParams);

        // FrameLayout'a ImageView ekleme
        FrameLayout fragmentContainer = findViewById(R.id.fragment_container);
        fragmentContainer.addView(mLogo);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        signInButton = (Button) findViewById(R.id.signInButton);
        signUpButton = (Button) findViewById(R.id.signUpButton);

        userDatabaseHelper = new UserDatabaseHelper(this);
        sharedPreferences = getSharedPreferences("MyPreferences", MODE_PRIVATE);

        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
            signInButton.setVisibility(GONE);
            signUpButton.setVisibility(GONE);
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;

                switch (item.getItemId()) {
                    case R.id.action_profile:
                        selectedFragment = new ProfileFragment();
                        mLogo.setVisibility(GONE);
                        break;

                    case R.id.action_gallery:
                        selectedFragment = new GalleryFragment();
                        pickImageFromGallery();
                        break;

                    case R.id.action_museums:
                        selectedFragment = new MuseumsFragment();
                        mLogo.setVisibility(GONE);
                        break;
                    case R.id.action_photo:
                        selectedFragment = new PhotoFragment();
                        checkPermissionsAndOpenCamera();
                        mLogo.setVisibility(GONE);
                        signInButton.setVisibility(GONE);
                        signUpButton.setVisibility(GONE);
                        break;
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                }
                return true;
            }
        });

        Button signInButton = findViewById(R.id.signInButton);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSignInDialog();
            }
        });

        Button signUpButton = findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSignUpDialog();
            }
        });

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getBaseUrl())
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build();

        apiService = retrofit.create(ApiService.class);
    }


    public void checkPermissionsAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_IMAGE_CAPTURE); } else {
            openCamera();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Kamera izni verilmedi", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


    public void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_GALLERY_IMAGE);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Bundle extras = data.getExtras();
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");

            sendImageToApi(bitmap, requestCode, null);  // requestcode ve null sonradan eklendi

            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            if(isLoggedIn){
                ProfileFragment profileFragment = new ProfileFragment();
                fragmentTransaction.replace(R.id.fragment_container, profileFragment);
            }else{
                GalleryFragment galleryFragment = new GalleryFragment();
                fragmentTransaction.replace(R.id.fragment_container, galleryFragment);
            }
            fragmentTransaction.commit();

        } else if (requestCode == REQUEST_GALLERY_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            grantUriPermission(getPackageName(), selectedImageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                Bitmap convertedBitmap = convertToJPEG(bitmap);
                sendImageToApi(convertedBitmap, requestCode, selectedImageUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    private Bitmap convertToJPEG(Bitmap sourceBitmap) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            sourceBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        }

        private void saveCurrentUserId(int userId) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt("currentUserId", userId);
            editor.apply();
        }

        private int getCurrentUserId() {
            int userId = sharedPreferences.getInt("currentUserId", -1);
            if (userId == -1) {
                Log.e("MainActivity", "Kullanıcı ID'si geçersiz (-1)");
                // Alternatif olarak, burada bir hata mesajı gösterebilir veya kullanıcıyı giriş yapmaya zorlayabilirsiniz.
            }
            return userId;
        }


    private void sendImageToApi(Bitmap bitmap, int requestCode, Uri selectedImageUri) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        String encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("image", encodedImage);

        Call<ArtifactResponse> call = apiService.predictArtifact(jsonObject);
        call.enqueue(new Callback<ArtifactResponse>() {
            @Override
            public void onResponse(Call<ArtifactResponse> call, Response<ArtifactResponse> response) {
                if (!isFinishing() && !isDestroyed()){
                if (response.isSuccessful()) {
                    ArtifactResponse artifactResponse = response.body();
                    if (artifactResponse != null) {
                        String artifactName = artifactResponse.getArtifactName();
                        String capitalizedArtifactName = capitalizeWords(artifactName);
                        String artifactDetails = getArtifactDetailsByName(artifactName);

                        Bundle args = new Bundle();
                        args.putString("artifact_name", capitalizedArtifactName);
                        args.putString("artifact_details", artifactDetails);

                        FragmentManager fragmentManager = getSupportFragmentManager();
                        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                        if (requestCode == REQUEST_GALLERY_IMAGE) {
                            args.putParcelable("image_uri", selectedImageUri);
                            int userId = getCurrentUserId();
                            userDatabaseHelper.addArtHistoryItem(userId, selectedImageUri.toString(), capitalizedArtifactName);
                        } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                            args.putParcelable("image_bitmap", bitmap);
                            int userId = getCurrentUserId();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] byteArray = baos.toByteArray();
                            String bitmapString = Base64.encodeToString(byteArray, Base64.DEFAULT);
                            userDatabaseHelper.addArtHistoryItem(userId, bitmapString, capitalizedArtifactName);
                            GalleryFragment galleryFragment = new GalleryFragment();
                            galleryFragment.setArguments(args);
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, galleryFragment).commit();

                        }
                        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
                        if (requestCode == REQUEST_GALLERY_IMAGE) {
                            if (isLoggedIn) {
                                ProfileFragment profileFragment = new ProfileFragment();
                                profileFragment.setArguments(args);
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, profileFragment).commit();
                            } else {
                                GalleryFragment galleryFragment = new GalleryFragment();
                                galleryFragment.setArguments(args);
                                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, galleryFragment).commit();
                            }
                        }

                    }
                } else {
                    Toast.makeText(MainActivity.this, "API'den yanıt alınamadı", Toast.LENGTH_SHORT).show();
                }
            }
            }

            @Override
            public void onFailure(Call<ArtifactResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Hata: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    public interface ApiService {
        @POST("predict")
        Call<ArtifactResponse> predictArtifact(@Body JsonObject base64Image);
    }
    public static class ArtifactResponse {

        @SerializedName("result")
        private String artifactName;

        public String getArtifactName() {
            return artifactName;
        }
        public void setArtifactName(String artifactName) {
            this.artifactName = artifactName;
        }
        @Override
        public String toString(){
            return "ArtifactResponse{" + "result=" + artifactName + '\'' + '}';
        }
    }
}