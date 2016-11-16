package com.app.hopity.extra;

import android.os.AsyncTask;

/**
 * Created by Mushi on 12/8/2015.
 */
public class LoadingTask extends AsyncTask<String, Integer, Integer> {

    // This is the listener that will be told when this task is finished
    private final LoadingTaskFinishedListener finishedListener;

    public LoadingTask(LoadingTaskFinishedListener finishedListener) {
        this.finishedListener = finishedListener;
    }

    @Override
    protected Integer doInBackground(String... params) {
        if (resourcesDontAlreadyExist()) {
            downloadResources();
        }
        // Perhaps you want to return something to your post execute
        return 1234;
    }

    private boolean resourcesDontAlreadyExist() {
        // Here you would query your app's internal state to see if this download had been performed before
        // Perhaps once checked save this in a shared preference for speed of access next time
        return true; // returning true so we show the splash every time
    }

    private void downloadResources() {
        // We are just imitating some process that takes a bit of time (loading of resources / downloading)
        // seconds to wait
        int count = 2;
        for (int i = 1; i <= count; i++) {
            // Do some long loading things
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {
            }
        }
    }

    @Override
    protected void onPostExecute(Integer result) {
        super.onPostExecute(result);
        finishedListener.onTaskFinished(); // Tell whoever was listening we have finished
    }

    public interface LoadingTaskFinishedListener {
        void onTaskFinished(); // If you want to pass something back to the listener add a param to this method
    }
}
