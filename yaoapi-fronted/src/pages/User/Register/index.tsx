import Footer from '@/components/Footer';
import {ArrowRightOutlined, LockOutlined, UserOutlined} from '@ant-design/icons';
import {LoginForm, ProFormText} from '@ant-design/pro-components';
import {Alert, message, Tabs} from 'antd';
import React, {useEffect, useState} from 'react';
import {history, Link} from '@@/exports';

import {randomStr} from '@antfu/utils';
import {Helmet} from "@umijs/max";
import Settings from "../../../../config/defaultSettings";
import {useEmotionCss} from '@ant-design/use-emotion-css';
import {getNumPicUsingGET, userRegisterUsingPOST} from "@/services/yaoapi-backend/userController";

const LoginMessage: React.FC<{
  content: string;
}> = ({content}) => {
  return (
    <Alert
      style={{
        marginBottom: 24,
      }}
      message={content}
      type="error"
      showIcon
    />
  );
};

const Register: React.FC = () => {
  const containerClassName = useEmotionCss(() => {
    return {
      display: 'flex',
      flexDirection: 'column',
      height: '100vh',
      overflow: 'auto',
      backgroundImage:
        "url('https://mdn.alipayobjects.com/yuyan_qk0oxh/afts/img/V-_oS6r-i7wAAAAAAAAAAAAAFl94AQBr')",
      backgroundSize: '100% 100%',
    };
  });
  const [imageUrl, setImageUrl] = useState<any>(null);

 /* React.useEffect( async () => {
    await getCaptcha();
    return () => {
      //return出来的函数本来就是更新前，销毁前执行的函数，现在不监听任何状态，所以只在销毁前执行
    };
  }, []); //第二个参数一定是一个空数组，因为如果不写会默认监听所有状态，这样写就不会监听任何状态，只在初始化时执行一次。*/
  useEffect(() => {
    getUrlNum();
  }, []);
  /**
   * 获取图形验证码
   */
  const getUrlNum = async () => {
    let randomString;
    const temp = localStorage.getItem('api-open-platform-randomString');
    if (temp) {
      randomString = temp;
    } else {
      randomString = randomStr(
        32,
        '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ',
      );
      localStorage.setItem('api-open-platform-randomString', randomString);
    }
    //携带浏览器请求标识
    //生成验证码图
    const res = await getNumPicUsingGET({
      headers: {
        signature: randomString,
      },
      responseType: 'blob', //必须指定为'blob'
    });
    let url = window.URL.createObjectURL(res);
    setImageUrl(url);
  };

  const handleSubmit = async (values: API.UserRegisterRequest) => {
    const {userPassword, checkPassword} = values;
    console.log(values)
    if (userPassword !== checkPassword) {
      message.error('两次输入密码不一致');
      return;
    }
    try {
      const signature = localStorage.getItem("api-open-platform-randomString")
      // 注册
      const res = await userRegisterUsingPOST(values, {
        headers: {
          "signature": signature
        },
      });
      if (res.code === 200) {
        const defaultLoginSuccessMessage = '注册成功！';
        message.success(defaultLoginSuccessMessage);
        /** 此方法会跳转到 redirect 参数所在的位置 */

        if (!history) return;
        history.push('/user/login');
        return;
      } else {
        message.error(res.description)
      }
    } catch (error: any) {
      console.log(error);
      message.error(error.message);
    }
  };
  const [userRegisterState] = useState<API.UserRegisterRequest>({});
  // @ts-ignore
  const {status, type: registerState} = userRegisterState;

  const [type, setType] = useState<string>('register');

  return (
    <div className={containerClassName}>
      <Helmet>
        <title>
          {'登录'}- {Settings.title}
        </title>
      </Helmet>
      <div
        style={{
          flex: '1',
          padding: '32px 0',
        }}
      >
        <LoginForm
          contentStyle={{
            minWidth: 280,
            maxWidth: '75vw',
          }}
          logo={<img alt="logo" src="/logo.svg"/>}
          title="Y API"
          subTitle={'好用丰富的API调用平台'}
          initialValues={{
            autoLogin: true,
          }}
          onFinish={async (values) => {
            console.log(values)
            await handleSubmit(values as API.UserRegisterRequest);
          }}
        >
          <Tabs
            activeKey={type}
            onChange={setType}
            centered
            items={[
              {
                key: 'register',
                label: '账号密码注册',
              },
            ]}
          />
          {status === 'error' && registerState === 'register' && (
            <LoginMessage content={'错误的用户名和密码'}/>
          )}
          {type === 'register' &&
            <>
              <ProFormText
                name="userAccount"
                fieldProps={{
                  size: 'large',
                  prefix: <UserOutlined/>,
                }}
                placeholder={'账号：至少4位且少于16位'}
                rules={[
                  {
                    required: true,
                    pattern: /^.{4,16}$/,
                    message: '账号至少4位且少于16位！',
                  },
                ]}
              />
              <ProFormText.Password
                name="userPassword"
                fieldProps={{
                  size: 'large',
                  prefix: <LockOutlined/>,
                }}
                placeholder={'密码: 至少8位且少于16位'}
                rules={[
                  {
                    required: true,
                    pattern: /^.{8,16}$/,
                    message: '密码至少8位且少于16位',
                  },
                ]}
              />
              <ProFormText.Password
                name="checkPassword"
                fieldProps={{
                  size: 'large',
                  prefix: <LockOutlined/>,
                }}
                placeholder={'确认密码'}
                rules={[
                  {
                    required: true,
                    pattern: /^.{8,16}$/,
                    message: '两次密码必须一致！',
                  },
                ]}
              />
              <div style={{display: 'flex'}}>
                <ProFormText
                  fieldProps={{
                    autoComplete: "off",
                    size: 'large',
                    prefix: <ArrowRightOutlined className={'prefixIcon'}/>,
                  }}
                  name="urlNum"
                  placeholder={'请输入右侧验证码'}
                  rules={[
                    {
                      required: true,
                      message: '请输入图形验证码！',
                    },
                    {
                      pattern: /^[a-z0-9A-Z]+$/,
                      message: '验证码格式错误！',
                    },
                  ]}
                />
                <img
                  src={imageUrl}
                  onClick={getUrlNum}
                  style={{marginLeft: 18}}
                  width="100px"
                  height="39px"
                />
              </div>
            </>
          }
          <div
            style={{
              marginBottom: 24,
            }}
          >
            <Link
              style={{
                marginBottom: 24,
                float: 'right'
              }}
              to={'/user/login'}
            >
              已有帐号，去登陆！
            </Link>
          </div>

        </LoginForm>
      </div>
      <Footer/>
    </div>
  );
};
export default Register;
