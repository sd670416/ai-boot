<template>
  <div class="page-header">
    <div>
      <h2>{{ auth.isAdmin ? '商品管理' : '商品中心' }}</h2>
      <p>{{ auth.isAdmin ? '后台统一维护商品主数据、分类与多规格 SKU。' : '浏览商品并查看图文详情。' }}</p>
    </div>
    <n-space v-if="auth.isAdmin">
      <n-button @click="showCategoryModal = true">管理分类</n-button>
      <n-button type="primary" @click="openProductModal()">新增商品</n-button>
    </n-space>
  </div>

  <n-card class="page-card" title="筛选条件">
    <div class="toolbar">
      <n-input v-model:value="filters.keyword" placeholder="搜索商品名称/副标题/描述" clearable style="width: 260px" />
      <n-select v-model:value="filters.categoryId" :options="categoryOptions" clearable placeholder="商品分类" style="width: 180px" />
      <n-select v-model:value="filters.status" :options="statusOptions" clearable placeholder="商品状态" style="width: 140px" />
      <n-button type="primary" @click="loadProducts">查询</n-button>
      <n-button @click="resetFilters">重置</n-button>
    </div>
  </n-card>

  <n-card class="page-card" title="商品列表">
    <n-data-table :columns="columns" :data="products" :bordered="false" />
    <div style="display:flex;justify-content:flex-end;margin-top:16px;">
      <n-pagination v-model:page="pagination.page" v-model:page-size="pagination.pageSize" :item-count="pagination.total" show-size-picker :page-sizes="[10,20,50]" @update:page="loadProducts" @update:page-size="loadProducts" />
    </div>
  </n-card>

  <product-form-modal
    v-model:show="showProductModal"
    :product="editingProduct"
    :categories="categories"
    @submit="saveProduct"
  />

  <n-modal :show="showCategoryModal" preset="card" title="分类管理" style="width:min(760px, 96vw)" @update:show="showCategoryModal = $event">
    <n-space vertical>
      <div class="form-grid">
        <n-input v-model:value="categoryForm.name" placeholder="分类名称" />
        <n-input v-model:value="categoryForm.description" placeholder="分类描述" />
      </div>
      <n-space>
        <n-button type="primary" @click="saveCategory">{{ categoryForm.id ? '更新分类' : '新增分类' }}</n-button>
        <n-button @click="resetCategoryForm">重置</n-button>
      </n-space>
      <n-data-table :columns="categoryColumns" :data="categories" :bordered="false" />
    </n-space>
  </n-modal>

  <n-modal :show="showDetailModal" preset="card" style="width:min(1120px, 96vw)" title="商品详情" @update:show="showDetailModal = $event">
    <div v-if="currentDetail" class="product-detail-layout">
      <section class="detail-gallery">
        <n-image :src="currentDetail.coverImage || currentDetail.detailImages?.[0]" width="100%" height="340" object-fit="cover" class="detail-cover" />
        <div v-if="currentDetail.detailImages?.length" class="detail-thumb-grid">
          <n-image
            v-for="(image, index) in currentDetail.detailImages"
            :key="`${image}-${index}`"
            :src="image"
            width="100%"
            height="120"
            object-fit="cover"
            class="detail-thumb"
          />
        </div>
      </section>

      <section class="detail-main">
        <div class="detail-heading">
          <h3>{{ currentDetail.name }}</h3>
          <p>{{ currentDetail.subtitle || '暂无副标题' }}</p>
        </div>
        <div class="detail-meta">
          <span>起售价 ￥{{ currentDetail.price }}</span>
          <span>总库存 {{ currentDetail.stock }}</span>
          <span>{{ currentDetail.status === 1 ? '上架中' : '已下架' }}</span>
        </div>

        <div class="sku-section">
          <h4>规格列表</h4>
          <div class="sku-card-grid">
            <div v-for="sku in currentDetail.skuList || []" :key="sku.id || sku.skuCode" class="sku-card">
              <n-image v-if="sku.image" :src="sku.image" width="100%" height="136" object-fit="cover" class="sku-card-image" />
              <div class="sku-card-body">
                <strong>{{ sku.skuName }}</strong>
                <p>{{ sku.specValues }}</p>
                <div class="sku-card-meta">
                  <span>￥{{ sku.salePrice }}</span>
                  <span>库存 {{ sku.stock }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="desc-section">
          <h4>图文详情</h4>
          <div class="rich-desc" v-html="currentDetail.description || '<p>暂无商品描述</p>'"></div>
        </div>
      </section>
    </div>
  </n-modal>
</template>

<script setup lang="ts">
import { h, onMounted, reactive, ref, computed } from 'vue'
import {
  NButton,
  NCard,
  NDataTable,
  NImage,
  NInput,
  NModal,
  NPagination,
  NPopconfirm,
  NSelect,
  NSpace,
  useMessage,
  type DataTableColumns,
} from 'naive-ui'
import { api } from '@/api'
import ProductFormModal from '@/components/ProductFormModal.vue'
import { useAuthStore } from '@/stores/auth'
import type { Category, Product, ProductSaveRequest } from '@/types'

const message = useMessage()
const auth = useAuthStore()

const categories = ref<Category[]>([])
const products = ref<Product[]>([])
const showProductModal = ref(false)
const showCategoryModal = ref(false)
const showDetailModal = ref(false)
const editingProduct = ref<Product | null>(null)
const currentDetail = ref<Product | null>(null)

const filters = reactive<{ keyword?: string; categoryId?: number; status?: number }>({})
const pagination = reactive({ page: 1, pageSize: 10, total: 0 })
const categoryForm = reactive<Partial<Category>>({ name: '', description: '', status: 1, sortOrder: 0 })

const categoryOptions = computed(() => categories.value.map((item) => ({ label: item.name, value: item.id })))
const statusOptions = [
  { label: '上架', value: 1 },
  { label: '下架', value: 0 },
]

const columns: DataTableColumns<Product> = [
  {
    title: '商品',
    key: 'name',
    render: (row) => h(NSpace, { align: 'center' }, {
      default: () => [
        row.coverImage ? h(NImage, { src: row.coverImage, width: 58, height: 58, objectFit: 'cover', style: 'border-radius: 12px;' }) : null,
        h('div', null, [
          h('div', { style: 'font-weight: 700' }, row.name),
          h('div', { style: 'font-size: 12px; color: #78716c; margin-top: 6px;' }, row.subtitle || '暂无副标题'),
        ]),
      ],
    }),
  },
  {
    title: '分类',
    key: 'categoryId',
    render: (row) => categories.value.find((item) => item.id === row.categoryId)?.name || '-',
  },
  {
    title: 'SKU数',
    key: 'skuCount',
    render: (row) => row.skuList?.length || 0,
  },
  {
    title: '起售价',
    key: 'price',
    render: (row) => h('span', { class: 'price-text' }, `￥${row.price}`),
  },
  { title: '总库存', key: 'stock' },
  {
    title: '状态',
    key: 'status',
    render: (row) => (row.status === 1 ? '上架' : '下架'),
  },
  {
    title: '操作',
    key: 'actions',
    render: (row) => h(NSpace, null, {
      default: () => [
        h(NButton, { size: 'small', tertiary: !auth.isAdmin, onClick: () => openDetailModal(row.id) }, { default: () => '查看详情' }),
        auth.isAdmin
          ? h(NButton, { size: 'small', onClick: () => openProductModal(row) }, { default: () => '编辑' })
          : h(NButton, { size: 'small', type: 'primary', secondary: true, onClick: () => addToCart(row) }, { default: () => '加入购物车' }),
        auth.isAdmin
          ? h(
              NPopconfirm,
              { onPositiveClick: () => removeProduct(row.id) },
              {
                trigger: () => h(NButton, { size: 'small', type: 'error', secondary: true }, { default: () => '删除' }),
                default: () => '确认删除该商品？',
              },
            )
          : null,
      ],
    }),
  },
]

const categoryColumns: DataTableColumns<Category> = [
  { title: '分类名', key: 'name' },
  { title: '描述', key: 'description' },
  {
    title: '操作',
    key: 'actions',
    render: (row) => h(NSpace, null, {
      default: () => [
        h(NButton, { size: 'small', onClick: () => Object.assign(categoryForm, row) }, { default: () => '编辑' }),
        h(
          NPopconfirm,
          { onPositiveClick: () => removeCategory(row.id) },
          {
            trigger: () => h(NButton, { size: 'small', type: 'error', secondary: true }, { default: () => '删除' }),
            default: () => '确认删除该分类？',
          },
        ),
      ],
    }),
  },
]

onMounted(async () => {
  await Promise.all([loadCategories(), loadProducts()])
})

async function loadCategories() {
  categories.value = await api.listCategories()
}

async function loadProducts() {
  try {
    const page = await api.listProducts({
      pageNum: pagination.page,
      pageSize: pagination.pageSize,
      keyword: filters.keyword,
      categoryId: filters.categoryId,
      status: filters.status,
    })
    products.value = page.records
    pagination.total = page.total
  } catch (error) {
    message.error((error as Error).message)
  }
}

function resetFilters() {
  filters.keyword = undefined
  filters.categoryId = undefined
  filters.status = undefined
  pagination.page = 1
  loadProducts()
}

async function openProductModal(product?: Product) {
  if (product?.id) {
    editingProduct.value = await api.productDetail(product.id)
  } else {
    editingProduct.value = null
  }
  showProductModal.value = true
}

async function openDetailModal(productId: number) {
  try {
    currentDetail.value = await api.productDetail(productId)
    showDetailModal.value = true
  } catch (error) {
    message.error((error as Error).message)
  }
}

async function saveProduct(payload: ProductSaveRequest) {
  try {
    if (editingProduct.value?.id) {
      await api.updateProduct(editingProduct.value.id, payload)
      message.success('商品更新成功')
    } else {
      await api.createProduct(payload)
      message.success('商品创建成功')
    }
    showProductModal.value = false
    await loadProducts()
  } catch (error) {
    message.error((error as Error).message)
  }
}

async function removeProduct(id: number) {
  try {
    await api.deleteProduct(id)
    message.success('商品已删除')
    await loadProducts()
  } catch (error) {
    message.error((error as Error).message)
  }
}

async function addToCart(product: Product) {
  if (!auth.user) return
  try {
    await api.addCart({ userId: auth.user.id, productId: product.id, quantity: 1 })
    message.success('已加入购物车')
  } catch (error) {
    message.error((error as Error).message)
  }
}

async function saveCategory() {
  try {
    if (categoryForm.id) {
      await api.updateCategory(categoryForm.id, categoryForm)
      message.success('分类更新成功')
    } else {
      await api.createCategory(categoryForm)
      message.success('分类创建成功')
    }
    resetCategoryForm()
    await loadCategories()
  } catch (error) {
    message.error((error as Error).message)
  }
}

async function removeCategory(id: number) {
  try {
    await api.deleteCategory(id)
    message.success('分类已删除')
    await loadCategories()
  } catch (error) {
    message.error((error as Error).message)
  }
}

function resetCategoryForm() {
  categoryForm.id = undefined
  categoryForm.name = ''
  categoryForm.description = ''
  categoryForm.status = 1
  categoryForm.sortOrder = 0
}
</script>

<style scoped>
.product-detail-layout {
  display: grid;
  grid-template-columns: minmax(320px, 0.95fr) minmax(0, 1.25fr);
  gap: 24px;
}

.detail-gallery,
.detail-main {
  min-width: 0;
}

.detail-cover,
.detail-thumb,
.sku-card-image {
  overflow: hidden;
  border-radius: 18px;
}

.detail-thumb-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
  margin-top: 12px;
}

