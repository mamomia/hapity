package com.app.hopity.fragments;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.androidmapsextensions.ClusterGroup;
import com.androidmapsextensions.ClusteringSettings;
import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.Marker;
import com.androidmapsextensions.MarkerOptions;
import com.app.hopity.R;
import com.app.hopity.appdata.Constants;
import com.app.hopity.appdata.GlobalSharedPrefs;
import com.app.hopity.clustring.BaseMapFragment;
import com.app.hopity.clustring.DemoClusterOptionsProvider;
import com.app.hopity.models.BroadcastItemListModel;
import com.app.hopity.models.BroadcastSingle;
import com.app.hopity.modelsResponse.ErrorModel;
import com.app.hopity.playingActivities.OfflineStreamBroadcastActivity;
import com.app.hopity.playingActivities.OnlineStreamBroadcastActivity;
import com.app.hopity.utils.ImageUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.mingle.sweetpick.BlurEffect;
import com.mingle.sweetpick.CustomDelegate;
import com.mingle.sweetpick.SweetSheet;

import net.lateralview.simplerestclienthandler.RestClientManager;
import net.lateralview.simplerestclienthandler.base.RequestCallbacks;
import net.lateralview.simplerestclienthandler.base.RequestHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ua.com.crosp.solutions.library.prettytoast.PrettyToast;

public class MapFragment extends BaseMapFragment implements GoogleMap.OnMarkerClickListener {

    protected static String appToken, appUserId;
    private String TAG = "Map Fragment";

    private ClusteringSettings clusterSettings;

    private SweetSheet mBroadcastsSweetSheet;
    private BroadcastSheetAdapter mSweetSheetBroadcastsAdapter;
    private List<Marker> mSweetSheetBroadcastsMarkers;

