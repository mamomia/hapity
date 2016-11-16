package com.app.hopity.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.app.hopity.R;
import com.app.hopity.ViewProfileActivity;
import com.app.hopity.appdata.Constants;
import com.app.hopity.models.UserInfo;
import com.app.hopity.modelsResponse.ErrorModel;
import com.app.hopity.modelsResponse.FollowUnfollowResponse;
import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import net.lateralview.simplerestclienthandler.RestClientManager;
import net.lateralview.simplerestclienthandler.base.RequestCallbacks;
import net.lateralview.simplerestclienthandler.base.RequestHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * Created by Mushi on 3/29/2016.
 */
public class RecyclerPeopleListAdapter extends RecyclerView.Adapter<RecyclerPeopleListAdapter.ViewHolder> {
    private final String mAppUserId;
    private final String mAppToken;
    private List<UserInfo> items = new ArrayList<>();
    private Context context;

    public RecyclerPeopleListAdapter(Context context, String appUserId, String token) {
        this.context = context;
        mAppUserId = appUserId;
        mAppToken = token;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.people_list_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int index) {
        final int position = holder.getAdapterPosition();
        final UserInfo item = items.get(position);

        holder.ivAddUser.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // start follow unfollow task
                if (item.is_following.equalsIgnoreCase("yes")) {
                    holder.ivAddUser.setImageResource(R.drawable.img_add_user);

                    String urlToUnfollow = Constants.UNFOLLOW_USER_URL
                            + "token=" + mAppToken
                            + "&follower_id=" + mAppUserId
                            + "&following_id=" + item.user_id;
                    new FollowUnfollowTask().execute(urlToUnfollow, "" + position);
                } else {
                    holder.ivAddUser.setImageResource(R.drawable.img_added_user);

                    String urlToFollow = Constants.FOLLOW_USER_URL
                            + "token=" + mAppToken
                            + "&follower_id=" + mAppUserId
                            + "&following_id=" + item.user_id;
                    new FollowUnfollowTask().execute(urlToFollow, "" + position);
                }
                return false;
            }
        });

        if (item.is_following.equalsIgnoreCase("yes")) {
            holder.ivAddUser.setImageResource(R.drawable.img_added_user);
        } else {
            holder.ivAddUser.setImageResource(R.drawable.img_add_user);
        }

        holder.username.setText(item.username);
        holder.followers.setText(item.follower_count + " Followers");
        holder.following.setText(item.following_count + " Following");

        holder.progressBar.setVisibility(View.VISIBLE);
        if (!item.profile_picture.isEmpty()) {
            Picasso.with(context).
                    load(item.profile_picture).
                    placeholder(R.drawable.com_facebook_profile_picture_blank_square).
                    resize(1024, 1024).
                    into(holder.ivProfilePic, new Callback() {
                        @Override
                        public void onSuccess() {
                            holder.progressBar.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onError() {
                            holder.progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
        } else
            holder.progressBar.setVisibility(View.INVISIBLE);

        holder.ivProfilePic.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Intent i = new Intent(context, ViewProfileActivity.class);
                // inserting data
                i.putExtra("user_id_ViewProfile", item.user_id + "");
                context.startActivity(i);
                return false;
            }
        });

        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, ViewProfileActivity.class);
                // inserting data
                i.putExtra("user_id_ViewProfile", item.user_id + "");
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public UserInfo getItemAt(int index) {
        if (getItemCount() > index)
            return items.get(index);
        else
            return null;
    }

    public void add(UserInfo res) {
        items.add(0, res);
        notifyItemInserted(0);
    }

    public void removeAll() {
        if (items.isEmpty()) return;
        items.clear();
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username;
        public TextView followers;
        public TextView following;
        public CircularImageView ivProfilePic;
        public ImageView ivAddUser;
        public ProgressBar progressBar;

        public ViewHolder(View view) {
            super(view);
            username = (TextView) view.findViewById(R.id.tvUsernamePeopleItem);
            followers = (TextView) view.findViewById(R.id.tvNumberFollowersPeopleItem);
            following = (TextView) view.findViewById(R.id.tvNumberFollowingPeopleItem);
            ivProfilePic = (CircularImageView) view.findViewById(R.id.ivProfilePicPeopleItem);
            ivAddUser = (ImageView) view.findViewById(R.id.ivAddUserPeopleItem);
            progressBar = (ProgressBar) view.findViewById(R.id.progressBarPeopleItem);
        }
    }

    private class FollowUnfollowTask extends AsyncTask<String, Void, String> {
        private SweetAlertDialog progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
            progress.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            progress.setTitleText("Please wait..");
            progress.setCancelable(false);
            progress.show();
        }//close onPreExecute

        @Override
        protected String doInBackground(final String... params) {
            String url = params[0];
            final int position = Integer.parseInt(params[1]);

            RestClientManager.getInstance().makeJsonRequest(Request.Method.GET,
                    url,
                    new RequestHandler<>(new RequestCallbacks<FollowUnfollowResponse, ErrorModel>() {
                        @Override
                        public void onRequestSuccess(FollowUnfollowResponse response) {
                            progress.dismiss();
                            UserInfo item = items.remove(position);
                            if (item.is_following.equalsIgnoreCase("yes")) {
                                item.is_following = "no";
                            } else {
                                item.is_following = "yes";
                            }
                            items.add(position, item);
                            notifyDataSetChanged();
                        }

                        @Override
                        public void onRequestError(ErrorModel error) {
                            progress.dismiss();
                        }

                    }, new HashMap<String, String>()));

            return "";
        }//close doInBackground
    }// close validateUserTask
}
