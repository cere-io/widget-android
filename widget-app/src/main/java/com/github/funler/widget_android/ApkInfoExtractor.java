package com.github.funler.widget_android;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class ApkInfoExtractor {

    private Context context;

    public ApkInfoExtractor(Context context) {
        this.context = context;
    }

    public List<AppInfo> getInstalledAppsInfo() {
        List<AppInfo> appsInfo = new ArrayList<>();

        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);

        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> resolveInfoList = packageManager.queryIntentActivities(intent, 0);

        for (ResolveInfo resolveInfo : resolveInfoList) {
            if (appsInfo.size() < 3) {
                ActivityInfo activityInfo = resolveInfo.activityInfo;
                if (!isSystemPackage(resolveInfo)) {
                    String appPackageName = activityInfo.applicationInfo.packageName;

                    AppInfo appInfo = new AppInfo(
                            appPackageName,
                            getAppName(packageManager, appPackageName),
                            getAppIconByPackageName(packageManager, appPackageName)
                    );

                    appsInfo.add(appInfo);
                }
            }
        }

        return appsInfo;
    }

    private boolean isSystemPackage(ResolveInfo resolveInfo) {
        return ((resolveInfo.activityInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0);
    }

    private String getAppIconByPackageName(PackageManager packageManager, String appPackageName) {
        Drawable drawable = null;

        try {
            drawable = packageManager.getApplicationIcon(appPackageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (drawable != null) {
            Bitmap bitmap = null;

            if (drawable instanceof BitmapDrawable) {
                bitmap = ((BitmapDrawable) drawable).getBitmap();
            } else {
                bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
            }

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] arr = stream.toByteArray();
            return "data:image/png;base64," + Base64.encodeToString(arr, Base64.URL_SAFE);
        }

        return "";
    }

    private String getAppName(PackageManager packageManager, String appPackageName) {
        String name = "";
        ApplicationInfo applicationInfo;

        try {
            applicationInfo = packageManager.getApplicationInfo(appPackageName, 0);
            if (applicationInfo != null) {
                name = (String) packageManager.getApplicationLabel(applicationInfo);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return name;
    }
}
