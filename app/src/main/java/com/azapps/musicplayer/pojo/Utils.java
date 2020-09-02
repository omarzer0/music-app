package com.azapps.musicplayer.pojo;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


public class Utils {

    public static void replaceFragments(Fragment fragment, FragmentManager fragmentManager, int container,String tag) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .add(container, fragment, tag)
                .addToBackStack(null)
                .commit();
    }

}
