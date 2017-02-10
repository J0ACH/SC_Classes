Idef2 : IStage {
	classvar <library;

	var <key;

	*initClass {
		library = IdentityDictionary.new();
	}

	*new {|name|
		// var instrStage = stage ? \newDefault;
		var iDef = this.exist(name);
		if(iDef.isNil) { iDef = super.new(\default).init(name) };
		^iDef;

		// ^super.new(instrStage).init(name);
	}

	*exist { |name| if(library.at(name).notNil) { ^library.at(name) } { ^nil } }

	init {|name|
		// super.init;
		key = name;
		library.put(name.asSymbol, this);
		"IDef2.init".warn;
	}

	stage { ^super.stage }
	stage_ {|name| super.stage = name }

	printOn { |stream|	stream << this.class.name << "('" << key << "')"; }
}