package com.app.hopity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.app.hopity.appdata.GlobalSharedPrefs;
import com.app.hopity.models.BroadcastSingle;
import com.app.hopity.models.UserInfo;
import com.app.hopity.playingActivities.OfflineStreamBroadcastActivity;
import com.app.hopity.playingActivities.OnlineStreamBroadcastActivity;
import com.app.hopity.utils.ImageUtils;
import com.pkmmte.view.CircularImageView;

public class ViewListActivity extends AppCompatActivity {

    public static UserInfo[] followers;
    public static UserInfo[] following;
    public static UserInfo[] blocked;
    public static BroadcastSingle[] broadcasts;
    private Toolbar toolbar;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_list);

        new GlobalSharedPrefs(this);

        followers = GlobalSharedPrefs.followers;
        following = GlobalSharedPrefs.following;
        blocked = GlobalSharedPrefs.blocked;
        broadcasts = GlobalSharedPrefs.broadcasts;

        toolbar = (Toolbar) findViewById(R.id.toolbarViewList);
        toolbar.setNavigationIcon(R.drawable.img_back_arrow);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.gc();
                finish();
            }
        });

        listView = (ListView) findViewById(R.id.listViewOfViewList);

        String listToShow = getIntent().getStringExtra("listToShow");
        if (listToShow.equalsIgnoreCase("followers")) {
            showListViewOfFollowers();
        } else if (listToShow.equalsIgnoreCase("following")) {
            showListViewOfFollowing();
        } else if (listToShow.equalsIgnoreCase("blocked")) {
            showListViewOfBlocked();
        } else if (listToShow.equalsIgnoreCase("broadcasts")) {
            showListViewOfBroadcasts();
        }
    }

    private void startBroadcastActivity(final BroadcastSingle file) {

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String isSensitiveFlag = file.is_sensitive;
        boolean isInformFlag = prefs.getBoolean("inform_me_settings", true);

        if (!isInformFlag && isSensitiveFlag.equalsIgnoreCase("yes")) {
            new AlertDialog.Builder(getApplicationContext())
                    .setMessage("This broadcast contains sensitive media, do you really want to continue?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        // do something when the button is clicked
                        public void onClick(DialogInterface arg0, int arg1) {

                            if (file.status.equalsIgnoreCase("online")) {
                                Intent au = new Intent(getApplicationContext(), OnlineStreamBroadcastActivity.class);
                                au.putExtra("bUserId", "" + file.user_id);
                                au.putExtra("broadcastId", file.id);
                                au.putExtra("bImageUrl", file.broadcast_image);
                                au.putExtra("streamPathFileName", file.stream_url);
                                au.putExtra("broadcastTitle", file.title);
                                au.putExtra("bUserProfilePic", file.profile_picture);
                                au.putExtra("bStatus", file.status);
                                au.putExtra("bShareUrl", file.share_url);

                                startActivityForResult(au, 0);
                                overridePendingTransition(R.anim.lefttoright, R.anim.stable);
                            } else {
                                Intent au = new Intent(getApplicationContext(), OfflineStreamBroadcastActivity.class);
                                au.putExtra("bUserId", "" + file.user_id);
                                au.putExtra("broadcastId", file.id);
                                au.putExtra("bImageUrl", file.broadcast_image);
                                au.putExtra("streamPathFileName", file.filename + ".mp4");
                                au.putExtra("broadcastTitle", file.title);
                                au.putExtra("bUserProfilePic", file.profile_picture);
                                au.putExtra("bStatus", file.status);
                                au.putExtra("bShareUrl", file.share_url);

                                startActivityForResult(au, 0);
                                overridePendingTransition(R.anim.lefttoright, R.anim.stable);
                            }

                            System.gc();
                            finish();
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
                Intent au = new Intent(getApplicationContext(), OnlineStreamBroadcastActivity.class);
                au.putExtra("bUserId", "" + file.user_id);
                au.putExtra("broadcastId", file.id);
                au.putExtra("bImageUrl", file.broadcast_image);
                au.putExtra("streamPathFileName", file.stream_url);
                au.putExtra("broadcastTitle", file.title);
                au.putExtra("bUserProfilePic", file.profile_picture);
                au.putExtra("bStatus", file.status);
                au.putExtra("bShareUrl", file.share_url);

                startActivityForResult(au, 0);
                overridePendingTransition(R.anim.lefttoright, R.anim.stable);
            } else {
                Intent au = new Intent(getApplicationContext(), OfflineStreamBroadcastActivity.class);
                au.putExtra("bUserId", "" + file.user_id);
                au.putExtra("broadcastId", file.id);
                au.putExtra("bImageUrl", file.broadcast_image);
                au.putExtra("streamPathFileName", file.filename + ".mp4");
                au.putExtra("broadcastTitle", file.title);
                au.putExtra("bUserProfilePic", file.profile_picture);
                au.putExtra("bStatus", file.status);
                au.putExtra("bShareUrl", file.share_url);

                startActivityForResult(au, 0);
                overridePendingTransition(R.anim.lefttoright, R.anim.stable);
            }

            System.gc();
            finish();
        }
    }

    private void startViewProfileActivity(String sid) {
        Intent i = new Intent(getApplicationContext(), ViewProfileActivity.class);
        // inserting data
        i.putExtra("user_id_ViewProfile", sid);
        startActivityForResult(i, 0);
        overridePendingTransition(R.anim.lefttoright, R.anim.stable);
    }

    // show list of all followers or following of user
    private void showListViewOfBlocked() {
        // intialize and show items
        if (blocked != null && blocked.length > 0) {
            toolbar.setTitle("BLOCKED");
            toolbar.setTitleTextAppearance(this, R.style.TitleBarText);
            listView.setAdapter(new CustomFFAdapter(this, blocked));
        }
    }

    // show list of all followers or following of user
    private void showListViewOfFollowers() {
        // intialize and show items
        if (followers != null && followers.length > 0) {
            toolbar.setTitle("FOLLOWERS");
            toolbar.setTitleTextAppearance(this, R.style.TitleBarText);
            listView.setAdapter(new CustomFFAdapter(this, followers));
        }
    }


    // show list of all followers or following of user
    private void showListViewOfFollowing() {
        // intialize and show items
        if (following != null && following.length > 0) {
            toolbar.setTitle("FOLLOWING");
            toolbar.setTitleTextAppearance(this, R.style.TitleBarText);
            listView.setAdapter(new CustomFFAdapter(this, following));
        }
    }


    // show list of all followers or following of user
    private void showListViewOfBroadcasts() {
        // initialize and show items
        if (broadcasts != null && broadcasts.length > 0) {
            toolbar.setTitle("BROADCASTS");
            toolbar.setTitleTextAppearance(this, R.style.TitleBarText);
            listView.setAdapter(new CustomBroadcastAdapter(this, broadcasts));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        System.gc();
        finish();
    }

    // Followers Followers Listview Adaptors
    private class CustomFFAdapter extends BaseAdapter {
        UserInfo[] result;
        Context context;
        private LayoutInflater inflater = null;

        public CustomFFAdapter(Activity mainActivity, UserInfo[] data) {
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
        public Object getItem(int position) {
            return position;
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
                rowView = (View) inflater.inflate(R.layout.followerfollowing_list_item, null);
                // Do some initialization
            } else {
                rowView = convertView;
            }

            holder.tv = (TextView) rowView.findViewById(R.id.tvUsernameFFItem);
            holder.img = (CircularImageView) rowView.findViewById(R.id.ivProfilePicFFItem);

            holder.tv.setText(result[position].username);
            if (!result[position].profile_picture.isEmpty())
                ImageUtils.loadWebImageIntoCircular(holder.img, result[position].profile_picture, ViewListActivity.this, 80, 80);
            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startViewProfileActivity(result[position].sid);
                }
            });
            return rowView;
        }

        public class Holder {
            TextView tv;
            CircularImageView img;
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
                    holder.tvStatus.setBackgroundColor(getColor(R.color.colorPrimaryDark));
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
                ImageUtils.loadWebImageIntoSimple(holder.img, result[position].broadcast_image, ViewListActivity.this, 1024, 1024);

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startBroadcastActivity(result[position]);
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
