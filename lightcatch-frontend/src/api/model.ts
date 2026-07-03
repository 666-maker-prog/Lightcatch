import request from '@/utils/request'

export function getModelList(type?: string) {
  const params = type ? { type } : {}
  return request.get('/ai/model/list', { params })
}

export function createModel(data: any) {
  return request.post('/ai/model/create', data)
}

export function updateModel(id: string, data: any) {
  return request.put(`/ai/model/${id}`, data)
}

export function deleteModel(id: string) {
  return request.delete(`/ai/model/${id}`)
}

export function testModel(id: string) {
  return request.post(`/ai/model/${id}/test`)
}

export function setDefaultModel(id: string) {
  return request.put(`/ai/model/${id}/default`)
}
