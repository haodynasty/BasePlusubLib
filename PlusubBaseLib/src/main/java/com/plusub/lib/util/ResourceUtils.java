package com.plusub.lib.util;

import android.content.Context;
import android.view.animation.AnimationUtils;

import com.plusub.lib.bean.ResType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * ResourceUtils
 * 资源获取工具类
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2012-5-26
 */
public class ResourceUtils {

    /**
     * get an asset using ACCESS_STREAMING mode. This provides access to files that have been bundled with an
     * application as assets -- that is, files placed in to the "assets" directory.
     * 
     * @param context
     * @param fileName The name of the asset to open. This name can be hierarchical.
     * @return
     */
    public static String geFileFromAssets(Context context, String fileName) {
        if (context == null || StringUtils.isEmpty(fileName)) {
            return null;
        }

        StringBuilder s = new StringBuilder("");
        try {
            InputStreamReader in = new InputStreamReader(context.getResources().getAssets().open(fileName));
            BufferedReader br = new BufferedReader(in);
            String line;
            while ((line = br.readLine()) != null) {
                s.append(line);
            }
            return s.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * get content from a raw resource. This can only be used with resources whose value is the name of an asset files
     * -- that is, it can be used to open drawable, sound, and raw resources; it will fail on string and color
     * resources.
     * 
     * @param context
     * @param resId The resource identifier to open, as generated by the appt tool.
     * @return
     */
    public static String geFileFromRaw(Context context, int resId) {
        if (context == null) {
            return null;
        }

        StringBuilder s = new StringBuilder();
        try {
            InputStreamReader in = new InputStreamReader(context.getResources().openRawResource(resId));
            BufferedReader br = new BufferedReader(in);
            String line;
            while ((line = br.readLine()) != null) {
                s.append(line);
            }
            return s.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取资源类型
     * @param type
     * @param context
     * @param id
     * @return
     */
    public static Object loadRes(ResType type, Context context, int id) {
        if (context == null || id < 1) return null;
        switch (type) {
            case Animation:
                return AnimationUtils.loadAnimation(context, id);
            case Boolean:
                return context.getResources().getBoolean(id);
            case Color:
                return context.getResources().getColor(id);
            case ColorStateList:
                return context.getResources().getColorStateList(id);
            case Dimension:
                return context.getResources().getDimension(id);
            case DimensionPixelOffset:
                return context.getResources().getDimensionPixelOffset(id);
            case DimensionPixelSize:
                return context.getResources().getDimensionPixelSize(id);
            case Drawable:
                return context.getResources().getDrawable(id);
            case Integer:
                return context.getResources().getInteger(id);
            case IntArray:
                return context.getResources().getIntArray(id);
            case Movie:
                return context.getResources().getMovie(id);
            case String:
                return context.getResources().getString(id);
            case StringArray:
                return context.getResources().getStringArray(id);
            case Text:
                return context.getResources().getText(id);
            case TextArray:
                return context.getResources().getTextArray(id);
            case Xml:
                return context.getResources().getXml(id);
            default:
                break;
        }

        return null;
    }

}
