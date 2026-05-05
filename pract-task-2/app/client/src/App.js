import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Login from './components/Login';
import Register from './components/Register';
import MapComponent from './components/MapComponent';
import './App.css';

function App() {
  const isAuthenticated = !!localStorage.getItem('token');
  const username = localStorage.getItem('username');

  const handleLogout = () => {
    localStorage.clear();
    window.location.href = '/login';
  };

  return (
    <Router>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route 
          path="/map" 
          element={
            isAuthenticated ? (
              <div className="app-layout">
                <header className="header" role="banner">
                  <h1>Vibe Map</h1>
                  <nav aria-label="User menu">
                    <div className="user-controls">
                      <span id="user-greeting">
                        Welcome, <strong>{username}</strong>!
                      </span>
                      <button 
                        onClick={handleLogout}
                        aria-label={`Logout ${username}`}
                        className="logout-button"
                      >
                        Logout
                      </button>
                    </div>
                  </nav>
                </header>

                <main id="main-content" tabIndex="-1">
                  <MapComponent />
                </main>
              </div>
            ) : (
              <Navigate to="/login" replace />
            )
          } 
        />
        <Route path="*" element={<Navigate to="/map" replace />} />
      </Routes>
    </Router>
  );
}

export default App;