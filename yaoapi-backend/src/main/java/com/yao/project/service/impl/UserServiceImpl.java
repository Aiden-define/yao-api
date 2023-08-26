package com.yao.project.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.captcha.generator.RandomGenerator;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.yao.common.commonUtils.ErrorCode;
import com.yao.common.commonUtils.JwtUtils;
import com.yao.common.exception.BusinessException;
import com.yao.common.model.entity.User;
import com.yao.common.model.vo.UserVO;
import com.yao.project.common.AliyunOSSUtil;
import com.yao.project.common.EmailUtils;
import com.yao.project.mapper.UserMapper;
import com.yao.project.model.dto.user.*;
import com.yao.project.model.vo.UserKeyVO;
import com.yao.project.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.yao.common.constant.UserConstant.*;


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
    public long userRegister(UserRegisterRequest userRegisterRequest, HttpServletRequest request) {
        String signature = request.getHeader("signature");
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String urlNum = userRegisterRequest.getUrlNum();
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, urlNum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
        String urlNumRedis = stringRedisTemplate.opsForValue().get(NUMPIC_PREFIX + signature);
        if (StringUtils.isEmpty(urlNumRedis)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片校验码失效，请刷新后重试");
        }
        //这里的比较忽略大小写
        if (!urlNumRedis.equalsIgnoreCase(urlNum)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片校验码输入有误");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }

        synchronized (userAccount.intern()) {
            return userRegisterDetails(userAccount, userPassword);
        }

    }

    private Long userRegisterDetails(String userAccount, String userPassword) {
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
        user.setUserName("yapi" + userAccount.substring(0, 5));
        user.setAccessKey(setUserKey().getAccessKey());
        user.setSecretKey(setUserKey().getSecretKey());
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
        }
        return user.getId();
    }

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

    @Override
    public Long userAdd(UserAddRequest userAddRequest) {
        long userAdd = userRegisterDetails(userAddRequest.getUserAccount(), userAddRequest.getUserPassword());
        User user = new User();
        user.setId(userAdd);
        //加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userAddRequest.getUserPassword()).getBytes());
        //赋值
        BeanUtils.copyProperties(userAddRequest, user);
        user.setUserPassword(encryptPassword);
        //插入
        userMapper.updateById(user);
        return userAdd;
    }

    @Override
    public boolean updateUserByAdmin(UserVO userVO) {
        if (StringUtils.isAnyBlank(userVO.getUserAccount(), userVO.getUserRole())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数不能为空");
        }
        User user = new User();
        BeanUtils.copyProperties(userVO, user);
        int i = userMapper.updateById(user);
        return i > 0;
    }

    @Override
    public Page<UserVO> listUserByPage(UserQueryRequest userQueryRequest) {
        long current = 1;
        long size = 5;
        User userQuery = new User();
        if (userQueryRequest != null) {
            BeanUtils.copyProperties(userQueryRequest, userQuery);
            current = userQueryRequest.getCurrent();
            size = userQueryRequest.getPageSize();
        }
        if (size > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "页面一次最多显示50条");
        }
        QueryWrapper<User> queryWrapper = this.addCondition(userQuery);
        Page<User> userPage = this.page(new Page<>(current, size), queryWrapper);
        Page<UserVO> userVOPage = new PageDTO<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserVO> userVOList = userPage.getRecords().stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
        }).collect(Collectors.toList());
        userVOPage.setRecords(userVOList);
        return userVOPage;
    }

    public QueryWrapper<User> addCondition(User userQuery) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        String userName = userQuery.getUserName();
        if (StringUtils.isNotBlank(userName)) {
            queryWrapper.like("userName", userName);
        }
        Integer gender = userQuery.getGender();
        if (gender != null) {
            queryWrapper.eq("gender", gender);
        }
        String userRole = userQuery.getUserRole();
        if (StringUtils.isNotBlank(userRole)) {
            queryWrapper.like("userRole", userRole);
        }
        String userAccount = userQuery.getUserAccount();
        if (StringUtils.isNotBlank(userAccount)) {
            queryWrapper.like("userAccount", userAccount);
        }
        String email = userQuery.getEmail();
        if (StringUtils.isNotBlank(email)) {
            queryWrapper.like("email", email);
        }
        Date createTime = userQuery.getCreateTime();
        if (createTime != null) {
            queryWrapper.le("createTime", createTime);
        }
        return queryWrapper;

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
            createUser.setUserName("yapi" + account.substring(0, 5));
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

    @Override
    public UserKeyVO resetUserKey(HttpServletRequest request) {
        // 先判断是否已登录
        User loginUser = this.getLoginUser(request);

        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        UserKeyVO userKeyVO = this.setUserKey();
        loginUser.setAccessKey(userKeyVO.getAccessKey());
        loginUser.setSecretKey(userKeyVO.getSecretKey());
        int updateById = userMapper.updateById(loginUser);
        if (updateById < 1) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "更新失败");
        }
        String userJson = new Gson().toJson(loginUser);
        //缓存重置
        stringRedisTemplate.opsForValue().set(USER_LOGIN_REDIS + loginUser.getId(), userJson, JwtUtils.EXPIRE, TimeUnit.MILLISECONDS);
        return userKeyVO;
    }

    @Override
    public UserKeyVO getUserKey(HttpServletRequest request) {
        User loginUser = this.getLoginUser(request);
        UserKeyVO userKeyVO = new UserKeyVO();
        userKeyVO.setSecretKey(loginUser.getSecretKey());
        userKeyVO.setAccessKey(loginUser.getAccessKey());
        return userKeyVO;
    }

    @Override
    public boolean updateUserPic(MultipartFile pic, HttpServletRequest request) {
        User loginUser = this.getLoginUser(request);
        String url = AliyunOSSUtil.OSSUploadFile(pic);
        loginUser.setUserAvatar(url);
        //更新缓存
        String json = new Gson().toJson(loginUser);
        stringRedisTemplate.opsForValue().set(USER_LOGIN_REDIS + loginUser.getId(), json, JwtUtils.EXPIRE, TimeUnit.MILLISECONDS);
        return this.updateById(loginUser);

    }

    @Override
    public boolean updateUser(UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        if (StringUtils.isBlank(userUpdateRequest.getUserName())) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "参数不能为空");
        }
        User loginUser = this.getLoginUser(request);
        loginUser.setGender(userUpdateRequest.getGender());
        loginUser.setUserName(userUpdateRequest.getUserName());
        userMapper.updateById(loginUser);
        //更新缓存
        String json = new Gson().toJson(loginUser);
        stringRedisTemplate.opsForValue().set(USER_LOGIN_REDIS + loginUser.getId(), json, JwtUtils.EXPIRE, TimeUnit.MILLISECONDS);
        return this.updateById(loginUser);
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
     * 设置用户的ak，sk
     *
     * @return
     */
    public UserKeyVO setUserKey() {
        UserKeyVO userKeyVO = new UserKeyVO();
        // 3. 分配ak,sk
        String accessKey = DigestUtils.md5DigestAsHex((SALT + RandomUtil.randomNumbers(10)).getBytes());
        String secretKey = DigestUtils.md5DigestAsHex((SALT + RandomUtil.randomNumbers(12)).getBytes());
        userKeyVO.setAccessKey(accessKey);
        userKeyVO.setSecretKey(secretKey);
        return userKeyVO;
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




