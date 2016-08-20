package ru.o2genum.howtosay;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;

/**
 * Created by o2genum on 04/07/15.
 */
public abstract class SearchableFragment extends Fragment implements SearchView.OnQueryTextListener,
        SearchView.OnSuggestionListener {

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }

    @Override
    public boolean onSuggestionSelect(int position) {
        return false;
    }

    @Override
    public boolean onSuggestionClick(int position) {
        return false;
    }
}
