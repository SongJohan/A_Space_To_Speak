package com.example.aspacetospeak;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    EditText editText;
    TextView emailText, greetingText, thankText;
    Button sendButton, logoutButton, homeButton;
    FirebaseStorage storage;
    FirebaseAuth mAuth;
    FirebaseUser user;
    String fileName = "fail";
    boolean sent = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        editText = findViewById(R.id.messageText);
        sendButton = findViewById(R.id.btn_send);
        logoutButton = findViewById(R.id.btn_logout);
        homeButton = findViewById(R.id.btn_home);
        greetingText = findViewById(R.id.greeting);
        thankText = findViewById(R.id.thanks);
        emailText = findViewById(R.id.txt_email);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();

        storage = FirebaseStorage.getInstance();

        if(user == null){
            Intent intent = new Intent(getApplicationContext(), Login.class);
            startActivity(intent);
            finish();
        }
        else{
            emailText.setText(user.getEmail());
        }
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = String.valueOf(editText.getText());
                if(!sent){
                    try {
                        uploadText(text);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                else{
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

        });
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SplashScreen.class);
                startActivity(intent);
                finish();
            }
        });

    }

    private void uploadText(String text) throws FileNotFoundException {
        StorageReference folderRef = storage.getReference().child("Messages");

        folderRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        boolean found = false;
                        while(!found){
                            found = true;
                            fileName = generateString("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz", 5);
                            for (StorageReference item : listResult.getItems()) {
                                // All the items under listRef.
                                //Toast.makeText(MainActivity.this, "item: " + item.getName(), Toast.LENGTH_SHORT).show();
                                if((fileName +".txt").equals(item.getName())){
                                    found = false;
                                }

                            }
                            upload2();
                        }
                    }

                    private void upload2() {

                        StorageReference textRef;
                        //Toast.makeText(MainActivity.this, fileName, Toast.LENGTH_SHORT).show();
                        textRef = folderRef.child(fileName +".txt");

                        //ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] data = text.getBytes();

                        UploadTask uploadTask = textRef.putBytes(data);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                Toast.makeText(MainActivity.this, "upload failed", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                                // ...
                                //Toast.makeText(MainActivity.this, "We hear you.", Toast.LENGTH_SHORT).show();
                                sent = true;
                                final Animation out = new AlphaAnimation(1.0f, 0.0f);
                                out.setDuration(1000);
                                out.setAnimationListener(new Animation.AnimationListener() {
                                    @Override
                                    public void onAnimationStart(Animation animation) {

                                    }

                                    @Override
                                    public void onAnimationEnd(Animation animation) {
                                        sendButton.setAlpha(0);
                                        greetingText.setAlpha(0);
                                        emailText.setAlpha(0);
                                        greetingText.setAlpha(0);
                                        AnimationSet zoomAndAlpha = new AnimationSet(true);
                                        final Animation fly = new ScaleAnimation(1.0f, 0.1f, 1.0f, 0.1f,ScaleAnimation.RELATIVE_TO_PARENT, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
                                        fly.setDuration(3000);

                                        final Animation fade = new AlphaAnimation(1.0f, 0.0f);
                                        fade.setDuration(3000);
                                        fade.setAnimationListener(new Animation.AnimationListener() {
                                            @Override
                                            public void onAnimationStart(Animation animation) {

                                            }

                                            @Override
                                            public void onAnimationEnd(Animation animation) {
                                                editText.setAlpha(0);
                                                Animation thankAnim = new AlphaAnimation(0.0f, 1.0f);
                                                thankAnim.setDuration(5000);
                                                thankAnim.setAnimationListener(new Animation.AnimationListener() {
                                                    @Override
                                                    public void onAnimationStart(Animation animation) {
                                                        thankText.setAlpha(1);
                                                    }

                                                    @Override
                                                    public void onAnimationEnd(Animation animation) {
                                                        Animation buttons = new AlphaAnimation(0.0f, 1.0f);
                                                        buttons.setDuration(5000);
                                                        buttons.setAnimationListener(new Animation.AnimationListener() {
                                                            @Override
                                                            public void onAnimationStart(Animation animation) {
                                                                homeButton.setAlpha(1);
                                                                sendButton.setAlpha(1);
                                                                sendButton.setText("Send another?");
                                                            }

                                                            @Override
                                                            public void onAnimationEnd(Animation animation) {

                                                                final Animation fade = new AlphaAnimation(1.0f, 0.0f);
                                                                fade.setDuration(3000);
                                                                thankText.startAnimation(fade);
                                                                fade.setAnimationListener(new Animation.AnimationListener() {
                                                                    @Override
                                                                    public void onAnimationStart(Animation animation) {

                                                                    }

                                                                    @Override
                                                                    public void onAnimationEnd(Animation animation) {
                                                                        thankText.setAlpha(0);
                                                                    }

                                                                    @Override
                                                                    public void onAnimationRepeat(Animation animation) {

                                                                    }
                                                                });
                                                            }

                                                            @Override
                                                            public void onAnimationRepeat(Animation animation) {

                                                            }
                                                        });
                                                        homeButton.startAnimation(buttons);
                                                        sendButton.startAnimation(buttons);
                                                    }

                                                    @Override
                                                    public void onAnimationRepeat(Animation animation) {

                                                    }
                                                });
                                                String[] thanks = {"Your words have been heard among the stars",
                                                "We hear you",
                                                "We wish you the best",
                                                "Thank you for sharing your message with the universe",
                                                "We're here for you"};
                                                thankText.setText(thanks[(int)(Math.random()*thanks.length)]);
                                                thankText.startAnimation(thankAnim);

                                            }

                                            @Override
                                            public void onAnimationRepeat(Animation animation) {

                                            }
                                        });
                                        zoomAndAlpha.addAnimation(fly);
                                        zoomAndAlpha.addAnimation(fade);
                                        editText.startAnimation(zoomAndAlpha);
                                    }

                                    @Override
                                    public void onAnimationRepeat(Animation animation) {

                                    }
                                });
                                sendButton.startAnimation(out);
                                greetingText.startAnimation(out);

                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Error generating text file name", Toast.LENGTH_SHORT).show();

                    }
                });
    }
    public static String generateString(String characters, int length)
    {
        Random rng = new Random();
        char[] text = new char[length];
        for (int i = 0; i < length; i++)
        {
            text[i] = characters.charAt(rng.nextInt(characters.length()));
        }
        return new String(text);
    }
}