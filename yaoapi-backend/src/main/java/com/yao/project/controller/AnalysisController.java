package com.yao.project.controller;

import com.yao.common.commonUtils.Result;
import com.yao.project.annotation.AuthCheck;
import com.yao.project.model.vo.InterfaceInfoVo;
import com.yao.project.service.UserInterfaceInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author DH
 * @version 1.0
 * @description 分析控制器
 * @date 2023/5/25 15:32
 */
@RestController
@RequestMapping("/analysis")
@Slf4j
public class AnalysisController {
    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;
    @GetMapping("/top/interface/invoke")
    @AuthCheck(mustRole = "admin")
    public Result<List<InterfaceInfoVo>> orderByTimesHasLimit(){
        List<InterfaceInfoVo> interfaceInfoVos = userInterfaceInfoService.orderByTimesHasLimit();
        return Result.success(interfaceInfoVos);
    }

}
