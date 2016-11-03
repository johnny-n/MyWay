package com.projects.johnny.myway;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.LatLng;
import com.google.maps.model.TravelMode;

import java.util.concurrent.ConcurrentHashMap;

public class EtaHandlerThread<T> extends HandlerThread {
    private static final String TAG = "EtaHandlerThread";
    private static final int ETA_TIME = 0;

    private GeoApiContext ctx;
    private double lat;
    private double lng;
    private Handler mRequestHandler;
    private Handler mResponseHandler;
    private ConcurrentHashMap<T, MyLocation> mRequestMap;
    private EtaInterface mEtaInterface;

    public interface EtaInterface {
        void setInformation(DirectionsFragment.DirectionItemViewHolder viewHolder, String travelTime);
    }

    public EtaHandlerThread(Handler responseHandler, GeoApiContext context, Double latitude, Double longitude) {
        super(TAG);
        mResponseHandler = responseHandler;
        ctx = context;
        lat = latitude;
        lng = longitude;
    }

    public void setEtaInterface(EtaInterface etaInterface) {
        mEtaInterface = etaInterface;
    }

    @Override
    protected void onLooperPrepared() {
        mRequestMap = new ConcurrentHashMap<>();
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                T target = (T) msg.obj;
                handleEtaRequest(target);
            }
        };
    }

    private void handleEtaRequest(T target) {
        final MyLocation location = mRequestMap.get(target);
        final String locationAddress = location.getAddress();

        // Create request for GoogleDirectionsApi
        // Last known location is set here
        DirectionsApiRequest directionsApiRequest = DirectionsApi.newRequest(ctx)
                .origin(new LatLng(lat, lng))
                .mode(TravelMode.DRIVING);

        directionsApiRequest.destination(locationAddress);
        final DirectionsResult result;
        try {
            // Obtain result from api request
            result = directionsApiRequest.await();
            // Obtain travel time deep within result
            final String travelTime = result.routes[0].legs[0].duration.humanReadable;
            Log.d(TAG, "Travel time: " + travelTime);

            final DirectionsFragment.DirectionItemViewHolder viewHolder = (DirectionsFragment.DirectionItemViewHolder) target;
            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    mEtaInterface.setInformation(viewHolder, travelTime);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void requestEtaTime(T target, MyLocation location) {
        if (location == null) {
            mRequestMap.remove(target);
        } else {
            mRequestMap.put(target, location);
            mRequestHandler.obtainMessage(ETA_TIME, target).sendToTarget();
        }
    }

    public void clearQueue() {
        mRequestHandler.removeMessages(ETA_TIME);
    }
}
