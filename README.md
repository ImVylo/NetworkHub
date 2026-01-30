# NetworkHub

A comprehensive multi-server network coordination plugin for Hytale that enables seamless server connectivity, cross-server communication, and advanced network management features.

## Features

### Core Network Management
- **Server Registry**: Automatic server registration and discovery across the network
- **Health Monitoring**: Real-time server health checks with automatic failover
- **Hub System**: Designate hub servers with priority-based fallback
- **Player Tracking**: Track player locations and movement across all servers
- **Graceful Fallback**: Automatic player transfer to hub servers on crashes or shutdowns

### Player Transfer System
- **Physical Teleporters**: Create teleporter blocks that transfer players between servers
- **Countdown System**: Configurable countdown before transfer (cancels on movement)
- **GUI Server Selector**: Interactive menu for players to browse and join servers
- **Admin Controls**: Transfer players individually or in bulk via commands or GUI

### Queue System
- **Priority Queuing**: VIP-priority queue system for full servers
- **Auto-Queue**: Automatically join queue when attempting to join full server
- **Position Tracking**: Real-time queue position updates
- **Smart Processing**: Efficient queue processing with configurable intervals

### Cross-Server Communication
- **Redis Pub/Sub**: High-performance real-time messaging via Redis
- **Global Chat**: Network-wide chat channel accessible from any server
- **Staff Chat**: Private staff-only chat across all servers
- **Direct Messaging**: Send DMs to players on any server with /reply support
- **Announcements**: Broadcast network-wide announcements with popup GUIs

### Network HUD
- **Persistent Sidebar**: Real-time network statistics displayed in hub servers
- **Player Counts**: Live player counts per server
- **Server Health**: Online/offline server status
- **Queue Information**: Total queued players across network
- **Notifications**: Unread message indicators

### GUI System
- **Server Selector**: Browse and join servers with real-time status
- **Admin Panel**: Central hub for all network management
- **Server Management**: Configure hubs, priorities, and settings
- **Player Management**: Transfer and track players across network
- **Teleporter Editor**: Visual teleporter creation and editing
- **Queue Viewer**: View and manage server queues
- **Announcement Creator**: Create network-wide announcements with previews

### Database Support
- **PostgreSQL**: Primary database support
- **MySQL**: Full MySQL compatibility
- **MariaDB**: MariaDB support
- **Connection Pooling**: High-performance HikariCP connection pooling
- **Async Operations**: Non-blocking database operations
- **Auto-Schema**: Automatic database table creation on first run

## Requirements

- **Hytale Server**: Compatible with Hytale Server API
- **Java**: 17 or higher
- **Database**: PostgreSQL 12+, MySQL 8+, or MariaDB 10+
- **Redis** (optional): For real-time cross-server communication
- **Gradle**: For building from source

## Installation

1. **Download** the latest release from the releases page
2. **Place** `NetworkHub.jar` in your `mods/` directory
3. **Configure** the database in `config/NetworkHub/database-config.json`
4. **Configure** Redis in `config/NetworkHub/redis-config.json` (optional)
5. **Configure** server settings in `config/NetworkHub/network-config.json`
6. **Start** your server - the plugin will create all necessary tables
7. **Repeat** steps 1-6 on all servers in your network

## Configuration

### Network Configuration (`network-config.json`)

```json
{
  "server": {
    "serverId": "game-01",
    "serverName": "Game Server 1",
    "serverType": "GAME",
    "isHub": false,
    "hubPriority": 0,
    "maxPlayers": 100
  },
  "heartbeat": {
    "intervalSeconds": 10,
    "timeoutSeconds": 30,
    "failureThreshold": 3
  },
  "fallback": {
    "enabled": true,
    "triggerOnShutdown": true
  }
}
```

### Database Configuration (`database-config.json`)

```json
{
  "type": "POSTGRESQL",
  "host": "localhost",
  "port": 5432,
  "database": "hytale_network",
  "username": "hytale_user",
  "password": "your_password"
}
```

### Redis Configuration (`redis-config.json`)

```json
{
  "enabled": true,
  "host": "localhost",
  "port": 6379,
  "password": null,
  "database": 0
}
```

## Commands

### Admin Commands
- `/networkgui` - Open main admin panel (GUI)
- `/network listservers` - List all registered servers
- `/network sethub <server> [priority]` - Designate hub with priority
- `/network unsethub <server>` - Remove hub designation
- `/network transfer <player> <server>` - Transfer specific player
- `/network transferall <server>` - Transfer all players on current server

