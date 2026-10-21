import request from '../utils/request'

// ============ 认证 ============
export const login = (data) => request.post('/auth/login', data)
export const register = (data) => request.post('/auth/register', data)

// ============ 用户 ============
export const getUserInfo = () => request.get('/user/info')
export const updateProfile = (data) => request.post('/user/profile', data)
export const updatePassword = (data) => request.post('/user/password', data)
export const pageReaders = (params) => request.get('/user/readers', { params })
export const addReader = (data) => request.post('/user/reader', data)
export const updateReader = (id, data) => request.put(`/user/reader/${id}`, data)
export const toggleReader = (id) => request.post(`/user/reader/${id}/toggle`)
export const rechargeReader = (id, data) => request.post(`/user/reader/${id}/recharge`, data)

// ============ 分类 ============
export const listBookTypes = () => request.get('/bookType/list')
export const addBookType = (data) => request.post('/bookType/add', data)
export const updateBookType = (data) => request.put('/bookType/update', data)
export const deleteBookType = (id) => request.delete(`/bookType/delete/${id}`)

// ============ 图书 ============
export const pageBooks = (params) => request.get('/book/page', { params })
export const getBookDetail = (id) => request.get(`/book/${id}`)
export const addBook = (data) => request.post('/book/add', data)
export const updateBook = (data) => request.put('/book/update', data)
export const deleteBook = (id) => request.delete(`/book/delete/${id}`)

// ============ 借阅 ============
export const borrowBook = (bookId) => request.post('/borrow/add', { bookId })
export const returnBook = (borrowId) => request.post(`/borrow/return/${borrowId}`)
export const myBorrows = (status) => request.get('/borrow/my', { params: { status } })
export const pageBorrows = (params) => request.get('/borrow/page', { params })
export const processOverdue = () => request.post('/borrow/processOverdue')

// ============ 预约 ============
export const reserveBook = (bookId) => request.post('/reserve/add', { bookId })
export const cancelReserve = (id) => request.post(`/reserve/cancel/${id}`)
export const myReserves = () => request.get('/reserve/my')
export const pageReserves = (params) => request.get('/reserve/page', { params })
export const processExpired = () => request.post('/reserve/processExpired')

// ============ 购买 ============
export const buyBook = (bookId) => request.post(`/purchase/buy/${bookId}`)
export const myOrders = () => request.get('/purchase/my')
export const pageOrders = (params) => request.get('/purchase/page', { params })

// ============ 公告 ============
export const pageNotices = (params) => request.get('/notice/page', { params })
export const latestNotices = () => request.get('/notice/latest')
export const addNotice = (data) => request.post('/notice/add', data)
export const updateNotice = (data) => request.put('/notice/update', data)
export const deleteNotice = (id) => request.delete(`/notice/delete/${id}`)

// ============ 统计 ============
export const getStat = () => request.get('/stat/overview')
