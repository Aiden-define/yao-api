import Footer from '@/components/Footer';
import {Question} from '@/components/RightContent';
import {LinkOutlined} from '@ant-design/icons';
import {SettingDrawer} from '@ant-design/pro-components';
import type {RunTimeLayoutConfig} from '@umijs/max';
import {history, Link} from '@umijs/max';
import {requestConfig} from './requestConfig';
import React from 'react';
import {AvatarDropdown, AvatarName} from './components/RightContent/AvatarDropdown';
import {getLoginUserUsingGET} from '@/services/yaoapi-backend/userController';
import Settings from "../config/defaultSettings";

const isDev = process.env.NODE_ENV === 'development';
const loginPath = '/user/login';

/**
 * @see  https://umijs.org/zh-CN/plugins/plugin-initial-state
 * */
export async function getInitialState(): Promise<InitialState> {
  //当页面首次加载时，获取要全局保存的数据，比如登录信息
  const state: InitialState = {
    loginUser: undefined,
  };
  try {
    const res = await getLoginUserUsingGET();
    console.log(res);
    if (res.code === 200) {
      state.loginUser = res.data;
    }
  } catch (error) {
    history.push(loginPath);
  }
  return state;
}

// 页面初始化的头像，水印等
export const layout: RunTimeLayoutConfig = ({initialState, setInitialState}) => {
  return {
    actionsRender: () => [<Question key="doc"/>],
    avatarProps: {
      src: initialState?.loginUser?.userAvatar,
      title: <AvatarName/>,
      render: (_, avatarChildren) => {
        return <AvatarDropdown>{avatarChildren}</AvatarDropdown>;
      },
    },
    waterMarkProps: {
      content: initialState?.loginUser?.userName,
    },
    footerRender: () => <Footer/>,
    onPageChange: () => {
      const {location} = history;
      // 如果没有登录，重定向到 login
      if (!initialState?.loginUser && location.pathname !== loginPath) {
        history.push(loginPath);
      }
    },
    layoutBgImgList: [
      {
        src: 'https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/D2LWSqNny4sAAAAAAAAAAAAAFl94AQBr',
        left: 85,
        bottom: 100,
        height: '303px',
      },
      {
        src: 'https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/C2TWRpJpiC0AAAAAAAAAAAAAFl94AQBr',
        bottom: -68,
        right: -45,
        height: '303px',
      },
      {
        src: 'https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/F6vSTbj8KpYAAAAAAAAAAAAAFl94AQBr',
        bottom: 0,
        left: 0,
        width: '331px',
      },
    ],
    links: isDev
      ? [
        <Link key="openapi" to="/umi/plugin/openapi" target="_blank">
          <LinkOutlined/>
          <span>OpenAPI 文档</span>
        </Link>,
      ]
      : [],
    menuHeaderRender: undefined,
    // 自定义 403 页面
    // unAccessible: <div>unAccessible</div>,
    // 增加一个 loading 的状态
    childrenRender: (children) => {
      // if (initialState?.loading) return <PageLoading />;

      return (
        <>
          {children}
          <SettingDrawer
            disableUrlParams
            enableDarkTheme
            // @ts-ignore
            settings={Settings}
            onSettingChange={(settings) => {
              setInitialState((preInitialState) => ({
                ...preInitialState,
                settings,
              }));
            }}
          />
        </>
      );
    },
    // @ts-ignore
    ...initialState?.settings,
  };
};

/**
 * @name request 配置，可以配置错误处理
 * 它基于 axios 和 ahooks 的 useRequest 提供了一套统一的网络请求和错误处理方案。
 * @doc https://umijs.org/docs/max/request#配置
 */
export const request = {
  ...requestConfig,
};
/**
 * 让前端请求都带上拦截器
 * @param url
 * @param options
 */