### User Commands
- `/servers` - Open server selector GUI
- `/whereis <player>` - Find which server a player is on
- `/msg <player> <message>` - Send direct message to player
- `/reply <message>` - Reply to last DM
- `/g <message>` - Send message to global chat
- `/hud toggle` - Show/hide network HUD (hub only)

### Staff Commands
- `/sc <message>` - Send message to staff chat
- `/announce <message>` - Create quick network announcement

### Queue Commands
- `/queue join <server>` - Join queue for full server
- `/queue leave <server>` - Leave queue
- `/queue info <server>` - Show queue position

### Teleporter Commands
- `/teleporter create <server> [displayName]` - Create teleporter at location
- `/teleporter remove` - Remove teleporter at location
- `/teleporter list` - List all teleporters on server

## Permissions

- `networkhub.admin` - Access to admin panel and commands
- `networkhub.announce` - Create network announcements
- `networkhub.staffchat` - Access to staff chat
- `networkhub.queue.vip` - Priority queue access
- `networkhub.hud` - View network HUD in hub servers

## Architecture

### Core Components
- **DatabaseManager**: HikariCP connection pooling with async operations
- **RedisManager**: Jedis pub/sub for real-time messaging
- **ServerRegistryManager**: Server registration and discovery with caching
- **HeartbeatManager**: Health monitoring with automatic failure detection
- **HubManager**: Priority-based hub selection and fallback
- **TransferManager**: Player transfer orchestration
- **PlayerTrackingManager**: Cross-server player location tracking
- **TeleporterManager**: Teleporter CRUD and cooldown management
- **QueueManager**: Priority-based queue processing
- **MessagingManager**: Cross-server direct messaging
- **ChatManager**: Global and staff chat coordination
- **AnnouncementManager**: Network-wide announcement broadcasting
- **GUIManager**: GUI lifecycle management
- **NetworkHUD**: Persistent sidebar with network stats

### Database Schema
- `servers` - Registry of all network servers
- `server_health` - Real-time health monitoring
- `player_locations` - Cross-server player tracking
- `teleporters` - Physical teleporter blocks
- `transfer_history` - Audit log
- `server_queues` - Player queues for full servers
- `chat_messages` - Cross-server chat history (optional)
- `announcements` - Network-wide announcement history

## Building from Source

```bash
# Clone the repository
git clone https://github.com/ImVylo/NetworkHub.git
cd NetworkHub

# Build with Gradle
./gradlew build

# Output: build/libs/NetworkHub.jar
```

## Performance

- **Connection Pooling**: HikariCP for efficient database connections
- **Async Operations**: Non-blocking database queries
- **Caching**: 30-second TTL cache for server registry
- **Redis Pub/Sub**: Sub-50ms cross-server message delivery
- **Optimized Queries**: Indexed database queries for fast lookups
- **Batch Updates**: Efficient batch location updates

## Troubleshooting

### Server not appearing in network
1. Check database connection in `database-config.json`
2. Verify server is sending heartbeats: Check `server_health` table
3. Check logs for registration errors

### Players not transferring
1. Verify destination server is online
2. Check network connectivity between servers
3. Ensure `playerRef.referToServer()` API is available
4. Check transfer_history table for error logs

### Redis connection issues
1. Verify Redis is running: `redis-cli ping`
2. Check Redis credentials in `redis-config.json`
3. Ensure Redis is accessible from all servers
4. Check firewall rules

### Queue not processing
1. Verify queue system is enabled in config
2. Check QueueProcessTask is running (every 2 seconds)
3. Check `server_queues` table for stuck entries
4. Verify target server has available slots

## Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch
3. Make your changes with clear commit messages
4. Test thoroughly on multiple servers
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues, questions, or feature requests:
- Open an issue on [GitHub](https://github.com/ImVylo/NetworkHub/issues)
- Join our community Discord (link coming soon)

## Roadmap

- [ ] Web dashboard for monitoring and remote management
- [ ] Party system (keep groups together)
- [ ] Friend system (join friend's server)
- [ ] Automated server scaling with cloud integration
- [ ] Advanced analytics and metrics dashboard
- [ ] Cross-server economy integration
- [ ] Enhanced moderation tools
- [ ] Inventory/data persistence across transfers

## Credits

Developed by the NetworkHub team for the Hytale community.

Special thanks to the Hytale modding community for their support and feedback.
