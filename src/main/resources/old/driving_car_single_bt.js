
numOfLanes = 4;
laneLength = 9;
var malfunctionProbability = 0.2;
var malfunctionWindow=4;
var laneDirection;
var failureType;

for (var i = 0; i < numOfLanes; i++){
    laneDirection = i%2;
    failureType = i%2;

    if (laneDirection==1){
        (function(n, window, p){
            bp.registerBThread("Main"+n, function() {
                for (var j = 0; j < laneLength; j++) {
                    if ((j+1)%window==0 && p > bp.random.nextFloat()) {
                        bp.sync( {waitFor: bp.all} );
                    } else {
                        bp.sync( {request:bp.Event("Recognized", {id:n, x:j, y:n})});
                    }
                }
            });
        })(i, malfunctionWindow, malfunctionProbability);
    } else {
        (function(n, window, p){
            bp.registerBThread("Main"+n, function() {
                for (var j = laneLength-1; j > -1; j--) {
                    if ((j+1)%window==0 && p > bp.random.nextFloat()) {
                        bp.sync( {request: bp.Event("Recognized", {id:n, x:j+(bp.random.nextBoolean()*2-1), y:n})} );
                    } else {
                        bp.sync( {request:bp.Event("Recognized", {id:n, x:j, y:n})});
                    }
                }
            });
        })(i, malfunctionWindow, malfunctionProbability);
    }


}


