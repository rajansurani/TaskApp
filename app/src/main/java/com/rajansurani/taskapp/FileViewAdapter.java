package com.rajansurani.taskapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rajansurani.taskapp.Model.Files;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FileViewAdapter extends RecyclerView.Adapter<FileViewAdapter.ViewHolder> {

    private List<Files> mFilesList;

    public FileViewAdapter(List<Files> filesList) {
        mFilesList = filesList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem= layoutInflater.inflate(R.layout.file_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(listItem);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final Files myListData =  mFilesList.get (position);
        holder.filename.setText(myListData.getName ());
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFilesList.remove (position);
                notifyDataSetChanged ();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFilesList.size ();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView filename;
        public ImageView delete;
        public ViewHolder(View itemView) {
            super(itemView);
            filename = itemView.findViewById (R.id.tv_file_name);
            delete = itemView.findViewById (R.id.delete_file);
        }
    }
}
