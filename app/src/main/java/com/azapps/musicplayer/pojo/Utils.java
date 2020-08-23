package com.azapps.musicplayer.pojo;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import static com.azapps.musicplayer.pojo.Constant.FRAGMENT_TAG;

public class Utils {

    public static void replaceFragments(Fragment fragment, FragmentManager fragmentManager, int container) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .replace(container, fragment, FRAGMENT_TAG)
                .addToBackStack(null)
                .commit();
    }

}
