import axios from 'axios'
import { getToken, removeToken } from './token'
import { message } from 'ant-design-vue'
import { addLog } from './debug'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000,
})

request.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers['X-Access-Token'] = token
    }
    if (config.method === 'get') {
      config.params = { ...config.params, _t: Date.now() }
    }
    // Log the request
    const params = config.params ? '?' + new URLSearchParams(config.params).toString() : ''
    addLog('REQ', `${config.method?.toUpperCase()} ${config.url}${params}`,
      config.data ? JSON.stringify(config.data).slice(0, 200) : '')
    return config
  },
  (error) => {
    addLog('ERR', 'Request Error: ' + error.message)
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
    const body = response.data
    addLog('RES', `${response.config.url}`, JSON.stringify(body).slice(0, 300))
    if (body && body.success === false) {
      message.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message))
    }
    return body
  },
  (error) => {
    const status = error.response?.status
    const url = error.config?.url || ''
    addLog('ERR', `${status} ${url} - ${error.message}`)
    if (status === 401) {
      removeToken()
      window.location.href = '/login'
      message.error('登录已过期，请重新登录')
    } else {
      message.error(error.message || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request
