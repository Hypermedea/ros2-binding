/**
 * <p>
 *   ROS binding for Hypermedea. To register it to an instance of Hypermedea,
 *   one simply has to include it in the class path of the Hypermedea application.
 * </p>
 * <p>
 *   The ROS binding can perform operations on URIs with the {@code ros+ws} scheme
 *   (ROS over WebSocket, as specified in the
 *   <a href="https://github.com/RobotWebTools/rosbridge_suite/blob/ros1/ROSBRIDGE_PROTOCOL.md">rosbridge v2.0</a>
 *   specification).
 * </p>
 * <p>
 *   ROS URIs are standard {@code ros+ws} URIs, e.g. {@code ros+ws://example.org/path/to/resource#action_id}.
 *   In this example URI:
 * </p>
 * <ul>
 *   <li>{@code example.org} is the URI of the rosbridge server, listening over WebSocket,</li>
 *   <li>{@code /path/to/resource} is the name of a topic or an action server and</li>
 *   <li>{@code action_id} is the ID of some action (if {@code /path/to/resource} refers to an action server).</li>
 * </ul>
 * <p>
 *   Note that from the URI alone, it is not possible to distinguish between plain topics and action servers
 *   (managing several topics such as {@code goal}, {@code status} and {@code cancel}). However, if a URI has
 *   a fragment, it necessary refers to an action and not to a topic.
 *   The mapping to Hypermedea operations is then as follows:
 * </p>
 * <table>
 *     <caption>Mapping between Hypermedea and ROS</caption>
 *     <tr>
 *         <th>Hypermedea operation</th>
 *         <th>ROS operation</th>
 *     </tr>
 *     <tr>
 *         <td>GET</td>
 *         <td>
 *             Subscribe to a topic or, if the URI includes a fragment, to the action server's
 *             {@code status} topic (single synchronous notification)
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>WATCH</td>
 *         <td>
 *             Subscribe to a topic or, if the URI includes a fragment, to the action server's
 *             {@code status} topic.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>PUT</td>
 *         <td>Publish on topic</td>
 *     </tr>
 *     <tr>
 *         <td>POST</td>
 *         <td>Publish on action server's <code>goal</code> topic</td>
 *     </tr>
 *     <tr>
 *         <td>PATCH</td>
 *         <td><i>Not supported</i></td>
 *     </tr>
 *     <tr>
 *         <td>DELETE</td>
 *         <td>Publish on action's server's <code>cancel</code> topic</td>
 *     </tr>
 * </table>
 * <p>
 *   See the running example in the {@code /example} folder, in which a Jason agent controls a Turtlesim node.
 *   This example also illustrates some of the configuration points of the ROS binding,
 *   as documented in the {@link org.hypermedea.ros.ROS2 ROS} class.
 * </p>
 */
package org.hypermedea.ros;