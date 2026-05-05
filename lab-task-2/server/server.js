import cluster from 'cluster';
import os from 'os';
import express from 'express';
import mongoose from 'mongoose';
import cors from 'cors';
import http from 'http';
import { Server } from 'socket.io';

import videoRoutes from './routes/videos.js';
import commentRoutes from './routes/comments.js';
import authRoutes from './routes/auth.js';
import adminRoutes from './routes/admin.js';
import userRoutes from './routes/users.js';
import { seedAdmin } from './seed/admin.js';
import { setupSocket } from './socket.js';

const PORT = 5000;
const OPTIMISED = false;

if (OPTIMISED && cluster.isPrimary) {
    const numCPUs = os.cpus().length;
    console.log(`Master process ${process.pid} is running`);
    console.log(`Forking server for ${numCPUs} CPUs...`);

    for (let i = 0; i < numCPUs; i++) {
        cluster.fork();
    }

    cluster.on('exit', (worker, code, signal) => {
        console.log(`Worker ${worker.process.pid} died. Restarting...`);
        cluster.fork();
    });

} else {
    const app = express();
    const server = http.createServer(app);

    const io = new Server(server, {
        cors: {
            origin: "http://localhost:3000",
            methods: ["GET", "POST"]
        }
    });

    setupSocket(io);

    app.use((req, res, next) => {
        req.io = io;
        next();
    });

    app.use(express.json());
    app.use(cors());

    mongoose.connect('mongodb://127.0.0.1:27017/videoDB', {
        maxPoolSize: 100,
    })
    .then(() => {
        console.log(`Worker ${process.pid} connected to MongoDB`);
        seedAdmin();
    })
    .catch(err => console.error(`Worker ${process.pid} DB error:`, err));

    app.use('/auth', authRoutes);
    app.use('/videos', videoRoutes);
    app.use('/comments', commentRoutes);
    app.use('/admin', adminRoutes);
    app.use('/users', userRoutes);

    server.listen(PORT, () => {
        console.log(`Worker ${process.pid} started server on port ${PORT}`);
    });
}