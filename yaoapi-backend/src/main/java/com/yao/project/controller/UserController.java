package com.yao.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import com.yao.common.model.entity.User;
import com.yao.common.model.vo.UserVO;
import com.yao.project.common.DeleteRequest;
import com.yao.project.common.ErrorCode;
import com.yao.project.common.Result;
import com.yao.project.exception.BusinessException;
import com.yao.project.model.dto.user.*;
import com.yao.project.model.vo.UserKeyVO;
import com.yao.project.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户接口
 *
 * @author DH
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    //登录操作
    /**
     * 用户注册
     *
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")
    public Result<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest,HttpServletRequest httpServletRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        long result = userService.userRegister(userRegisterRequest,httpServletRequest);
        return Result.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 登录封装类
     * @param res res
     * @return UserVO
     */
    @PostMapping("/login")
    public Result<UserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletResponse res) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UserVO userVO = userService.userLogin(userAccount, userPassword, res);
        return Result.success(userVO);
    }


    /**
     * 发送邮箱验证码
     * @return
     */
    @PostMapping("/sendCode")
    public Result<Boolean> sendCode(@RequestParam String email){
        return Result.success(userService.sendCode(email));
    }

    /**
     * 通过邮箱登录
     * @param userLoginBySmsRequest
     * @param res
     * @return
     */
    @PostMapping("/loginBySms")
    public Result<UserVO> userLoginBySms(@RequestBody UserLoginRequest userLoginBySmsRequest, HttpServletResponse res){
        if(userLoginBySmsRequest == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        UserVO userVO = userService.userLoginBySms(userLoginBySmsRequest,res);
        return Result.success(userVO);
    }

    /**
     * 获取图形验证码
     *
     * @param request
     * @param response
     */
    @GetMapping("/getNumPic")
    public void getNumPic(HttpServletRequest request, HttpServletResponse response) {
        userService.getNumPic(request, response);
    }

    /**
     * 用户注销
     *
     * @param request
     * @param response
     * @return
     */
    @PostMapping("/logout")
    public Result<Boolean> userLogout(HttpServletRequest request,HttpServletResponse response) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.userLogout(request,response);
        return Result.success(result);
    }

    /**
     * 获取当前登录用户
     *
     * @param
     * @return
     */
    @GetMapping("/get/login")
    public Result<UserVO> getLoginUser(HttpServletRequest request) {
        //走本地
        User loginUser = userService.getLoginUser(request);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(loginUser, userVO);
        return Result.success(userVO);
    }


    /**
     * 创建用户
     *
     * @param userAddRequest 添加封装类
     * @param request  request
     * @return 用户id
     */
   /* @PostMapping("/add")
    public Result<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return Result.success(userService.userAdd(userAddRequest));
    }*/

    /**
     * 删除用户
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public Result<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(deleteRequest.getId());
        return Result.success(b);
    }

    /**
     * 更新用户
     *
     * @param userUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public Result<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }


        boolean result = userService.updateUser(userUpdateRequest,request);
        return Result.success(result);
    }

    @PostMapping("/updateUserPic")
    public Result<Boolean> updateUserPic(@RequestParam(required = false) MultipartFile file, HttpServletRequest request) {
        if (file == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请选择上传图片");
        }
        boolean result = userService.updateUserPic(file,request);
        return Result.success(result);
    }


    /**
     * 根据 id 获取用户
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public Result<UserVO> getUserById(int id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return Result.success(userVO);
    }

    /**
     * 获取用户列表
     *
     * @param userQueryRequest
     * @return
     */
    @GetMapping("/list")
    public Result<List<UserVO>> listUser(UserQueryRequest userQueryRequest) {
        User userQuery = new User();
        if (userQueryRequest != null) {
            BeanUtils.copyProperties(userQueryRequest, userQuery);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>(userQuery);
        List<User> userList = userService.list(queryWrapper);
        List<UserVO> userVOList = userList.stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
        }).collect(Collectors.toList());
        return Result.success(userVOList);
    }

    /**
     * 分页获取用户列表
     *
     * @param userQueryRequest
     * @return
     */
    @GetMapping("/list/page")
    public Result<Page<UserVO>> listUserByPage(UserQueryRequest userQueryRequest) {
        long current = 1;
        long size = 5;
        User userQuery = new User();
        if (userQueryRequest != null) {
            BeanUtils.copyProperties(userQueryRequest, userQuery);
            current = userQueryRequest.getCurrent();
            size = userQueryRequest.getPageSize();
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>(userQuery);
        Page<User> userPage = userService.page(new Page<>(current, size), queryWrapper);
        Page<UserVO> userVOPage = new PageDTO<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserVO> userVOList = userPage.getRecords().stream().map(user -> {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return userVO;
        }).collect(Collectors.toList());
        userVOPage.setRecords(userVOList);
        return Result.success(userVOPage);
    }


    @PostMapping("/resetUserKey")
    public Result<UserKeyVO> resetUserKey(HttpServletRequest request){
        return Result.success(userService.resetUserKey(request));
    }

    @PostMapping("/getUserKey")
    public Result<UserKeyVO> getUserKey(HttpServletRequest request){
        return Result.success(userService.getUserKey(request));
    }

}
