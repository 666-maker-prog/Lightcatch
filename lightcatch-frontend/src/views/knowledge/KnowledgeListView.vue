<template>
  <div class="knowledge-page" style="padding: 24px; max-width: 1200px; margin: 0 auto;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;">
      <div>
        <h2 style="margin: 0 0 4px;">素材库</h2>
        <p style="color: #64748b; margin: 0; font-size: 14px;">上传素材，AI 在创作时会参考其中的内容风格</p>
      </div>
      <a-button type="primary" @click="openCreate">
        <template #icon><PlusOutlined /></template>
        新建素材库
      </a-button>
    </div>

    <a-row :gutter="[16, 16]">
      <a-col v-for="(kb, idx) in knowledgeStore.knowledgeList" :key="kb.id" :span="8">
        <a-card hoverable class="kb-card">
          <template #cover>
            <div class="kb-cover" :style="{ background: colors[idx % colors.length] }">
              <FolderOutlined style="font-size: 36px; color: white; opacity: 0.5" />
            </div>
          </template>
          <a-card-meta>
            <template #title>
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <span>{{ kb.name }}</span>
                <a-tag v-if="kb.docCount > 0" color="green">{{ kb.docCount }} 篇</a-tag>
              </div>
            </template>
            <template #description>
              {{ kb.description || '暂无描述' }}
            </template>
          </a-card-meta>
          <template #actions>
            <a-button type="link" @click="selectKB(kb)"><FolderOpenOutlined /> 管理文档</a-button>
            <a-button type="link" @click="openEdit(kb)"><EditOutlined /> 编辑</a-button>
            <a-button type="link" danger @click="handleDelete(kb)"><DeleteOutlined /> 删除</a-button>
          </template>
        </a-card>
      </a-col>

      <a-col v-if="!knowledgeStore.loading && knowledgeStore.knowledgeList.length === 0" :span="24">
        <div style="text-align: center; padding: 60px 0;">
          <FolderOutlined style="font-size: 48px; color: #cbd5e1" />
          <p style="margin: 16px 0; color: #94a3b8;">还没有素材库</p>
          <a-button type="primary" @click="openCreate">新建素材库</a-button>
        </div>
      </a-col>
    </a-row>

    <!-- 文档管理弹窗（纯 div，无组件依赖） -->
    <div v-if="showDocModal" class="doc-overlay" @click.self="showDocModal = false">
      <div class="doc-panel">
        <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;">
          <h3 style="margin:0;">{{ currentKB?.name || '素材库' }} - 文档管理</h3>
          <button @click="showDocModal = false" style="background:none;border:none;font-size:20px;cursor:pointer;color:#94a3b8;">✕</button>
        </div>
        <div style="margin-bottom:16px;">
          <label style="display:inline-block;padding:8px 20px;background:#7c3aed;color:white;border-radius:8px;cursor:pointer;font-size:14px;">
            <UploadOutlined style="margin-right:6px;" />上传文档
            <input type="file" accept=".txt,.md,.pdf,.doc,.docx" style="display:none" @change="handleFileSelected" />
          </label>
        </div>
        <div v-if="knowledgeStore.currentDocs.length === 0" style="text-align:center;padding:40px 0;color:#94a3b8;">暂无文档</div>
        <div v-for="doc in knowledgeStore.currentDocs" :key="doc.id" style="display:flex;justify-content:space-between;align-items:center;padding:10px 0;border-bottom:1px solid #f1f5f9;">
          <div>
            <div style="font-weight:500;"><FileTextOutlined style="margin-right:6px;" />{{ doc.title }}</div>
            <div style="font-size:12px;color:#94a3b8;">
              状态: {{ doc.status === 1 ? '已就绪' : doc.status === 2 ? '失败' : '处理中' }}
              | 字数: {{ doc.wordCount || 0 }}
            </div>
          </div>
          <div style="display:flex;gap:4px;">
            <button v-if="doc.status === 2" @click="reEmbed(doc)" style="background:#7c3aed;color:white;border:none;border-radius:4px;padding:4px 8px;cursor:pointer;font-size:12px;">↻ 重新向量化</button>
            <button @click="handleDeleteDoc(doc)" style="background:none;border:none;color:#ef4444;cursor:pointer;font-size:16px;">🗑</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 新建/编辑素材库 Modal -->
    <a-modal
      v-model:open="showEditModal"
      :title="editingKB ? '编辑素材库' : '新建素材库'"
      @ok="handleSaveKB"
      :confirm-loading="savingKB"
    >
      <a-form layout="vertical">
        <a-form-item label="名称" required>
          <a-input v-model:value="editName" placeholder="素材库名称" />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model:value="editDesc" placeholder="描述这个素材库的内容方向" :rows="3" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import {
  PlusOutlined, FolderOutlined, FolderOpenOutlined, EditOutlined,
  DeleteOutlined, FileTextOutlined, UploadOutlined,
} from '@ant-design/icons-vue'
import { useKnowledgeStore } from '@/stores/knowledge'
import { message } from 'ant-design-vue'
import request from '@/utils/request'

