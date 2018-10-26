package com.yunfeng.tools.phoneproxy.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;

import com.yunfeng.tools.phoneproxy.MainActivity;
import com.yunfeng.tools.phoneproxy.R;

public class NotificationUtils {
    // The id of the channel.
    private static final String channel_id = "yunfeng_channel_showNotifyWithRing";

    /**
     * 最普通的通知效果
     */
    public static void showNotifyOnlyText(Context context, int smallIcon, int largeIcon) {
        NotificationManager mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (null != mManager) {
            Bitmap mLargeIcon = BitmapFactory.decodeResource(context.getResources(), largeIcon);
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channel_id)
                    .setSmallIcon(smallIcon)
                    .setLargeIcon(mLargeIcon)
                    .setContentIntent(pendingIntent)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText("Proxy is running");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = mManager.getNotificationChannel(channel_id);
                if (null == channel) {
                    // The user-visible nameof the channel.
                    CharSequence name = context.getString(R.string.channel_name);
                    // The user-visibledescription of the channel.
                    String description = context.getString(R.string.channel_description);
                    int importance = NotificationManager.IMPORTANCE_DEFAULT;
                    NotificationChannel mChannel = new NotificationChannel(channel_id, name, importance);
                    // Configure thenotification channel.
                    mChannel.setDescription(description);
                    mChannel.enableLights(true);
                    // Sets the notificationlight color for notifications posted to this
                    // channel, if the devicesupports this feature.
                    mChannel.setLightColor(Color.RED);
                    mChannel.enableVibration(true);
                    mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                    mManager.createNotificationChannel(mChannel);
                }
            }

