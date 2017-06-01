package com.past.music.api.base;

import com.neu.gaojin.annotation.HttpUrlParams;
import com.neu.gaojin.annotation.RequiredParam;
import com.neu.gaojin.request.RequestBase;

/**
 * =======================================================
 * 作者：GaoJin
 * 日期：2017/4/23 13:50
 * 描述：
 * 备注：
 * =======================================================
 */
public class BaseRequestQQApi<T> extends RequestBase<T> {

    @RequiredParam("showapi_appid")
    String showapi_appid = "32384";

    @RequiredParam("showapi_sign")
    String showapi_sign = "0bfb8ffd39e045fcaa90f0f6c2ee4078";
}
