<template>
  <div class="chat-page">
    <a-layout style="height: 100vh; background: #f8fafc">
      <!-- 左侧对话列表 -->
      <a-layout-sider
        width="300"
        style="background: white; border-right: 1px solid #f1f5f9; height: 100vh; overflow-y: auto;"
      >
        <div class="conversation-header">
          <h3>历史对话</h3>
          <a-button type="primary" size="small" @click="newConversation" ghost>
            <template #icon><PlusOutlined /></template>
            新建
          </a-button>
        </div>

        <div class="conversation-list">
          <div
            v-for="conv in chatStore.conversations"
            :key="conv.id"
            class="conversation-item"
            :class="{ active: conv.id === chatStore.currentConversationId }"
            @click="selectConversation(conv)"
          >
            <div class="conv-icon">
              <MessageOutlined />
            </div>
            <div class="conv-content">
              <div v-if="renamingId !== conv.id" class="conv-title" @dblclick="startRename(conv)">{{ conv.title || '新对话' }}</div>
              <a-input v-else v-model:value="renameText" size="small" @pressEnter="doRename(conv)" @blur="doRename(conv)" @click.stop style="width:100%;" ref="renameInputRef" />
              <div class="conv-time">{{ formatTime(conv.createTime) }}</div>
            </div>
            <a-button
              type="text"
              size="small"
              danger
              class="delete-btn"
              @click.stop="deleteConv(conv)"
            >
              <DeleteOutlined />
            </a-button>
          </div>

          <div v-if="chatStore.conversations.length === 0" class="empty-conv">
            <MessageOutlined style="font-size: 32px; color: #cbd5e1" />
            <p style="margin: 12px 0; color: #94a3b8;">还没有对话</p>
            <a-button type="primary" ghost @click="newConversation">开始新对话</a-button>
          </div>
        </div>
      </a-layout-sider>

      <!-- 聊天主区域 -->
      <a-layout-content style="display: flex; flex-direction: column; height: 100vh;">
        <div class="messages-container" ref="messagesRef">
          <div class="messages-inner">
            <div v-if="chatStore.messages.length === 0 && !chatStore.isStreaming" class="welcome">
              <div class="welcome-icon">
                <svg viewBox="0 0 40 40" width="48" height="48">
                  <circle cx="20" cy="20" r="18" fill="rgba(124,58,237,0.1)" />
                  <path d="M12 28 L20 8 L28 28" stroke="#7c3aed" stroke-width="2.5" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M15 22 L25 22" stroke="#7c3aed" stroke-width="2" fill="none" stroke-linecap="round"/>
                </svg>
              </div>
              <h2>你好！我是你的 AI 创作助手</h2>
              <p>上传素材库，我能模仿你的写作风格。试试对我说：</p>
              <div class="suggestions">
                <a-tag color="purple" @click="sendSuggestion('帮我写一篇小红书风格的旅游攻略')">
                  ✈️ 写旅游攻略
                </a-tag>
                <a-tag color="purple" @click="sendSuggestion('给这篇文章起10个吸引人的标题')">
                  📝 起标题
                </a-tag>
                <a-tag color="purple" @click="sendSuggestion('帮我改写这段文案，让它更口语化')">
                  ✏️ 改写文案
                </a-tag>
              </div>
            </div>

            <div
              v-for="(msg, idx) in chatStore.messages"
              :key="idx"
              class="message"
              :class="msg.role"
            >
              <a-avatar :size="36" :style="{ background: msg.role === 'user' ? '#7c3aed' : '#10b981', flexShrink: 0 }">
                {{ msg.role === 'user' ? 'U' : 'A' }}
              </a-avatar>
              <div class="message-content">
                <div class="message-bubble" v-html="renderMarkdown(msg.content)"></div>
              </div>
            </div>

            <div v-if="chatStore.isStreaming" class="message assistant">
              <a-avatar :size="36" style="background: #10b981; flex-shrink: 0">A</a-avatar>
              <div class="message-content">
                <div class="message-bubble streaming">
                  <span v-for="(char, i) in chatStore.streamingContent" :key="i">{{ char }}</span>
                  <span class="cursor">▍</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="input-area">
          <div class="input-tools">
            <a-select v-model:value="selectedKnowledgeId" placeholder="选择素材库" style="width: 160px" size="small" allow-clear>
              <a-select-option v-for="kb in knowledgeStore.knowledgeList" :key="kb.id" :value="kb.id">
                <DatabaseOutlined /> {{ kb.name }}
              </a-select-option>
            </a-select>
          </div>
          <div class="input-wrapper">
            <a-textarea
              v-model:value="inputMessage"
              placeholder="输入消息，按 Enter 发送..."
              :rows="1"
              :disabled="chatStore.isStreaming"
              @pressEnter="sendMessage"
              :auto-size="{ minRows: 1, maxRows: 4 }"
              class="chat-input"
            />
            <a-button
              type="primary"
              shape="circle"
              size="large"
              :loading="chatStore.isStreaming"
              @click="chatStore.isStreaming ? stopStream() : sendMessage()"
              class="send-btn"
            >
              <template #icon>
                <SendOutlined v-if="!chatStore.isStreaming" />
                <CloseOutlined v-else />
              </template>
            </a-button>
          </div>
        </div>
      </a-layout-content>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  MessageOutlined, PlusOutlined, DeleteOutlined,
  SendOutlined, CloseOutlined, DatabaseOutlined,
} from '@ant-design/icons-vue'
import { useChatStore } from '@/stores/chat'
import { useKnowledgeStore } from '@/stores/knowledge'
import { message } from 'ant-design-vue'
import { marked } from 'marked'
import request from '@/utils/request'

