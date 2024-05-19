package com.example.aspacetospeak;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.RelativeLayout;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.util.ArrayList;

public class SplashScreen extends AppCompatActivity {
    Button exitSplash;
    TextView title, subtitle;
    int activeAnimation = 0;
    FirebaseStorage storage;
    ArrayList<String> downloadedItems;

    ArrayList<TextView> floatingText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splash), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        title = findViewById(R.id.welcome);

        storage = FirebaseStorage.getInstance();
        subtitle = findViewById(R.id.subtitle);
        exitSplash = findViewById(R.id.btn_exitSplash);
        exitSplash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Login.class);
                startActivity(intent);
                finish();
            }
        });
        final Animation in = new AlphaAnimation(0.0f, 1.0f);
        in.setDuration(7000);
        in.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if(activeAnimation == 0){
                    title.clearAnimation();
                    activeAnimation = 1;
                    subtitle.startAnimation(in);
                    subtitle.setAlpha(1);
                }
                else if(activeAnimation == 1){
                    subtitle.clearAnimation();
                    exitSplash.startAnimation(in);
                    exitSplash.setAlpha(1);
                    activeAnimation = 2;
                }
                else if(activeAnimation == 2){
                    exitSplash.clearAnimation();
                    exitSplash.setClickable(true);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        title.setAlpha(1);
        title.startAnimation(in);
        //final Animation rotate = new RotateAnimation();
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.splash);
        try {
            downloadText();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    private void downloadText() throws FileNotFoundException {
        StorageReference folderRef = storage.getReference().child("Messages");

        folderRef.listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        downloadedItems = new ArrayList<>();
                        for(int i=0; i<listResult.getItems().size();i++){
                            StorageReference item = listResult.getItems().get(i);
                            final long ONE_MEGABYTE = 1024 * 1024;
                            item.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    // Data for "images/island.jpg" is returns, use this as needed
                                    downloadedItems.add(new String(bytes));

                                    RelativeLayout textContainer = (RelativeLayout) findViewById(R.id.splash);
                                    TextView textView = new TextView(SplashScreen.this);
                                    textView.setText(downloadedItems.get(downloadedItems.size()-1));
                                    textView.setTextSize((float)(Math.random()*10.0f)+10);
                                    textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                    //float offsetX = (float)(Math.random()*(textContainer.getWidth()))-30.0f;
                                    //float offsetY = (float)Math.random()*((float)textContainer.getHeight()-subtitle.getY())+subtitle.getY();
                                    float offsetX = (float)(Math.random()*(textContainer.getWidth()*2))-textContainer.getWidth();
                                    float offsetY = (float)Math.random()*((float)(textContainer.getHeight()-subtitle.getY())*2)+subtitle.getY()-textContainer.getHeight();
                                    textView.setX(offsetX);
                                    textView.setY(offsetY);
                                    textView.setPivotX(offsetX);
                                    textView.setPivotY(offsetY);
                                    textContainer.addView(textView );
                                    AnimationSet anim = new AnimationSet(true);
                                    final Animation rotate = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                                    rotate.setDuration((int) (Math.random() * 20000) +10000);
                                    rotate.setRepeatCount(Animation.INFINITE);
                                    final Animation in = new AlphaAnimation(0.0f, 1.0f);
                                    in.setDuration(2000);
                                    anim.addAnimation(in);
                                    anim.addAnimation(rotate);
                                    textView.startAnimation(anim);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    Toast.makeText(SplashScreen.this, "Error downloading files from internet", Toast.LENGTH_SHORT).show();

                                }
                            });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SplashScreen.this, "Error generating text file name", Toast.LENGTH_SHORT).show();

                    }
                });
    }

}