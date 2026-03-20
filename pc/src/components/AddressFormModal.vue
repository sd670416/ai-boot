<template>
  <n-modal :show="show" preset="card" :title="editing ? '编辑地址' : '新增地址'" style="width: min(760px, 96vw)" @update:show="emit('update:show', $event)">
    <n-form label-placement="top">
      <div class="form-grid">
        <n-form-item label="收货人">
          <n-input v-model:value="formValue.receiverName" />
        </n-form-item>
        <n-form-item label="联系电话">
          <n-input v-model:value="formValue.receiverPhone" />
        </n-form-item>
        <n-form-item label="省份">
          <n-input v-model:value="formValue.province" />
        </n-form-item>
        <n-form-item label="城市">
          <n-input v-model:value="formValue.city" />
        </n-form-item>
        <n-form-item label="区县">
          <n-input v-model:value="formValue.district" />
        </n-form-item>
        <n-form-item label="邮编">
          <n-input v-model:value="formValue.postalCode" />
        </n-form-item>
      </div>
      <n-form-item label="详细地址">
        <n-input v-model:value="formValue.detailAddress" type="textarea" :autosize="{ minRows: 2, maxRows: 4 }" />
      </n-form-item>
      <n-form-item>
        <n-checkbox :checked="formValue.isDefault === 1" @update:checked="formValue.isDefault = $event ? 1 : 0">设为默认地址</n-checkbox>
      </n-form-item>
    </n-form>
    <template #footer>
      <n-space justify="end">
        <n-button @click="emit('update:show', false)">取消</n-button>
        <n-button type="primary" @click="emit('submit', { ...formValue })">保存</n-button>
      </n-space>
    </template>
  </n-modal>
</template>

<script setup lang="ts">
import { reactive, watch, computed } from 'vue'
import { NButton, NCheckbox, NForm, NFormItem, NInput, NModal, NSpace } from 'naive-ui'
import type { Address } from '@/types'

const props = defineProps<{
  show: boolean
  userId: number
  address?: Address | null
}>()

const emit = defineEmits<{
  (e: 'update:show', value: boolean): void
  (e: 'submit', value: Omit<Address, 'id' | 'status'>): void
}>()

const formValue = reactive<Omit<Address, 'id' | 'status'>>({
  userId: props.userId,
  receiverName: '',
  receiverPhone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  postalCode: '',
  isDefault: 0,
})

const editing = computed(() => Boolean(props.address?.id))

watch(
  () => props.show,
  (visible) => {
    if (!visible) return
    Object.assign(formValue, props.address || {
      userId: props.userId,
      receiverName: '',
      receiverPhone: '',
      province: '',
      city: '',
      district: '',
      detailAddress: '',
      postalCode: '',
      isDefault: 0,
    })
    formValue.userId = props.userId
  },
  { immediate: true },
)
</script>
