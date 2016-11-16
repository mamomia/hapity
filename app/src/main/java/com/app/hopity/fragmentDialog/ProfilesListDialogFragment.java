package com.app.hopity.fragmentDialog;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.app.hopity.R;
import com.app.hopity.ViewProfileActivity;
import com.app.hopity.models.UserInfo;
import com.app.hopity.utils.ImageUtils;
import com.pkmmte.view.CircularImageView;

/**
 * Created by Mushi on 2/16/2016.
 */
public class ProfilesListDialogFragment extends DialogFragment {

    UserInfo[] listItems;
    ListView mylist;

    @SuppressLint("ValidFragment")
    public ProfilesListDialogFragment(UserInfo[] items) {
        listItems = items;
    }

    public ProfilesListDialogFragment() {
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
        mylist.setAdapter(new CustomFFAdapter(getActivity(), listItems));
    }

    private void startViewProfileActivity(String sid) {

        dismiss();

        Intent i = new Intent(getActivity(), ViewProfileActivity.class);
        // inserting data
        i.putExtra("user_id_ViewProfile", sid);
        startActivityForResult(i, 0);
        getActivity().overridePendingTransition(R.anim.lefttoright, R.anim.stable);
    }

    // Followers Followers Listview Adaptors
    public class CustomFFAdapter extends BaseAdapter {
        UserInfo[] result;
        Context context;
        private LayoutInflater inflater = null;

        public CustomFFAdapter(Activity mainActivity, UserInfo[] data) {
            // TODO Auto-generated constructor stub
            result = data;
            context = mainActivity;
            inflater = (LayoutInflater) context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return result.length;
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub
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
                ImageUtils.loadWebImageIntoCircular(holder.img, result[position].profile_picture, getContext(), 80, 80);

            rowView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
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
}