// 用户 & 认证
export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  email?: string
  phone?: string
}

export interface LoginResponse {
  token: string
  userId: number
  username: string
  role: string
}

// 海洋数据
export interface OceanData {
  id?: number
  dataTime?: string
  longitude?: number
  latitude?: number
  seaArea?: string
  temperature?: number
  salinity?: number
  oxygen?: number
  ph?: number
  currentSpeed?: number
  currentDir?: string
  tideLevel?: number
  waveHeight?: number
  pressure?: number
}

export interface OceanDataQuery {
  seaArea?: string
  startTime?: string
  endTime?: string
  minLat?: number
  maxLat?: number
  minLng?: number
  maxLng?: number
}

// 监测点位
export interface MonitorPoint {
  id?: number
  name: string
  longitude: number
  latitude: number
  type: string
  status: string
  description?: string
  seaArea?: string
}

// 预警
export interface Alert {
  id?: number
  type: string
  level: string
  title: string
  content: string
  seaArea?: string
  startTime?: string
  endTime?: string
  status: string
}

// 科普
export interface Knowledge {
  id?: number
  title: string
  category: string
  coverUrl?: string
  summary?: string
  content: string
  author?: string
  viewCount?: number
  status?: string
}
