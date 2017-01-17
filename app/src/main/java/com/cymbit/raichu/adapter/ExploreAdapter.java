package com.cymbit.raichu.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cymbit.raichu.ImageViewActivity;
import com.cymbit.raichu.R;
import com.cymbit.raichu.model.Listing;
import com.cymbit.raichu.model.ListingData;
import com.marshalchen.ultimaterecyclerview.UltimateGridLayoutAdapter;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.squareup.picasso.Picasso;
import com.varunest.sparkbutton.SparkButton;
import com.varunest.sparkbutton.SparkEventListener;

import org.parceler.Parcels;

import java.util.List;

public class ExploreAdapter extends UltimateGridLayoutAdapter implements View.OnClickListener {
    private List<ListingData> listings;
    private Context mContext;

    public ExploreAdapter(List<ListingData> listings) {
        super(listings);
        this.listings = listings;
    }

    @Override
    public int getAdapterItemCount() {
        return listings.size();
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        Listing listing = getItem(position);
        LinearLayout textLayout = (LinearLayout) holder.itemView.findViewById(R.id.gridText);
        TextView textTitleView = (TextView) holder.itemView.findViewById(R.id.gridview_title);
        TextView textAuthorView = (TextView) holder.itemView.findViewById(R.id.gridview_author);
        ImageView imageView = (ImageView) holder.itemView.findViewById(R.id.gridview_image);
        SparkButton heartButton = (SparkButton) holder.itemView.findViewById(R.id.heart_button);
        textTitleView.setText(listing.getTitle());
        textAuthorView.setText(listing.getAuthor());
        imageView.setTag(position);
        textLayout.setTag(position);
        Picasso.with(mContext).load(listing.getImageUrl()).fit().centerCrop().into(imageView);
        heartButton.setEventListener(new SparkEventListener() {
            @Override
            public void onEvent(ImageView button, boolean buttonState) {
                String status = (buttonState) ? "Removed from Favorites" : "Added to Favorites";
                Snackbar snackbar = Snackbar.make(holder.itemView, status, Snackbar.LENGTH_SHORT);
                snackbar.show();
            }
        });
        imageView.setOnClickListener(this);
        textLayout.setOnClickListener(this);
    }

    @Override
    public UltimateRecyclerviewViewHolder onCreateViewHolder(ViewGroup parent) {
        mContext = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.gridview_explore, parent, false);
        return new ViewHolder(view);
    }

    class ViewHolder extends UltimateRecyclerviewViewHolder {
        ViewHolder(View view) {
            super(view);
        }
    }

    @Override
    public void onClick(View view) {
        Listing listing = getItem((Integer) view.getTag());
        Intent myIntent = new Intent(view.getContext(), ImageViewActivity.class);
        myIntent.putExtra("listing", Parcels.wrap(listing));
        view.getContext().startActivity(myIntent);
    }

    @Override
    public Listing getItem(int position) {
        return listings.get(position).getData();
    }

    @Override
    protected int getNormalLayoutResId() {
        return 0;
    }

    @Override
    protected UltimateRecyclerviewViewHolder newViewHolder(View view) {
        return null;
    }

    @Override
    public long generateHeaderId(int position) {
        return 0;
    }

    @Override
    protected void withBindHolder(UltimateRecyclerviewViewHolder holder, Object data, int position) {

    }

    @Override
    protected void bindNormal(UltimateRecyclerviewViewHolder b, Object o, int position) {

    }
}
