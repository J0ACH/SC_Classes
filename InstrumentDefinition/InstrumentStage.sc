InstrumentStage : InstrumentObject {

	classvar <stages;
	var key;

	*new { |name|
		var iDef;
		if(name.notNil)
		{
			iDef = this.exist(name);
			if(iDef.isNil) { iDef = super.newCopyArgs(name).initStage };
		}
		{ iDef = super.newCopyArgs(name).initStage };
		^iDef;
	}

	initStage {
		"initStage %".format(key).warn;
	}

}