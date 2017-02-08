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
import com.cymbit.raichu.fragment.ExploreFragment;
import com.cymbit.raichu.model.Favorites;
import com.cymbit.raichu.model.Listing;
import com.marshalchen.ultimaterecyclerview.UltimateGridLayoutAdapter;
import com.marshalchen.ultimaterecyclerview.UltimateRecyclerviewViewHolder;
import com.squareup.picasso.Picasso;
import com.varunest.sparkbutton.SparkButton;
import com.varunest.sparkbutton.SparkEventListener;

import org.parceler.Parcels;

import java.util.List;

public class FavoriteAdapter extends UltimateGridLayoutAdapter implements View.OnClickListener {
    private List<Favorites> favorites;
    private Context mContext;

    public FavoriteAdapter(List<Favorites> favorites) {
        super(favorites);
        this.favorites = favorites;
    }

    @Override
    public int getAdapterItemCount() {
        return favorites.size();
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final Favorites favorite = getItem(position);
        LinearLayout textLayout = (LinearLayout) holder.itemView.findViewById(R.id.gridText);
        TextView textTitleView = (TextView) holder.itemView.findViewById(R.id.gridview_title);
        TextView textSubView = (TextView) holder.itemView.findViewById(R.id.gridview_sub);
        ImageView imageView = (ImageView) holder.itemView.findViewById(R.id.gridview_image);
        SparkButton heartButton = (SparkButton) holder.itemView.findViewById(R.id.heart_button);
        textTitleView.setText(favorite.getTitle());
        textSubView.setText("/r/" + favorite.getSub());
        imageView.setTag(position);
        textLayout.setTag(position);
        Picasso.with(mContext).load(favorite.getSource()).fit().centerCrop().into(imageView);
        heartButton.setChecked(true);
        heartButton.setEventListener(new SparkEventListener() {
            @Override
            public void onEvent(ImageView button, boolean buttonState) {
                Favorites.findById(Favorites.class, favorite.getId()).delete();
                favorites.remove(favorite);
                notifyDataSetChanged();
                ExploreFragment.update();
                Snackbar snackbar = Snackbar.make(holder.itemView, "Removed from Favorites", Snackbar.LENGTH_SHORT);

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

    @Override
    public void onClick(View view) {
        Favorites favorite = getItem((Integer) view.getTag());
        Listing listing = new Listing(favorite.getID(), favorite.getDomain(), favorite.getScore(), favorite.isNSFW(), null, favorite.getLink(), favorite.getComments(), favorite.getCreated(), favorite.getSource(), favorite.getTitle(), favorite.getAuthor(), favorite.getSub(), favorite.getPreview());
        Intent myIntent = new Intent(view.getContext(), ImageViewActivity.class);
        myIntent.putExtra("favorite", Parcels.wrap(listing));
        view.getContext().startActivity(myIntent);
    }

    @Override
    public Favorites getItem(int position) {
        return favorites.get(position);
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
