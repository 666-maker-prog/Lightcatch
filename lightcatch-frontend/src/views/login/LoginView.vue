<template>
  <div class="login-page">
    <div class="login-container">
      <!-- 左侧品牌区 -->
      <div class="brand-section">
        <div class="brand-content">
          <div class="brand-logo">
            <svg viewBox="0 0 40 40" width="48" height="48">
              <circle cx="20" cy="20" r="18" fill="rgba(255,255,255,0.15)" />
              <path d="M12 28 L20 8 L28 28" stroke="#c4b5fd" stroke-width="2.5" fill="none" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M15 22 L25 22" stroke="#c4b5fd" stroke-width="2" fill="none" stroke-linecap="round"/>
            </svg>
          </div>
          <h1 class="brand-title">拾光</h1>
          <p class="brand-subtitle">LightCatch</p>
          <p class="brand-desc">AI 内容工坊 · 让创作更简单</p>
          <div class="brand-features">
            <div class="feature-item">
              <CheckCircleOutlined />
              <span>AI 智能写作</span>
            </div>
            <div class="feature-item">
              <CheckCircleOutlined />
              <span>素材库风格模仿</span>
            </div>
            <div class="feature-item">
              <CheckCircleOutlined />
              <span>工作流自动化</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录 -->
      <div class="form-section">
        <div class="form-container">
          <h2 class="form-title">{{ isLogin ? '欢迎回来' : '创建账号' }}</h2>
          <p class="form-desc">{{ isLogin ? '登录到你的创作空间' : '开始你的 AI 创作之旅' }}</p>

          <a-form
            :model="form"
            :rules="rules"
            @finish="handleSubmit"
            layout="vertical"
            size="large"
          >
            <a-form-item name="username" label="用户名">
              <a-input
                v-model:value="form.username"
                placeholder="请输入用户名"
                prefix="user"
              >
                <template #prefix><UserOutlined style="color: #999" /></template>
              </a-input>
            </a-form-item>

            <a-form-item name="password" label="密码">
              <a-input-password
                v-model:value="form.password"
                placeholder="请输入密码"
              >
                <template #prefix><LockOutlined style="color: #999" /></template>
              </a-input-password>
            </a-form-item>

            <a-form-item v-if="!isLogin" name="realName" label="昵称">
              <a-input v-model:value="form.realName" placeholder="你的昵称（选填）" />
            </a-form-item>

            <a-form-item>
              <a-button type="primary" html-type="submit" :loading="loading" block size="large">
                {{ isLogin ? '登录' : '注册' }}
              </a-button>
            </a-form-item>
          </a-form>

          <div class="switch-mode">
            <span>{{ isLogin ? '还没有账号？' : '已有账号？' }}</span>
            <a-button type="link" @click="isLogin = !isLogin">
              {{ isLogin ? '立即注册' : '去登录' }}
            </a-button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { UserOutlined, LockOutlined, CheckCircleOutlined } from '@ant-design/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { message } from 'ant-design-vue'

const router = useRouter()
const authStore = useAuthStore()
const isLogin = ref(true)
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  realName: '',
})

const rules: any = {
  username: [{ required: true, message: '请输入用户名' }],
  password: [{ required: true, message: '请输入密码' }, { min: 4, message: '密码至少4位' }],
}

async function handleSubmit() {
  loading.value = true
  try {
    if (isLogin.value) {
      await authStore.login(form.username, form.password)
      router.push('/chat')
    } else {
      await authStore.register(form.username, form.password, form.realName)
      message.success('注册成功，请登录')
      isLogin.value = true
    }
  } catch {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-container {
  display: flex;
  width: 880px;
  min-height: 560px;
  background: white;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.brand-section {
  width: 420px;
  background: linear-gradient(135deg, #1e1b4b 0%, #312e81 50%, #4c1d95 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  position: relative;
  overflow: hidden;
}

.brand-section::before {
  content: '';
  position: absolute;
  width: 300px;
  height: 300px;
  background: radial-gradient(circle, rgba(124, 58, 237, 0.2) 0%, transparent 70%);
  top: -100px;
  right: -100px;
  border-radius: 50%;
}

.brand-section::after {
  content: '';
  position: absolute;
  width: 200px;
  height: 200px;
  background: radial-gradient(circle, rgba(167, 139, 250, 0.15) 0%, transparent 70%);
  bottom: -50px;
  left: -50px;
  border-radius: 50%;
}

.brand-content {
  position: relative;
  z-index: 1;
  color: white;
}

.brand-logo {
  margin-bottom: 16px;
}

.brand-title {
  font-size: 36px;
  font-weight: 700;
  margin: 0;
  color: #e0e7ff;
}

.brand-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.5);
  letter-spacing: 4px;
  text-transform: uppercase;
  margin: 4px 0 20px;
}

.brand-desc {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
  margin-bottom: 32px;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  color: rgba(255, 255, 255, 0.7);
  font-size: 14px;
}

.feature-item .anticon {
  color: #a78bfa;
  font-size: 16px;
}

.form-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.form-container {
  width: 100%;
  max-width: 360px;
}

.form-title {
  font-size: 24px;
  font-weight: 600;
  color: #1e1b4b;
  margin: 0 0 4px;
}

.form-desc {
  color: #94a3b8;
  margin-bottom: 28px;
  font-size: 14px;
}

.switch-mode {
  text-align: center;
  color: #94a3b8;
  font-size: 14px;
}

.switch-mode .ant-btn {
  padding: 0 4px;
}

:deep(.ant-form-item) {
  margin-bottom: 20px;
}

:deep(.ant-form-item-label) {
  padding-bottom: 4px;
}

:deep(.ant-input-affix-wrapper) {
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  padding: 8px 12px;
}

:deep(.ant-btn-primary) {
  height: 44px;
  border-radius: 10px;
  font-size: 16px;
  background: linear-gradient(135deg, #7c3aed 0%, #6366f1 100%);
  border: none;
}

:deep(.ant-btn-primary:hover) {
  background: linear-gradient(135deg, #6d28d9 0%, #4f46e5 100%);
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(124, 58, 237, 0.3);
}
</style>
