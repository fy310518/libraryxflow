package com.fy.baselibrary.application;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Visibility;
import android.util.ArrayMap;
import android.view.Window;

import androidx.annotation.RequiresApi;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;

import com.fy.baselibrary.application.ioc.ConfigUtils;
import com.fy.baselibrary.application.mvvm.IBaseMVVM;
import com.fy.baselibrary.utils.AnimUtils;
import com.fy.baselibrary.utils.ResUtils;
import com.fy.baselibrary.utils.notify.L;


/**
 * activity 生命周期回调 (api 14+)
 * 注意：使用本框架 activity 与 activity 之间传递数据 统一使用 Bundle
 * Created by fangs on 2017/5/18.
 */
public class BaseActivityLifecycleCallbacks extends BaseLifecycleCallback {
    public static final String TAG = "lifeCycle --> ";

    private ArrayMap<String, BaseOrientationListener> orientationListenerMap = new ArrayMap<>();

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        L.e(TAG + activity.getClass().getSimpleName(), "Create()   " + activity.getTaskId());

        ResUtils.setFontDefault(activity);

        ViewDataBinding vdb = null;
        AndroidViewModel bvm = null;
        IBaseMVVM act = null;
        if (activity instanceof IBaseMVVM) {
            act = (IBaseMVVM) activity;

            vdb = DataBindingUtil.setContentView(activity, act.executeBefore());
            if (activity instanceof LifecycleOwner) vdb.setLifecycleOwner((LifecycleOwner) activity);
            bvm = AnimUtils.createViewModel(activity);
        }

//            注册屏幕旋转监听
        if (ConfigUtils.isOrientation()) {
            BaseOrientationListener orientationListener = new BaseOrientationListener(activity);
            boolean autoRotateOn = (Settings.System.getInt(activity.getContentResolver(),
                    Settings.System.ACCELEROMETER_ROTATION, 0) == 1);

            //检查系统是否开启自动旋转
            if (autoRotateOn) orientationListener.enable();
            orientationListenerMap.put(activity.getClass().getSimpleName() + "-" + activity.getTaskId(), orientationListener);
        }

        //基础配置 执行完成，再执行 初始化 activity 操作
        if (null != act) act.initData(bvm, vdb, savedInstanceState);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        L.e(TAG + activity.getClass().getSimpleName(), "--Start()");
    }

    @Override
    public void onActivityResumed(Activity activity) {
        String simpleName = activity.getClass().getSimpleName();
        L.e(TAG + simpleName, "--Resume()");
    }

    @Override
    public void onActivityPaused(Activity activity) {
        String simpleName = activity.getClass().getSimpleName();
        L.e(TAG + simpleName, "--Pause()");
    }

    @Override
    public void onActivityStopped(Activity activity) {
        L.e(TAG + activity.getClass().getSimpleName(), "--Stop()");
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        L.e(TAG + activity.getClass().getSimpleName(), "--SaveInstanceState()");
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        L.e(TAG + activity.getClass().getSimpleName(), "--Destroy()");

        //销毁 屏幕旋转监听
        BaseOrientationListener orientationListener = orientationListenerMap.get(activity.getClass().getSimpleName() + "-" + activity.getTaskId());
        if (null != orientationListener){
            orientationListener.disable();
            orientationListenerMap.remove(activity.getClass().getSimpleName() + "-" + activity.getTaskId());
        }
    }


    // 设置 activity 进出动画
    private void setAnim(Activity activity) {
        String transition = activity.getIntent().getStringExtra("transition");

        if (TextUtils.isEmpty(transition)) return;

        Visibility animType = null;

        // 设置进入时进入动画
        if (transition.equals("explode")) {
            animType = new Explode();  // 分解
        } else if (transition.equals("slide")) {
            animType = new Slide();  // 滑进滑出
        } else if (transition.equals("fade")) {
            animType = new Fade(); // 淡入淡出
        }

        if (null != animType){
            activity.getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS); // 开启动画的特征

            animType.setDuration(500);
            activity.getWindow().setEnterTransition(animType);
            activity.getWindow().setExitTransition(animType);
        }
    }


}
