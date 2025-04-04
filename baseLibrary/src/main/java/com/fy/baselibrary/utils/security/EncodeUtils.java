package com.fy.baselibrary.utils.security;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;

import com.fy.baselibrary.application.ioc.ConfigUtils;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * 编码、解码工具类
 * Created by fangs on 2017/5/18.
 */
public final class EncodeUtils {

    private EncodeUtils() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 使用 URLEncoder.encode 编码然后在做网络请求
     * @param input The input.
     * @return the urlencoded string
     */
    public static String urlEncode(final String input) {
        try {
            return URLEncoder.encode(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
//            throw new AssertionError(e);
        }

        return "";
    }

    /**
     * 使用 URLDecoder.decode解码，然后在使用对应的 加密方式解码（如：base64）
     *
     * @param input The input.
     * @return the string of decode urlencoded string
     */
    public static String urlDecode(final String input) {
        try {
            return URLDecoder.decode(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }



    /**
     * Base64编码 字符串返回 byte数
     *
     * @param input The input.
     * @return Base64-encode bytes
     */
    public static byte[] base64Encode(final String input) {
        return Base64.encode(input.getBytes(), Base64.NO_WRAP);
    }

    /**
     * Base64编码 byte数组 返回字符串
     *
     * @param input The input.
     * @return Base64-encode string
     */
    public static String base64Encode2String(final byte[] input) {
        return Base64.encodeToString(input, Base64.NO_WRAP);
    }

    /**
     * Base64解码 字符串返回字符串
     *
     * @param input The input.
     * @return the string of decode Base64-encode string
     */
    public static String base64Decode2String(final String input) {
        byte[] temp = Base64.decode(input, Base64.NO_WRAP);
        try {
            return new String(temp, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Base64 转码并保存成文件
     * @param base64Code
     * @param savePath
     * @throws Exception
     */
    public static void decoderBase64File(String base64Code, String savePath)  {
        if (TextUtils.isEmpty(base64Code)) return;
        byte[] buffer = Base64.decode(base64Code.split(",")[1], Base64.DEFAULT);

        for (int i = 0; i < buffer.length; ++i) {
            if (buffer[i] < 0) {// 调整异常数据
                buffer[i] += 256;
            }
        }

        FileOutputStream out = null;
        try {
            out = new FileOutputStream(savePath);
            out.write(buffer);

            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != out) out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Base64 转码并保存成文件
     * @param base64Code
     * @param uri
     */
    public static void decoderBase64File(String base64Code, Uri uri)  {
        if (TextUtils.isEmpty(base64Code)) return;
        byte[] buffer = Base64.decode(base64Code.split(",")[1], Base64.DEFAULT);

        for (int i = 0; i < buffer.length; ++i) {
            if (buffer[i] < 0) {// 调整异常数据
                buffer[i] += 256;
            }
        }
        ContentResolver resolver = ConfigUtils.getAppCtx().getContentResolver();

        OutputStream out = null;
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer);
        try {
            out = resolver.openOutputStream(uri);

            byte data[] = new byte[1024];
            int len;
            while ((len = byteArrayInputStream.read(data, 0, 1024)) != -1) {
                out.write(data, 0, len);
            }

            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != out) out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将图片转换成Base64编码的字符串
     */
    public static String imageToBase64(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        InputStream is = null;
        byte[] data = null;
        String result = null;
        try {
            is = new FileInputStream(path);
            //创建一个字符流大小的数组。
            data = new byte[is.available()];
            //写入数组
            is.read(data);
            //用默认的编码格式进行编码
            result = Base64.encodeToString(data, Base64.NO_CLOSE);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return "data:image/jpeg;base64," + result;
    }

    /**
     * 将Base64编码转换为图片
     * @param base64Str
     * @param path
     * @return true
     */
    public static boolean base64ToFile(String base64Str, String path) {
        byte[] data = Base64.decode(base64Str, Base64.NO_WRAP);
        for (int i = 0; i < data.length; i++) {
            if (data[i] < 0) {
                //调整异常数据
                data[i] += 256;
            }
        }
        OutputStream os = null;
        try {
            os = new FileOutputStream(path);
            os.write(data);
            os.flush();
            os.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Return html-encode string.
     *
     * @param input The input.
     * @return html-encode string
     */
    public static String htmlEncode(final CharSequence input) {
        StringBuilder sb = new StringBuilder();
        char c;
        for (int i = 0, len = input.length(); i < len; i++) {
            c = input.charAt(i);
            switch (c) {
                case '<':
                    sb.append("&lt;"); //$NON-NLS-1$
                    break;
                case '>':
                    sb.append("&gt;"); //$NON-NLS-1$
                    break;
                case '&':
                    sb.append("&amp;"); //$NON-NLS-1$
                    break;
                case '\'':
                    //http://www.w3.org/TR/xhtml1
                    // The named character reference &apos; (the apostrophe, U+0027) was
                    // introduced in XML 1.0 but does not appear in HTML. Authors should
                    // therefore use &#39; instead of &apos; to work as expected in HTML 4
                    // user agents.
                    sb.append("&#39;"); //$NON-NLS-1$
                    break;
                case '"':
                    sb.append("&quot;"); //$NON-NLS-1$
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Return the string of decode html-encode string.
     *
     * @param input The input.
     * @return the string of decode html-encode string
     */
    @SuppressWarnings("deprecation")
    public static CharSequence htmlDecode(final String input) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(input, Html.FROM_HTML_MODE_LEGACY);
        } else {
            return Html.fromHtml(input);
        }
    }
}
