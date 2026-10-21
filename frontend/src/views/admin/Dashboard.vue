<template>
  <div>
    <el-row :gutter="20">
      <el-col :span="6" v-for="card in cards" :key="card.title">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-body">
            <el-icon :size="40" :color="card.color"><component :is="card.icon" /></el-icon>
            <div class="stat-info">
              <div class="stat-value">{{ card.value }}</div>
              <div class="stat-title">{{ card.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getStat } from '../../api'

const stat = ref(null)

const cards = computed(() => {
  if (!stat.value) return []
  return [
    { title: '图书总数', value: stat.value.bookCount, icon: 'Notebook', color: '#409eff' },
    { title: '读者总数', value: stat.value.readerCount, icon: 'User', color: '#67c23a' },
    { title: '借阅中', value: stat.value.borrowingCount, icon: 'Tickets', color: '#e6a23c' },
    { title: '逾期转购', value: stat.value.overdueCount, icon: 'Warning', color: '#f56c6c' },
    { title: '预约排队中', value: stat.value.queuingReserveCount, icon: 'Timer', color: '#909399' },
    { title: '直接购买订单', value: stat.value.directPurchaseCount, icon: 'ShoppingCart', color: '#409eff' },
    { title: '逾期转购订单', value: stat.value.overduePurchaseCount, icon: 'Sell', color: '#f56c6c' },
    { title: '购买总额(¥)', value: stat.value.totalPurchaseAmount, icon: 'Money', color: '#67c23a' }
  ]
})

onMounted(async () => {
  const res = await getStat()
  stat.value = res.data
})
</script>

<style scoped>
.stat-card {
  margin-bottom: 20px;
}
.stat-body {
  display: flex;
  align-items: center;
  gap: 16px;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}
.stat-title {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}
</style>
