package com.appify.umcaapp.recycler_adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.appify.umcaapp.R;
import com.appify.umcaapp.fragments.DirectoryFragment;
import com.appify.umcaapp.model.Member;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

public class MembersRecyclerAdapter extends RecyclerView.Adapter<MembersRecyclerAdapter.Holder>{
    private ArrayList<Member> members;
    private Context context;
    private DirectoryFragment.OnFragmentInteractionListener mListener;

    public MembersRecyclerAdapter(ArrayList<Member> members, DirectoryFragment.OnFragmentInteractionListener mListener) {
        this.members = members;
        this.mListener = mListener;
    }
    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.directory_row_layout, parent, false);
        Holder holder = new Holder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        final Member member = members.get(position);

        Glide.with(context).load(member.getImageUrl())
                .apply(RequestOptions.circleCropTransform())
                .into(holder.imageView);
        holder.nameTv.setText(member.getName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "already in holder.itemView", Toast.LENGTH_SHORT).show();
                if (mListener != null) {
                    Toast.makeText(context, "mListener is not null", Toast.LENGTH_SHORT).show();
                    mListener.onMemberClick(member);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView nameTv;

        public Holder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.image_view);
            nameTv = itemView.findViewById(R.id.name_tv);
        }
    }
}
