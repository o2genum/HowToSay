package ru.o2genum.howtosay;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.quinny898.library.persistentsearch.SearchBox;

/**
 * Created by o2genum on 05/07/15.
 */
public class SearchBoxAndRecyclerViewCoordinator extends RecyclerView.OnScrollListener {
    protected float mScrollYPosition = 0;
    SearchBox mSearchBox;
    RecyclerView mRecyclerView;
    int mSearchBoxHeight = 0;

    public SearchBoxAndRecyclerViewCoordinator(SearchBox searchBox, RecyclerView recyclerView) {
        mSearchBox = searchBox;
        mRecyclerView = recyclerView;

        mRecyclerView.setPadding(mRecyclerView.getPaddingLeft(), getSearchBoxHeight(),
                mRecyclerView.getPaddingRight(), mRecyclerView.getPaddingBottom());
    }

    public int getSearchBoxHeight() {
        if (mSearchBoxHeight == 0) {
            mSearchBox.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            mSearchBoxHeight = mSearchBox.getMeasuredHeight();
        }
        return mSearchBoxHeight;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            float translation = mSearchBox.getTranslationY();
            float height = getSearchBoxHeight();
            if (-translation < height/2 || mScrollYPosition < height) {
                mSearchBox.animate().translationY(0);
            } else {
                mSearchBox.animate().translationY(-height);
            }
        }
    }

    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        mScrollYPosition += dy;
        float translationBefore = mSearchBox.getTranslationY();
        float newTranslation = translationBefore - dy;
        if (-newTranslation >= getSearchBoxHeight()) {
            newTranslation = -getSearchBoxHeight();
        } else if (-newTranslation <= 0) {
            newTranslation = 0;
        }
        mSearchBox.setTranslationY(newTranslation);
    }

    public void showSearchBox() {
        mSearchBox.animate().translationY(0);
    }
}
