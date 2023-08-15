package com.yao.project.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.project.mapper.CityMapper;
import com.yao.project.service.CityService;
import com.yao.yaoapiclientsdk.model.City;
import org.springframework.stereotype.Service;

/**
* @author DH
* @description 针对表【city】的数据库操作Service实现
* @createDate 2023-08-15 21:19:58
*/
@Service
public class CityServiceImpl extends ServiceImpl<CityMapper, City>
    implements CityService {

}




