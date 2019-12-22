importPackage(java.util);
const width = 20;
const height = 20;
const numOfSteps = 100;
const observation_std = 2;

const Observations = bp.EventSet("Observations", function(evt) {return evt.name == "Observation"});
const States = bp.EventSet("States", function(evt) {return evt.name == "State"});
const Moves = bp.EventSet("Moves", function(evt) {return evt.name == "Move"});


const map =
    [
        "   |       |           |       |       ",
        "                                       ",
        "   |       |           |       |       ",
        "    -                   -              ",
        "   |       |           |       |       ",
        "        - -                 - -        ",
        "           |                   |       ",
        "                                       ",
        "           |                   |       ",
        "- - -               - - -              ",
        "                                       ",
        "                                       ",
        "       |                   |           ",
        "        - - - -             - - - -    ",
        "       |                   |           ",
        "                                       ",
        "       |                   |           ",
        "                                       ",
        "               |                   |   ",
        "                                       ",
        "   |       |           |       |       ",
        "                                       ",
        "   |       |           |       |       ",
        "    -                   -              ",
        "   |       |           |       |       ",
        "        - -                 - -        ",
        "           |                   |       ",
        "                                       ",
        "           |                   |       ",
        "- - -               - - -              ",
        "                                       ",
        "                                       ",
        "       |                   |           ",
        "        - - - -             - - - -    ",
        "       |                   |           ",
        "                                       ",
        "       |                   |           ",
        "                                       ",
        "               |                   |   ",

    ];

const walls = generateWalls();

function generateInnerWalls(){
    var ans = [];
    for (var i = 0; i < map.length; i++) {
        for (var j = 0; j < map[i].length; j++) {
            if (map[i].charAt(j) == "|"){
                ans.push({orientation:"E", x: (j-1)/2, y: i/2});
                ans.push({orientation:"W", x: (j+1)/2, y: i/2});
            }
            if (map[i].charAt(j) == "-"){
                ans.push({orientation:"S", x: j/2, y: (i-1)/2});
                ans.push({orientation:"N", x: j/2, y: (i+1)/2});
            }
        }
    }
    return ans;
}

function generateWalls(){
    var ans = generateInnerWalls();
    for (var i = 0; i < width; i++) {
        ans.push({orientation:"N", x: i, y: 0});
        ans.push({orientation:"S", x: i, y: height-1});
    }
    for (var i = 0; i < height; i++) {
        ans.push({orientation:"W", x: 0, y: i});
        ans.push({orientation:"E", x: width-1, y: i});
    }
    return ans;
}

function nextGaussian() {
    var initial_val = new Random().nextGaussian() * observation_std;
    return Math.round(initial_val);
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
    var dx = nextGaussian();
    var dy = nextGaussian();
    return bp.Event("Observation", {x:state.x+dx, y:state.y+dy});
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
    var s = bp.sync( {request:bp.Event("State", {x:new Random().nextInt(width), y:new Random().nextInt(height)})});
    while (true){
        var m = bp.sync( {waitFor:Moves});
        s = nextState(s, m);
        bp.sync( {request:s});
    }
});

bp.registerBThread("Observation", function() {
    while (true){
        var s = bp.sync( {waitFor:States}).data;
        o = nextObservation(s);
        bp.sync( {request:o});
    }
});

bp.registerBThread("Move", function() {
    while (true){
        bp.sync( {request:[bp.Event("Move", {orientation:"N"}),
                bp.Event("Move", {orientation:"S"}),
                bp.Event("Move", {orientation:"W"}),
                bp.Event("Move", {orientation:"E"})]});
    }
});

bp.registerBThread("WallsFilter", function() {
    while (true){
        var s = bp.sync( {waitFor:States});
        var illegalMoves = walls.filter(function(wall) {
            return wall.x == s.data.x && wall.y == s.data.y;
        }).map(function (wall) {
            return bp.Event("Move", {orientation:wall.orientation})
        });
        bp.sync( {block: illegalMoves, waitFor:Moves});
    }
});

