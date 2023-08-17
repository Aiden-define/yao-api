package com.yao.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.yao.common.model.entity.InterfaceInfo;
import com.yao.common.model.entity.User;
import com.yao.common.model.entity.UserInterfaceInfo;
import com.yao.project.common.ErrorCode;
import com.yao.project.exception.BusinessException;
import com.yao.project.mapper.InterfaceInfoMapper;
import com.yao.project.mapper.UserInterfaceInfoMapper;
import com.yao.project.model.dto.userInterfaceInfo.UserInterfaceInfoAddRequest;
import com.yao.project.model.vo.InterfaceInfoVo;
import com.yao.project.service.UserInterfaceInfoService;
import com.yao.project.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.yao.project.constant.UserConstant.USER_INTERFACE;

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

    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /*数据校验*/
    @Override
    public void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo) {
        if (userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Long interfaceInfoId = userInterfaceInfo.getInterfaceInfoId();
        if (interfaceInfoId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

    }

    /**
     * 调用接口成功后调用次数+1 剩余次数减1
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
     *
     * @return
     */

    @Override
    public List<InterfaceInfoVo> orderByTimesHasLimit() {
        List<UserInterfaceInfo> interfaceInfoList = userInterfaceInfoMapper.orderByTimesHasLimit(3);
        //根据id分组
        Map<Long, List<UserInterfaceInfo>> listMap = interfaceInfoList.stream().collect(Collectors.groupingBy(UserInterfaceInfo::getInterfaceInfoId));
        QueryWrapper<InterfaceInfo> interfaceInfoQueryWrapper = new QueryWrapper<>();
        interfaceInfoQueryWrapper.in("id", listMap.keySet());
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

    @Override
    public Boolean addUserInterfaceInfo(UserInterfaceInfoAddRequest userInterfaceInfoAddRequest, HttpServletRequest request) {
        UserInterfaceInfo userInterfaceInfo = new UserInterfaceInfo();
        BeanUtils.copyProperties(userInterfaceInfoAddRequest, userInterfaceInfo);
        Long interfaceInfoId = userInterfaceInfo.getInterfaceInfoId();
        // 校验
        User loginUser = userService.getLoginUser(request);
        userInterfaceInfo.setUserId(loginUser.getId());
        Integer leftNum = userInterfaceInfo.getLeftNum();
        if (leftNum >= 500) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用次数暂时充足，请用完再获取");
        }
        this.validUserInterfaceInfo(userInterfaceInfo);
        String key = USER_INTERFACE + loginUser.getId()+":"+interfaceInfoId;
        if(StringUtils.isBlank(stringRedisTemplate.opsForValue().get(key))){
        //判断该用户在接口用户表中是否有过记录
            //初始给10次调用接口机会
            userInterfaceInfo.setTotalNum(0L);
            userInterfaceInfo.setLeftNum(10);
            boolean save = this.save(userInterfaceInfo);
            String json = new Gson().toJson(interfaceInfoId);
            //首次存入Redis，便于之后的判断
            stringRedisTemplate.opsForValue().set(key,json);
            return save;
        }
        //有，接口次数在原来基础上加10
        UpdateWrapper<UserInterfaceInfo> infoUpdateWrapper = new UpdateWrapper<>();
        infoUpdateWrapper.eq("userId", loginUser.getId())
                .eq("interfaceInfoId", userInterfaceInfo.getInterfaceInfoId())
                .setSql("leftNum = leftNum+10");
        return this.update(infoUpdateWrapper);
    }
}





