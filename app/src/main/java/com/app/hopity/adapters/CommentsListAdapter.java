package com.app.hopity.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.hopity.R;
import com.app.hopity.modelsResponse.SingleComment;
import com.pkmmte.view.CircularImageView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by Mushi on 9/2/2015.
 */
public class CommentsListAdapter extends BaseAdapter {
    private Activity activity;
    private List<SingleComment> commentsItems;

    public CommentsListAdapter(Activity activity, List<SingleComment> movieItems) {
        this.activity = activity;
        this.commentsItems = movieItems;
    }

    @Override
    public int getCount() {
        return commentsItems.size();
    }

    @Override
    public Object getItem(int location) {
        return commentsItems.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        int layoutType;
        if (commentsItems.get(position).status.equalsIgnoreCase("comment"))
            layoutType = 0;
        else
            layoutType = 1;
        return layoutType;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SingleComment item = commentsItems.get(position);
        final ViewHolder holder;
        int type = getItemViewType(position);

        if (convertView == null) {
            holder = new ViewHolder();
            // Inflate the layout according to the view type
            LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (type == 0) {
                // Inflate the layout with image
                convertView = inflater.inflate(R.layout.list_row_comment, parent, false);

                holder.tvUsername = (TextView) convertView.findViewById(R.id.tvUsernameComment);
                holder.tvComment = (TextView) convertView.findViewById(R.id.tvCommentComment);
                holder.ivProfilePic = (CircularImageView) convertView.findViewById(R.id.ivProfilePicComment);
                holder.progressBar = (ProgressBar) convertView.findViewById(R.id.progressBarComment);

            } else {
                convertView = inflater.inflate(R.layout.list_row_viewer_status, parent, false);

                holder.tvUsername = (TextView) convertView.findViewById(R.id.tvUsernameCommentStatus);
                holder.tvStatus = (TextView) convertView.findViewById(R.id.tvStatusCommentStatus);
                holder.layout = (LinearLayout) convertView.findViewById(R.id.containerCommentStatus);

            }

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (type == 0) {

            Log.e("CommentAdapter", item.toString());
            holder.tvComment.setText(item.commentMsg);
            holder.tvUsername.setText(item.commenterUsername);
            holder.progressBar.setVisibility(View.VISIBLE);
            if (!item.commenterProfilePicture.isEmpty()) {
                Picasso.with(activity).
                        load(item.commenterProfilePicture).
                        placeholder(R.drawable.com_facebook_profile_picture_blank_square).
                        resize(60, 60).
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

        } else {

            holder.tvUsername.setText(item.commenterUsername);
            if (item.status.equalsIgnoreCase("join")) {
                holder.tvStatus.setText("Joined");
                holder.layout.setBackgroundResource(R.drawable.background_layout_status_joined);
            } else if (item.status.equalsIgnoreCase("left")) {
                holder.tvStatus.setText("Left");
                holder.layout.setBackgroundResource(R.drawable.background_layout_status_left);
            } else if (item.status.equalsIgnoreCase("block")) {
                holder.tvStatus.setText("Blocked");
                holder.layout.setBackgroundResource(R.drawable.background_layout_status_blocked);
            }
        }

        return convertView;
    }

    private static class ViewHolder {
        public ProgressBar progressBar;
        public TextView tvUsername;
        public TextView tvComment;
        public TextView tvStatus;
        public CircularImageView ivProfilePic;
        public LinearLayout layout;
    }
}
