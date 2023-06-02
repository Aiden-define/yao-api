package com.yao.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.common.model.entity.InterfaceInfo;
import com.yao.common.model.entity.UserInterfaceInfo;
import com.yao.project.common.ErrorCode;
import com.yao.project.exception.BusinessException;
import com.yao.project.mapper.InterfaceInfoMapper;
import com.yao.project.mapper.UserInterfaceInfoMapper;
import com.yao.project.model.vo.InterfaceInfoVo;
import com.yao.project.service.UserInterfaceInfoService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author DH
 * @description 针对表【user_interface_info(用户接口关系表)】的数据库操作Service实现
 * @createDate 2023-05-08 19:42:49
 */
@Service
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
        implements UserInterfaceInfoService {
    @Resource
    private UserInterfaceInfoMapper userInterfaceInfoMapper;

    @Resource
    private InterfaceInfoMapper interfaceInfoMapper;
    /*数据校验*/
    @Override
    public void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add) {
        if (userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long interfaceInfoId = userInterfaceInfo.getInterfaceInfoId();
        Long userId = userInterfaceInfo.getUserId();
        // 创建时，所有参数必须非空
        if (add) {
            if (interfaceInfoId <= 0 || userId <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }
    }

    /**
     * 调用接口成功后调用次数+1
     *
     * @param interfaceInfoId 接口id
     * @param userId          用户id
     * @return 成功
     */
    @Override
    public boolean invokeCount(long interfaceInfoId, long userId) {
        if (interfaceInfoId <= 0 || userId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        UpdateWrapper<UserInterfaceInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("interfaceInfoId", interfaceInfoId)
                .eq("userId", userId)
                .setSql("totalNum = totalNum + 1, leftNum = leftNum - 1");
        int update = userInterfaceInfoMapper.update(null, updateWrapper);
        if (update < 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR);
        }
        return true;
    }

    /**
     * 获取调用次数前3位的接口数据前端用ECharts展示
     * @return
     */

    @Override
    public List<InterfaceInfoVo> orderByTimesHasLimit() {
        List<UserInterfaceInfo> interfaceInfoList = userInterfaceInfoMapper.orderByTimesHasLimit(3);
        //根据id分组
        Map<Long, List<UserInterfaceInfo>> listMap = interfaceInfoList.stream().collect(Collectors.groupingBy(UserInterfaceInfo::getInterfaceInfoId));
        QueryWrapper<InterfaceInfo> interfaceInfoQueryWrapper = new QueryWrapper<>();
        interfaceInfoQueryWrapper.in("id",listMap.keySet());
        //根据id查接口名称
        List<InterfaceInfo> interfaceInfos = interfaceInfoMapper.selectList(interfaceInfoQueryWrapper);
        //通过map方法interfaceInfos赋给interfaceInfoVo并添加上total返回
        return interfaceInfos.stream().map(interfaceInfo -> {
            InterfaceInfoVo interfaceInfoVo = new InterfaceInfoVo();
            BeanUtils.copyProperties(interfaceInfo, interfaceInfoVo);
            int total = listMap.get(interfaceInfo.getId()).get(0).getTotal();
            interfaceInfoVo.setTotal(total);
            return interfaceInfoVo;
        }).collect(Collectors.toList());
    }
}





