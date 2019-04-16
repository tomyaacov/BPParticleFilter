
numOfLanes = 2;
laneLength = 5;
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

    if (laneDirection==1){
        (function(n){
            bp.registerBThread("Main"+n, function() {
                for (var j = 0; j < laneLength; j++) {
                    bp.sync( {request:bp.Event("Recognized", {id:n, x:j, y:n}), waitFor: bp.Event("Tick", {id:-1})});
                }
            });
        })(i);
    } else {
        (function(n){
            bp.registerBThread("Main"+n, function() {
                for (var j = laneLength-1; j > -1; j--) {
                    bp.sync( {request:bp.Event("Recognized", {id:n, x:j, y:n}), waitFor: bp.Event("Tick", {id:-1})});
                }
            });
        })(i);
    }

    if (failureType==1){
        (function(n, window, p){
            bp.registerBThread("car"+n+"MalfunctionA", function() {
                for (var j = 0; j < laneLength; j++) {
                    if ((j+1)%window==0 && p > bp.random.nextFloat()) {
                        bp.sync( {waitFor: bp.Event("Tick", {id:-1}), block:bp.EventSet('', function(e) {return (e.data.id == n);})} );
                    } else {
                        bp.sync( {waitFor: bp.Event("Tick", {id:-1})} );
                    }
                }
            });
        })(i, malfunctionWindow, malfunctionProbability);
    } else {
        (function(n, window, p){
            bp.registerBThread("car"+n+"MalfunctionB", function() {
                for (var j = 0; j < laneLength; j++) {
                    if ((j+1)%window==0 && p > bp.random.nextFloat()) {
                        bp.sync( {request: bp.Event("Recognized", {id:n, x:j+(bp.random.nextBoolean()*2-1), y:n}), block:bp.Event("Recognized", {id:n, x:j, y:n})} );
                    } else {
                        bp.sync( {waitFor: bp.Event("Tick", {id:-1})} );
                    }
                }
            });
        })(i, malfunctionWindow, malfunctionProbability);

    }
    (function(n){
        bp.registerBThread("interleave"+n, function() {
            for (var j = 0; j < laneLength; j++) {
                bp.sync( {waitFor:  bp.EventSet('', function(e) {return (e.data.id === n);})});
                bp.sync( {waitFor: bp.Event("Tick", {id:-1}), block:bp.EventSet('', function(e) {
                    return e.data.id == n;
                })});
            }
        });
    })(i);



}
bp.registerBThread("Tick", function() {
    for (var j = 0; j < laneLength*numOfLanes; j++) {
        bp.sync({request:bp.Event("Tick", {id:-1})}, 0)
    }
});