const knowledgeStore = useKnowledgeStore()
const showEditModal = ref(false)
const savingKB = ref(false)
const editingKB = ref<any>(null)
const editName = ref('')
const editDesc = ref('')
const showDocModal = ref(false)
const currentKB = ref<any>(null)
const colors = ['#7c3aed', '#0891b2', '#059669', '#d97706', '#dc2626']

function openCreate() {
  editingKB.value = null
  editName.value = ''
  editDesc.value = ''
  showEditModal.value = true
}

function openEdit(kb: any) {
  editingKB.value = kb
  editName.value = kb.name
  editDesc.value = kb.description || ''
  showEditModal.value = true
}

async function handleSaveKB() {
  if (!editName.value.trim()) { message.error('请输入名称'); return }
  savingKB.value = true
  try {
    if (editingKB.value) {
      await request.put('/ai/knowledge/' + editingKB.value.id, { name: editName.value, description: editDesc.value })
      message.success('已更新')
    } else {
      await knowledgeStore.create({ name: editName.value, description: editDesc.value })
      message.success('创建成功')
    }
    showEditModal.value = false
    await knowledgeStore.fetchList()
  } catch (e: any) { message.error(e.message || '操作失败') }
  finally { savingKB.value = false }
}

async function selectKB(kb: any) {
  currentKB.value = kb
  showDocModal.value = true
  await nextTick()
  try {
    await knowledgeStore.fetchDocs(kb.id)
  } catch {}
}

function closeDocModal() {
  showDocModal.value = false
  currentKB.value = null
}

async function handleFileSelected(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file || !currentKB.value) return
  try {
    await knowledgeStore.upload(currentKB.value.id, file)
    message.success('上传成功，正在处理中')
    await knowledgeStore.fetchDocs(currentKB.value.id)
  } catch (e2: any) {
    message.error('上传失败: ' + (e2.message || '请检查后端是否运行'))
  }
  input.value = ''
}

async function handleDelete(kb: any) {
  await knowledgeStore.remove(kb.id)
  message.success('已删除')
}

async function reEmbed(doc: any) {
  try {
    message.loading({ content: '重新向量化中...', key: 'reembed' })
    await request.post(`/ai/knowledge/doc/${doc.id}/re-embed?knowledgeId=${currentKB.value?.id || doc.knowledgeId}`)
    message.destroy('reembed')
    message.success('向量化完成')
    await knowledgeStore.fetchDocs(currentKB.value?.id || doc.knowledgeId)
  } catch (e: any) {
    message.destroy('reembed')
    message.error('向量化失败: ' + (e.message || ''))
  }
}

async function handleDeleteDoc(doc: any) {
  await knowledgeStore.removeDoc(doc.id)
  message.success('文档已删除')
}

onMounted(() => knowledgeStore.fetchList())
</script>

<style scoped>
.kb-card { border-radius: 12px; overflow: hidden; transition: transform 0.2s; }
.kb-card:hover { transform: translateY(-2px); }
.kb-cover { height: 100px; display: flex; align-items: center; justify-content: center; }
.doc-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.4); z-index: 1000; display: flex; align-items: center; justify-content: center; }
.doc-panel { background: white; border-radius: 12px; padding: 24px; width: 700px; max-height: 80vh; overflow-y: auto; box-shadow: 0 20px 60px rgba(0,0,0,0.15); }
</style>
