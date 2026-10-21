<template>
  <el-card shadow="never">
    <template #header>
      <div class="header-row">
        <div class="header-left">
          <el-input v-model="keyword" placeholder="搜索账号/姓名" clearable style="width: 220px" @keyup.enter="handleSearch" />
          <el-button type="primary" @click="handleSearch" style="margin-left: 8px">搜索</el-button>
        </div>
        <el-button type="primary" @click="openAdd">新增读者</el-button>
      </div>
    </template>
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="account" label="账号" width="140" />
      <el-table-column prop="name" label="姓名" width="120" />
      <el-table-column label="余额" width="120">
        <template #default="{ row }">
          <span style="color: #f56c6c; font-weight: bold">¥{{ row.balance }}</span>
        </template>
      </el-table-column>
      <el-table-column label="惩罚截止" width="180">
        <template #default="{ row }">
          <span v-if="row.punishEndTime">{{ formatTime(row.punishEndTime) }}</span>
          <span v-else>—</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'success' : 'danger'">{{ row.status === 0 ? '正常' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="注册时间" width="180">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" @click="openRecharge(row)">充值</el-button>
          <el-button type="warning" size="small" @click="openEdit(row)">编辑</el-button>
          <el-button :type="row.status === 0 ? 'danger' : 'success'" size="small" @click="handleToggle(row)">
            {{ row.status === 0 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination background layout="prev, pager, next, total" :total="total" :page-size="size" :current-page="page" @current-change="handlePageChange" />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="账号" prop="account">
          <el-input v-model="form.account" placeholder="R+8位数字" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password :placeholder="isEdit ? '留空则不修改' : '至少6位'" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 充值对话框 -->
    <el-dialog v-model="rechargeVisible" title="充值" width="400px">
      <el-form label-width="90px">
        <el-form-item label="当前余额">
          <span style="color: #f56c6c; font-weight: bold">¥{{ currentUser?.balance }}</span>
        </el-form-item>
        <el-form-item label="充值金额">
          <el-input-number v-model="rechargeAmount" :min="0.01" :precision="2" :step="10" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rechargeVisible = false">取消</el-button>
        <el-button type="primary" @click="handleRecharge">确定充值</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { pageReaders, addReader, updateReader, toggleReader, rechargeReader } from '../../api'

const list = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const keyword = ref('')

const dialogVisible = ref(false)
const dialogTitle = ref('新增读者')
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref()
const form = reactive({ account: '', name: '', password: '' })

const rechargeVisible = ref(false)
const rechargeAmount = ref(10)
const currentUser = ref(null)

const rules = {
  account: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { pattern: /^R\d{8}$/, message: '账号格式：R + 8位数字', trigger: 'blur' }
  ],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [
    { validator: (rule, value, callback) => {
      if (!isEdit.value && (!value || value.length < 6)) {
        callback(new Error('密码至少6位'))
      } else {
        callback()
      }
    }, trigger: 'blur' }
  ]
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await pageReaders({ page: page.value, size: size.value, keyword: keyword.value || undefined })
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  loadData()
}

const handlePageChange = (p) => {
  page.value = p
  loadData()
}

const openAdd = () => {
  isEdit.value = false
  editId.value = null
  dialogTitle.value = '新增读者'
  form.account = ''
  form.name = ''
  form.password = ''
  dialogVisible.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  editId.value = row.id
  dialogTitle.value = '编辑读者'
  form.account = row.account
  form.name = row.name
  form.password = ''
  dialogVisible.value = true
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    if (isEdit.value) {
      await updateReader(editId.value, { name: form.name, password: form.password })
      ElMessage.success('编辑成功')
    } else {
      await addReader({ account: form.account, name: form.name, password: form.password })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  })
}

const handleToggle = async (row) => {
  await toggleReader(row.id)
  ElMessage.success('操作成功')
  loadData()
}

const openRecharge = (row) => {
  currentUser.value = row
  rechargeAmount.value = 10
  rechargeVisible.value = true
}

const handleRecharge = async () => {
  await rechargeReader(currentUser.value.id, { amount: rechargeAmount.value })
  ElMessage.success('充值成功')
  rechargeVisible.value = false
  loadData()
}

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

onMounted(loadData)
</script>

<style scoped>
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-left {
  display: flex;
}
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
