package com.rajansurani.taskapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rajansurani.taskapp.Model.Task;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class TaskViewAdapter extends RecyclerView.Adapter<TaskViewAdapter.ViewHolder> {

    private ArrayList<Task> mTaskArrayList;
    private ArrayList<String> mTaskID;
    private Context mContext;
    public TaskViewAdapter(ArrayList<Task> taskArrayList, Context context, ArrayList<String> taskID) {
        mTaskArrayList = taskArrayList;
        mContext = context;
        mTaskID = taskID;
    }

    @NonNull
    @Override
    public TaskViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.task_item, parent, false);
        TaskViewAdapter.ViewHolder viewHolder = new TaskViewAdapter.ViewHolder (listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewAdapter.ViewHolder holder, final int position) {
        final Task t = mTaskArrayList.get (position);
        holder.title.setText (t.getTitle ());
        holder.by.setText (t.getCreatedBy ());
        holder.card.setOnClickListener (new View.OnClickListener () {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (mContext, ViewTask.class);
                intent.putExtra ("task",t);
                intent.putExtra ("taskId",mTaskID.get (position));
                mContext.startActivity (intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTaskArrayList.size ();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title, by;
        public CardView card;
        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById (R.id.tv_title);
            by = itemView.findViewById (R.id.tv_allocatedby);
            card = itemView.findViewById (R.id.card);
        }
    }
}
