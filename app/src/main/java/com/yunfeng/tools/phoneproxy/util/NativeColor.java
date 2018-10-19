package com.yunfeng.tools.phoneproxy.util;

import java.util.HashMap;
import java.util.Map;

/**
 * native color
 * Created by xll on 2018/10/19.
 */
public class NativeColor {
    private static final Map<String, Integer> nativeColorMap = new HashMap<String, Integer>(8);

    static {
        nativeColorMap.put("white", android.R.color.white);
        nativeColorMap.put("red", android.R.color.holo_red_dark);
        nativeColorMap.put("blue", android.R.color.holo_blue_dark);
        nativeColorMap.put("green", android.R.color.holo_green_dark);
    }

    public static Integer getNativeColorByName(String color) {
        return nativeColorMap.get(color);
    }

}
