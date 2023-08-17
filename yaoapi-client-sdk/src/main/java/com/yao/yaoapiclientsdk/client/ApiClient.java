package com.yao.yaoapiclientsdk.client;

import com.yao.yaoapiclientsdk.model.City;
import com.yao.yaoapiclientsdk.model.UrlApi;
import com.yao.yaoapiclientsdk.model.User;

/**
 * @author DH
 * @version 1.0
 * @description 接口
 * @date 2023/8/15 19:35
 */
public interface ApiClient {
    /**
     * 调用名称的接口
     * @param user
     * @return
     */
    String getNameByPost(User user);

    /**
     * 获取天气
     * @param city
     * @return
     */
    String getWeatherByPost(City city);

    /**
     * 获取网页图标
     * @param urlApi
     * @return
     */
    String getFaviconByPost(UrlApi urlApi);

    /**
     * 每日一语
     * @return
     */
    String getDailyContentByPost();
}
