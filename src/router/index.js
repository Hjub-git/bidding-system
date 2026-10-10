import Vue from 'vue'
import VueRouter from 'vue-router'
import Home from '../views/Home.vue'
import Login from '../views/Login.vue'
import Register from '../views/Register.vue'
import ManufacturerDashboard from '../views/manufacturer/Dashboard.vue'
import TenderDashboard from '../views/tender/Dashboard.vue'
import AdminDashboard from '../views/admin/Dashboard.vue'
import MessageBoard from '../views/messages/MessageBoard.vue'
import MessageDetail from '../views/messages/MessageDetail.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/messages',
    name: 'MessageBoard',
    component: MessageBoard
  },
  {
    path: '/messages/:id',
    name: 'MessageDetail',
    component: MessageDetail,
    props: true
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/register',
    name: 'Register',
    component: Register
  },
  {
    path: '/manufacturer',
    name: 'ManufacturerDashboard',
    component: ManufacturerDashboard,
    meta: { requiresAuth: true, role: 'manufacturer' }
  },
  {
    path: '/tender',
    name: 'TenderDashboard',
    component: TenderDashboard,
    meta: { requiresAuth: true, role: 'tender' }
  },
  {
    path: '/admin',
    name: 'AdminDashboard',
    component: AdminDashboard,
    meta: { requiresAuth: true, role: 'admin' }
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const loggedIn = localStorage.getItem('user')
  let user = null
  if (loggedIn) {
    try {
      user = JSON.parse(loggedIn)
    } catch (e) {
      // 清理无效的 user 值，避免解析异常造成导航中断
      localStorage.removeItem('user')
    }
  }
  
  // 检查是否需要认证
  if (to.matched.some(record => record.meta.requiresAuth)) {
    if (!user) {
      next('/login')
      return
    }
    
    // 检查角色权限
    if (to.meta.role) {
      if (user.role !== to.meta.role) {
        next('/')
        return
      }
    }
  }
  
  next()
})

export default router