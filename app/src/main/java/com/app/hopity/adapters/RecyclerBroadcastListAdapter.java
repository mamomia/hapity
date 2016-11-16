package com.app.hopity.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.hopity.R;
import com.app.hopity.ViewProfileActivity;
import com.app.hopity.models.BroadcastSingle;
import com.app.hopity.playingActivities.OfflineStreamBroadcastActivity;
import com.app.hopity.playingActivities.OnlineStreamBroadcastActivity;
import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mushi on 3/29/2016.
 */
public class RecyclerBroadcastListAdapter extends RecyclerView.Adapter<RecyclerBroadcastListAdapter.ViewHolder> {
    private List<BroadcastSingle> items = new ArrayList<>();
    private Context context;

    public RecyclerBroadcastListAdapter(Context context) {
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.broadcast_list_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int i) {
        final BroadcastSingle item = items.get(i);
        final int pos = viewHolder.getAdapterPosition();

        viewHolder.share.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Watch my Broadcast at  " + items.get(pos).share_url);
                sendIntent.setType("text/plain");
                context.startActivity(Intent.createChooser(sendIntent, "Share this broadcast with..."));
                return false;
            }
        });

        viewHolder.viewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startViewProfileActivity(items.get(pos).user_id);
            }
        });

        viewHolder.name.setText(item.title);
        viewHolder.username.setText(item.username);

        if (item.status.equalsIgnoreCase("Online")) {
            viewHolder.status.setVisibility(View.VISIBLE);
            viewHolder.status.setText("LIVE");
            viewHolder.status.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
        } else {
            viewHolder.status.setVisibility(View.INVISIBLE);
        }

        viewHolder.progressBar.setVisibility(View.VISIBLE);
        if (!item.broadcast_image.trim().equalsIgnoreCase("")) {
            Picasso.with(context).load(item.broadcast_image).placeholder(R.drawable.img_default_broadcast).into(viewHolder.broadcastImage, new Callback() {
                @Override
                public void onSuccess() {
                    viewHolder.progressBar.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onError() {
                    viewHolder.progressBar.setVisibility(View.INVISIBLE);
                }
            });
        } else
            viewHolder.progressBar.setVisibility(View.INVISIBLE);

        if (!item.profile_picture.trim().equalsIgnoreCase("")) {
            Picasso.with(context).
                    load(item.profile_picture).
                    placeholder(R.drawable.com_facebook_profile_picture_blank_square).
                    resize(50, 50).
                    into(viewHolder.profilepic);
        }

        viewHolder.broadcastImageOverlay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onListItemClick(item);
                return false;
            }
        });

        viewHolder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onListItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public BroadcastSingle getItemAt(int index) {
        if (getItemCount() > index)
            return items.get(index);
        else
            return null;
    }

    public void add(BroadcastSingle res) {
        items.add(0, res);
        notifyItemInserted(0);
    }

    public void removeAll() {
        if (items.isEmpty()) return;
        items.clear();
        notifyDataSetChanged();
    }

    private void startViewProfileActivity(String sid) {
        Intent i = new Intent(context, ViewProfileActivity.class);
        // inserting data
        i.putExtra("user_id_ViewProfile", sid);
        context.startActivity(i);
    }

    private void onListItemClick(final BroadcastSingle file) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String isSensitiveFlag = file.is_sensitive;
        boolean isInformFlag = prefs.getBoolean("inform_me_settings", false);

        if (isInformFlag && isSensitiveFlag.equalsIgnoreCase("yes")) {
            new AlertDialog.Builder(context)
                    .setMessage("This broadcast contains sensitive media, do you really want to continue?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        // do something when the button is clicked
                        public void onClick(DialogInterface arg0, int arg1) {
//                            removeAll();
                            if (file.status.equalsIgnoreCase("online")) {
                                Intent au = new Intent(context, OnlineStreamBroadcastActivity.class);
                                au.putExtra("bUserId", "" + file.user_id);
                                au.putExtra("broadcastId", file.id);
                                au.putExtra("bImageUrl", file.broadcast_image);
                                au.putExtra("streamPathFileName", file.stream_url);
                                au.putExtra("broadcastTitle", file.title);
                                au.putExtra("bUserProfilePic", file.profile_picture);
                                au.putExtra("bStatus", file.status);
                                au.putExtra("bShareUrl", file.share_url);
                                context.startActivity(au);
                            } else {
                                Intent au = new Intent(context, OfflineStreamBroadcastActivity.class);
                                au.putExtra("bUserId", "" + file.user_id);
                                au.putExtra("bUserName", "" + file.username);
                                au.putExtra("broadcastId", file.id);
                                au.putExtra("bImageUrl", file.broadcast_image);
                                au.putExtra("streamPathFileName", file.filename + ".mp4");
                                au.putExtra("broadcastTitle", file.title);
                                au.putExtra("bUserProfilePic", file.profile_picture);
                                au.putExtra("bStatus", file.status);
                                au.putExtra("bShareUrl", file.share_url);
                                context.startActivity(au);
                            }
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        // do something when the button is clicked
                        public void onClick(DialogInterface arg0, int arg1) {
                            arg0.dismiss();
                        }
                    })
                    .show();
        } else {
//            removeAll();
            if (file.status.equalsIgnoreCase("online")) {
                Intent au = new Intent(context, OnlineStreamBroadcastActivity.class);
                au.putExtra("bUserId", "" + file.user_id);
                au.putExtra("broadcastId", file.id);
                au.putExtra("bImageUrl", file.broadcast_image);
                au.putExtra("streamPathFileName", file.stream_url);
                au.putExtra("broadcastTitle", file.title);
                au.putExtra("bUserProfilePic", file.profile_picture);
                au.putExtra("bStatus", file.status);
                au.putExtra("bShareUrl", file.share_url);
                context.startActivity(au);
            } else {
                Intent au = new Intent(context, OfflineStreamBroadcastActivity.class);
                au.putExtra("bUserId", "" + file.user_id);
                au.putExtra("bUserName", "" + file.username);
                au.putExtra("broadcastId", file.id);
                au.putExtra("bImageUrl", file.broadcast_image);
                au.putExtra("streamPathFileName", file.filename + ".mp4");
                au.putExtra("broadcastTitle", file.title);
                au.putExtra("bUserProfilePic", file.profile_picture);
                au.putExtra("bStatus", file.status);
                au.putExtra("bShareUrl", file.share_url);
                context.startActivity(au);
            }
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView username;
        LinearLayout viewProfile;
        ImageView share;
        TextView status;
        ImageView broadcastImage;
        CircularImageView profilepic;
        RelativeLayout broadcastImageOverlay;
        ProgressBar progressBar;

        public ViewHolder(View view) {
            super(view);
            broadcastImageOverlay = (RelativeLayout) view.findViewById(R.id.broadcastImageOverlayItem);
            broadcastImage = (ImageView) view.findViewById(R.id.ivBroadcastImageItem);
            profilepic = (CircularImageView) view.findViewById(R.id.ivProfilePicItem);
            name = (TextView) view.findViewById(R.id.tvBroadcastNameItem);
            username = (TextView) view.findViewById(R.id.tvUsernameItem);
            status = (TextView) view.findViewById(R.id.tvStatusItem);
            share = (ImageView) view.findViewById(R.id.ivShareItem);
            viewProfile = (LinearLayout) view.findViewById(R.id.layoutUserBroadcastItem);
            progressBar = (ProgressBar) view.findViewById(R.id.progressBarItem);
        }
    }
}
