import request from '@/utils/request'

export function getConversations() {
  return request.get('/ai/chat/conversations')
}

export function getMessages(conversationId: string) {
  return request.get(`/ai/chat/conversation/${conversationId}/messages`)
}

export function deleteConversation(id: string) {
  return request.delete(`/ai/chat/conversation/${id}`)
}
