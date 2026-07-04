<template>
  <div style="padding:24px;max-width:1000px;margin:0 auto;">
    <div style="margin-bottom:24px;">
      <h2 style="margin:0 0 4px;">草稿箱</h2>
      <p style="color:#64748b;margin:0;font-size:14px;">工作流执行生成的内容会保存在这里，点击预览和编辑</p>
    </div>

    <div v-if="loading" style="text-align:center;padding:60px 0;"><a-spin size="large" /></div>

    <div v-if="!loading && drafts.length === 0" style="text-align:center;padding:60px 0;color:#94a3b8;">
      <FileTextOutlined style="font-size:48px;color:#cbd5e1;" />
      <p style="margin-top:12px;">还没有草稿</p>
      <p style="font-size:13px;">去工作流生成一篇内容，结果会自动保存到这里</p>
    </div>

    <div v-for="d in drafts" :key="d.id" style="background:white;border-radius:12px;padding:20px;margin-bottom:12px;border:1px solid #f1f5f9;">
      <div style="display:flex;justify-content:space-between;align-items:flex-start;">
        <div style="flex:1;cursor:pointer;" @click="toggleExpand(d.id)">
          <h3 style="margin:0 0 4px;font-size:16px;color:#1e293b;">{{ d.title }}</h3>
          <div style="font-size:12px;color:#94a3b8;display:flex;gap:12px;">
            <span>🕐 生成于 {{ d.createTime }}</span>
            <span v-if="d.updateTime !== d.createTime">✏️ 编辑于 {{ d.updateTime }}</span>
          </div>
          <div v-if="expandedId !== d.id" style="margin-top:8px;font-size:14px;color:#64748b;line-height:1.5;max-height:48px;overflow:hidden;">
            {{ stripMd(d.content || '').slice(0, 120) }}{{ (d.content || '').length > 120 ? '...' : '' }}
          </div>
        </div>
        <div style="display:flex;gap:6px;flex-shrink:0;margin-left:16px;">
          <a-button size="small" type="primary" ghost @click="showPublish(d)"><SendOutlined /> 发布</a-button>
          <a-button size="small" @click="toggleExpand(d.id)">
            {{ expandedId === d.id ? '收起' : '预览' }}
          </a-button>
          <a-button size="small" danger @click="deleteDraft(d)"><DeleteOutlined /> 删除</a-button>
        </div>
      </div>

      <!-- 展开后的内容 -->
      <div v-if="expandedId === d.id" style="margin-top:12px;border-top:1px solid #f1f5f9;padding-top:12px;">
        <div v-if="editingId === d.id">
          <RichTextEditor v-model="editContent" placeholder="编辑内容..." />
          <div style="margin-top:8px;display:flex;gap:8px;justify-content:flex-end;">
            <a-button size="small" @click="cancelEdit">取消</a-button>
            <a-button size="small" type="primary" @click="saveDraft(d)">保存</a-button>
          </div>
        </div>
        <div v-else>
          <div class="prose-content" v-html="renderMd(d.content)"></div>
          <div style="margin-top:12px;">
            <a-button size="small" @click="startEdit(d)"><EditOutlined /> 编辑</a-button>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 发布平台选择弹窗 -->
  <div v-if="showPublishModal" class="doc-overlay" @click.self="showPublishModal = false">
    <div class="doc-panel" style="width:450px;">
      <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;">
        <h3 style="margin:0;">选择发布平台</h3>
        <button @click="showPublishModal = false" style="background:none;border:none;font-size:20px;cursor:pointer;color:#94a3b8;">✕</button>
      </div>
      <div v-for="p in platforms" :key="p.key"
        class="platform-item"
        @click="publishTo(p.key)">
        <span style="font-size:24px;">{{ p.icon }}</span>
        <div>
          <div style="font-weight:500;">{{ p.name }}</div>
          <div style="font-size:12px;color:#94a3b8;">{{ p.desc }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { FileTextOutlined, EditOutlined, DeleteOutlined, SendOutlined } from '@ant-design/icons-vue'
import { getDrafts, deleteDraft as deleteApi, updateDraft } from '@/api/drafts'
import { message } from 'ant-design-vue'
import { marked } from 'marked'
import RichTextEditor from '@/components/editor/RichTextEditor.vue'

const platforms = [
  { key: 'xiaohongshu', name: '小红书', icon: '📕', desc: '发布到小红书笔记' },
  { key: 'gongzhonghao', name: '公众号', icon: '📢', desc: '发布到微信公众号' },
  { key: 'csdn', name: 'CSDN', icon: '💻', desc: '发布到 CSDN 博客' },
  { key: 'douyin', name: '抖音', icon: '🎵', desc: '发布到抖音' },
]

const showPublishModal = ref(false)
const publishDraft = ref<any>(null)

function showPublish(d: any) {
  publishDraft.value = d
  showPublishModal.value = true
}

async function publishTo(platform: string) {
  if (!publishDraft.value) return
  message.success(`已记录发布意向：${platform}，对接开发中`)
  showPublishModal.value = false
}

function renderMd(text: string): string {
  if (!text) return ''
  try {
    const result = marked.parse(text)
    return typeof result === 'string' ? result : text
  } catch { return text }
}

function isMarkdown(text: string) {
  return /[#*`\[\]>\-_|]/.test(text || '')
}

function stripMd(text: string) {
  if (!text) return ''
  return text
    .replace(/^#+\s*/gm, '')
    .replace(/\*\*(.+?)\*\*/g, '$1')
    .replace(/`.*?`/g, '')
    .replace(/[\[\]()>|\\]/g, '')
    .replace(/\n{2,}/g, ' ')
    .trim()
}

const drafts = ref<any[]>([])
const loading = ref(true)
const expandedId = ref<string | null>(null)
const editingId = ref<string | null>(null)
const editContent = ref('')
const previewMd = ref(false)
const editTextarea = ref<HTMLTextAreaElement | null>(null)

function insertMd(before: string, after: string) {
  const ta = editTextarea.value
  if (!ta) return
  const start = ta.selectionStart
  const end = ta.selectionEnd
  const selected = editContent.value.substring(start, end)
  editContent.value = editContent.value.substring(0, start) + before + selected + after + editContent.value.substring(end)
  setTimeout(() => { ta.focus(); ta.setSelectionRange(start + before.length, start + before.length + selected.length) }, 10)
}

async function fetchDrafts() {
  loading.value = true
  try { const res = await getDrafts(); drafts.value = res.data || [] }
  finally { loading.value = false }
}

function toggleExpand(id: string) {
  expandedId.value = expandedId.value === id ? null : id
  editingId.value = null
}

function startEdit(d: any) {
  editingId.value = d.id
  // 如果是 Markdown 内容，转成 HTML 给富文本编辑器
  if (isMarkdown(d.content)) {
    editContent.value = renderMd(d.content)
  } else {
    editContent.value = d.content || ''
  }
}

function cancelEdit() {
  editingId.value = null
  editContent.value = ''
}

async function saveDraft(d: any) {
  try {
    const html = editContent.value
    await updateDraft(d.id, { content: html })
    d.content = html
    message.success('已保存')
    editingId.value = null
  } catch (e: any) {
    message.error(e.message || '保存失败')
  }
}

async function deleteDraft(d: any) {
  await deleteApi(d.id)
  message.success('已删除')
  await fetchDrafts()
}

onMounted(fetchDrafts)
</script>

<style scoped>
.prose-content { font-size:15px; line-height:1.8; color:#334155; }
.prose-content :deep(h1), .prose-content :deep(h2), .prose-content :deep(h3) { margin:16px 0 8px; color:#1e293b; }
.prose-content :deep(p) { margin-bottom:10px; }
.prose-content :deep(ul), .prose-content :deep(ol) { padding-left:20px; margin-bottom:10px; }
.prose-content :deep(code) { background:#f1f5f9; padding:2px 6px; border-radius:4px; font-size:13px; color:#1e293b; }
.prose-content :deep(pre) { background:#1e293b; color:#e2e8f0; padding:16px; border-radius:8px; overflow-x:auto; margin-bottom:12px; }
.prose-content :deep(pre code) { background:none; padding:0; color:#e2e8f0; font-size:13px; }
.prose-content :deep(blockquote) { border-left:4px solid #7c3aed; padding-left:16px; color:#64748b; margin:12px 0; }
.prose-content :deep(img) { max-width:100%; border-radius:8px; }
.prose-content :deep(a) { color:#7c3aed; }
.platform-item { display:flex;align-items:center;gap:12px;padding:12px 16px;margin-bottom:8px;border:1px solid #e2e8f0;border-radius:8px;cursor:pointer;transition:border-color 0.15s; }
.platform-item:hover { border-color:#7c3aed; }
</style>
