package com.example.easychat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.example.easychat.databinding.ActivityLoginPhoneNumberBinding;
import com.hbb20.CountryCodePicker;

public class LoginPhoneNumberActivity extends AppCompatActivity {
    ActivityLoginPhoneNumberBinding binding;
    CountryCodePicker countryCodePicker;
    EditText phoneInput;
    Button sendOtpBtn;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginPhoneNumberBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        countryCodePicker = binding.loginCountrycode;
        phoneInput = binding.loginMobileNumber;
        sendOtpBtn = binding.sendBtn;
        progressBar = binding.loginProgressBar;

        progressBar.setVisibility(View.GONE);

        countryCodePicker.registerCarrierNumberEditText(phoneInput);
        sendOtpBtn.setOnClickListener(v -> {

           if(! countryCodePicker.isValidFullNumber()){
               phoneInput.setError("Número celular inválido");
               return;
           }

            Intent intent = new Intent(LoginPhoneNumberActivity.this, LoginOtpActivity.class);
           intent.putExtra("phone", countryCodePicker.getFullNumberWithPlus());
           startActivity(intent);

        });

    }
}