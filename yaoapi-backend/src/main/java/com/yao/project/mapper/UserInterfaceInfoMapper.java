package com.yao.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yao.common.model.entity.InterfaceInfo;
import com.yao.common.model.entity.UserInterfaceInfo;

import java.util.List;

/**
* @author DH
* @description 针对表【user_interface_info(用户接口关系表)】的数据库操作Mapper
* @createDate 2023-05-08 19:42:49
* @Entity com.yao.project.model.entity.UserInterfaceInfo
*/
public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {
    //查询调用次数前3名的接口
    List<UserInterfaceInfo> orderByTimesHasLimit(int n);

}




