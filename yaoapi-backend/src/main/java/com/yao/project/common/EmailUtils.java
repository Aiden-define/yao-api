package com.yao.project.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author DH
 * @version 1.0
 * @description 发送验证码
 * @date 2023/8/13 14:44
 */
@Component
public class EmailUtils {
    @Value("${spring.mail.username}")
    private String username;
    @Resource
    private MailSender sender;

    public boolean sendMail(String email,String code){
        //构建标准的简单邮件信息
        //发送人和xml保持一致
        SimpleMailMessage mailMessage=new SimpleMailMessage();
        //发送人
        mailMessage.setFrom(username);
        //接收人
        mailMessage.setTo(email);
        //邮件标题
        mailMessage.setSubject("登录验证");
        //内容
        String message = "你正在登录Y API接口调用平台，你的验证码为："+code+",2分钟内有效，若非本人请忽略！";
        mailMessage.setText(message);
        sender.send(mailMessage);//发送邮件
        System.out.println("发送成功！");
        return true;
    }

}
