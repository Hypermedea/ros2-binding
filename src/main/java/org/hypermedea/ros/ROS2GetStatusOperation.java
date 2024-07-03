package org.hypermedea.ros;

import org.hypermedea.op.InvalidFormException;

import javax.json.JsonObject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

public class ROS2GetStatusOperation extends ROS2Operation {

    private final String id;

    public ROS2GetStatusOperation(String targetURI, Map<String, Object> formFields) {
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
        if (!payload.isEmpty()) {
            // TODO warn that payload isn't taken into account
        }

        // do nothing, SendGoalOperation should already have subscribed to action feedback
    }

    @Override
    protected void onMessage(JsonObject payload) {
        String op = payload.getString("op");
        if (op.equals("action_feedback") || op.equals("action_result")) {
            if (payload.getString("id").equals(id)) {
                if (payload.containsKey("result") && !payload.getBoolean("result")) {
                    onError();
                } else {
                    JsonObject json = payload.getJsonObject("values");
                    onResponse(new ROS2Response(json, this));
                }
            }
        }
    }

    @Override
    protected void end() throws IOException {
        super.end();
    }

    @Override
    protected String getDefaultMessageType() {
        // FIXME not true anymore
        return ROS2GoalStatusArrayWrapper.GOAL_STATUS_ARRAY_MESSAGE_TYPE;
    }

}
