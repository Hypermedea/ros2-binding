package org.hypermedea.ros;

import org.hypermedea.op.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.*;

/**
 * Web of Things (WoT) protocol binding for ROS 2, via the
 * <a href="https://github.com/RobotWebTools/rosbridge_suite/blob/ros2/ROSBRIDGE_PROTOCOL.md">rosbridge protocol</a>.
 */
public class ROS2Binding extends BaseProtocolBinding {

    public static final String ROSBRIDGE_PROTOCOL = "rosbridge-v2";

    public static final String URI_SCHEME = "ros2+ws";

    private final WebSocket.Builder wsBuilder = HttpClient.newHttpClient().newWebSocketBuilder();

    @Override
    public String getProtocol() {
        return ROSBRIDGE_PROTOCOL;
    }

    @Override
    public Collection<String> getSupportedSchemes() {
        Set<String> singleton = new HashSet<>();
        singleton.add(URI_SCHEME);

        return singleton;
    }

    @Override
    protected Operation bindGet(String targetURI, Map<String, Object> formFields) {
        Optional<String> idOpt = getFragment(targetURI);
        if (idOpt.isPresent()) return new ROS2GetStatusOperation(targetURI, formFields);

        ROS2SubscribeOperation op = new ROS2SubscribeOperation(targetURI, formFields);
        op.setTimeout(0); // unset timeout by default
        return op;
    }

    @Override
    protected Operation bindWatch(String targetURI, Map<String, Object> formFields) {
        return bindGet(targetURI, formFields);
    }

    @Override
    protected Operation bindPut(String targetURI, Map<String, Object> formFields) {
        // TODO if fragment, throw InvalidFormException
        return new ROS2PublishOperation(targetURI, formFields);
    }

    @Override
    protected Operation bindPost(String targetURI, Map<String, Object> formFields) {
        return new ROS2SendGoalOperation(targetURI, formFields);
    }

    @Override
    protected Operation bindDelete(String targetURI, Map<String, Object> formFields) {
        Optional<String> idOpt = getFragment(targetURI);
        if (idOpt.isPresent()) return new ROS2CancelOperation(targetURI, formFields);

        throw new InvalidFormException("Non-action resources cannot be deleted in ROS");
    }

    private Optional<String> getFragment(String uri) {
        try {
            String id = new URI(uri).getFragment();
            return id == null ? Optional.empty() : Optional.of(id);
        } catch (URISyntaxException e) {
            throw new InvalidFormException(e);
        }
    }

}
