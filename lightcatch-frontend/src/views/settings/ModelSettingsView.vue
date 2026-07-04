<template>
  <div style="padding: 24px; max-width: 1100px; margin: 0 auto;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px;">
      <div>
        <h2 style="margin: 0 0 4px;">模型管理</h2>
        <p style="color: #64748b; margin: 0; font-size: 14px;">管理 AI 模型供应商和密钥，支持对话、向量嵌入、图片、语音、视频</p>
      </div>
      <a-button type="primary" @click="openCreate">
        <template #icon><PlusOutlined /></template>添加模型
      </a-button>
    </div>

    <!-- 按类型分组 -->
    <a-tabs v-model:activeKey="activeTab" style="margin-bottom:16px;">
      <a-tab-pane key="all" tab="全部" />
      <a-tab-pane key="chat" tab="💬 对话" />
      <a-tab-pane key="embedding" tab="📐 向量嵌入" />
      <a-tab-pane key="image" tab="🎨 图片" />
      <a-tab-pane key="voice" tab="🔊 语音" />
      <a-tab-pane key="video" tab="🎬 视频" />
    </a-tabs>
    <a-table :data-source="filteredModels" :columns="columns" row-key="id" :loading="loading" :pagination="false" size="small">
      <template #bodyCell="{ column, record }">
        <template v-if="column.key === 'modelType'">
          <a-tag :color="typeColor(record.modelType)">{{ typeLabel(record.modelType) }}</a-tag>
        </template>
        <template v-if="column.key === 'status'">
          <a-switch :checked="record.status === 1" @change="toggleStatus(record)" size="small" />
        </template>
        <template v-if="column.key === 'default'">
          <a-tag v-if="record.isDefault" color="gold">默认</a-tag>
          <a-button v-else size="small" type="link" @click="setDefault(record)">设为默认</a-button>
        </template>
        <template v-if="column.key === 'actions'">
          <a-space size="small">
            <a-button size="small" @click="testModel(record)">
              <template #icon><ExperimentOutlined /></template>测试
            </a-button>
            <a-button size="small" @click="editModel(record)">
              <template #icon><EditOutlined /></template>
            </a-button>
            <a-button size="small" danger @click="deleteModel(record)">
              <template #icon><DeleteOutlined /></template>
            </a-button>
          </a-space>
        </template>
      </template>
    </a-table>

    <!-- 添加/编辑 Modal -->
    <a-modal v-model:open="showModal" :title="editing ? '编辑模型' : '添加模型'" @ok="handleSubmit" :confirm-loading="submitting" width="640px">
      <a-form layout="vertical">
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="名称" required>
              <a-input v-model:value="form.name" placeholder="如: DeepSeek V3" />
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="模型类型" required>
              <a-select v-model:value="form.modelType" @change="onTypeChange">
                <a-select-option value="chat">💬 对话模型（DeepSeek/GPT/通义）</a-select-option>
                <a-select-option value="embedding">📐 向量嵌入模型（Embedding）</a-select-option>
                <a-select-option value="image">🎨 图片生成（文生图/图生图）</a-select-option>
                <a-select-option value="voice">🔊 语音合成（TTS）</a-select-option>
                <a-select-option value="video">🎬 视频生成</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="12">
            <a-form-item label="供应商" required>
              <a-select v-model:value="form.provider" @change="onProviderChange">
                <a-select-option value="DeepSeek">DeepSeek</a-select-option>
                <a-select-option value="OpenAI">OpenAI</a-select-option>
                <a-select-option value="Qwen">通义千问（阿里）</a-select-option>
                <a-select-option value="Zhipu">智谱 AI</a-select-option>
                <a-select-option value="Ollama">Ollama（本地）</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="模型名" required>
              <a-input v-model:value="form.modelName" :placeholder="modelPlaceholder" />
            </a-form-item>
          </a-col>
        </a-row>
        <a-form-item label="API Key" required>
          <a-input-password v-model:value="form.apiKey" placeholder="sk-..." />
        </a-form-item>
        <a-form-item label="API 地址">
          <a-input v-model:value="form.baseUrl" :placeholder="baseUrlPlaceholder" />
        </a-form-item>
        <div v-if="form.modelType !== 'chat'" style="background:#f0fdf4;border:1px solid #bbf7d0;border-radius:8px;padding:12px;font-size:13px;color:#166534;">
          <template v-if="form.modelType === 'embedding'">
            📐 <strong>向量嵌入模型</strong>：用于把文档转成向量，支持知识库搜索。
            推荐：OpenAI <code>text-embedding-3-small</code>（1536维）或 通义千问 <code>text-embedding-v3</code>
          </template>
          <template v-else-if="form.modelType === 'image'">
            🎨 <strong>图片生成模型</strong>：支持文生图、图生图。
            推荐：通义千问 <code>wan2.2-t2i-flash</code>，NVIDIA <code>sdxl</code>
          </template>
          <template v-else-if="form.modelType === 'voice'">
            🔊 <strong>语音合成模型</strong>：文本转语音。
            推荐：OpenAI <code>tts-1</code> 或 智谱 <code>glm-tts</code>
          </template>
          <template v-else-if="form.modelType === 'video'">
            🎬 <strong>视频生成模型</strong>：文本生成视频。
            推荐：通义千问 <code>wan2.1-i2v-turbo</code> 或 智谱 <code>cogvideo</code>
          </template>
        </div>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { PlusOutlined, ExperimentOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { getModelList, createModel, updateModel, deleteModel as deleteModelApi, testModel as testApi, setDefaultModel } from '@/api/model'
import { message } from 'ant-design-vue'

const models = ref<any[]>([])
const loading = ref(false)
const showModal = ref(false)
const submitting = ref(false)
const editing = ref<any>(null)
const activeTab = ref('all')

const columns = [
  { title: '名称', dataIndex: 'name', key: 'name' },
  { title: '模型名', dataIndex: 'modelName', key: 'model' },
  { title: '供应商', dataIndex: 'provider', key: 'provider', width: 90 },
  { title: '类型', key: 'modelType', width: 100 },
  { title: '状态', key: 'status', width: 60 },
  { title: '默认', key: 'default', width: 60 },
  { title: '操作', key: 'actions', width: 200 },
]

function typeColor(type: string) {
  const map: Record<string, string> = { chat: 'blue', embedding: 'green', image: 'purple', voice: 'orange', video: 'red' }
  return map[type] || 'default'
}
function typeLabel(type: string) {
  const map: Record<string, string> = { chat: '💬 对话', embedding: '📐 向量', image: '🎨 图片', voice: '🔊 语音', video: '🎬 视频' }
  return map[type] || type
}

const modelPlaceholder = computed(() => {
  const map: Record<string, string> = {
    chat: 'deepseek-chat, gpt-4o, qwen-plus',
    embedding: 'text-embedding-3-small, text-embedding-v3',
    image: 'wan2.2-t2i-flash, dall-e-3',
    voice: 'tts-1, glm-tts',
    video: 'wan2.1-i2v-turbo, cogvideo',
  }
  return map[form.modelType] || '模型名称'
})

const baseUrlPlaceholder = computed(() => {
  const map: Record<string, string> = {
    DeepSeek: 'https://api.deepseek.com/v1',
    OpenAI: 'https://api.openai.com/v1',
    Qwen: 'https://dashscope.aliyuncs.com/compatible-mode/v1',
    Zhipu: 'https://open.bigmodel.cn/api/paas/v4',
    Ollama: 'http://localhost:11434/v1',
  }
  return map[form.provider] || 'https://api.openai.com/v1'
})

const filteredModels = computed(() => {
  if (activeTab.value === 'all') return models.value
  return models.value.filter((m: any) => m.modelType === activeTab.value)
})

const form = reactive({
  name: '', provider: 'DeepSeek', modelName: '', apiKey: '', baseUrl: '', modelType: 'chat',
})

const providers: Record<string, any> = {
  chat: { DeepSeek: 'deepseek-chat', OpenAI: 'gpt-4o', Qwen: 'qwen-plus' },
  embedding: { OpenAI: 'text-embedding-3-small', Qwen: 'text-embedding-v3' },
  image: { Qwen: 'wan2.2-t2i-flash', OpenAI: 'dall-e-3' },
  voice: { OpenAI: 'tts-1', Zhipu: 'glm-tts' },
  video: { Qwen: 'wan2.1-i2v-turbo', Zhipu: 'cogvideo' },
}

function onTypeChange() {
  const p = providers[form.modelType]
  if (p) {
    const keys = Object.keys(p)
    form.provider = keys[0] || 'DeepSeek'
    form.modelName = p[form.provider] || ''
  }
  onProviderChange()
}

function onProviderChange() {
  const p = providers[form.modelType]
  if (p && p[form.provider]) {
    form.modelName = p[form.provider]
  }
  form.baseUrl = baseUrlPlaceholder.value
}

function openCreate() {
  editing.value = null
  form.name = ''; form.provider = 'DeepSeek'; form.modelName = ''; form.apiKey = ''; form.baseUrl = ''; form.modelType = 'chat'
  showModal.value = true
}

async function fetchModels() {
  loading.value = true
  try { const res = await getModelList(); models.value = res.data || [] }
  finally { loading.value = false }
}

async function handleSubmit() {
  if (!form.name || !form.modelName) { message.error('请填写必要信息'); return }
  submitting.value = true
  try {
    if (editing.value) { await updateModel(editing.value.id, form); message.success('已更新') }
    else { await createModel(form); message.success('已添加') }
    showModal.value = false; await fetchModels()
  } finally { submitting.value = false }
}

function editModel(record: any) {
  editing.value = record
  Object.assign(form, {
    name: record.name, provider: record.provider, modelName: record.modelName,
    apiKey: record.apiKey || '', baseUrl: record.baseUrl || '', modelType: record.modelType,
  })
  showModal.value = true
}

async function deleteModel(record: any) {
  await deleteModelApi(record.id); message.success('已删除'); await fetchModels()
}

async function testModel(record: any) {
  message.loading({ content: '正在测试...', key: 'test' })
  try {
    const res = await testApi(record.id)
    message.destroy('test')
    if (res.data) message.success('连接成功！')
    else message.error('连接失败')
  } catch { message.destroy('test'); message.error('测试失败') }
}

async function setDefault(record: any) {
  await setDefaultModel(record.id); message.success('已设为默认'); await fetchModels()
}

function toggleStatus(record: any) {
  updateModel(record.id, { status: record.status === 1 ? 0 : 1 })
  record.status = record.status === 1 ? 0 : 1
}

onMounted(fetchModels)
</script>
