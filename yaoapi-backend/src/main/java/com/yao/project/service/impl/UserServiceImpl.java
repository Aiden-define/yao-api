package com.yao.project.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.yao.common.model.entity.User;
import com.yao.common.model.vo.UserVO;
import com.yao.project.common.ErrorCode;
import com.yao.project.common.JwtUtils;
import com.yao.project.exception.BusinessException;
import com.yao.project.mapper.UserMapper;
import com.yao.project.model.dto.user.UserAddRequest;
import com.yao.project.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.concurrent.TimeUnit;

import static com.yao.project.constant.UserConstant.*;


/**
 * 用户服务实现类
 *
 * @author DH
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "yao";

    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = userMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 分配ak,sk
            String accessKey = DigestUtils.md5DigestAsHex((SALT + RandomUtil.randomNumbers(10)).getBytes());
            String secretKey = DigestUtils.md5DigestAsHex((SALT + RandomUtil.randomNumbers(12)).getBytes());
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setAccessKey(accessKey);
            user.setSecretKey(secretKey);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }

    }

    @Override
    public long userAdd(UserAddRequest userAddRequest) {
        long userAdd = userRegister(userAddRequest.getUserAccount(), userAddRequest.getUserPassword(), userAddRequest.getUserPassword());
        User user = new User();
        user.setId(userAdd);
        //加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userAddRequest.getUserPassword()).getBytes());
        //赋值
        BeanUtils.copyProperties(userAddRequest, user);
        user.setUserPassword(encryptPassword);
        //更新
        userMapper.updateById(user);
        return userAdd;
    }

    @Override
    public UserVO userLogin(String userAccount, String userPassword, HttpServletResponse res) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号错误");
        }
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码错误");
        }

        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
        }
        return setUserState(user,res);
    }

    /**
     * 获取当前登录用户
     *
     * @param request request
     * @return user
     */
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Long userId = JwtUtils.getUserIdByToken(request);

        if (userId == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        String userJson = stringRedisTemplate.opsForValue().get(USER_LOGIN_REDIS+userId);
        User user = new Gson().fromJson(userJson, User.class);
        if (user == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return user;
    }


    /**
     * JWT加密，用户信息缓存
     * @param user user
     * @param res res
     * @return userVO
     */
    private UserVO setUserState(User user,HttpServletResponse res) {

        String token = JwtUtils.getJwtToken(user.getId(), user.getUserName());
        Cookie cookie = new Cookie("token",token);
        // 让所有路径可拿到
        cookie.setPath("/");
        res.addCookie(cookie);
        // 用户信息缓存
        String userJson = new Gson().toJson(user);
        stringRedisTemplate.opsForValue().set(USER_LOGIN_REDIS+user.getId(),userJson, JwtUtils.EXPIRE,TimeUnit.MILLISECONDS);
        return setLoginUserVO(user);
    }

    /**
     * 获取用户的脱敏信息
     * @param user user
     * @return userVO
     */
    private UserVO setLoginUserVO(User user) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user,userVO);
        return userVO;
    }

    /**
     * 是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        User loginUser = getLoginUser(request);
        return ADMIN_ROLE.equals(loginUser.getUserRole());
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request,HttpServletResponse response) {
        Long userIdByToken = JwtUtils.getUserIdByToken(request);
        if (userIdByToken == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            // 移除登录态
            if(cookie.getName().equals("token")){
                Cookie timeOut = new Cookie(cookie.getName(), cookie.getValue());
                timeOut.setMaxAge(0);
                response.addCookie(timeOut);
                stringRedisTemplate.delete(USER_LOGIN_REDIS + userIdByToken);
            }
        }
        return true;

    }


}




