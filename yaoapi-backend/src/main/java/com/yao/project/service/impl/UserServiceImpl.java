package com.yao.project.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.yao.common.model.entity.User;
import com.yao.common.model.vo.UserVO;
import com.yao.project.common.EmailUtils;
import com.yao.project.common.ErrorCode;
import com.yao.project.common.JwtUtils;
import com.yao.project.exception.BusinessException;
import com.yao.project.mapper.UserMapper;
import com.yao.project.model.dto.user.UserAddRequest;
import com.yao.project.model.dto.user.UserLoginRequest;
import com.yao.project.model.dto.user.UserRegisterRequest;
import com.yao.project.model.vo.UserKeyVO;
import com.yao.project.service.UserService;
import lombok.Data;
import lombok.Synchronized;
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

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.yao.project.constant.UserConstant.*;
import static com.yao.project.constant.UserConstant.CODE_LENGTH;


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
    @Resource
    private EmailUtils emailUtils;

    /**
     * 盐值，混淆密码
     */
    private static final String SALT = "yao";

    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest,HttpServletRequest request) {
        String signature = request.getHeader("signature");
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String urlNum = userRegisterRequest.getUrlNum();
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword,urlNum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        String urlNumRedis = stringRedisTemplate.opsForValue().get(NUMPIC_PREFIX + signature);
        if(StringUtils.isEmpty(urlNumRedis)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"图片校验码失效，请刷新后重试");
        }
        //这里的比较忽略大小写
        if(!urlNumRedis.equalsIgnoreCase(urlNum)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"图片校验码输入有误");
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
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号已注册过啦");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 分配ak,sk
            this.setUserKey();
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setUserAvatar(USER_PIC);
            user.setUserName("yapi"+userAccount.substring(0,5));
            user.setAccessKey(setUserKey().getAccessKey());
            user.setSecretKey(setUserKey().getSecretKey());
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            return user.getId();
        }

    }

