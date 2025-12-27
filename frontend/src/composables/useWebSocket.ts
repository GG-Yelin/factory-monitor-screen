import { ref, onMounted, onUnmounted } from 'vue'
import type { DashboardData } from '@/types'

export function useWebSocket() {
  const data = ref<DashboardData | null>(null)
  const connected = ref(false)
  const error = ref<string | null>(null)

  let ws: WebSocket | null = null
  let reconnectTimer: number | null = null
  let reconnectAttempts = 0
  const maxReconnectAttempts = 10
  const reconnectDelay = 3000

  const connect = () => {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = window.location.host
    const wsUrl = `${protocol}//${host}/ws/monitor`

    console.log('Connecting to WebSocket:', wsUrl)

    ws = new WebSocket(wsUrl)

    ws.onopen = () => {
      console.log('WebSocket connected')
      connected.value = true
      error.value = null
      reconnectAttempts = 0
    }

    ws.onmessage = (event) => {
      try {
        const parsedData = JSON.parse(event.data)
        data.value = parsedData
      } catch (e) {
        console.error('Failed to parse WebSocket message:', e)
      }
    }

    ws.onclose = () => {
      console.log('WebSocket disconnected')
      connected.value = false
      scheduleReconnect()
    }

    ws.onerror = (e) => {
      console.error('WebSocket error:', e)
      error.value = 'WebSocket connection error'
    }
  }

  const scheduleReconnect = () => {
    if (reconnectAttempts < maxReconnectAttempts) {
      reconnectAttempts++
      console.log(`Reconnecting in ${reconnectDelay}ms... (attempt ${reconnectAttempts})`)
      reconnectTimer = window.setTimeout(() => {
        connect()
      }, reconnectDelay)
    } else {
      error.value = 'Max reconnection attempts reached'
    }
  }

  const disconnect = () => {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
    if (ws) {
      ws.close()
      ws = null
    }
  }

  const refresh = () => {
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send('refresh')
    }
  }

  onMounted(() => {
    connect()
  })

  onUnmounted(() => {
    disconnect()
  })

  return {
    data,
    connected,
    error,
    refresh
  }
}
