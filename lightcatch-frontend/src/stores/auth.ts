import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getToken, setToken, removeToken } from '@/utils/token'
import { login as loginApi, register as registerApi, getUserInfo } from '@/api/auth'
import { message } from 'ant-design-vue'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(getToken())
  const userInfo = ref<any>(null)

  async function login(username: string, password: string) {
    const res = await loginApi({ username, password })
    const newToken = res.data
    setToken(newToken)
    token.value = newToken
    await fetchUserInfo()
    message.success('登录成功')
  }

  async function register(username: string, password: string, realName?: string) {
    await registerApi({ username, password, realName })
    message.success('注册成功')
  }

  async function fetchUserInfo() {
    try {
      const res = await getUserInfo()
      userInfo.value = res.data
    } catch {
      // ignore
    }
  }

  function logout() {
    removeToken()
    token.value = null
    userInfo.value = null
  }

  return { token, userInfo, login, register, fetchUserInfo, logout }
})
