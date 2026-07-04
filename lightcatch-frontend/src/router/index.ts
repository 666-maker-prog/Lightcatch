import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/token'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/LoginView.vue'),
      meta: { title: '登录' },
    },
    {
      path: '/',
      component: () => import('@/components/layout/AppLayout.vue'),
      redirect: '/chat',
      children: [
        {
          path: 'chat',
          name: 'Chat',
          component: () => import('@/views/chat/ChatView.vue'),
          meta: { title: 'AI 聊天' },
        },
        {
          path: 'chat/:id',
          name: 'ChatDetail',
          component: () => import('@/views/chat/ChatView.vue'),
          meta: { title: 'AI 聊天' },
        },
        {
          path: 'knowledge',
          name: 'Knowledge',
          component: () => import('@/views/knowledge/KnowledgeListView.vue'),
          meta: { title: '素材库' },
        },
        {
          path: 'writing',
          name: 'Writing',
          component: () => import('@/views/writing/WritingView.vue'),
          meta: { title: 'AI 写作' },
        },
        {
          path: 'workflow',
          name: 'Workflow',
          component: () => import('@/views/workflow/WorkflowView.vue'),
          meta: { title: '工作流' },
        },
        {
          path: 'drafts',
          name: 'Drafts',
          component: () => import('@/views/drafts/DraftsView.vue'),
          meta: { title: '草稿箱' },
        },
        {
          path: 'settings/models',
          name: 'ModelSettings',
          component: () => import('@/views/settings/ModelSettingsView.vue'),
          meta: { title: '模型管理' },
        },
      ],
    },
  ],
})

router.beforeEach((to, _from, next) => {
  document.title = (to.meta.title as string)
    ? `${to.meta.title} - LightCatch 拾光`
    : 'LightCatch 拾光'
  if (to.name !== 'Login' && !getToken()) {
    next({ name: 'Login' })
  } else {
    next()
  }
})

export default router
