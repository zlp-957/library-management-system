<template>
  <el-card shadow="never">
    <template #header>
      <div class="card-header">
        <span>我的借阅</span>
        <el-radio-group v-model="status" @change="loadData">
          <el-radio-button :label="undefined">全部</el-radio-button>
          <el-radio-button :label="0">借阅中</el-radio-button>
          <el-radio-button :label="1">已归还</el-radio-button>
          <el-radio-button :label="2">逾期转购</el-radio-button>
        </el-radio-group>
      </div>
    </template>
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column label="图书封面" width="100">
        <template #default="{ row }">
          <el-image v-if="row.bookCover" :src="row.bookCover" fit="cover" style="width: 50px; height: 60px; border-radius: 4px" />
          <div v-else style="width: 50px; height: 60px; background: #f5f7fa; border-radius: 4px; display: flex; align-items: center; justify-content: center">
            <el-icon color="#c0c4cc"><Notebook /></el-icon>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="bookName" label="书名" min-width="150" />
      <el-table-column label="借阅时间" width="180">
        <template #default="{ row }">{{ formatTime(row.borrowTime) }}</template>
      </el-table-column>
      <el-table-column label="应还时间" width="180">
        <template #default="{ row }">{{ formatTime(row.deadlineTime) }}</template>
      </el-table-column>
      <el-table-column label="实际归还" width="180">
        <template #default="{ row }">{{ row.returnTime ? formatTime(row.returnTime) : '—' }}</template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.status === 0" type="primary">借阅中</el-tag>
          <el-tag v-else-if="row.status === 1" type="success">已归还</el-tag>
          <el-tag v-else type="danger">逾期转购</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" type="success" size="small" @click="handleReturn(row)">归还</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && list.length === 0" description="暂无借阅记录" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { myBorrows, returnBook } from '../../api'

const list = ref([])
const loading = ref(false)
const status = ref(undefined)

const loadData = async () => {
  loading.value = true
  try {
    const res = await myBorrows(status.value)
    list.value = res.data
  } finally {
    loading.value = false
  }
}

const handleReturn = async (row) => {
  try {
    await ElMessageBox.confirm(`确定归还《${row.bookName}》吗？`, '归还确认', { type: 'info' })
  } catch (e) {
    return
  }
  await returnBook(row.id)
  ElMessage.success('归还成功')
  loadData()
}

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

onMounted(loadData)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
