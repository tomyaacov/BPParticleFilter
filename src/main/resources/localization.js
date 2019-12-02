const width = 5;
const height = 5;
const steps = 100;
var x;
var y;

bp.registerBThread("ObservationMain", function() {
    for (var i = 0; i < steps; i++){
        bp.sync( {waitFor:bp.all});
        bp.sync( {request:[bp.Event("Observation", {x:x-1, y:y}),
                bp.Event("Observation", {x:x+1, y:y}),
                bp.Event("Observation", {x:x, y:y-1}),
                bp.Event("Observation", {x:x, y:y+1}),
                bp.Event("Observation", {x:x, y:y})]});
    }

});

bp.registerBThread("StateMain", function() {
    x = bp.random.nextInt(width);
    y = bp.random.nextInt(height);
    for (var i = 0; i < steps; i++) {
        e = bp.sync({
            request: [bp.Event("State", {x: x - 1, y: y}),
                bp.Event("State", {x: x + 1, y: y}),
                bp.Event("State", {x: x, y: y - 1}),
                bp.Event("State", {x: x, y: y + 1})]
        });
        x = e.data.x;
        y = e.data.y;
        bp.sync({waitFor: bp.all});
    }
});

bp.registerBThread("eastWall", function() {
    var eastWallEventSet = bp.EventSet("eastWallEventSet", function(evt) {
        return evt.data.x < 0
    });
    while(true){
        bp.sync({block: eastWallEventSet});
    }
});

bp.registerBThread("westWall", function() {
    var westWallEventSet = bp.EventSet("westWallEventSet", function(evt) {
        return evt.data.x >= width
    });
    while(true){
        bp.sync({block: westWallEventSet});
    }
});

bp.registerBThread("southWall", function() {
    var southWallEventSet = bp.EventSet("southWallEventSet", function(evt) {
        return evt.data.y < 0
    });
    while(true){
        bp.sync({block: southWallEventSet});
    }
});

bp.registerBThread("northWall", function() {
    var northWallEventSet = bp.EventSet("northWallEventSet", function(evt) {
        return evt.data.y >= height
    });
    while(true){
        bp.sync({block: northWallEventSet});
    }
});