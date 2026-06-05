<script setup lang="ts">
import { ref, computed, nextTick, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import SvgIcon from '@/components/SvgIcon.vue'

interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
  time: string
}

const messages = ref<ChatMessage[]>([])
const input = ref('')
const streaming = ref(false)
const chatBox = ref<HTMLElement>()
let abortController: AbortController | null = null

// 欢迎消息
const now = () => new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
messages.value.push({
  role: 'assistant',
  content: '🌊 你好！我是**南海智答**，基于 DeepSeek + RAG 向量检索的海洋数据 AI 助手。\n\n我可以帮你解答海洋环境相关问题，包括水温、盐度、洋流、潮汐、污染监测、海洋生态等。试着问我吧！',
  time: now(),
})

const hasConversation = computed(() => messages.value.length > 1)

// 推荐问题
const suggestions = [
  { icon: '🌡️', text: '南海水温最高的海域是哪个？' },
  { icon: '🔄', text: '南海有哪些主要的洋流系统？' },
  { icon: '🛡️', text: '南海污染监测的主要指标有哪些？' },
  { icon: '🌙', text: '南海潮汐有什么特点？' },
]

const clickSuggestion = (text: string) => {
  input.value = text
  sendMessage()
}

const scrollToBottom = async () => {
  await nextTick()
  if (chatBox.value) chatBox.value.scrollTop = chatBox.value.scrollHeight
}

const clearMessages = () => {
  messages.value = [{
    role: 'assistant',
    content: '🌊 对话已清空。有什么想了解的吗？',
    time: now(),
  }]
}

