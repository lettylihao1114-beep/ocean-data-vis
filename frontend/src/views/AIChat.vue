<script setup lang="ts">
import { ref, nextTick, onBeforeUnmount } from 'vue'
import SvgIcon from '@/components/SvgIcon.vue'

interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
}

const messages = ref<ChatMessage[]>([])
const input = ref('')
const streaming = ref(false)
const chatBox = ref<HTMLElement>()
let abortController: AbortController | null = null

// 欢迎消息
messages.value.push({
  role: 'assistant',
  content: '🌊 你好！我是**南海智答**，你的海洋数据 AI 助手。\n\n我可以帮你解答海洋环境相关问题，包括水温、盐度、洋流、潮汐、污染监测、海洋生态等。试着问我吧！',
})

const scrollToBottom = async () => {
  await nextTick()
  if (chatBox.value) chatBox.value.scrollTop = chatBox.value.scrollHeight
}

const sendMessage = async () => {
  const text = input.value.trim()
  if (!text || streaming.value) return

  input.value = ''
  messages.value.push({ role: 'user', content: text })
  messages.value.push({ role: 'assistant', content: '' })
  await scrollToBottom()

  streaming.value = true
  abortController = new AbortController()

  try {
    // 构建历史对话
    const history = messages.value.slice(0, -2).map(m => ({
      role: m.role,
      content: m.content,
    }))

    const token = localStorage.getItem('token')
    const response = await fetch('/api/ai/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`,
      },
      body: JSON.stringify({ message: text, history }),
      signal: abortController.signal,
    })

    if (!response.ok) {
      const err = await response.text()
      throw new Error(err || '请求失败')
    }

    const reader = response.body?.getReader()
    if (!reader) throw new Error('不支持流式读取')

    const decoder = new TextDecoder()
    let buffer = ''
    let streamDone = false

    // 安全超时：30 秒无响应自动停止
    let timeoutId: ReturnType<typeof setTimeout> | null = null
    const resetTimeout = () => {
      if (timeoutId) clearTimeout(timeoutId)
      timeoutId = setTimeout(() => {
        if (!streamDone) {
          streamDone = true
          reader.cancel()
        }
      }, 30_000)
    }
    resetTimeout()

    while (!streamDone) {
      const { done, value } = await reader.read()
      if (done) break

      resetTimeout()
      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      for (const line of lines) {
        if (line.startsWith('event:done')) {
          streamDone = true
          break
        }
        if (line.startsWith('event:error')) {
          const errLine = lines.find(l => l.startsWith('data:'))
          const errMsg = errLine ? errLine.substring(5).trim() : 'AI 服务异常'
          const lastMsg = messages.value[messages.value.length - 1]
          if (lastMsg && lastMsg.role === 'assistant') {
            lastMsg.content += `\n\n⚠️ ${errMsg}`
          }
          streamDone = true
          break
        }
        if (line.startsWith('event:token')) continue
        if (line.startsWith('data:')) {
          const data = line.substring(5).trim()
          if (data) {
            const lastMsg = messages.value[messages.value.length - 1]
            if (lastMsg && lastMsg.role === 'assistant') {
              lastMsg.content += data
              await nextTick()
              scrollToBottom()
            }
          }
        }
      }
    }

    if (timeoutId) clearTimeout(timeoutId)
  } catch (e: any) {
    if (e.name !== 'AbortError') {
      const lastMsg = messages.value[messages.value.length - 1]
      if (lastMsg && lastMsg.role === 'assistant' && !lastMsg.content) {
        lastMsg.content = '⚠️ 连接失败，请检查 AI 服务配置后重试。'
      }
    }
  }

  streaming.value = false
  abortController = null
}

const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    sendMessage()
  }
}

const stopStreaming = () => {
  abortController?.abort()
  streaming.value = false
}

// 简易 Markdown 渲染（加粗 + 换行）
const renderMarkdown = (text: string) => {
  return text
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/\*\*(.+?)\*\*/g, '<b>$1</b>')
    .replace(/\n/g, '<br/>')
}

// 快捷提问
const quickQuestions = [
  '南海水温最高的海域是哪个？',
  '什么是南海季风洋流？',
  '南海污染主要来源有哪些？',
  '红树林对海洋生态的作用？',
]

const sendQuick = (q: string) => {
  input.value = q
  sendMessage()
}

onBeforeUnmount(() => abortController?.abort())
</script>

<template>
  <div class="chat-page">
    <div class="ai-chat">
      <!-- 头部 -->
      <div class="chat-header">
        <span class="chat-logo">🤖</span>
        <div>
          <div class="chat-title">南海智答</div>
          <div class="chat-subtitle">DeepSeek + RAG 向量检索增强 · 海洋知识 AI 助手</div>
        </div>
      </div>

      <!-- 快捷提问 -->
      <div class="quick-chips" v-if="messages.length <= 1">
        <span class="chips-label">💡 试试问我：</span>
        <button
          v-for="q in quickQuestions"
          :key="q"
          class="chip-btn"
          @click="sendQuick(q)"
        >{{ q }}</button>
      </div>

      <!-- 消息区 -->
      <div class="chat-messages" ref="chatBox">
        <div
          v-for="(msg, i) in messages"
          :key="i"
          class="msg-row"
          :class="msg.role"
        >
          <div class="msg-avatar">{{ msg.role === 'user' ? '👤' : '🤖' }}</div>
          <div class="msg-bubble" v-html="renderMarkdown(msg.content)"></div>
        </div>

        <!-- 流式输出光标 -->
        <div v-if="streaming" class="typing-indicator">
          <span></span><span></span><span></span>
        </div>
      </div>

      <!-- 输入区 -->
      <div class="chat-input-bar">
        <div class="input-wrapper">
          <textarea
            v-model="input"
            @keydown="handleKeydown"
            placeholder="输入你的海洋问题，如：南海水温最高的海域是哪个？"
            :rows="streaming ? 2 : 1"
            :disabled="streaming"
          />
          <button v-if="streaming" class="btn-stop-inline" @click="stopStreaming">⏹ 停止</button>
          <button v-else class="btn-send-inline" @click="sendMessage" :disabled="!input.trim()">
            <span class="btn-send-icon">↑</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.chat-page {
  max-width: 1100px; margin: 0 auto; padding: 4px 24px;
}
.ai-chat {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 180px);
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 1px 8px rgba(0,0,0,.06);
  border: 1px solid #eaf0f6;
  overflow: hidden;
}

