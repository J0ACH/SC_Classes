Canvas {

	classvar <>library;

	var <view;

	*initClass {
		library = IdentityDictionary.new;
	}

	*new { |name, parent|
		var singleton;

		if(name.notNil)
		{
			singleton = this.exist(name);
			if(singleton.isNil) { singleton = super.new.initCanvas(name, parent) };
			^singleton;
		}
		{ ^nil }
	}

	*exist { |name|
		var sigleton = this.library.at(name.asSymbol);
		if(sigleton.notNil) { ^sigleton } { ^nil; }
	}

	*printAll { this.library.postTree; ^nil; }

	initCanvas {|name, parent|
		if(parent.isNil)
		{
			var win = Window(
				name: name.asSymbol,
				bounds: Rect(1200, 750, 400, 200),
				border: false
			);
			win.front;
			win.view.alwaysOnTop_(true);
			view = win.asView;
		}
		{
			view = UserView(parent.view, Rect(0,0,100,100));
			view.name = name.asSymbol;
		};

		library.put(name.asSymbol, this);
		view.addAction({|view| library.removeAt(name.asSymbol) }, \onClose);


		view.addAction({|view, x, y| this.onMouseDown(view.name, x, y) }, \mouseDownAction);

	}

	close {	this.view.close }

	name { ^view.name }


	background_ {|r, g, b, a = 1| this.view.background_(Color.new255(r,g,b,a * 255)) }
	/*
	background { ^this.view.background }
	alpha_ {|a|
	case
	{ win.isKindOf(Window) } { win.alpha_(a) }
	{ win.isKindOf(View) } {
	var color = this.background;
	this.background_(color.red * 255, color.green * 255, color.blue * 255, a);
	}
	}
	// alpha { ^this.view.alpha }

	size_ {|x, y| this.view.fixedSize_(Size(x, y)) }
	size { ^this.view.bounds.size }

	origin_ {|x, y|	this.view.bounds_(Rect(x, y, this.size.width, this.size.height)) }
	origin { ^this.view.bounds.origin }
	*/

	// screenOrigin_ {|x, y| win.bounds.origin_(Point(x, y)); }
	// screenOrigin { ^win.bounds.origin }

	printOn { |stream|	stream << this.class.name << "('" << view.name << "')"; }

	onMouseDown {|name, x, y|
		"mouse click view % [%, %]".format(name, x, y).postln
	}

}