/*    @Override
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
    }*/

    @Override
    public UserVO userLogin(String userAccount, String userPassword, HttpServletResponse res) {
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        if (userAccount.length() < 4 || userAccount.length() > 16) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度不符");
        }
        if (userPassword.length() < 8 || userPassword.length() > 16) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度不符");
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
        return setUserState(user, res);
    }


    /**
     * 通过邮箱登录
     *
     * @param userLoginBySmsRequest 登录的封装类
     * @param res                   响应值设置
     * @return
     */
    @Override
    public UserVO userLoginBySms(UserLoginRequest userLoginBySmsRequest, HttpServletResponse res) {
        String code = userLoginBySmsRequest.getCode();
        String email = userLoginBySmsRequest.getEmail();
        // 1. 校验
        if (StringUtils.isAnyBlank(code, email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不可为空");
        }
        if (!email.matches(EMAIL_REGEX)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入正确的QQ邮箱号");
        }
        String codeTrue = stringRedisTemplate.opsForValue().get(USER_CODE_REDIS + email);
        if (codeTrue == null || codeTrue.isEmpty()) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "请先发送验证码!");
        }
        if (!codeTrue.equals(code)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "验证码错误");
        }
        //2、通过email查找用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        User user = userMapper.selectOne(queryWrapper);
        //2.1 用户不存在，自动注册
        UserVO userVO = new UserVO();
        if (user == null) {
            User user1 = registerByEmail(email);
            return setUserState(user1, res);
        }
        //2.2 用户存在，返回脱敏数据
        BeanUtils.copyProperties(user, userVO);
        return setUserState(user, res);
    }

    /**
     * 邮箱登录，用户不存在，注册
     *
     * @param email
     * @return
     */
    private User registerByEmail(String email) {
        User createUser = new User();
        //防止恶意注册
        synchronized (email.intern()) {
            UserKeyVO userKeyVO = setUserKey();
            createUser.setEmail(email);
            createUser.setUserAvatar(USER_PIC);
            //取邮箱前面的数字为账号和密码
            String account = email.split("@")[0];
            createUser.setUserName( "yapi"+account.substring(0,5));
            createUser.setUserAccount(account);
            // 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + account).getBytes());
            createUser.setUserPassword(encryptPassword);
            createUser.setAccessKey(userKeyVO.getAccessKey());
            createUser.setSecretKey(userKeyVO.getSecretKey());
            //存入数据库
            int insert = userMapper.insert(createUser);
            if (insert < 1) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "邮箱注册失败");
            }
        }
        return createUser;
    }

    public UserKeyVO setUserKey() {
        UserKeyVO userKeyVO = new UserKeyVO();
        // 3. 分配ak,sk
        String accessKey = DigestUtils.md5DigestAsHex((SALT + RandomUtil.randomNumbers(10)).getBytes());
        String secretKey = DigestUtils.md5DigestAsHex((SALT + RandomUtil.randomNumbers(12)).getBytes());
        userKeyVO.setAccessKey(accessKey);
        userKeyVO.setSecretKey(secretKey);
        return userKeyVO;
    }

    @Override
    public boolean sendCode(String email) {
        //1.校验邮箱号
        if (email.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "邮箱不能为空");
        }
        if (!email.matches(EMAIL_REGEX)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请输入正确的邮箱号");
        }
        //2.发送验证码，这里暂时由随机数生成
        String code = RandomUtil.randomNumbers(CODE_LENGTH);
        //SendSmsConfig.getSendSms(telephone,code);
        log.info("当前验证码为{}", code);
        boolean mail = emailUtils.sendMail(email, code);
        if (!mail) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码发送失败");
        }
        //3.将验证码保存到Redis中,并设置有效时常为2分钟
        stringRedisTemplate.opsForValue().set(USER_CODE_REDIS + email, code, 2, TimeUnit.MINUTES);
        return true;
    }

    @Override
    public void getNumPic(HttpServletRequest request, HttpServletResponse response) {
        // signature 用来存到Redis保持唯一性
        String signature = request.getHeader("signature");

        if (StringUtils.isEmpty(signature)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "校验码为空");
        }

        try {
            // 自定义验证码（随机4位，可重复）
            RandomGenerator randomGenerator = new RandomGenerator("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ", 4);
            //hutool工具包生成校验图片
            LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(125, 35);
            lineCaptcha.setGenerator(randomGenerator);
            //设置响应头
            response.setContentType("image/jpeg");
            response.setHeader("Pragma", "No-cache");
            // 输出到页面
            lineCaptcha.write(response.getOutputStream());
            // 打印日志
            log.info("captchaId：{} ----生成的验证码:{}", signature, lineCaptcha.getCode());
            // 将验证码设置到Redis中,2分钟过期
            stringRedisTemplate.opsForValue().set(NUMPIC_PREFIX + signature, lineCaptcha.getCode(), 2, TimeUnit.MINUTES);
            // 关闭流
            response.getOutputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        if (userId == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        String userJson = stringRedisTemplate.opsForValue().get(USER_LOGIN_REDIS + userId);
        User user = new Gson().fromJson(userJson, User.class);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return user;
    }


    /**
     * JWT加密，用户信息缓存
     *
     * @param user user
     * @param res  res
     * @return userVO
     */
    private UserVO setUserState(User user, HttpServletResponse res) {

        String token = JwtUtils.getJwtToken(user.getId(), user.getUserName());
        Cookie cookie = new Cookie("token", token);
        // 让所有路径可拿到
        cookie.setPath("/");
        res.addCookie(cookie);
        // 用户信息缓存
        String userJson = new Gson().toJson(user);
        stringRedisTemplate.opsForValue().set(USER_LOGIN_REDIS + user.getId(), userJson, JwtUtils.EXPIRE, TimeUnit.MILLISECONDS);
        return getLoginUserVO(user);
    }

    /**
     * 获取用户的脱敏信息
     *
     * @param user user
     * @return userVO
     */
    private UserVO getLoginUserVO(User user) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
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
    public boolean userLogout(HttpServletRequest request, HttpServletResponse response) {
        Long userIdByToken = JwtUtils.getUserIdByToken(request);
        if (userIdByToken == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            // 移除登录态
            if (cookie.getName().equals("token")) {
                Cookie timeOut = new Cookie(cookie.getName(), cookie.getValue());
                timeOut.setMaxAge(0);
                response.addCookie(timeOut);
                stringRedisTemplate.delete(USER_LOGIN_REDIS + userIdByToken);
            }
        }
        return true;

    }
}




