package org.hypermedea.ros;

/**
 * <p>
 *   Vocabulary to declare ROS form fields in Hypermedea operations.
 * </p>
 * <p>
 *   Example for basic publish/subscribe, where the path of the target URI corresponds to a topic name:
 * </p>
 * <pre><code>+!call_cmdvel &lt;-
  put(
    "ros+ws://example.org/turtlesim/cmd_vel",
    json([kv("linear", ...), kv("angular", ...)]),
    [kv("urn:hypermedea:ros2:messageType", "geometry_msgs/Twist")]
  ) .</code></pre>
 * <p>
 *   Example for <code>actionlib</code> operations, where the path of the target URI corresponds to an action name
 *   and the URI fragment corresponds to the ID of an on-going action:
 * </p>
 * <pre><code>+!call_shape &lt;-
  post("ros+ws://example.org/turtle_shape") ;
  .wait({ +rdf("ros+ws://example.org/turtle_shape", "urn:hypermedea:ros2:goalId", ID) }) ;
 .print(ID) .</code></pre>
 */
public class ROS2 {

    public static final String NAMESPACE = "urn:hypermedea:ros2:";

    public static final String messageType = NAMESPACE + "messageType";

    public static final String goalId = NAMESPACE + "goalId";

}
