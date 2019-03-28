package com.github.funler.widget_android;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

class WidgetUtil {
    static float dpFromPx(Context context, float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    static float pxFromDp(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    static float pxWidthFromPercents(Context context, float percents) {
        return getMetrics(context).widthPixels * percents / 100;
    }

    static float pxHeightFromPercents(Context context, float percents) {
        return getMetrics(context).heightPixels * percents / 100;
    }

    static DisplayMetrics getMetrics(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        return metrics;
    }
}
