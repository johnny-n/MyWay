package com.projects.johnny.myway;

import android.app.Application;

public class App extends Application {

    private String UID;
    private static App instance;

    public App() { }

    public static App getInstance() {
        if (instance == null) {
            instance = new App();
        }
        return instance;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }
}
