package com.example.easychat.util;

import android.util.Log;

import com.google.firebase.Firebase;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.List;

public class FirebaseUtil {

    public static String currentUserId(){
        //devuelvo el id de autenticacion del usuario logeado
        return FirebaseAuth.getInstance().getUid();
    }

    //metodo para abrir la actividad principal si ya esta logeado
    public static  boolean isLoggedIn(){
        if (currentUserId()!=null){
            return true;
        }
        return false;
    }
    public static DocumentReference currentUserDetails(){
        //obtengo el documento del usuario logeado
        return FirebaseFirestore.getInstance().collection("users").document(currentUserId());
    }

    public static CollectionReference allUserCollectionReference(){
        return FirebaseFirestore.getInstance().collection("users");
    }


    public static DocumentReference getChatroomReference(String chatroomId){
        //Recupera el documento del chatroomId
        return FirebaseFirestore.getInstance().collection("chatrooms").document(chatroomId);
    }


    public static CollectionReference getChatroomMessageReference(String chatroomId){
        //obtengo el doc chatroomId por su Id y creo una colecion "chat de mensajes. Y recupero esa coleccion
        return getChatroomReference(chatroomId).collection("chats");
    }

    //genero el chatroomId a la que pertencen dos personas que estas chateando
    public static String getChatroomId(String userId1, String userId2){
        //El método hashCode() es un método de la clase String que devuelve un valor entero que representa la cadena
        if(userId1.hashCode()<userId2.hashCode()){
            return userId1+"_"+userId2;

        }else {
            return userId2+"_"+userId1;//posible error
        }
    }

    public static CollectionReference allChatroomCollectionReference(){
        //Obtengo toda la coleccion de chatrooms en general
        return FirebaseFirestore.getInstance().collection("chatrooms");
    }

    public static DocumentReference getOtherUserFromChatroom(List<String> userIds){
        if(userIds.get(0).equals(FirebaseUtil.currentUserId())){
            return allUserCollectionReference().document(userIds.get(1));
        }else{
            return allUserCollectionReference().document(userIds.get(0));
        }
    }

    public static String timestampToString(Timestamp timestamp){
        return new SimpleDateFormat("HH:MM").format(timestamp.toDate());
    }

    public static void logout(){
        FirebaseAuth.getInstance().signOut();
    }

    public static StorageReference getCurrentProfilePicStorageRef(){
        return FirebaseStorage.getInstance().getReference().child("profile_pic").child(FirebaseUtil.currentUserId());
    }

    public static StorageReference getOtherProfilePicStorageRef(String otherUserId){
        return FirebaseStorage.getInstance().getReference().child("profile_pic").child(otherUserId);
    }
}
