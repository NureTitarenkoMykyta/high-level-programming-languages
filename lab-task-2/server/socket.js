export const onlineUsers = new Map();

export function setupSocket(io) {
  io.on('connection', (socket) => {
    socket.on('identify', (username) => {
      onlineUsers.set(username, socket.id);
    });

    socket.on('disconnect', () => {
      for (let [username, id] of onlineUsers.entries()) {
        if (id === socket.id) {
          onlineUsers.delete(username);
          break;
        }
      }
    });
  });
}