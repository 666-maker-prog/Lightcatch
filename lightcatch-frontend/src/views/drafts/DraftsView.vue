<template>
  <div style="padding:24px;max-width:1000px;margin:0 auto;">
    <div style="margin-bottom:24px;">
      <h2 style="margin:0 0 4px;">草稿箱</h2>
      <p style="color:#64748b;margin:0;font-size:14px;">工作流执行生成的内容会保存在这里</p>
    </div>

    <div v-if="loading" style="text-align:center;padding:60px 0;"><a-spin size="large" /></div>

    <div v-if="!loading && drafts.length === 0" style="text-align:center;padding:60px 0;color:#94a3b8;">
      <FileTextOutlined style="font-size:48px;color:#cbd5e1;" />
      <p style="margin-top:12px;">还没有草稿</p>
      <p style="font-size:13px;">去工作流生成一篇内容，结果会自动保存到这里</p>
    </div>

    <div v-for="d in drafts" :key="d.id" style="background:white;border-radius:12px;padding:20px;margin-bottom:12px;border:1px solid #f1f5f9;">
      <div style="display:flex;justify-content:space-between;align-items:flex-start;">
        <div style="flex:1;">
          <h3 style="margin:0 0 4px;font-size:15px;">{{ d.title }}</h3>
          <div style="font-size:12px;color:#94a3b8;margin-bottom:8px;">
            生成于 {{ d.createTime }}
            <span v-if="d.platforms"> | 发布状态: 未发布</span>
          </div>
          <div style="font-size:14px;color:#475569;line-height:1.6;max-height:80px;overflow:hidden;">
            {{ (d.content || '').slice(0, 200) }}{{ (d.content || '').length > 200 ? '...' : '' }}
          </div>
        </div>
        <div style="display:flex;gap:6px;flex-shrink:0;margin-left:16px;">
          <a-button size="small" @click="editDraft(d)"><EditOutlined /> 编辑</a-button>
          <a-button size="small" danger @click="deleteDraft(d)"><DeleteOutlined /> 删除</a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { FileTextOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { getDrafts, deleteDraft as deleteApi } from '@/api/drafts'
import { message } from 'ant-design-vue'

const drafts = ref<any[]>([])
const loading = ref(true)

async function fetchDrafts() {
  loading.value = true
  try { const res = await getDrafts(); drafts.value = res.data || [] }
  finally { loading.value = false }
}

async function deleteDraft(d: any) {
  await deleteApi(d.id)
  message.success('已删除')
  await fetchDrafts()
}

function editDraft(d: any) {
  message.info('编辑功能开发中')
}

onMounted(fetchDrafts)
</script>
