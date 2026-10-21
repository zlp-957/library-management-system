import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/',
    redirect: '/reader'
  },
  // 读者端
  {
    path: '/reader',
    component: () => import('../components/ReaderLayout.vue'),
    children: [
      { path: '', name: 'ReaderHome', component: () => import('../views/reader/Home.vue') },
      { path: 'book/:id', name: 'BookDetail', component: () => import('../views/reader/BookDetail.vue') },
      { path: 'borrows', name: 'MyBorrows', component: () => import('../views/reader/MyBorrows.vue') },
      { path: 'reserves', name: 'MyReserves', component: () => import('../views/reader/MyReserves.vue') },
      { path: 'orders', name: 'MyOrders', component: () => import('../views/reader/MyOrders.vue') },
      { path: 'profile', name: 'Profile', component: () => import('../views/reader/Profile.vue') }
    ]
  },
  // 管理员端
  {
    path: '/admin',
    component: () => import('../components/AdminLayout.vue'),
    children: [
      { path: '', name: 'AdminDashboard', component: () => import('../views/admin/Dashboard.vue') },
      { path: 'users', name: 'AdminUsers', component: () => import('../views/admin/Users.vue') },
      { path: 'types', name: 'AdminTypes', component: () => import('../views/admin/Types.vue') },
      { path: 'books', name: 'AdminBooks', component: () => import('../views/admin/Books.vue') },
      { path: 'borrows', name: 'AdminBorrows', component: () => import('../views/admin/Borrows.vue') },
      { path: 'reserves', name: 'AdminReserves', component: () => import('../views/admin/Reserves.vue') },
      { path: 'orders', name: 'AdminOrders', component: () => import('../views/admin/Orders.vue') },
      { path: 'notices', name: 'AdminNotices', component: () => import('../views/admin/Notices.vue') }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')

  if (to.path === '/login' || to.path === '/register') {
    // 已登录则跳转
    if (token && userInfo) {
      next(userInfo.role === 1 ? '/admin' : '/reader')
    } else {
      next()
    }
  } else {
    if (!token) {
      next('/login')
    } else if (to.path.startsWith('/admin') && userInfo?.role !== 1) {
      // 非管理员访问后台
      next('/reader')
    } else {
      next()
    }
  }
})

export default router
