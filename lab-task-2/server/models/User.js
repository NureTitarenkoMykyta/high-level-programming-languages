import mongoose from 'mongoose';

const UserSchema = new mongoose.Schema({
  username: { type: String, unique: true, required: true },
  password: { type: String, required: true },
  role: { 
    type: String, 
    enum: ['user', 'moderator', 'admin'], 
    default: 'user'
  },
  subscriptions: [{ type: String }]
});

export default mongoose.model('User', UserSchema);