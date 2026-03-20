<template>
  <div class="page-header">
    <div>
      <h2>收货地址</h2>
      <p>维护默认地址和多地址切换，下单时可直接复用。</p>
    </div>
    <n-button type="primary" @click="openModal()">新增地址</n-button>
  </div>

  <n-grid cols="1 m:2" responsive="screen" :x-gap="16" :y-gap="16">
    <n-grid-item v-for="item in addresses" :key="item.id">
      <n-card :title="item.receiverName" style="border-radius: 18px;">
        <template #header-extra>
          <n-tag v-if="item.isDefault === 1" type="success">默认</n-tag>
        </template>
        <n-space vertical size="small">
          <div>联系电话：{{ item.receiverPhone }}</div>
          <div>地区：{{ item.province }} {{ item.city }} {{ item.district }}</div>
          <div>详细地址：{{ item.detailAddress }}</div>
          <div>邮编：{{ item.postalCode || '-' }}</div>
          <n-space>
            <n-button size="small" @click="openModal(item)">编辑</n-button>
            <n-popconfirm @positive-click="removeAddress(item.id)">
              <template #trigger>
                <n-button size="small" type="error" secondary>删除</n-button>
              </template>
              确认删除该地址？
            </n-popconfirm>
          </n-space>
        </n-space>
      </n-card>
    </n-grid-item>
  </n-grid>

  <address-form-modal
    v-if="auth.user"
    v-model:show="showModal"
    :user-id="auth.user.id"
    :address="editingAddress"
    @submit="saveAddress"
  />
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { NButton, NCard, NGrid, NGridItem, NPopconfirm, NSpace, NTag, useMessage } from 'naive-ui'
import { api } from '@/api'
import AddressFormModal from '@/components/AddressFormModal.vue'
import { useAuthStore } from '@/stores/auth'
import type { Address } from '@/types'

const auth = useAuthStore()
const message = useMessage()
const addresses = ref<Address[]>([])
const showModal = ref(false)
const editingAddress = ref<Address | null>(null)

onMounted(loadAddresses)

async function loadAddresses() {
  if (!auth.user) return
  try {
    addresses.value = await api.listAddresses(auth.user.id)
  } catch (error) {
    message.error((error as Error).message)
  }
}

function openModal(address?: Address) {
  editingAddress.value = address ?? null
  showModal.value = true
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
    showModal.value = false
    await loadAddresses()
  } catch (error) {
    message.error((error as Error).message)
  }
}

async function removeAddress(id: number) {
  if (!auth.user) return
  try {
    await api.deleteAddress(id, auth.user.id)
    message.success('地址已删除')
    await loadAddresses()
  } catch (error) {
    message.error((error as Error).message)
  }
}
</script>
