<template>
  <div class="page-header">
    <div>
      <h2>{{ pageTitle }}</h2>
      <p>{{ pageDescription }}</p>
    </div>
    <n-space>
      <n-button @click="loadUsers">刷新</n-button>
      <n-button v-if="auth.isSuperAdmin" type="primary" @click="openUserModal()">新增用户</n-button>
    </n-space>
  </div>

  <n-card class="page-card" title="用户列表">
    <n-data-table :columns="columns" :data="users" :bordered="false" />
  </n-card>

  <n-drawer v-model:show="showAddressDrawer" width="760">
    <n-drawer-content :title="selectedUser ? `${selectedUser.nickname} 的收货地址` : '收货地址管理'">
      <n-space vertical>
        <n-button type="primary" @click="openAddressModal()">新增地址</n-button>
        <n-data-table :columns="addressColumns" :data="addresses" :bordered="false" />
      </n-space>
    </n-drawer-content>
  </n-drawer>

  <address-form-modal
    v-if="selectedUser"
    v-model:show="showAddressModal"
    :user-id="selectedUser.id"
    :address="editingAddress"
    @submit="saveAddress"
  />

  <n-modal :show="showUserModal" preset="card" :title="editingUser ? '编辑用户' : '新增用户'" style="width:min(760px,96vw)" @update:show="showUserModal = $event">
    <n-form label-placement="top">
      <div class="form-grid">
        <n-form-item label="用户名">
          <n-input v-model:value="userForm.username" :disabled="Boolean(editingUser)" />
        </n-form-item>
        <n-form-item label="昵称">
          <n-input v-model:value="userForm.nickname" />
        </n-form-item>
        <n-form-item label="密码">
          <n-input v-model:value="userForm.password" type="password" show-password-on="click" placeholder="编辑时留空表示不修改" />
        </n-form-item>
        <n-form-item label="手机号">
          <n-input v-model:value="userForm.phone" />
        </n-form-item>
        <n-form-item label="邮箱">
          <n-input v-model:value="userForm.email" />
        </n-form-item>
        <n-form-item label="用户类型">
          <n-select v-model:value="userForm.userType" :options="userTypeOptions" @update:value="handleUserTypeChange" />
        </n-form-item>
        <n-form-item v-if="userForm.userType === 'BACKEND'" label="角色编码">
          <n-select v-model:value="userForm.roleCode" :options="roleOptions" />
        </n-form-item>
        <n-form-item label="状态">
          <n-select v-model:value="userForm.status" :options="statusOptions" />
        </n-form-item>
      </div>
      <n-space justify="end">
        <n-button @click="showUserModal = false">取消</n-button>
        <n-button type="primary" @click="saveUser">保存</n-button>
      </n-space>
    </n-form>
  </n-modal>
</template>

<script setup lang="ts">
import { computed, h, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NButton, NCard, NDataTable, NDrawer, NDrawerContent, NForm, NFormItem, NInput, NModal, NPopconfirm, NSelect, NSpace, NTag, useMessage, type DataTableColumns } from 'naive-ui'
import { api } from '@/api'
import AddressFormModal from '@/components/AddressFormModal.vue'
import { useAuthStore } from '@/stores/auth'
import type { Address, UserProfile } from '@/types'

interface UserFormModel {
  username: string
  nickname: string
  password: string
  phone: string
  email: string
  userType: UserProfile['userType']
  roleCode?: string
  status: number
}

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const message = useMessage()
const users = ref<UserProfile[]>([])
const addresses = ref<Address[]>([])
const showAddressDrawer = ref(false)
const showAddressModal = ref(false)
const selectedUser = ref<UserProfile | null>(null)
const editingAddress = ref<Address | null>(null)
const showUserModal = ref(false)
const editingUser = ref<UserProfile | null>(null)

const currentUserType = computed<UserProfile['userType']>(() => route.meta.userType === 'BACKEND' ? 'BACKEND' : 'FRONTEND')
const pageTitle = computed(() => currentUserType.value === 'BACKEND' ? '后台用户管理' : '前台用户管理')
const pageDescription = computed(() => currentUserType.value === 'BACKEND'
  ? '维护后台账号、角色与状态，只有后台用户参与角色权限控制。'
  : '维护前台会员账号，并可查看订单与收货地址。')

const userForm = reactive<UserFormModel>({
  username: '',
  nickname: '',
  password: '',
  phone: '',
  email: '',
  userType: 'FRONTEND',
  roleCode: undefined,
  status: 1,
})

const userTypeOptions = [
  { label: '后台用户', value: 'BACKEND' },
  { label: '前台用户', value: 'FRONTEND' },
]
const roleOptions = computed(() => [
  { label: '超级管理员', value: 'SUPER_ADMIN' },
  { label: '运营人员', value: 'OPERATOR' },
  { label: '客服人员', value: 'CUSTOMER_SERVICE' },
])
const statusOptions = [
  { label: '启用', value: 1 },
  { label: '禁用', value: 0 },
]

