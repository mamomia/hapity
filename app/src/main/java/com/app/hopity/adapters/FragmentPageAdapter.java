package com.app.hopity.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.app.hopity.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mushi on 11/23/2015.
 */
public class FragmentPageAdapter extends SmartFragmentStatePagerAdapter {
    private Context mContext;

    private List<Fragment> mFragments;
    private List<String> mFragmentTitles;
    private List<Integer> mFragmentIcons;

    public FragmentPageAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.mContext = context;
        mFragments = new ArrayList<>();
        mFragmentTitles = new ArrayList<>();
        mFragmentIcons = new ArrayList<>();
    }

    public void addFragment(Fragment fragment, String title, int drawable) {
        mFragments.add(fragment);
        mFragmentTitles.add(title);
        mFragmentIcons.add(drawable);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitles.get(position);
    }

    public View getTabView(int position) {
        View tab = LayoutInflater.from(mContext).inflate(R.layout.tabber_view, null);
        ImageView tabImage = (ImageView) tab.findViewById(R.id.tabImage);
        tabImage.setImageResource(mFragmentIcons.get(position));

        if (position == 0) {
            tab.setSelected(true);
        }

        return tab;
    }
}
