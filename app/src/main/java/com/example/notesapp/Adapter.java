package com.example.notesapp;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.List;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

    LayoutInflater inflater;
    List<Note> notes;

    Adapter(Context context, List<Note> notes){
        this.inflater = LayoutInflater.from(context);
        this.notes = notes;
    }

    @NonNull
    @Override
    public Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_list_view,parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Adapter.ViewHolder holder, int position) {


        String title = notes.get(position).getTitle();
        String date = notes.get(position).getDate();
        String time = notes.get(position).getTime();
        String id = notes.get(position).getID();
        Log.d("ID", "Id:" +id);


        holder.nTitle.setText(title);
        holder.nDate.setText(date);
        holder.nTime.setText(time);
        holder.nID.setText(String.valueOf(id));

        //check type of note (R-regular, M-MapNote, F-friend note)
        switch (notes.get(position).getNoteType()){
            case "R":
                holder.nImg.setImageResource(R.drawable.ic_baseline_note_24);
                break;
            case "M":
                holder.nImg.setImageResource(R.drawable.ic_baseline_map_24);
                break;
            case "F":
                holder.nImg.setImageResource(R.drawable.ic_baseline_person_outline_24);
                break;
            default:
                holder.nImg.setImageResource(R.drawable.ic_launcher_background);

        }



        holder.itemView.setOnClickListener(view -> {
            Intent i = new Intent(view.getContext(), Details.class);
            i.putExtra("Date", date);
            i.putExtra("Time", time);
            i.putExtra("Title", title);
            view.getContext().startActivity(i);
        });

    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView nTitle,nDate,nTime, nID;
        ImageView nImg;
        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            nTitle = itemView.findViewById(R.id.nTitle);
            nDate = itemView.findViewById(R.id.nDate);
            nTime = itemView.findViewById(R.id.nTime);
            nID = itemView.findViewById(R.id.listId);
            nImg = itemView.findViewById(R.id.imageView3);

        }

    }
}

