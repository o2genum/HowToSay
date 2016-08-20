package ru.o2genum.howtosay;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.quinny898.library.persistentsearch.SearchBox;

import java.io.IOException;
import java.util.List;

import ru.o2genum.howtosay.api.ForvoApi;
import ru.o2genum.howtosay.api.MyApi;
import ru.o2genum.howtosay.api.dto.forvo.Pronunciation;
import ru.o2genum.howtosay.api.dto.forvo.WordPronunciations;
import ru.o2genum.howtosay.api.dto.my.ApiSettings;
import ru.o2genum.howtosay.api.dto.my.Key;

/**
 * Created by o2genum on 04/07/15.
 */
public class PronunciationsFragment extends SearchableFragment {

    private App mApp;
    private MainActivity mActivity;
    private ViewGroup mContainer;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private SearchBoxAndRecyclerViewCoordinator mOnScrollListener;
    private Adapter mAdapter;
    private SearchBox mSearchBox;
    private TextView mNoItemsText;

    private String mSearchText;
    private String mWord;
    private SearchBox.SearchListener mSearchListener;

    private boolean mAlreadyShownRateDialog = false;

    public static PronunciationsFragment newInstance() {
        PronunciationsFragment fragment = new PronunciationsFragment();
        return fragment;
    }

    public void setWord(String word) {
        mWord = word;
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
        mContainer = container;
        View rootView = inflater.inflate(R.layout.fragment_pronunciations, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.pronunciations_recycler_view);
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.pronunciations_swipe_refresh_layout);
        mNoItemsText = (TextView) rootView.findViewById(R.id.no_items_text);

        mOnScrollListener = new SearchBoxAndRecyclerViewCoordinator(((MainActivity) getActivity()).getSearchBox(), mRecyclerView);
        mRecyclerView.addOnScrollListener(mOnScrollListener);
        mSearchBox = mActivity.getSearchBox();

        if (mAdapter == null) {
            mAdapter = new Adapter(new Pronunciation[]{});
        }
        mRecyclerView.setAdapter(mAdapter);

        if (mSearchText != null) {
            mSearchBox.setSearchString(mSearchText);
            mSearchBox.setLogoText(mSearchText);
            if (mAdapter != null && mAdapter.getItemCount() == 0) {
                mNoItemsText.setText(R.string.no_pronunciations_found);
            } else {
                mNoItemsText.setText("");
            }
        } else {
            mSearchBox.setSearchString("");
            mSearchBox.setLogoText("Search here");
            mNoItemsText.setText(R.string.search_in_the_searchbar);
        }

