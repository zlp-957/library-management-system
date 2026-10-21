<template>
  <div>
    <!-- 搜索栏 -->
    <el-card class="search-card" shadow="never">
      <div class="search-bar">
        <el-input v-model="keyword" placeholder="搜索书名 / 作者 / ISBN" clearable size="large" style="width: 320px" @keyup.enter="handleSearch">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-select v-model="typeId" placeholder="全部分类" clearable size="large" style="width: 180px">
          <el-option v-for="t in types" :key="t.id" :label="t.name" :value="t.id" />
        </el-select>
        <el-button type="primary" size="large" @click="handleSearch">搜索</el-button>
      </div>
    </el-card>

    <!-- 预约提醒 -->
    <el-alert v-if="notifyReserves.length > 0" type="success" :closable="false" class="notify-alert" show-icon>
      <template #title>
        <span>您有 {{ notifyReserves.length }} 本预约图书可以借阅了：</span>
        <el-tag v-for="r in notifyReserves" :key="r.id" type="success" style="margin-right: 8px">
          《{{ r.bookName }}》有效期至 {{ formatTime(r.validDeadline) }}
        </el-tag>
      </template>
    </el-alert>

    <!-- 图书列表 -->
    <el-row :gutter="20">
      <el-col v-for="book in books" :key="book.id" :xs="12" :sm="8" :md="6" :lg="4">
        <el-card class="book-card" shadow="hover" @click="goDetail(book.id)">
          <div class="book-cover">
            <el-image v-if="book.cover" :src="book.cover" fit="cover" class="cover-img" />
            <div v-else class="cover-placeholder">
              <el-icon :size="40" color="#c0c4cc"><Notebook /></el-icon>
            </div>
            <el-tag v-if="book.stock <= 0" type="danger" class="stock-tag" size="small">缺货</el-tag>
          </div>
          <div class="book-info">
            <div class="book-name" :title="book.name">{{ book.name }}</div>
            <div class="book-author">{{ book.author }}</div>
            <div class="book-bottom">
              <span class="book-price">¥{{ book.price }}</span>
              <span class="book-stock">库存 {{ book.stock }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!loading && books.length === 0" description="暂无图书" />

    <!-- 分页 -->
    <div class="pagination" v-if="total > 0">
      <el-pagination
        background
        layout="prev, pager, next, total"
        :total="total"
        :page-size="pageSize"
        :current-page="page"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { pageBooks, listBookTypes, myReserves } from '../../api'

const router = useRouter()
const keyword = ref('')
const typeId = ref(null)
const books = ref([])
const types = ref([])
const notifyReserves = ref([])
const loading = ref(false)
const page = ref(1)
const pageSize = ref(12)
const total = ref(0)

const loadTypes = async () => {
  const res = await listBookTypes()
  types.value = res.data
}

const loadBooks = async () => {
  loading.value = true
  try {
    const res = await pageBooks({
      page: page.value,
      size: pageSize.value,
      keyword: keyword.value || undefined,
      typeId: typeId.value || undefined
    })
    books.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

const loadNotifyReserves = async () => {
  try {
    const res = await myReserves()
    notifyReserves.value = res.data.filter(r => r.status === 1)
  } catch (e) {
    // 忽略
  }
}

const handleSearch = () => {
  page.value = 1
  loadBooks()
}

const handlePageChange = (p) => {
  page.value = p
  loadBooks()
}

const goDetail = (id) => {
  router.push(`/reader/book/${id}`)
}

const formatTime = (time) => {
  if (!time) return ''
  return time.replace('T', ' ').substring(0, 16)
}

onMounted(() => {
  loadTypes()
  loadBooks()
  loadNotifyReserves()
})
</script>

<style scoped>
.search-card {
  margin-bottom: 16px;
}
.search-bar {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.notify-alert {
  margin-bottom: 16px;
}
.book-card {
  margin-bottom: 20px;
  cursor: pointer;
}
.book-cover {
  position: relative;
  height: 180px;
  overflow: hidden;
  border-radius: 4px;
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
.stock-tag {
  position: absolute;
  top: 8px;
  right: 8px;
}
.book-info {
  padding-top: 12px;
}
.book-name {
  font-size: 15px;
  font-weight: bold;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.book-author {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}
.book-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 8px;
}
.book-price {
  color: #f56c6c;
  font-size: 16px;
  font-weight: bold;
}
.book-stock {
  font-size: 12px;
  color: #909399;
}
.pagination {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
