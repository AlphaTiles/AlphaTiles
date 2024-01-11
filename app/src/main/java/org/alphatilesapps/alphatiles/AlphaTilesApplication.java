package org.alphatilesapps.alphatiles;

import android.app.Application;
import android.content.Context;

import com.segment.analytics.Analytics;
//import com.segment.analytics.android.integrations.firebase.FirebaseIntegration;

public class AlphaTilesApplication extends Application {

    protected static Context applicationContext;
    protected static String writeKey = "zUhUqolnBCrAAdkU7Fh1MyOrbrmAzlly";

    @Override public void onCreate(){
        super.onCreate();
        applicationContext = getApplicationContext();

        Analytics analytics = new Analytics.Builder(applicationContext, writeKey).build();
        Analytics.setSingletonInstance(analytics);

    }
}
