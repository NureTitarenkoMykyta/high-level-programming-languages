import express from 'express';
import jwt from 'jsonwebtoken';
import bcrypt from 'bcryptjs';
import User from '../models/User.js';

const router = express.Router();
const JWT_SECRET = 'your_super_secret_key';

router.post('/register', async (req, res) => {
  try {
    const { username, password } = req.body;
    const hashedPassword = await bcrypt.hash(password, 10);

    await User.create({ 
      username, 
      password: hashedPassword,
      role: 'user'
    });

    res.status(201).json({ message: "User created" });
  } catch (e) {
    res.status(400).json({ error: "Username already exists" });
  }
});

router.post('/login', async (req, res) => {
  const user = await User.findOne({ username: req.body.username });
  if (!user || !(await bcrypt.compare(req.body.password, user.password))) {
    return res.status(401).json({ error: "Invalid credentials" });
  }

  const token = jwt.sign(
    { id: user._id, username: user.username, role: user.role }, 
    JWT_SECRET
  );

  res.json({ token, username: user.username, role: user.role });
});

export default router;