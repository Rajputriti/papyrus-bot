package games.redpoint;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.flowpowered.math.vector.Vector3f;
import com.nukkitx.protocol.bedrock.packet.PlayerListPacket;

import org.apache.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

public class BotWebSocketServer extends WebSocketServer {
    private static final Logger LOG = Logger.getLogger(BotWebSocketServer.class);

    private PapyrusBot bot;

    public BotWebSocketServer(PapyrusBot bot) throws UnknownHostException {
        super(new InetSocketAddress(8080));

        this.bot = bot;
    }

    @Override
    public void onClose(WebSocket conn, int code, String reason, boolean remote) {
        LOG.debug("Got WebSocket close");
    }

    @Override
    public void onError(WebSocket conn, Exception ex) {
        LOG.debug("Got WebSocket exception: " + ex.toString());
    }

    @Override
    public void onMessage(WebSocket conn, String message) {
        LOG.debug("Got WebSocket message: " + message);
    }

    @Override
    public void onOpen(WebSocket conn, ClientHandshake handshake) {
        LOG.info("WebSocket connection opened from " + conn.getRemoteSocketAddress().toString());

        for (PlayerListPacket.Entry entry : this.bot.players.values()) {
            if (this.bot.knownPlayerPositions.containsKey(entry.getUuid().toString())) {
                Vector3f position = this.bot.knownPlayerPositions.get(entry.getUuid().toString());

                PlayerPositionWebSocketMessage msg = new PlayerPositionWebSocketMessage();
                msg.playerName = entry.getName();
                msg.x = position.getX();
                msg.z = position.getZ();
                try {
                    conn.send(this.bot.objectMapper.writeValueAsString(msg));
                } catch (JsonProcessingException e) {
                }
            }
        }
    }

    @Override
    public void onStart() {
        LOG.info("WebSocket server is listening on port 8080");
    }
}