        mSearchListener = new SearchBox.SearchListener() {
            @Override
            public void onSearchOpened() {

            }

            @Override
            public void onSearchCleared() {
                mSearchText = "";
                mSearchBox.setLogoText("Search here");
            }

            @Override
            public void onSearchClosed() {

            }

            @Override
            public void onSearchTermChanged() {

            }

            @Override
            public void onSearch(String s) {
                final String trimmed = s.trim();
                mSearchText = trimmed;

                if (mSearchText.isEmpty()) {
                    onSearchCleared();
                    return;
                }

                if (mApp.getForvoApi() == null) {
                    MyApi myApi = new MyApi(mApp.getRequestQueue(), "http://o2genum.com/forvo.json");
                    myApi.getApiSettings(new Response.Listener<ApiSettings>() {
                        @Override
                        public void onResponse(ApiSettings response) {
                            int keyIndex = mApp.getSharedPreferences().getInt("RANDOM_NUMBER", 0) % response.keys.length;
                            Key key = response.keys[keyIndex];
                            ForvoApi forvoApi = new ForvoApi(mApp.getRequestQueue(), key.host, key.key);
                            mApp.setForvoApi(forvoApi);

                            getPronunciations(trimmed);
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            ForvoApi forvoApi = new ForvoApi(mApp.getRequestQueue(),
                                    "apicommercial.forvo.com", "bd1ea80089bf823e55791eff92168548");
                            mApp.setForvoApi(forvoApi);

                            getPronunciations(trimmed);
                        }
                    });
                } else {
                    getPronunciations(trimmed);
                }

                mNoItemsText.setText("");
            }

            private void getPronunciations(final String word) {
                mSwipeRefreshLayout.setRefreshing(true);
                mApp.getForvoApi().getWordPronunciations(word, new Response.Listener<WordPronunciations>() {
                    @Override
                    public void onResponse(WordPronunciations response) {
                        if (response.items.length != 0) {
                            mApp.addToHistory(word);
                        } else {
                            mNoItemsText.setText("No pronunciations found");
                        }
                        mAdapter.setDataset(response.items);
                        mSwipeRefreshLayout.setRefreshing(false);
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                mRecyclerView.smoothScrollToPosition(0);
                            }
                        });
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        mSwipeRefreshLayout.setRefreshing(false);
                        String errorText;
                        if (error instanceof NoConnectionError) {
                            errorText = "No Internet connection";
                        } else {
                            errorText = "Error: " + error.getClass().getSimpleName();
                        }
                        Snackbar.make(mContainer, errorText, Snackbar.LENGTH_LONG).show();
                    }
                });
            }
        };
        mSearchBox.setSearchListener(mSearchListener);
        mOnScrollListener.showSearchBox();

        mSwipeRefreshLayout.setEnabled(false);
        mSwipeRefreshLayout.setProgressViewOffset(false, 0, (int) (mOnScrollListener.getSearchBoxHeight() * 1.20));

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSearchBox.setSearchListener(mSearchListener);

        if (mWord != null) {
            mSearchText = mWord;
        }

        if (mSearchText != null && !mSearchText.isEmpty()) {
            mSearchBox.setSearchString(mSearchText);
            mSearchBox.setLogoText(mSearchText);
        } else {
            mSearchBox.setSearchString("");
            mSearchBox.setLogoText("Search here");
        }

        if (mWord != null) {
            mSearchListener.onSearch(mSearchText);
            mWord = null;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mRecyclerView.removeOnScrollListener(mOnScrollListener);
        mSearchBox.setSearchListener(null);
    }

    public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
        private Pronunciation[] mDataset;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public View mRootView;
            public CardView mCardView;
            public TextView mLanguageTextView;
            public TextView mUsernameAndCountryTextView;
            public ImageView mStarsImageView;
            public ImageView mPlayIndicatorImageView;

            public MediaPlayer mMediaPlayer;

            public ViewHolder(View rootView, CardView cardView, TextView languageTextView, TextView usernameAndCountryTextView,
                              ImageView starsImageView, ImageView playIndicatorImageView) {
                super(rootView);
                mRootView = rootView;
                mCardView = cardView;
                mLanguageTextView = languageTextView;
                mUsernameAndCountryTextView = usernameAndCountryTextView;
                mStarsImageView = starsImageView;
                mPlayIndicatorImageView = playIndicatorImageView;
            }
        }

        public Adapter(Pronunciation[] dataset) {
            mDataset = dataset;
        }

        public void setDataset(Pronunciation[] dataset) {
            mDataset = dataset;
            notifyDataSetChanged();
        }

        @Override
        public Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.pronunciations_item, parent, false);
            ViewHolder vh = new ViewHolder(v,
                    (CardView) v.findViewById(R.id.pronunciation_card_view),
                    (TextView) v.findViewById(R.id.pronunciation_language),
                    (TextView) v.findViewById(R.id.pronunciation_username_and_country),
                    (ImageView) v.findViewById(R.id.pronunciation_stars),
                    (ImageView) v.findViewById(R.id.pronunciation_play_indicator));
            return vh;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final Pronunciation p = mDataset[position];
            holder.mLanguageTextView.setText(p.langname);
            holder.mUsernameAndCountryTextView.setText(p.username + " (" + p.country + ")");
            if (p.num_votes > 0) {
                int nStars = (int)(1 + Math.ceil(4 * ((double) p.num_positive_votes / (double) p.num_votes)));
                int starRes = 0;
                switch (nStars) {
                    case 1:
                        starRes = R.drawable.star_rating_1;
                        break;
                    case 2:
                        starRes = R.drawable.star_rating_2;
                        break;
                    case 3:
                        starRes = R.drawable.star_rating_3;
                        break;
                    case 4:
                        starRes = R.drawable.star_rating_4;
                        break;
                    case 5:
                        starRes = R.drawable.star_rating_5;
                        break;
                }
                holder.mStarsImageView.setImageResource(starRes);
            } else {
                holder.mStarsImageView.setImageBitmap(null);
            }

            holder.mPlayIndicatorImageView.setAlpha(0f);

            holder.mCardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        final MediaPlayer mediaPlayer;
                        if (holder.mMediaPlayer != null) {
                            mediaPlayer = holder.mMediaPlayer;
                            mediaPlayer.start();
                            holder.mPlayIndicatorImageView.animate().alpha(1f);
                        } else {
                            mediaPlayer = new MediaPlayer();
                            holder.mMediaPlayer = mediaPlayer;
                            mediaPlayer.setDataSource(mActivity, Uri.parse(p.pathmp3));
                            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    mActivity.incrementPlaysCounter();
                                    showRateDialogIfNeeded();
                                    mediaPlayer.start();
                                    holder.mPlayIndicatorImageView.animate().alpha(1f);
                                }
                            });
                            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                                @Override
                                public void onCompletion(MediaPlayer mp) {
                                    holder.mPlayIndicatorImageView.animate().alpha(0f);
                                }
                            });
                            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                                @Override
                                public boolean onError(MediaPlayer mp, int what, int extra) {
                                    holder.mPlayIndicatorImageView.animate().alpha(0f);
                                    String errorText;
                                    switch (extra) {
                                        case MediaPlayer.MEDIA_ERROR_IO:
                                        errorText = "Network error";
                                            break;
                                        case MediaPlayer.MEDIA_ERROR_MALFORMED:
                                            errorText = "Bad audio file";
                                            break;
                                        case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                                            errorText = "Unsupported audio file";
                                            break;
                                        case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                                            errorText = "Playback timed out";
                                            break;
                                        default:
                                            errorText = "Unknown error";
                                    }
                                    Snackbar.make(mContainer, errorText, Snackbar.LENGTH_LONG).show();
                                    return true;
                                }
                            });
                            mediaPlayer.prepareAsync();
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }

        @Override
        public void onViewRecycled(ViewHolder holder) {
            if(holder.mMediaPlayer != null) {
                holder.mMediaPlayer.release();
                holder.mMediaPlayer = null;
            }
        }

        private String iterate(String s, int n) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < n; i++) {
                sb.append(s);
            }
            return sb.toString();
        }

        private void showRateDialogIfNeeded() {
            int nSearches = mActivity.getPlaysCounter();
            if (mAlreadyShownRateDialog) {
                return;
            }
            if (mActivity.isAlreadyRated()) {
                return;
            }
            if (nSearches == 0) {
                return;
            }
            if ((int) Math.pow(nSearches, 0.7) % 3 == 0 && (int) Math.pow(nSearches+1, 0.7) % 3 == 1) {
                mAlreadyShownRateDialog = true;
                mActivity.showRateDialog();
            }
        }

        // Return the size of your dataset (invoked by the layout manager)
        @Override
        public int getItemCount() {
            return mDataset.length;
        }
    }
}
