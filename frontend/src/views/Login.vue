<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-title">
        <el-icon :size="40" color="#409eff"><Reading /></el-icon>
        <h1>图书管理系统</h1>
        <p>免费借阅 · 付费购买 · 图书预约</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" size="large">
        <el-form-item prop="account">
          <el-input v-model="form.account" placeholder="账号（读者R+8位数字 / 管理员A+6位数字）" clearable>
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="密码" show-password @keyup.enter="handleLogin">
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="width: 100%" :loading="loading" @click="handleLogin">登 录</el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <span>还没有账号？</span>
        <el-link type="primary" @click="$router.push('/register')">立即注册</el-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../api'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  account: '',
  password: ''
})

const rules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await login(form)
      userStore.setLogin(res.data.token, {
        userId: res.data.userId,
        account: res.data.account,
        name: res.data.name,
        role: res.data.role,
        balance: res.data.balance
      })
      ElMessage.success('登录成功')
      router.push(res.data.role === 1 ? '/admin' : '/reader')
    } catch (e) {
      // 错误已在拦截器提示
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 420px;
  padding: 40px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.15);
}
.login-title {
  text-align: center;
  margin-bottom: 30px;
}
.login-title h1 {
  font-size: 24px;
  color: #303133;
  margin: 12px 0 8px;
}
.login-title p {
  font-size: 13px;
  color: #909399;
}
.login-footer {
  text-align: center;
  font-size: 14px;
  color: #606266;
  margin-top: 10px;
}
</style>
