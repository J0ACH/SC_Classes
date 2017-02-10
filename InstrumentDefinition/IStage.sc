IStage : IClock {
	classvar currentStage;
	classvar <stages;

	var <>stage;

	*initClass {
		currentStage = \default;
		stages = IdentityDictionary.new();
	}

	*new {|name|
		var iStage = this.exist(name);
		if(iStage.isNil) { iStage = super.new.initStage(name) };
		^iStage;
	}

	*exist { |name| if(stages.at(name).notNil) { ^stages.at(name) } { ^nil } }

	*printAll { stages.printAll }

	initStage { |name|
		stage = name;
		stages.put(name.asSymbol, this);
		"IStage.init %".format(stage).warn;
	}

	// stage { ^currentStage }
	// *stage_ {|name| currentStage = name }

	printOn { |stream|	stream << this.class.name << "('" << stage << "')"; }

}