package org.hypermedea.ros;

import org.hypermedea.op.Response;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.Map;

public class ROS2SubscribeOperation extends ROS2Operation {

    public ROS2SubscribeOperation(String targetURI, Map<String, Object> formFields) {
        super(targetURI, formFields);
    }

    @Override
    protected void sendSingleRequest() {
        if (!payload.isEmpty()) {
            // TODO warn that payload isn't taken into account
        }

        JsonObjectBuilder msgBuilder = Json.createObjectBuilder()
                .add("op", "subscribe")
                .add("topic", name);

        sendMessage(msgBuilder.build());
    }

    @Override
    protected void onMessage(JsonObject payload) {
        // TODO check that payload is well-formed
        JsonObject msg = payload.getJsonObject("msg");

        Response r = new ROS2Response(msg, ROS2SubscribeOperation.this);
        onResponse(r);
    }

}
