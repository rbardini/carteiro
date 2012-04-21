package com.rbardini.carteiro.svc;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

public class DetachableResultReceiver extends ResultReceiver {
    private static final String TAG = "DetachableResultReceiver";
    private Receiver receiver;

    public DetachableResultReceiver(Handler handler) {
        super(handler);
        receiver = null;
    }

    public void clearReceiver() {
        receiver = null;
    }

    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    public interface Receiver {
        public void onReceiveResult(int resultCode, Bundle resultData);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        if (receiver != null) {
            receiver.onReceiveResult(resultCode, resultData);
        } else {
            Log.w(TAG, "Dropping result on floor for code "+ resultCode +": "+ resultData.toString());
        }
    }
}
