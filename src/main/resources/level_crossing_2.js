const n = 5;
const ClosingRequests = bp.EventSet("ClosingRequests", function(evt) {return evt.name.startsWith("ClosingRequest")});
const ReopeningRequests = bp.EventSet("ReopeningRequests", function(evt) {return evt.name.startsWith("ReopeningRequest")});
const Enters = bp.EventSet("Enters", function(evt) {return evt.name.startsWith("Entering")});

for (var i = 0; i < n; i++){
    (function(i){
        bp.registerBThread("RailwayTraffic_" + i, function() {
            while (true){
                bp.sync({request: bp.Event("Approaching_" + i)});
                bp.sync({request: bp.Event("ClosingRequest" + i), block: bp.Event("Raise")});
                bp.sync({request: bp.Event("Entering_" + i), block: bp.Event("Raise")});
                bp.sync({request: bp.Event("Leaving_" + i), block: bp.Event("Raise")});
                bp.sync({request: bp.Event("ReopeningRequest" + i)});
            }
        });
    })(i);
}

bp.registerBThread("Barriers", function() {
    while (true){
        bp.sync({waitFor: ClosingRequests});
        bp.sync({request: bp.Event("Lower")});
        bp.sync({waitFor: ReopeningRequests});
        bp.sync({request: bp.Event("Raise")});
    }
});

bp.registerBThread("no_entering_when_barrier_up", function() {
    while (true){
        bp.sync({waitFor: bp.Event("Lower"), block: Enters});
        bp.sync({waitFor: bp.Event("Raise")});
    }
});
