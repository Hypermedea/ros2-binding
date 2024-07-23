package org.hypermedea.ros;

import org.hypermedea.op.InvalidFormException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class ROS2PublishOperation extends ROS2Operation {

    public ROS2PublishOperation(String targetURI, Map<String, Object> formFields) {
        super(targetURI, formFields);
    }

    @Override
    protected void sendSingleRequest() {
        // TODO first advertize if not done already

        Optional<JsonObject> jsonPayload = Optional.empty();

        try {
            jsonPayload = getJsonPayload();
        } catch (IOException e) {
            // TODO in parent class instead? (redundant with SendGoalOperation)
            throw new InvalidFormException(e);
        }

        if (jsonPayload.isEmpty()) {
            // TODO warn and return
        } else {
            JsonObjectBuilder msgBuilder = Json.createObjectBuilder()
                    .add("op", "publish")
                    .add("topic", name)
                    .add("msg", jsonPayload.get());

            sendMessage(msgBuilder.build());

            onResponse(new ROS2Response(this));
        }
    }

}
