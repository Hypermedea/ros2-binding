package org.hypermedea.ros;

import org.hypermedea.op.InvalidFormException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class ROS2CancelOperation extends ROS2Operation {

    private final String id;

    public ROS2CancelOperation(String targetURI, Map<String, Object> formFields) {
        super(targetURI, formFields);

        try {
            URI uri = new URI(targetURI);
            id = uri.getFragment();
        } catch (URISyntaxException e) {
            throw new InvalidFormException(e);
        }
    }

    @Override
    protected void sendSingleRequest() {
        if (payload != null) {
            // TODO warn that payload isn't taken into account
        }

        JsonObjectBuilder msgBuilder = Json.createObjectBuilder()
                .add("op", "cancel_action_goal")
                .add("action", name)
                .add("id", id);

        sendMessage(msgBuilder.build());
        onResponse(new ROS2Response(this));
    }

}
