package com.example.customview;

import android.content.Context;
import android.util.TypedValue;

/**
 * Describe :
 * Created by Knight on 2018/12/22
 * 点滴之行,看世界
 **/
public class SystemUtil {
    /**
     * dp转px
     *
     * @param context
     * @param dpVal
     * @return
     */
    public static int dp2px(Context context,float dpVal){
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dpVal,context.getResources().getDisplayMetrics());
    }

    /**
     * sp转px
     * @param context
     * @param spVal
     * @return
     *
     */
    public static int sp2px(Context context,float spVal){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,spVal,context.getResources().getDisplayMetrics());
    }

}
