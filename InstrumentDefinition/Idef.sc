Idef {
	classvar <library;
	classvar <>stage;

	var key, stage;
	var busAudio, busVolume;

	*initClass {
		library = MultiLevelIdentityDictionary.new;
		stage = \default;
	}

	*new { |name|
		var iDef;
		if(name.notNil)
		{
			iDef = this.exist(name);
			if(iDef.isNil) { iDef = super.newCopyArgs(name).initInstrument };
		}
		{ iDef = super.newCopyArgs(name).initInstrument };
		^iDef;
	}

	*exist { |name|
		var path = [name, stage];
		var iDef = this.library.atPath(path);
		if(iDef.notNil) { ^iDef; } { ^nil; }
	}

	*printAll { this.library.postTree; ^nil; }

	initInstrument {
		var path = [key, stage];
		super.class.library.putAtPath(path, this);
		this.initBuses;
		// "initInstrument".warn;
	}

	free {
		busAudio.free;
		busVolume.free;
	}

	initBuses {
		if(Server.default.serverRunning.not)
		{ Server.default.onBootAdd({ this.initBuses }) }
		{
			busAudio = Bus.audio(Server.default, 2);
			busVolume = Bus.control(Server.default, 1);
			"\t- Idef(%) alloc audio bus at index %".format(key, busAudio.index).postln;
		}
	}

	printOn { |stream|	stream << this.class.name << "('" << key << "')"; }
}