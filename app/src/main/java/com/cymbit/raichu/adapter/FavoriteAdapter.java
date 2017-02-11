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
import com.marshalchen.ultimaterecyclerview.UltimateGridLayoutAdapter;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;
import com.varunest.sparkbutton.SparkButton;
import com.varunest.sparkbutton.SparkEventListener;

import org.parceler.Parcels;

import java.util.List;

public class FavoriteAdapter extends UltimateGridLayoutAdapter {
    private List<Favorites> favorites;
    private Context mContext;
    private static Bus bus;

    public FavoriteAdapter(List<Favorites> favorites) {
        super(favorites);
        this.favorites = favorites;

        bus = MainActivity.bus;
        bus.register(this);
    }

    @Override
    public int getAdapterItemCount() {
        return favorites.size();
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (position < getItemCount() && (customHeaderView != null ? position <= favorites.size() : position < favorites.size()) && (customHeaderView == null || position > 0)) {
            final Favorites favorite = favorites.get(customHeaderView != null ? position - 1 : position);

            ((ViewHolder) holder).textTitleView.setText(favorite.getTitle());
            String sub = mContext.getResources().getString(R.string.sub_prefix) + favorite.getSub();
            ((ViewHolder) holder).textSubView.setText(sub);
            Picasso.with(mContext).load(favorite.getImageUrl()).fit().centerCrop().into(((ViewHolder) holder).imageView);
            ((ViewHolder) holder).heartButton.setChecked(true);
            ((ViewHolder) holder).heartButton.setEventListener(new SparkEventListener() {
                @Override
                public void onEvent(ImageView button, boolean buttonState) {
                    Favorites.findById(Favorites.class, favorite.getId()).delete();
                    favorites.remove(favorite);
                    notifyDataSetChanged();

                    MainActivity.OttoData t = new MainActivity.OttoData();
                    t.favorites = favorites;
                    t.action = "favorite_explore";
                    bus.post(t);
                    t.action = "favorite";
                    bus.post(t);

                    Snackbar snackbar = Snackbar.make(holder.itemView, mContext.getResources().getString(R.string.favorite_remove), Snackbar.LENGTH_SHORT);
                    snackbar.show();
                }
            });
            ((ViewHolder) holder).imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickItem(favorite);
                }
            });
            ((ViewHolder) holder).textLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickItem(favorite);
                }
            });
        }
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

    private void onClickItem(Favorites favorite) {
        Listing listing = new Listing(favorite.getID(), favorite.getDomain(), favorite.getScore(), favorite.isNSFW(), favorite.getImageUrl(), favorite.getLink(), favorite.getComments(), favorite.getCreated(), favorite.getSource(), favorite.getTitle(), favorite.getAuthor(), favorite.getSub(), null);
        Intent myIntent = new Intent(mContext, ImageViewActivity.class);
        myIntent.putExtra("favorite", Parcels.wrap(listing));
        mContext.startActivity(myIntent);
    }

    public void clear() {
        clearInternal(favorites);
    }

    @Override
    public Favorites getItem(int position) {
        if (customHeaderView != null)
            position--;
        // URLogs.d("position----"+position);
        if (position >= 0 && position < favorites.size())
            return favorites.get(position);
        else return null;
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
