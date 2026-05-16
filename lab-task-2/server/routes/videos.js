import express from 'express';
import fs from 'fs';
import path from 'path';
import multer from 'multer';
import { fileURLToPath } from 'url';
import authenticateToken from '../middleware/authenticateToken.js';
import Video from '../models/Video.js';
import User from '../models/User.js';
import { onlineUsers } from '../socket.js';

const router = express.Router();
const __dirname = path.dirname(fileURLToPath(import.meta.url));
const videosPath = path.resolve(__dirname, '../videos');

if (!fs.existsSync(videosPath)) fs.mkdirSync(videosPath);

const storage = multer.diskStorage({
  destination: (req, file, cb) => cb(null, videosPath),
  filename: (req, file, cb) => cb(null, `${Date.now()}-${file.originalname}`)
});

const upload = multer({ 
  storage,
  fileFilter: (req, file, cb) => {
    file.mimetype === 'video/mp4' ? cb(null, true) : cb(new Error('Only MP4 allowed'), false);
  }
});

router.get('/', authenticateToken, async (req, res) => {
    const videos = await Video.find();
    res.json(videos);
});

router.post('/upload', authenticateToken, upload.single('video'), async (req, res) => {
  try {
    if (!req.file) {
      return res.status(400).json({ error: "No file" });
    }

    const newVideo = await Video.create({
      filename: req.file.filename,
      title: req.body.title,
      username: req.body.username
    });

    const subscribers = await User.find({ subscriptions: req.body.username });

    subscribers.forEach(sub => {
      const socketId = onlineUsers.get(sub.username);
      if (socketId) {
        req.io.to(socketId).emit('new_video_notification', {
          author: req.body.username,
          title: newVideo.title,
          filename: newVideo.filename
        });
      }
    });

    res.status(201).json(newVideo);
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: "Failed to upload video information" });
  }
});

router.get('/stream/:filename', (req, res) => {
  const videoPath = path.join(videosPath, req.params.filename);
  if (!fs.existsSync(videoPath)) return res.status(404).send("Not found");

  const fileSize = fs.statSync(videoPath).size;
  const range = req.headers.range;

  if (range) {
    const parts = range.replace(/bytes=/, "").split("-");
    const start = parseInt(parts[0], 10);
    const end = parts[1] ? parseInt(parts[1], 10) : fileSize - 1;
    const chunksize = (end - start) + 1;
    const file = fs.createReadStream(videoPath, { start, end });
    res.writeHead(206, {
      'Content-Range': `bytes ${start}-${end}/${fileSize}`,
      'Accept-Ranges': 'bytes',
      'Content-Length': chunksize,
      'Content-Type': 'video/mp4',
    });
    file.pipe(res);
  } else {
    res.writeHead(200, { 'Content-Length': fileSize, 'Content-Type': 'video/mp4' });
    fs.createReadStream(videoPath).pipe(res);
  }
});

router.get('/:filename', async (req, res) => {
  try {
    const videos = await Video.findOne({ filename: req.params.filename });
    if (!videos) return res.status(404).json({ error: "Video not found" });
    res.json(videos);
  }
  catch (err) {
    res.status(500).json({ error: "Database error" });
  }
});

router.post('/:filename/like', authenticateToken, async (req, res) => {
  try {
    const { filename } = req.params;
    const { username } = req.user; // From authenticateToken middleware

    const video = await Video.findOneAndUpdate(
      { filename },
      { $addToSet: { likes: username } }, // Only adds if not already present
      { new: true }
    );

    if (!video) return res.status(404).json({ error: "Video not found" });
    res.json({ likesCount: video.likes.length, isLiked: true });
  } catch (err) {
    res.status(500).json({ error: "Server error" });
  }
});

router.delete('/:filename/like', authenticateToken, async (req, res) => {
  try {
    const { filename } = req.params;
    const { username } = req.user;

    const video = await Video.findOneAndUpdate(
      { filename },
      { $pull: { likes: username } }, // Removes user from array
      { new: true }
    );

    if (!video) return res.status(404).json({ error: "Video not found" });
    res.json({ likesCount: video.likes.length, isLiked: false });
  } catch (err) {
    res.status(500).json({ error: "Server error" });
  }
});

export default router;