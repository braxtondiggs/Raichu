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
import com.cymbit.raichu.MainActivity;
import com.cymbit.raichu.R;
import com.cymbit.raichu.model.Favorites;
import com.cymbit.raichu.model.Listing;
import com.cymbit.raichu.model.ListingsData;
import com.marshalchen.ultimaterecyclerview.UltimateGridLayoutAdapter;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;
import com.varunest.sparkbutton.SparkButton;
import com.varunest.sparkbutton.SparkEventListener;

import org.parceler.Parcels;

import java.util.List;

public class ExploreAdapter extends UltimateGridLayoutAdapter {

    private List<ListingsData.ListingData> listings;
    private Context mContext;
    private List<Favorites> favorites;
    private static Bus bus;

    public ExploreAdapter(List<ListingsData.ListingData> listings) {
        super(listings);
        this.listings = listings;

        bus = MainActivity.bus;
        bus.register(this);
    }

    @Override
    public int getAdapterItemCount() {
        return listings.size();
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (position < getItemCount() && (customHeaderView != null ? position <= listings.size() : position < listings.size()) && (customHeaderView == null || position > 0)) {
            final Listing listing = listings.get(customHeaderView != null ? position - 1 : position).getData();
            favorites = Favorites.listAll(Favorites.class);
            ((ViewHolder) holder).textTitleView.setText(listing.getTitle());
            String sub = mContext.getResources().getString(R.string.sub_prefix) + listing.getSub();
            ((ViewHolder) holder).textSubView.setText(sub);
            Picasso.with(mContext).load(listing.getImageUrl()).fit().centerCrop().into(((ViewHolder) holder).imageView);

            ((ViewHolder) holder).heartButton.setChecked(false);
            for (Favorites favorite : favorites) {
                if (favorite.getID().trim().contains(listing.getID().trim())) {
                    ((ViewHolder) holder).heartButton.setChecked(true);
                }
            }

            ((ViewHolder) holder).heartButton.setEventListener(new SparkEventListener() {
                @Override
                public void onEvent(ImageView button, boolean buttonState) {
                    String status = (buttonState) ? mContext.getResources().getString(R.string.favorite_add) : mContext.getResources().getString(R.string.favorite_remove);
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

                    MainActivity.OttoData t = new MainActivity.OttoData();
                    t.favorites = favorites;
                    t.action = "favorite";
                    bus.post(t);
                    Snackbar snackbar = Snackbar.make(holder.itemView, status, Snackbar.LENGTH_SHORT);

                    snackbar.show();
                }
            });

            ((ViewHolder) holder).imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickItem(listing);
                }
            });
            ((ViewHolder) holder).textLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickItem(listing);
                }
            });
        }
    }

    @Override
    protected void withBindHolder(UltimateRecyclerviewViewHolder holder, Object data, int position) {

    }

    @Override
    protected void bindNormal(UltimateRecyclerviewViewHolder b, Object o, int position) {

    }

    @Override
    public UltimateRecyclerviewViewHolder onCreateViewHolder(ViewGroup parent) {
        mContext = parent.getContext();
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.gridview, parent, false);
        return new ViewHolder(v);

    }

    private class ViewHolder extends UltimateRecyclerviewViewHolder {
        LinearLayout textLayout;
        TextView textTitleView;
        TextView textSubView;
        ImageView imageView;
        SparkButton heartButton;

        ViewHolder(View view) {
            super(view);
            textLayout = (LinearLayout) view.findViewById(R.id.gridText);
            textTitleView = (TextView) view.findViewById(R.id.gridview_title);
            textSubView = (TextView) view.findViewById(R.id.gridview_sub);
            imageView = (ImageView) view.findViewById(R.id.gridview_image);
            heartButton = (SparkButton) view.findViewById(R.id.heart_button);
        }
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
    public UltimateRecyclerviewViewHolder newFooterHolder(View view) {
        // return new itemCommonBinder(view, false);
        return new UltimateRecyclerviewViewHolder<>(view);
    }

    @Override
    public UltimateRecyclerviewViewHolder newHeaderHolder(View view) {
        return new UltimateRecyclerviewViewHolder<>(view);
    }

    public void clear() {
        clearInternal(listings);
    }

    private void onClickItem(Listing listing) {
        Intent myIntent = new Intent(mContext, ImageViewActivity.class);
        myIntent.putExtra("listing", Parcels.wrap(listing));
        mContext.startActivity(myIntent);
    }


    @Override
    public ListingsData.ListingData getItem(int position) {
        if (customHeaderView != null)
            position--;
        // URLogs.d("position----"+position);
        if (position >= 0 && position < listings.size())
            return listings.get(position);
        else return null;
    }

    @Subscribe
    public void getMessage(MainActivity.OttoData data) {
        if (data.action.equals("favorite_explore")) {
            favorites = data.favorites;
            notifyDataSetChanged();
        }
    }
}
