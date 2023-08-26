package com.yao.project.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yao.common.commonUtils.DeleteRequest;
import com.yao.common.commonUtils.ErrorCode;
import com.yao.common.commonUtils.IdRequest;
import com.yao.common.commonUtils.Result;
import com.yao.common.exception.BusinessException;
import com.yao.common.model.entity.InterfaceInfo;
import com.yao.common.model.entity.User;
import com.yao.common.model.entity.UserInterfaceInfo;
import com.yao.project.annotation.AuthCheck;
import com.yao.project.model.dto.interfaceinfo.InterfaceInfoAddRequest;
import com.yao.project.model.dto.interfaceinfo.InterfaceInfoInvokeRequest;
import com.yao.project.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import com.yao.project.model.dto.interfaceinfo.InterfaceInfoUpdateRequest;
import com.yao.project.model.vo.InterfaceInfoUserVO;
import com.yao.project.service.InterfaceInfoService;
import com.yao.project.service.UserInterfaceInfoService;
import com.yao.project.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * 接口
 *
 * @author DH
 */
@RestController
@RequestMapping("/interfaceInfo")
@Slf4j
public class InterfaceInfoController {

    @Resource
    private InterfaceInfoService interfaceInfoService;

    @Resource
    private UserService userService;

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;
    /**
     * 增
     * @param interfaceInfoAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public Result<Long> addInterfaceInfo(@RequestBody InterfaceInfoAddRequest interfaceInfoAddRequest, HttpServletRequest request) {
        if (interfaceInfoAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"请输入完整参数");
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoAddRequest, interfaceInfo);
        // 校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo, true);
        User loginUser = userService.getLoginUser(request);
        interfaceInfo.setUserId(loginUser.getId());
        boolean result = interfaceInfoService.save(interfaceInfo);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"添加失败");
        }
        long newInterfaceInfoId = interfaceInfo.getId();
        //返回id
        return Result.success(newInterfaceInfoId);
    }

    /**
     * 删除
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public Result<Boolean> deleteInterfaceInfo(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可删除
        if (!oldInterfaceInfo.getUserId().equals(loginUser.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        boolean b = interfaceInfoService.removeById(id);
        return Result.success(b);
    }

    /**
     * 更新
     *
     * @param interfaceInfoUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public Result<Boolean> updateInterfaceInfo(@RequestBody InterfaceInfoUpdateRequest interfaceInfoUpdateRequest,
                                            HttpServletRequest request) {
        if (interfaceInfoUpdateRequest == null || interfaceInfoUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoUpdateRequest, interfaceInfo);
        // 参数校验
        interfaceInfoService.validInterfaceInfo(interfaceInfo, false);
        User loginUser = userService.getLoginUser(request);
        long id = interfaceInfoUpdateRequest.getId();
        // 判断是否存在
        InterfaceInfo oldInterfaceInfo = interfaceInfoService.getById(id);
        if (oldInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 仅本人或管理员可修改
        if (!oldInterfaceInfo.getUserId().equals(loginUser.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        boolean result = interfaceInfoService.updateById(interfaceInfo);
        return Result.success(result);
    }

    /**
     * 根据 id 获取
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public Result<InterfaceInfoUserVO> getInterfaceInfoById(long id,HttpServletRequest request) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfoUserVO interfaceInfoUserVO = new InterfaceInfoUserVO();

        User loginUser = userService.getLoginUser(request);
        InterfaceInfo interfaceInfo = interfaceInfoService.getById(id);
        BeanUtils.copyProperties(interfaceInfo,interfaceInfoUserVO);
        QueryWrapper<UserInterfaceInfo> infoQueryWrapper = new QueryWrapper<>();
        infoQueryWrapper.eq("userId",loginUser.getId()).eq("interfaceInfoId",id);
        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoService.getOne(infoQueryWrapper);
        if(userInterfaceInfo==null){
          //说明该用户还没有该接口的调用次数
            interfaceInfoUserVO.setTotalNum(0L);
            interfaceInfoUserVO.setLeftNum(0);
        }else {
            interfaceInfoUserVO.setTotalNum(userInterfaceInfo.getTotalNum());
            interfaceInfoUserVO.setLeftNum(userInterfaceInfo.getLeftNum());
        }
        return Result.success(interfaceInfoUserVO);
    }

    /**
     * 获取列表（仅管理员可使用）
     *
     * @param interfaceInfoQueryRequest
     * @return
     */
    @AuthCheck(mustRole = "admin")
    @GetMapping("/list")
    public Result<List<InterfaceInfo>> listInterfaceInfo(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        if (interfaceInfoQueryRequest != null) {
            BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfoQuery);
        }
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>(interfaceInfoQuery);
        List<InterfaceInfo> interfaceInfoList = interfaceInfoService.list(queryWrapper);
        return Result.success(interfaceInfoList);
    }

    /**
     * 分页获取列表
     *
     * @return
     */
    @GetMapping("/list/page")
    public Result<Page<InterfaceInfo>> listInterfaceInfoByPage(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        Page<InterfaceInfo> interfaceInfoPage = interfaceInfoService.listInterfaceInfoByPage(interfaceInfoQueryRequest);
        return Result.success(interfaceInfoPage);
    }

    /**
     * 接口上线
     */
    @AuthCheck(mustRole = "admin")
    @PostMapping("/onLine")
    public Result<Boolean> interfaceOnLine(@RequestBody IdRequest idRequest,HttpServletRequest request){
        if(idRequest == null || idRequest.getId() < 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Boolean updateById = interfaceInfoService.interfaceOnLine(idRequest,request);
        return Result.success(updateById);
    }
    /**
     * 接口下线
     */
    @PostMapping("/offLine")
    @AuthCheck(mustRole = "admin")
    public Result<Boolean> interfaceOffLine(@RequestBody IdRequest idRequest){
        if(idRequest == null || idRequest.getId() < 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long id = idRequest.getId();
        //判断id是否存在
        InterfaceInfo byId = interfaceInfoService.getById(id);
        if(byId == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        //仅仅本人或者管理员可以进行修改
        InterfaceInfo interfaceInfo = new InterfaceInfo();
        interfaceInfo.setId(id);
        interfaceInfo.setStatus(0);
        boolean updateById = interfaceInfoService.updateById(interfaceInfo);
        return Result.success(updateById);
    }

    /**
     * 测试调用
     */
    @PostMapping("/invoke")
    public Result<String> invokeInterface(@RequestBody InterfaceInfoInvokeRequest interfaceInfoInvokeRequest, HttpServletRequest request){
        if(interfaceInfoInvokeRequest == null || interfaceInfoInvokeRequest.getId() < 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String returnString = interfaceInfoService.invokeInterface(interfaceInfoInvokeRequest,request);
        return Result.success(returnString);
    }


    /**
     * sdk下载
     *
     * @return
     */
    @GetMapping("/downloadSdk")
    public void downloadSdk(HttpServletResponse req) {
        interfaceInfoService.downloadSdk(req);
    }
}
