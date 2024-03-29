package com.yao.project.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yao.project.mapper.CityMapper;
import com.yao.yaoapiclientsdk.model.City;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.List;

@SpringBootTest
class UserInterfaceInfoServiceTest {
    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private CityMapper cityMapper;

    @Test
    void invokeCount() {
        boolean b = userInterfaceInfoService.invokeCount(1L, 1L);
        Assertions.assertTrue(b);
    }


    @Test
    public void cache(){
        QueryWrapper<City> cityQueryWrapper = new QueryWrapper<>();
        List<City> cities = cityMapper.selectList(cityQueryWrapper);
        for (City city :cities) {
            stringRedisTemplate.opsForValue().set("city"+city.getCityName(),city.getAdcode());
        }
    }
}
