const n = 2;

const ClosingRequests = bp.EventSet("ClosingRequests", function(evt) {return evt.name.startsWith("ClosingRequest")});
const ReopeningRequests = bp.EventSet("ReopeningRequests", function(evt) {return evt.name.startsWith("ReopeningRequest")});

for (var i = 0; i < n; i++){
    (function(i){
        bp.registerBThread("RailwayTraffic_" + i, function() {
            while (true){
                bp.sync({request: bp.Event("Approaching_" + i)});
                bp.sync({waitFor: [bp.Event("KeepDown"), bp.Event("Lower")], block: ReopeningRequests});
                bp.sync({request: bp.Event("Entering_" + i), block: ReopeningRequests});
                bp.sync({request: bp.Event("Leaving_" + i), block: ReopeningRequests});
            }
        });

        bp.registerBThread("LCController_" + i, function() {
            while (true){
                evt = bp.sync({waitFor: bp.all});
                if (evt.name == "Approaching_" + i){
                    bp.sync({request: bp.Event("ClosingRequest" + i)});
                } else {
                    if (evt.name == "Leaving_" + i){
                        bp.sync({request: bp.Event("ReopeningRequest" + i), block: bp.Event("Approaching_" + i)});//?
                    }
                }
            }
        })

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

bp.registerBThread("Barriers_KeepDown", function() {
    while (true){
        bp.sync({waitFor: bp.Event("Lower")});
        while (true){
            evt = bp.sync({waitFor: bp.all});
            if (evt.name.startsWith("ClosingRequest")){
                bp.sync({request: bp.Event("KeepDown"), block: ClosingRequests});
            } else {
                if (evt.name == "Raise"){
                    break;
                }
            }
        }
    }
});