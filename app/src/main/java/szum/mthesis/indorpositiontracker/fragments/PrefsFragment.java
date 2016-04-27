package szum.mthesis.indorpositiontracker.fragments;

import android.os.Bundle;

import szum.mthesis.indorpositiontracker.R;

/**
 * Created by blazej on 2/26/2016.
 */
public class PrefsFragment extends android.support.v7.preference.PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);
    }
}