const router = useRouter()
const route = useRoute()
const chatStore = useChatStore()
const knowledgeStore = useKnowledgeStore()

const inputMessage = ref('')
const selectedKnowledgeId = ref<string | undefined>(undefined)
const messagesRef = ref<HTMLElement | null>(null)
const renamingId = ref<string | null>(null)
const renameText = ref('')
const renameInputRef = ref<HTMLInputElement | null>(null)
let stopStreamFn: (() => void) | null = null

function formatTime(time: string) {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const diff = now.getTime() - d.getTime()
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return d.getHours() + ':' + String(d.getMinutes()).padStart(2, '0')
  return (d.getMonth() + 1) + '/' + d.getDate()
}

function renderMarkdown(text: string) {
  if (!text) return ''
  try { return marked(text) as string } catch { return text }
}

async function sendMessage(e?: KeyboardEvent) {
  if (e && e.shiftKey) return
  const msg = inputMessage.value.trim()
  if (!msg || chatStore.isStreaming) return
  inputMessage.value = ''
  stopStreamFn = chatStore.sendMessageStream(
    msg, selectedKnowledgeId.value, undefined, chatStore.currentConversationId || undefined
  )
  await nextTick()
  scrollToBottom()
}

function stopStream() {
  if (stopStreamFn) { stopStreamFn(); stopStreamFn = null }
}

function newConversation() {
  chatStore.currentConversationId = null
  chatStore.messages = []
  router.push('/chat')
}

async function selectConversation(conv: any) {
  chatStore.currentConversationId = conv.id
  router.push('/chat/' + conv.id)
  await chatStore.fetchMessages(conv.id)
  scrollToBottom()
}

async function deleteConv(conv: any) {
  await chatStore.removeConversation(conv.id)
  if (chatStore.currentConversationId === conv.id) {
    chatStore.currentConversationId = null
    chatStore.messages = []
    router.push('/chat')
  }
}

function startRename(conv: any) {
  renamingId.value = conv.id
  renameText.value = conv.title || ''
  setTimeout(() => renameInputRef.value?.focus(), 50)
}

async function doRename(conv: any) {
  if (renamingId.value !== conv.id) return
  renamingId.value = null
  const newTitle = renameText.value.trim()
  if (!newTitle || newTitle === (conv.title || '')) return
  try {
    await request.put('/ai/chat/conversation/' + conv.id + '/update/title', { title: newTitle })
    conv.title = newTitle
  } catch {}
}

function sendSuggestion(text: string) {
  inputMessage.value = text
  sendMessage()
}

function scrollToBottom() {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

onMounted(async () => {
  await chatStore.fetchConversations()
  await knowledgeStore.fetchList()

  // Watch for route param changes to load conversations
  if (route.params.id) {
    const id = route.params.id as string
    chatStore.currentConversationId = id
    await chatStore.fetchMessages(id)
    scrollToBottom()
  }
})
</script>

<style scoped>
.chat-page { height: 100vh; overflow: hidden; }
.conversation-header { display: flex; justify-content: space-between; align-items: center; padding: 20px 16px 12px; }
.conversation-header h3 { margin: 0; font-size: 16px; color: #1e293b; }
.conversation-list { padding: 0 8px; }
.conversation-item { display: flex; align-items: center; gap: 10px; padding: 10px 12px; border-radius: 8px; cursor: pointer; transition: all 0.15s; }
.conversation-item:hover { background: #f1f5f9; }
.conversation-item:hover .delete-btn { opacity: 1; }
.conversation-item.active { background: #f3e8ff; }
.conv-icon { color: #7c3aed; font-size: 16px; flex-shrink: 0; }
.conv-content { flex: 1; min-width: 0; }
.conv-title { font-size: 13px; color: #1e293b; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.conv-time { font-size: 11px; color: #94a3b8; }
.delete-btn { opacity: 0; transition: opacity 0.15s; }
.empty-conv { text-align: center; padding: 40px 16px; }
.messages-container { flex: 1; overflow-y: auto; padding: 24px; }
.messages-inner { max-width: 800px; margin: 0 auto; }
.welcome { text-align: center; padding: 60px 20px; }
.welcome h2 { color: #1e293b; margin: 16px 0 8px; font-size: 22px; }
.welcome p { color: #64748b; margin-bottom: 20px; }
.suggestions { display: flex; gap: 8px; justify-content: center; flex-wrap: wrap; }
.suggestions .ant-tag { padding: 6px 14px; border-radius: 20px; cursor: pointer; }
.message { display: flex; gap: 12px; margin-bottom: 20px; }
.message.user { flex-direction: row-reverse; }
.message.user .message-bubble { background: #7c3aed; color: white; border-radius: 16px 16px 4px 16px; }
.message-bubble { padding: 12px 16px; background: white; border-radius: 16px 16px 16px 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); line-height: 1.6; font-size: 14px; max-width: 600px; }
.streaming .cursor { animation: blink 1s step-end infinite; }
@keyframes blink { 50% { opacity: 0; } }
.input-area { padding: 16px 24px 24px; background: white; border-top: 1px solid #f1f5f9; }
.input-tools { margin-bottom: 8px; display: flex; gap: 8px; }
.input-wrapper { display: flex; gap: 8px; align-items: flex-end; }
.chat-input { border-radius: 12px; resize: none; }
.send-btn { flex-shrink: 0; background: #7c3aed; border: none; }
.send-btn:hover { background: #6d28d9; }
</style>
