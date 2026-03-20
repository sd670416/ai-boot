<template>
  <div class="image-upload-field" :class="{ 'is-compact': compact }">
    <n-upload
      list-type="image-card"
      accept="image/*"
      :max="max"
      :file-list="fileList"
      :custom-request="handleUpload"
      @remove="handleRemove"
    >
      {{ uploadText }}
    </n-upload>
    <p v-if="tip" class="field-tip">{{ tip }}</p>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { NUpload, useMessage, type UploadCustomRequestOptions, type UploadFileInfo } from 'naive-ui'
import { api } from '@/api'

const props = withDefaults(defineProps<{
  modelValue: string | string[] | undefined
  max?: number
  tip?: string
  uploadText?: string
  compact?: boolean
}>(), {
  max: 1,
  tip: '',
  uploadText: '上传图片',
  compact: false,
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string | string[]): void
}>()

const message = useMessage()

const isMultiple = computed(() => props.max > 1)
const normalizedValue = computed<string[]>(() => {
  if (Array.isArray(props.modelValue)) return props.modelValue
  if (typeof props.modelValue === 'string' && props.modelValue) return [props.modelValue]
  return []
})

const fileList = computed<UploadFileInfo[]>(() => normalizedValue.value.map((url, index) => ({
  id: `upload-${index}`,
  name: `image-${index + 1}`,
  status: 'finished',
  url,
})))

async function handleUpload(options: UploadCustomRequestOptions) {
  const file = options.file.file
  if (!file) return
  try {
    const result = await api.uploadFile(file)
    const nextValue = isMultiple.value ? [...normalizedValue.value, result.url] : result.url
    emit('update:modelValue', nextValue)
    options.onFinish?.()
    message.success('图片上传成功')
  } catch (error) {
    options.onError?.()
    message.error((error as Error).message)
  }
}

function handleRemove(options: { file: UploadFileInfo }) {
  const nextList = normalizedValue.value.filter((url) => url !== options.file.url)
  emit('update:modelValue', isMultiple.value ? nextList : '')
  return true
}
</script>

<style scoped>
.image-upload-field {
  min-width: 0;
}

.field-tip {
  margin: 6px 0 0;
  font-size: 12px;
  color: #6b7280;
}

:deep(.n-upload-file-list) {
  gap: 8px;
}

:deep(.n-upload-trigger),
:deep(.n-upload-file) {
  width: 96px;
  height: 96px;
}

.is-compact :deep(.n-upload-trigger),
.is-compact :deep(.n-upload-file) {
  width: 88px;
  height: 88px;
}
</style>
