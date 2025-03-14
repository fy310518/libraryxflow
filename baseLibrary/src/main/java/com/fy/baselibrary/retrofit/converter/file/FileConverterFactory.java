package com.fy.baselibrary.retrofit.converter.file;

import androidx.annotation.Nullable;

import com.fy.baselibrary.retrofit.load.down.DownLoadFileType;
import com.fy.baselibrary.retrofit.load.up.UpLoadFileType;
import com.fy.baselibrary.retrofit.load.up.UpLoadFileType2;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * 上传文件 转换器工厂
 * Created by fangs on 2018/11/12.
 */
public class FileConverterFactory extends Converter.Factory {

    private FileConverterFactory() {}

    public static FileConverterFactory create(){
        return new FileConverterFactory();
    }


    @Nullable
    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type,
                                                          Annotation[] parameterAnnotations,
                                                          Annotation[] methodAnnotations,
                                                          Retrofit retrofit) {
        //进行条件判断，如果传进来的 methodAnnotations 不包含 UpLoadFileType，则匹配失败
        for (Annotation annotation : methodAnnotations) {
            if (annotation instanceof UpLoadFileType) {
                return new FileRequestBodyConverter();
            } else if (annotation instanceof UpLoadFileType2){
                return new FileRequestBodyConverter2();
            }
        }

        return null;
    }

    @Override
    public Converter<ResponseBody, File> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
//        for (Annotation annotation : annotations) {
//            if (annotation instanceof DownLoadFileType) {
//                return new FileResponseBodyConverter();
//            }
//        }
        return null;
    }
}
