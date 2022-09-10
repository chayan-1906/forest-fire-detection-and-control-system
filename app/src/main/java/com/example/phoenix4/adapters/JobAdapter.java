package com.example.phoenix4.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phoenix4.MyApplication;
import com.example.phoenix4.databinding.JobListItemBinding;
import com.example.phoenix4.models.ModelJob;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.text.MessageFormat;
import java.util.ArrayList;

public class JobAdapter extends RecyclerView.Adapter<JobAdapter.ViewHolder> {

    private static final String TAG = "JobAdapter";

    // view binding for job_list_item.xml
    @SuppressLint("StaticFieldLeak")
    static JobListItemBinding jobListItemBinding;

    // context
    private final Context context;

    // arraylist to hold list of data of type ModelPdf
    public static ArrayList<ModelJob> jobArrayList;

    public JobAdapter(Context context, ArrayList<ModelJob> jobArrayList) {
        this.context = context;
        JobAdapter.jobArrayList = jobArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        jobListItemBinding = JobListItemBinding.inflate ( LayoutInflater.from ( context ), parent, false );
        return new ViewHolder ( jobListItemBinding.getRoot ( ) );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        /* Get data, set data */
        ModelJob modelJob = jobArrayList.get ( position );
//        String uid = modelJob.getUid ( );
        Double fireLocationLat = modelJob.getFireLocationLat ( );
        Double fireLocationLong = modelJob.getFireLocationLong ( );
        String fireLocation = fireLocationLat + ", " + fireLocationLong;
        Double distance = modelJob.getDistance ( );
        String reportTimestamp = MyApplication.formatTimestampToDateTime ( String.valueOf ( modelJob.getTimestamp ( ) ) );
        String formattedTimeSpent = String.valueOf ( modelJob.getTimeSpent ( ) / 1000 );
        if (Long.parseLong ( formattedTimeSpent ) < 60)
            formattedTimeSpent = formattedTimeSpent + " sec";
        else if (Long.parseLong ( formattedTimeSpent ) >= 60 && Long.parseLong ( formattedTimeSpent ) < 3600)
            formattedTimeSpent = formattedTimeSpent + " min";
        else if (Long.parseLong ( formattedTimeSpent ) >= 3600 && Long.parseLong ( formattedTimeSpent ) < 216000)
            formattedTimeSpent = formattedTimeSpent + " hr";
        // set data to views
        holder.textViewFireLocation.setText ( MessageFormat.format ( "Fire Location: {0}", fireLocation ) );
        holder.textViewDistance.setText ( MessageFormat.format ( "Distance: {0}", distance ) );
        holder.textViewDateTime.setText ( MessageFormat.format ( "Date: {0}", reportTimestamp ) );
        holder.textViewTimeSpent.setText ( MessageFormat.format ( "Time Spent: {0}", formattedTimeSpent ) );
    }

    @Override
    public int getItemCount() {
        return jobArrayList.size ( );
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        // UI views of job_list_item.xml
        private final TextView textViewFireLocation;
        private final TextView textViewDistance;
        private final TextView textViewDateTime;
        private final TextView textViewTimeSpent;

        public ViewHolder(@NonNull View itemView) {
            super ( itemView );

            textViewFireLocation = jobListItemBinding.textViewFireLocation;
            textViewDistance = jobListItemBinding.textViewDistance;
            textViewDateTime = jobListItemBinding.textViewDateTime;
            textViewTimeSpent = jobListItemBinding.textViewTimeSpent;
        }
    }
}
