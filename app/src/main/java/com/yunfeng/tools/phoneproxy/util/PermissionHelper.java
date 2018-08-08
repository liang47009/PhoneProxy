/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yunfeng.tools.phoneproxy.util;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.yunfeng.tools.phoneproxy.MainActivity;

/**
 * Helper to ask  permission.
 */
public class PermissionHelper {
    private static final String[] PERMISSIONS = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.INTERNET};
    private static final int REQ_PERMISSION_CODE = 0x1001;

    /**
     * Check to see we have the necessary permissions for this app.
     */
    private static boolean hasPermission(Activity activity) {
        boolean granted = true;
        for (String permission : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_DENIED) {
                granted = false;
                break;
            }
        }
        return granted;
    }

    /**
     * Check to see we have the necessary permissions for this app, and ask for them if we don't.
     */
    private static void requestPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, PERMISSIONS, REQ_PERMISSION_CODE);
    }

    /**
     * Check to see if we need to show the rationale for this permission.
     */
    private static boolean shouldShowRequestPermissionRationale(Activity activity) {
        boolean should = false;
        for (String permission : PERMISSIONS) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                should = true;
                break;
            }
        }
        return should;
    }

    /**
     * Launch Application Setting to grant permission.
     */
    private static void launchPermissionSettings(Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        activity.startActivity(intent);
    }

    private static void response(final Activity activity) {
        if (!hasPermission(activity)) {
            AlertDialog.Builder builder = null;
            if (shouldShowRequestPermissionRationale(activity)) {
                if (Build.VERSION.SDK_INT >= 21) {
                    builder = new AlertDialog.Builder(activity, 16974394);
                } else {
                    builder = new AlertDialog.Builder(activity);
                }
                builder.setCancelable(false);
                builder.setMessage("Click OK and allow access to use.");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermission(activity);
//                        dialog.dismiss();
                    }
                });
                builder.show();
            } else {
                if (Build.VERSION.SDK_INT >= 21) {
                    builder = new AlertDialog.Builder(activity, 16974394);
                } else {
                    builder = new AlertDialog.Builder(activity);
                }

                builder.setCancelable(false);
                String message = "This app requires permission to access . If permission not granted, will be unavailable.";
                builder.setPositiveButton("Setting", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        launchPermissionSettings(activity);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        activity.finish();
                    }
                });
                builder.setMessage(message);
                builder.show();
            }
        }
    }

    public static void onRequestPermissionsResult(Activity activity, int requestCode, String[] permissions, int[] grantResults) {
        response(activity);
    }

    public static void request(Activity activity) {
        if (!hasPermission(activity)) {
            requestPermission(activity);
        }
    }
}
