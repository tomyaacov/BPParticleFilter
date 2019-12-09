const width = 10;
const height = 10;
const numOfSteps = 10;

walls = [{orientation:"S", x: 5, y: 7},
        {orientation:"N", x: 5, y: 8},
    {orientation:"S", x: 6, y: 7},
    {orientation:"N", x: 6, y: 8},
    {orientation:"S", x: 7, y: 7},
    {orientation:"N", x: 7, y: 8},
    {orientation:"S", x: 8, y: 7},
    {orientation:"N", x: 8, y: 8},
    {orientation:"S", x: 9, y: 7},
    {orientation:"N", x: 9, y: 8},
    {orientation:"E", x: 2, y: 9},
    {orientation:"W", x: 3, y: 9},
    {orientation:"E", x: 2, y: 8},
    {orientation:"W", x: 3, y: 8},
    {orientation:"E", x: 2, y: 7},
    {orientation:"W", x: 3, y: 7},
    {orientation:"E", x: 3, y: 0},
    {orientation:"W", x: 4, y: 0},
    {orientation:"E", x: 3, y: 1},
    {orientation:"W", x: 4, y: 1},
    {orientation:"S", x: 4, y: 1},
    {orientation:"N", x: 4, y: 2},
    {orientation:"E", x: 4, y: 2},
    {orientation:"W", x: 5, y: 2},
    {orientation:"E", x: 4, y: 3},
    {orientation:"W", x: 5, y: 3},
    {orientation:"E", x: 4, y: 4},
    {orientation:"W", x: 5, y: 4}];

Observations = bp.EventSet("Observations", function(evt) {return evt.name == "Observation"});
States = bp.EventSet("States", function(evt) {return evt.name == "State"});
Moves = bp.EventSet("Moves", function(evt) {return evt.name == "Move"});

function generateWalls(){
    for (var i = 0; i < width; i++) {
        walls.push({orientation:"N", x: i, y: 0});
        walls.push({orientation:"S", x: i, y: height-1});
    }
    for (var i = 0; i < height; i++) {
        walls.push({orientation:"W", x: 0, y: i});
        walls.push({orientation:"E", x: width-1, y: i});
    }
}

function nextState(state, move) {
    switch(move.data.orientation) {
        case "N":
            return bp.Event("State", {x:state.data.x, y:state.data.y-1});
        case "S":
            return bp.Event("State", {x:state.data.x, y:state.data.y+1});
        case "W":
            return bp.Event("State", {x:state.data.x-1, y:state.data.y});
        default:
            return bp.Event("State", {x:state.data.x+1, y:state.data.y});
    }
}

function nextObservation(state) {
    // illegalObservations = walls.filter(function(wall) {
    //     return wall.x == state.data.x && wall.y == state.data.y;
    // }).map(function (wall) {
    //     switch(wall.orientation) {
    //         case "N":
    //             return bp.Event("Observation", {x:state.data.x, y:state.data.y-1});
    //         case "S":
    //             return bp.Event("Observation", {x:state.data.x, y:state.data.y+1});
    //         case "W":
    //             return bp.Event("Observation", {x:state.data.x-1, y:state.data.y});
    //         default:
    //             return bp.Event("Observation", {x:state.data.x+1, y:state.data.y});
    //     }
    // });
    // allObservations = [bp.Event("Observation", {x:state.data.x-1, y:state.data.y}),
    //     bp.Event("Observation", {x:state.data.x+1, y:state.data.y}),
    //     bp.Event("Observation", {x:state.data.x, y:state.data.y-1}),
    //     bp.Event("Observation", {x:state.data.x, y:state.data.y+1}),
    //     bp.Event("Observation", {x:state.data.x, y:state.data.y})];
    // bp.log.info(typeof illegalObservations);
    // return allObservations.filter(x => !illegalObservations.includes(x))
    return [bp.Event("Observation", {x:state.data.x-1, y:state.data.y}),
         bp.Event("Observation", {x:state.data.x+1, y:state.data.y}),
         bp.Event("Observation", {x:state.data.x, y:state.data.y-1}),
         bp.Event("Observation", {x:state.data.x, y:state.data.y+1}),
         bp.Event("Observation", {x:state.data.x, y:state.data.y})];
}

bp.registerBThread("Interleave", function() {
    step = 0;
    while (step < numOfSteps){
        bp.sync( {waitFor:States, block:[Observations, Moves]});
        bp.sync( {waitFor:Observations, block:[Moves, States]});
        bp.sync( {waitFor:Moves, block:[Observations, States]});
        step++;
    }
    bp.sync( {block:bp.all});
});

bp.registerBThread("State", function() {
    s = bp.sync( {request:bp.Event("State", {x:0, y:0})});
    while (true){
        m = bp.sync( {waitFor:Moves});
        s = nextState(s, m);
        bp.sync( {request:s});
    }
});

bp.registerBThread("Observation", function() {
    while (true){
        s = bp.sync( {waitFor:States});
        o = nextObservation(s);
        bp.sync( {request:o});
    }
});

bp.registerBThread("Move", function() {
    while (true){
        bp.sync( {waitFor:States});
        bp.sync( {request:[bp.Event("Move", {orientation:"N"}),
                bp.Event("Move", {orientation:"S"}),
                bp.Event("Move", {orientation:"W"}),
                bp.Event("Move", {orientation:"E"})]});
    }
});

bp.registerBThread("WallsFilter", function() {
    generateWalls();
    while (true){
        s = bp.sync( {waitFor:States});
        illegalMoves = walls.filter(function(wall) {
            return wall.x == s.data.x && wall.y == s.data.y;
        }).map(function (wall) {
            return bp.Event("Move", {orientation:wall.orientation})
        });
        bp.sync( {block: illegalMoves, waitFor:Moves});
    }
});

