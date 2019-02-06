
bp.registerBThread( "Main", function(){
    var i;
    for (i = 0; i < 10; i++) {
        bp.sync( {request:[bp.Event("GPS1"), bp.Event("GPS2"), bp.Event("GPS3")]} );
    }
} );

bp.registerBThread( "FaultTypeA", function(){
    while (true) {
        if (bp.random.nextFloat() < 0.02) {
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
        } else {
            bp.sync({waitFor: bp.all});
        }
    }
} );

bp.registerBThread( "FaultTypeB", function(){
    while (true){
        if (bp.random.nextFloat() < 0.02) {
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
        } else {
            bp.sync({waitFor: bp.all});
        }
    }
} );
