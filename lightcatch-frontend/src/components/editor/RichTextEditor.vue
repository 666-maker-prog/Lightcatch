<template>
  <div class="editor-wrapper" @mousedown.stop>
    <!-- 工具栏 -->
    <div class="editor-toolbar" v-if="!readonly">
      <div class="toolbar-group">
        <button @click="toggleBold" :class="{ active: editor?.isActive('bold') }" title="加粗"><b>B</b></button>
        <button @click="toggleItalic" :class="{ active: editor?.isActive('italic') }" title="斜体"><i>I</i></button>
        <button @click="toggleUnderline" :class="{ active: editor?.isActive('underline') }" title="下划线"><u>U</u></button>
        <button @click="toggleStrike" :class="{ active: editor?.isActive('strike') }" title="删除线"><s>S</s></button>
      </div>
      <div class="toolbar-group">
        <button @click="toggleHeading(1)" :class="{ active: editor?.isActive('heading', { level: 1 }) }" title="大标题">H1</button>
        <button @click="toggleHeading(2)" :class="{ active: editor?.isActive('heading', { level: 2 }) }" title="中标题">H2</button>
        <button @click="toggleHeading(3)" :class="{ active: editor?.isActive('heading', { level: 3 }) }" title="小标题">H3</button>
      </div>
      <div class="toolbar-group">
        <button @click="toggleBulletList" :class="{ active: editor?.isActive('bulletList') }" title="无序列表">≡</button>
        <button @click="toggleOrderedList" :class="{ active: editor?.isActive('orderedList') }" title="有序列表">#</button>
        <button @click="toggleBlockquote" :class="{ active: editor?.isActive('blockquote') }" title="引用">❝</button>
      </div>
      <div class="toolbar-group">
        <button @click="setLink" :class="{ active: editor?.isActive('link') }" title="插入链接">🔗</button>
        <button v-if="editor?.isActive('link')" @click="openLink" title="打开链接">↗</button>
        <button v-if="editor?.isActive('link')" @click="unsetLink" title="取消链接">✕</button>
        <button @click="addImage" title="插入图片">🖼</button>
      </div>
      <div class="toolbar-group">
        <input type="color" v-model="textColor" @change="setColor" title="文字颜色" style="width:24px;height:24px;border:none;cursor:pointer;padding:0;" />
      </div>
    </div>

    <!-- 编辑区 -->
    <editor-content :editor="editor" class="editor-content" />
  </div>
</template>

<script setup lang="ts">
import { useEditor, EditorContent } from '@tiptap/vue-3'
import StarterKit from '@tiptap/starter-kit'
import Link from '@tiptap/extension-link'
import ImageExt from '@tiptap/extension-image'
import Underline from '@tiptap/extension-underline'
import TextAlign from '@tiptap/extension-text-align'
import Color from '@tiptap/extension-color'
import Placeholder from '@tiptap/extension-placeholder'
import { ref, watch, h } from 'vue'
import { message, Modal } from 'ant-design-vue'

const props = defineProps<{
  modelValue: string
  readonly?: boolean
  placeholder?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()

const textColor = ref('#000000')

const editor = useEditor({
  content: props.modelValue,
  extensions: [
    StarterKit,
    Underline,
    Link.configure({ openOnClick: false }),
    ImageExt.configure({ inline: false }),
    TextAlign.configure({ types: ['heading', 'paragraph'] }),
    Color,
    Placeholder.configure({ placeholder: props.placeholder || '开始创作...' }),
  ],
  onUpdate: ({ editor }) => {
    emit('update:modelValue', editor.getHTML())
  },
})

watch(() => props.modelValue, (val) => {
  if (editor.value && val !== editor.value.getHTML()) {
    editor.value.commands.setContent(val)
  }
})

function toggleBold() { editor.value?.chain().focus().toggleBold().run() }
function toggleItalic() { editor.value?.chain().focus().toggleItalic().run() }
function toggleUnderline() { editor.value?.chain().focus().toggleUnderline().run() }
function toggleStrike() { editor.value?.chain().focus().toggleStrike().run() }
function toggleHeading(level: number) { editor.value?.chain().focus().toggleHeading({ level: level as any }).run() }
function toggleBulletList() { editor.value?.chain().focus().toggleBulletList().run() }
function toggleOrderedList() { editor.value?.chain().focus().toggleOrderedList().run() }
function toggleBlockquote() { editor.value?.chain().focus().toggleBlockquote().run() }

function openLink() {
  const href = editor.value?.getAttributes('link').href
  if (href) window.open(href, '_blank')
}
function unsetLink() { editor.value?.chain().focus().unsetLink().run() }

function setColor() {
  editor.value?.chain().focus().setColor(textColor.value).run()
}

function setLink() {
  if (!editor.value) return
  // 检查是否选中了文字
  if (editor.value.state.selection.empty) {
    message.warning('请先选中要加链接的文字')
    return
  }
  let url = ''
  Modal.confirm({
    title: '插入链接',
    content: h('input', {
      style: 'width:100%;padding:8px;border:1px solid #e2e8f0;border-radius:6px;margin-top:8px;',
      placeholder: 'https://...',
      value: url,
      onInput: (e: any) => { url = e.target.value }
    }),
    onOk: () => {
      if (url) editor.value?.chain().focus().setLink({ href: url }).run()
    }
  })
}

function addImage() {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.onchange = async () => {
    const file = input.files?.[0]
    if (!file) return
    const formData = new FormData()
    formData.append('file', file)
    try {
      const res = await fetch('/api/upload/image', { method: 'POST', body: formData })
      const json = await res.json()
      if (json.success) {
        editor.value?.chain().focus().setImage({ src: json.data }).run()
      } else {
        message.error(json.message || '上传失败')
      }
    } catch { message.error('上传失败') }
  }
  input.click()
}
</script>

<style scoped>
.editor-wrapper { border: 1px solid #e2e8f0; border-radius: 8px; overflow: hidden; }
.editor-toolbar {
  display: flex; flex-wrap: wrap; gap: 4px; padding: 8px;
  border-bottom: 1px solid #e2e8f0; background: #f8fafc;
}
.toolbar-group { display: flex; gap: 2px; padding: 0 4px; border-right: 1px solid #e2e8f0; }
.toolbar-group:last-child { border-right: none; }
.editor-toolbar button {
  width: 32px; height: 32px; border: none; background: transparent;
  border-radius: 4px; cursor: pointer; font-size: 14px;
  display: flex; align-items: center; justify-content: center;
}
.editor-toolbar button:hover { background: #e2e8f0; }
.editor-toolbar button.active { background: #7c3aed; color: white; }
.editor-content { padding: 16px; min-height: 200px; }
.editor-content :deep(.ProseMirror) { outline: none; min-height: 200px; }
.editor-content :deep(.ProseMirror p.is-editor-empty:first-child::before) {
  color: #94a3b8; content: attr(data-placeholder); float: left; height: 0; pointer-events: none;
}
</style>
