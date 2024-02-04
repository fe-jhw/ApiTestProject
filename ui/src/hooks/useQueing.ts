import { useCallback, useEffect, useState } from 'react'
import useWebSocket from 'react-use-websocket'

let GLOBALQUEUE: Array<MessageEvent<any>> = []
let timer: NodeJS.Timeout

type MsgTypeChecker = (msg: MessageEvent<any>) => boolean

const isRealtime: MsgTypeChecker = msg => {
  return msg !== null && !msg.data.startsWith('connection')
}

const isOpen: MsgTypeChecker = msg => {
  return msg?.data.includes('open')
}

const isClose: MsgTypeChecker = msg => {
  return msg?.data.includes('close')
}

interface UseQueuingProps {
  websocketUrl: string
  onOpen?: (msg: MessageEvent<any>) => void
  onQueue?: (queue: Array<MessageEvent<any>>) => void
  startMsg: string | null
  onClose?: (msg: MessageEvent<any>) => void
}

export const useQueuing = ({ websocketUrl, onOpen, startMsg, onQueue, onClose }: UseQueuingProps) => {
  const { sendMessage, lastMessage, getWebSocket } = useWebSocket(websocketUrl)

  const [queue, setQueue] = useState<any[]>([])

  const startTick = useCallback(() => {
    const timer = setInterval(() => {
      console.log('tick')
      if (GLOBALQUEUE.length !== 0) {
        const t = [...GLOBALQUEUE]
        GLOBALQUEUE = []
        if (onQueue) {
          onQueue?.(t)
        } else {
          setQueue(prev => {
            return [...prev, ...t]
          })
        }
      }
    }, 500)
    return timer
  }, [onQueue])

  useEffect(() => {
    if (lastMessage != null) {
      if (isRealtime(lastMessage)) {
        GLOBALQUEUE.push(lastMessage)
      } else if (isOpen(lastMessage)) {
        if (startMsg != null) {
          sendMessage(startMsg)
        }
        onOpen?.(lastMessage)
        timer = startTick()
      } else if (isClose(lastMessage)) {
        onClose?.(lastMessage)
        setTimeout(() => {
          clearInterval(timer)
        }, 2000)
      }
    }
  }, [lastMessage, onClose, onOpen, sendMessage, startMsg, startTick])

  useEffect(() => {
    return () => {
      getWebSocket()?.close()
      clearInterval(timer)
    }
  }, [getWebSocket])

  return [queue]
}