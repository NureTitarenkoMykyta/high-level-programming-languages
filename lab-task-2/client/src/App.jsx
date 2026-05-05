import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, Link } from 'react-router-dom';
import MainLayout from './layouts/MainLayout';
import Home from './components/Home/Home';
import VideoPlayer from './components/VideoPlayer/VideoPlayer';
import Login from './components/Auth/Login';
import Register from './components/Auth/Register';
import Upload from './components/Upload/Upload';
import AdminPanel from './components/AdminPanel/AdminPanel';
import { io } from 'socket.io-client';

const socket = io('http://localhost:5000');

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(!!localStorage.getItem('token'));
  const [username, setUsername] = useState(localStorage.getItem('username') || "");
  const [role, setRole] = useState(localStorage.getItem('role') || "user");
  const [notification, setNotification] = useState(null);

  useEffect(() => {
    if (isAuthenticated && username) {
      socket.emit('identify', username);

      socket.on('new_video_notification', (data) => {
        setNotification(data);
        setTimeout(() => setNotification(null), 5000);
      });
    }
    return () => socket.off('new_video_notification');
  }, [isAuthenticated, username]);

  const handleLogin = (userData) => {
    localStorage.setItem('token', userData.token);
    localStorage.setItem('username', userData.username);
    localStorage.setItem('role', userData.role);
    setIsAuthenticated(true);
    setUsername(userData.username);
    setRole(userData.role);
  };

  const handleLogout = () => {
    localStorage.clear();
    setIsAuthenticated(false);
    setUsername("");
    setRole("user");
  };

  return (
    <Router>
      {notification && (
        <div className="socket-toast">
          <span>New video from {notification.author}: <strong>{notification.title}</strong></span>
          <Link to={`/watch/${notification.filename}`}>Watch</Link>
        </div>
      )}
      <Routes>
        <Route 
          path="/login" 
          element={
            <Login 
              onLogin={handleLogin} 
              isAuthenticated={isAuthenticated} 
            />
          } 
        />
        <Route 
          path="/register" 
          element={<Register isAuthenticated={isAuthenticated} />} 
        />
        
        <Route element={
          <MainLayout 
            isAuthenticated={isAuthenticated} 
            username={username} 
            handleLogout={handleLogout}
            role={role}
          />
        }>
          <Route path="/" element={<Home />} />
          <Route path="/watch/:filename" element={<VideoPlayer username={username} role={role} />} />
          <Route path="/upload" element={<Upload username={username} />} />
          
          {role === 'admin' && (
            <Route path="/admin" element={<AdminPanel />} />
          )}
        </Route>

        <Route path="*" element={<Navigate to={isAuthenticated ? "/" : "/login"} replace />} />
      </Routes>
    </Router>
  );
}

export default App;