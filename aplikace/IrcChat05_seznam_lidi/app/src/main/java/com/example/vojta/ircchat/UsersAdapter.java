package com.example.vojta.ircchat;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {
    static class ViewHolder extends RecyclerView.ViewHolder {
        ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    public interface OnUserClickedListener {
        void OnUserClicked(String username);
    }

    private OnUserClickedListener mListener;
    private List<String> mUserNames;

    UsersAdapter(OnUserClickedListener listener) {
        super();
        mListener = listener;
    }

    public void setUsers(List<String> users) {
        mUserNames = users;
        notifyDataSetChanged();
    }

    public int getItemCount() {
        return mUserNames.size();
    }

    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_user, viewGroup, false);
        return new ViewHolder(view);
    }

    public void onBindViewHolder(ViewHolder viewHolder, int pos) {
        final String username = mUserNames.get(pos);
        TextView nameView = viewHolder.itemView.findViewById(R.id.userName);
        nameView.setText(username);
        nameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.OnUserClicked(username);
            }
        });
    }
}