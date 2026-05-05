import express from 'express';
import mongoose from 'mongoose';
import cors from 'cors';
import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';

const app = express();
app.use(express.json());
app.use(cors());

const PORT = 5000;
const JWT_SECRET = 'your_super_secret_key';

mongoose.connect('mongodb://127.0.0.1:27017/chatDB');

const UserSchema = new mongoose.Schema({
  username: { type: String, unique: true, required: true },
  password: { type: String, required: true }
});

const User = mongoose.model('User', UserSchema);

const EventSchema = new mongoose.Schema({
  title: { type: String, required: true },
  description: { type: String },
  lat: { type: Number, required: true },
  lng: { type: Number, required: true },
  creator: { type: String, required: true },
  createdAt: { type: Date, default: Date.now }
});

const Event = mongoose.model('Event', EventSchema);

app.post('/register', async (req, res) => {
  try {
    const hashedPassword = await bcrypt.hash(req.body.password, 10);
    const user = await User.create({
      username: req.body.username,
      password: hashedPassword
    });
    res.status(201).json({ message: "User created" });
  } catch (e) {
    res.status(400).json({ error: "Username already exists" });
  }
});

app.post('/login', async (req, res) => {
  const user = await User.findOne({ username: req.body.username });
  if (!user || !(await bcrypt.compare(req.body.password, user.password))) {
    return res.status(401).json({ error: "Invalid credentials" });
  }
  const token = jwt.sign({ id: user._id }, JWT_SECRET);
  res.json({ token, username: user.username });
});

app.get('/events', async (req, res) => {
  try {
    const events = await Event.find();
    res.json(events);
  } catch (e) {
    res.status(500).json({ error: "Помилка при читанні подій" });
  }
});

app.post('/events', async (req, res) => {
  try {
    const { title, description, lat, lng, creator } = req.body;
    const newEvent = await Event.create({
      title,
      description,
      lat,
      lng,
      creator
    });
    res.status(201).json(newEvent);
  } catch (e) {
    res.status(400).json({ error: "Помилка при створенні події" });
  }
});

app.listen(PORT, () => console.log(`Server running on port ${PORT}`));