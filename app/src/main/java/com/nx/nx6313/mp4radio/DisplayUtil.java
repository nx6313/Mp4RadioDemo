package com.nx.nx6313.mp4radio;

import android.content.Context;

/**
 * Created by nx6313 on 2018/3/10.
 */

public class DisplayUtil {
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
