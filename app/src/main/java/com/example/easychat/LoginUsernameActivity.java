package com.example.easychat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.example.easychat.databinding.ActivityLoginUsernameBinding;
import com.example.easychat.model.UserModel;
import com.example.easychat.util.FirebaseUtil;
import com.google.firebase.Timestamp;

public class LoginUsernameActivity extends AppCompatActivity {
    ActivityLoginUsernameBinding binding;
    EditText usernameInput;
    Button letmeinBtn;
    ProgressBar progressBar;
    String phoneNumber;
    UserModel userModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginUsernameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        usernameInput = binding.loginUsername;
        letmeinBtn = binding.loginLetmeinBtn;
        progressBar = binding.loginProgressBar;

        phoneNumber = getIntent().getStringExtra("phone");
        getUserName();

        letmeinBtn.setOnClickListener(v -> {
            setUsername();
        });

    }
    public void setUsername(){

        String username = usernameInput.getText().toString();
        if(username.isEmpty() || username.length()<3){
            usernameInput.setError("El nombre de usuario debe tener más de 3 caracteres");
            return;
        }

        setInProgress(true);
        if (userModel!=null){
            userModel.setUsername(username);
        }else {
            userModel = new UserModel(phoneNumber,username, Timestamp.now(),FirebaseUtil.currentUserId());
        }

        FirebaseUtil.currentUserDetails().set(userModel)
                .addOnCompleteListener(task -> {
                    setInProgress(false);
                    if(task.isSuccessful()){
                        Intent intent = new Intent(LoginUsernameActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                });
    }

    public void getUserName(){
        setInProgress(true);
        //Obtengo el documento del usuario pro el id de autenticacion
        FirebaseUtil.currentUserDetails().get()
                .addOnCompleteListener(task -> {
                    setInProgress(false);
                    if (task.isSuccessful()){
                        userModel = task.getResult().toObject(UserModel.class);

                        //siempre valida
                        if(userModel!=null){
                            usernameInput.setText(userModel.getUsername());
                        }
                    }
                });
    }
    public void setInProgress(boolean isProgress){
        if(isProgress){
            progressBar.setVisibility(View.VISIBLE);
            letmeinBtn.setVisibility(View.GONE);
        }else {
            progressBar.setVisibility(View.GONE);
            letmeinBtn.setVisibility(View.VISIBLE);
        }
    }
}