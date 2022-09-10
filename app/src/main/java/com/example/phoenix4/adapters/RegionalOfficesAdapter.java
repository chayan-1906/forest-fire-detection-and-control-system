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
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.phoenix4.R;
import com.example.phoenix4.activities.RegionalOfficeDetailsActivity;
import com.example.phoenix4.databinding.RegionalOfficeListItemBinding;
import com.example.phoenix4.models.ModelRegionalOffice;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegionalOfficesAdapter extends RecyclerView.Adapter<RegionalOfficesAdapter.ViewHolder> {

    private static final String TAG = "RegionalOfficesAdapter";

    // context
    private final Context context;

    // viewBinding
    @SuppressLint("StaticFieldLeak")
    static RegionalOfficeListItemBinding regionalOfficeListItemBinding;

    // arraylist to hold list of data of type ModelRegionalOffice
    public static ArrayList<ModelRegionalOffice> regionalOfficeArrayList;

    private SpannableStringBuilder spannableStringBuilder;
    private String searchText = "";

    // progress dialog
    private final ProgressDialog progressDialog;

    public RegionalOfficesAdapter(Context context, ArrayList<ModelRegionalOffice> regionalOfficeArrayList) {
        this.context = context;
        RegionalOfficesAdapter.regionalOfficeArrayList = regionalOfficeArrayList;
        progressDialog = new ProgressDialog ( context );
        progressDialog.setTitle ( "Please wait..." );
        progressDialog.setCancelable ( false );
        progressDialog.setCanceledOnTouchOutside ( false );
    }

    public void filterList(ArrayList<ModelRegionalOffice> filteredList, String searchText) {
        regionalOfficeArrayList = filteredList;
        this.searchText = searchText;
        notifyDataSetChanged ( );
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // bind layout using view binding
        regionalOfficeListItemBinding = RegionalOfficeListItemBinding.inflate ( LayoutInflater.from ( context ), parent, false );
        return new RegionalOfficesAdapter.ViewHolder ( regionalOfficeListItemBinding.getRoot ( ) );
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RegionalOfficesAdapter.ViewHolder holder, int position) {
        // get data
        ModelRegionalOffice modelRegionalOffice = regionalOfficeArrayList.get ( position );
        String userName = modelRegionalOffice.getUserName ( );
        String email = modelRegionalOffice.getEmail ( );
        Double currentLocationRegOfficeLat = modelRegionalOffice.getRegionalOfficeLat ( );
        Double currentLocationRegOfficeLong = modelRegionalOffice.getRegionalOfficeLong ( );
        Double distance = modelRegionalOffice.getDistance ( );
        // set data
        holder.textViewUserName.setText ( MessageFormat.format ( "User Name: {0}", userName ) );
        holder.textViewEmail.setText ( MessageFormat.format ( "Email: {0}", email ) );
        holder.textViewLocation.setText ( MessageFormat.format ( "Location: {0}", currentLocationRegOfficeLat + ", " + currentLocationRegOfficeLong ) );
        holder.textViewDistance.setText ( String.valueOf ( MessageFormat.format ( "Distance: {0} km", distance ) ) );

        holder.cardViewRegionalListItem.setOnLongClickListener ( v -> {
            Intent intentRegionalOfficeDetailsActivity = new Intent ( context, RegionalOfficeDetailsActivity.class );
            intentRegionalOfficeDetailsActivity.putExtra ( "regionalOfficeUid", regionalOfficeArrayList.get ( position ).getRegionalOfficeId ( ) );
            context.startActivity ( intentRegionalOfficeDetailsActivity );
            Animatoo.animateDiagonal ( context );
            return false;
        } );

        animateFallDown ( holder );

//        String toBeSearched = userName + email + distance;
        if (searchText.length ( ) > 0) {
            spannableStringBuilder = new SpannableStringBuilder ( userName );
            Pattern pattern = Pattern.compile ( searchText.toLowerCase ( ) );
            Matcher matcher = pattern.matcher ( userName.toLowerCase ( ) );
            while (matcher.find ( )) {
                ForegroundColorSpan foregroundColorSpan = new ForegroundColorSpan ( Color.rgb ( 62, 219, 240 ) );
                spannableStringBuilder.setSpan ( foregroundColorSpan, matcher.start ( ), matcher.end ( ), Spanned.SPAN_INCLUSIVE_INCLUSIVE );
            }
            holder.textViewUserName.setText ( spannableStringBuilder );
        } else {
            holder.textViewUserName.setText ( "User Name: " + userName );
        }
    }

    @Override
    public int getItemCount() {
        return regionalOfficeArrayList.size ( );
    }

    public void animateFallDown(RecyclerView.ViewHolder viewHolder) {
        final Animation animItemFallDown = AnimationUtils.loadAnimation ( context, R.anim.item_fall_down );
        viewHolder.itemView.setAnimation ( animItemFallDown );
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        // UI views of report_list_item.xml
        private final CardView cardViewRegionalListItem;
        private final RelativeLayout relativeLayoutRegionalListItem;
        private final TextView textViewUserName;
        private final TextView textViewEmail;
        private final TextView textViewLocation;
        private final TextView textViewDistance;

        public ViewHolder(@NonNull View itemView) {
            super ( itemView );

            cardViewRegionalListItem = itemView.findViewById ( R.id.cardViewRegionalListItem );
            relativeLayoutRegionalListItem = itemView.findViewById ( R.id.relativeLayoutRegionalListItem );
            textViewUserName = itemView.findViewById ( R.id.textViewUserName );
            textViewEmail = itemView.findViewById ( R.id.textViewEmail );
            textViewLocation = itemView.findViewById ( R.id.textViewLocation );
            textViewDistance = itemView.findViewById ( R.id.textViewDistance );
        }
    }
}