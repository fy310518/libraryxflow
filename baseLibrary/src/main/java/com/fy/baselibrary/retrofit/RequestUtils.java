package com.fy.baselibrary.retrofit;

import android.os.Handler;
import android.os.Looper;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * 网络请求入口
 * Created by fangs on 2018/3/13.
 */
public final class RequestUtils {

    public volatile static RequestUtils instance;

    protected Retrofit netRetrofit;

    protected OkHttpClient.Builder okBuilder;//使 上层依赖 可以获得唯一的 OkHttpClient；


    private RequestUtils() {
        okBuilder = RequestModule.getClient();
        netRetrofit = RequestModule.getService(okBuilder);
    }

    public static synchronized RequestUtils getInstance() {
        if (null == instance) {
            synchronized (RequestUtils.class) {
                if (null == instance) {
                    instance = new RequestUtils();
                }
            }
        }

        return instance;
    }

    public static OkHttpClient.Builder getOkBuilder() {
        return getInstance().okBuilder;
    }

    /**
     * 得到 RxJava + Retrofit 被观察者 实体类
     *
     * @param clazz 被观察者 类（ApiService.class）
     * @param <T>   被观察者 实体类（ApiService）
     * @return 封装的网络请求api
     */
    public static <T> T create(Class<T> clazz) {
        return getInstance().netRetrofit.create(clazz);
    }

    public interface OnRunUiThreadListener{
        void onRun();
    }

    /**
     * 定义 回调 UI线程
     * @param runUiThreadListener
     */
    public static void runUiThread(OnRunUiThreadListener runUiThreadListener){
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(() -> {
            runUiThreadListener.onRun();
        });
    }


}
