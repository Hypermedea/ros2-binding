package org.hypermedea.ros;

import jason.asSyntax.Literal;
import org.hypermedea.ct.RepresentationHandlers;
import org.hypermedea.op.*;
import org.hypermedea.tools.Identifiers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.json.Json;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Before running tests, run:
 * <pre>$ ros2 run turtlesim turtlesim_node
$ ros2 run rosbridge_server rosbridge_websocket
 * </pre>
 * (after sourcing the proper ROS2 <code>setup</code> files)
 */
public class ROS2BindingTest {

    private static class EmptyCallback implements ResponseCallback {

        @Override
        public void onResponse(Response response) {
            // do nothing
        }

        @Override
        public void onError() {
            // do nothing
        }

    }

    @Test
    public void testPublish() throws IOException {
        String uri = "ros2+ws://localhost:9090/turtle1/cmd_vel";

        Map<String, Object> form = new HashMap<>();
        form.put(ROS2.messageType, "geometry_msgs/Twist");
        form.put(Operation.METHOD_NAME_FIELD, Operation.PUT);

        Operation op = ProtocolBindings.bind(uri, form);

        assertInstanceOf(ROS2PublishOperation.class, op);

       String twist = "{\n" +
               "  \"linear\": { \"x\":-2.0, \"y\": 0.0, \"z\": 0.0 },\n" +
               "  \"angular\": { \"x\":0.0, \"y\": 0.0, \"z\": 0.0 }\n" +
               "}";

        setJsonPayload(op, twist);

        op.sendRequest();
        Response r = op.getResponse();

        assertEquals(Response.ResponseStatus.OK, r.getStatus());
    }

    @Test
    public void testSubscribe() throws IOException {
        String uri = "ros2+ws://localhost:9090/turtle1/pose";

        Map<String, Object> form = new HashMap<>();
        form.put(ROS2.messageType, "turtlesim/Pose");
        form.put(Operation.METHOD_NAME_FIELD, Operation.WATCH);

        Operation op = ProtocolBindings.bind(uri, form);

        ((BaseOperation) op).setTimeout(2);

        assertInstanceOf(ROS2SubscribeOperation.class, op);

        // avoid that op ends because no one watches
        ResponseCallback cb = new EmptyCallback();
        op.registerResponseCallback(cb);

        // wait synchronously for the first response
        op.sendRequest();
        Response response = op.getResponse();

        assertEquals(Response.ResponseStatus.OK, response.getStatus());

        assertFalse(response.getPayload().isEmpty());

        JsonObject pose = getJsonPayload(response);

        assertTrue(pose.containsKey("x"));
        assertInstanceOf(JsonNumber.class, pose.get("x"));
    }

    @Test
    public void testService() throws IOException {
        String uri = "ros2+ws://localhost:9090/turtle1/teleport_relative";

        Map<String, Object> form = new HashMap<>();
        form.put(Operation.METHOD_NAME_FIELD, Operation.POST);
        form.put(ROS2.requestType, ROS2.callService);
        //form.put(ROS2.messageType, "turtlesim/srv/TeleportRelative");

        Operation callService = ProtocolBindings.bind(uri, form);

        assertInstanceOf(ROS2CallServiceOperation.class, callService);

        String goal = "{ \"linear\": 1.3, \"angular\": 0.2}";

        setJsonPayload(callService, goal);

        callService.sendRequest();
        Response response = callService.getResponse();

        assertEquals(Response.ResponseStatus.OK, response.getStatus());
    }

    @Test
    public void testAction() throws IOException {
        String uri = "ros2+ws://localhost:9090/turtle1/rotate_absolute";

        Map<String, Object> form = new HashMap<>();
        form.put(Operation.METHOD_NAME_FIELD, Operation.POST);
        form.put(ROS2.messageType, "turtlesim/action/RotateAbsolute");

        Operation sendGoal = ProtocolBindings.bind(uri, form);

        assertInstanceOf(ROS2SendGoalOperation.class, sendGoal);

        String goal = "{\n" +
                "  \"theta\": 1.3\n" +
                "}";

        setJsonPayload(sendGoal, goal);

        sendGoal.sendRequest();
        Response response = sendGoal.getResponse();

        assertEquals(1, sendGoal.getPayload().size());

        Optional<Literal> linkToAction = response.getPayload().stream().findAny();

        assertTrue(linkToAction.isPresent());

        String rel = Identifiers.getLexicalForm(linkToAction.get().getTerm(1));
        String actionURI = Identifiers.getLexicalForm(linkToAction.get().getTerm(2));

        assertEquals(ROS2.goalId, rel);

        HashMap<String, Object> form1 = new HashMap<>();
        HashMap<String, Object> form2 = new HashMap<>();

        form1.put(Operation.METHOD_NAME_FIELD, Operation.GET);
        form2.put(Operation.METHOD_NAME_FIELD, Operation.DELETE);

        Operation getStatus = ProtocolBindings.bind(actionURI, form1);
        Operation cancel = ProtocolBindings.bind(actionURI, form2);

        assertInstanceOf(ROS2GetStatusOperation.class, getStatus);
        assertInstanceOf(ROS2CancelOperation.class, cancel);

        ResponseCallback cb = new EmptyCallback();

        // avoid that getStatus op ends because no one watches
        getStatus.registerResponseCallback(cb);

        getStatus.sendRequest();
        Response status = getStatus.getResponse();

        assertFalse(status.getPayload().isEmpty());
        assertNotNull(getJsonPayload(status).get("remaining"));

        // ends getStatus op
        getStatus.unregisterResponseCallback(cb);

        cancel.sendRequest();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }

        // TODO no error is returned by rosbridge if the goal ID doesn't exist?

//        // operation must be rebound
//        getStatus = ProtocolBindings.bind(actionURI, form1);
//
//        // avoid that getStatus op ends because no one watches
//        getStatus.registerResponseCallback(cb);
//
//        getStatus.sendRequest();
//        status = getStatus.getResponse();
//
//        if (!status.getPayload().isEmpty()) {
//            assertEquals(Json.createValue(2), getJsonPayload(status).get("status"));
//        } else {
//            assertEquals(Response.ResponseStatus.CLIENT_ERROR, status.getStatus());
//        }
    }

    private void setJsonPayload(Operation op, String json) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(json.getBytes());
        Collection<Literal> p = RepresentationHandlers.deserialize(in, op.getTargetURI(), "application/json");

        op.setPayload(p);
    }

    private JsonObject getJsonPayload(Response r) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        RepresentationHandlers.serialize(r.getPayload(), out, r.getOperation().getTargetURI());

        JsonReader reader = Json.createReader(new StringReader(out.toString()));
        return reader.readObject();
    }

}
