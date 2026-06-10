import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authAPI, profileAPI } from '@/api'
import type { LoginRequest, RegisterRequest, UpdateProfileRequest } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const username = ref(localStorage.getItem('username') || '')
  const role = ref(localStorage.getItem('role') || '')
  const email = ref('')
  const phone = ref('')
  const avatar = ref('')

  const login = async (data: LoginRequest) => {
    const res: any = await authAPI.login(data)
    const payload = res?.data
    if (!payload) {
      throw new Error(res?.message || '登录失败')
    }
    token.value = payload.token
    username.value = payload.username
    role.value = payload.role
    localStorage.setItem('token', payload.token)
    localStorage.setItem('username', payload.username)
    localStorage.setItem('role', payload.role)
    return res
  }

  const register = (data: RegisterRequest) => authAPI.register(data)

  const fetchProfile = async () => {
    const res: any = await profileAPI.getProfile()
    const data = res?.data
    if (data) {
      email.value = data.email || ''
      phone.value = data.phone || ''
      avatar.value = data.avatar || ''
    }
  }

  const updateProfile = async (data: UpdateProfileRequest) => {
    await profileAPI.updateProfile(data)
    if (data.email !== undefined) email.value = data.email
    if (data.phone !== undefined) phone.value = data.phone
  }

  const changePassword = async (data: { oldPassword: string; newPassword: string }) => {
    await profileAPI.changePassword(data)
  }

  const logout = () => {
    token.value = ''
    username.value = ''
    role.value = ''
    email.value = ''
    phone.value = ''
    avatar.value = ''
    localStorage.clear()
  }

  return { token, username, role, email, phone, avatar, login, register, fetchProfile, updateProfile, changePassword, logout }
})
