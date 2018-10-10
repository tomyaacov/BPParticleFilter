importPackage(java.util);

bp.registerBThread( "1", function(){
    while (true){
        bp.sync( {request:bp.Event("A1")} );
        bp.sync( {request:bp.Event("A2")} );
        bp.sync( {request:bp.Event("A3")} );
        bp.sync( {request:bp.Event("A4")} );
        bp.sync( {request:bp.Event("A5")} );
        bp.sync( {request:bp.Event("A6")} );
        bp.sync( {request:bp.Event("A7")} );
    }

} );

bp.registerBThread( "2", function(){
    rand = new Random();
    while (true){
        bp.sync( {waitFor:bp.Event("A1")} );
        bp.sync( {waitFor:bp.Event("A2")} );
        bp.sync( {waitFor:bp.Event("A3")} );
        bp.sync( {waitFor:bp.Event("A4")} );
        bp.sync( {waitFor:bp.Event("A5")} );
        bp.sync( {waitFor:bp.Event("A6")} );
         if (rand.nextDouble() > 0.7){
             bp.sync( {request:bp.Event("B1"), block:bp.Event("A7")} );
         } else {
             bp.sync( {waitFor:bp.Event("A7")} );
         }
    }
} );
