import { useEffect, useState } from 'react';

export function useWebSocket(url: string) {
  const [isConnected, setIsConnected] = useState(false);
  const [messages, setMessages] = useState<any[]>([]);

  useEffect(() => {
    // Placeholder for actual SockJS / STOMP implementation
    console.log(`Connecting to WebSocket at ${url}`);
    setIsConnected(true);

    return () => {
      console.log('Disconnecting from WebSocket');
      setIsConnected(false);
    };
  }, [url]);

  const sendMessage = (destination: string, body: any) => {
    console.log(`Sending message to ${destination}:`, body);
  };

  return { isConnected, messages, sendMessage };
}
