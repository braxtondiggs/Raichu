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
import com.cymbit.raichu.fragment.FavoriteFragment;
import com.cymbit.raichu.model.Favorites;
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
    private List<Favorites> favorites;
    private Context mContext;

    public ExploreAdapter(List<ListingData> listings, List<Favorites> favorites) {
        super(listings);
        this.listings = listings;
        this.favorites = favorites;
    }

    @Override
    public int getAdapterItemCount() {
        return listings.size();
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final Listing listing = getItem(position);
        LinearLayout textLayout = (LinearLayout) holder.itemView.findViewById(R.id.gridText);
        TextView textTitleView = (TextView) holder.itemView.findViewById(R.id.gridview_title);
        TextView textSubView = (TextView) holder.itemView.findViewById(R.id.gridview_sub);
        ImageView imageView = (ImageView) holder.itemView.findViewById(R.id.gridview_image);
        SparkButton heartButton = (SparkButton) holder.itemView.findViewById(R.id.heart_button);
        textTitleView.setText(listing.getTitle());
        textSubView.setText("/r/" + listing.getSub());
        imageView.setTag(position);
        textLayout.setTag(position);
        Picasso.with(mContext).load(listing.getImageUrl()).fit().centerCrop().into(imageView);
        for (int i = 0; i < favorites.size(); i++) {
            Favorites favorite = favorites.get(i);
            if (favorite.getID().equals(listing.getID())) {
                heartButton.setChecked(true);
            }
        }
        heartButton.setEventListener(new SparkEventListener() {
            @Override
            public void onEvent(ImageView button, boolean buttonState) {
                String status = (buttonState) ? "Added to Favorites" : "Removed from Favorites";
                if (buttonState) {
                    Favorites favorite = new Favorites(listing);
                    favorite.save();
                } else {
                    for (int i = 0; i < favorites.size(); i++) {
                        List<Favorites> favorite = Favorites.find(Favorites.class, "identifier = ?", listing.getID());
                        if (!favorite.isEmpty()) {
                            favorite.get(0).delete();
                        }
                    }
                }
                FavoriteFragment.update();
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
        View view = inflater.inflate(R.layout.gridview, parent, false);
        return new ViewHolder(view);
    }

    private class ViewHolder extends UltimateRecyclerviewViewHolder {
        ViewHolder(View view) {
            super(view);
        }
    }

    public void clearData() {
        listings.clear();
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
