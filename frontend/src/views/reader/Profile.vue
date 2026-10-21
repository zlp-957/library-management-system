<template>
  <div class="profile-container">
    <el-row :gutter="20">
      <el-col :span="10">
        <el-card shadow="never">
          <template #header><span>个人信息</span></template>
          <div class="info-display">
            <div class="avatar-big">
              <el-avatar :size="80" style="background: #409eff; font-size: 32px">{{ user?.name?.charAt(0) }}</el-avatar>
            </div>
            <div class="info-items">
              <div class="info-item"><span class="label">账号：</span>{{ user?.account }}</div>
              <div class="info-item"><span class="label">姓名：</span>{{ user?.name }}</div>
              <div class="info-item"><span class="label">角色：</span>{{ user?.role === 1 ? '管理员' : '读者' }}</div>
              <div class="info-item"><span class="label">余额：</span><span class="balance">¥{{ user?.balance }}</span></div>
              <div class="info-item" v-if="user?.punishEndTime && isPunishing">
                <el-alert type="error" :closable="false" show-icon :title="`惩罚期内（至 ${formatTime(user.punishEndTime)}），禁止借阅`" />
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="14">
        <el-card shadow="never" style="margin-bottom: 20px">
          <template #header><span>修改信息</span></template>
          <el-form :model="profileForm" label-width="80px">
            <el-form-item label="姓名">
              <el-input v-model="profileForm.name" placeholder="请输入姓名" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdateProfile">保存</el-button>
            </el-form-item>
          </el-form>
        </el-card>
        <el-card shadow="never">
          <template #header><span>修改密码</span></template>
          <el-form :model="pwdForm" label-width="80px">
            <el-form-item label="原密码">
              <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="至少6位" />
            </el-form-item>
            <el-form-item label="确认密码">
              <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdatePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserInfo, updateProfile, updatePassword } from '../../api'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const user = ref(null)
const profileForm = reactive({ name: '' })
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const isPunishing = computed(() => {
  if (!user.value?.punishEndTime) return false
  return new Date(user.value.punishEndTime.replace(' ', 'T')) > new Date()
})

const loadUser = async () => {
  const res = await getUserInfo()
  user.value = res.data
  profileForm.name = res.data.name
  userStore.setUserInfo({ ...userStore.userInfo, name: res.data.name, balance: res.data.balance })
}

const handleUpdateProfile = async () => {
  if (!profileForm.name) {
    ElMessage.warning('姓名不能为空')
    return
  }
  await updateProfile({ name: profileForm.name })
  ElMessage.success('修改成功')
  loadUser()
}

const handleUpdatePassword = async () => {
  if (pwdForm.newPassword.length < 6) {
    ElMessage.warning('新密码至少6位')
    return
  }
  if (pwdForm.newPassword !== pwdForm.confirmPassword) {
    ElMessage.warning('两次密码不一致')
    return
  }
  await updatePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
  ElMessage.success('密码修改成功，请重新登录')
  userStore.logout()
  window.location.href = '/login'
}

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

onMounted(loadUser)
</script>

<style scoped>
.info-display {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}
.info-items {
  flex: 1;
}
.info-item {
  margin-bottom: 12px;
  font-size: 14px;
  color: #303133;
}
.info-item .label {
  color: #909399;
}
.balance {
  color: #f56c6c;
  font-weight: bold;
  font-size: 18px;
}
</style>
