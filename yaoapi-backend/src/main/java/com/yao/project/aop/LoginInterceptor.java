package com.yao.project.aop;

import cn.hutool.core.bean.BeanUtil;
import com.google.gson.Gson;
import com.yao.project.common.ErrorCode;
import com.yao.project.common.UserHolder;
import com.yao.project.exception.BusinessException;
import com.yao.project.model.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author DH
 * @version 1.0
 * @description 登陆鉴权AOP
 * @date 2023/6/3 13:33
 */
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {
    private final StringRedisTemplate stringRedisTemplate;

    public LoginInterceptor(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("authorization");
        if (token.isEmpty()) {

            throw new BusinessException(ErrorCode.NOT_LOGIN);
            //return false;
        }
        String stringUser = stringRedisTemplate.opsForValue().get(token);
        if (stringUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        //将userVO存入到UserHolder
        Gson gson = new Gson();
        UserVO userVO = gson.fromJson(stringUser, UserVO.class);
        UserHolder.setLocalUser(userVO);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.removeLocalUser();
    }
}
