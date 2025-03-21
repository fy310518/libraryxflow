package com.fy.baselibrary.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.ArrayRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.fy.baselibrary.application.ioc.ConfigUtils;
import com.fy.baselibrary.utils.notify.L;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;
import java.util.Random;

/**
 * 获取项目资源 （如：res目录下的，assets 目录下的，manifest文件配置的资源等）
 * Created by fangs on 2017/9/13.
 */
public class ResUtils {

    private ResUtils() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 获取 dimens 资源文件 指定 id 的资源
     *
     * @param dimenId 资源id
     * @return dimen 值
     */
    public static float getDimen(@DimenRes int dimenId) {
        return ConfigUtils.getAppCtx().getResources().getDimension(dimenId);
    }

    /**
     * 获取 colors 资源文件 指定 id 的资源 (getResources().getColor 过时 替代方式)
     *
     * @param colorId 资源id
     * @return color
     */
    @ColorInt
    public static int getColor(@ColorRes int colorId) {
        return ContextCompat.getColor(ConfigUtils.getAppCtx(), colorId);
    }

    /**
     * 生成随机颜色
     * @return 颜色值
     */
    public static int getRandomColor() {
        Random random = new Random();
        int r = random.nextInt(256);
        int g = random.nextInt(256);
        int b = random.nextInt(256);

        return Color.rgb(r, g, b);
    }

    /**
     * 获取 strings 资源文件 指定 id 的资源
     *
     * @param stringId 资源id
     * @return ""
     */
    public static String getStr(@StringRes int stringId) {
        return ConfigUtils.getAppCtx().getString(stringId);
    }

    /**
     * 获取 strings 资源文件 指定 id 的资源
     * @param StrArrayId 资源数组id
     * @return String[]
     */
    public static String[] getStrArray(@ArrayRes int StrArrayId){
        return ConfigUtils.getAppCtx().getResources().getStringArray(StrArrayId);
    }

    /**
     * 获取 strings 资源文件 指定 id 的资源
     * @param intArrayId 资源数组id
     * @return int[]
     */
    public static int[] getIntArray(@ArrayRes int intArrayId){
        return ConfigUtils.getAppCtx().getResources().getIntArray(intArrayId);
    }

    /**
     * %d   （表示整数）
     * %f   （表示浮点数）
     * %s   （表示字符串）
     * 获取 strings 资源文件，指定 id 的资源，替换后的字符串
     *
     * @param id   资源ID（如：ID内容为 “病人ID：%1$d”）
     * @param args 将要替换的内容
     * @return 替换后的字符串
     */
    public static String getReplaceStr(@StringRes int id, Object... args) {
        String format = getStr(id);
        return String.format(format, args);
    }

    /**
     * 获取清单文件 指定 key的 meta-data 的值；（meta-data是一个键值对）
     *
     * @param metaKey       meta-data 的 key
     * @param defaultValue 返回值类型
     * @return
     */
    public static Object getMetaData(Context context, String metaKey, Object defaultValue) {
        Object value = null;

        try {
            ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);

            Bundle bundle = ai.metaData;
            if (defaultValue instanceof Integer) {
                value = bundle.getInt(metaKey, (Integer) defaultValue);
            } else if (defaultValue instanceof Float) {
                value = bundle.getFloat(metaKey, (Float) defaultValue);
            } else if (defaultValue instanceof Boolean) {
                value = bundle.getBoolean(metaKey, (Boolean) defaultValue);
            } else if (defaultValue instanceof String) {
                value = bundle.getString(metaKey, (String) defaultValue);
            }

        } catch (Exception e) {
            L.e("获取清单文件配置失败", "Dear developer. Don't forget to configure <meta-data android:name=\"my_test_metagadata\" android:value=\"testValue\"/> in your AndroidManifest.xml file.");
        }

