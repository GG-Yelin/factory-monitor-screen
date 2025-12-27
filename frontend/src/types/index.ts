export interface DeviceInfo {
  deviceId: string
  deviceName: string
  deviceType: string
  itemId: string
  itemName: string
  status: number // 0: 离线, 1: 在线, 2: 报警
}

export interface DataPoint {
  id: string
  name: string
  dataType: number
  set: boolean
  isCoil: boolean
  unit: string
  value: string
  valueString: string
  deviceId: string
  deviceName: string
}

export interface ProjectInfo {
  itemId: string
  itemName: string
  lnglat: string
  parentGroupId: string
  deviceCount: number
  onlineCount: number
}

export interface ProductionTrend {
  date: string
  production: number
  plan: number
  rate: number
}

export interface AlarmInfo {
  id: string
  deviceId: string
  deviceName: string
  alarmType: string
  alarmContent: string
  alarmTime: string
  level: number // 1: 一般, 2: 重要, 3: 紧急
  status: number // 0: 未处理, 1: 已处理
}

export interface DashboardData {
  totalDevices: number
  onlineDevices: number
  offlineDevices: number
  alarmDevices: number
  todayProduction: number
  planProduction: number
  productionRate: number
  equipmentEfficiency: number
  qualityRate: number
  runningRate: number
  projects: ProjectInfo[]
  devices: DeviceInfo[]
  dataPoints: DataPoint[]
  productionTrend: ProductionTrend[]
  alarms: AlarmInfo[]
  updateTime: number
}

// 每日统计
export interface DailyStatistics {
  id: number
  statisticsDate: string
  totalProduction: number
  totalPlan: number
  completionRate: number
  qualifiedQuantity: number
  qualityRate: number
  totalDevices: number
  onlineDevices: number
  alarmCount: number
  avgOee: number
  totalRunningMinutes: number
  createdAt: string
  updatedAt: string
}

// 月度统计
export interface MonthlyStatistics {
  id: number
  year: number
  month: number
  totalProduction: number
  totalPlan: number
  completionRate: number
  qualifiedQuantity: number
  qualityRate: number
  avgDailyProduction: number
  maxDailyProduction: number
  minDailyProduction: number
  totalAlarmCount: number
  avgOee: number
  workDays: number
  createdAt: string
  updatedAt: string
}

// 统计概览
export interface StatisticsOverview {
  today: DailyStatistics | null
  currentMonth: MonthlyStatistics | null
  weekTrend: DailyStatistics[]
  yearTrend: MonthlyStatistics[]
}
