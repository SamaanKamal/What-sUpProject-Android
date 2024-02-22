package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.chatapp.Chat.ChatListAdapter;
import com.example.chatapp.Chat.ChatObject;
import com.example.chatapp.User.UserObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainPageActivity extends AppCompatActivity {

    private RecyclerView mChatList;
    private RecyclerView.Adapter mChatListAdapter;
    private RecyclerView.LayoutManager mChatListLayoutManager;

    ArrayList<ChatObject> chatList = new ArrayList<ChatObject>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        Button mLogout = findViewById(R.id.logout);
        Button mFindUser = findViewById(R.id.findUser);

        //Find User button
        mFindUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               startActivity(new Intent(getApplicationContext(), FindUserActivity.class));
            }
        });
        //Logout button
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
                return;
            }
        });

        getPermissions();
        initializeRecyclerView();
        getUserChatList();
    }

    private void getUserChatList(){
        DatabaseReference mUserChatDB = FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth
        .getInstance().getUid()).child("chat");

        //Always listening to the DB and checking for changes
        mUserChatDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //looks for all IDs inside the userChat
                if (dataSnapshot.exists()){
                    for (DataSnapshot childSnapshot : dataSnapshot.getChildren()){
                        ChatObject mChat = new ChatObject(childSnapshot.getKey());
                        boolean exists = false;
                        for (ChatObject mChatIterator : chatList){
                            if(mChatIterator.getChatId().equals(mChat.getChatId())){
                                exists = true;
                            }if (exists){
                                continue;
                            }
                        }
                        chatList.add(mChat);
                        getChatData(mChat.getChatId());

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void getChatData(String chatId) {
        DatabaseReference mChatDB = FirebaseDatabase.getInstance().getReference().child("chat").child(chatId).child("info");
        mChatDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists())
                {
                    String chatId = "";

                    // to not make the app crush
                    if(dataSnapshot.child("id").getValue() != null)
                    {
                        chatId = dataSnapshot.child("id").getValue().toString(); // get Id value
                    }
                    for(DataSnapshot userSnapshot : dataSnapshot.child("users").getChildren())
                    {
                        for(ChatObject mChat : chatList) // to find the chat that corresponds to chat id
                        {
                            if(mChat.getChatId().equals(chatId))
                            {
                                UserObject mUser = new UserObject(userSnapshot.getKey()); // get the user id
                                mChat.addUserToArrayList(mUser);
                                getUserData(mUser);
                            }
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUserData(UserObject mUser) {
        DatabaseReference mUserDb = FirebaseDatabase.getInstance().child("user").child(mUser.getUid());
        mUserDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserObject mUser = new UserObject(dataSnapshot.getKey());

                if(dataSnapshot.child("notificationKey").getValue() != null)
                {
                    mUser.SetNotificationKey(dataSnapshot.child("notificationKey").getValue().toString());
                }

                //to check which user in the arraylist of users
                for(ChatObject mChat : chatList)
                {
                    for (UserObject mUserIt : mChat.getUserObjectArrayList())
                    {
                        if(mUserIt.getUid().equals(mUser.getUid()))
                        {
                            mUserIt.SetNotificationKey(mUser.getNotificationKey()); //update userIt
                        }
                    }
                }
                mChatListAdapter.notifyDataSetChanged(); //update the Chat list
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initializeRecyclerView() {
        chatList = new ArrayList<>();
        mChatList = findViewById(R.id.chatList);
        mChatList.setNestedScrollingEnabled(false);
        mChatList.setHasFixedSize(false);
        mChatListLayoutManager = new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false);
        mChatList.setLayoutManager(mChatListLayoutManager);
        mChatListAdapter = new ChatListAdapter(chatList);
        mChatList.setAdapter(mChatListAdapter);
    }

    //Asks the user for permissions to access the contacts
    private void getPermissions() {
        requestPermissions(new String[]{Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS}, 1);
    }

}