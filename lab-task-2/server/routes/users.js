import express from 'express';
import User from '../models/User.js';
import Video from '../models/Video.js';
import authenticateToken from '../middleware/authenticateToken.js';

const router = express.Router();

router.post('/subscribe', authenticateToken, async (req, res) => {
  const { authorName } = req.body;
  if (req.user.username === authorName) return res.status(400).json({ error: "Cannot subscribe to yourself" });

  await User.findOneAndUpdate(
    { username: req.user.username },
    { $addToSet: { subscriptions: authorName } }
  );
  res.json({ message: "Subscribed" });
});

router.post('/unsubscribe', authenticateToken, async (req, res) => {
  const { authorName } = req.body;
  await User.findOneAndUpdate(
    { username: req.user.username },
    { $pull: { subscriptions: authorName } }
  );
  res.json({ message: "Unsubscribed" });
});

router.get('/feed', authenticateToken, async (req, res) => {
  const user = await User.findOne({ username: req.user.username });
  const videos = await Video.find({ username: { $in: user.subscriptions } }).sort({ createdAt: -1 });
  res.json(videos);
});

router.get('/me', authenticateToken, async (req, res) => {
  try {
    const user = await User.findById(req.user.id).select('-password');
    if (!user) return res.status(404).json({ error: "User not found" });
    
    res.json(user);
  } catch (e) {
    res.status(500).json({ error: "Server error" });
  }
});

export default router;