package com.example.smiledetection.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smiledetection.R;
import com.example.smiledetection.model.SuitcaseFaceDetection;

import java.util.ArrayList;

public class RecyclerFacedetectAdapter extends RecyclerView.Adapter<RecyclerFacedetectAdapter.ViewHolder> {


    Context context;
    ArrayList<SuitcaseFaceDetection> faceDetectionArrayList;

    public RecyclerFacedetectAdapter(Context context, ArrayList<SuitcaseFaceDetection> faceDetectionArrayList) {
        this.context = context;
        this.faceDetectionArrayList = faceDetectionArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.custom_row,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        SuitcaseFaceDetection faceDetection=faceDetectionArrayList.get(position);

        int id=faceDetection.getId()+1;

        holder.textView1.setText("Face: "+id+"   ");
        holder.textView2.setText(faceDetection.getText()+"%");

    }

    @Override
    public int getItemCount() {

        return faceDetectionArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView1,textView2;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textView1=itemView.findViewById(R.id.item_face_detect_tv1);
            textView2=itemView.findViewById(R.id.item_face_detect_tv2);

        }
    }
}
