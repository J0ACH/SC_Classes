InstrumentObject {

	classvar <library;

	var key;

	*initClass {
		library = Order.new;
	}

	*new { |name|
		var iObj;
		if(name.notNil)
		{
			iObj = this.exist(name);
			if(iObj.isNil) { iObj = super.newCopyArgs(name).init };
		}
		{ iObj = super.newCopyArgs(name).init };
		^iObj;
	}

	*exist { |name|
		var iObj = this.library.at(name);
		if(iObj.notNil) { ^iObj; } { ^nil; }
	}

	*printAll { this.library.printAll; ^nil; }

	init {
		super.class.library.put(key, this);
	}

	printOn { |stream|	stream << this.class.name << "('" << key << "')"; }

}