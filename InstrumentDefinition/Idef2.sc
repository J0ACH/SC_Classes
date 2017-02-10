Idef2 : IStage {
	var key;

	*new {|name|
		^super.new.init(name);
	}

	init {|name|
		key = name;
	}

	printOn { |stream|	stream << this.class.name << "('" << key << "')"; }
}