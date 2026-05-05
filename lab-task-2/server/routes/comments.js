import express from 'express';
import jwt from 'jsonwebtoken';
import Comment from '../models/Comment.js';
import authenticateToken from '../middleware/authenticateToken.js';
import { authorize } from '../middleware/role.js';

const router = express.Router();

router.get('/:filename', authenticateToken, async (req, res) => {
  try {
    const comments = await Comment.find({ videoFilename: req.params.filename }).sort({ createdAt: -1 });
    res.json(comments);
  } catch (e) {
    res.status(500).json({ error: "Error fetching comments" });
  }
});

router.post('/', authenticateToken, async (req, res) => {
  try {
    const { videoFilename, text, username } = req.body;
    const newComment = await Comment.create({
      videoFilename,
      text,
      username
    });
    res.status(201).json(newComment);
  } catch (e) {
    res.status(400).json({ error: "Error saving comment" });
  }
});

router.delete('/:id', authenticateToken, authorize(['moderator', 'admin']), async (req, res) => {
  try {
    await Comment.findByIdAndDelete(req.params.id);
    res.json({ message: "Comment deleted" });
  } catch (e) {
    res.status(500).json({ error: "Error deleting comment" });
  }
});

export default router;