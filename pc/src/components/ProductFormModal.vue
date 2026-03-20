<template>
  <n-modal :show="show" preset="card" :title="editing ? '编辑商品' : '新建商品'" style="width: min(1180px, 98vw)" @update:show="emit('update:show', $event)">
    <n-form label-placement="top">
      <div class="product-grid">
        <n-form-item label="商品名称" class="field-name">
          <n-input v-model:value="formValue.name" placeholder="请输入商品名称" />
        </n-form-item>

        <n-form-item label="分类" class="field-category">
          <n-select v-model:value="formValue.categoryId" :options="categoryOptions" placeholder="请选择分类" />
        </n-form-item>

        <n-form-item label="副标题" class="field-subtitle">
          <n-input v-model:value="formValue.subtitle" placeholder="如：高性能 5G 手机" />
        </n-form-item>

        <n-form-item label="状态" class="field-status">
          <n-select v-model:value="formValue.status" :options="statusOptions" />
        </n-form-item>

        <n-form-item label="封面图" class="field-cover">
          <ImageUploadField
            v-model="coverImageValue"
            :max="1"
            upload-text="上传封面"
            compact
          />
        </n-form-item>

        <n-form-item label="商品详情图片" class="field-detail-images">
          <ImageUploadField
            v-model="detailImagesValue"
            :max="6"
            upload-text="上传图片"
            tip="最多 6 张"
            compact
          />
        </n-form-item>
      </div>

      <n-form-item label="商品描述" class="editor-item">
        <WangEditorField
          v-model="descriptionValue"
          placeholder="输入商品图文详情，支持图片和视频上传到 OSS"
          :height="220"
        />
      </n-form-item>

      <div class="sku-header">
        <h3>商品规格</h3>
        <n-button type="primary" secondary @click="addSku">新增规格</n-button>
      </div>

      <n-data-table :columns="skuColumns" :data="formValue.skuList" :bordered="false" />
    </n-form>

    <template #footer>
      <n-space justify="end">
        <n-button @click="emit('update:show', false)">取消</n-button>
        <n-button type="primary" @click="submit">保存</n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup lang="ts">
import { computed, h, reactive, watch } from 'vue'
import {
  NButton,
  NDataTable,
  NForm,
  NFormItem,
  NInput,
  NInputNumber,
  NModal,
  NPopconfirm,
  NSelect,
  NSpace,
  useMessage,
  type DataTableColumns,
} from 'naive-ui'
import ImageUploadField from '@/components/ImageUploadField.vue'
import WangEditorField from '@/components/WangEditorField.vue'
import type { Category, Product, ProductSaveRequest, ProductSku } from '@/types'

const props = defineProps<{
  show: boolean
  product?: Product | null
  categories: Category[]
}>()

const emit = defineEmits<{
  (e: 'update:show', value: boolean): void
  (e: 'submit', value: ProductSaveRequest): void
}>()

const message = useMessage()
const formValue = reactive<ProductSaveRequest>({
  categoryId: 0,
  name: '',
  subtitle: '',
  description: '',
  coverImage: '',
  detailImages: [],
  status: 1,
  skuList: [],
})

const editing = computed(() => Boolean(props.product?.id))
const categoryOptions = computed(() => props.categories.map((item) => ({ label: item.name, value: item.id })))
const statusOptions = [
  { label: '上架', value: 1 },
  { label: '下架', value: 0 },
]
const skuStatusOptions = [
  { label: '启用', value: 1 },
  { label: '停用', value: 0 },
]

const coverImageValue = computed({
  get: () => formValue.coverImage || '',
  set: (value: string | string[]) => {
    formValue.coverImage = Array.isArray(value) ? value[0] || '' : value
  },
})

const detailImagesValue = computed({
  get: () => formValue.detailImages || [],
  set: (value: string | string[]) => {
    formValue.detailImages = Array.isArray(value) ? value : value ? [value] : []
  },
})

const descriptionValue = computed({
  get: () => formValue.description || '',
  set: (value: string) => {
    formValue.description = value
  },
})

