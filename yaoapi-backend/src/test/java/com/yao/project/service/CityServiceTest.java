package com.yao.project.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yao.project.mapper.CityMapper;
import com.yao.yaoapiclientsdk.model.City;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * @author DH
 * @version 1.0
 * @description 缓存城市信息
 * @date 2023/8/15 21:04
 */
@SpringBootTest
public class CityServiceTest {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private CityMapper cityMapper;
    @Test
    public void cache(){
        /*Set<String> city = stringRedisTemplate.keys("city"+"*");
        stringRedisTemplate.delete(city);*/
        QueryWrapper<City> cityQueryWrapper = new QueryWrapper<>();
        List<City> cities = cityMapper.selectList(cityQueryWrapper);
        for (City city :cities) {
            stringRedisTemplate.opsForValue().set("city:cache:"+city.getCityName(),city.getAdcode());
        }
    }
}
