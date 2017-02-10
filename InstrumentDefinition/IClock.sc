IClock {
	classvar clock;

	*initClass {
		clock = nil;
	}

	*new {
		if(clock.isNil) { clock = TempoClock.new(1) };
		^super.new.initClock;
	}

	initClock {
		// clock = TempoClock.new(1);
	}

	bpm { ^clock.tempo * 60	}
	*bpm_ {|bpm| clock.tempo = bpm/60 }
	beats { ^clock.beats }
}