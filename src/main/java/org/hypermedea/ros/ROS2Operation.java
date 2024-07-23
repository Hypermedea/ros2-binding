package org.hypermedea.ros;

import org.hypermedea.ct.RepresentationHandlers;
import org.hypermedea.op.BaseOperation;
import org.hypermedea.op.InvalidFormException;

import javax.json.*;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParsingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public abstract class ROS2Operation extends BaseOperation {

    private class Listener implements WebSocket.Listener {

        @Override
        public void onOpen(WebSocket webSocket) {
            WebSocket.Listener.super.onOpen(webSocket);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            try {
                JsonParser p = Json.createParser(new StringReader(data.toString()));
                p.next();
                JsonObject json = p.getObject();

                onGoingOps.forEach(op -> op.onMessage(json));
            } catch (JsonParsingException e) {
                // TODO warn
            }

            return WebSocket.Listener.super.onText(webSocket, data, last);
        }

        @Override
        public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
            return onText(webSocket, new String(data.array()), last);
        }

        @Override
        public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
            URI uriToRemove = null;

            for (URI uri : openSockets.keySet()) {
                if (openSockets.get(uri).equals(webSocket)) uriToRemove = uri;
            }

            if (uriToRemove == null) {
                // TODO warn
                return null;
            } else {
                openSockets.remove(uriToRemove);
                return new CompletableFuture<>();
            }
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            // TODO re-open the socket
            WebSocket.Listener.super.onError(webSocket, error);
        }
    }

    public static final String DEFAULT_MESSAGE_TYPE = "std_msgs/String";

    private static final WebSocket.Builder wsBuilder = HttpClient.newHttpClient().newWebSocketBuilder();

    private static final Map<URI, WebSocket> openSockets = new HashMap<>();

    private static final Collection<ROS2Operation> onGoingOps = new HashSet<>();

    private final WebSocket socket;

    protected final String name;

    protected final String msgType;

    public ROS2Operation(String targetURI, Map<String, Object> formFields) {
        super(targetURI, formFields);

        try {
            URI uri = new URI(targetURI);
            URI conURI = getConnectionURI(uri);

            if (openSockets.containsKey(conURI)) {
                socket = openSockets.get(conURI);
            } else {
                socket = wsBuilder.buildAsync(conURI, new Listener()).get();
                openSockets.put(conURI, socket);
            }

            name = uri.getPath();

            String msgTypeFromForm = (String) form.get(ROS2.messageType);
            msgType = msgTypeFromForm != null ? msgTypeFromForm : getDefaultMessageType();

            onGoingOps.add(this);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    protected void sendMessage(JsonObject msg) {
//        socket.sendText(msg.toString(), true);
        try {
            socket.sendText(msg.toString(), true).get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    protected void onMessage(JsonObject msg) {
        // do nothing by default
    }

    @Override
    protected void end() throws IOException {
        onGoingOps.remove(this);
    }

    protected Optional<JsonObject> getJsonPayload() throws IOException {
        Optional<JsonObject> jsonPayload = Optional.empty();

        if (!payload.isEmpty()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            RepresentationHandlers.serialize(payload, out, getTargetURI());
            String str = out.toString(StandardCharsets.UTF_8);

            JsonParser p = Json.createParser(new StringReader(str));
            JsonParser.Event e = p.next();

            if (!e.equals(JsonParser.Event.START_OBJECT)) {
                throw new InvalidFormException("The ROS2 binding expects JSON objects as payload but got " + str);
            }

            jsonPayload = Optional.of(p.getObject());
        }

        return jsonPayload;
    }

    /**
     * Get the name of the topic or the action on which the operation applies.
     *
     * @return a topic of action name
     */
    public String getName() {
        return name;
    }

    /**
     * Return the operation-specific default message type.
     *
     * @return a message type, e.g. {@value DEFAULT_MESSAGE_TYPE}
     */
    protected String getDefaultMessageType() {
        return DEFAULT_MESSAGE_TYPE;
    }

    protected String getMessageType() {
        if (form.containsKey(ROS2.messageType)) return (String) form.get(ROS2.messageType);
        else return getDefaultMessageType();
    }

    private URI getConnectionURI(URI uri) {
        try {
            return new URI("ws", uri.getUserInfo(), uri.getHost(), uri.getPort(), null, null, null);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
