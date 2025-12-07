package com.example.vagmobile.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.vagmobile.ui.fragment.UserArtworksFragment;
import com.example.vagmobile.ui.fragment.UserExhibitionsFragment;

public class ProfilePagerAdapter extends FragmentStateAdapter {

    private final Long userId;

    public ProfilePagerAdapter(@NonNull FragmentActivity fragmentActivity, Long userId) {
        super(fragmentActivity);
        this.userId = userId;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return UserArtworksFragment.newInstance(userId);
            case 1:
                return UserExhibitionsFragment.newInstance(userId);
            default:
                return UserArtworksFragment.newInstance(userId);
        }
    }

    @Override
    public int getItemCount() {
        return 2; // Два таба: Публикации и Выставки
    }
}
