package com.example.phoenix4.adapters;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.phoenix4.MyApplication;
import com.example.phoenix4.R;
import com.example.phoenix4.activities.ReportDetailsActivity;
import com.example.phoenix4.databinding.ReportListItemBinding;
import com.example.phoenix4.models.ModelReport;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {

    private static final String TAG = "ReportAdapter";

    // context
    private final Context context;

    // viewBinding
    @SuppressLint("StaticFieldLeak")
    static ReportListItemBinding reportListItemBinding;

    // arraylist to hold list of data of type ModelReport
    public static ArrayList<ModelReport> reportArrayList;

    private SpannableStringBuilder spannableStringBuilder;
    private String searchText = "";

    // progress dialog
    private final ProgressDialog progressDialog;

    public ReportAdapter(Context context, ArrayList<ModelReport> reportArrayList) {
        this.context = context;
        ReportAdapter.reportArrayList = reportArrayList;
        progressDialog = new ProgressDialog ( context );
        progressDialog.setTitle ( "Please wait..." );
        progressDialog.setCancelable ( false );
        progressDialog.setCanceledOnTouchOutside ( false );
    }

    public void filterList(ArrayList<ModelReport> filteredList, String searchText) {
        reportArrayList = filteredList;
        this.searchText = searchText;
        notifyDataSetChanged ( );
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // bind layout using view binding
        reportListItemBinding = ReportListItemBinding.inflate ( LayoutInflater.from ( context ), parent, false );
        return new ViewHolder ( reportListItemBinding.getRoot ( ) );
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // get data
        ModelReport modelReport = reportArrayList.get ( position );
        String userName = modelReport.getUserName ( );
        String email = modelReport.getEmail ( );
        Double fireLocationLat = modelReport.getFireLocationLat ( );
        Double fireLocationLong = modelReport.getFireLocationLong ( );
        Double currentLocationRegOfficeLat = modelReport.getCurrentLocationRegOfficeLat ( );
        Double currentLocationRegOfficeLong = modelReport.getCurrentLocationRegOfficeLong ( );
        Double distance = modelReport.getDistance ( );
        Long timestamp = modelReport.getTimestamp ( );
        // convert timestamp into dd/MM/yyyy format
        String formattedDateTime = MyApplication.formatTimestampToDateTime ( String.valueOf ( timestamp ) );
        // set data
        holder.textViewUserName.setText ( MessageFormat.format ( "User Name: {0}", userName ) );
        holder.textViewEmail.setText ( MessageFormat.format ( "Email: {0}", email ) );
        holder.textViewFireLocation.setText ( MessageFormat.format ( "Fire Location: {0}", fireLocationLat + ", " + fireLocationLong ) );
        holder.textViewCurrentLocationRegOffice.setText ( MessageFormat.format ( "Office Location: {0}", currentLocationRegOfficeLat + ", " + currentLocationRegOfficeLong ) );
        holder.textViewDistance.setText ( String.valueOf ( MessageFormat.format ( "Distance: {0} km", distance ) ) );
        holder.textViewTimestamp.setText ( MessageFormat.format ( "Date: {0}", formattedDateTime ) );

        holder.cardViewReportListItem.setOnLongClickListener ( v -> {
            Intent intentReportDetailsActivity = new Intent ( context, ReportDetailsActivity.class );
            intentReportDetailsActivity.putExtra ( "reportId", reportArrayList.get ( position ).getReportId ( ) );
            Log.i ( TAG, "onBindViewHolder: reportId " + reportArrayList.get ( position ).getReportId ( ) );
            context.startActivity ( intentReportDetailsActivity );
            Animatoo.animateSplit ( context );
            return false;
        } );

        animateBounceInterpolator ( holder );

        //        String toBeSearched = userName + email + distance;
        if (searchText.length ( ) > 0) {
            spannableStringBuilder = new SpannableStringBuilder ( userName );
            Pattern pattern = Pattern.compile ( searchText.toLowerCase ( ) );
            Matcher matcher = pattern.matcher ( userName.toLowerCase ( ) );
            while (matcher.find ( )) {
                ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan ( Color.rgb ( 180, 43, 81 ) );
                spannableStringBuilder.setSpan ( foregroundColorSpan, matcher.start ( ), matcher.end ( ), Spanned.SPAN_INCLUSIVE_INCLUSIVE );
            }
            holder.textViewUserName.setText ( spannableStringBuilder );
        } else {
            holder.textViewUserName.setText ( "User Name: " + userName );
        }
    }

    @Override
    public int getItemCount() {
        return reportArrayList.size ( );
    }

    public void animateBounceInterpolator(RecyclerView.ViewHolder viewHolder) {
        final Animation animAnticipateOvershoot = AnimationUtils.loadAnimation ( context, R.anim.bounce_interpolator );
        viewHolder.itemView.setAnimation ( animAnticipateOvershoot );
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        // UI views of report_list_item.xml
        private final CardView cardViewReportListItem;
        private final RelativeLayout relativeLayoutReportListItem;
        private final TextView textViewUserName;
        private final TextView textViewEmail;
        private final TextView textViewFireLocation;
        private final TextView textViewCurrentLocationRegOffice;
        private final TextView textViewDistance;
        private final TextView textViewTimestamp;

        public ViewHolder(@NonNull View itemView) {
            super ( itemView );

            cardViewReportListItem = itemView.findViewById ( R.id.cardViewReportListItem );
            relativeLayoutReportListItem = itemView.findViewById ( R.id.relativeLayoutReportListItem );
            textViewUserName = itemView.findViewById ( R.id.textViewUserName );
            textViewEmail = itemView.findViewById ( R.id.textViewEmail );
            textViewFireLocation = itemView.findViewById ( R.id.textViewFireLocation );
            textViewCurrentLocationRegOffice = itemView.findViewById ( R.id.textViewCurrentLocationRegOffice );
            textViewDistance = itemView.findViewById ( R.id.textViewDistance );
            textViewTimestamp = itemView.findViewById ( R.id.textViewTimestamp );
        }
    }
}