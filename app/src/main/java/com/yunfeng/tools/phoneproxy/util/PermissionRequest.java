package com.yunfeng.tools.phoneproxy.util;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Process;
import android.os.Build.VERSION;

import androidx.core.app.ActivityCompat;

public class PermissionRequest {
    private Activity activity;
    private String permission;
    private int requestCode = 0x00001123;
    private PermissionRequest.Callback callback;
    private boolean isShowAlert;

    public PermissionRequest(Activity activity) {
        this.activity = activity;
    }

    public void startRequest(String permission, PermissionRequest.Callback callback) {
        this.permission = permission;
        this.callback = callback;
        this.requesting();
    }

    private void requesting() {
        int hasPermission;
        if (VERSION.SDK_INT >= 23) {
            hasPermission = this.activity.checkSelfPermission(this.permission);
            if (hasPermission == 0) {
                this.callback.onCallback(true);
            } else {
                this.activity.requestPermissions(new String[]{this.permission}, this.requestCode);
            }
        } else {
            hasPermission = this.activity.checkPermission(this.permission, Process.myPid(), Process.myUid());
            if (hasPermission == 0) {
                this.callback.onCallback(true);
            } else {
                this.openSettings();
            }
        }

    }

    public void onResponse(int requestCode, String[] permissions, int[] grantResults) {
        if (this.requestCode == requestCode) {
            for (int i = 0; i < permissions.length; ++i) {
                if (permissions[i].equals(this.permission)) {
                    boolean isAllowed = grantResults[i] == 0;
                    if (isAllowed) {
                        this.callback.onCallback(true);
                    } else {
                        this.openSettings();
                    }
                }
            }
        }

    }

    public void onResponse(int requestCode, String[] permissions, int[] grantResults, String accountType) {
        if (this.requestCode == requestCode) {
            for (int i = 0; i < permissions.length; ++i) {
                if (permissions[i].equals(this.permission)) {
                    boolean isAllowed = grantResults[i] == 0;
                    if (isAllowed) {
                        this.callback.onCallback(true);
                    } else {
                        this.openSettings();
                    }
                }
            }
        }

        if (accountType == null || accountType.equals("")) {
            ;
        }
    }

    private void openSettings() {
        Builder builder;
        if (ActivityCompat.shouldShowRequestPermissionRationale(this.activity, this.permission)) {
            builder = new Builder(this.activity);
            if (VERSION.SDK_INT >= 21) {
                builder = new Builder(this.activity, 16974394);
            }

            builder.setCancelable(false);
            builder.setMessage("request permission");
            builder.setPositiveButton("grant", new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ActivityCompat.requestPermissions(PermissionRequest.this.activity, new String[]{PermissionRequest.this.permission}, PermissionRequest.this.requestCode);
                }
            });
            builder.show();
        } else {
            builder = new Builder(this.activity);
            if (VERSION.SDK_INT >= 21) {
                builder = new Builder(this.activity, 16974394);
            }

            builder.setCancelable(false);
            builder.setPositiveButton("grant", new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    PermissionRequest.this.callback.onCallback(false);

                    try {
                        Intent intent = new Intent();
                        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                        intent.setData(Uri.parse("package:" + PermissionRequest.this.activity.getPackageName()));
                        PermissionRequest.this.activity.startActivity(intent);
                    } catch (Exception var4) {
                        var4.printStackTrace();
                    }

                }
            });
            builder.setNegativeButton("cancel", new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    PermissionRequest.this.callback.onCallback(false);
                }
            });
            builder.setMessage("request permission");
            builder.show();
        }

    }

    private String getStringName() {
        String m = "";
        if (this.permission.equals("android.permission.WRITE_EXTERNAL_STORAGE")) {
            m = "_storage";
        } else if (this.permission.equals("android.permission.GET_ACCOUNTS")) {
            m = "_contacts";
        } else if (this.permission.equals("android.permission.READ_PHONE_STATE")) {
            m = "_phone";
        } else if (this.permission.equals("android.permission.RECORD_AUDIO")) {
            m = "_record";
        }
        return m;
    }

    public String getApplicationName() {
        try {
            PackageManager packageManager = this.activity.getApplicationContext().getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(this.activity.getPackageName(), 0);
            return (String) packageManager.getApplicationLabel(applicationInfo);
        } catch (NameNotFoundException var3) {
            return "";
        }
    }

    private void lastAlertDialog() {
        Builder builder = new Builder(this.activity);
        if (VERSION.SDK_INT >= 21) {
            builder = new Builder(this.activity, 16974394);
        }

        builder.setCancelable(false);
        builder.setPositiveButton("grant", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                PermissionRequest.this.callback.onCallback(false);
                if (PermissionRequest.this.activity != null) {
                    PermissionRequest.this.activity.finish();
                }
            }
        });
        builder.setMessage("request permission");
        builder.show();
    }

    public interface Callback {
        void onCallback(boolean var1);
    }
}