const sendMessage = async () => {
  const text = input.value.trim()
  if (!text || streaming.value) return

  input.value = ''
  const msgTime = now()
  messages.value.push({ role: 'user', content: text, time: msgTime })
  messages.value.push({ role: 'assistant', content: '', time: now() })
  await scrollToBottom()

  streaming.value = true
  abortController = new AbortController()

  try {
    const history = messages.value.slice(0, -2).map(m => ({ role: m.role, content: m.content }))
    const token = localStorage.getItem('token')

    const response = await fetch('/api/ai/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
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

    let timeoutId: ReturnType<typeof setTimeout> | null = null
    const resetTimeout = () => {
      if (timeoutId) clearTimeout(timeoutId)
      timeoutId = setTimeout(() => { if (!streamDone) { streamDone = true; reader.cancel() } }, 30_000)
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
        if (line.startsWith('event:done')) { streamDone = true; break }
        if (line.startsWith('event:error')) {
          const errLine = lines.find(l => l.startsWith('data:'))
          const errMsg = errLine ? errLine.substring(5).trim() : 'AI 服务异常'
          const lastMsg = messages.value[messages.value.length - 1]
          if (lastMsg && lastMsg.role === 'assistant') lastMsg.content += `\n\n⚠️ ${errMsg}`
          streamDone = true; break
        }
        if (line.startsWith('event:token')) continue
        if (line.startsWith('data:')) {
          const data = line.substring(5).trim()
          if (data) {
            const lastMsg = messages.value[messages.value.length - 1]
            if (lastMsg && lastMsg.role === 'assistant') {
              lastMsg.content += data
              await nextTick(); scrollToBottom()
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
    e.preventDefault(); sendMessage()
  }
}

const stopStreaming = () => {
  abortController?.abort()
  streaming.value = false
}

// Markdown 渲染
const renderMarkdown = (text: string) => {
  return text
    .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
    .replace(/^### (.+)$/gm, '<h3>$1</h3>')
    .replace(/^## (.+)$/gm, '<h2>$1</h2>')
    .replace(/\*\*(.+?)\*\*/g, '<b>$1</b>')
    .replace(/^- (.+)$/gm, '<li>$1</li>')
    .replace(/^(\d+)\. (.+)$/gm, '<li>$2</li>')
    .replace(/\n\n/g, '<br/><br/>')
    .replace(/\n/g, '<br/>')
}

onBeforeUnmount(() => abortController?.abort())
</script>

<template>
  <div class="ai-chat">
    <!-- 头部 -->
    <div class="chat-header">
      <div class="chat-header-left">
        <span class="chat-logo"><SvgIcon name="bot" :size="28" color="#0ea5e9"/></span>
        <div>
          <div class="chat-title">南海智答</div>
          <div class="chat-subtitle">DeepSeek · RAG 向量检索增强</div>
        </div>
      </div>
      <button v-if="hasConversation" class="btn-clear" @click="clearMessages" title="清空对话">
        <SvgIcon name="trash" :size="15"/> 清空
      </button>
    </div>

    <!-- 消息区 -->
    <div class="chat-messages" ref="chatBox">
      <!-- 欢迎面板（无对话时显示） -->
      <div v-if="!hasConversation" class="welcome-panel">
        <div class="welcome-icon">
          <SvgIcon name="bot" :size="48" color="#0ea5e9"/>
        </div>
        <h3>你好，我是南海智答</h3>
        <p>基于 DeepSeek 大模型 + RAG 向量检索，助你探索南海海洋科学</p>
        <div class="suggestions">
          <button
            v-for="(s, i) in suggestions"
            :key="i"
            class="suggest-btn"
            @click="clickSuggestion(s.text)"
          >
            <span class="suggest-icon">{{ s.icon }}</span>
            <span>{{ s.text }}</span>
          </button>
        </div>
      </div>

      <!-- 消息列表 -->
      <div v-for="(msg, i) in messages" :key="i" class="msg-row" :class="msg.role">
        <div class="msg-avatar">
          <SvgIcon v-if="msg.role === 'assistant'" name="bot" :size="18" color="#0ea5e9"/>
          <SvgIcon v-else name="user" :size="18" color="#3b82f6"/>
        </div>
        <div class="msg-body">
          <div class="msg-meta">
            <span class="msg-sender">{{ msg.role === 'assistant' ? '南海智答' : '你' }}</span>
            <span class="msg-time">{{ msg.time }}</span>
          </div>
          <div class="msg-bubble" :class="msg.role" v-html="renderMarkdown(msg.content)"></div>
        </div>
      </div>

      <!-- 流式输出光标 -->
      <div v-if="streaming" class="typing-dot">●</div>
    </div>

    <!-- 输入区 -->
    <div class="chat-input-bar">
      <textarea
        v-model="input"
        @keydown="handleKeydown"
        placeholder="输入问题，如：南海水温最高的海域是哪个？"
        rows="2"
        :disabled="streaming"
      />
      <div class="input-actions">
        <span class="char-count" v-if="input.length > 200">{{ input.length }} / 2000</span>
        <button v-if="streaming" class="btn-stop" @click="stopStreaming">
          <span class="stop-icon"></span> 停止
        </button>
        <button v-else class="btn-send" @click="sendMessage" :disabled="!input.trim()">
          <SvgIcon name="send" :size="16"/>
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.ai-chat {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 200px);
  min-height: 500px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 3px rgba(0,0,0,.04);
  border: 1px solid #eaf0f6;
  overflow: hidden;
}

/* ========== 头部 ========== */
.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 20px;
  background: #fff;
  border-bottom: 1px solid #f1f5f9;
  flex-shrink: 0;
}
.chat-header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}
.chat-logo {
  width: 40px; height: 40px;
  border-radius: 10px;
  background: linear-gradient(135deg, #eff6ff, #ecfeff);
  display: flex; align-items: center; justify-content: center;
}
.chat-title { font-size: 16px; font-weight: 700; color: #1e293b; }
.chat-subtitle { font-size: 12px; color: #94a3b8; margin-top: 1px; }

.btn-clear {
  display: flex; align-items: center; gap: 4px;
  padding: 6px 14px; border: 1px solid #e2e8f0; border-radius: 8px;
  background: #fff; color: #94a3b8; font-size: 12px; cursor: pointer;
  transition: all .15s;
}
.btn-clear:hover { color: #ef4444; border-color: #fecaca; background: #fef2f2; }

/* ========== 欢迎面板 ========== */
.welcome-panel {
  display: flex; flex-direction: column; align-items: center;
  padding: 48px 24px; text-align: center;
}
.welcome-icon {
  width: 80px; height: 80px; border-radius: 20px;
  background: linear-gradient(135deg, #eff6ff, #ecfeff);
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 20px;
}
.welcome-panel h3 { margin: 0; font-size: 20px; color: #1e293b; font-weight: 700; }
.welcome-panel > p { margin: 8px 0 28px; color: #94a3b8; font-size: 14px; }
.suggestions {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
  max-width: 520px;
  width: 100%;
}
.suggest-btn {
  display: flex; align-items: center; gap: 8px;
  padding: 12px 16px;
  border: 1px solid #e2e8f0; border-radius: 10px;
  background: #fff; color: #475569; font-size: 13px;
  cursor: pointer; transition: all .15s; text-align: left;
}
.suggest-btn:hover { border-color: #0ea5e9; color: #0ea5e9; background: #f8fafc; }
.suggest-icon { font-size: 18px; flex-shrink: 0; }

/* ========== 消息区 ========== */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 20px 20px 8px;
  background: #f8fafc;
}
.msg-row {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
  align-items: flex-start;
}
.msg-row.user { flex-direction: row-reverse; }
.msg-avatar {
  width: 34px; height: 34px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0; background: #e8edf4;
}
.msg-row.user .msg-avatar { background: #dbeafe; }
.msg-row.assistant .msg-avatar { background: #e0f2fe; }
.msg-body {
  max-width: 70%;
  min-width: 0;
}
.msg-meta {
  display: flex; align-items: center; gap: 8px;
  margin-bottom: 4px; padding: 0 4px;
}
.msg-row.user .msg-meta { justify-content: flex-end; }
.msg-sender { font-size: 12px; font-weight: 600; color: #64748b; }
.msg-time { font-size: 11px; color: #94a3b8; }

.msg-bubble {
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.7;
  color: #1e293b;
  word-break: break-word;
}
.msg-bubble.user {
  background: #3b82f6; color: #fff;
  border-bottom-right-radius: 4px;
}
.msg-bubble.user :deep(b) { color: #fff; }
.msg-bubble.assistant {
  background: #fff; border: 1px solid #e8edf4;
  border-bottom-left-radius: 4px;
}
.msg-bubble :deep(b) { color: #0ea5e9; }
.msg-bubble :deep(h2) { font-size: 16px; margin: 8px 0 4px; }
.msg-bubble :deep(h3) { font-size: 14px; margin: 6px 0 4px; }
.msg-bubble :deep(li) { margin: 2px 0 2px 16px; }

/* 打字光标 */
.typing-dot {
  color: #0ea5e9; font-size: 16px; margin-left: 44px;
  animation: blink 0.8s infinite;
}
@keyframes blink { 0%,100%{opacity:1} 50%{opacity:0} }

/* ========== 输入区 ========== */
.chat-input-bar {
  display: flex; gap: 10px;
  padding: 12px 16px;
  border-top: 1px solid #e8edf4;
  background: #fff; align-items: flex-end;
  flex-shrink: 0;
}
.chat-input-bar textarea {
  flex: 1; resize: none;
  border: 1px solid #e2e8f0; border-radius: 10px;
  padding: 10px 14px; font-size: 14px; font-family: inherit;
  line-height: 1.5; outline: none; transition: border-color 0.2s;
  background: #f8fafc; color: #1e293b;
}
.chat-input-bar textarea::placeholder { color: #94a3b8; }
.chat-input-bar textarea:focus { border-color: #0ea5e9; background: #fff; }
.chat-input-bar textarea:disabled { background: #f1f5f9; }

.input-actions {
  display: flex; align-items: center; gap: 8px;
  flex-shrink: 0;
}
.char-count { font-size: 11px; color: #94a3b8; }

.btn-send, .btn-stop {
  padding: 10px; border: none; border-radius: 10px;
  cursor: pointer; transition: all 0.2s; display: flex; align-items: center; justify-content: center;
  width: 40px; height: 40px;
}
.btn-send {
  background: #0ea5e9; color: #fff;
}
.btn-send:hover:not(:disabled) { background: #0284c7; }
.btn-send:disabled { background: #cbd5e1; cursor: not-allowed; }
.btn-stop {
  background: #fef2f2; color: #ef4444; width: auto; padding: 10px 16px; font-size: 13px; font-weight: 600; border-radius: 10px;
}
.btn-stop:hover { background: #fee2e2; }
.stop-icon {
  width: 8px; height: 8px; background: #ef4444; border-radius: 1px;
}

@media (max-width: 600px) {
  .suggestions { grid-template-columns: 1fr; }
  .msg-body { max-width: 85%; }
}
</style>