        return value;
    }

    /**
     * 读取指定文件的 Json内容
     * @param fileName  文件，带后缀名(手机sd卡 中的文件要传 文件绝对路径，assets 目录下的文件，如果嵌套在 文件夹下，要带 父级文件路径)
     * @return 返回json 字符串
     */
    public static String getAssetsJson(String fileName) {
        StringBuilder stringBuilder = new StringBuilder();

        InputStream is = null;
        BufferedReader bf = null;
        try {
            if (FileUtils.fileIsExist(fileName)) {
                is = new FileInputStream(fileName);
            } else {
                is = ResUtils.getAssetsInputStream(fileName);
            }

            bf = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != is) is.close();
                if (null != bf) bf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 读取 assets 目录下指定文件名的 内容 【如：config.properties】
     * @param fileName
     * @return Properties （使用 properties.getProperty("key") 获取 value）
     */
    public static Properties getProperties(String fileName) {
        Context context = ConfigUtils.getAppCtx();
        Properties props = new Properties();

        try {
            InputStream in = context.getAssets().open(fileName);
            props.load(in);
        } catch (Exception var5) {
            var5.printStackTrace();
        }

        return props;
    }

    /**
     * 读取 assets 目录下指定文件名的 InputStream
     * @param fileName 【aa.txt 或 img/semll.jpg】
     * @return
     */
    public static InputStream getAssetsInputStream(String fileName){
        Context context = ConfigUtils.getAppCtx();
        try {
            return context.getAssets().open(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 判断assets文件夹下的文件是否存在
     * @return false 不存在 true 存在
     */
    public static boolean isFileExists(String filename, String path) {
        AssetManager assetManager = ConfigUtils.getAppCtx().getAssets();

        try {
            String[] names = assetManager.list(path);

            for(String name : names){
                if(name.equals(filename)){
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    /*****************************以下为 通过资源名称 获取资源id ***************************************/
    private static final String RES_ID = "id";
    private static final String RES_STRING = "string";
    private static final String RES_DRAWABLE = "drawable";
    private static final String RES_LAYOUT = "layout";
    private static final String RES_STYLE = "style";
    private static final String RES_COLOR = "color";
    private static final String RES_DIMEN = "dimen";
    private static final String RES_ANIM = "anim";
    private static final String RES_MENU = "menu";

    /**
     * 获取资源文件的id
     *
     * @param resName
     * @return
     */
    public static int getId(String resName) {
        return getResId(resName, RES_ID);
    }

    /**
     * 获取资源文件string的id
     *
     * @param resName
     * @return
     */
    public static int getStringId(String resName) {
        return getResId(resName, RES_STRING);
    }

    /**
     * 获取资源文件drawable的id
     * @param drawableName
     * @return
     */
    public static int getDrawableId(String drawableName) {
        return getResId(drawableName, RES_DRAWABLE);
    }

    /**
     * 获取资源文件layout的id
     *
     * @param resName
     * @return
     */
    public static int getLayoutId(String resName) {
        return getResId(resName, RES_LAYOUT);
    }

    /**
     * 获取资源文件style的id
     *
     * @param resName
     * @return
     */
    public static int getStyleId(String resName) {
        return getResId(resName, RES_STYLE);
    }

    /**
     * 获取资源文件color的id
     *
     * @param resName
     * @return
     */
    public static int getColorId(String resName) {
        return getResId(resName, RES_COLOR);
    }

    /**
     * 获取资源文件dimen的id
     *
     * @param resName
     * @return
     */
    public static int getDimenId(String resName) {
        return getResId(resName, RES_DIMEN);
    }

    /**
     * 获取资源文件ainm的id
     *
     * @param resName
     * @return
     */
    public static int getAnimId(String resName) {
        return getResId(resName, RES_ANIM);
    }

    /**
     * 获取资源文件menu的id
     */
    public static int getMenuId(String resName) {
        return getResId(resName, RES_MENU);
    }

    /**
     * 获取资源文件ID
     *
     * @param resName
     * @param defType
     * @return
     */
    public static int getResId(String resName, String defType) {
        Context context = ConfigUtils.getAppCtx();
        return context.getResources().getIdentifier(resName, defType, context.getPackageName());
    }



    /**
     * 设置 app字体是否跟随系统 字体
     * @param act
     */
    public static void setFontDefault(Activity act) {
        if (ConfigUtils.isFontDefault()) return;

        Resources res = act.getResources();
        if (res.getConfiguration().fontScale != 1) {//非默认值
            Configuration newConfig = new Configuration();
            newConfig.setToDefaults();//设置默认
            res.updateConfiguration(newConfig, res.getDisplayMetrics());
        }
    }

}
