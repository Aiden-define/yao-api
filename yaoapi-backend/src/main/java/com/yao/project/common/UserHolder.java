package com.yao.project.common;

import com.yao.project.model.vo.UserVO;

/**
 * @author DH
 * @version 1.0
 * @description 通过ThreadLocal获取拦截器校验时的用户信息
 * @date 2023/6/3 18:45
 */
public class UserHolder {
    private final static ThreadLocal<UserVO> localUser =  new ThreadLocal<>();
    public static UserVO getLocalUser(){
        return localUser.get();
    }
    public static void setLocalUser(UserVO user){
        localUser.set(user);
    }
    public static void removeLocalUser(){
        localUser.remove();
    }

}
