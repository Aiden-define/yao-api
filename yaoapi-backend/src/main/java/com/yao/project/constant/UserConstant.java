package com.yao.project.constant;

/**
 * 用户常量
 *
 * @author DH
 */
public interface UserConstant {

    String USER_LOGIN_STATE = "userLoginState";
    /**
     * 用户登录态键
     */
    String USER_LOGIN_REDIS = "user:login:";

    /**
     * 系统用户 id（虚拟用户）
     */
    long SYSTEM_USER_ID = 0;

    //  region 权限

    /**
     * 默认权限
     */
    String DEFAULT_ROLE = "user";

    /**
     * 管理员权限
     */
    String ADMIN_ROLE = "admin";

    /**
     * 邮箱校验码
     */
    String EMAIL_REGEX = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";

    /**
     * 发送的验证码长度
     */
    int CODE_LENGTH = 6;

    /**
     * 用户验证码存入redis
     */
    String USER_CODE_REDIS = "user:code:";

    /**
     * 用户默认头像
     */
    String USER_PIC = "https://logistics-back-pic.oss-cn-beijing.aliyuncs.com/2023-08-09/1691583032956-pic.png";

    /**
     * 注册生成的验证码图
     */
    String NUMPIC_PREFIX = "number:pic:";

    /**
     * 用户拥有的接口
     */
    String USER_INTERFACE = "user:interface:";


}
