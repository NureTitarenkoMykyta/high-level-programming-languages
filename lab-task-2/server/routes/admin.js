import express from 'express';
import User from '../models/User.js';
import authenticateToken from '../middleware/authenticateToken.js';
import { authorize } from '../middleware/role.js';

const router = express.Router();

router.get('/users', authenticateToken, authorize('admin'), async (req, res) => {
  const users = await User.find({}, '-password');
  res.json(users);
});

router.patch('/users/:id/role', authenticateToken, authorize('admin'), async (req, res) => {
  try {
    const { role } = req.body;
    const updatedUser = await User.findByIdAndUpdate(
      req.params.id, 
      { role }, 
      { new: true }
    ).select('-password');
    res.json(updatedUser);
  } catch (e) {
    res.status(400).json({ error: "Update failed" });
  }
});

router.delete('/users/:id', authenticateToken, authorize('admin'), async (req, res) => {
  try {
    await User.findByIdAndDelete(req.params.id);
    res.json({ message: "User deleted" });
  } catch (e) {
    res.status(500).json({ error: "Error deleting user" });
  }
});

export default router;