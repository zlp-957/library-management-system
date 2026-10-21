<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="header-left" @click="$router.push('/reader')">
        <el-icon :size="24" color="#409eff"><Reading /></el-icon>
        <span class="logo">图书管理系统</span>
      </div>
      <el-menu mode="horizontal" :default-active="activeMenu" router class="header-menu" :ellipsis="false">
        <el-menu-item index="/reader">图书检索</el-menu-item>
        <el-menu-item index="/reader/borrows">我的借阅</el-menu-item>
        <el-menu-item index="/reader/reserves">我的预约</el-menu-item>
        <el-menu-item index="/reader/orders">我的订单</el-menu-item>
      </el-menu>
      <div class="header-right">
        <el-dropdown @command="handleCommand">
          <span class="user-info">
            <el-avatar :size="32" style="background: #409eff">{{ userStore.userInfo?.name?.charAt(0) || 'U' }}</el-avatar>
            <span class="username">{{ userStore.userInfo?.name }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="profile">个人中心</el-dropdown-item>
              <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-main class="main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => {
  const path = route.path
  if (path.startsWith('/reader/book')) return '/reader'
  return path
})

const handleCommand = async (command) => {
  if (command === 'profile') {
    router.push('/reader/profile')
  } else if (command === 'logout') {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout {
  height: 100%;
}
.header {
  display: flex;
  align-items: center;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 0 24px;
}
.header-left {
  display: flex;
  align-items: center;
  cursor: pointer;
  margin-right: 40px;
}
.logo {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  margin-left: 8px;
  white-space: nowrap;
}
.header-menu {
  flex: 1;
  border-bottom: none;
}
.header-right {
  display: flex;
  align-items: center;
}
.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 6px;
}
.username {
  font-size: 14px;
  color: #303133;
}
.main {
  padding: 24px;
  background: #f5f7fa;
}
</style>
