package com.example.chatapp.Chat;

import com.example.chatapp.User.UserObject;

import java.util.ArrayList;

public class ChatObject {
    private String chatId;



    private ArrayList<UserObject> userObjectArrayList = new ArrayList<>();
    public ChatObject(String chatId){
        this.chatId = chatId;
    }

    public String getChatId(){
        return chatId;
    }

    public ArrayList<UserObject> getUserObjectArrayList() {
        return userObjectArrayList;
    }
    public void addUserToArrayList (UserObject mUser) {
        userObjectArrayList.add(mUser);
    }
}


