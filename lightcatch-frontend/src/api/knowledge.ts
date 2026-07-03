import request from '@/utils/request'

export function getKnowledgeList() {
  return request.get('/ai/knowledge/list')
}

export function createKnowledge(data: { name: string; description?: string }) {
  return request.post('/ai/knowledge/create', data)
}

export function deleteKnowledge(id: string) {
  return request.delete(`/ai/knowledge/${id}`)
}

export function getDocs(knowledgeId: string) {
  return request.get(`/ai/knowledge/${knowledgeId}/docs`)
}

export function uploadDoc(knowledgeId: string, file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post(`/ai/knowledge/${knowledgeId}/upload`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function deleteDoc(docId: string) {
  return request.delete(`/ai/knowledge/doc/${docId}`)
}
