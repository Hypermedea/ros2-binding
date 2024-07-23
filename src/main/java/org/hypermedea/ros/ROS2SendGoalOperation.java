package org.hypermedea.ros;

import org.hypermedea.op.InvalidFormException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ROS2SendGoalOperation extends ROS2Operation {

    private static final long TIMEOUT = 60l;

    public ROS2SendGoalOperation(String targetURI, Map<String, Object> formFields) {
        super(targetURI, formFields);
    }

    @Override
    protected void sendSingleRequest() {
        String msgType = getMessageType();
        String id = UUID.randomUUID().toString();

        JsonObjectBuilder msgBuilder = Json.createObjectBuilder()
                .add("op", "send_action_goal")
                .add("action", name)
                .add("action_type", msgType)
                .add("id", id)
                .add("feedback", true);

        try {
            Optional<JsonObject> jsonPayload = getJsonPayload();
            if (jsonPayload.isPresent()) msgBuilder.add("args", jsonPayload.get());
        } catch (IOException e) {
            throw new InvalidFormException(e);
        }

        sendMessage(msgBuilder.build());

        ROS2Response r = new ROS2Response(this);
        r.addLink(ROS2.goalId, target + "#" + id);

        onResponse(r);
    }

}
