importPackage(java.util);
const rand = new Random();

bp.registerBThread( "1", function(){
        for (var j = 0; j < 5; j++) {
            bp.log.info(rand.nextGaussian());
            bp.sync({request: bp.Event("A1"), waitFor:bp.all});
        }

} );

