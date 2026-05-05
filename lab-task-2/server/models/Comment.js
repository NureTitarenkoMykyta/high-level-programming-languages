import mongoose from 'mongoose';

const CommentSchema = new mongoose.Schema({
  videoFilename: { type: String, required: true },
  username: { type: String, required: true },
  text: { type: String, required: true },
  createdAt: { type: Date, default: Date.now }
});

export default mongoose.model('Comment', CommentSchema);