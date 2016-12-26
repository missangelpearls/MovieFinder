package com.example.priyavjti.moviefinder;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import com.example.priyavjti.moviefinder.RetrofitAPICall.RetrofitCall;
import com.example.priyavjti.moviefinder.RetrofitAPICall.ServiceSettings;

import java.util.List;
import com.example.priyavjti.moviefinder.RetrofitAPICall.*;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ServiceSettings.ResultWithDetail> {

    private Button mSearchButton;
    private EditText mSearch;
    private RecyclerView mMovieListRecyclerView;
    private MovieRecyclerViewAdapter mMovieAdapter;
    private String mMovieTitle;
    private ProgressBar mProgressBar;

    private static final int LOADER_ID = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSearch = (EditText) findViewById(R.id.search_edittext);

        mSearch.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))) {
                    startSearch();
                    handled = true;
                }
                return handled;
            }
        });
        mSearchButton = (Button) findViewById(R.id.search_button);
        mMovieListRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSearch();
            }
        });
        mMovieAdapter = new MovieRecyclerViewAdapter(null);
        mMovieListRecyclerView.setAdapter(mMovieAdapter);

        StaggeredGridLayoutManager gridLayoutManager =
                new StaggeredGridLayoutManager(getResources().getInteger(R.integer.grid_column_count), StaggeredGridLayoutManager.VERTICAL);
        mMovieListRecyclerView.setItemAnimator(null);

        mMovieListRecyclerView.setLayoutManager(gridLayoutManager);
        getSupportLoaderManager().enableDebugLogging(true);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_spinner);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("mMovieTitle", mMovieTitle);
        outState.putInt("progress_visibility",mProgressBar.getVisibility());
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);
        int progress_visibility= savedInstanceState.getInt("progress_visibility");

        if(progress_visibility == View.VISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
        }

        mMovieTitle = savedInstanceState.getString("mMovieTitle");
        if (mMovieTitle != null) {
            Bundle args = new Bundle();
            args.putString("movieTitle", mMovieTitle);
            getSupportLoaderManager().initLoader(LOADER_ID, args, this);
        }
    }

    @Override
    public Loader<ServiceSettings.ResultWithDetail> onCreateLoader(int id, Bundle args) {
        return new RetrofitCall(MainActivity.this, args.getString("movieTitle"));
    }

    @Override
    public void onLoadFinished(Loader<ServiceSettings.ResultWithDetail> loader, ServiceSettings.ResultWithDetail resultWithDetail) {
        mProgressBar.setVisibility(View.GONE);
        mMovieListRecyclerView.setVisibility(View.VISIBLE);
        if(resultWithDetail.getResponse().equals("True")) {
            mMovieAdapter.swapData(resultWithDetail.getMovieDetailList());
        } else {
            Snackbar.make(mMovieListRecyclerView,
                    getResources().getString(R.string.snackbar_title_not_found), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLoaderReset(Loader<ServiceSettings.ResultWithDetail> loader) {
        mMovieAdapter.swapData(null);
    }

    public class MovieRecyclerViewAdapter
            extends RecyclerView.Adapter<MovieRecyclerViewAdapter.ViewHolder> {

        private List<ServiceSettings.Detail> mValues;

        public MovieRecyclerViewAdapter(List<ServiceSettings.Detail> items) {
            mValues = items;
        }


        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_item_movie, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            final ServiceSettings.Detail detail = mValues.get(position);
            final String title = detail.Title;
            final String imdbId = detail.imdbID;
            final String director = detail.Director;
            final String year = detail.Year;
            holder.mDirectorView.setText(director);
            holder.mTitleView.setText(title);
            holder.mYearView.setText(year);

            final String imageUrl;
            if (! detail.Poster.equals("N/A")) {
                imageUrl = detail.Poster;
            } else {

                imageUrl = getResources().getString(R.string.default_poster);
            }
            holder.mThumbImageView.layout(0, 0, 0, 0);
            Glide.with(MainActivity.this).load(imageUrl).into(holder.mThumbImageView);

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, DetailActivity.class);

                    intent.putExtra(DetailActivity.MOVIE_DETAIL, detail);
                    intent.putExtra(DetailActivity.IMAGE_URL, imageUrl);

                    ActivityOptionsCompat options = ActivityOptionsCompat.
                            makeSceneTransitionAnimation(MainActivity.this,
                                    holder.mThumbImageView, "poster");
                    startActivity(intent, options.toBundle());
                }
            });
        }

        @Override
        public int getItemCount() {
            if(mValues == null) {
                return 0;
            }
            return mValues.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mTitleView;
            public final TextView mYearView;
            public final TextView mDirectorView;
            public final ImageView mThumbImageView;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mTitleView = (TextView) view.findViewById(R.id.movie_title);
                mYearView = (TextView) view.findViewById(R.id.movie_year);
                mThumbImageView = (ImageView) view.findViewById(R.id.thumbnail);
                mDirectorView = (TextView) view.findViewById(R.id.movie_director);
            }

        }

        @Override
        public void onViewRecycled(ViewHolder holder) {
            super.onViewRecycled(holder);
            Glide.clear(holder.mThumbImageView);
        }

        public void swapData(List<ServiceSettings.Detail> items) {
            if(items != null) {
                mValues = items;
                notifyDataSetChanged();

            } else {
                mValues = null;
            }
        }
    }

    private void startSearch() {
        if(Utils.isNetworkAvailable(getApplicationContext())) {
            Utils.hideSoftKeyboard(MainActivity.this);
            String movieTitle = mSearch.getText().toString().trim();
            if (!movieTitle.isEmpty()) {
                Bundle args = new Bundle();
                args.putString("movieTitle", movieTitle);
                getSupportLoaderManager().restartLoader(LOADER_ID, args, this);
                mMovieTitle = movieTitle;
                mProgressBar.setVisibility(View.VISIBLE);
                mMovieListRecyclerView.setVisibility(View.GONE);
            }
            else
                Snackbar.make(mMovieListRecyclerView,
                        getResources().getString(R.string.snackbar_title_empty),
                        Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(mMovieListRecyclerView,
                    getResources().getString(R.string.network_not_available),
                    Snackbar.LENGTH_LONG).show();
        }
    }


    @Override
    public void onBackPressed() {
        finish();
    }
}
