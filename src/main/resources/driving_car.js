
numOfLanes = 5;
laneLength = 9;
laneNumOfIterations = 5;
malfunctionProbabilityMin = 0.01;
malfunctionProbabilityMax = 0.2;
var malfunctionProbability = 0.1;
malfunctionWindowMin = 2;
malfunctionWindowMax = 10;
var malfunctionWindow=4;
var laneDirection;
var failureType;

for (var i = 0; i < numOfLanes; i++){
    laneDirection = i%2;
    failureType = i%2;

    if (laneDirection){
        (function(n){
        bp.registerBThread("Main"+n, function() {
            for (var j = 0; j < laneLength; j++) {
                bp.sync( {request:bp.Event("Recognized", {id:n, x:j, y:n}), waitFor:  bp.EventSet('', function(e) {
                        return (e.data.id == n);
                    })} );
            }
        });
        })(i);
    } else {
        (function(n){
        bp.registerBThread("Main"+n, function() {
            for (var j = laneLength-1; j > -1; j--) {
                bp.sync( {request:bp.Event("Recognized", {id:n, x:j, y:n}), waitFor:  bp.EventSet('', function(e) {
                        return (e.data.id == n);
                    })} );
            }
        });
        })(i);
    }

    if (failureType){
        (function(n, window, p){
        bp.registerBThread("car"+n+"MalfunctionA", function() {
            for (var j = 0; j < laneLength; j++) {
                if ((j+1)%window==0 && p > bp.random.nextFloat()) {
                    bp.sync( {request: bp.Event("Recognized", {id:n, x:-1, y:n}), block:bp.Event("Recognized", {id:n, x:j, y:n})} );
                } else {
                    bp.sync( {waitFor: bp.Event("Recognized", {id:n, x:j, y:n})} );
                }
            }
        });
        })(i, malfunctionWindow, malfunctionProbability);
    } else {
        (function(n, window, p){
            bp.registerBThread("car"+n+"MalfunctionB", function() {
            for (var j = laneLength-1; j > -1; j--) {
                if ((j+1)%window==0 && p > bp.random.nextFloat()) {
                    if (bp.random.nextBoolean()) {
                        bp.sync( {request: bp.Event("Recognized", {id:n, x:j-1, y:n}), block:bp.Event("Recognized", {id:n, x:j, y:n})} );
                    } else {
                        bp.sync( {request: bp.Event("Recognized", {id:n, x:j+1, y:n}), block:bp.Event("Recognized", {id:n, x:j, y:n})} );
                    }
                } else {
                    bp.sync( {waitFor: bp.Event("Recognized", {id:n, x:j, y:n})} );
                }
            }
        });
        })(i, malfunctionWindow, malfunctionProbability);

    }


}

