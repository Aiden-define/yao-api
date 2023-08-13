package com.yao.project.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.common.model.entity.User;
import com.yao.common.model.vo.UserVO;
import com.yao.project.model.dto.user.UserAddRequest;
import com.yao.project.model.dto.user.UserLoginRequest;
import com.yao.project.model.dto.user.UserRegisterRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 用户服务
 *
 * @author DH
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @return 新用户 id
     */
    long userRegister(UserRegisterRequest userRegisterRequest,HttpServletRequest request);

   /* *//**
     * 添加用户
     * @param userAddRequest
     * @return
     *//*
    long userAdd(UserAddRequest userAddRequest);*/

    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param res
     * @return token
     */
    UserVO userLogin(String userAccount, String userPassword, HttpServletResponse res);

    /**
     * 获取当前登录用户
     * @param request
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取当前登录用户
     * 以ThreadLocal优化流程
     *
     * @param request
     * @return
     */
    //User getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    boolean userLogout(HttpServletRequest request,HttpServletResponse response);

    /**
     * 通过邮箱登录
     * @param userLoginBySmsRequest
     * @param res
     * @return
     */
    UserVO userLoginBySms(UserLoginRequest userLoginBySmsRequest, HttpServletResponse res);

    /**
     * 发送验证码
     * @return
     */
    boolean sendCode(String email);

    /**
     * 获取图形验证码
     * @param request
     * @param response
     */
    void getNumPic(HttpServletRequest request, HttpServletResponse response);
}
