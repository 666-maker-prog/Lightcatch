<template>
  <div class="writing-page" style="padding: 24px; max-height: 100vh; overflow-y: auto;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
      <div>
        <h2 style="margin: 0 0 4px;">AI 写作</h2>
        <p style="color: #64748b; margin: 0; font-size: 14px;">使用前请先在"模型管理"中添加并配置 AI 模型</p>
      </div>
    </div>

    <a-alert
      v-if="modelCount === 0"
      type="warning"
      show-icon
      message="未配置 AI 模型"
      description="请先在左侧菜单「模型管理」中添加一个 AI 模型（如 DeepSeek 或通义千问），否则写作功能无法使用。"
      style="margin-bottom: 16px;"
      :closable="false"
    />

    <a-row :gutter="16" style="min-height: 500px;">
      <a-col :span="7">
        <a-card title="写作设置" style="border-radius: 12px;">
          <a-form layout="vertical">
            <a-form-item label="写作模式">
              <a-select v-model:value="mode" size="large">
                <a-select-option value="generate">✍️ 生成内容</a-select-option>
                <a-select-option value="rewrite">🔄 改写</a-select-option>
                <a-select-option value="titles">🏷️ 起标题</a-select-option>
                <a-select-option value="outline">📋 列大纲</a-select-option>
                <a-select-option value="optimize">✨ 优化</a-select-option>
                <a-select-option value="expand">📖 扩写</a-select-option>
                <a-select-option value="shorten">✂️ 缩写</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="主题/输入" required>
              <a-textarea v-model:value="input" :placeholder="inputPlaceholder" :rows="4" />
            </a-form-item>
            <a-form-item label="风格" v-if="showStyle">
              <a-select v-model:value="style" size="large">
                <a-select-option value="口语化">😊 口语化</a-select-option>
                <a-select-option value="正式">📝 正式</a-select-option>
                <a-select-option value="小红书">🌸 小红书风</a-select-option>
                <a-select-option value="干货型">📚 干货型</a-select-option>
                <a-select-option value="情绪化">🔥 情绪化</a-select-option>
                <a-select-option value="种草">🌿 种草风</a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item label="参考素材库" help="选择素材库后，AI 会参考其中的内容进行创作">
              <a-select v-model:value="knowledgeId" placeholder="选择素材库" allow-clear>
                <a-select-option v-for="kb in knowledgeStore.knowledgeList" :key="kb.id" :value="kb.id">
                  {{ kb.name }}
                </a-select-option>
              </a-select>
            </a-form-item>
            <a-button type="primary" block size="large" :loading="loading" @click="handleGenerate" :disabled="modelCount === 0">
              {{ loading ? 'AI 创作中...' : '开始写作' }}
            </a-button>
          </a-form>
        </a-card>
      </a-col>
      <a-col :span="17">
        <a-card title="生成结果" style="border-radius: 12px; min-height: 500px;">
          <template #extra>
            <a-button v-if="result" type="primary" ghost size="small" @click="copyResult">
              <CopyOutlined /> 复制
            </a-button>
          </template>
          <div v-if="!result && !loading" style="text-align: center; padding: 80px 20px; color: #94a3b8;">
            <EditOutlined style="font-size: 48px; color: #cbd5e1" />
            <p style="margin-top: 12px;">在左侧输入主题或内容，开始创作</p>
          </div>
          <div v-if="loading" style="text-align: center; padding: 60px 0;">
            <a-spin size="large" />
            <p style="margin-top: 16px; color: #64748b;">AI 正在创作中...</p>
          </div>
          <div v-if="result && !loading" class="result-content" v-html="renderMarkdown(result)"></div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { CopyOutlined, EditOutlined } from '@ant-design/icons-vue'
import { useKnowledgeStore } from '@/stores/knowledge'
import { generateContent, rewriteText, generateTitles, generateOutline, optimizeText } from '@/api/writing'
import { getModelList } from '@/api/model'
import { message } from 'ant-design-vue'
import { marked } from 'marked'

const knowledgeStore = useKnowledgeStore()
const mode = ref('generate')
const input = ref('')
const style = ref('口语化')
const knowledgeId = ref<string | undefined>(undefined)
const loading = ref(false)
const result = ref('')
const modelCount = ref(0)

const showStyle = computed(() => ['generate', 'rewrite'].includes(mode.value))
const inputPlaceholder = computed(() => {
  const map: Record<string, string> = {
    generate: '请输入文章主题，如：大学生暑假旅游攻略',
    rewrite: '请输入要改写的文本',
    titles: '请输入主题，如：智能家居推荐',
    outline: '请输入文章主题',
    optimize: '请输入要优化的文本',
    expand: '请输入要扩写的文本',
    shorten: '请输入要缩写的文本',
  }
  return map[mode.value] || '请输入内容'
})

async function handleGenerate() {
  if (!input.value.trim()) { message.error('请输入内容'); return }
  loading.value = true; result.value = ''
  try {
    const params: any = { topic: input.value, text: input.value, style: style.value, knowledgeId: knowledgeId.value }
    let res: any
    switch (mode.value) {
      case 'generate': res = await generateContent(params); break
      case 'rewrite': res = await rewriteText(params); break
      case 'titles': res = await generateTitles(params); break
      case 'outline': res = await generateOutline(params); break
      case 'optimize': res = await optimizeText(params); break
      default: res = await generateContent(params)
    }
    result.value = res.data
  } catch (e: any) {
    result.value = ''
    if (e.message && e.message.includes('apiKey')) {
      message.error('模型 API Key 未配置或无效，请先在"模型管理"中配置')
    } else {
      message.error(e.message || '生成失败')
    }
  } finally { loading.value = false }
}

function renderMarkdown(text: string) { try { return marked(text) as string } catch { return text } }

async function copyResult() {
  try { await navigator.clipboard.writeText(result.value); message.success('已复制到剪贴板') }
  catch { message.error('复制失败') }
}

onMounted(async () => {
  await knowledgeStore.fetchList()
  try {
    const res = await getModelList('chat')
    modelCount.value = (res.data || []).length
  } catch { modelCount.value = 0 }
})
</script>

<style scoped>
.result-content { line-height: 1.8; font-size: 15px; }
.result-content :deep(h1), .result-content :deep(h2), .result-content :deep(h3) { margin-top: 16px; margin-bottom: 8px; }
.result-content :deep(p) { margin-bottom: 12px; }
.result-content :deep(ul), .result-content :deep(ol) { padding-left: 20px; }
</style>
