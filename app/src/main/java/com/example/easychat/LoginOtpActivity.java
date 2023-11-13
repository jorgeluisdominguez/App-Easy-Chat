package com.example.easychat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.easychat.databinding.ActivityLoginOtpBinding;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class LoginOtpActivity extends AppCompatActivity {
    FirebaseFirestore db;
    ActivityLoginOtpBinding binding;
    String phoneNumber;
    EditText otpInput;
    Button nextBtn;
    ProgressBar progressBar;
    TextView resendOtpTextView;
    FirebaseAuth auth = FirebaseAuth.getInstance();
    Long timeoutSeconds = 60L;
    String verificationCode;
    PhoneAuthProvider.ForceResendingToken resendingToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginOtpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        otpInput = binding.loginOtp;
        nextBtn = binding.loginNextBtn;
        progressBar = binding.loginProgressBar;
        resendOtpTextView = binding.resendOtpTextView;

        phoneNumber = getIntent().getStringExtra("phone");

        sendOtp(phoneNumber,false); //false porque aun no se hace click en reenviar


        nextBtn.setOnClickListener(v -> {
            //Obenter el OTP ingresada por el ususario
            String enterOTP = otpInput.getText().toString();

            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationCode, enterOTP);
            signIn(credential);

        });

        resendOtpTextView.setOnClickListener(v -> {
            sendOtp(phoneNumber,true);
        });
    }

    public void sendOtp(String phoneNumber, boolean isResend){
        startResendTimer(); //reenvio del codigo de verificacion
        setInProgress(true);
        PhoneAuthOptions.Builder builder = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(phoneNumber)
                .setTimeout(timeoutSeconds, TimeUnit.SECONDS)
                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        signIn(phoneAuthCredential);
                        setInProgress(false);
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        Toast.makeText(LoginOtpActivity.this, "La verificación falló", Toast.LENGTH_SHORT).show();
                        setInProgress(false);
                    }

                    @Override
                    public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        super.onCodeSent(s, forceResendingToken);
                        verificationCode = s;
                        resendingToken = forceResendingToken;
                        Toast.makeText(LoginOtpActivity.this, "Código enviado", Toast.LENGTH_SHORT).show();
                        setInProgress(false);
                    }
                });

        if(isResend){
            PhoneAuthProvider.verifyPhoneNumber(builder.setForceResendingToken(resendingToken).build());
        }else {
            PhoneAuthProvider.verifyPhoneNumber(builder.build());
        }
    }

    public void setInProgress(boolean isProgress){
        if(isProgress){
            progressBar.setVisibility(View.VISIBLE);
            nextBtn.setVisibility(View.GONE);
        }else {
            progressBar.setVisibility(View.GONE);
            nextBtn.setVisibility(View.VISIBLE);
        }
    }

    public void signIn(PhoneAuthCredential phoneAuthCredential){
        //login y luego ir a la actividad
        setInProgress(true);
        auth.signInWithCredential(phoneAuthCredential)
                .addOnCompleteListener(task -> {
                    setInProgress(false);
                    if (task.isSuccessful()){
                        Intent intent = new Intent(LoginOtpActivity.this,LoginUsernameActivity.class);
                        intent.putExtra("phone",phoneNumber);
                        startActivity(intent);
                    }else {
                        Toast.makeText(this, "Verificacion OTP falló", Toast.LENGTH_SHORT).show();
                    }

                });


    }

    public void startResendTimer(){
        resendOtpTextView.setEnabled(false);
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeoutSeconds--;
                resendOtpTextView.setText("Reenviar código en "+timeoutSeconds+" segundos");
                if(timeoutSeconds<=0){
                    timeoutSeconds = 60L;
                    timer.cancel();
                    runOnUiThread(() -> {
                        resendOtpTextView.setEnabled(true);
                    });
                }
            }
        },0,1000);
    }
}