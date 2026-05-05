import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

const Register = () => {
  const [form, setForm] = useState({ username: '', password: '' });
  const [error, setError] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      await axios.post('http://localhost:5000/register', form);
      alert("Registration successful! Please log in.");
      navigate('/login');
    } catch (err) {
      setError(err.response?.data?.error || "Registration error");
    }
  };

  return (
    <main className="container">
      <section className="auth-form" aria-labelledby="reg-title">
        <h2 id="reg-title">Registration</h2>
        
        {error && (
          <div 
            className="error-message" 
            role="alert" 
            aria-live="assertive"
          >
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} aria-describedby={error ? "error-id" : undefined}>
          <div className="field-group">
            <label htmlFor="username">Username</label>
            <input 
              id="username"
              type="text"
              placeholder="Enter your username" 
              value={form.username}
              onChange={e => setForm({...form, username: e.target.value})} 
              required 
              aria-required="true"
            />
          </div>

          <div className="field-group">
            <label htmlFor="password">Password</label>
            <input 
              id="password"
              type="password" 
              placeholder="Enter your password" 
              value={form.password}
              onChange={e => setForm({...form, password: e.target.value})} 
              required 
              aria-required="true"
            />
          </div>

          <button type="submit" aria-label="Create new account">
            Create Account
          </button>
        </form>

        <div className="auth-footer">
          <span>Already have an account?</span>
          <button 
            type="button"
            className="link-button"
            onClick={() => navigate('/login')}
          >
            Login
          </button>
        </div>
      </section>
    </main>
  );
};

export default Register;