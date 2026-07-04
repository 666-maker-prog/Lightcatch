<template>
  <a-layout style="min-height: 100vh">
    <!-- 侧边栏 -->
    <a-layout-sider
      v-model:collapsed="collapsed"
      :trigger="null"
      collapsible
      style="
        background: linear-gradient(180deg, #1e1b4b 0%, #312e81 100%);
        position: fixed;
        left: 0;
        top: 0;
        bottom: 0;
        z-index: 100;
      "
      :width="240"
    >
      <!-- Logo -->
      <div class="logo" @click="router.push('/')">
        <div class="logo-icon">
          <svg viewBox="0 0 40 40" width="32" height="32">
            <circle cx="20" cy="20" r="18" fill="rgba(255,255,255,0.15)" />
            <path d="M12 28 L20 8 L28 28" stroke="#a78bfa" stroke-width="2.5" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M15 22 L25 22" stroke="#a78bfa" stroke-width="2" fill="none" stroke-linecap="round"/>
          </svg>
        </div>
        <transition name="fade">
          <span v-if="!collapsed" class="logo-text">拾光 LightCatch</span>
        </transition>
      </div>

      <!-- 导航菜单 -->
      <a-menu
        theme="dark"
        mode="inline"
        :selectedKeys="[currentRoute]"
        style="background: transparent; border-inline-end: none; margin-top: 8px;"
      >
        <a-menu-item key="chat" @click="router.push('/chat')">
          <template #icon><MessageOutlined style="font-size: 18px" /></template>
          <span>AI 对话</span>
        </a-menu-item>
        <a-menu-item key="knowledge" @click="router.push('/knowledge')">
          <template #icon><DatabaseOutlined style="font-size: 18px" /></template>
          <span>素材库</span>
        </a-menu-item>
        <a-menu-item key="writing" @click="router.push('/writing')">
          <template #icon><EditOutlined style="font-size: 18px" /></template>
          <span>AI 写作</span>
        </a-menu-item>
        <a-menu-item key="workflow" @click="router.push('/workflow')">
          <template #icon><ApartmentOutlined style="font-size: 18px" /></template>
          <span>工作流</span>
        </a-menu-item>
        <a-menu-item key="drafts" @click="router.push('/drafts')">
          <template #icon><FileTextOutlined style="font-size: 18px" /></template>
          <span>草稿箱</span>
        </a-menu-item>
        <a-menu-divider style="background: rgba(255,255,255,0.1); margin: 8px 16px" />
        <a-menu-item key="settings" @click="router.push('/settings/models')">
          <template #icon><SettingOutlined style="font-size: 18px" /></template>
          <span>模型管理</span>
        </a-menu-item>
      </a-menu>

      <!-- 底部用户信息 -->
      <div class="sidebar-footer" v-if="!collapsed">
        <a-divider style="background: rgba(255,255,255,0.1); margin: 8px 16px" />
        <div class="user-info">
          <a-avatar :size="32" style="background-color: #7c3aed">
            {{ (authStore.userInfo?.username || 'U')[0].toUpperCase() }}
          </a-avatar>
          <div class="user-detail">
            <div class="user-name">{{ authStore.userInfo?.realName || authStore.userInfo?.username || '用户' }}</div>
            <div class="user-role">创作者</div>
          </div>
          <a-button type="text" size="small" @click="handleLogout" style="color: rgba(255,255,255,0.5)">
            <LogoutOutlined />
          </a-button>
        </div>
      </div>
    </a-layout-sider>

    <!-- 主内容区 -->
    <a-layout :style="{ marginLeft: collapsed ? '80px' : '240px', transition: 'margin-left 0.2s' }">
      <a-layout-content style="min-height: 100vh; background: #f8fafc;">
        <router-view />
      </a-layout-content>
    </a-layout>

    <!-- 折叠按钮 -->
    <div class="collapse-trigger" @click="collapsed = !collapsed">
      <MenuFoldOutlined v-if="!collapsed" />
      <MenuUnfoldOutlined v-else />
    </div>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import {
  MessageOutlined, DatabaseOutlined, EditOutlined,
  ApartmentOutlined, SettingOutlined, FileTextOutlined,
  MenuFoldOutlined, MenuUnfoldOutlined, LogoutOutlined,
} from '@ant-design/icons-vue'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const collapsed = ref(false)

const currentRoute = computed(() => {
  const path = route.path.split('/')[1]
  return path || 'chat'
})

function handleLogout() {
  authStore.logout()
  router.push('/login')
}

onMounted(() => {
  if (!authStore.userInfo) {
    authStore.fetchUserInfo()
  }
})
</script>

<style scoped>
.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 24px 20px 20px;
  cursor: pointer;
}
.logo-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.logo-text {
  color: #e0e7ff;
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 0.5px;
  white-space: nowrap;
}
:deep(.ant-menu-item) {
  height: 44px;
  line-height: 44px;
  margin: 2px 8px;
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.7);
}
:deep(.ant-menu-item-selected) {
  background: rgba(124, 58, 237, 0.3) !important;
  color: #e0e7ff !important;
}
:deep(.ant-menu-item:hover) {
  color: #c4b5fd !important;
}
:deep(.ant-menu-item-selected .anticon) {
  color: #a78bfa !important;
}
.sidebar-footer {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding-bottom: 16px;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 16px;
}
.user-detail {
  flex: 1;
  min-width: 0;
}
.user-name {
  color: rgba(255, 255, 255, 0.9);
  font-size: 13px;
  font-weight: 500;
  line-height: 1.3;
}
.user-role {
  color: rgba(255, 255, 255, 0.4);
  font-size: 11px;
}
.collapse-trigger {
  position: fixed;
  bottom: 16px;
  left: 16px;
  z-index: 101;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(30, 27, 75, 0.8);
  border-radius: 8px;
  color: rgba(255, 255, 255, 0.5);
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}
.collapse-trigger:hover {
  background: rgba(30, 27, 75, 0.95);
  color: rgba(255, 255, 255, 0.8);
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
