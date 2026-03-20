export interface ApiResponse<T> {
  success: boolean
  message: string
  data: T
}

export interface LoginResponse {
  token: string
  tokenType: string
  expiresIn: number
  userId: number
  username: string
  nickname: string
}

export interface UserProfile {
  id: number
  username: string
  nickname: string
  phone?: string
  email?: string
  userType: 'BACKEND' | 'FRONTEND'
  roleCode?: string | null
  status: number
}

export interface Category {
  id: number
  name: string
  description?: string
  sortOrder?: number
  status?: number
}

export interface ProductSku {
  id?: number
  productId?: number
  skuCode: string
  skuName: string
  specValues: string
  image?: string
  salePrice: number
  stock: number
  status: number
}

export interface Product {
  id: number
  categoryId: number
  name: string
  subtitle?: string
  description?: string
  price: number
  stock: number
  coverImage?: string
  detailImages?: string[]
  status?: number
  skuList?: ProductSku[]
}

export interface ProductSaveRequest {
  id?: number
  categoryId: number
  name: string
  subtitle?: string
  description?: string
  coverImage?: string
  detailImages?: string[]
  status: number
  skuList: ProductSku[]
}

export interface UploadResult {
  objectKey: string
  url: string
  originalFilename?: string
}

export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

export interface CartItem {
  id: number
  userId: number
  productId: number
  quantity: number
  checked: number
  createTime?: string
  updateTime?: string
}

export interface Address {
  id: number
  userId: number
  receiverName: string
  receiverPhone: string
  province: string
  city: string
  district?: string
  detailAddress: string
  postalCode?: string
  isDefault: number
  status: number
}

export interface Order {
  id: number
  orderNo: string
  userId: number
  totalAmount: number
  status: string
  receiverName: string
  receiverPhone: string
  receiverAddress: string
  deliveryCompany?: string
  deliveryNo?: string
  deliveryTime?: string
  receiveTime?: string
  refundTime?: string
  remark?: string
  createTime?: string
}

export interface OrderItem {
  id: number
  orderId: number
  orderNo: string
  productId: number
  productName: string
  productImage?: string
  productPrice: number
  quantity: number
  totalAmount: number
}

export interface Payment {
  id: number
  orderId: number
  orderNo: string
  userId: number
  payNo: string
  payAmount: number
  payType: string
  status: string
  payTime?: string
}

export interface SystemParamConfig {
  id: number
  paramType: 'SYSTEM' | 'BUSINESS'
  paramGroup?: string
  paramName: string
  paramKey: string
  paramValue?: string
  valueType: 'STRING' | 'TEXT' | 'PASSWORD' | 'NUMBER' | 'BOOLEAN'
  remark?: string
  status: number
  createTime?: string
  updateTime?: string
}
