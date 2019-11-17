importPackage(java.util);

bp.registerBThread( "1", function(){
        for (var j = 0; j < 5; j++) {
            bp.sync({request: bp.Event("A1"), waitFor:bp.all});
        }

} );

bp.registerBThread( "2", function(){
    for (var j = 0; j < 5; j++) {
        bp.sync({request: bp.Event("A2"), waitFor:bp.all});
    }

} );

bp.registerBThread( "3", function(){
    for (var j = 0; j < 5; j++) {
        bp.sync({request: bp.Event("A3"), waitFor:bp.all});
    }

} );

bp.registerBThread( "4", function(){
    for (var j = 0; j < 5; j++) {
        bp.sync({request: bp.Event("A4"), waitFor:bp.all});
    }

} );

