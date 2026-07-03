<template>
  <div class="workflow-page">
    <!-- 顶部 -->
    <div class="wf-header">
      <div>
        <h2 style="margin:0 0 4px;">工作流</h2>
        <p style="color:#64748b;margin:0;font-size:14px;">用文字描述你的创作流程，AI 自动生成工作流</p>
      </div>
    </div>

    <!-- 输入区 -->
    <div class="wf-input-area">
      <a-textarea
        v-model:value="inputText"
        placeholder="描述你的创作流程，例如：每天8点从素材库找一篇阅读量最高的文章，模仿它的风格写一篇小红书笔记，并配一张图"
        :rows="3"
        style="border-radius:12px;font-size:14px;"
      />
      <a-button type="primary" size="large" :loading="generating" @click="generateFlow" style="margin-top:12px;">
        <template #icon><RobotOutlined /></template>
        {{ generating ? 'AI 正在理解...' : 'AI 生成工作流' }}
      </a-button>
    </div>

    <!-- 空状态 -->
    <div v-if="!generating && flows.length === 0" class="wf-empty">
      <ApartmentOutlined style="font-size:48px;color:#cbd5e1;" />
      <p style="color:#94a3b8;margin-top:12px;">描述你的创作流程，AI 帮你生成工作流</p>
      <div style="color:#94a3b8;font-size:13px;margin-top:8px;">
        试试：<a-tag color="purple" @click="inputText='每天8点从素材库找阅读量最高文章，写一篇小红书笔记并配图'" style="cursor:pointer;">📝 每日热点写作</a-tag>
        <a-tag color="purple" @click="inputText='搜索某产品的最新评价，分析正负面观点，生成一篇测评文章'" style="cursor:pointer;">🔍 产品测评</a-tag>
      </div>
    </div>

    <!-- 工作流列表 -->
    <div class="wf-list" v-if="flows.length > 0">
      <div v-for="flow in flows" :key="flow.id" class="wf-card">
        <div class="wf-card-header">
          <div>
            <h3 style="margin:0;font-size:16px;">{{ flow.name }}</h3>
            <p style="margin:4px 0 0;font-size:13px;color:#94a3b8;">{{ flow.description }}</p>
          </div>
          <div style="display:flex;gap:4px;flex-shrink:0;">
            <a-button size="small" type="primary" ghost @click="runFlow(flow)"><CaretRightOutlined /> 运行</a-button>
            <a-button size="small" danger @click="deleteFlow(flow)"><DeleteOutlined /></a-button>
          </div>
        </div>
        <div class="wf-steps">
          <div v-for="(step, idx) in parseSteps(flow)" :key="idx" class="wf-step">
            <div class="wf-step-num">{{ idx + 1 }}</div>
            <div class="wf-step-body">
              <div class="wf-step-name">{{ step.icon }} {{ step.name }}</div>
              <div class="wf-step-type">{{ step.typeLabel }}</div>
            </div>
            <div v-if="idx < parseSteps(flow).length - 1" class="wf-step-line"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ApartmentOutlined, RobotOutlined, CaretRightOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import request from '@/utils/request'
import { message } from 'ant-design-vue'

const flows = ref<any[]>([])
const inputText = ref('')
const generating = ref(false)

const nodeIcons: Record<string, string> = {
  trigger: '⏰', manual: '▶️', knowledge: '📚', llm: '🤖', image: '🎨', web_search: '🌐', condition: '🔀',
}
const nodeLabels: Record<string, string> = {
  trigger: '定时触发', manual: '手动触发', knowledge: '素材库检索', llm: 'AI 生成', image: '图片生成', web_search: '联网搜索', condition: '条件判断',
}

function parseSteps(flow: any) {
  try {
    const design = JSON.parse(flow.design || '{}')
    const nodes = design.nodes || []
    return nodes.map((n: any) => ({
      name: n.name || n.type || '未知',
      type: n.type || 'llm',
      typeLabel: nodeLabels[n.type] || n.type,
      icon: nodeIcons[n.type] || '📦',
    }))
  } catch {
    // fallback: parse from chain text
    if (!flow.chain) return []
    return flow.chain.split(' -> ').map((s: string) => {
      const icon = Object.values(nodeIcons).find(i => s.includes(i)) || '📦'
      const type = Object.keys(nodeIcons).find(k => s.includes(nodeIcons[k])) || 'llm'
      return { name: s.replace(/[^\w一-鿿]/g, '').trim(), type, typeLabel: nodeLabels[type] || type, icon }
    })
  }
}

async function generateFlow() {
  const desc = inputText.value.trim()
  if (!desc) { message.error('请描述你的创作流程'); return }
  generating.value = true
  try {
    const res = await request.post('/ai/flow/gen', { description: desc })
    message.success('工作流生成成功')
    inputText.value = ''
    await fetchFlows()
  } catch (e: any) {
    message.error(e.message || '生成失败')
  } finally { generating.value = false }
}

async function fetchFlows() {
  try { const res = await request.get('/ai/flow/list'); flows.value = res.data || [] } catch {}
}

async function runFlow(flow: any) {
  message.loading({ content: '正在运行...', key: 'flow' })
  try {
    const res = await request.post('/ai/flow/run', { flowId: flow.id, input: 'test' })
    message.destroy('flow')
    message.success('结果: ' + (res.data || '完成'))
  } catch (e: any) { message.destroy('flow'); message.error(e.message || '运行失败') }
}

async function deleteFlow(flow: any) {
  await request.delete(`/ai/flow/${flow.id}`)
  message.success('已删除')
  await fetchFlows()
}

onMounted(fetchFlows)
</script>

<style scoped>
.workflow-page { padding: 24px; max-width: 900px; margin: 0 auto; }
.wf-header { margin-bottom: 24px; }
.wf-input-area { margin-bottom: 32px; }
.wf-empty { text-align: center; padding: 60px 20px; }
.wf-list { display: flex; flex-direction: column; gap: 16px; }
.wf-card {
  background: white; border-radius: 12px; padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06); border: 1px solid #f1f5f9;
}
.wf-card-header { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 16px; }
.wf-steps { display: flex; flex-direction: column; gap: 0; position: relative; }
.wf-step { display: flex; align-items: flex-start; gap: 12px; position: relative; padding: 8px 0; }
.wf-step-num {
  width: 28px; height: 28px; border-radius: 50%;
  background: #7c3aed; color: white; display: flex;
  align-items: center; justify-content: center;
  font-size: 13px; font-weight: 600; flex-shrink: 0;
}
.wf-step-body { flex: 1; }
.wf-step-name { font-size: 14px; font-weight: 500; color: #1e293b; }
.wf-step-type { font-size: 12px; color: #94a3b8; }
.wf-step-line {
  position: absolute; left: 13px; top: 36px;
  width: 2px; height: calc(100% - 36px);
  background: #e2e8f0;
}
</style>
