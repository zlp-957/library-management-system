<template>
  <el-card shadow="never">
    <template #header>
      <span>我的订单</span>
    </template>
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="orderNo" label="订单编号" min-width="180" />
      <el-table-column prop="bookName" label="书名" min-width="150">
        <template #default="{ row }">
          <el-link type="primary" @click="$router.push(`/reader/book/${row.bookId}`)">{{ row.bookName }}</el-link>
        </template>
      </el-table-column>
      <el-table-column label="类型" width="140">
        <template #default="{ row }">
          <el-tag v-if="row.buyType === 0" type="success">直接购买</el-tag>
          <el-tag v-else type="danger">逾期转购</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="金额" width="120">
        <template #default="{ row }">
          <span style="color: #f56c6c; font-weight: bold">¥{{ row.price }}</span>
        </template>
      </el-table-column>
      <el-table-column label="下单时间" width="180">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
    </el-table>
    <el-empty v-if="!loading && list.length === 0" description="暂无订单" />
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { myOrders } from '../../api'

const list = ref([])
const loading = ref(false)

const loadData = async () => {
  loading.value = true
  try {
    const res = await myOrders()
    list.value = res.data
  } finally {
    loading.value = false
  }
}

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

onMounted(loadData)
</script>
