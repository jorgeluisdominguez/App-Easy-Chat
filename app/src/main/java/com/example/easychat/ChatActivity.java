package com.example.easychat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.easychat.adapter.ChatRecyclerAdapter;
import com.example.easychat.databinding.ActivityChatBinding;
import com.example.easychat.model.ChatMessageModel;
import com.example.easychat.model.ChatroomModel;
import com.example.easychat.model.UserModel;
import com.example.easychat.util.AndroidUtils;
import com.example.easychat.util.FirebaseUtil;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {
    ActivityChatBinding binding;
    UserModel otherUser;
    String chatroomId;
    ChatroomModel chatroomModel;
    ChatRecyclerAdapter adapter;

    EditText messageInput;
    ImageButton sendMessageBtn;
    ImageButton backBtn;
    TextView otherUsername;
    RecyclerView recyclerView;
    ImageView imageOhterUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        imageOhterUser = binding.profilePicLayout.profilePicImgView;
        messageInput = binding.chatMessageInput;
        sendMessageBtn = binding.messageSendBtn;
        backBtn = binding.backBtn;
        otherUsername = binding.otherUsername;
        recyclerView = binding.chatRecycleView;


        //get el user con quien conversará
        otherUser = AndroidUtils.getUserModelFromIntent(getIntent());
        //obtener la foto del usuarioBuscar de FirebaseStorage (Descargar archivos)
        FirebaseUtil.getOtherProfilePicStorageRef(otherUser.getUserId()).getDownloadUrl()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        Uri uri = task.getResult();
                        AndroidUtils.setProfilePic(this,uri,imageOhterUser);
                    }
                });

        chatroomId = FirebaseUtil.getChatroomId(FirebaseUtil.currentUserId(),otherUser.getUserId());



        otherUsername.setText(otherUser.getUsername());
        backBtn.setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        sendMessageBtn.setOnClickListener(v -> {
            //trim() elimina los espacios en blanco al principio y al final de la cadena, pero no en el medio.
            String message = messageInput.getText().toString().trim();
            if (message.isEmpty())
                return;
            sendMessageToUser(message);
        });


        getOrCreateChatroomModel();
        setupChatRecyclerView();
    }

    public void setupChatRecyclerView(){

        Query query = FirebaseUtil.getChatroomMessageReference(chatroomId).orderBy("timestamp", Query.Direction.DESCENDING);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()){
               for (QueryDocumentSnapshot documentSnapshots : task.getResult() ) {
                   ChatMessageModel modelchat = documentSnapshots.toObject(ChatMessageModel.class);
                   Log.d("msg-test",modelchat.getMessage());
                   Log.d("msg-test",modelchat.getSenderId());
               }

            }
        });
        FirestoreRecyclerOptions<ChatMessageModel> options = new FirestoreRecyclerOptions.Builder<ChatMessageModel>()
                .setQuery(query,ChatMessageModel.class).build();


        adapter = new ChatRecyclerAdapter(options,ChatActivity.this);
        recyclerView.setAdapter(adapter);
        //Para que los mensajes aparezcan de abajo hacia arriba
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(true);
        recyclerView.setLayoutManager(manager);
        adapter.startListening();
        //Para que haya scroll al enviar los mensajes
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                recyclerView.smoothScrollToPosition(0);
            }
        });
    }

    public void sendMessageToUser(String message){
        chatroomModel.setLastMessageTimestamp(Timestamp.now());
        chatroomModel.setLastMessageSenderId(FirebaseUtil.currentUserId());
        chatroomModel.setLastMessage(message);
        FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);

        ChatMessageModel chatMessageModel = new ChatMessageModel(message,FirebaseUtil.currentUserId(),Timestamp.now());
        FirebaseUtil.getChatroomMessageReference(chatroomId).add(chatMessageModel)
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        messageInput.setText("");
                        //enviar la notificacion al mandar mensaje
                        sendNotification(message);
                    }
                });

    }
    public void sendNotification(String message){
        //current username, message, currentUserId, otherUserToken
        FirebaseUtil.currentUserDetails().get().addOnCompleteListener(task -> {
           if(task.isSuccessful()){
               UserModel currentUser = task.getResult().toObject(UserModel.class);

               try {
                   JSONObject jsonObject = new JSONObject();

                   JSONObject notificationObj = new JSONObject();
                   notificationObj.put("title",currentUser.getUsername());
                   notificationObj.put("body",message);


                   JSONObject dataObj = new JSONObject();
                   dataObj.put("userId",currentUser.getUserId());   //mismo identificador que el putExtra del splashActivity


                   jsonObject.put("notification",notificationObj);
                   jsonObject.put("data",dataObj);
                   jsonObject.put("to",otherUser.getFcmToken());


                   callApi(jsonObject);


               }catch (Exception e){

               }
           }
        });
    }
    public void callApi(JSONObject jsonObject){
        MediaType JSON = MediaType.get("application/json");
        OkHttpClient client = new OkHttpClient();
        String url = "https://fcm.googleapis.com/fcm/send";
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .header("Authorization","Bearer AAAAPb2z3s4:APA91bHeLyz2TsHuXfi9w4B8VRcrGwXHL1NZndf0X18PJk3Z63YHZT0k9wtLEhdIeeqAIe__NgvMp-ZQMTOX8KQcrEZB4ok0F3dkzqFuFa5qcXi7k8cEH26AIOxaxfGTKRVfG4qXB9IS")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

            }
        });

    }


    public void getOrCreateChatroomModel(){
        FirebaseUtil.getChatroomReference(chatroomId).get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                chatroomModel = task.getResult().toObject(ChatroomModel.class);

                if(chatroomModel == null){//si no hay una sala de chat previa hay que crearla
                    chatroomModel = new ChatroomModel(chatroomId, Arrays.asList(FirebaseUtil.currentUserId(),
                                                        otherUser.getUserId()), Timestamp.now(),"");

                    FirebaseUtil.getChatroomReference(chatroomId).set(chatroomModel);
                }
            }
        });
    }
}