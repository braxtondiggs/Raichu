package com.cymbit.raichu.adapter;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.cymbit.raichu.R;
import com.cymbit.raichu.model.Listing;
import com.cymbit.raichu.model.ListingData;
import com.marshalchen.ultimaterecyclerview.UltimateGridLayoutAdapter;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.squareup.picasso.Picasso;
import com.varunest.sparkbutton.SparkButton;
import com.varunest.sparkbutton.SparkEventListener;

import java.util.List;

public class GridViewAdapter extends UltimateGridLayoutAdapter {
    private List<ListingData> listings;
    private Context mContext;

    public GridViewAdapter(List<ListingData> listings) {
        super(listings);
        this.listings = listings;
    }

    @Override
    public int getAdapterItemCount() {
        return listings.size();
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        final Listing listing = getItem(position);
        TextView textTitleView = (TextView) holder.itemView.findViewById(R.id.gridview_title);
        TextView textAuthorView = (TextView) holder.itemView.findViewById(R.id.gridview_author);
        ImageView imageView = (ImageView) holder.itemView.findViewById(R.id.gridview_image);
        SparkButton heartButton = (SparkButton) holder.itemView.findViewById(R.id.heart_button);
        textTitleView.setText(listing.getTitle());
        textAuthorView.setText(listing.getAuthor());
        Picasso.with(mContext).load(listing.getImageUrl()).fit().centerCrop().into(imageView);
        heartButton.setEventListener(new SparkEventListener() {
            @Override
            public void onEvent(ImageView button, boolean buttonState) {
                String status = (buttonState) ? "Removed from Favorites" : "Added to Favorites";
                Snackbar snackbar = Snackbar.make(holder.itemView, status, Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        });
    }

    @Override
    public UltimateRecyclerviewViewHolder onCreateViewHolder(ViewGroup parent) {
        mContext = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.gridview_explore, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    class ViewHolder extends UltimateRecyclerviewViewHolder {
        public ViewHolder(View view) {
            super(view);
        }
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
