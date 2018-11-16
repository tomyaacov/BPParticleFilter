importPackage(java.util);

bp.registerBThread( "Main", function(){
    while (true){
        bProgramState.updateState("Main", "1");
        bp.sync( {request:[bp.Event("Hot"), bp.Event("Cold")]} );
    }
} );

bp.registerBThread( "Fault1", function(){
    rand1 = new Random();
    while (true) {
        if (rand1.nextDouble() < 0.02) {
            bProgramState.updateState("Fault1", "2");
            bp.sync({waitFor: bp.all, block: bp.Event("Hot")});
            bProgramState.updateState("Fault1", "3");
            bp.sync({waitFor: bp.all});
            bProgramState.updateState("Fault1", "4");
            bp.sync({waitFor: bp.all, block: bp.Event("Hot")});
            bProgramState.updateState("Fault1", "5");
            bp.sync({waitFor: bp.all});
            bProgramState.updateState("Fault1", "6");
            bp.sync({waitFor: bp.all, block: bp.Event("Hot")});
            bProgramState.updateState("Fault1", "7");
            bp.sync({waitFor: bp.all});
            bProgramState.updateState("Fault1", "8");
            bp.sync({waitFor: bp.all, block: bp.Event("Hot")});
            bProgramState.updateState("Fault1", "9");
            bp.sync({waitFor: bp.all});
            bProgramState.updateState("Fault1", "10");
            bp.sync({waitFor: bp.all, block: bp.Event("Hot")});
            bProgramState.updateState("Fault1", "11");
            bp.sync({waitFor: bp.all});
        } else {
            bProgramState.updateState("Fault1", "1");
            bp.sync({waitFor: bp.all});
        }
    }
} );

bp.registerBThread( "Fault2", function(){
    rand2 = new Random();
    while (true){
        if (rand2.nextDouble() < 0.02) {
            bProgramState.updateState("Fault2", "2");
            bp.sync({waitFor: bp.all, block: bp.Event("Cold")});
            bProgramState.updateState("Fault2", "3");
            bp.sync({waitFor: bp.all});
            bProgramState.updateState("Fault2", "4");
            bp.sync({waitFor: bp.all, block: bp.Event("Cold")});
            bProgramState.updateState("Fault2", "5");
            bp.sync({waitFor: bp.all});
            bProgramState.updateState("Fault2", "6");
            bp.sync({waitFor: bp.all, block: bp.Event("Cold")});
            bProgramState.updateState("Fault2", "7");
            bp.sync({waitFor: bp.all});
            bProgramState.updateState("Fault2", "8");
            bp.sync({waitFor: bp.all, block: bp.Event("Cold")});
            bProgramState.updateState("Fault2", "9");
            bp.sync({waitFor: bp.all});
            bProgramState.updateState("Fault2", "10");
            bp.sync({waitFor: bp.all, block: bp.Event("Cold")});
            bProgramState.updateState("Fault2", "11");
            bp.sync({waitFor: bp.all});
        } else {
            bProgramState.updateState("Fault2", "1");
            bp.sync({waitFor: bp.all});
        }
    }
} );
