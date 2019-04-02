package com.github.funler.widget_android;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

class WidgetUtil {
    static double dpFromPx(Context context, double px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    static double pxFromDp(Context context, double dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    static double pxWidthFromPercents(Context context, double percents) {
        return getMetrics(context).widthPixels * percents / 100;
    }

    static double pxHeightFromPercents(Context context, double percents) {
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
