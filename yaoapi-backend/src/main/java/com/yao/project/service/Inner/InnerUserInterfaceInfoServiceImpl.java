package com.yao.project.service.Inner;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yao.common.model.entity.UserInterfaceInfo;
import com.yao.common.service.InnerUserInterfaceInfoService;
import com.yao.project.mapper.UserInterfaceInfoMapper;
import com.yao.project.service.UserInterfaceInfoService;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;

/**
 * @author DH
 * @version 1.0
 * @description 调用成功返回次数加1
 * @date 2023/5/14 21:24
 */
@DubboService
public class InnerUserInterfaceInfoServiceImpl implements InnerUserInterfaceInfoService {
    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;
    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;

    @Override
    public boolean invokeCount(long interfaceInfoId, long userId) {
        //直接调用实现类即可
        return userInterfaceInfoService.invokeCount(interfaceInfoId, userId);
    }

    @Override
    public boolean callTimes(long interfaceInfoId, long userId) {
        QueryWrapper<UserInterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("interfaceInfoId", interfaceInfoId).eq("userId", userId);
        UserInterfaceInfo userInterfaceInfo = userInterfaceInfoMapper.selectOne(queryWrapper);
        //返回false，由网关抛异常即可
        if (userInterfaceInfo == null) {
            return false;
        }
        Integer leftNum = userInterfaceInfo.getLeftNum();
        return leftNum > 0;
    }

}
