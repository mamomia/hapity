package com.app.hopity.fragmentDialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.app.hopity.R;
import com.app.hopity.models.BroadcastSingle;
import com.app.hopity.playingActivities.OfflineStreamBroadcastActivity;
import com.app.hopity.playingActivities.OnlineStreamBroadcastActivity;
import com.app.hopity.utils.ImageUtils;

/**
 * Created by Mushi on 2/16/2016.
 */
public class BroadcastsListDialogFragment extends DialogFragment {

    BroadcastSingle[] listItems;
    ListView mylist;

    @SuppressLint("ValidFragment")
    public BroadcastsListDialogFragment(BroadcastSingle[] items) {
        listItems = items;
    }

    public BroadcastsListDialogFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.dialog_fragment_list, null, false);
        mylist = (ListView) view.findViewById(R.id.listview_fragment_dialog);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mylist.setAdapter(new CustomBroadcastAdapter(getActivity(), listItems));
    }

    private void startPlayBroadcastActivity(final BroadcastSingle file) {

        dismiss();

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String isSensitiveFlag = file.is_sensitive;
        boolean isInformFlag = prefs.getBoolean("inform_me_settings", true);

        if (!isInformFlag && isSensitiveFlag.equalsIgnoreCase("yes")) {
            new AlertDialog.Builder(getActivity())
                    .setMessage("This broadcast contains sensitive media, do you really want to continue?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        // do something when the button is clicked
                        public void onClick(DialogInterface arg0, int arg1) {

                            if (file.status.equalsIgnoreCase("online")) {
                                Intent au = new Intent(getActivity(), OnlineStreamBroadcastActivity.class);
                                au.putExtra("bUserId", "" + file.user_id);
                                au.putExtra("broadcastId", file.id);
                                au.putExtra("bImageUrl", file.broadcast_image);
                                au.putExtra("streamPathFileName", file.stream_url);
                                au.putExtra("broadcastTitle", file.title);
                                au.putExtra("bUserProfilePic", file.profile_picture);
                                au.putExtra("bStatus", file.status);
                                au.putExtra("bShareUrl", file.share_url);
                                startActivity(au);
                            } else {
                                Intent au = new Intent(getActivity(), OfflineStreamBroadcastActivity.class);
                                au.putExtra("bUserId", "" + file.user_id);
                                au.putExtra("bUserName", "" + file.username);
                                au.putExtra("broadcastId", file.id);
                                au.putExtra("bImageUrl", file.broadcast_image);
                                au.putExtra("streamPathFileName", file.filename + ".mp4");
                                au.putExtra("broadcastTitle", file.title);
                                au.putExtra("bUserProfilePic", file.profile_picture);
                                au.putExtra("bStatus", file.status);
                                au.putExtra("bShareUrl", file.share_url);
                                startActivity(au);
                            }
                            getActivity().finish();
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
            if (file.status.equalsIgnoreCase("online")) {
                Intent au = new Intent(getActivity(), OnlineStreamBroadcastActivity.class);
                au.putExtra("bUserId", "" + file.user_id);
                au.putExtra("broadcastId", file.id);
                au.putExtra("bImageUrl", file.broadcast_image);
                au.putExtra("streamPathFileName", file.stream_url);
                au.putExtra("broadcastTitle", file.title);
                au.putExtra("bUserProfilePic", file.profile_picture);
                au.putExtra("bStatus", file.status);
                au.putExtra("bShareUrl", file.share_url);
                startActivity(au);
            } else {
                Intent au = new Intent(getActivity(), OfflineStreamBroadcastActivity.class);
                au.putExtra("bUserId", "" + file.user_id);
                au.putExtra("bUserName", "" + file.username);
                au.putExtra("broadcastId", file.id);
                au.putExtra("bImageUrl", file.broadcast_image);
                au.putExtra("streamPathFileName", file.filename + ".mp4");
                au.putExtra("broadcastTitle", file.title);
                au.putExtra("bUserProfilePic", file.profile_picture);
                au.putExtra("bStatus", file.status);
                au.putExtra("bShareUrl", file.share_url);
                startActivity(au);
            }

            getActivity().finish();
        }
    }

    // Broadcasts ListView Adaptors
    public class CustomBroadcastAdapter extends BaseAdapter {
        BroadcastSingle[] result;
        Context context;
        private LayoutInflater inflater = null;

        public CustomBroadcastAdapter(Activity mainActivity, BroadcastSingle[] data) {
            result = data;
            context = mainActivity;
            inflater = (LayoutInflater) context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return result.length;
        }

        @Override
        public BroadcastSingle getItem(int position) {
            return result[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Holder holder = new Holder();
            View rowView;

            if (convertView == null) {
                rowView = (View) inflater.inflate(R.layout.viewprofile_broadcast_item, null);
                // Do some initialization
            } else {
                rowView = convertView;
            }

            holder.tvName = (TextView) rowView.findViewById(R.id.tvNameViewProfileItem);
            holder.tvShare = (TextView) rowView.findViewById(R.id.tvShareViewProfileItem);
            holder.tvStatus = (TextView) rowView.findViewById(R.id.tvStatusBroadcastViewProfileItem);
            holder.img = (ImageView) rowView.findViewById(R.id.ivImageBroadcastViewProfileItem);

            holder.tvName.setText(result[position].title);
            if (result[position].status.equalsIgnoreCase("Online")) {
                holder.tvStatus.setText("LIVE");
                if (Build.VERSION.SDK_INT >= 23)
                    holder.tvStatus.setBackgroundColor(context.getColor(R.color.colorPrimaryDark));
                else
                    holder.tvStatus.setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
            } else {
                holder.tvStatus.setVisibility(View.INVISIBLE);
            }

            holder.tvShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "Watch my Broadcast at  " + result[position].share_url);
                    sendIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendIntent, "Share this broadcast with..."));
                }
            });

            if (!result[position].broadcast_image.isEmpty())
                ImageUtils.loadWebImageIntoSimple(holder.img, result[position].broadcast_image, getContext());

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startPlayBroadcastActivity(result[position]);
                }
            });

            return rowView;
        }

        private class Holder {
            public TextView tvName;
            public TextView tvShare;
            public TextView tvStatus;
            public ImageView img;
        }
    }
}
