package sip.piotr.com.sipclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import sip.piotr.com.sipclient.api.SipCallSession;
import sip.piotr.com.sipclient.api.SipManager;
import sip.piotr.com.sipclient.pjsip.service.SipService;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getName();
    private SipService.MyBinder sipService;
    private boolean mBound;
    private ServiceConnection mSipConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            sipService = (SipService.MyBinder) service;
            try {
                sipService.addAllAccounts();

            } catch (RemoteException e) {
                e.printStackTrace();
            }
            Log.d(TAG,"Service connected");

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
            sipService = null;
            Log.d(TAG, "service unbound");
        }
    };

    public void makeCall(String number) {
        if (sipService != null) {

            Bundle b = new Bundle();
            b.putBoolean(SipCallSession.OPT_CALL_VIDEO, true);
            try {
                sipService.makeCallWithOptions(number, 0, b);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void hangUpCall(int callId) {
        if (sipService != null) {
            try {
                sipService.hangup(callId, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void requestPermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.USE_SIP)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.USE_SIP)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.USE_SIP},
                        222);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }

    // Service monitoring stuff
    private void startSipService() {
        Thread t = new Thread("StartSip") {
            public void run() {
                Intent serviceIntent = new Intent(MainActivity.this, SipService.class);
                // Optional, but here we bundle so just ensure we are using csipsimple package
                serviceIntent.setPackage(MainActivity.this.getPackageName());
                serviceIntent.putExtra(SipManager.EXTRA_OUTGOING_ACTIVITY, new ComponentName(MainActivity.this, MainActivity.class));
                bindService(serviceIntent, mSipConnection, Context.BIND_AUTO_CREATE);

                startService(serviceIntent);
                //postStartSipService();
            }

            ;
        };
        t.start();

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions();
        startSipService();

    }

}
