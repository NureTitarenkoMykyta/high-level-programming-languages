import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import './Upload.css';

const Upload = ({ username }) => {
  const [file, setFile] = useState(null);
  const [title, setTitle] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleUpload = async (e) => {
    e.preventDefault();
    if (!file) return;

    const formData = new FormData();
    formData.append('video', file);
    formData.append('title', title);
    formData.append('username', username);

    setLoading(true);
    try {
      await axios.post('http://localhost:5000/videos/upload', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      navigate('/');
    } catch (err) {
      alert("Upload failed");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="upload-container">
      <div className="upload-card">
        <h2>Upload New Video</h2>
        <form onSubmit={handleUpload} className="upload-form">
          <div className="file-input-wrapper">
            <input 
              type="file" 
              id="video-file"
              accept="video/mp4" 
              onChange={(e) => setFile(e.target.files[0])} 
              required 
            />
            <label htmlFor="video-file" className="file-label">
              {file ? file.name : "Choose MP4 Video"}
            </label>
          </div>

          <input 
            type="text" 
            className="title-input"
            placeholder="Enter video title" 
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required 
          />

          <button type="submit" className="submit-btn" disabled={loading}>
            {loading ? <div className="loader"></div> : "Publish Video"}
          </button>
        </form>
      </div>
    </div>
  );
};

export default Upload;