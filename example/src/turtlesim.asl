action_uri("ros2+ws://localhost:9090/turtle1/rotate_absolute") .
action_payload(json(M)) :- .random(X) & Theta = X*3.14 & .map.create(M) & .map.put(M, theta, Theta) .
action_form(M) :- .map.create(M) & .map.put(M, "urn:hypermedea:ros2:messageType", "turtlesim/action/RotateAbsolute") .

+!start :
    action_uri(URI) & action_payload(P) & action_form(F)
    <-
    post(URI, P, F) .

+rdf(Anchor, "urn:hypermedea:ros2:goalId", Target) :
    action_uri(Anchor)
    <-
    watch(Target) ;
    +watching(Target) .

+json(Msg) :
    watching(URI)
    <-
    if (.map.get(Msg, status, Status)) {
        .print(Status) ;
        if (Status = 4) {
            .print("Action done, forgetting resource: ", URI) ;
            forget(URI)
        }
    } .

!start .