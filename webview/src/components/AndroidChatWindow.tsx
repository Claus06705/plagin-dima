import React, { useState, useRef, useEffect } from 'react';
import '../styles/chat.css';

interface Message {
    id: string;
    text: string;
    sender: 'user' | 'ai';
}

interface ChatWindowProps {
    userName: string;
}

const AndroidChatWindow: React.FC<ChatWindowProps> = ({ userName }) => {
    const [messages, setMessages] = useState<Message[]>([
        { id: '1', text: `Привет, ${userName}! Чем я могу помочь тебе сегодня?`, sender: 'ai' }
    ]);
    const [inputValue, setInputValue] = useState('');
    const messagesEndRef = useRef<HTMLDivElement>(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    const handleSend = () => {
        if (!inputValue.trim()) return;

        const userMsg: Message = {
            id: Date.now().toString(),
            text: inputValue,
            sender: 'user'
        };

        setMessages(prev => [...prev, userMsg]);
        setInputValue('');

        // Simulate AI Response (logic will be connected to DeepSeekService later)
        setTimeout(() => {
            const aiMsg: Message = {
                id: (Date.now() + 1).toString(),
                text: `${userName}, я получил твой вопрос: "${inputValue}". Начинаю анализировать...`,
                sender: 'ai'
            };
            setMessages(prev => [...prev, aiMsg]);
        }, 1000);
    };

    return (
        <div className="chat-container">
            <div className="welcome-header">
                <h2>Привет, {userName}! 🚀</h2>
                <p>Android Studio DeepSeek Assistant</p>
            </div>

            <div className="chat-messages">
                {messages.map(msg => (
                    <div key={msg.id} className={`message ${msg.sender}`}>
                        {msg.text}
                    </div>
                ))}
                <div ref={messagesEndRef} />
            </div>

            <div className="input-area">
                <input
                    type="text"
                    value={inputValue}
                    onChange={(e) => setInputValue(e.target.value)}
                    onKeyPress={(e) => e.key === 'Enter' && handleSend()}
                    placeholder="Напиши запрос (например: объясни MVVM)..."
                />
                <button className="send-button" onClick={handleSend}>
                    Отправить
                </button>
            </div>
        </div>
    );
};

export default AndroidChatWindow;