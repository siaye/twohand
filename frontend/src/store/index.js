import { createStore } from 'vuex';
import { login, getUserInfo } from '@/api/user';

// 验证 token 是否过期
const isTokenExpired = (token) => {
  if (!token) return true;
  try {
    // 移除 Bearer 前缀
    const tokenWithoutBearer = token.replace(/^Bearer\s+/i, '');
    
    // 解析 JWT token
    const base64Url = tokenWithoutBearer.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(c => {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2);
    }).join(''));

    const { exp } = JSON.parse(jsonPayload);
    const currentTime = Math.floor(Date.now() / 1000);
    
    console.log('【Store】Token 验证:', {
      token: tokenWithoutBearer,
      expiration: new Date(exp * 1000),
      currentTime: new Date(currentTime * 1000),
      isValid: exp > currentTime
    });
    
    return exp <= currentTime;
  } catch (error) {
    console.error('【Store】Token 解析失败:', error);
    return true;
  }
};

const store = createStore({
  state: {
    token: '',
    user: null
  },
  mutations: {
    SET_TOKEN(state, token) {
      // 确保 token 格式正确，移除所有空格和 Bearer 前缀
      const cleanToken = token.replace(/^Bearer\s+/i, '').trim()
      const authToken = `Bearer ${cleanToken}`
      
      state.token = authToken
      localStorage.setItem('token', authToken)
      
      console.log('【Store】设置 token:', {
        originalToken: token,
        cleanToken,
        authToken,
        stateToken: state.token,
        localStorageToken: localStorage.getItem('token')
      })
    },
    SET_USER(state, user) {
      // 确保 role 是数字类型
      if (user && typeof user.role !== 'undefined') {
        user.role = Number(user.role);
      }
      
      state.user = user;
      localStorage.setItem('userInfo', JSON.stringify(user));
      
      console.log('【Store】设置用户信息:', {
        user,
        role: user?.role,
        roleType: typeof user?.role,
        stateUser: state.user,
        localStorageUser: JSON.parse(localStorage.getItem('userInfo') || 'null')
      });
    },
    CLEAR_USER(state) {
      state.token = '';
      state.user = null;
      localStorage.removeItem('token');
      localStorage.removeItem('userInfo');
      console.log('【Store】清除用户信息:', {
        stateToken: state.token,
        stateUser: state.user,
        localStorageToken: localStorage.getItem('token'),
        localStorageUser: localStorage.getItem('userInfo')
      });
    },
    // 更新用户头像
    UPDATE_USER_AVATAR(state, avatar) {
      if (state.user) {
        state.user.avatar = avatar
        // 同时更新 localStorage 中的用户信息
        const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
        userInfo.avatar = avatar
        localStorage.setItem('userInfo', JSON.stringify(userInfo))
      }
    },
    // 更新用户信息
    UPDATE_USER_INFO(state, userInfo) {
      if (state.user) {
        state.user = { ...state.user, ...userInfo }
        // 同时更新 localStorage 中的用户信息
        const storedUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
        localStorage.setItem('userInfo', JSON.stringify({ ...storedUserInfo, ...userInfo }))
      }
    }
  },
  actions: {
    // 初始化 store
    initStore({ commit, dispatch }) {
      console.log('【Store】初始化 store');
      const token = localStorage.getItem('token');
      const userInfo = localStorage.getItem('userInfo');
      
      // 验证 token 有效性
      if (token && !isTokenExpired(token)) {
        console.log('【Store】发现有效 token，恢复登录状态');
        commit('SET_TOKEN', token);
        if (userInfo) {
          try {
            const user = JSON.parse(userInfo);
            commit('SET_USER', user);
          } catch (error) {
            console.error('【Store】解析用户信息失败:', error);
            commit('CLEAR_USER');
          }
        }
      } else {
        console.log('【Store】未发现有效 token，清除登录状态');
        commit('CLEAR_USER');
      }
    },
    
    // 登录
    async login({ commit }, loginData) {
      try {
        console.log('【Store】开始登录:', loginData);
        const response = await login(loginData);
        console.log('【Store】登录响应完整数据:', {
          code: response.code,
          message: response.message,
          data: response.data,
          role: response.data?.role,
          roleType: typeof response.data?.role,
          rawResponse: JSON.stringify(response, null, 2)
        });
        
        if (response && response.code === 200 && response.data) {
          // 先清除旧的状态
          commit('CLEAR_USER');
          
          // 设置 token
          const token = response.data.token;
          console.log('【Store】准备设置 token:', token);
          commit('SET_TOKEN', token);
          
          // 设置用户信息
          const userInfo = {
            id: response.data.id,
            username: response.data.username,
            role: Number(response.data.role), // 确保 role 是数字类型
            userType: Number(response.data.userType),
            realName: response.data.realName,
            avatar: response.data.avatar,
            phone: response.data.phone,
            email: response.data.email
          };
          
          console.log('【Store】准备设置用户信息:', {
            userInfo,
            role: userInfo.role,
            roleType: typeof userInfo.role,
            originalRole: response.data.role,
            originalRoleType: typeof response.data.role,
            rawUserInfo: JSON.stringify(userInfo, null, 2)
          });
          
          commit('SET_USER', userInfo);
          
          // 验证用户信息是否正确保存
          const currentUser = JSON.parse(localStorage.getItem('userInfo') || 'null');
          console.log('【Store】验证用户信息保存:', {
            currentUser,
            role: currentUser?.role,
            roleType: typeof currentUser?.role,
            isAdmin: currentUser?.role === 2,
            rawCurrentUser: JSON.stringify(currentUser, null, 2)
          });
          
          if (!currentUser) {
            console.error('【Store】用户信息未保存到 localStorage');
            throw new Error('用户信息保存失败：localStorage 为空');
          }
          
          if (typeof currentUser.role === 'undefined') {
            console.error('【Store】用户信息中缺少 role 字段');
            throw new Error('用户信息保存失败：缺少 role 字段');
          }
          
          // 返回完整的响应数据
          return response;
        } else {
          console.error('【Store】登录响应格式错误:', {
            hasResponse: !!response,
            code: response?.code,
            hasData: !!response?.data,
            message: response?.message
          });
          throw new Error(response?.message || '登录响应格式错误');
        }
      } catch (error) {
        console.error('【Store】登录失败:', error);
        commit('CLEAR_USER');
        throw error;
      }
    },
    
    // 获取用户信息
    async getUserInfo({ commit, state }, identifier) {
      try {
        console.log('【Store】获取用户信息:', identifier);
        const user = await getUserInfo(identifier);
        console.log('【Store】用户信息响应:', user);
        
        if (user) {
          commit('SET_USER', user);
          return user;
        } else {
          throw new Error('获取用户信息失败');
        }
      } catch (error) {
        console.error('【Store】获取用户信息失败:', error);
        throw error;
      }
    },
    
    // 登出
    logout({ commit }) {
      console.log('【Store】执行登出操作');
      commit('CLEAR_USER');
    }
  },
  getters: {
    isLoggedIn: state => {
      const hasToken = !!state.token && !isTokenExpired(state.token);
      console.log('【Store】检查登录状态:', { 
        hasToken, 
        token: state.token,
        user: state.user,
        isTokenExpired: isTokenExpired(state.token)
      });
      return hasToken;
    },
    user: state => {
      console.log('【Store】获取用户信息:', {
        stateUser: state.user,
        localStorageUser: JSON.parse(localStorage.getItem('userInfo') || 'null')
      });
      return state.user;
    },
    isAdmin: state => {
      const role = Number(state.user?.role);
      const isAdmin = role === 2;
      
      console.log('【Store】检查管理员权限:', {
        user: state.user,
        role,
        roleType: typeof role,
        isAdmin,
        localStorageUser: JSON.parse(localStorage.getItem('userInfo') || 'null')
      });
      
      return isAdmin;
    },
    token: state => state.token
  }
});

// 初始化 store
store.dispatch('initStore');

export default store; 