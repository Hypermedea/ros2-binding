action_uri("ros2+ws://localhost:9090/turtle1/rotate_absolute") .
action_payload(json([ kv(theta, 0.0) ])) .
action_form([kv("https://github.com/RobotWebTools/rosbridge_suite/blob/ros1/ROSBRIDGE_PROTOCOL.md#messageType", "turtlesim/action/RotateAbsolute")]) .

+!start :
    action_uri(URI) & action_payload(P) & action_form(F)
    <-
    post(URI, P, F) .

+rdf(Anchor, "https://github.com/RobotWebTools/rosbridge_suite/blob/ros1/ROSBRIDGE_PROTOCOL.md#goalId", Target) :
    action_uri(Anchor)
    <-
    watch(Target) ;
    +watching(Target) .

+json(Msg) :
    watching(URI)
    <-
    if (.member(kv(status, Status), Msg)) {
        .print(Status) ;
        if (Status = 4) {
            .print("Action done, forgetting resource: ", URI) ;
            forget(URI)
        }
    } .

{ include("$jacamoJar/templates/common-cartago.asl") }
{ include("$jacamoJar/templates/common-moise.asl") }
