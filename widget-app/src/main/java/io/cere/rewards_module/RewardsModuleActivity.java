package io.cere.rewards_module;

import android.animation.LayoutTransition;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.github.funler.jsbridge.BridgeWebView;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static io.cere.rewards_module.RewardsModuleActivity.ActivityEvents.close_widget_view;
import static io.cere.rewards_module.RewardsModuleActivity.ActivityEvents.initialized_widget_view;
import static io.cere.rewards_module.RewardsModuleActivity.ActivityEvents.input_blurred;
import static io.cere.rewards_module.RewardsModuleActivity.ActivityEvents.input_focused;
import static io.cere.rewards_module.RewardsModuleActivity.ActivityEvents.maximize_widget_view;
import static io.cere.rewards_module.RewardsModuleActivity.ActivityEvents.restore_widget_view;

public class RewardsModuleActivity extends AppCompatActivity {

    private BridgeWebView bridgeWebView;
    private RelativeLayout root;
    private RewardsModule rewardsModule = RewardsModule.getInstance();
    private final int LEFT_RIGHT_MARGIN = 5;
    private final int TOP_BOTTOM_MARGIN = 5;

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
            rewardsModule.setMaximized(true);
            maximize();
        }
    };

    private final BroadcastReceiver restoreReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            rewardsModule.setMaximized(false);
            minimize();
        }
    };

    private final BroadcastReceiver focusReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            double windowHeight = Util.dpFromPx(getBaseContext(), getWindow().getDecorView().getHeight());
            double webViewHeight = Util.dpFromPx(getBaseContext(), bridgeWebView.getHeight());
            double margin = (windowHeight - webViewHeight);

            double y = intent.getExtras().getFloat("y") + margin;

            double visibleHeight = windowHeight / 2;

            if (y > visibleHeight) {
                double newY = visibleHeight - y - (margin / 2);
                bridgeWebView.animate().translationY((int) Util.pxFromDp(getBaseContext(), newY)).start();
            }
        }
    };

    private final BroadcastReceiver blurReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            bridgeWebView.animate().translationY(0).start();
        }
    };

    private final BroadcastReceiver initReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            runOnUiThread(() -> {
                RelativeLayout cereLogoLayout = findViewById(R.id.cere_logo_layout);
                root.removeView(cereLogoLayout);

                if (bridgeWebView.getParent() == null) {
                    attachBridgetView();
                }
            });
        }
    };

    @Override
    public void onCreate(Bundle savedStateInstance) {
        super.onCreate(savedStateInstance);
        overridePendingTransition(R.anim.scale_up, R.anim.scale_down);
        setContentView(R.layout.activity_widget_view);

        root = findViewById(R.id.root);
        root.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        bridgeWebView = rewardsModule.getBridgeWebView();

        if (rewardsModule.isInitialized()) {
            attachBridgetView();
        } else {
            RelativeLayout cereLogoLayout = findViewById(R.id.cere_logo_layout);
            cereLogoLayout.setVisibility(View.VISIBLE);

            ImageView cereLogoBg1 = findViewById(R.id.cere_logo_bg);
            ImageView cereLogoBg2 = findViewById(R.id.cere_logo_bg_2);

            cereLogoBg1.animate().translationY(50).setDuration(7500).start();
            cereLogoBg2.animate().translationY(-50).setDuration(7500).start();
        }

        makeFullScreenWithoutSystemUI();
        configureInitialSize();

        registerReceivers();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        detachBridgeView();
        unregisterReceivers();
    }

    @Override
    public void onBackPressed() {
        rewardsModule.inputBlurred();
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
        registerReceiver(closeReceiver, new IntentFilter(close_widget_view.name()));
        registerReceiver(maximizeReceiver, new IntentFilter(maximize_widget_view.name()));
        registerReceiver(restoreReceiver, new IntentFilter(restore_widget_view.name()));
        registerReceiver(focusReceiver, new IntentFilter(input_focused.name()));
        registerReceiver(blurReceiver, new IntentFilter(input_blurred.name()));
        registerReceiver(initReceiver, new IntentFilter(initialized_widget_view.name()));
    }

    private void unregisterReceivers() {
        unregisterReceiver(closeReceiver);
        unregisterReceiver(maximizeReceiver);
        unregisterReceiver(restoreReceiver);
        unregisterReceiver(focusReceiver);
        unregisterReceiver(blurReceiver);
        unregisterReceiver(initReceiver);
    }

    private void configureInitialSize() {
        DisplayMetrics metrics = Util.getMetrics(getBaseContext());

        if (rewardsModule.getWidthPx() == 0) {
            rewardsModule.setWidthPx(metrics.widthPixels - (int) Math.round(Util.pxWidthFromPercents(getBaseContext(), LEFT_RIGHT_MARGIN)));
            rewardsModule.setHeightPx(metrics.heightPixels - (int) Math.round(Util.pxHeightFromPercents(getBaseContext(), TOP_BOTTOM_MARGIN)));
        }

        if (rewardsModule.isMaximized()) {
            maximize();
        } else {
            minimize();
        }
    }

    private void updateLayoutParams(int width, int height, int top, int left) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);

        if (top == -1 && left == -1) {
            params.gravity = Gravity.CENTER;
        } else {
            params.topMargin = top > 0 ? top : 0;
            params.leftMargin = left > 0 ? left : 0;
        }

        root.setLayoutParams(params);
    }

    private void attachBridgetView() {
        if (bridgeWebView.getParent() == null) {
            root.addView(bridgeWebView);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) bridgeWebView.getLayoutParams();
            params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            params.width = MATCH_PARENT;
            params.height = MATCH_PARENT;
            bridgeWebView.setLayoutParams(params);
        }
    }

    private void detachBridgeView() {
        if (bridgeWebView != null && bridgeWebView.getParent() != null) {
            ((ViewGroup) bridgeWebView.getParent()).removeAllViews();
        }
    }

    private void maximize() {
        updateLayoutParams(MATCH_PARENT, MATCH_PARENT, -1, -1);
    }

    private void minimize() {
        updateLayoutParams(
                rewardsModule.getWidthPx(),
                rewardsModule.getHeightPx(),
                rewardsModule.getTopPx(),
                rewardsModule.getLeftPx()
        );
    }

    enum ActivityEvents {
        close_widget_view,
        maximize_widget_view,
        restore_widget_view,
        input_focused,
        input_blurred,
        initialized_widget_view
    }
}
