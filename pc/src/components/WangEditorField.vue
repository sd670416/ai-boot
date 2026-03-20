<template>
  <div class="wang-editor-field">
    <div ref="toolbarRef" class="editor-toolbar"></div>
    <div ref="editorRef" class="editor-body" :style="editorBodyStyle"></div>
  </div>
</template>

<script setup lang="ts">
import { createEditor, createToolbar, type IDomEditor } from '@wangeditor/editor'
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { api } from '@/api'

const props = withDefaults(defineProps<{
  modelValue?: string
  placeholder?: string
  height?: number
}>(), {
  modelValue: '',
  placeholder: '请输入内容',
  height: 220,
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()

const toolbarRef = ref<HTMLDivElement | null>(null)
const editorRef = ref<HTMLDivElement | null>(null)
const editorBodyStyle = computed(() => ({ minHeight: `${props.height}px` }))
let editor: IDomEditor | null = null
let syncingFromEditor = false

async function uploadFile(file: File) {
  const result = await api.uploadFile(file)
  return result.url
}

function createInstance() {
  if (!editorRef.value || !toolbarRef.value) return

  editor = createEditor({
    selector: editorRef.value,
    html: props.modelValue || '',
    config: {
      placeholder: props.placeholder,
      hoverbarKeys: {},
      MENU_CONF: {
        uploadImage: {
          async customUpload(file: File, insertFn: (url: string, alt?: string, href?: string) => void) {
            const url = await uploadFile(file)
            insertFn(url, file.name, url)
          },
        },
        uploadVideo: {
          async customUpload(file: File, insertFn: (url: string, poster?: string) => void) {
            const url = await uploadFile(file)
            insertFn(url, '')
          },
        },
      },
      onChange(currentEditor: IDomEditor) {
        syncingFromEditor = true
        emit('update:modelValue', currentEditor.getHtml())
        nextTick(() => {
          syncingFromEditor = false
        })
      },
    },
    mode: 'default',
  })

  createToolbar({
    editor,
    selector: toolbarRef.value,
    config: {
      toolbarKeys: [
        'bold',
        'italic',
        'underline',
        'through',
        '|',
        'bulletedList',
        'numberedList',
        '|',
        'insertImage',
        'insertVideo',
        '|',
        'undo',
        'redo',
      ],
    },
    mode: 'default',
  })
}

onMounted(() => {
  createInstance()
})

watch(
  () => props.modelValue,
  (value) => {
    if (!editor || syncingFromEditor) return
    const currentHtml = editor.getHtml()
    const nextHtml = value || ''
    if (currentHtml !== nextHtml) {
      editor.setHtml(nextHtml)
    }
  },
)

onBeforeUnmount(() => {
  if (editor) {
    editor.destroy()
    editor = null
  }
})
</script>

<style scoped>
.wang-editor-field {
  width: 100%;
  overflow: hidden;
  border: 1px solid #d6dbe3;
  border-radius: 16px;
  background: #fff;
}

.editor-toolbar {
  border-bottom: 1px solid #eef1f5;
}

:deep(.w-e-text-container),
:deep(.w-e-scroll) {
  min-height: inherit;
}
</style>


