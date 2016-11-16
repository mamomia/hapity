package com.app.hopity.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.hopity.R;
import com.app.hopity.models.UserInfo;
import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Mushi on 3/4/2016.
 */
// custom adopter for viewers
public class BroadcastViewersAdapter extends BaseAdapter {
    private ArrayList<UserInfo> result;
    private Activity mContext;

    public BroadcastViewersAdapter(Activity mainActivity, ArrayList<UserInfo> data) {
        result = data;
        mContext = mainActivity;
    }

    @Override
    public int getCount() {
        return result.size();
    }

    @Override
    public Object getItem(int position) {
        return result.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Holder holder;
        final UserInfo item = result.get(position);

        if (convertView == null) {

            holder = new Holder();
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.followerfollowing_list_item, parent, false);
            // Do some initialization
            holder.tv = (TextView) convertView.findViewById(R.id.tvUsernameFFItem);
            holder.img = (CircularImageView) convertView.findViewById(R.id.ivProfilePicFFItem);
            holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBarFFItem);

            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }


        holder.tv.setText(item.username);
        holder.progressBar.setVisibility(View.VISIBLE);
        if (!item.profile_picture.isEmpty()) {
            Picasso.with(mContext).load(item.profile_picture).placeholder(R.drawable.com_facebook_profile_picture_blank_square).into(holder.img, new Callback() {
                @Override
                public void onSuccess() {
                    holder.progressBar.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onError() {
                    holder.progressBar.setVisibility(View.INVISIBLE);
                }
            });
        } else holder.progressBar.setVisibility(View.INVISIBLE);

        return convertView;
    }

    public static class Holder {
        public TextView tv;
        public CircularImageView img;
        public ProgressBar progressBar;
    }
}
