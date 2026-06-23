package com.fy.baselibrary.utils;

import android.annotation.SuppressLint;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.collection.LruCache;
import androidx.core.graphics.TypefaceCompat;

import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;

import com.fy.baselibrary.application.ioc.ConfigUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SpannableString 富文本显示 工具类
 * Created by fangs on 2018/5/10.
 */
public class SpanUtils {

    private static LruCache<String, Typeface> cache = new LruCache<String, Typeface>(5){
        // 单个体积粗略估算，Typeface无直接字节API，一般固定返回1按个数限制
        @Override
        protected int sizeOf(String key, Typeface value) {
            return 1;
        }
    };


    private SpanUtils () {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 获取复用Typeface，统一使用Application上下文
     */
    public static Typeface getTypeface(int weight, boolean italic) {
        String key = "Typeface.SANS_SERIF_" + weight + "_" + italic;

        // 命中缓存直接返回
        Typeface typeface = cache.get(key);
        if(typeface == null){
            // 强制使用Application上下文，杜绝页面泄漏
            typeface = TypefaceCompat.create(ConfigUtils.getAppCtx(), Typeface.SANS_SERIF, weight, italic);
            cache.put(key, typeface);

            return typeface;
        } else {
            return typeface;
        }
    }

    // 低内存时清空缓存释放Native资源
    public static void releaseAll() {
        cache.evictAll();
    }


    /**
     * 获取建造者
     * @return
     */
    public static Builder getBuilder() {
        return new Builder();
    }
    public static Builder getBuilder(@NonNull CharSequence text) {
        return new Builder(text);
    }


    public static class Builder {

        private SpannableStringBuilder spanBuilder;
        private int flag;

        /** 截取字符 */
        private String replaceStr;
        private int orientation;

        /** 前景色 */
        @ColorRes
        private int fgColor;
        /** 背景色 */
        @ColorRes
        private int bgColor;
        /**
         * 字体大小
         */
        @DimenRes
        private int textDpSize;
        /**
         * 字重
         */
        private int textFontWeight = 0;
        /** 字体样式 */
        private int textStyle = Typeface.NORMAL;
        /** 是否添加删除线 */
        private boolean isStrikethrough = false;

        public Builder() {
            init("");
        }

        public Builder(@NonNull CharSequence text) {
            init(text);
        }

        private void init(@NonNull CharSequence text){
            flag = Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;
            spanBuilder = new SpannableStringBuilder(text);
        }

        public Builder setFgColor(int fgColor) {
            this.fgColor = fgColor;
            return this;
        }

        public Builder setBgColor(int bgColor) {
            this.bgColor = bgColor;
            return this;
        }

        public Builder setTextDpSize(int textDpSize) {
            this.textDpSize = textDpSize;
            return this;
        }

        public Builder setTextFontWeight(int textFontWeight) {
            this.textFontWeight = textFontWeight;
            return this;
        }

        public Builder setTextStyle(int textStyle) {
            this.textStyle = textStyle;
            return this;
        }

        public Builder setStrikethrough(boolean strikethrough) {
            isStrikethrough = strikethrough;
            return this;
        }

        /**
         * 创建样式字符串
         *
         * @return 样式字符串
         */
        public SpannableStringBuilder create() {
            return spanBuilder;
        }


        /**
         * 根据指定的 “子字符串” 以及方向（orientation）设置 字符串样式
         * @param orientation 0 之前（0-n），-1 之后（n - strContent.length()）
         */
        public Builder slice(@NonNull String replaceStr, @IntRange(from = -1, to = 0) int orientation, ClickableSpan clickableSpan) {
            this.replaceStr = replaceStr;
            this.orientation = orientation;

            int index = spanBuilder.toString().indexOf(replaceStr);
            if (index != -1) {
                int start, end;
                if (orientation == 0) {
                    start = 0;
                    end = index;
                } else {
                    start = index;
                    end = spanBuilder.length();
                }
                setSpan(start, end, clickableSpan);
            }

            return this;
        }

        /**
         * 设置 指定字符串 样式
         *
         * @return 样式字符串
         */
        public Builder append(@NonNull CharSequence text, ClickableSpan clickableSpan) {
            int start = spanBuilder.length();
            spanBuilder.append(text);
            int end = spanBuilder.length();

            setSpan(start, end, clickableSpan);
            return this;
        }

        /**
         * 关键字 高亮显示
         * @param keywordText  关键字
         */
        public Builder keywordHighlight(@NonNull String keywordText) {
            Pattern p = Pattern.compile(keywordText);
            Matcher m = p.matcher(spanBuilder);

            while (m.find()) {
                int start = m.start();
                int end = m.end();
                setSpan(start, end, null);
            }

            return this;
        }

        /**
         * 设置样式
         */
        @SuppressLint("ResourceType")
        private void setSpan(int start, int end, ClickableSpan clickableSpan) {
            if (fgColor > 0) {
                int color = ResUtils.getColor(fgColor);
                spanBuilder.setSpan(new ForegroundColorSpan(color), start, end, flag);
            }

            if (bgColor > 0) {
                int color = ResUtils.getColor(bgColor);
                spanBuilder.setSpan(new BackgroundColorSpan(color), start, end, flag);
            }

            if (textDpSize > 0) {
                int size = (int) ResUtils.getDimen(textDpSize);
                //设置字体大小（绝对值,单位：像素）,第二个参数boolean dip，如果为true，表示前面的字体大小单位为dip，否则为像素
                spanBuilder.setSpan(new AbsoluteSizeSpan(size, false), start, end, flag);
            }

            if (textFontWeight > 0) {
                Typeface tf = getTypeface(textFontWeight, false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    spanBuilder.setSpan(new TypefaceSpan(tf), start, end, flag);
                } else {
                    spanBuilder.setSpan(new StyleSpan(Typeface.NORMAL), start, end, flag);
                }
            }

            if (textStyle > Typeface.NORMAL){
                spanBuilder.setSpan(new StyleSpan(textStyle), start, end, flag);
            }

            if(isStrikethrough){
                spanBuilder.setSpan(new StrikethroughSpan(), start, end, flag);
            }

            if (null != clickableSpan){
                spanBuilder.setSpan(clickableSpan, start, end, flag);
            }
        }
    }



    //局部 文本点击事件 抽象类
    public abstract static class ClickText extends ClickableSpan{
        @Override
        public void updateDrawState(TextPaint ds) {
//            super.updateDrawState(ds);
//            ds.setColor(ds.linkColor);//设置颜色
            ds.setUnderlineText(false);//去掉下划线
        }
    }

}
