package com.example.easychat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.easychat.model.UserModel;
import com.example.easychat.util.AndroidUtils;
import com.example.easychat.util.FirebaseUtil;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if(getIntent().getExtras()!=null){
            //from notificaciones
            /*Querenmos saner quien ha mandando el mensaje y navegar hasya ese intent*/
            String userId = getIntent().getStringExtra("userId");
            FirebaseUtil.allUserCollectionReference().document(userId).get()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){
                            UserModel model = task.getResult().toObject(UserModel.class);

                            //Ir a la actividad principal
                            Intent mainIntent = new Intent(this,MainActivity.class);
                            mainIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(mainIntent);


                            //ir a la actiividad del chat
                            Intent intent = new Intent(this, ChatActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            AndroidUtils.passUserModelAsIntent(intent,model);
                            startActivity(intent);
                            finish();
                        }
                    });


        }else {
            new Handler().postDelayed(() -> {
                //si el usuario ya ha iniciado sesion antes
                if(FirebaseUtil.isLoggedIn()){
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                }else {
                    startActivity(new Intent(SplashActivity.this,LoginPhoneNumberActivity.class));
                }
                finish();
            },1000);
        }


    }
}