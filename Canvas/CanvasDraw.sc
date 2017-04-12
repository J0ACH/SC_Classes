CanvasLayer {
	var parent, name;
	// var <drawFnc;

	*new {|parent, name|
		^super.newCopyArgs(parent, name).init;
	}

	init {
		// drawFnc = nil;
	}

	draw_ {|fnc|
		// var currentDrawFnc = parent.view.drawFunc;
		// parent.view.drawFunc.postln;
		// drawFnc = fnc;
		parent.view.drawFunc = parent.view.drawFunc.addFunc({fnc.value(parent.view.bounds)});
		// parent.view.drawFunc = { fnc.value(parent.view.bounds) };
		// parent.view.drawFunc.postln;
		parent.refresh;
	}

	animate_ {|fnc, dur = 1, valFrom = 0, valTo = 1|
		// this.draw = animFnc;
		parent.view.drawFunc = parent.view.drawFunc.addFunc({fnc.value(parent.view.bounds, 0.5)});
		"anim".warn;
		// parent.view.drawFunc = { animFnc.value(parent.view.bounds) };
		SystemClock.sched(dur, { "anim End".warn; nil });
		parent.refresh;
	}


}