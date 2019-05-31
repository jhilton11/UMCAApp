package com.appify.umcaapp.recycler_adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appify.umcaapp.R;
import com.appify.umcaapp.fragments.AudioFragment;
import com.appify.umcaapp.model.Audio;

import java.util.ArrayList;
import java.util.zip.Inflater;

public class AudioRecyclerAdapter extends RecyclerView.Adapter<AudioRecyclerAdapter.Holder>{
    private AudioFragment.OnFragmentInteractionListener mListener;
    private ArrayList<Audio> audioMessages;
    private Context context;

    public AudioRecyclerAdapter(AudioFragment.OnFragmentInteractionListener mListener, ArrayList<Audio> audioMessages) {
        this.mListener = mListener;
        this.audioMessages = audioMessages;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.audiomsg_row_layout, parent, false);
        Holder holder = new Holder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        final Audio message = audioMessages.get(position);
        holder.titleTv.setText(message.getTitle());
        holder.dateTv.setText(message.getDate());
        holder.imageView.setImageResource(R.drawable.video_image_avatar);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onFragmentInteraction(message);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return audioMessages.size();
    }

    public static class Holder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleTv;
        TextView dateTv;

        public Holder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.image_view);
            dateTv = itemView.findViewById(R.id.date_tv);
            titleTv = itemView.findViewById(R.id.title_tv);
        }
    }
}
