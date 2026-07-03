<template>
  <div style="height: 100vh; display: flex; flex-direction: column; overflow: hidden;">
    <div style="display: flex; align-items: center; justify-content: space-between; padding: 12px 24px; background: white; border-bottom: 1px solid #f1f5f9;">
      <div style="display: flex; align-items: center; gap: 12px;">
        <h3 style="margin: 0;">工作流设计器</h3>
        <a-select v-model:value="selectedFlowId" style="width: 240px" placeholder="选择或创建工作流" @change="loadFlow" allow-clear>
          <a-select-option v-for="flow in flows" :key="flow.id" :value="flow.id">{{ flow.name }}</a-select-option>
        </a-select>
        <a-button type="primary" size="small" @click="showCreateModal = true">
          <template #icon><PlusOutlined /></template>新建
        </a-button>
      </div>
      <div style="display: flex; gap: 8px;">
        <a-button @click="saveDesign" type="primary" :loading="saving" :disabled="!selectedFlowId">
          <SaveOutlined /> 保存
        </a-button>
        <a-button @click="runFlow" type="primary" ghost :disabled="!selectedFlowId || nodes.length === 0">
          <CaretRightOutlined /> 运行
        </a-button>
      </div>
    </div>

    <div style="flex: 1; display: flex; overflow: hidden;">
      <div style="width: 200px; background: white; border-right: 1px solid #f1f5f9; padding: 16px;">
        <h4 style="margin: 0 0 12px; font-size: 14px; color: #64748b;">拖拽节点到画布</h4>
        <div v-for="nt in nodeTypes" :key="nt.type"
          draggable="true"
          @dragstart="onDragStart($event, nt.type)"
          style="padding: 10px 12px; margin-bottom: 8px; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; cursor: grab; font-size: 13px;">
          {{ nt.icon }} {{ nt.label }}
        </div>
      </div>

      <div style="flex: 1; position: relative;" ref="canvasRef">
        <VueFlow
          v-if="ready"
          v-model:nodes="nodes"
          v-model:edges="edges"
          @drop.prevent="onDrop"
          @dragover.prevent
          :default-viewport="{ x: 100, y: 100, zoom: 0.85 }"
          :min-zoom="0.2"
          :max-zoom="2"
          class="workflow-canvas"
        >
          <template #node-custom="nodeProps">
            <div class="flow-node" :style="{ borderColor: getNodeColor(nodeProps.data.type) }">
              <div class="flow-node-header" :style="{ background: getNodeColor(nodeProps.data.type) }">
                <strong>{{ getNodeLabel(nodeProps.data.type) }}</strong>
              </div>
              <div class="flow-node-body">
                <input v-model="nodeProps.data.name" placeholder="节点名称"
                  style="width:100%;border:1px solid #e2e8f0;border-radius:4px;padding:4px 8px;font-size:12px;"
                  @click.stop @mousedown.stop @keydown.stop />
              </div>
              <Handle type="target" :position="Position.Top" style="background:#7c3aed" />
              <Handle type="source" :position="Position.Bottom" style="background:#7c3aed" />
            </div>
          </template>
        </VueFlow>
      </div>
    </div>

    <a-modal v-model:open="showCreateModal" title="新建工作流" @ok="handleCreate" :confirm-loading="creating">
      <a-form layout="vertical">
        <a-form-item label="名称" required>
          <a-input v-model:value="newName" placeholder="如: 每日内容生产流水线" />
        </a-form-item>
        <a-form-item label="描述">
          <a-textarea v-model:value="newDesc" placeholder="描述这个工作流的用途" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, markRaw, onMounted } from 'vue'
