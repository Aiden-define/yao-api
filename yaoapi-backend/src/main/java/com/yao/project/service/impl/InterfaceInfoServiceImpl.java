package com.yao.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yao.common.model.entity.InterfaceInfo;
import com.yao.project.common.ErrorCode;
import com.yao.project.constant.CommonConstant;
import com.yao.project.exception.BusinessException;
import com.yao.project.mapper.InterfaceInfoMapper;
import com.yao.project.model.dto.interfaceinfo.InterfaceInfoQueryRequest;
import com.yao.project.service.InterfaceInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author DH
 * @description 针对表【interface_info(接口信息)】的数据库操作Service实现
 * @createDate 2023-04-25 19:55:32
 */
@Service
public class InterfaceInfoServiceImpl extends ServiceImpl<InterfaceInfoMapper, InterfaceInfo>
        implements InterfaceInfoService {
    @Resource
    private InterfaceInfoMapper interfaceInfoMapper;
    //数据校验
    @Override
    public void validInterfaceInfo(InterfaceInfo interfaceInfo, boolean add) {
        if (interfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String name = interfaceInfo.getName();
        String description = interfaceInfo.getDescription();
        String url = interfaceInfo.getUrl();
        String requestHeader = interfaceInfo.getRequestHeader();
        String responseHeader = interfaceInfo.getResponseHeader();
        String method = interfaceInfo.getMethod();
        // 创建时，所有参数必须非空
        if (add) {
            if (StringUtils.isAnyBlank(name, description, url, requestHeader, responseHeader, method)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
        }
        if (StringUtils.isNotBlank(name) && name.length() > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
        }
    }

    @Override
    public void downloadSdk(HttpServletResponse res) {
        ClassPathResource pathResource = new org.springframework.core.io.ClassPathResource("yaoapi-client-sdk-0.0.1.jar");
        InputStream inputStream = null;
        if(!pathResource.exists()){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"文件不存在");
        }
        try {
            inputStream = pathResource.getInputStream();
            // 设置响应头
            res.setContentType("application/octet-stream");
            res.setHeader("Content-Disposition", "attachment; filename=yaoapi-client-sdk-0.0.1.jar");
            ServletOutputStream outputStream = res.getOutputStream();
            byte[] bytes = new byte[1024]; //1mb方式读取
            int length = 0;
            while ((length = inputStream.read(bytes)) > 0) {
                outputStream.write(bytes, 0, length);
            }
            outputStream.flush();
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Page<InterfaceInfo> listInterfaceInfoByPage(InterfaceInfoQueryRequest interfaceInfoQueryRequest) {
        if (interfaceInfoQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        InterfaceInfo interfaceInfoQuery = new InterfaceInfo();
        BeanUtils.copyProperties(interfaceInfoQueryRequest, interfaceInfoQuery);
        long current = interfaceInfoQueryRequest.getCurrent();
        long size = interfaceInfoQueryRequest.getPageSize();
        String sortField = interfaceInfoQueryRequest.getSortField();
        String sortOrder = interfaceInfoQueryRequest.getSortOrder();
        // 限制爬虫
        if (size > 50) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"页面一次最多显示50条");
        }
        QueryWrapper<InterfaceInfo> queryWrapper = this.addCondition(interfaceInfoQuery);
        queryWrapper.orderBy(StringUtils.isNotBlank(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return this.page(new Page<>(current, size), queryWrapper);
    }

    @Override
    public QueryWrapper<InterfaceInfo> addCondition(InterfaceInfo interfaceInfoQuery) {
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        String name = interfaceInfoQuery.getName();
        if(StringUtils.isNotBlank(name)){
            queryWrapper.like("name",name);
        }
        String description = interfaceInfoQuery.getDescription();
        if(StringUtils.isNotBlank(description)){
            queryWrapper.like("description",description);
        }
        String requestHeader = interfaceInfoQuery.getRequestHeader();
        if(StringUtils.isNotBlank(requestHeader)){
            queryWrapper.like("requestHeader",requestHeader);
        }
        String requestParams = interfaceInfoQuery.getRequestParams();
        if(StringUtils.isNotBlank(requestParams)){
            queryWrapper.like("requestParams",requestParams);
        }
        String responseHeader = interfaceInfoQuery.getResponseHeader();
        if(StringUtils.isNotBlank(responseHeader)){
            queryWrapper.like("responseHeader",responseHeader);
        }
        String method = interfaceInfoQuery.getMethod();
        if(StringUtils.isNotBlank(method)){
            queryWrapper.eq("method",method);
        }
        String url = interfaceInfoQuery.getUrl();
        if(StringUtils.isNotBlank(url)){
            queryWrapper.like("url",url);
        }
        Integer status = interfaceInfoQuery.getStatus();
        if(status!=null&&status>=0&&status<=1 ){
            queryWrapper.eq("status",status);
        }
        return queryWrapper;

    }

}




