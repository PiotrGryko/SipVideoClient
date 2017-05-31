package sip.piotr.com.sipclient;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.webrtc.videoengine.ViERenderer;

import sip.piotr.com.sipclient.api.SipCallSession;
import sip.piotr.com.sipclient.api.SipManager;
import sip.piotr.com.sipclient.pjsip.service.SipService;

/**
 * Created by piotr on 17/01/17.
 */
public class FragmentSip extends Fragment {
    private String TAG = FragmentSip.class.getName();
    private SurfaceView mSurfaceView;
    private RelativeLayout container;
    private LinearLayout layoutInfo;
    private TextView tvLabel;
    private ProgressBar progressBar;
    private EditText etIp;
    private int currentState = 0;
    private RelativeLayout layoutBase;
    private boolean scheduledToRestart = false;
    private int callID;

    class SipCallReceiver extends BroadcastReceiver {

        private boolean isRegistered = false;

        public void register() {
            if (!isRegistered) {
                getActivity().registerReceiver(this, new IntentFilter(SipManager.ACTION_SIP_CALL_CHANGED));
                isRegistered = true;

            }
        }

        public void unregister() {
            if (isRegistered) {
                getActivity().unregisterReceiver(this);
                isRegistered = false;
            }
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(SipManager.ACTION_SIP_CALL_CHANGED)) {
                SipCallSession callSession = intent.getParcelableExtra(SipManager.EXTRA_CALL_INFO);
                Log.d(TAG, "on call state changed " + callSession.getCallState());
                currentState = callSession.getCallState();
                switch (callSession.getCallState()) {

                    case 5:
                        setVideoView(callSession.getCallId());
                        break;
                }
                refreshControls();


            }
        }


    }

    private SipCallReceiver callStateReceiver = new SipCallReceiver();

    private void refreshControls() {
        switch (currentState) {
            case 1:
            case 4:
                layoutInfo.setVisibility(View.VISIBLE);
                tvLabel.setText(getString(R.string.camera_connecting));
                progressBar.setVisibility(View.VISIBLE);
                break;
            case 5:
                layoutInfo.setVisibility(View.GONE);
                break;

            case 6:
            case 0:
            default:
                if (scheduledToRestart) {
                    scheduledToRestart = false;
                    loadCamera();
                } else {
                    layoutInfo.setVisibility(View.VISIBLE);
                    tvLabel.setText("Camera Not Connected\n Touch Screen to Connect");
                    progressBar.setVisibility(View.GONE);
                }


                break;


        }
    }

    public void loadUrl(String url) {

        ((MainActivity) getActivity()).makeCall(url);
    }

    private void loadCamera() {
        String ip = etIp.getText().toString().trim();
        if(!ip.equals(""))
        loadUrl(ip);

    }

    public void setVideoView(int callId) {
        Log.d(TAG, "set video view");
        this.callID = callId;
        SipService.setVideoWindow(callId, mSurfaceView, false);

    }

    public void onDestroyView() {
        super.onDestroyView();
        callStateReceiver.unregister();
    }

    public void setFullScreen(boolean fullscreen) {
        if (fullscreen) {
            layoutBase.setPadding(0, 0, 0, 0);
            layoutBase.setBackgroundColor(Color.BLACK);
        } else {
            int padding = (int) getResources().getDimension(R.dimen.video_padding);
            layoutBase.setPadding(padding, padding, padding, padding);
            layoutBase.setBackgroundColor(Color.WHITE);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callStateReceiver.register();
        View v = inflater.inflate(R.layout.fragment_sip, parent, false);

        layoutInfo = (LinearLayout) v.findViewById(R.id.layout_info);
        progressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        tvLabel = (TextView) v.findViewById(R.id.tv_label);
        layoutBase = (RelativeLayout) v.findViewById(R.id.layout_base);
        etIp = (EditText)v.findViewById(R.id.et_ip);

        container = (RelativeLayout) v.findViewById(R.id.container);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.ALIGN_LEFT, RelativeLayout.TRUE);
        lp.addRule(RelativeLayout.ALIGN_RIGHT, RelativeLayout.TRUE);
        lp.addRule(RelativeLayout.ALIGN_TOP, RelativeLayout.TRUE);
        mSurfaceView = ViERenderer.CreateRenderer(getActivity(), true);
        mSurfaceView.setLayoutParams(lp);
       // container.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT));
        container.addView(mSurfaceView, 0);

        mSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentState == 0 || currentState == 6) {
                    loadCamera();
                } else {
                    scheduledToRestart = true;
                    ((MainActivity) getActivity()).hangUpCall(callID);
                }

            }
        });
        refreshControls();
        return v;
    }
}