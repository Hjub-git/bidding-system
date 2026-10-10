import axios from 'axios';

// 创建axios实例
const api = axios.create({
  // 走 dev server 代理（vue.config.js 把 /api 转发到 8080 后端）
  baseURL: '/api',
  timeout: 10000
});

// 请求拦截器
api.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// 响应拦截器：后端统一返回 { code, message, data }，这里直接解包 data
api.interceptors.response.use(
  response => {
    const body = response.data;
    if (body && typeof body === 'object' && Object.prototype.hasOwnProperty.call(body, 'data')) {
      return body.data;
    }
    return body;
  },
  error => {
    const status = error.response && error.response.status;
    const url = error.config && error.config.url;
    // 登录接口返回 401 表示“用户名或密码错误”，不能因此跳转登录页
    if (status === 401 && url && url.indexOf('/auth/login') === -1) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

// 用户相关API
export const userApi = {
  // 用户登录
  login(data) {
    return api.post('/auth/login', data);
  },
  
  // 用户注册
  register(data) {
    return api.post('/auth/register', data);
  },
  
  // 获取用户信息
  getUserInfo() {
    return api.get('/user/info');
  },
  
  // 更新用户信息
  updateUserInfo(data) {
    return api.put('/user/info', data);
  }
};

// 招标项目相关API
export const tenderApi = {
  // 获取招标项目列表
  getTenders(params) {
    return api.get('/tenders', { params });
  },
  // 获取当前登录招标单位的招标项目
  getMyTenders(params) {
    // 使用不冲突的新端点，避免被 /tenders/:id 误匹配
    return api.get('/my-tenders', { params });
  },
  
  // 获取招标项目详情
  getTenderDetail(id) {
    return api.get(`/tenders/${id}`);
  },
  
  // 创建招标项目
  createTender(data) {
    return api.post('/tenders', data);
  },
  
  // 更新招标项目
  updateTender(id, data) {
    return api.put(`/tenders/${id}`, data);
  },
  
  // 删除招标项目
  deleteTender(id) {
    return api.delete(`/tenders/${id}`);
  }
};

// 投标相关API
export const bidApi = {
  // 获取投标列表
  getBids(params) {
    return api.get('/bids', { params });
  },
  // 获取当前登录厂家提交的投标
  getMyBids(params) {
    return api.get('/bids/mine', { params });
  },
  
  // 获取投标详情
  getBidDetail(id) {
    return api.get(`/bids/${id}`);
  },
  
  // 创建投标
  createBid(data) {
    return api.post('/bids', data);
  },
  
  // 更新投标
  updateBid(id, data) {
    return api.put(`/bids/${id}`, data);
  },

  // 撤销投标
  deleteBid(id) {
    return api.delete(`/bids/${id}`);
  },
  
  // 接受投标
  acceptBid(id) {
    return api.put(`/bids/${id}/accept`);
  },
  
  // 拒绝投标
  rejectBid(id) {
    return api.put(`/bids/${id}/reject`);
  }
};

// 留言相关API
export const messageApi = {
  // 获取留言列表
  getMessages(params) {
    return api.get('/messages', { params });
  },
  // 获取留言详情
  getMessageDetail(id) {
    return api.get(`/messages/${id}`);
  },
  
  // 创建留言
  createMessage(data) {
    return api.post('/messages', data);
  },
  
  // 回复留言
  replyMessage(id, data) {
    return api.put(`/messages/${id}/reply`, data);
  },

  // 删除留言（作者或管理员）
  deleteMessage(id) {
    return api.delete(`/messages/${id}`);
  },

  // 删除回复（作者或管理员）
  deleteReply(id) {
    return api.delete(`/replies/${id}`);
  }
};

// 管理员相关API
export const adminApi = {
  // 获取系统统计数据
  getStatistics() {
    return api.get('/admin/statistics');
  },
  
  // 获取用户列表
  getUsers(params) {
    return api.get('/admin/users', { params });
  },
  
  // 更改用户状态
  updateUserStatus(id, status) {
    return api.put(`/admin/users/${id}/status`, { status });
  },

  // 获取所有留言（管理员）
  getMessages(params) {
    return api.get('/admin/messages', { params });
  },

  // 删除留言（管理员）
  deleteMessage(id) {
    return api.delete(`/admin/messages/${id}`);
  }
  ,
  // 获取招标列表（管理员，含全部状态）
  getTenders(params) {
    return api.get('/admin/tenders', { params });
  }
};

export default api;