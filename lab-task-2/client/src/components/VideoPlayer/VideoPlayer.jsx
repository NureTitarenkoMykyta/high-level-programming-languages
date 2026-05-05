import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import axios from 'axios';
import './VideoPlayer.css';

const VideoPlayer = ({ username, role }) => {
  const { filename } = useParams();
  const [comments, setComments] = useState([]);
  const [video, setVideo] = useState(null);
  const [newComment, setNewComment] = useState("");
  const [isSubscribed, setIsSubscribed] = useState(false);
  const token = localStorage.getItem('token');

  const handleShare = () => {
    navigator.clipboard.writeText(window.location.href);
    alert("Your browser support link copying!");
  };

  const checkSubscription = async (authorName) => {
    try {
      const res = await axios.get('http://localhost:5000/users/me', {
        headers: { Authorization: `Bearer ${token}` }
      });
      setIsSubscribed(res.data.subscriptions.includes(authorName));
    } catch (err) {
      console.error(err);
    }
  };

  const toggleSubscribe = async () => {
    const endpoint = isSubscribed ? 'unsubscribe' : 'subscribe';
    await axios.post(`http://localhost:5000/users/${endpoint}`, 
      { authorName: video.username },
      { headers: { Authorization: `Bearer ${token}` } }
    );
    setIsSubscribed(!isSubscribed);
  };

  const fetchVideo = async () => {
    try {
      const res = await axios.get(`http://localhost:5000/videos/${filename}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setVideo(res.data);
      await checkSubscription(res.data.username);
    } catch (err) {
      console.error("Failed to load video");
    }
  };

  const fetchComments = async () => {
    try {
      const res = await axios.get(`http://localhost:5000/comments/${filename}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setComments(res.data);
    } catch (err) {
      console.error("Failed to load comments");
    }
  };

  useEffect(() => {
    fetchVideo();
    fetchComments();
  }, [filename]);

  const handleSendComment = async (e) => {
    e.preventDefault();
    if (!newComment.trim()) return;
    try {
      await axios.post('http://localhost:5000/comments', {
        videoFilename: filename,
        text: newComment,
        username: username
      }, { headers: { Authorization: `Bearer ${token}` } });
      setNewComment("");
      fetchComments();
    } catch (err) {
      alert("Failed to send comment");
    }
  };

  const handleDeleteComment = async (commentId) => {
    if (!window.confirm("Delete this comment?")) return;
    try {
      await axios.delete(`http://localhost:5000/comments/${commentId}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      fetchComments();
    } catch (err) {
      alert("Not enough permissions");
    }
  };

  if (!video) return <div className="loading">Loading...</div>;

  return (
    <div className="player-container">
      <div className="main-content">
        <div className="video-section">
          <video 
            className="video-element"
            controls 
            src={`http://localhost:5000/videos/stream/${filename}`}
          >
            Your browser does not support the video tag.
          </video>
          
          <div className="video-header">
            <h1>{video.title}</h1>
            {video.username !== username && (
              <div className="video-actions">
                <button 
                  onClick={toggleSubscribe} 
                  className={`subscribe-btn ${isSubscribed ? 'active' : ''}`}
                >
                  {isSubscribed ? "Unsubscribe" : "Subscribe"}
              </button>
              <button onClick={handleShare} className="share-btn">Share</button>
            </div>)}
          </div>
          <div className="author-info">
            Uploaded by <strong>{video.username}</strong>
          </div>
        </div>

        <div className="comments-section">
          <h3>Comments ({comments.length})</h3>
          <form onSubmit={handleSendComment} className="comment-form">
            <textarea
              placeholder="Write a comment..."
              value={newComment}
              onChange={(e) => setNewComment(e.target.value)}
              required
            />
            <button type="submit">Send</button>
          </form>

          <div className="comments-list">
            {comments.map((c) => (
              <div key={c._id} className="comment-item">
                <div className="comment-meta">
                  <strong>{c.username}</strong>
                  <span>{new Date(c.createdAt).toLocaleString()}</span>
                  {(role === 'admin' || role === 'moderator') && (
                    <button className="delete-comment" onClick={() => handleDeleteComment(c._id)}>Delete</button>
                  )}
                </div>
                <p>{c.text}</p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default VideoPlayer;