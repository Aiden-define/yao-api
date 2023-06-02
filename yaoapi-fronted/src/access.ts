import {loginUser} from "@/services/swagger/user";

/**
 * @see https://umijs.org/zh-CN/plugins/plugin-access
 * */
export default function access(initialState: { currentUser?: API.CurrentUser } | undefined) {
  const { currentUser } = initialState ?? {};
  return {
    canUser: loginUser,
    //canAdmin: currentUser && currentUser.access === 'admin',
    canAdmin: true
  };
}
