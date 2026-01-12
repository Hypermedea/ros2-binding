package org.hypermedea.ros;

import org.hypermedea.op.InvalidFormException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ROS2CallServiceOperation extends ROS2Operation {

    private static final long TIMEOUT = 60;

    private final String id;

    public ROS2CallServiceOperation(String targetURI, Map<String, Object> formFields) {
        super(targetURI, formFields);

        id = UUID.randomUUID().toString();

        keepAlive(getGoalURI());
    }

    @Override
    protected void sendSingleRequest() {
        String msgType = getMessageType();

        JsonObjectBuilder msgBuilder = Json.createObjectBuilder()
                .add("op", "call_service")
                .add("service", name)
                .add("id", id);

        try {
            Optional<JsonObject> jsonPayload = getJsonPayload();
            jsonPayload.ifPresent(jsonObject -> msgBuilder.add("args", jsonObject));
        } catch (IOException e) {
            throw new InvalidFormException(e);
        }

        sendMessage(msgBuilder.build());
    }

    @Override
    protected void onMessage(JsonObject payload) {
        String op = payload.getString("op");
        if (op.equals("service_response")) {
            if (payload.getString("id").equals(id)) {
                if (payload.containsKey("result") && !payload.getBoolean("result")) {
                    onError();
                } else {
                    JsonObject json = payload.getJsonObject("values");
                    onResponse(new ROS2Response(json, this));
                }

                try {
                    end();
                } catch (IOException e) {
                    // do nothing
                }
            }
        }
    }

    private String getGoalURI() {
        return target + "#" + id;
    }

}
