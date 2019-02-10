package com.github.funler.widget_android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.github.funler.jsbridge.BridgeWebView;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class WidgetViewActivity extends AppCompatActivity {

    private BridgeWebView bridgeWebView;

    private int restoreWidth;
    private int restoreHeight;

    private final BroadcastReceiver closeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
            overridePendingTransition(R.anim.scale_up, R.anim.scale_down);
        }
    };

    private final BroadcastReceiver maximizeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ViewGroup.LayoutParams params = bridgeWebView.getLayoutParams();
            params.height = MATCH_PARENT;
            params.width = MATCH_PARENT;
            updateLayoutParams(params);
        }
    };

    private final BroadcastReceiver restoreReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ViewGroup.LayoutParams params = bridgeWebView.getLayoutParams();
            params.height = restoreHeight;
            params.width = restoreWidth;
            updateLayoutParams(params);
        }
    };

    @Override
    public void onCreate(Bundle savedStateInstance) {
        super.onCreate(savedStateInstance);
        overridePendingTransition(R.anim.scale_up, R.anim.scale_down);

        RelativeLayout root = new RelativeLayout(getBaseContext());
        root.setLayoutParams(new RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

        bridgeWebView = WidgetView.getInstance().getBridgeWebView();
        root.addView(bridgeWebView);

        setContentView(root);

        makeFullScreenWithoutSystemUI();
        configureInitialSize();

        registerReceivers();
    }

    @Override
    public void onBackPressed() {
        // do nothing
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bridgeWebView != null && bridgeWebView.getParent() != null) {
            ((ViewGroup) bridgeWebView.getParent()).removeView(bridgeWebView);
        }
        unregisterReceivers();
    }

    private void makeFullScreenWithoutSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.INVISIBLE);
        }
    }

    private void registerReceivers() {
        registerReceiver(closeReceiver, new IntentFilter("close_widget_view"));
        registerReceiver(maximizeReceiver, new IntentFilter("maximize_widget_view"));
        registerReceiver(restoreReceiver, new IntentFilter("restore_widget_view"));
    }

    private void unregisterReceivers() {
        unregisterReceiver(closeReceiver);
        unregisterReceiver(maximizeReceiver);
        unregisterReceiver(restoreReceiver);
    }

    private void configureInitialSize() {
        WindowManager wm = (WindowManager) WidgetView.getInstance().getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) bridgeWebView.getLayoutParams();
        restoreWidth = metrics.widthPixels - 60;
        restoreHeight = metrics.heightPixels - 100;
        params.height = restoreHeight;
        params.width = restoreWidth;
        params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);

        updateLayoutParams(params);
    }

    private void updateLayoutParams(ViewGroup.LayoutParams params) {
        bridgeWebView.setLayoutParams(params);
    }
}
