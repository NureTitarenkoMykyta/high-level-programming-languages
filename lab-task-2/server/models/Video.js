import mongoose from 'mongoose';

const VideoSchema = new mongoose.Schema({
  filename: { type: String, unique: true, required: true },
  title: { type: String, required: true },
  username: { type: String, required: true },
  createdAt: { type: Date, default: Date.now }
});

export default mongoose.model('Video', VideoSchema);