const skuColumns: DataTableColumns<ProductSku> = [
  {
    title: '规格图',
    key: 'image',
    width: 132,
    render: (row, index) => h(ImageUploadField, {
      modelValue: row.image || '',
      max: 1,
      compact: true,
      uploadText: '上传',
      'onUpdate:modelValue': (value: string | string[]) => {
        formValue.skuList[index].image = Array.isArray(value) ? value[0] || '' : value
      },
    }),
  },
  {
    title: 'SKU编码',
    key: 'skuCode',
    render: (row, index) => h(NInput, {
      value: row.skuCode,
      placeholder: '如：PHONE-BLACK-256',
      'onUpdate:value': (value: string) => (formValue.skuList[index].skuCode = value),
    }),
  },
  {
    title: 'SKU名称',
    key: 'skuName',
    render: (row, index) => h(NInput, {
      value: row.skuName,
      placeholder: '如：黑色 8G+256G',
      'onUpdate:value': (value: string) => (formValue.skuList[index].skuName = value),
    }),
  },
  {
    title: '规格值',
    key: 'specValues',
    render: (row, index) => h(NInput, {
      value: row.specValues,
      placeholder: '颜色:黑色;内存:8G+256G',
      'onUpdate:value': (value: string) => (formValue.skuList[index].specValues = value),
    }),
  },
  {
    title: '售价',
    key: 'salePrice',
    width: 130,
    render: (row, index) => h(NInputNumber, {
      value: row.salePrice,
      min: 0,
      style: 'width: 120px',
      'onUpdate:value': (value: number | null) => (formValue.skuList[index].salePrice = value || 0),
    }),
  },
  {
    title: '库存',
    key: 'stock',
    width: 130,
    render: (row, index) => h(NInputNumber, {
      value: row.stock,
      min: 0,
      style: 'width: 120px',
      'onUpdate:value': (value: number | null) => (formValue.skuList[index].stock = value || 0),
    }),
  },
  {
    title: '状态',
    key: 'status',
    width: 120,
    render: (row, index) => h(NSelect, {
      value: row.status,
      options: skuStatusOptions,
      style: 'width: 110px',
      'onUpdate:value': (value: number) => (formValue.skuList[index].status = value),
    }),
  },
  {
    title: '操作',
    key: 'actions',
    width: 90,
    render: (_, index) => h(NPopconfirm, {
      onPositiveClick: () => formValue.skuList.splice(index, 1),
    }, {
      trigger: () => h(NButton, { size: 'small', type: 'error', secondary: true }, { default: () => '删除' }),
      default: () => '确认删除该规格？',
    }),
  },
]

watch(
  () => props.show,
  (visible) => {
    if (!visible) return
    Object.assign(formValue, {
      categoryId: props.product?.categoryId || 0,
      name: props.product?.name || '',
      subtitle: props.product?.subtitle || '',
      description: props.product?.description || '',
      coverImage: props.product?.coverImage || '',
      detailImages: props.product?.detailImages ? [...props.product.detailImages] : [],
      status: props.product?.status ?? 1,
      skuList: props.product?.skuList?.length ? props.product.skuList.map((item) => ({ ...item })) : [createEmptySku()],
    })
  },
  { immediate: true },
)

function createEmptySku(): ProductSku {
  return {
    skuCode: '',
    skuName: '',
    specValues: '',
    image: '',
    salePrice: 0,
    stock: 0,
    status: 1,
  }
}

function addSku() {
  formValue.skuList.push(createEmptySku())
}

function submit() {
  if (!formValue.coverImage) {
    message.warning('请上传封面图')
    return
  }
  emit('submit', {
    id: props.product?.id,
    categoryId: formValue.categoryId,
    name: formValue.name,
    subtitle: formValue.subtitle,
    description: formValue.description,
    coverImage: formValue.coverImage,
    detailImages: [...(formValue.detailImages || [])],
    status: formValue.status,
    skuList: formValue.skuList.map((item) => ({ ...item })),
  })
}
</script>

<style scoped>
.product-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
  align-items: start;
}

.product-grid :deep(.n-form-item) {
  margin-bottom: 12px;
}

.field-name,
.field-category,
.field-subtitle,
.field-status,
.field-cover,
.field-detail-images {
  grid-column: auto;
}

.editor-item {
  margin-bottom: 12px;
}

.editor-item :deep(.n-form-item-blank),
.editor-item :deep(.n-form-item-feedback-wrapper) {
  width: 100%;
}

.sku-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 4px 0 10px;
}

.sku-header h3 {
  margin: 0;
}

@media (max-width: 640px) {
  .product-grid {
    grid-template-columns: 1fr;
  }
}
</style>

