package com.example.easychat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.easychat.adapter.SearchUserRecyclerAdapter;
import com.example.easychat.databinding.ActivitySearchUserBinding;
import com.example.easychat.model.UserModel;
import com.example.easychat.util.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.Query;

public class SearchUserActivity extends AppCompatActivity {
    ActivitySearchUserBinding binding;
    EditText searchInptu;
    ImageButton searchBtn;
    ImageButton backBtn;
    RecyclerView recyclerView;
    SearchUserRecyclerAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchUserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        searchInptu = binding.searchUsernameInput;
        searchBtn = binding.searchUserBtn;
        backBtn = binding.backBtn;
        recyclerView = binding.searchUserRecycleView;

        //El teclado de usuario debera abrirse automaticamente cuando se ingrese a esta actividad
        searchInptu.requestFocus();

        backBtn.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        searchBtn.setOnClickListener(v -> {
            String searchTerm = searchInptu.getText().toString();
            if(searchTerm.isEmpty() || searchTerm.length()<3){
                searchInptu.setError("Usuario inválido");
                return;
            }

            setupSearchRecycleView(searchTerm);

        });
    }

    public void setupSearchRecycleView(String searchTerm){

        Query query = FirebaseUtil.allUserCollectionReference().whereGreaterThanOrEqualTo("username", searchTerm)
                .whereLessThanOrEqualTo("username",searchTerm+'\uf8ff');

        FirestoreRecyclerOptions<UserModel> options = new FirestoreRecyclerOptions.Builder<UserModel>()
                .setQuery(query,UserModel.class).build();

        adapter = new SearchUserRecyclerAdapter(options,SearchUserActivity.this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(adapter!=null){
            adapter.startListening();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(adapter!=null){
            adapter.stopListening();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(adapter!=null){
            adapter.startListening();
        }
    }
}