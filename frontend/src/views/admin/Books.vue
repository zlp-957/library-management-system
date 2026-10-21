<template>
  <el-card shadow="never">
    <template #header>
      <div class="header-row">
        <div class="header-left">
          <el-input v-model="keyword" placeholder="搜索书名/作者/ISBN" clearable style="width: 220px" @keyup.enter="handleSearch" />
          <el-select v-model="typeId" placeholder="全部分类" clearable style="width: 150px; margin-left: 8px" @change="handleSearch">
            <el-option v-for="t in types" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
          <el-button type="primary" @click="handleSearch" style="margin-left: 8px">搜索</el-button>
        </div>
        <el-button type="primary" @click="openAdd">新增图书</el-button>
      </div>
    </template>
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column label="封面" width="90">
        <template #default="{ row }">
          <el-image v-if="row.cover" :src="row.cover" fit="cover" style="width: 50px; height: 60px; border-radius: 4px" />
          <div v-else style="width: 50px; height: 60px; background: #f5f7fa; border-radius: 4px; display: flex; align-items: center; justify-content: center">
            <el-icon color="#c0c4cc"><Notebook /></el-icon>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="书名" min-width="150" />
      <el-table-column prop="author" label="作者" width="120" />
      <el-table-column prop="isbn" label="ISBN" width="140" />
      <el-table-column prop="typeName" label="分类" width="100" />
      <el-table-column label="售价" width="100">
        <template #default="{ row }">
          <span style="color: #f56c6c">¥{{ row.price }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button type="warning" size="small" @click="openEdit(row)">编辑</el-button>
          <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div class="pagination">
      <el-pagination background layout="prev, pager, next, total" :total="total" :page-size="size" :current-page="page" @current-change="handlePageChange" />
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑图书' : '新增图书'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="书名" prop="name">
          <el-input v-model="form.name" placeholder="请输入书名" />
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="form.author" placeholder="请输入作者" />
        </el-form-item>
        <el-form-item label="ISBN" prop="isbn">
          <el-input v-model="form.isbn" placeholder="请输入ISBN" />
        </el-form-item>
        <el-form-item label="分类" prop="typeId">
          <el-select v-model="form.typeId" placeholder="请选择分类" style="width: 100%">
            <el-option v-for="t in types" :key="t.id" :label="t.name" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="售价" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="封面">
          <el-upload
            class="cover-uploader"
            :show-file-list="false"
            :http-request="handleUpload"
            :before-upload="beforeUpload"
            accept=".jpg,.jpeg,.png"
          >
            <img v-if="form.cover" :src="form.cover" class="cover-preview" />
            <div v-else class="cover-upload-btn">
              <el-icon><Plus /></el-icon>
              <span>上传封面</span>
            </div>
          </el-upload>
          <div class="upload-tip">仅支持 jpg/png，大小不超过 2MB</div>
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.intro" type="textarea" :rows="4" placeholder="请输入图书简介" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { pageBooks, listBookTypes, addBook, updateBook, deleteBook } from '../../api'
import request from '../../utils/request'

const list = ref([])
const types = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const keyword = ref('')
const typeId = ref(null)

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref()
const form = reactive({
  name: '', author: '', isbn: '', typeId: null, price: 0, stock: 0, cover: '', intro: ''
})

const rules = {
  name: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者', trigger: 'blur' }],
  typeId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入售价', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

const loadTypes = async () => {
  const res = await listBookTypes()
  types.value = res.data
}

const loadData = async () => {
  loading.value = true
  try {
    const res = await pageBooks({
      page: page.value, size: size.value,
      keyword: keyword.value || undefined, typeId: typeId.value || undefined
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

const openAdd = () => {
  isEdit.value = false
  editId.value = null
  Object.assign(form, { name: '', author: '', isbn: '', typeId: null, price: 0, stock: 0, cover: '', intro: '' })
  dialogVisible.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  editId.value = row.id
  Object.assign(form, {
    name: row.name, author: row.author, isbn: row.isbn,
    typeId: row.typeId, price: Number(row.price), stock: row.stock,
    cover: row.cover, intro: row.intro
  })
  dialogVisible.value = true
}

const beforeUpload = (file) => {
  const isImage = ['image/jpeg', 'image/png'].includes(file.type)
  if (!isImage) {
    ElMessage.error('只支持 jpg/png 图片')
    return false
  }
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

const handleUpload = async (options) => {
  const fd = new FormData()
  fd.append('file', options.file)
  try {
    const res = await request.post('/book/uploadCover', fd, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    form.cover = res.data.url
    ElMessage.success('封面上传成功')
  } catch (e) {
    options.onError(e)
  }
}

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return
    const data = {
      name: form.name, author: form.author, isbn: form.isbn,
      typeId: form.typeId, price: form.price, stock: form.stock,
      cover: form.cover, intro: form.intro
    }
    if (isEdit.value) {
      await updateBook({ ...data, id: editId.value })
      ElMessage.success('修改成功')
    } else {
      await addBook(data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadData()
  })
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除《${row.name}》吗？`, '删除确认', { type: 'warning' })
  } catch (e) {
    return
  }
  await deleteBook(row.id)
  ElMessage.success('删除成功')
  loadData()
}

onMounted(() => {
  loadTypes()
  loadData()
})
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
.cover-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  width: 120px;
  height: 160px;
  overflow: hidden;
}
.cover-preview {
  width: 120px;
  height: 160px;
  object-fit: cover;
}
.cover-upload-btn {
  width: 120px;
  height: 160px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #8c939d;
  gap: 8px;
}
.upload-tip {
  font-size: 12px;
  color: #909399;
  margin-left: 12px;
}
</style>
