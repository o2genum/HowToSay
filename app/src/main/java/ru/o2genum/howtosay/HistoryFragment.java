package ru.o2genum.howtosay;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.quinny898.library.persistentsearch.SearchBox;

import java.util.ArrayList;

/**
 * Created by o2genum on 04/07/15.
 */
public class HistoryFragment extends SearchableFragment {

    private App mApp;
    private MainActivity mActivity;
    private RecyclerView mRecyclerView;
    private SearchBoxAndRecyclerViewCoordinator mOnScrollListener;
    private Adapter mAdapter;
    private SearchBox mSearchBox;
    private String mSearchText;
    private SearchBox.SearchListener mSearchListener;

    public static HistoryFragment newInstance() {
        HistoryFragment fragment = new HistoryFragment();
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
        mActivity = (MainActivity) getActivity();
        mApp = (App) mActivity.getApplication();
        View rootView = inflater.inflate(R.layout.fragment_history, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.history_recycler_view);

        mOnScrollListener = new SearchBoxAndRecyclerViewCoordinator(((MainActivity) getActivity()).getSearchBox(), mRecyclerView);
        mRecyclerView.addOnScrollListener(mOnScrollListener);
        mSearchBox = mActivity.getSearchBox();

        if (mSearchText != null) {
            mSearchBox.setSearchString(mSearchText);
            mSearchBox.setLogoText(mSearchText);
        } else {
            mSearchBox.setSearchString("");
            mSearchBox.setLogoText("Search in history");
        }
        mSearchListener = new SearchBox.SearchListener() {
            @Override
            public void onSearchOpened() {

            }

            @Override
            public void onSearchCleared() {
                mSearchText = "";
                mSearchBox.setLogoText("Search in history");
                mAdapter.mDataset = mApp.getHistory();
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onSearchClosed() {

            }

            @Override
            public void onSearchTermChanged() {
                mSearchText = mSearchBox.getSearchText().trim();
                if (mSearchText.isEmpty()) {
                    onSearchCleared();
                    return;
                }
                ArrayList<String> history = mApp.getHistory();
                ArrayList<String> filteredHistory = new ArrayList<String>();
                for (String word : history) {
                    if (word.contains(mSearchText.trim())) {
                        filteredHistory.add(word);
                    }
                }
                mAdapter.mDataset = filteredHistory;
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onSearch(String s) {

            }
        };
        mSearchBox.setSearchListener(mSearchListener);
        mOnScrollListener.showSearchBox();

        if (mAdapter == null) {
            mAdapter = new Adapter(mApp.getHistory());
        }
        mRecyclerView.setAdapter(mAdapter);
        return rootView;
    }


    @Override
    public void onResume() {
        super.onResume();
        mSearchBox.setSearchListener(mSearchListener);

        if (mSearchText != null && !mSearchText.isEmpty()) {
            mSearchBox.setSearchString(mSearchText);
            mSearchBox.setLogoText(mSearchText);
        } else {
            mSearchBox.setSearchString("");
            mSearchBox.setLogoText("Search in history");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mRecyclerView.removeOnScrollListener(mOnScrollListener);
        mSearchBox.setSearchListener(null);
    }

    public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
        private ArrayList<String> mDataset;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public View mRootView;
            public TextView mWordTextView;
            public CardView mCardView;

            public ViewHolder(View rootView, CardView cardView, TextView wordTextView) {
                super(rootView);
                mCardView = cardView;
                mWordTextView = wordTextView;
            }
        }

        public Adapter(ArrayList<String> myDataset) {
            mDataset = myDataset;
        }

        @Override
        public Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
            ViewHolder vh = new ViewHolder(v,
                    (CardView) v.findViewById(R.id.history_card_view),
                    (TextView) v.findViewById(R.id.history_word));
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.mWordTextView.setText(mDataset.get(mDataset.size() - 1 - position));
            holder.mCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActivity.onHistoryClicked(mDataset.get(mDataset.size() - 1 - position));
                }
            });
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.size();
        }
    }
}
