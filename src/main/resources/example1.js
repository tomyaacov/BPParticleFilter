importPackage(java.util);

bp.registerBThread( "Main", function(){
    var i;
    for (i = 0; i < 1000; i++) {
        bProgramState.updateState("Main", "Good");
        bp.sync( {request:[bp.Event("GPS1"), bp.Event("GPS2"), bp.Event("GPS3")]} );
    }
} );

bp.registerBThread( "FaultTypeA", function(){
    rand1 = new Random();
    while (true) {
        if (rand1.nextDouble() < 0.02) {
            bProgramState.updateState("FaultTypeA", "Fault1");
            if (rand1.nextDouble() < 0.8){
                bp.sync({waitFor: bp.all, block: bp.Event("GPS1")});
            } else {
                bp.sync({waitFor: bp.all});
            }
            bProgramState.updateState("FaultTypeA", "Fault2");
            if (rand1.nextDouble() < 0.8){
                bp.sync({waitFor: bp.all, block: bp.Event("GPS1")});
            } else {
                bp.sync({waitFor: bp.all});
            }
            bProgramState.updateState("FaultTypeA", "Fault3");
            if (rand1.nextDouble() < 0.8){
                bp.sync({waitFor: bp.all, block: bp.Event("GPS1")});
            } else {
                bp.sync({waitFor: bp.all});
            }
        } else {
            bProgramState.updateState("FaultTypeA", "Good");
            bp.sync({waitFor: bp.all});
        }
    }
} );

bp.registerBThread( "FaultTypeB", function(){
    rand2 = new Random();
    while (true){
        if (rand2.nextDouble() < 0.02) {
            bProgramState.updateState("FaultTypeB", "Fault1");
            if (rand1.nextDouble() < 0.9){
                bp.sync({waitFor: bp.all, block: bp.Event("GPS2")});
            } else {
                bp.sync({waitFor: bp.all});
            }
            bProgramState.updateState("FaultTypeB", "Fault2");
            if (rand1.nextDouble() < 0.9){
                bp.sync({waitFor: bp.all, block: bp.Event("GPS2")});
            } else {
                bp.sync({waitFor: bp.all});
            }
            bProgramState.updateState("FaultTypeB", "Fault3");
            if (rand1.nextDouble() < 0.9){
                bp.sync({waitFor: bp.all, block: bp.Event("GPS2")});
            } else {
                bp.sync({waitFor: bp.all});
            }
        } else {
            bProgramState.updateState("FaultTypeB", "Good");
            bp.sync({waitFor: bp.all});
        }
    }
} );