/* 头部 */
.chat-header {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 24px;
  background: linear-gradient(135deg, #eff6ff 0%, #ecfdf5 100%);
  border-bottom: 1px solid #e8edf4;
  flex-shrink: 0;
}
.chat-logo { font-size: 34px; }
.chat-title { font-size: 18px; font-weight: 700; color: #1e293b; }
.chat-subtitle { font-size: 12px; color: #94a3b8; margin-top: 2px; }

/* 快捷提问 */
.quick-chips {
  padding: 14px 24px;
  background: #fafcfd;
  border-bottom: 1px solid #f1f5f9;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}
.chips-label { font-size: 12px; color: #94a3b8; flex-shrink: 0; }
.chip-btn {
  padding: 6px 14px;
  border: 1px solid #d0d7de;
  border-radius: 16px;
  background: #fff;
  color: #475569;
  font-size: 12px;
  cursor: pointer;
  transition: all .15s;
  white-space: nowrap;
}
.chip-btn:hover { border-color: #0ea5e9; color: #0ea5e9; background: #f0f9ff; }

/* 消息区 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px 24px;
  background: #f8fafc;
}
.msg-row {
  display: flex;
  gap: 10px;
  margin-bottom: 18px;
  align-items: flex-start;
}
.msg-row.user { flex-direction: row-reverse; }
.msg-avatar {
  width: 38px; height: 38px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
  background: #e8edf4;
}
.msg-row.user .msg-avatar { background: #dbeafe; }
.msg-row.assistant .msg-avatar { background: #d1fae5; }
.msg-bubble {
  max-width: 75%;
  padding: 12px 18px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.75;
  color: #1e293b;
  word-break: break-word;
}
.msg-row.user .msg-bubble {
  background: #3b82f6;
  color: #fff;
  border-bottom-right-radius: 4px;
}
.msg-row.user .msg-bubble :deep(b) { color: #fff; }
.msg-row.assistant .msg-bubble {
  background: #fff;
  border: 1px solid #e8edf4;
  border-bottom-left-radius: 4px;
}
.msg-bubble :deep(b) { color: #0ea5e9; }

/* 打字动画 */
.typing-indicator {
  display: flex; gap: 5px; margin-left: 48px; padding: 8px 0;
}
.typing-indicator span {
  width: 8px; height: 8px; border-radius: 50%;
  background: #94a3b8;
  animation: bounce 1.4s infinite ease-in-out both;
}
.typing-indicator span:nth-child(1) { animation-delay: -0.32s; }
.typing-indicator span:nth-child(2) { animation-delay: -0.16s; }
@keyframes bounce { 0%,80%,100%{transform:scale(0)} 40%{transform:scale(1)} }

/* 输入区 */
.chat-input-bar {
  padding: 14px 20px;
  border-top: 1px solid #e8edf4;
  background: #fff;
}
.input-wrapper {
  position: relative;
  display: flex;
  align-items: flex-end;
}
.chat-input-bar textarea {
  width: 100%;
  resize: none;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 12px 52px 12px 18px;
  font-size: 14px;
  font-family: inherit;
  line-height: 1.5;
  outline: none;
  transition: border-color 0.2s, box-shadow 0.2s;
  background: #f8fafc;
}
.chat-input-bar textarea:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59,130,246,.08); background: #fff; }
.chat-input-bar textarea:disabled { background: #f1f5f9; }
.btn-send-inline, .btn-stop-inline {
  position: absolute;
  right: 8px;
  bottom: 8px;
  border: none;
  cursor: pointer;
  transition: all 0.2s;
  display: flex; align-items: center; justify-content: center;
}
.btn-send-inline {
  width: 36px; height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  color: #fff;
  box-shadow: 0 2px 6px rgba(59,130,246,.25);
}
.btn-send-inline:hover:not(:disabled) { background: linear-gradient(135deg, #2563eb, #1d4ed8); box-shadow: 0 3px 10px rgba(59,130,246,.35); transform: scale(1.05); }
.btn-send-inline:disabled { background: #cbd5e1; color: #94a3b8; box-shadow: none; cursor: not-allowed; transform: none; }
.btn-send-icon { font-size: 16px; font-weight: 700; line-height: 1; }
.btn-stop-inline {
  width: auto; height: 36px; border-radius: 10px;
  padding: 0 14px; font-size: 13px; font-weight: 600; white-space: nowrap;
  background: #fef2f2; color: #ef4444;
}
.btn-stop-inline:hover { background: #fee2e2; }
</style>
