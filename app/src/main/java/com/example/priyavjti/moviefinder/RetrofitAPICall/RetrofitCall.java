package com.example.priyavjti.moviefinder.RetrofitAPICall;

import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.util.Log;

import java.io.IOException;

/**
 * Created by priyavjti on 26-Dec-16.
 */
public class RetrofitCall extends AsyncTaskLoader<ServiceSettings.ResultWithDetail> {
    private static final String LOG_TAG = "RetrofitCall";

    private final String mTitle;

    private ServiceSettings.ResultWithDetail mData;

    public RetrofitCall(Context context, String title) {
        super(context);
        mTitle = title;
    }

    @Override
    public ServiceSettings.ResultWithDetail loadInBackground() {

        try {
            ServiceSettings.Result result =  ServiceSettings.performSearch(mTitle);
            ServiceSettings.ResultWithDetail resultWithDetail = new ServiceSettings.ResultWithDetail(result);
            if(result.Search != null) {
                for(ServiceSettings.Movie movie: result.Search) {
                    resultWithDetail.addToList(ServiceSettings.getDetail(movie.imdbID));
                }
            }
            return  resultWithDetail;
        } catch(final IOException e) {
            Log.e(LOG_TAG, "Error from api access", e);
        }
        return null;
    }

    @Override
    protected void onStartLoading() {
        if (mData != null) {

            deliverResult(mData);
        } else {
            forceLoad();
        }
    }


    @Override
    protected void onReset() {
        Log.d(LOG_TAG, "onReset");
        super.onReset();
        mData = null;
    }

    @Override
    public void deliverResult(ServiceSettings.ResultWithDetail data) {
        if (isReset()) {

            return;
        }

        ServiceSettings.ResultWithDetail oldData = mData;
        mData = data;

        if (isStarted()) {

            super.deliverResult(data);
        }

    }

}
