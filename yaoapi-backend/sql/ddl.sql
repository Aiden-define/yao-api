-- 创建库
create database if not exists yaoapi;

-- 切换库
use yaoapi;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userName     varchar(256)                           null comment '用户昵称',
    userAccount  varchar(256)                           not null comment '账号',
    userAvatar   varchar(1024)                          null comment '用户头像',
    gender       tinyint                                null comment '性别',
    email        varchar(128)                           null comment '邮箱',
    `accessKey`    varchar(512)                         NULL COMMENT 'accessKey',
    `secretKey`    varchar(512)                         null COMMENT 'secretKey',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user / admin',
    userPassword varchar(512)                           not null comment '密码',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    constraint uni_userAccount
        unique (userAccount)
) comment '用户';


create table interface_info
(
    id             bigint                             not null AUTO_INCREMENT  comment '主键' primary key,
    name           varchar(256)                       null comment '名称',
    description    varchar(256)                       null comment '接口描述',
    url            varchar(512)                       null comment '接口地址',
    requestHeader  text                               null comment '请求头',
    responseHeader text                               null comment '响应头',
    status         int      default 0                 null comment '接口状态(0-关闭 1-开启）',
    method         varchar(256)                       null comment '请求类型',
    userId         bigint                             null comment '创建人',
    createTime     datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime     datetime default CURRENT_TIMESTAMP not null comment '更新时间',
    isDelete       int      default 0                 null comment '是否删除（0-未删  1-删除）'
)
    comment '接口信息';

create table user_interface_info
(
    id              bigint                             not null AUTO_INCREMENT COMMENT 'id'
        primary key,
    userId          bigint                             not null comment '用户主键',
    interfaceInfoId bigint                             not null comment '接口id',
    totalNum        bigint                             not null comment '接口总调用次数',
    leftNum         int                                not null comment '剩余调用次数',
    status          int      default 0                 not null comment '0-正常 1-禁止',
    createTime      datetime default CURRENT_TIMESTAMP not null,
    updateTime      datetime default CURRENT_TIMESTAMP not null,
    isDelete        int      default 0                 not null comment '是否删除（0-未删 1-删除）'
)
    comment '用户接口关系表';

