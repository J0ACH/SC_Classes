IClock {
	classvar clock;

	*initClass {
		clock = nil;
	}

	*new { ^super.new.initClock }

	initClock {
		if(clock.isNil) { clock = TempoClock.new(1) };
		"IClock.init".warn;
	}

	bpm { ^clock.tempo * 60	}
	*bpm_ {|bpm| clock.tempo = bpm/60 }
	beats { ^clock.beats }
}