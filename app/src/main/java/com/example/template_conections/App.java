package com.example.template_conections;

import android.app.Application;

import com.example.template_conections.utils.Connection;
import com.example.template_conections.utils.DataHandler;

public class App extends Application {

    private Connection mConnection;
    private DataHandler mDataHandler;
    private boolean isForeground;
    private boolean isSynchronized;

    @Override
    public void onCreate() {
        super.onCreate();
        if (mConnection == null) {
            mConnection = new Connection(getApplicationContext());
        }
        if (mDataHandler == null) {
            mDataHandler = new DataHandler(getApplicationContext(), mConnection);
        }

        isForeground = false;
        isSynchronized = false;
    }

    public Connection getConnection() {
        return mConnection;
    }

    public DataHandler getDataHandler() {
        return mDataHandler;
    }

    /*public AppLogger getLogger() {
        return mLogger;
    }*/

    public boolean isForeground() {
        return isForeground;
    }

    public void setForeground(boolean isForeground) {
        this.isForeground = isForeground;
    }

    public boolean isSynchronized()
    {
        return isSynchronized;
    }

    public void setSynchronized(boolean isSynchronized)
    {
        this.isSynchronized = isSynchronized;
    }

}

