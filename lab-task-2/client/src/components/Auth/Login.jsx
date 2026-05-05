import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate, Navigate } from 'react-router-dom';
import './Auth.css';

const Login = ({ onLogin, isAuthenticated }) => {
  const [form, setForm] = useState({ username: '', password: '' });
  const [error, setError] = useState('');
  const navigate = useNavigate();

  if (isAuthenticated) return <Navigate to="/" replace />;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const res = await axios.post('http://localhost:5000/auth/login', form);
      
      onLogin({
        token: res.data.token,
        username: res.data.username,
        role: res.data.role
      });
      
      navigate('/'); 
    } catch (err) {
      setError(err.response?.data?.error || "Login error");
    }
  };

  return (
    <main className="container">
      <section className="auth-form" aria-labelledby="login-title">
        <h2 id="login-title">Login</h2>

        {error && (
          <div className="error-message" role="alert" aria-live="assertive">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="field-group">
            <label htmlFor="login-username">Username</label>
            <input 
              id="login-username"
              type="text"
              placeholder="Enter your username" 
              value={form.username}
              onChange={e => setForm({...form, username: e.target.value})} 
              required 
              aria-required="true"
            />
          </div>

          <div className="field-group">
            <label htmlFor="login-password">Password</label>
            <input 
              id="login-password"
              type="password" 
              placeholder="Enter your password" 
              value={form.password}
              onChange={e => setForm({...form, password: e.target.value})} 
              required 
              aria-required="true"
            />
          </div>

          <button type="submit" aria-label="Log in to your account">
            Login
          </button>
        </form>

        <div className="auth-footer">
          <span>Don't have an account?</span>
          <button 
            type="button"
            className="link-button"
            onClick={() => navigate('/register')}
          >
            Register
          </button>
        </div>
      </section>
    </main>
  );
};

export default Login;