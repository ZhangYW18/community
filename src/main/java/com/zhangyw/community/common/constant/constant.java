package com.zhangyw.community.common.constant;

public interface constant {

    /**
     * 用户激活状态，0:激活成功, 1:重复激活, 2:激活失败
     */
    int ACTIVATION_SUCCESS = 0;
    int ACTIVATION_REPEAT = 1;
    int ACTIVATION_FAILURE = 2;

    /**
     *  用户登录状态保存时间，分为勾选/不勾选"记住我"
     */
    int REMEMBER_EXPIRED_SECONDS = 3600*24*30;
    int DEFAULT_EXPIRED_SECONDS = 3600*12;
}
