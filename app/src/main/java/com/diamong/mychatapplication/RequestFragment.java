package com.diamong.mychatapplication;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {
    public static final String CHAT_REQUESTS = "Chat Requests";
    public static final String REQUEST_TYPE = "request_type";
    public static final String RECEIVED = "received";
    public static final String USERS = "Users";
    public static final String IMAGE = "image";
    public static final String NAME = "name";
    public static final String STATUS = "status";

    private View RequestFragmentView;
    private RecyclerView myRequestsList;

    private DatabaseReference ChatRequestRef,UsersRef;
    private FirebaseAuth mAuth;
    private String currentUserID;


    public RequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        RequestFragmentView= inflater.inflate(R.layout.fragment_request, container, false);

        ChatRequestRef= FirebaseDatabase.getInstance().getReference().child(CHAT_REQUESTS);
        UsersRef=FirebaseDatabase.getInstance().getReference().child(USERS);
        mAuth = FirebaseAuth.getInstance();
        currentUserID=mAuth.getCurrentUser().getUid();


        myRequestsList=RequestFragmentView.findViewById(R.id.chat_request_list);
        myRequestsList.setLayoutManager(new LinearLayoutManager(getContext()));


        return RequestFragmentView;
    }


    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerOptions<Contacts> options =
                new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(ChatRequestRef.child(currentUserID),Contacts.class)
                .build();


        FirebaseRecyclerAdapter<Contacts,RequestViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options ) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestViewHolder holder, int position, @NonNull Contacts model) {
                        holder.itemView.findViewById(R.id.requst_accpt_button).setVisibility(View.VISIBLE);
                        holder.itemView.findViewById(R.id.requst_cancel_button).setVisibility(View.VISIBLE);


                        final String list_user_id=getRef(position).getKey();

                        DatabaseReference getTypeRef=getRef(position).child(REQUEST_TYPE).getRef();

                        getTypeRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                if (dataSnapshot.exists()){
                                    String type = dataSnapshot.getValue().toString();

                                    if (type.equals(RECEIVED)){

                                        UsersRef.child(list_user_id).addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                                if (dataSnapshot.hasChild(IMAGE)){

                                                    final String requestUserName = dataSnapshot.child(NAME).getValue().toString();
                                                    final String requestUserStatus = dataSnapshot.child(STATUS).getValue().toString();
                                                    final String requestUserProfileImage = dataSnapshot.child(IMAGE).getValue().toString();

                                                    holder.userName.setText(requestUserName);
                                                    holder.userStatus.setText(requestUserStatus);
                                                    Picasso.get()
                                                            .load(requestUserProfileImage)
                                                            .placeholder(R.drawable.profile_image)
                                                            .into(holder.profile_Image);
                                                }else{
                                                    final String requestUserName = dataSnapshot.child(NAME).getValue().toString();
                                                    final String requestUserStatus = dataSnapshot.child(STATUS).getValue().toString();

                                                    holder.userName.setText(requestUserName);
                                                    holder.userStatus.setText(requestUserStatus);
                                                }
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                            }
                                        });
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }

                    @NonNull
                    @Override
                    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view =LayoutInflater
                                .from(viewGroup.getContext())
                                .inflate(R.layout.users_display_layout,viewGroup,false);
                        RequestViewHolder holder = new RequestViewHolder(view);
                        return holder;
                    }
                };

        myRequestsList.setAdapter(adapter);
        adapter.startListening();

    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder{
        TextView userName, userStatus;
        CircleImageView profile_Image;
        Button AcceptButton, CancelButton;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.user_profile_name);
            userStatus=itemView.findViewById(R.id.user_status);
            profile_Image=itemView.findViewById(R.id.users_profile_image);
            AcceptButton=itemView.findViewById(R.id.requst_accpt_button);
            CancelButton=itemView.findViewById(R.id.requst_cancel_button);
        }
    }
}
