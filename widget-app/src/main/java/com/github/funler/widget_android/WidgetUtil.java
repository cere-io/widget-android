package com.github.funler.widget_android;

import android.content.Context;

class WidgetUtil {
    static float dpFromPx(Context context, float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    static float pxFromDp(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }
}
