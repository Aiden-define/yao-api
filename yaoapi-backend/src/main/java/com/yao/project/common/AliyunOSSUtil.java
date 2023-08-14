package com.yao.project.common;

import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.PutObjectResult;
import com.yao.project.constant.OssConstant;
import com.yao.project.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author DH
 * @version 1.0
 * @description OSS服务配置文件
 * @date 2023/8/8 19:11
 */
public class AliyunOSSUtil {
    private static final String accessKeyId= OssConstant.OSS_ACCESS_KEY_ID_IM;
    private static final String accessKeySecret=OssConstant.OSS_ACCESS_KEY_SECRET_IM;
    private static final String endpoint =  OssConstant.OSS_END_POINT_IM;
    private static final String bucket = OssConstant.OSS_BUCKET_IM;

    private static final Logger logger = LoggerFactory.getLogger(AliyunOSSUtil.class);

    private static final List<String> TYPE_OF_FILE = Arrays.asList("image/jpg", "image/png", "image/gif");
    private static final int MAX_PIC = 2 * 1024 * 1024;

    public static String OSSUploadFile(MultipartFile file) {
        if(!validate(file)){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        OSSClient ossClient = new OSSClient(endpoint,accessKeyId,accessKeySecret);
        try {
            InputStream fileInputStream = file.getInputStream();
            String originalFilename = file.getOriginalFilename();
            String dateStr = format.format(new Date());
            String fileName = dateStr + "/" + new Date().getTime()+"-"+originalFilename;
            //调用oss方法实现上传
            //第一个参数  Bucket名称
            //第二个参数  上传到oss文件路径和文件名称
            //第三个参数  上传文件输入流
            PutObjectResult result = ossClient.putObject(bucket, fileName, fileInputStream);
            String url = "https://"+bucket+"."+endpoint+"/"+fileName;
            if(null != result){
                logger.info("==========>OSS文件上传成功,OSS地址："+url);
                return url;
            }
        }catch (Exception oe){
            logger.error(oe.getMessage());
        }finally {
            //关闭
            ossClient.shutdown();
        }
        return null;
    }
    public static boolean validate(MultipartFile file) {
        if (file.isEmpty()) {
            return false;
        }

        if (file.getSize() > MAX_PIC) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"图片不能大于2MB");
        }
        if (!TYPE_OF_FILE.contains(file.getContentType())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"只支持图片格式");
        }
        return true;
    }
}

