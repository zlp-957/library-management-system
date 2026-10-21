<template>
  <el-card shadow="never">
    <template #header>
      <div class="header-row">
        <el-input v-model="keyword" placeholder="搜索账号/书名/订单号" clearable style="width: 240px" @keyup.enter="handleSearch" />
        <el-select v-model="buyType" placeholder="全部类型" clearable style="width: 160px; margin-left: 8px" @change="handleSearch">
          <el-option label="直接购买" :value="0" />
          <el-option label="逾期转购" :value="1" />
        </el-select>
        <el-button type="primary" @click="handleSearch" style="margin-left: 8px">搜索</el-button>
      </div>
    </template>
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="orderNo" label="订单编号" min-width="180" />
      <el-table-column prop="userAccount" label="读者账号" width="130" />
      <el-table-column prop="userName" label="读者姓名" width="110" />
      <el-table-column prop="bookName" label="书名" min-width="150" />
      <el-table-column label="类型" width="120">
        <template #default="{ row }">
          <el-tag v-if="row.buyType === 0" type="success">直接购买</el-tag>
          <el-tag v-else type="danger">逾期转购</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="金额" width="110">
        <template #default="{ row }">
          <span style="color: #f56c6c; font-weight: bold">¥{{ row.price }}</span>
        </template>
      </el-table-column>
      <el-table-column label="下单时间" width="170">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination background layout="prev, pager, next, total" :total="total" :page-size="size" :current-page="page" @current-change="handlePageChange" />
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { pageOrders } from '../../api'

const list = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const keyword = ref('')
const buyType = ref(null)

const loadData = async () => {
  loading.value = true
  try {
    const res = await pageOrders({
      page: page.value, size: size.value,
      keyword: keyword.value || undefined, buyType: buyType.value === null ? undefined : buyType.value
    })
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

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

onMounted(loadData)
</script>

<style scoped>
.header-row {
  display: flex;
  align-items: center;
}
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
