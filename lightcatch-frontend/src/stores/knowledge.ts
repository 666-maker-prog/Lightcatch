import { defineStore } from 'pinia'
import { ref } from 'vue'
import {
  getKnowledgeList,
  createKnowledge,
  deleteKnowledge,
  getDocs,
  uploadDoc,
  deleteDoc,
} from '@/api/knowledge'
import { message } from 'ant-design-vue'

export const useKnowledgeStore = defineStore('knowledge', () => {
  const knowledgeList = ref<any[]>([])
  const currentDocs = ref<any[]>([])
  const loading = ref(false)

  async function fetchList() {
    loading.value = true
    try {
      const res = await getKnowledgeList()
      knowledgeList.value = res.data || []
    } finally {
      loading.value = false
    }
  }

  async function create(data: { name: string; description?: string }) {
    await createKnowledge(data)
    message.success('素材库创建成功')
    await fetchList()
  }

  async function remove(id: string) {
    await deleteKnowledge(id)
    message.success('已删除')
    await fetchList()
  }

  async function fetchDocs(knowledgeId: string) {
    const res = await getDocs(knowledgeId)
    currentDocs.value = res.data || []
  }

  async function upload(knowledgeId: string, file: File) {
    await uploadDoc(knowledgeId, file)
    message.success('上传成功，正在处理中')
    await fetchDocs(knowledgeId)
  }

  async function removeDoc(docId: string) {
    await deleteDoc(docId)
    message.success('文档已删除')
    currentDocs.value = currentDocs.value.filter((d: any) => d.id !== docId)
  }

  return { knowledgeList, currentDocs, loading, fetchList, create, remove, fetchDocs, upload, removeDoc }
})
