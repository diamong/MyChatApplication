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
    public static final String REQUEST_STATE_NEW = "new";
    public static final String REQUEST_STATE_RECEIVED = "request_received";
    public static final String USERS = "Users";
    public static final String CHAT_REQUESTS = "Chat Requests";
    public static final String CONTACTS = "Contacts";
    public static final String VISIT_USER_ID = "visit_user_id";
    public static final String IMAGE = "image";
    public static final String STATUS = "status";
    public static final String NAME = "name";
    public static final String REQUEST_TYPE = "request_type";
    public static final String SENT = "sent";
    public static final String RECEIVED = "received";
    public static final String SAVED = "Saved";
    public static final String REQUEST_STATE_FRIENDS = "friends";
    private String receiverUserID, Current_State, sender_userID;

    private CircleImageView userProfileImage;
    private TextView userProfileName, userProfileStatus;
    private Button SendMessageRequestButton, DeclineMessageRequestButton;

    private DatabaseReference UserRef, ChatRequestRef, ContactsRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        mAuth = FirebaseAuth.getInstance();
        UserRef = FirebaseDatabase.getInstance().getReference().child(USERS);
        ChatRequestRef = FirebaseDatabase.getInstance().getReference().child(CHAT_REQUESTS);
        ContactsRef = FirebaseDatabase.getInstance().getReference().child(CONTACTS);


        receiverUserID = getIntent().getExtras().get(VISIT_USER_ID).toString();
        sender_userID = mAuth.getCurrentUser().getUid();

        Toast.makeText(this, "User Id :   " + receiverUserID, Toast.LENGTH_SHORT).show();

        userProfileImage = findViewById(R.id.visit_profile_image);
        userProfileName = findViewById(R.id.visit_user_name);
        userProfileStatus = findViewById(R.id.visit_profile_status);
        Current_State = REQUEST_STATE_NEW;

        SendMessageRequestButton = findViewById(R.id.send_message_request_button);
        DeclineMessageRequestButton = findViewById(R.id.decline_message_request_button);


        RetrieveUserInfo();
    }

    private void RetrieveUserInfo() {
        UserRef.child(receiverUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ((dataSnapshot.exists()) && (dataSnapshot.hasChild(IMAGE))) {
                    String userImage = dataSnapshot.child(IMAGE).getValue().toString();
                    String userStatus = dataSnapshot.child(STATUS).getValue().toString();
                    String userName = dataSnapshot.child(NAME).getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(userProfileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    ManageChatRequest();

                } else {
                    String userStatus = dataSnapshot.child(STATUS).getValue().toString();
                    String userName = dataSnapshot.child(NAME).getValue().toString();

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
                                    dataSnapshot.child(receiverUserID).child(REQUEST_TYPE).getValue().toString();

                            if (request_type.equals(SENT)) {
                                Current_State = REQUEST_STATE_SENT;
                                SendMessageRequestButton.setText(getString(R.string.cancel_chat_button));

                            } else if (request_type.equals(RECEIVED)) {
                                Current_State = REQUEST_STATE_RECEIVED;
                                SendMessageRequestButton.setText(getString(R.string.accept_chat_request));
                                DeclineMessageRequestButton.setVisibility(View.VISIBLE);

                                DeclineMessageRequestButton.setEnabled(true);

                                DeclineMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        CancelChatRequest();
                                    }
                                });
                            }
                        } else{
                            ContactsRef.child(sender_userID)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.hasChild(receiverUserID)){
                                                Current_State=REQUEST_STATE_FRIENDS;
                                                SendMessageRequestButton.setText("Remove this Contact");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        if (!sender_userID.equals(receiverUserID)) {
            SendMessageRequestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SendMessageRequestButton.setEnabled(false);
                    if (Current_State.equals(REQUEST_STATE_NEW)) {
                        SendChatRequest();
                    }

                    if (Current_State.equals(REQUEST_STATE_SENT)) {
                        CancelChatRequest();
                    }
                    if (Current_State.equals(REQUEST_STATE_RECEIVED)) {
                        AcceptChatRequest();
                    }

                    if (Current_State.equals(REQUEST_STATE_FRIENDS)) {
                        RemoveSpecificContact();
                    }
                }
            });
        } else {
            SendMessageRequestButton.setVisibility(View.INVISIBLE);
        }

    }

    private void RemoveSpecificContact() {
        ContactsRef.child(sender_userID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            ContactsRef.child(receiverUserID).child(sender_userID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                SendMessageRequestButton.setEnabled(true);
                                                Current_State = REQUEST_STATE_NEW;
                                                SendMessageRequestButton.setText(getString(R.string.send_message_button));

                                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void AcceptChatRequest() {
        ContactsRef.child(sender_userID).child(receiverUserID).child(CONTACTS).setValue(SAVED)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            ContactsRef.child(receiverUserID).child(sender_userID)
                                    .child(CONTACTS).setValue(SAVED)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                ChatRequestRef.child(sender_userID).child(receiverUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    ChatRequestRef.child(receiverUserID).child(sender_userID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                    SendMessageRequestButton.setEnabled(true);
                                                                                    Current_State= REQUEST_STATE_FRIENDS;
                                                                                    SendMessageRequestButton.setText("Remove this Contact");

                                                                                    DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                                                    DeclineMessageRequestButton.setEnabled(false);

                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
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
                                                SendMessageRequestButton.setEnabled(true);
                                                Current_State = REQUEST_STATE_NEW;
                                                SendMessageRequestButton.setText(getString(R.string.send_message_button));

                                                DeclineMessageRequestButton.setVisibility(View.INVISIBLE);
                                                DeclineMessageRequestButton.setEnabled(false);
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void SendChatRequest() {
        ChatRequestRef.child(sender_userID).child(receiverUserID)
                .child(REQUEST_TYPE).setValue(SENT)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            ChatRequestRef.child(receiverUserID).child(sender_userID)
                                    .child(REQUEST_TYPE).setValue(RECEIVED)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                SendMessageRequestButton.setEnabled(true);
                                                Current_State = REQUEST_STATE_SENT;
                                                SendMessageRequestButton.setText(getString(R.string.cancel_chat_button));
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
