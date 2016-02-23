package com.projects.johnny.myway;

import android.app.Application;

/**
 * Created by Johnny on 2/23/16.
 */
public class App extends Application {
    String UID = null;

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }
}
