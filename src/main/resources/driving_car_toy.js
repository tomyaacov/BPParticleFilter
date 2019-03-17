width = 9;
//malfunctionProbability = 0.5;

var car1EventSetReal = bp.EventSet('', function(e) {
    return (e.data.id == 1) && (e.data.x > -1);
});
var car2EventSetReal = bp.EventSet('', function(e) {
    return (e.data.id == 2) && (e.data.x > -1);
});
var car3EventSetReal = bp.EventSet('', function(e) {
    return (e.data.id == 3) && (e.data.x > -1);
});
var car4EventSetReal = bp.EventSet('', function(e) {
    return (e.data.id == 4) && (e.data.x > -1);
});
var car1EventSetAll = bp.EventSet('', function(e) {
    return (e.data.id == 1);
});
var car2EventSetAll = bp.EventSet('', function(e) {
    return (e.data.id == 2);
});
var car3EventSetAll = bp.EventSet('', function(e) {
    return (e.data.id == 3);
});
var car4EventSetAll = bp.EventSet('', function(e) {
    return (e.data.id == 4);
});

bp.registerBThread("car1Main", function() {
    var i;
    for (i = 0; i < width; i++) {
        bp.sync( {request:bp.Event("Recognized", {id:1, x:i, y:1}), waitFor: car1EventSetAll} );
    }
});

bp.registerBThread("car1Malfunction", function() {
    var i;
    for (i = 0; i < 2; i++) {
        bp.sync( {waitFor:car1EventSetReal} );
    }
    if (bp.random.nextFloat() < 0) {
        bp.sync( {request: bp.Event("Recognized", {id:1, x:-1, y:1}), block:car1EventSetReal} );
    } else {
        bp.sync( {waitFor: car1EventSetReal} );
    }
    for (i = 3; i < width; i++) {
        bp.sync( {waitFor:car1EventSetReal} );
    }
});

bp.registerBThread("car2Main", function() {
    var i;
    for (i = 0; i < width; i++) {
        bp.sync( {request:bp.Event("Recognized", {id:2, x:i, y:2}), waitFor: car2EventSetAll} );
    }
});

bp.registerBThread("car2Malfunction", function() {
    var i;
    for (i = 0; i < 2; i++) {
        bp.sync( {waitFor:car2EventSetReal} );
    }
    if (bp.random.nextFloat() < 0) {
        bp.sync( {request: bp.Event("Recognized", {id:2, x:-1, y:2}), block:car2EventSetReal} );
    } else {
        bp.sync( {waitFor: car2EventSetReal} );
    }
    for (i = 3; i < width; i++) {
        bp.sync( {waitFor:car2EventSetReal} );
    }
});

bp.registerBThread("car3Main", function() {
    var i;
    for (i = 0; i < width; i++) {
        bp.sync( {request:bp.Event("Recognized", {id:3, x:i, y:3}), waitFor: car3EventSetAll} );
    }
});

bp.registerBThread("car3Malfunction", function() {
    var i;
    for (i = 0; i < 2; i++) {
        bp.sync( {waitFor:car3EventSetReal} );
    }
    if (bp.random.nextFloat() < 0) {
        bp.sync( {request: bp.Event("Recognized", {id:3, x:-1, y:3}), block:car3EventSetReal} );
    } else {
        bp.sync( {waitFor: car3EventSetReal} );
    }
    for (i = 3; i < width; i++) {
        bp.sync( {waitFor:car3EventSetReal} );
    }
});



bp.registerBThread("car4Main", function() {
    var i;
    for (i = 0; i < width; i++) {
        bp.sync( {request:bp.Event("Recognized", {id:4, x:i, y:4}), waitFor: car4EventSetAll} );
    }
});

bp.registerBThread("car4Malfunction", function() {
    var i;
    for (i = 0; i < 2; i++) {
        bp.sync( {waitFor:car4EventSetReal} );
    }
    if (bp.random.nextFloat() < 0) {
        bp.sync( {request: bp.Event("Recognized", {id:4, x:-1, y:4}), block:car4EventSetReal} );
    } else {
        bp.sync( {waitFor: car4EventSetReal} );
    }
    for (i = 3; i < width; i++) {
        bp.sync( {waitFor:car4EventSetReal} );
    }
});
