<template>
  <div class="page-header">
    <div>
      <h2>系统参数与业务参数</h2>
      <p>统一维护平台配置。OSS 改为从数据库读取，并在读取后写入 Redis 缓存。</p>
    </div>
    <n-space>
      <n-button @click="loadParams">刷新</n-button>
      <n-button type="primary" @click="openModal()">新增参数</n-button>
    </n-space>
  </div>

  <n-card class="page-card">
    <n-space vertical size="large">
      <n-space justify="space-between" wrap>
        <n-tabs v-model:value="activeType" type="segment" @update:value="handleTypeChange">
          <n-tab-pane name="SYSTEM" tab="系统参数" />
          <n-tab-pane name="BUSINESS" tab="业务参数" />
        </n-tabs>
        <n-input v-model:value="keyword" clearable placeholder="搜索参数名称 / 参数键 / 分组" style="width: 320px" @keyup.enter="loadParams">
          <template #suffix>
            <n-button text @click="loadParams">搜索</n-button>
          </template>
        </n-input>
      </n-space>

      <n-data-table :columns="columns" :data="params" :bordered="false" />
    </n-space>
  </n-card>

  <n-modal :show="showModal" preset="card" :title="editingItem ? '编辑参数' : '新增参数'" style="width:min(760px,96vw)" @update:show="showModal = $event">
    <n-form label-placement="top">
      <div class="form-grid">
        <n-form-item label="参数类型">
          <n-select v-model:value="form.paramType" :options="typeOptions" />
        </n-form-item>
        <n-form-item label="参数分组">
          <n-input v-model:value="form.paramGroup" placeholder="例如 oss / shop / order" />
        </n-form-item>
        <n-form-item label="参数名称">
          <n-input v-model:value="form.paramName" />
        </n-form-item>
        <n-form-item label="参数键">
          <n-input v-model:value="form.paramKey" placeholder="例如 oss.endpoint" />
        </n-form-item>
        <n-form-item label="值类型">
          <n-select v-model:value="form.valueType" :options="valueTypeOptions" />
        </n-form-item>
        <n-form-item label="状态">
          <n-select v-model:value="form.status" :options="statusOptions" />
        </n-form-item>
      </div>

      <n-form-item label="参数值">
        <n-input
          v-if="form.valueType === 'TEXT'"
          v-model:value="form.paramValue"
          type="textarea"
          :autosize="{ minRows: 4, maxRows: 8 }"
        />
        <n-input
          v-else-if="form.valueType === 'PASSWORD'"
          v-model:value="form.paramValue"
          type="password"
          show-password-on="click"
          placeholder="敏感值会写入数据库并同步 Redis"
        />
        <n-input v-else v-model:value="form.paramValue" />
      </n-form-item>

      <n-form-item label="备注">
        <n-input v-model:value="form.remark" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" />
      </n-form-item>

      <n-space justify="end">
        <n-button @click="showModal = false">取消</n-button>
        <n-button type="primary" @click="saveParam">保存</n-button>
      </n-space>
    </n-form>
  </n-modal>
</template>

<script setup lang="ts">
import { computed, h, onMounted, reactive, ref } from 'vue'
import {
  NButton,
  NCard,
  NDataTable,
  NForm,
  NFormItem,
  NInput,
  NModal,
  NPopconfirm,
  NSelect,
  NSpace,
  NTabPane,
  NTabs,
  NTag,
  useMessage,
  type DataTableColumns,
} from 'naive-ui'
import { api } from '@/api'
import type { SystemParamConfig } from '@/types'

const message = useMessage()
const activeType = ref<'SYSTEM' | 'BUSINESS'>('SYSTEM')
const keyword = ref('')
const params = ref<SystemParamConfig[]>([])
const showModal = ref(false)
const editingItem = ref<SystemParamConfig | null>(null)

const form = reactive<Omit<SystemParamConfig, 'id' | 'createTime' | 'updateTime'>>({
  paramType: 'SYSTEM',
  paramGroup: '',
  paramName: '',
  paramKey: '',
  paramValue: '',
  valueType: 'STRING',
  remark: '',
  status: 1,
})

