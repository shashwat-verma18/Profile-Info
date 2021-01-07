package com.example.profileinfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.facebook.Profile;
import com.facebook.internal.ImageRequest;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int MY_REQUEST_CODE = 1804;
    List<AuthUI.IdpConfig> providers;
    Button sign_out;
    TextView txtName, txtEmail, txtUserId, txtProviderId, test;
    ImageView profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sign_out = findViewById(R.id.sign_out);

        sign_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Logout
                AuthUI.getInstance()
                        .signOut(MainActivity.this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                sign_out.setEnabled(false);
                                showSignInOptions();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        providers = Arrays.asList(
                new AuthUI.IdpConfig.FacebookBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build()
        );

        showSignInOptions();
    }

    private void showSignInOptions() {
        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setLogo(R.drawable.mlogo)
                .setTheme(R.style.LoginTheme)
                .build(),MY_REQUEST_CODE
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MY_REQUEST_CODE){
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if(resultCode == RESULT_OK){
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                txtName = findViewById(R.id.name);
                txtEmail = findViewById(R.id.email);
                txtUserId = findViewById(R.id.userId);
                txtProviderId = findViewById(R.id.providerId);
                profile = findViewById(R.id.profile);

                String  name = user.getDisplayName();
                String email = user.getEmail();
                email = "Email : "+email;
                String userId = user.getUid();
                userId = "User Id : "+userId;
                String providerId = "";
                String photoUrl = "";
                for (UserInfo info: FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
                    if (info.getProviderId().equals("facebook.com")) {
                        providerId = "User is signed in with Facebook";
                        int dimensionPixelSize = getResources().getDimensionPixelSize(com.facebook.R.dimen.com_facebook_profilepictureview_preset_size_large);
                        Uri profilePictureUri= Profile.getCurrentProfile().getProfilePictureUri(dimensionPixelSize , dimensionPixelSize);

                        Glide.with(this).load(profilePictureUri).into(profile);
                    }
                    else if(info.getProviderId().equals("google.com")){
                        providerId = "User is signed in with Google";
                        photoUrl = user.getPhotoUrl().toString();
                        photoUrl = photoUrl + "?type=large";
                        Picasso.get().load(photoUrl).into(profile);
                    }
                }

                txtName.setText(name);
                txtEmail.setText(email);
                txtUserId.setText(userId);
                txtProviderId.setText(providerId);


                Toast.makeText(this, "Signed in as : "+user.getDisplayName(), Toast.LENGTH_SHORT).show();


                sign_out.setEnabled(true);
            }
            else{
                Toast.makeText(this, ""+response.getError().getMessage(),Toast.LENGTH_SHORT).show();
            }
        }

    }
}