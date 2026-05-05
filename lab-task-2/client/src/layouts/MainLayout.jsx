import React from 'react';
import { Outlet, Navigate, Link } from 'react-router-dom';

const MainLayout = ({ isAuthenticated, username, handleLogout, role }) => {
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  return (
    <div className="app-layout">
      <header className="header" role="banner">
        <h1>
          <Link to="/" className="logo">
            VideoShare
          </Link>
        </h1>
        <nav aria-label="User menu">
          <div className="user-controls">
            <span id="user-greeting">
              Welcome, <strong>{username}</strong>!
            </span>
            <Link to="/upload">
              <button className="logout-button">
                Create New Video
              </button>
            </Link>
            {role === 'admin' && (
  <Link to="/admin" style={{ marginRight: '15px', color: 'red', fontWeight: 'bold' }}>
    Admin Panel
  </Link>
)}
            <button 
              onClick={handleLogout}
              className="logout-button"
            >
              Logout
            </button>
          </div>
        </nav>
      </header>

      <main id="main-content">
        <Outlet />
      </main>
    </div>
  );
};

export default MainLayout;