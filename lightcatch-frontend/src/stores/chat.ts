import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getConversations, getMessages, deleteConversation } from '@/api/chat'
import { getToken } from '@/utils/token'
import { addLog } from '@/utils/debug'

export const useChatStore = defineStore('chat', () => {
  const conversations = ref<any[]>([])
  const messages = ref<any[]>([])
  const currentConversationId = ref<string | null>(null)
  const isStreaming = ref(false)
  const streamingContent = ref('')

  async function fetchConversations() {
    const res = await getConversations()
    conversations.value = res.data || []
  }

  async function fetchMessages(conversationId: string) {
    const res = await getMessages(conversationId)
    messages.value = res.data || []
    currentConversationId.value = conversationId
  }

  async function removeConversation(id: string) {
    await deleteConversation(id)
    conversations.value = conversations.value.filter((c: any) => c.id !== id)
    if (currentConversationId.value === id) {
      currentConversationId.value = null
      messages.value = []
    }
  }

  function sendMessageStream(
    message: string,
    knowledgeId?: string,
    modelId?: string,
    conversationId?: string
  ) {
    isStreaming.value = true
    streamingContent.value = ''

    const token = getToken()
    const params = new URLSearchParams()
    params.append('message', message)
    if (conversationId) params.append('conversationId', conversationId)
    if (modelId) params.append('modelId', modelId)
    if (knowledgeId) params.append('knowledgeId', knowledgeId)
    if (token) params.append('token', token)

    const url = `/api/ai/chat/stream?${params.toString()}`
    addLog('INFO', `SSE connecting: ${url.slice(0, 120)}...`)

    const eventSource = new EventSource(url, {
      withCredentials: true,
    })

    eventSource.onopen = () => addLog('INFO', 'SSE connection opened')

    eventSource.onmessage = (event) => {
      try {
        addLog('INFO', `SSE raw: ${event.data.slice(0, 100)}`)
        const data = JSON.parse(event.data)
        if (data.type === 'init') {
          currentConversationId.value = data.conversationId
        } else if (data.type === 'token') {
          streamingContent.value += data.content
        } else if (data.type === 'done') {
          messages.value.push({
            role: 'user',
            content: message,
          })
          messages.value.push({
            role: 'assistant',
            content: streamingContent.value,
          })
          streamingContent.value = ''
          isStreaming.value = false
          eventSource.close()
          fetchConversations()
        } else if (data.type === 'error') {
          isStreaming.value = false
          eventSource.close()
        }
      } catch {
        streamingContent.value += event.data
      }
    }

    eventSource.onerror = () => {
      isStreaming.value = false
      eventSource.close()
    }

    return () => { eventSource.close(); isStreaming.value = false }
  }

  return {
    conversations,
    messages,
    currentConversationId,
    isStreaming,
    streamingContent,
    fetchConversations,
    fetchMessages,
    removeConversation,
    sendMessageStream,
  }
})
