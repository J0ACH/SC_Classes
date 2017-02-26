Canvas {


	classvar <>library;

	var win;

	*initClass {
		library = IdentityDictionary.new;
	}

	*new { |name = nil|
		var singleton;

		if(name.notNil)
		{
			singleton = this.exist(name);
			if(singleton.isNil) {
				singleton = super.new.initCanvas(name);
				library.put(name, singleton);
			};
		}
		^singleton;
	}

	*exist { |name|
		var sigleton = this.library.at(name.asSymbol);
		if(sigleton.notNil) { ^sigleton } { ^nil; }
	}

	*printAll { this.library.postTree; ^nil; }

	initCanvas {|name|
		win = Window(
			name: name,
			bounds: Rect(1200, 750, 400, 200),
			border: false
		);
		// win = UserView(nil, bounds: Rect(1200, 750, 400, 200));
		// win.alpha_(0.85);
		// win.name_(name);
		win.front;
		win.view.alwaysOnTop_(true);
		this.background_(60,30,30);
		// win.view..postln;
		// win.refresh;
	}

	background_ {|r, g, b| win.view.background_(Color.new255(r,g,b)) }
	background { ^win.view.background }

	printOn { |stream|	stream << this.class.name << "('" << win.name << "')"; }

}
