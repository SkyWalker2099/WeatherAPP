package com.example.zzh.androidbestpractice;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by Zzh on 2018/7/30.
 */

public class FragmentAdapter extends FragmentStatePagerAdapter {

    private List<Fragment> mFragmentList;

    private List<String> mTitles;


    public FragmentAdapter(FragmentManager fm, List<Fragment> fragmentList, List<String> mTitles){
        super(fm);
        mFragmentList = fragmentList;
        this.mTitles = mTitles;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitles.get(position);
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
    }

    public void remove(int position){
        mTitles.remove(position);
        mFragmentList.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
    }
}