            mManager.notify(1, builder.build());
        }
    }

    /**
     * 展示有自定义铃声效果的通知
     * 补充:使用系统自带的铃声效果:Uri.withAppendedPath(Audio.Media.INTERNAL_CONTENT_URI, "6");
     */
    public static void showNotifyWithRing(Context context, int smallIcon) {
        NotificationManager mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (null != mManager) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // The user-visible nameof the channel.
                CharSequence name = context.getString(R.string.channel_name);
                // The user-visibledescription of the channel.
                String description = context.getString(R.string.channel_description);
                int importance = NotificationManager.IMPORTANCE_NONE;
                NotificationChannel mChannel = new NotificationChannel(channel_id, name, importance);
                // Configure thenotification channel.
                mChannel.setDescription(description);
                mChannel.enableLights(true);
                // Sets the notificationlight color for notifications posted to this
                // channel, if the devicesupports this feature.
                mChannel.setLightColor(Color.RED);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                mManager.createNotificationChannel(mChannel);
            }

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setSmallIcon(smallIcon)
                    .setContentTitle("我是伴有铃声效果的通知")
                    .setContentText("美妙么?安静听~")
                    .setChannelId(channel_id)
                    //调用系统默认响铃,设置此属性后setSound()会无效
                    //.setDefaults(Notification.DEFAULT_SOUND)
                    //调用系统多媒体裤内的铃声
                    //.setSound(Uri.withAppendedPath(MediaStore.Audio.Media.INTERNAL_CONTENT_URI,"2"));
                    //调用自己提供的铃声，位于 /res/values/raw 目录下
                    .setSound(Uri.parse("android.resource://com.yunfeng.tools.phoneproxy/" + R.raw.sound));
            //另一种设置铃声的方法
            //Notification notify = builder.build();
            //调用系统默认铃声
            //notify.defaults = Notification.DEFAULT_SOUND;
            //调用自己提供的铃声
            //notify.sound = Uri.parse("android.resource://com.littlejie.notification/"+R.raw.sound);
            //调用系统自带的铃声
            //notify.sound = Uri.withAppendedPath(MediaStore.Audio.Media.INTERNAL_CONTENT_URI,"2");
            //mManager.notify(2,notify);
            mManager.notify(2, builder.build());
        }
    }

    /**
     * 展示有震动效果的通知,需要在AndroidManifest.xml中申请震动权限
     * <uses-permission android:name="android.permission.VIBRATE" />
     * 补充:测试震动的时候,手机的模式一定要调成铃声+震动模式,否则你是感受不到震动的
     */
    public static void showNotifyWithVibrate(Context context) {
        NotificationManager mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (null != mManager) {
            //震动也有两种设置方法,与设置铃声一样,在此不再赘述
            long[] vibrate = new long[]{0, 500, 1000, 1500};
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("我是伴有震动效果的通知")
                    .setContentText("颤抖吧,凡人~")
                    //使用系统默认的震动参数,会与自定义的冲突
                    //.setDefaults(Notification.DEFAULT_VIBRATE)
                    //自定义震动效果
                    .setVibrate(vibrate);
            //另一种设置震动的方法
            //Notification notify = builder.build();
            //调用系统默认震动
            //notify.defaults = Notification.DEFAULT_VIBRATE;
            //调用自己设置的震动
            //notify.vibrate = vibrate;
            //mManager.notify(3,notify);
            mManager.notify(3, builder.build());
        }
    }

    /**
     * 显示带有呼吸灯效果的通知,但是不知道为什么,自己这里测试没成功
     */
    public static void showNotifyWithLights(Context context) {
        final NotificationManager mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (null != mManager) {
            final NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("我是带有呼吸灯效果的通知")
                    .setContentText("一闪一闪亮晶晶~")
                    //ledARGB 表示灯光颜色、 ledOnMS 亮持续时间、ledOffMS 暗的时间
                    .setLights(0xFF0000, 3000, 3000);
            Notification notify = builder.build();
            //只有在设置了标志符Flags为Notification.FLAG_SHOW_LIGHTS的时候，才支持呼吸灯提醒。
            notify.flags = Notification.FLAG_SHOW_LIGHTS;
            //设置lights参数的另一种方式
            //notify.ledARGB = 0xFF0000;
            //notify.ledOnMS = 500;
            //notify.ledOffMS = 5000;
            //使用handler延迟发送通知,因为连接usb时,呼吸灯一直会亮着
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mManager.notify(4, builder.build());
                }
            }, 10000);
        }
    }

    /**
     * 显示带有默认铃声、震动、呼吸灯效果的通知
     * 如需实现自定义效果,请参考前面三个例子
     */
    public static void showNotifyWithMixed(Context context) {
        NotificationManager mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (null != mManager) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("我是有铃声+震动+呼吸灯效果的通知")
                    .setContentText("我是最棒的~")
                    //等价于setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_LIGHTS | Notification.DEFAULT_VIBRATE);
                    .setDefaults(Notification.DEFAULT_ALL);
            mManager.notify(5, builder.build());
        }
    }

    /**
     * 通知无限循环,直到用户取消或者打开通知栏(其实触摸就可以了),效果与FLAG_ONLY_ALERT_ONCE相反
     * 注:这里没有给Notification设置PendingIntent,也就是说该通知无法响应,所以只能手动取消
     */
    public static void showInsistentNotify(Context context) {
        NotificationManager mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (null != mManager) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("我是一个死循环,除非你取消或者响应")
                    .setContentText("啦啦啦~")
                    .setDefaults(Notification.DEFAULT_ALL);
            Notification notify = builder.build();
            notify.flags |= Notification.FLAG_INSISTENT;
            mManager.notify(6, notify);
        }
    }

    /**
     * 通知只执行一次,与默认的效果一样
     */
    public static void showAlertOnceNotify(Context context) {
        NotificationManager mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (null != mManager) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("仔细看,我就执行一遍")
                    .setContentText("好了,已经一遍了~")
                    .setDefaults(Notification.DEFAULT_ALL);
            Notification notify = builder.build();
            notify.flags |= Notification.FLAG_ONLY_ALERT_ONCE;
            mManager.notify(7, notify);
        }
    }

    /**
     * 清除所有通知
     */
    public static void clearNotify(Context context) {
        NotificationManager mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (null != mManager) {
            mManager.cancelAll();
        }
    }

}
