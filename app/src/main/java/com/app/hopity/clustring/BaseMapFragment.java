package com.app.hopity.clustring;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.androidmapsextensions.GoogleMap;
import com.androidmapsextensions.SupportMapFragment;
import com.app.hopity.R;

/**
 * Created by Mushi on 8/11/2016.
 */
public abstract class BaseMapFragment extends Fragment {

    protected GoogleMap mMap;
    private SupportMapFragment mapFragment;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        createMapFragmentIfNeeded();
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void createMapFragmentIfNeeded() {
        FragmentManager fm = getChildFragmentManager();
        mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.mapwhere);
        if (mapFragment == null) {
            mapFragment = createMapFragment();
            FragmentTransaction tx = fm.beginTransaction();
            tx.add(R.id.mapwhere, mapFragment);
            tx.commit();
        }
    }

    protected SupportMapFragment createMapFragment() {
        return SupportMapFragment.newInstance();
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = mapFragment.getExtendedMap();
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    protected GoogleMap getMap() {
        return mMap;
    }

    protected abstract void setUpMap();
}