const typeOptions = [
  { label: '系统参数', value: 'SYSTEM' },
  { label: '业务参数', value: 'BUSINESS' },
]
const valueTypeOptions = [
  { label: '字符串', value: 'STRING' },
  { label: '长文本', value: 'TEXT' },
  { label: '密码', value: 'PASSWORD' },
  { label: '数字', value: 'NUMBER' },
  { label: '布尔值', value: 'BOOLEAN' },
]
const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 },
]

const columns = computed<DataTableColumns<SystemParamConfig>>(() => [
  { title: 'ID', key: 'id', width: 70 },
  {
    title: '类型',
    key: 'paramType',
    width: 100,
    render: (row) => h(NTag, { type: row.paramType === 'SYSTEM' ? 'warning' : 'info' }, { default: () => row.paramType === 'SYSTEM' ? '系统' : '业务' }),
  },
  { title: '分组', key: 'paramGroup', width: 120, render: (row) => row.paramGroup || '-' },
  { title: '参数名称', key: 'paramName', width: 180 },
  { title: '参数键', key: 'paramKey', width: 220 },
  {
    title: '参数值',
    key: 'paramValue',
    render: (row) => maskValue(row),
  },
  {
    title: '状态',
    key: 'status',
    width: 90,
    render: (row) => h(NTag, { type: row.status === 1 ? 'success' : 'default' }, { default: () => row.status === 1 ? '启用' : '禁用' }),
  },
  { title: '备注', key: 'remark', render: (row) => row.remark || '-' },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    render: (row) => h(NSpace, null, {
      default: () => [
        h(NButton, { size: 'small', onClick: () => openModal(row) }, { default: () => '编辑' }),
        h(
          NPopconfirm,
          { onPositiveClick: () => removeParam(row.id) },
          {
            trigger: () => h(NButton, { size: 'small', type: 'error', secondary: true }, { default: () => '删除' }),
            default: () => '删除后将同步清理 Redis 缓存，确认继续？',
          },
        ),
      ],
    }),
  },
])

onMounted(loadParams)

function maskValue(row: SystemParamConfig) {
  if (row.valueType === 'PASSWORD' || row.paramKey.toLowerCase().includes('secret') || row.paramKey.toLowerCase().includes('key')) {
    return row.paramValue ? '******' : '-'
  }
  if (!row.paramValue) {
    return '-'
  }
  return row.paramValue.length > 80 ? `${row.paramValue.slice(0, 80)}...` : row.paramValue
}

function openModal(item?: SystemParamConfig) {
  editingItem.value = item ?? null
  Object.assign(form, item ? {
    paramType: item.paramType,
    paramGroup: item.paramGroup || '',
    paramName: item.paramName,
    paramKey: item.paramKey,
    paramValue: item.paramValue || '',
    valueType: item.valueType,
    remark: item.remark || '',
    status: item.status,
  } : {
    paramType: activeType.value,
    paramGroup: '',
    paramName: '',
    paramKey: '',
    paramValue: '',
    valueType: 'STRING',
    remark: '',
    status: 1,
  })
  showModal.value = true
}

async function loadParams() {
  try {
    params.value = await api.listSystemParams({ paramType: activeType.value, keyword: keyword.value || undefined })
  } catch (error) {
    message.error((error as Error).message)
  }
}

async function saveParam() {
  const payload = {
    paramType: form.paramType,
    paramGroup: form.paramGroup || '',
    paramName: form.paramName,
    paramKey: form.paramKey,
    paramValue: form.paramValue || '',
    valueType: form.valueType,
    remark: form.remark || '',
    status: form.status,
  }
  try {
    if (editingItem.value) {
      await api.updateSystemParam(editingItem.value.id, payload)
      message.success('参数更新成功，Redis 已同步')
    } else {
      await api.createSystemParam(payload)
      message.success('参数创建成功')
    }
    showModal.value = false
    await loadParams()
  } catch (error) {
    message.error((error as Error).message)
  }
}

async function removeParam(id: number) {
  try {
    await api.deleteSystemParam(id)
    message.success('参数已删除，Redis 缓存已清理')
    await loadParams()
  } catch (error) {
    message.error((error as Error).message)
  }
}

function handleTypeChange(value: 'SYSTEM' | 'BUSINESS') {
  activeType.value = value
  loadParams()
}
</script>

<style scoped>
.form-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 0 16px;
}

@media (max-width: 768px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
</style>
