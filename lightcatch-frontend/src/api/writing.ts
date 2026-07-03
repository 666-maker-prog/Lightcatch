import request from '@/utils/request'

export function generateContent(data: {
  topic: string
  style?: string
  knowledgeId?: string
  modelId?: string
}) {
  return request.post('/ai/writing/generate', data)
}

export function rewriteText(data: { text: string; style?: string; modelId?: string }) {
  return request.post('/ai/writing/rewrite', data)
}

export function generateTitles(data: { topic: string; modelId?: string }) {
  return request.post('/ai/writing/titles', data)
}

export function generateOutline(data: { topic: string; modelId?: string }) {
  return request.post('/ai/writing/outline', data)
}

export function optimizeText(data: { text: string; modelId?: string }) {
  return request.post('/ai/writing/optimize', data)
}
