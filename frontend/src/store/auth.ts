import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authAPI } from '@/api'
import type { LoginRequest, RegisterRequest } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')
  const role = ref(localStorage.getItem('role') || '')

  const login = async (data: LoginRequest) => {
    const res: any = await authAPI.login(data)
    token.value = res.data.token
    username.value = res.data.username
    role.value = res.data.role
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('username', res.data.username)
    localStorage.setItem('role', res.data.role)
    return res
  }

  const register = (data: RegisterRequest) => authAPI.register(data)

  const logout = () => {
    token.value = ''
    username.value = ''
    role.value = ''
    localStorage.clear()
  }

  return { token, username, role, login, register, logout }
})
