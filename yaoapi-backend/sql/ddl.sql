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
    userRole     varchar(256) default 'user'            not null comment '用户角色：user / admin',
    userPassword varchar(512)                           not null comment '密码',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    constraint uni_userAccount
        unique (userAccount)
) comment '用户';

/*-- 帖子表
create table if not exists InterfaceInfo
(
    id            bigint auto_increment comment 'id' primary key,
    age           int comment '年龄',
    gender        tinyint  default 0                 not null comment '性别（0-男, 1-女）',
    education     varchar(512)                       null comment '学历',
    place         varchar(512)                       null comment '地点',
    job           varchar(512)                       null comment '职业',
    contact       varchar(512)                       null comment '联系方式',
    loveExp       varchar(512)                       null comment '感情经历',
    content       text                               null comment '内容（个人介绍）',
    photo         varchar(1024)                      null comment '照片地址',
    reviewStatus  int      default 0                 not null comment '状态（0-待审核, 1-通过, 2-拒绝）',
    reviewMessage varchar(512)                       null comment '审核信息',
    viewNum       int                                not null default 0 comment '浏览数',
    thumbNum      int                                not null default 0 comment '点赞数',
    userId        bigint                             not null comment '创建用户 id',
    createTime    datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime    datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete      tinyint  default 0                 not null comment '是否删除'
) comment '帖子';*/

-- auto-generated definition
create table interface_info
(
    id             bigint                             not null comment '主键' primary key,
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
    id              bigint                             not null
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
