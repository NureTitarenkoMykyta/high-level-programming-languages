import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import './Home.css';
import axios from 'axios';

const Home = () => {
  const [videos, setVideos] = useState([]);

  useEffect(() => {
    const token = localStorage.getItem('token');

    axios.get('http://localhost:5000/videos', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    })
      .then(res => {
        setVideos(Array.isArray(res.data) ? res.data : []);
      })
      .catch(err => {
        console.error(err);
        setVideos([]);
      });
  }, []);

  return (
    <div className="library-container">
      <h1 className="library-title">Video Library</h1>
      <div className="video-grid">
        {videos.length > 0 ? (
          videos.map(video => (
            <div key={video._id} className="video-card">
              <div className="video-thumbnail">
                <div className="play-icon">▶</div>
              </div>
              <div className="video-info">
                <h3>{video.title}</h3>
                <p>Uploaded by: {video.username}</p>
                <Link to={`/watch/${video.filename}`} className="watch-link">
                  Watch Now
                </Link>
              </div>
            </div>
          ))
        ) : (
          <p className="no-videos">No videos available or access denied.</p>
        )}
      </div>
    </div>
  );
};

export default Home;