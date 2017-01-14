package com.cymbit.raichu.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.cymbit.raichu.ImageViewActivity;
import com.cymbit.raichu.R;
import com.cymbit.raichu.model.Listing;
import com.cymbit.raichu.model.ListingData;
import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GridViewAdapter extends BaseAdapter {
    private Context mContext;
    private List<ListingData> listings;

    public GridViewAdapter(Context c, List<ListingData> listings) {
        mContext = c;
        this.listings = listings;
    }

    @Override
    public int getCount() {
        return listings.size();
    }

    @Override
    public Listing getItem(int position) {
        return listings.get(position).getData();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        ViewHolder holder;
        if (view != null) {
            holder = (ViewHolder) view.getTag();
        } else {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.gridview_explore, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }
        mContext = view.getContext();
        final Listing listing = getItem(position);
        holder.textTitleView.setText(listing.getTitle());
        holder.textAuthorView.setText(listing.getAuthor());
        Picasso.with(mContext).load(listing.getImageUrl()).fit().centerCrop().into(holder.imageView);
        holder.rippleLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent myIntent = new Intent(mContext, ImageViewActivity.class);
                myIntent.putExtra("listing", Parcels.wrap(listing));
                mContext.startActivity(myIntent);
            }
        });

        return view;
    }

    static class ViewHolder {
        @BindView(R.id.gridview_title)
        TextView textTitleView;
        @BindView(R.id.gridview_author)
        TextView textAuthorView;
        @BindView(R.id.gridview_image)
        ImageView imageView;
        @BindView(R.id.rippleLayout)
        MaterialRippleLayout rippleLayout;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
