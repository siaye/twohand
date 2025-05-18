import { createStore } from 'vuex';

const store = createStore({
  state() {
    return {
      user: null, // 存储用户信息
      isLoggedIn: false // 存储登录状态
    };
  },
  mutations: {
    // 设置用户登录状态和信息
    setLoginStatus(state, { user, isLoggedIn }) {
      state.user = user;
      state.isLoggedIn = isLoggedIn;
    },
    // 清除用户登录状态和信息
    clearLoginStatus(state) {
      state.user = null;
      state.isLoggedIn = false;
    }
  },
  actions: {
    // 异步登录操作示例
    // async login({ commit }, credentials) {
    //   try {
    //     // 调用登录 API
    //     // const response = await api.login(credentials);
    //     // commit('setLoginStatus', { user: response.data.user, isLoggedIn: true });
    //   } catch (error) {
    //     // 处理登录失败
    //     // console.error('Login failed:', error);
    //     // commit('clearLoginStatus');
    //     // throw error; // 或者其他错误处理
    //   }
    // },
    // 异步退出登录操作示例
    // logout({ commit }) {
    //   // 调用退出登录 API 或清除本地存储
    //   // api.logout();
    //   // 清除 token 等
    //   commit('clearLoginStatus');
    // }
  },
  getters: {
    // 获取登录状态
    isLoggedIn(state) {
      return state.isLoggedIn;
    },
    // 获取用户名
    username(state) {
      return state.user ? state.user.username : null;
    },
    // 获取完整用户信息
    user(state) {
      return state.user;
    }
  }
});

export default store; 