    public MapFragment() {
        // Required empty public constructor
    }

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        new GlobalSharedPrefs(getActivity());
        appToken = GlobalSharedPrefs.hapityPref.getString("appToken", "empty");
        appUserId = "" + GlobalSharedPrefs.hapityPref.getInt("userid", 0);

        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbarMap);
        toolbar.inflateMenu(R.menu.menu_map);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                new GetBroadcastsTask().execute(appToken, appUserId);
                return false;
            }
        });
        setupBroadcastsSweetSheetView(view);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        updateClusteringRadius(); // <= Assuming clustering is activated
        // Set onClick listener configured for spiderfication:
        mMap.setOnMarkerClickListener(this);
        new GetBroadcastsTask().execute(appToken, appUserId);
    }

    @Override
    protected void setUpMap() {
        updateClusteringRadius(); // <= Assuming clustering is activated
        // Set onClick listener configured for spiderfication:
        mMap.setOnMarkerClickListener(this);
        new GetBroadcastsTask().execute(appToken, appUserId);
    }

    @Override
    public void onPause() {
        super.onPause();
        System.gc();
    }

    private void moveCamera(LatLng position) {
        if (mMap != null) {
            return;
        }
        getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(position, 9f));
    }

    @Override
    public boolean onMarkerClick(com.androidmapsextensions.Marker marker) {
        // We need to figure out if it was a separate marker or a cluster marker
        if (marker.isCluster()) {
            updateClusteringRadius();
            mSweetSheetBroadcastsMarkers.clear();
            mSweetSheetBroadcastsMarkers.addAll(marker.getMarkers());
            mSweetSheetBroadcastsAdapter.notifyDataSetChanged();
            mBroadcastsSweetSheet.toggle();
            return true;
        } else if (!marker.isCluster()) {
            mSweetSheetBroadcastsMarkers.clear();
            ArrayList<Marker> tmp = new ArrayList<>();
            tmp.add(marker);
            mSweetSheetBroadcastsMarkers.addAll(tmp);
            mSweetSheetBroadcastsAdapter.notifyDataSetChanged();
            mBroadcastsSweetSheet.toggle();
            return true;
        }
        return false;
    }

    private void addMarkersAround(final BroadcastSingle broadcastSingle) {
        String latlng[] = broadcastSingle.geo_location.split(",");
        Bitmap icon = getMarkerBitmapFromView(R.drawable.m2);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(Double.parseDouble(latlng[0]),
                Double.parseDouble(latlng[1])));
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
        markerOptions.data(broadcastSingle);
        markerOptions.clusterGroup(ClusterGroup.FIRST_USER);
        getMap().addMarker(markerOptions);

        moveCamera(new LatLng(Double.parseDouble(latlng[0]),
                Double.parseDouble(latlng[1])));
    }

    private Bitmap getMarkerBitmapFromView(@DrawableRes int resId) {
        View customMarkerView = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_custom_marker, null);
        ImageView markerImageView = (ImageView) customMarkerView.findViewById(R.id.broadcast_image);
        markerImageView.setImageResource(resId);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }

    private int clusterRadiusCalculation() {
        final int minRad = 0, maxRad = 150;
        final float minRadZoom = 10F, maxRadZoom = 7.333F;

        if (mMap.getCameraPosition().zoom >= minRadZoom) {

            return minRad;

        } else if (mMap.getCameraPosition().zoom <= maxRadZoom)
            return maxRad;
        else
            // simple interpolation:
            return (int) (maxRad - (maxRadZoom - mMap.getCameraPosition().zoom) *
                    (maxRad - minRad) / (maxRadZoom - minRadZoom));
    }

    private void updateClusteringRadius() {
        if (clusterSettings == null) {
            clusterSettings = new ClusteringSettings();
            clusterSettings.clusterOptionsProvider(new DemoClusterOptionsProvider(getResources()));
            clusterSettings.addMarkersDynamically(true);
            clusterSettings.clusterSize(clusterRadiusCalculation());
            mMap.setClustering(clusterSettings.clusterOptionsProvider(new DemoClusterOptionsProvider(getResources())));
        } else {
            clusterSettings.clusterSize(clusterRadiusCalculation());
        }
    }

    //adding broadcasts of a cluster in sweet sheet
    private void setupBroadcastsSweetSheetView(View v) {
        FrameLayout layout = (FrameLayout) v.findViewById(R.id.rootFrameLayoutBroadcastList);
        mBroadcastsSweetSheet = new SweetSheet(layout);

        CustomDelegate customDelegate = new CustomDelegate(true,
                CustomDelegate.AnimationType.DuangAnimation);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_clusers_list, null, false);
        customDelegate.setCustomView(view);
        mBroadcastsSweetSheet.setDelegate(customDelegate);
        mBroadcastsSweetSheet.setBackgroundEffect(new BlurEffect(5f));
        mBroadcastsSweetSheet.setBackgroundClickEnable(true);

        ListView mListView = (ListView) view.findViewById(R.id.recyclerClustersBroadcasts);
        mSweetSheetBroadcastsMarkers = new ArrayList<>();
        mSweetSheetBroadcastsAdapter = new BroadcastSheetAdapter(getContext(), mSweetSheetBroadcastsMarkers);
        mListView.setAdapter(mSweetSheetBroadcastsAdapter);
    }

    // Broadcasts ListView Adaptors
    private static class BroadcastSheetAdapter extends BaseAdapter {
        List<Marker> result;
        Context context;
        private LayoutInflater inflater = null;

        public BroadcastSheetAdapter(Context mainActivity, List<Marker> data) {
            result = data;
            context = mainActivity;
            inflater = (LayoutInflater) context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return result.size();
        }

        @Override
        public Marker getItem(int position) {
            return result.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Holder holder;
            final BroadcastSingle item = (BroadcastSingle) result.get(position).getData();
            if (convertView == null) {
                holder = new Holder();
                convertView = inflater.inflate(R.layout.viewprofile_broadcast_item, null);
                // Do some initialization
                holder.tvName = (TextView) convertView.findViewById(R.id.tvNameViewProfileItem);
                holder.tvShare = (TextView) convertView.findViewById(R.id.tvShareViewProfileItem);
                holder.tvStatus = (TextView) convertView.findViewById(R.id.tvStatusBroadcastViewProfileItem);
                holder.img = (ImageView) convertView.findViewById(R.id.ivImageBroadcastViewProfileItem);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }

            holder.tvName.setText(item.title);
            if (item.status.equalsIgnoreCase("Online")) {
                holder.tvStatus.setText("LIVE");
                holder.tvStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
            } else {
                holder.tvStatus.setVisibility(View.INVISIBLE);
            }

            holder.tvShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "Watch my Broadcast at  " + item.share_url);
                    sendIntent.setType("text/plain");
                    context.startActivity(Intent.createChooser(sendIntent, "Share this broadcast with..."));
                }
            });

            if (!item.broadcast_image.isEmpty()) {
                ImageUtils.loadWebImageIntoSimple(holder.img, item.broadcast_image, context);
            }

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startBroadcastClicked(item);
                }
            });

            return convertView;
        }

        private void startBroadcastClicked(final BroadcastSingle file) {

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

        private static class Holder {
            public TextView tvName;
            public TextView tvShare;
            public TextView tvStatus;
            public ImageView img;
        }
    }

    // web services background async tasks
    class GetBroadcastsTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            HashMap<String, String> paramsHash = new HashMap<>();
            paramsHash.put("token", params[0]);
            paramsHash.put("user_id", params[1]);

            RestClientManager.getInstance().makeJsonRequest(Request.Method.POST,
                    Constants.GET_ALL_BROADCASTS,
                    new RequestHandler<>(new RequestCallbacks<BroadcastItemListModel, ErrorModel>() {
                        @Override
                        public void onRequestSuccess(BroadcastItemListModel response) {
                            try {
                                BroadcastSingle[] respArray = response.broadcast;
                                int size = respArray.length;
                                if (size > 0) {
                                    getMap().clear();
                                    for (BroadcastSingle item : respArray) {
                                        if (!item.geo_location.isEmpty() &&
                                                item.geo_location != null &&
                                                !item.geo_location.contains("null") &&
                                                !item.geo_location.equalsIgnoreCase("0,0")) {

                                            addMarkersAround(item);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Exception in Map Broadcast Successful Response");
                                PrettyToast.showError(getContext(), "There was a problem fetching broadcasts, Please try again");
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onRequestError(ErrorModel error) {
                            if (getContext() != null) {
                                PrettyToast.showError(getContext(), "There was a problem connecting to server, Please try again");
                            }
                        }
                    }, paramsHash));
            return "result";
        }
    }
}