
bp.registerBThread( "Main", function(){
    var i;
    for (i = 0; i < 15; i++) {
        bp.sync( {request:[bp.Event("GPS1"), bp.Event("GPS2"), bp.Event("GPS3")]} );
    }
} );

bp.registerBThread( "FaultTypeA", function(){
    while (true) {
        if (bp.random.nextFloat() < 0.02) {
            bp.log.info("FaultTypeA - started");
            if (bp.random.nextFloat()< 0.8){
                bp.sync({waitFor: bp.all, block: bp.Event("GPS1")});
            } else {
                bp.sync({waitFor: bp.all});
            }
            if (bp.random.nextFloat() < 0.8){
                bp.sync({waitFor: bp.all, block: bp.Event("GPS1")});
            } else {
                bp.sync({waitFor: bp.all});
            }
            if (bp.random.nextFloat() < 0.8){
                bp.sync({waitFor: bp.all, block: bp.Event("GPS1")});
            } else {
                bp.sync({waitFor: bp.all});
            }
            bp.log.info("FaultTypeA - ended");
        } else {
            bp.sync({waitFor: bp.all});
        }
    }
} );

bp.registerBThread( "FaultTypeB", function(){
    while (true){
        if (bp.random.nextFloat() < 0.02) {
            bp.log.info("FaultTypeB - started");
            if (bp.random.nextFloat() < 0.9){
                bp.sync({waitFor: bp.all, block: bp.Event("GPS2")});
            } else {
                bp.sync({waitFor: bp.all});
            }
            if (bp.random.nextFloat() < 0.9){
                bp.sync({waitFor: bp.all, block: bp.Event("GPS2")});
            } else {
                bp.sync({waitFor: bp.all});
            }
            if (bp.random.nextFloat() < 0.9){
                bp.sync({waitFor: bp.all, block: bp.Event("GPS2")});
            } else {
                bp.sync({waitFor: bp.all});
            }
            bp.log.info("FaultTypeB - ended");
        } else {
            bp.sync({waitFor: bp.all});
        }
    }
} );
