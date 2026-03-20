import { request } from './http'
import type {
  Address,
  CartItem,
  Category,
  LoginResponse,
  Order,
  OrderItem,
  PageResult,
  Payment,
  Product,
  ProductSaveRequest,
  SystemParamConfig,
  UploadResult,
  UserProfile,
} from '@/types'

export const api = {
  adminLogin(data: { username: string; password: string }) {
    return request<LoginResponse>({ url: '/api/admin/auth/login', method: 'post', data })
  },
  frontendLogin(data: { username: string; password: string }) {
    return request<LoginResponse>({ url: '/api/frontend/auth/login', method: 'post', data })
  },
  frontendRegister(data: { username: string; password: string; nickname: string; phone?: string; email?: string }) {
    return request<UserProfile>({ url: '/api/frontend/auth/register', method: 'post', data })
  },
  getMe() {
    return request<UserProfile>({ url: '/api/auth/me', method: 'get' })
  },
  listUsers(userType?: 'BACKEND' | 'FRONTEND') {
    return request<UserProfile[]>({ url: '/api/admin/users', method: 'get', params: userType ? { userType } : undefined })
  },
  createUser(data: Partial<UserProfile> & { username: string; password: string; nickname: string }) {
    return request<UserProfile>({ url: '/api/admin/users', method: 'post', data })
  },
  updateUser(id: number, userType: 'BACKEND' | 'FRONTEND', data: Partial<UserProfile> & { password?: string }) {
    return request<UserProfile>({ url: `/api/admin/users/${id}`, method: 'put', params: { userType }, data })
  },
  deleteUser(id: number, userType: 'BACKEND' | 'FRONTEND') {
    return request<void>({ url: `/api/admin/users/${id}`, method: 'delete', params: { userType } })
  },
  listCategories() {
    return request<Category[]>({ url: '/api/categories', method: 'get' })
  },
  createCategory(data: Partial<Category>) {
    return request<Category>({ url: '/api/categories', method: 'post', data })
  },
  updateCategory(id: number, data: Partial<Category>) {
    return request<Category>({ url: `/api/categories/${id}`, method: 'put', data })
  },
  deleteCategory(id: number) {
    return request<void>({ url: `/api/categories/${id}`, method: 'delete' })
  },
  uploadFile(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return request<UploadResult>({ url: '/api/files/upload', method: 'post', data: formData })
  },
  uploadImage(file: File) {
    return this.uploadFile(file)
  },
  listProducts(params: { pageNum?: number; pageSize?: number; keyword?: string; categoryId?: number; status?: number }) {
    return request<PageResult<Product>>({ url: '/api/products', method: 'get', params })
  },
  productDetail(id: number) {
    return request<Product>({ url: `/api/products/${id}`, method: 'get' })
  },
  createProduct(data: ProductSaveRequest) {
    return request<Product>({ url: '/api/products', method: 'post', data })
  },
  updateProduct(id: number, data: ProductSaveRequest) {
    return request<Product>({ url: `/api/products/${id}`, method: 'put', data })
  },
  deleteProduct(id: number) {
    return request<void>({ url: `/api/products/${id}`, method: 'delete' })
  },
  listSystemParams(params?: { paramType?: 'SYSTEM' | 'BUSINESS'; keyword?: string }) {
    return request<SystemParamConfig[]>({ url: '/api/admin/system-params', method: 'get', params })
  },
  createSystemParam(data: Omit<SystemParamConfig, 'id' | 'createTime' | 'updateTime'>) {
    return request<SystemParamConfig>({ url: '/api/admin/system-params', method: 'post', data })
  },
  updateSystemParam(id: number, data: Omit<SystemParamConfig, 'id' | 'createTime' | 'updateTime'>) {
    return request<SystemParamConfig>({ url: `/api/admin/system-params/${id}`, method: 'put', data })
  },
  deleteSystemParam(id: number) {
    return request<void>({ url: `/api/admin/system-params/${id}`, method: 'delete' })
  },
  addCart(data: { userId: number; productId: number; quantity: number }) {
    return request<void>({ url: '/api/cart', method: 'post', data })
  },
  listCart(userId: number) {
    return request<CartItem[]>({ url: '/api/cart', method: 'get', params: { userId } })
  },
  cartSummary(userId: number) {
    return request<{ items: CartItem[]; totalAmount: number; totalQuantity: number; checkedCount: number }>({
      url: '/api/cart/summary',
      method: 'get',
      params: { userId },
    })
  },
  updateCartQuantity(id: number, userId: number, quantity: number) {
    return request<void>({ url: `/api/cart/${id}/quantity`, method: 'put', params: { userId }, data: { quantity } })
  },
  updateCartChecked(id: number, userId: number, checked: number) {
    return request<void>({ url: `/api/cart/${id}/checked`, method: 'put', params: { userId }, data: { checked } })
  },
  deleteCart(id: number, userId: number) {
    return request<void>({ url: `/api/cart/${id}`, method: 'delete', params: { userId } })
  },
  clearCart(userId: number) {
    return request<void>({ url: `/api/cart/user/${userId}`, method: 'delete' })
  },
  listAddresses(userId: number) {
    return request<Address[]>({ url: '/api/addresses', method: 'get', params: { userId } })
  },
  createAddress(data: Omit<Address, 'id' | 'status'>) {
    return request<Address>({ url: '/api/addresses', method: 'post', data })
  },
  updateAddress(id: number, data: Omit<Address, 'id' | 'status'>) {
    return request<Address>({ url: `/api/addresses/${id}`, method: 'put', data })
  },
  deleteAddress(id: number, userId: number) {
    return request<void>({ url: `/api/addresses/${id}`, method: 'delete', params: { userId } })
  },
  listOrders(userId: number, status?: string) {
    return request<Order[]>({ url: '/api/orders', method: 'get', params: { userId, status } })
  },
  orderDetail(id: number, userId: number) {
    return request<{ order: Order; items: OrderItem[] }>({ url: `/api/orders/${id}`, method: 'get', params: { userId } })
  },
  createOrder(data: {
    userId: number
    addressId?: number
    receiverName?: string
    receiverPhone?: string
    receiverAddress?: string
    remark?: string
    items: Array<{ productId: number; quantity: number }>
  }) {
    return request<{ order: Order; items: OrderItem[] }>({ url: '/api/orders', method: 'post', data })
  },
  cancelOrder(id: number, userId: number, reason?: string) {
    return request<void>({ url: `/api/orders/${id}/cancel`, method: 'post', params: { userId }, data: { reason } })
  },
  deliverOrder(id: number, data: { deliveryCompany: string; deliveryNo: string }) {
    return request<void>({ url: `/api/orders/${id}/deliver`, method: 'post', data })
  },
  receiveOrder(id: number, userId: number) {
    return request<void>({ url: `/api/orders/${id}/receive`, method: 'post', params: { userId } })
  },
  refundOrder(id: number, userId: number, reason?: string) {
    return request<void>({ url: `/api/orders/${id}/refund`, method: 'post', params: { userId }, data: { reason } })
  },
  createPayment(orderId: number, userId: number, payType: string) {
    return request<{ payment: Payment; payUrl: string; message: string }>({
      url: `/api/payments/orders/${orderId}`,
      method: 'post',
      params: { userId },
      data: { payType },
    })
  },
  getPayment(orderId: number, userId: number) {
    return request<Payment>({ url: `/api/payments/orders/${orderId}`, method: 'get', params: { userId } })
  },
  confirmPayment(paymentId: number, thirdPartyTradeNo: string) {
    return request<{ payment: Payment; order: Order }>({
      url: `/api/payments/${paymentId}/success`,
      method: 'post',
      data: { thirdPartyTradeNo, status: 'SUCCESS' },
    })
  },
  getOverview() {
    return request<{ totalOrders: number; paidOrders: number; pendingPaymentOrders: number; refundOrders: number; totalSales: number }>({
      url: '/api/admin/statistics/overview',
      method: 'get',
    })
  },
  getTopProducts() {
    return request<Array<{ productId: number; productName: string; salesQuantity: number; salesAmount: number }>>({
      url: '/api/admin/statistics/top-products',
      method: 'get',
    })
  },
}
