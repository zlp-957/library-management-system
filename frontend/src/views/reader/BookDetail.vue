<template>
  <div v-if="book" class="detail-container">
    <el-card shadow="never">
      <div class="detail-body">
        <div class="cover">
          <el-image v-if="book.cover" :src="book.cover" fit="cover" class="cover-img" />
          <div v-else class="cover-placeholder">
            <el-icon :size="60" color="#c0c4cc"><Notebook /></el-icon>
          </div>
        </div>
        <div class="info">
          <h1 class="title">{{ book.name }}</h1>
          <div class="meta">
            <el-tag type="info">{{ book.typeName }}</el-tag>
            <span class="meta-item">作者：{{ book.author || '未知' }}</span>
            <span class="meta-item">ISBN：{{ book.isbn || '暂无' }}</span>
          </div>
          <div class="price-row">
            <span class="price-label">售价</span>
            <span class="price-value">¥{{ book.price }}</span>
          </div>
          <div class="stock-row">
            <span class="price-label">库存</span>
            <span :class="book.stock > 0 ? 'stock-value' : 'stock-value-zero'">
              {{ book.stock > 0 ? book.stock + ' 本' : '已售罄' }}
            </span>
          </div>
          <div class="intro">
            <h3>图书简介</h3>
            <p>{{ book.intro || '暂无简介' }}</p>
          </div>
          <div class="actions">
            <el-button v-if="book.stock > 0" type="primary" size="large" :loading="loading" @click="handleBorrow">
              <el-icon style="margin-right: 4px"><Tickets /></el-icon>免费借阅
            </el-button>
            <el-button v-if="book.stock > 0" type="success" size="large" :loading="loading" @click="handleBuy">
              <el-icon style="margin-right: 4px"><ShoppingCart /></el-icon>购买（¥{{ book.price }}）
            </el-button>
            <el-button v-if="book.stock <= 0" type="warning" size="large" :loading="loading" @click="handleReserve">
              <el-icon style="margin-right: 4px"><Timer /></el-icon>预约排队
            </el-button>
            <el-button size="large" @click="$router.back()">返回</el-button>
          </div>
          <div class="tips">
            <el-alert v-if="book.stock <= 0" type="warning" :closable="false" show-icon
              title="该图书库存为0，可提交预约排队，库存恢复后将自动通知您" />
            <el-alert v-else type="info" :closable="false" show-icon
              title="借阅免费（最多5本，期限30天）；购买将扣除余额" />
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBookDetail, borrowBook, buyBook, reserveBook } from '../../api'
import { useUserStore } from '../../stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const book = ref(null)
const loading = ref(false)

const loadDetail = async () => {
  const res = await getBookDetail(route.params.id)
  book.value = res.data
}

const handleBorrow = async () => {
  try {
    await ElMessageBox.confirm(`确定要免费借阅《${book.value.name}》吗？借阅期限30天。`, '借阅确认', { type: 'info' })
  } catch (e) {
    return
  }
  loading.value = true
  try {
    await borrowBook(book.value.id)
    ElMessage.success('借阅成功')
    loadDetail()
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

const handleBuy = async () => {
  try {
    await ElMessageBox.confirm(`确定要购买《${book.value.name}》吗？将扣除余额 ¥${book.value.price}。`, '购买确认', { type: 'warning' })
  } catch (e) {
    return
  }
  loading.value = true
  try {
    await buyBook(book.value.id)
    ElMessage.success('购买成功')
    loadDetail()
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

const handleReserve = async () => {
  try {
    await ElMessageBox.confirm(`确定要预约《${book.value.name}》吗？库存恢复后将按排队顺序通知您。`, '预约确认', { type: 'info' })
  } catch (e) {
    return
  }
  loading.value = true
  try {
    await reserveBook(book.value.id)
    ElMessage.success('预约成功，已进入排队队列')
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

onMounted(loadDetail)
</script>

<style scoped>
.detail-body {
  display: flex;
  gap: 40px;
}
.cover {
  width: 280px;
  height: 380px;
  flex-shrink: 0;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f7fa;
}
.cover-img {
  width: 100%;
  height: 100%;
}
.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.info {
  flex: 1;
}
.title {
  font-size: 28px;
  color: #303133;
  margin-bottom: 16px;
}
.meta {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 20px;
}
.meta-item {
  font-size: 14px;
  color: #606266;
}
.price-row {
  margin-bottom: 12px;
}
.price-label {
  font-size: 14px;
  color: #909399;
  margin-right: 16px;
}
.price-value {
  font-size: 28px;
  color: #f56c6c;
  font-weight: bold;
}
.stock-row {
  margin-bottom: 24px;
}
.stock-value {
  font-size: 16px;
  color: #67c23a;
  font-weight: bold;
}
.stock-value-zero {
  font-size: 16px;
  color: #f56c6c;
  font-weight: bold;
}
.intro {
  margin-bottom: 24px;
}
.intro h3 {
  font-size: 16px;
  color: #303133;
  margin-bottom: 8px;
}
.intro p {
  font-size: 14px;
  color: #606266;
  line-height: 1.8;
}
.actions {
  margin-bottom: 20px;
}
.tips {
  max-width: 500px;
}
</style>
