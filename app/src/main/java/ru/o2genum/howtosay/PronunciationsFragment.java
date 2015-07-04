package ru.o2genum.howtosay;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by o2genum on 04/07/15.
 */
public class PronunciationsFragment extends SearchableFragment {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private Adapter mAdapter;

    public static PronunciationsFragment newInstance(int sectionNumber) {
        PronunciationsFragment fragment = new PronunciationsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        Toast.makeText(getActivity(), query, Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pronunciations, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.pronunciations_recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.pronunciations_swipe_refresh_layout);

        mSwipeRefreshLayout.setEnabled(false);

        mAdapter = new Adapter(new String[]{});
        mRecyclerView.setAdapter(mAdapter);
        return rootView;
    }

    public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
        private String[] mDataset;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public View mRootView;
            public TextView mLanguageTextView;
            public TextView mUsernameAndCountryTextView;
            public TextView mStarsTextView;

            public ViewHolder(View rootView, TextView languageTextView, TextView usernameAndCountryTextView,
                              TextView starsTextView) {
                super(rootView);
                mLanguageTextView = languageTextView;
                mUsernameAndCountryTextView = usernameAndCountryTextView;
                mStarsTextView = starsTextView;
            }
        }

        public Adapter(String[] myDataset) {
            mDataset = myDataset;
        }

        @Override
        public Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pronunciations_item, parent, false);
            ViewHolder vh = new ViewHolder(v,
                    (TextView) v.findViewById(R.id.pronunciation_language),
                    (TextView) v.findViewById(R.id.pronunciation_username_and_country),
                    (TextView) v.findViewById(R.id.pronunciation_stars));
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.mLanguageTextView.setText("English");
            holder.mUsernameAndCountryTextView.setText("o2genum (Russia)");
            holder.mStarsTextView.setText("★★★★★");
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return 100;
            //return mDataset.length;
        }
    }
}
