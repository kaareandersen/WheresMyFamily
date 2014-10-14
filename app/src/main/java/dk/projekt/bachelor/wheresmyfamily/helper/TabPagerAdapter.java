package dk.projekt.bachelor.wheresmyfamily.helper;

/**
 * Created by KaareAndersen on 13/10/14.
 */
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import dk.projekt.bachelor.wheresmyfamily.fragments.CalenderFragment;
import dk.projekt.bachelor.wheresmyfamily.fragments.MapFragment;
import dk.projekt.bachelor.wheresmyfamily.fragments.OverviewFragment;

public class TabPagerAdapter extends FragmentStatePagerAdapter {
    public TabPagerAdapter(FragmentManager fm) {
        super(fm);
    }
    @Override
    public Fragment getItem(int i) {
        switch (i) {
            case 0:
                //Fragement for Android Tab
                return new OverviewFragment();
            case 1:
                //Fragment for Ios Tab
                return new MapFragment();
            case 2:
                //Fragment for Windows Tab
                return new CalenderFragment();
        }
        return null;
    }
    @Override
    public int getCount() {
        return 3; //No of Tabs
    }
}
