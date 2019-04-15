package com.diamong.mychatapplication;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    public static final String REQUEST_STATE_SENT = "request_sent";
    public static final String CURRENT_STATE_NEW = "new";
    private String receiverUserID, Current_State, sender_userID;

    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button sendMessageRequestButton;

    private DatabaseReference UserRef, ChatRequestRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");


        receiverUserID = getIntent().getExtras().get("visit_user_id").toString();
        sender_userID = mAuth.getCurrentUser().getUid();

        Toast.makeText(this, "User Id :   " + receiverUserID, Toast.LENGTH_SHORT).show();

        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileName = findViewById(R.id.visit_user_name);
        userProfileStatus = findViewById(R.id.visit_profile_status);
        Current_State = CURRENT_STATE_NEW;

        sendMessageRequestButton = findViewById(R.id.send_message_request_button);


        RetrieveUserInfo();
    }

    private void RetrieveUserInfo() {
        UserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("image"))) {
                    String userImage = dataSnapshot.child("image").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequest();

                } else {
                    String userStatus = dataSnapshot.child("status").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequest();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void ManageChatRequest() {

        ChatRequestRef.child(sender_userID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(receiverUserID)) {
                            String request_type =
                                    dataSnapshot.child(receiverUserID).child("request_type").getValue().toString();

                            if (request_type.equals("sent")) {
                                Current_State = "request_sent";
                                sendMessageRequestButton.setText(getString(R.string.cancel_chat_button));

                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        if (!sender_userID.equals(receiverUserID)) {
            sendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendMessageRequestButton.setEnabled(false);
                    if (Current_State.equals(CURRENT_STATE_NEW)) {
                        SendChatRequest();
                    }

                    if (Current_State.equals(REQUEST_STATE_SENT)) {
                        CancelChatRequest();
                    }
                }
            });
        } else {
            sendMessageRequestButton.setVisibility(View.INVISIBLE);
        }

    }

    private void CancelChatRequest() {
        ChatRequestRef.child(sender_userID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            ChatRequestRef.child(receiverUserID).child(sender_userID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                sendMessageRequestButton.setEnabled(true);
                                                Current_State = CURRENT_STATE_NEW;
                                                sendMessageRequestButton.setText(getString(R.string.send_message_button));
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void SendChatRequest() {
        ChatRequestRef.child(sender_userID).child(receiverUserID)
                .child("request_type").setValue("sent")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            ChatRequestRef.child(receiverUserID).child(sender_userID)
                                    .child("request_type").setValue("received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                sendMessageRequestButton.setEnabled(true);
                                                Current_State = REQUEST_STATE_SENT;
                                                sendMessageRequestButton.setText(getString(R.string.cancel_chat_button));
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