.detail-heading h3 {
  margin: 0;
  font-size: 28px;
}

.detail-heading p {
  margin: 10px 0 0;
  color: #6b7280;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin: 18px 0 24px;
}

.detail-meta span {
  padding: 8px 14px;
  border-radius: 999px;
  background: #f3f4f6;
  color: #111827;
  font-size: 13px;
}

.sku-section,
.desc-section {
  margin-top: 24px;
}

.sku-section h4,
.desc-section h4 {
  margin: 0 0 14px;
  font-size: 16px;
}

.sku-card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 14px;
}

.sku-card {
  overflow: hidden;
  border: 1px solid #e5e7eb;
  border-radius: 20px;
  background: #fff;
}

.sku-card-body {
  padding: 14px;
}

.sku-card-body p {
  margin: 8px 0;
  color: #6b7280;
  font-size: 13px;
}

.sku-card-meta {
  display: flex;
  justify-content: space-between;
  color: #111827;
  font-weight: 600;
}

.rich-desc {
  line-height: 1.9;
  color: #111827;
}

.rich-desc :deep(img),
.rich-desc :deep(video) {
  max-width: 100%;
  border-radius: 16px;
  margin: 14px 0;
}

@media (max-width: 900px) {
  .product-detail-layout {
    grid-template-columns: 1fr;
  }
}
</style>
