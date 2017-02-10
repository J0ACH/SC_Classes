Idef {
	classvar <library;
	classvar <>stage;

	var key;

	*initClass {
		library = MultiLevelIdentityDictionary.new;
		stage = \default;
	}

	*new { |name|
		var iDef;
		if(name.notNil)
		{
			iDef = this.exist(name);
			if(iDef.isNil) { iDef = super.newCopyArgs(name).libraryStore };
		}
		{ iDef = super.newCopyArgs(name).libraryStore };
		^iDef;
	}

	*exist { |name|
		var path = [name, stage];
		var iDef = this.library.atPath(path);
		if(iDef.notNil) { ^iDef; } { ^nil; }
	}

	*printAll { this.library.postTree; ^nil; }

	libraryStore {
		var path = [key, stage];
		super.class.library.putAtPath(path, this);
	}

	printOn { |stream|	stream << this.class.name << "('" << key << ")"; }
}