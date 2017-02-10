IStage : IClock {
	classvar currentStage;
	classvar <library;

	var stage;

	*initClass {
		currentStage = \default;
		library = Order.new();
	}

	*new {|name|
		// ^super.new.initStage(name);

		var iStage;
		if(name.notNil)
		{
			iStage = this.exist(name);
			if(iStage.isNil) { iStage = super.new.initStage(name) };
		}
		{ iStage = super.new.initStage(name) };
		^iStage;
	}

	*exist { |name|
		if(this.library.at(name)) { ^this; } { ^nil; }
	}

	initStage { |name|
		// stage = name;
		// var path = [key, stage];
		super.class.library.put(name, this);
	}

	stage { ^currentStage }
	*stage_ {|name| currentStage = name }

}