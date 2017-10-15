package com.chenghanlin.flashchatnewfirebase;


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

public class ChatListAdapter extends BaseAdapter{
    private Activity mActivity;
    private DatabaseReference mDatabaseReference;
    private String mDisplayName;
    private ArrayList<DataSnapshot> mDataSnapshotList;

    private ChildEventListener mListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
//         dataSnapshot in form of JSON
            mDataSnapshotList.add(dataSnapshot);
//         notify ListView to refresh itself
            notifyDataSetChanged();
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    public ChatListAdapter(Activity activity, DatabaseReference ref, String name){
        mActivity = activity;
        mDisplayName = name;
        mDatabaseReference = ref.child("messages");
        mDatabaseReference.addChildEventListener(mListener);

        mDataSnapshotList = new ArrayList<DataSnapshot>();
    }

    static class ViewHolder{
        TextView authorName;
        TextView body;
        LinearLayout.LayoutParams params;
    }

    //ListView will ask ChatListAdapter with these methods:

    @Override
    public int getCount() {
        return mDataSnapshotList.size();
    }

    @Override
    public InstantMessage getItem(int position) {
        DataSnapshot snapshot = mDataSnapshotList.get(position);
        //convert snapshot in JSON into InstantMessage obj
        return snapshot.getValue(InstantMessage.class);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        phone will not hold all chat rows in list in memory, only those displayed on screen and few above and below screen
//        convertView (if not null) is a ListView already created/available and
// Adapter can recycle/reuse the chat row ListView layout, just populate layout fields with updated  values
        if(convertView==null){
            //inflate means pass the XML, for layout creation
            LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.chat_msg_row, parent, false);
            final ViewHolder holder = new ViewHolder();
            holder.authorName =(TextView) convertView.findViewById(R.id.author);
            holder.body =(TextView) convertView.findViewById(R.id.message);
            holder.params = (LinearLayout.LayoutParams) holder.authorName.getLayoutParams();
            //temporarily store ViewHolder, a chat row list item, in the convertView
            convertView.setTag(holder);
        }

        //current position of chat message in list
        final InstantMessage message= getItem(position);
        //retrieve the Viewholder saved in convertView
        final ViewHolder holder = (ViewHolder) convertView.getTag();

        boolean isMe = message.getAuthor().equals(mDisplayName);
        setChatRowAppearance(isMe, holder);

        //replace old data in ViewHolder with current data
        String author = message.getAuthor();
        holder.authorName.setText(author);

        String msg = message.getMessage();
        holder.body.setText(msg);

        return convertView;
    }

    private  void  setChatRowAppearance(boolean isItMe, ViewHolder holder){
        if(isItMe){
            //set gravity to align message to the right
            holder.params.gravity= Gravity.END;
            holder.authorName.setTextColor(Color.rgb(0,180,0));
            holder.body.setBackgroundResource(R.drawable.bubble2);
        } else {
            //set gravity to align message to the left
            holder.params.gravity = Gravity.START;
            holder.authorName.setTextColor(Color.BLUE);
            holder.body.setBackgroundResource(R.drawable.bubble1);
        }

        holder.authorName.setLayoutParams(holder.params);
        holder.body.setLayoutParams(holder.params);

    }

    //when app leaves foreground, stop Adapter from checking for events from Firebase database
    public void cleanUp(){
        mDatabaseReference.removeEventListener(mListener);
    }
}