import { VueFlow, Handle, Position, useVueFlow } from '@vue-flow/core'
import '@vue-flow/core/dist/style.css'
import '@vue-flow/core/dist/theme-default.css'
import { PlusOutlined, SaveOutlined, CaretRightOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import request from '@/utils/request'

const flows = ref<any[]>([])
const selectedFlowId = ref<string | undefined>(undefined)
const saving = ref(false)
const showCreateModal = ref(false)
const creating = ref(false)
const newName = ref('')
const newDesc = ref('')
const ready = ref(false)
const canvasRef = ref<HTMLElement | null>(null)
let nodeCounter = 0

const nodeTypes = [
  { type: 'LLM_Node', label: 'LLM 调用', icon: '🤖' },
  { type: 'Knowledge_Node', label: '知识库检索', icon: '📚' },
]
const nodeColors: Record<string, string> = { LLM_Node: '#7c3aed', Knowledge_Node: '#0891b2' }
const nodeLabels: Record<string, string> = { LLM_Node: '🤖 LLM 调用', Knowledge_Node: '📚 知识库检索' }

const nodes = ref<any[]>([])
const edges = ref<any[]>([])

function getNodeColor(type: string) { return nodeColors[type] || '#7c3aed' }
function getNodeLabel(type: string) { return nodeLabels[type] || type }

function onDragStart(event: DragEvent, type: string) {
  event.dataTransfer?.setData('application/flow-node', type)
}

function onDrop(event: DragEvent) {
  const type = event.dataTransfer?.getData('application/flow-node')
  if (!type) return
  const rect = canvasRef.value?.getBoundingClientRect()
  if (!rect) return
  const x = event.clientX - rect.left - 90
  const y = event.clientY - rect.top - 30
  const id = `node_${++nodeCounter}`
  nodes.value = [...nodes.value, {
    id,
    type: 'custom',
    position: { x: Math.max(0, x), y: Math.max(0, y) },
    data: { type, name: '' },
  }]
}

function loadFlow(flowId: string) {
  if (!flowId) return
  const flow = flows.value.find((f: any) => f.id === flowId)
  if (!flow) return
  try {
    const design = flow.design ? JSON.parse(flow.design) : null
    nodes.value = design?.nodes || []
    edges.value = design?.edges || []
  } catch { nodes.value = []; edges.value = [] }
}

async function saveDesign() {
  if (!selectedFlowId.value) return
  saving.value = true
  try {
    const chain = nodes.value.map((n: any) => n.data.type).join(' -> ')
    await request.put(`/ai/flow/${selectedFlowId.value}`, {
      design: JSON.stringify({ nodes: nodes.value, edges: edges.value }),
      chain,
    })
    message.success('工作流已保存')
  } catch (e: any) { message.error(e.message || '保存失败')
  } finally { saving.value = false }
}

async function runFlow() {
  if (!selectedFlowId.value || !nodes.value.length) return
  message.loading({ content: '正在运行工作流...', key: 'flow' })
  try {
    const res = await request.post('/ai/flow/run', { flowId: selectedFlowId.value, input: 'test' })
    message.destroy('flow')
    message.success('结果: ' + (res.data || '完成'))
  } catch (e: any) {
    message.destroy('flow')
    message.error(e.message || '运行失败')
  }
}

async function fetchFlows() {
  try { const res = await request.get('/ai/flow/list'); flows.value = res.data || [] } catch { }
}

async function handleCreate() {
  if (!newName.value.trim()) { message.error('请输入名称'); return }
  creating.value = true
  try {
    await request.post('/ai/flow/create', { name: newName.value, description: newDesc.value })
    message.success('创建成功')
    showCreateModal.value = false
    newName.value = ''
    newDesc.value = ''
    await fetchFlows()
  } catch (e: any) { message.error(e.message || '创建失败')
  } finally { creating.value = false }
}

onMounted(async () => {
  await fetchFlows()
  ready.value = true
})
</script>

<style>
.workflow-canvas { width: 100%; height: 100%; background: #f8fafc; }
.flow-node { background: white; border: 2px solid #7c3aed; border-radius: 10px; width: 180px; box-shadow: 0 2px 8px rgba(0,0,0,0.08); font-size: 13px; overflow: hidden; }
.flow-node-header { padding: 6px 10px; color: white; font-size: 12px; }
.flow-node-body { padding: 8px 10px; }
</style>
