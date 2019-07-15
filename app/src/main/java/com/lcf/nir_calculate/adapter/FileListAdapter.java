package com.lcf.nir_calculate.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.lcf.nir_calculate.CalculateActivity;
import com.lcf.nir_calculate.R;

import java.io.File;
import java.util.List;

public class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.MyViewHolder> {

    Context context = null;
    private List<String> nameList = null;

    public FileListAdapter(Context context, List<String> nameList) {
        this.context = context;
        this.nameList = nameList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.item_main, parent, false);
        return new MyViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        Glide
                .with(context)
                .applyDefaultRequestOptions(RequestOptions.centerCropTransform())
                .load("file:///android_asset/" + nameList.get(position) + "/sample_origin.jpg")
                .transition(new DrawableTransitionOptions().crossFade())
                .into(holder.ivImg);
        holder.tvFileName.setText(nameList.get(position));
        holder.ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CalculateActivity.class);
                intent.putExtra("path", "file:///android_asset" + File.separator + nameList.get(position));
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return nameList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private final ImageView ivImg;
        private final TextView tvFileName;
        private final View ll;

        public MyViewHolder(View itemView) {
            super(itemView);
            ll = itemView.findViewById(R.id.ll);
            ivImg = itemView.findViewById(R.id.iv_img);
            tvFileName = itemView.findViewById(R.id.tv_file_name);
        }
    }
}