const columns: DataTableColumns<UserProfile> = [
  { title: 'ID', key: 'id' },
  { title: '用户名', key: 'username' },
  { title: '昵称', key: 'nickname' },
  { title: '手机号', key: 'phone' },
  { title: '邮箱', key: 'email' },
  {
    title: '用户类型',
    key: 'userType',
    render: (row) => h(NTag, { type: row.userType === 'BACKEND' ? 'warning' : 'info' }, { default: () => row.userType === 'BACKEND' ? '后台' : '前台' }),
  },
  {
    title: '角色编码',
    key: 'roleCode',
    render: (row) => row.userType === 'BACKEND'
      ? h(NTag, { type: 'success' }, { default: () => row.roleCode || '-' })
      : '-',
  },
  {
    title: '状态',
    key: 'status',
    render: (row) => h(NTag, { type: row.status === 1 ? 'success' : 'error' }, { default: () => (row.status === 1 ? '启用' : '禁用') }),
  },
  {
    title: '快捷操作',
    key: 'actions',
    render: (row) => h(NSpace, null, {
      default: () => [
        row.userType === 'FRONTEND' ? h(NButton, { size: 'small', onClick: () => router.push(`/orders?userId=${row.id}`) }, { default: () => '查看订单' }) : null,
        row.userType === 'FRONTEND' ? h(NButton, { size: 'small', type: 'primary', secondary: true, onClick: () => openAddressDrawer(row) }, { default: () => '地址管理' }) : null,
        auth.isSuperAdmin ? h(NButton, { size: 'small', onClick: () => openUserModal(row) }, { default: () => '编辑用户' }) : null,
      ],
    }),
  },
]

const addressColumns: DataTableColumns<Address> = [
  { title: '收货人', key: 'receiverName' },
  { title: '电话', key: 'receiverPhone' },
  {
    title: '地址',
    key: 'detailAddress',
    render: (row) => `${row.province}${row.city}${row.district || ''}${row.detailAddress}`,
  },
  { title: '默认', key: 'isDefault', render: (row) => (row.isDefault === 1 ? '是' : '否') },
  {
    title: '操作',
    key: 'actions',
    render: (row) => h(NSpace, null, {
      default: () => [
        h(NButton, { size: 'small', onClick: () => openAddressModal(row) }, { default: () => '编辑' }),
        h(
          NPopconfirm,
          { onPositiveClick: () => removeAddress(row.id) },
          {
            trigger: () => h(NButton, { size: 'small', type: 'error', secondary: true }, { default: () => '删除' }),
            default: () => '确认删除该地址？',
          },
        ),
      ],
    }),
  },
]

onMounted(loadUsers)
watch(currentUserType, () => {
  loadUsers()
  showAddressDrawer.value = false
  selectedUser.value = null
})

async function loadUsers() {
  try {
    users.value = await api.listUsers(currentUserType.value)
  } catch (error) {
    message.error((error as Error).message)
  }
}

function handleUserTypeChange(value: UserProfile['userType']) {
  userForm.userType = value
  userForm.roleCode = value === 'BACKEND' ? 'OPERATOR' : undefined
}

function openUserModal(user?: UserProfile) {
  editingUser.value = user ?? null
  Object.assign(userForm, user ? {
    username: user.username,
    nickname: user.nickname,
    password: '',
    phone: user.phone || '',
    email: user.email || '',
    userType: user.userType,
    roleCode: user.userType === 'BACKEND' ? (user.roleCode || 'OPERATOR') : undefined,
    status: user.status,
  } : {
    username: '',
    nickname: '',
    password: '',
    phone: '',
    email: '',
    userType: currentUserType.value,
    roleCode: currentUserType.value === 'BACKEND' ? 'OPERATOR' : undefined,
    status: 1,
  })
  showUserModal.value = true
}

async function saveUser() {
  const payload = {
    username: userForm.username,
    nickname: userForm.nickname,
    password: userForm.password,
    phone: userForm.phone,
    email: userForm.email,
    userType: userForm.userType,
    roleCode: userForm.userType === 'BACKEND' ? userForm.roleCode : undefined,
    status: userForm.status,
  }
  try {
    if (editingUser.value) {
      await api.updateUser(editingUser.value.id, editingUser.value.userType, payload)
      message.success('用户更新成功')
    } else {
      await api.createUser(payload)
      message.success('用户创建成功')
    }
    showUserModal.value = false
    await loadUsers()
  } catch (error) {
    message.error((error as Error).message)
  }
}

async function openAddressDrawer(user: UserProfile) {
  if (user.userType !== 'FRONTEND') return
  selectedUser.value = user
  showAddressDrawer.value = true
  await loadAddresses()
}

function openAddressModal(address?: Address) {
  editingAddress.value = address ?? null
  showAddressModal.value = true
}

async function loadAddresses() {
  if (!selectedUser.value) return
  try {
    addresses.value = await api.listAddresses(selectedUser.value.id)
  } catch (error) {
    message.error((error as Error).message)
  }
}

async function saveAddress(payload: Omit<Address, 'id' | 'status'>) {
  try {
    if (editingAddress.value?.id) {
      await api.updateAddress(editingAddress.value.id, payload)
      message.success('地址更新成功')
    } else {
      await api.createAddress(payload)
      message.success('地址创建成功')
    }
    showAddressModal.value = false
    await loadAddresses()
  } catch (error) {
    message.error((error as Error).message)
  }
}

async function removeAddress(id: number) {
  if (!selectedUser.value) return
  try {
    await api.deleteAddress(id, selectedUser.value.id)
    message.success('地址已删除')
    await loadAddresses()
  } catch (error) {
    message.error((error as Error).message)
  }
}
</script>
