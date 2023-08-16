package com.yao.project.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.yao.common.model.entity.UserInterfaceInfo;
import com.yao.project.model.dto.userInterfaceInfo.UserInterfaceInfoAddRequest;
import com.yao.project.model.vo.InterfaceInfoVo;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author DH
 * @description 针对表【user_interface_info(用户接口关系表)】的数据库操作Service
 * @createDate 2023-05-08 19:42:49
 */
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {
    /*数据校验*/
    void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo);

    //调用成功调用次数+1
    boolean invokeCount(long interfaceInfoId, long userId);

    //查询调用次数前n名的接口
    List<InterfaceInfoVo> orderByTimesHasLimit();

    /**
     * 获取接口调用次数
     * @param userInterfaceInfoAddRequest
     * @param request
     * @return
     */
    Boolean addUserInterfaceInfo(UserInterfaceInfoAddRequest userInterfaceInfoAddRequest, HttpServletRequest request);

}
