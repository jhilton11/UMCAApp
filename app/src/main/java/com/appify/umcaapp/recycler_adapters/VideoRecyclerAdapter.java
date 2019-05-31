package com.appify.umcaapp.recycler_adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.appify.umcaapp.R;
import com.appify.umcaapp.fragments.VideoFragment;
import com.appify.umcaapp.model.Video;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class VideoRecyclerAdapter extends RecyclerView.Adapter<VideoRecyclerAdapter.Holder> {
    private ArrayList<Video> videos;
    private VideoFragment.OnFragmentInteractionListener mListener;
    private Context context;

    public VideoRecyclerAdapter(ArrayList<Video> videos, VideoFragment.OnFragmentInteractionListener mListener) {
        this.videos = videos;
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.video_row_layout, parent, false);
        Holder holder = new Holder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int position) {
        final Video video = videos.get(position);

        holder.titleTv.setText(video.getTitle());
        holder.authorTv.setText(video.getAuthor());
        holder.dateTv.setText(video.getDate());
        Glide.with(context).load(video.getImageUrl()).placeholder(R.drawable.video_image_avatar).into(holder.imageView);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onVideoItemClick(video);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTv, authorTv, dateTv;

        public Holder(View itemView) {
            super(itemView);

            imageView = (ImageView) itemView.findViewById(R.id.image_view);
            titleTv = (TextView) itemView.findViewById(R.id.title_tv);
            dateTv = (TextView) itemView.findViewById(R.id.date_tv);
            authorTv = (TextView)itemView.findViewById(R.id.author_tv);
        }
    }

    public void getFilteredList(ArrayList<Video> filteredVideos) {
        this.videos = filteredVideos;

        notifyDataSetChanged();
    }
}
