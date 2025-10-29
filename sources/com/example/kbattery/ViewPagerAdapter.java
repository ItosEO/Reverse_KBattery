package com.example.kbattery;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/* loaded from: classes3.dex */
public class ViewPagerAdapter extends FragmentStateAdapter {
    private static final int FRAGMENT_COUNT = 3;

    public ViewPagerAdapter(FragmentActivity fa) {
        super(fa);
    }

    @Override // androidx.viewpager2.adapter.FragmentStateAdapter
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new Fragment1();
            case 1:
                return new Fragment3();
            case 2:
                return new Fragment2();
            default:
                return new Fragment1();
        }
    }

    @Override // androidx.recyclerview.widget.RecyclerView.Adapter
    public int getItemCount() {
        return 3;
    }
}
