import request from '@/utils/request'

export function getDrafts() {
  return request.get('/ai/flow/output/list')
}

export function deleteDraft(id: string) {
  return request.delete(`/ai/flow/output/${id}`)
}

export function updateDraft(id: string, data: any) {
  return request.put(`/ai/flow/output/${id}`, data)
}
