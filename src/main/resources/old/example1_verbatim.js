importPackage(java.util);

bp.registerBThread( "Main", function(){
    var i;
    for (i = 0; i < 1000; i++) {
        // Entering Good state
        bp.sync( {request:[bp.Event("GPS1"), bp.Event("GPS2"), bp.Event("GPS3")]} );
    }
} );

bp.registerBThread( "FaultTypeA", function(){
    rand1 = new Random();
    while (true) {
        if (rand1.nextDouble() < 0.02) {
            // Entering Fault1 state
            if (rand1.nextDouble() < 0.8){
                bp.sync({waitFor: bp.all, block: bp.Event("GPS1")});
            } else {
                bp.sync({waitFor: bp.all});
            }
            // Entering Fault2 state
            if (rand1.nextDouble() < 0.8){
                bp.sync({waitFor: bp.all, block: bp.Event("GPS1")});
            } else {
                bp.sync({waitFor: bp.all});
            }
            // Entering Fault3 state
            if (rand1.nextDouble() < 0.8){
                bp.sync({waitFor: bp.all, block: bp.Event("GPS1")});
            } else {
                bp.sync({waitFor: bp.all});
            }
        } else {
            // Entering Good state
            bp.sync({waitFor: bp.all});
        }
    }
} );

bp.registerBThread( "FaultTypeB", function(){
    rand2 = new Random();
    while (true){
        if (rand2.nextDouble() < 0.02) {
            // Entering Fault1 state
            if (rand1.nextDouble() < 0.9){
                bp.sync({waitFor: bp.all, block: bp.Event("GPS2")});
            } else {
                bp.sync({waitFor: bp.all});
            }
            // Entering Fault2 state
            if (rand1.nextDouble() < 0.9){
                bp.sync({waitFor: bp.all, block: bp.Event("GPS2")});
            } else {
                bp.sync({waitFor: bp.all});
            }
            // Entering Fault3 state
            if (rand1.nextDouble() < 0.9){
                bp.sync({waitFor: bp.all, block: bp.Event("GPS2")});
            } else {
                bp.sync({waitFor: bp.all});
            }
        } else {
            // Entering Good state
            bp.sync({waitFor: bp.all});
        }
    }
} );
