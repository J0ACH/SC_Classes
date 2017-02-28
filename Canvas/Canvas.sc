Canvas {

	classvar <>library;

	var <win, <view;

	*initClass {
		library = IdentityDictionary.new;
	}

	*new { |name, parent|
		var singleton;

		if(name.notNil)
		{
			singleton = this.exist(name);
			if(singleton.isNil) {
				singleton = super.new.initCanvas(name, parent);
				library.put(name, singleton);
			};
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
			win = Window(
				name: "win_%".format(name),
				bounds: Rect(1200, 750, 400, 200),
				border: false
			);
			win.front;
			win.view.alwaysOnTop_(true);

			view = win.view;
			view.name = name;
		}
		{
			win = nil;
			view = View(parent.view, Rect(5,5,100,100));
			view.name = name;
			// view.postln;
		};
		// };
		// this.background_(255,255,255);
	}

	/*
	view {
		case
		{ win.isKindOf(Window) } { ^win.view.findWindow }
		{ win.isKindOf(View) } { ^win };
	}
	*/

	name { ^view.name }

	background_ {|r, g, b, a = 1| this.view.background_(Color.new255(r,g,b,a * 255)) }
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

	// screenOrigin_ {|x, y| win.bounds.origin_(Point(x, y)); }
	// screenOrigin { ^win.bounds.origin }

	printOn { |stream|	stream << this.class.name << "('" << view.name << "')"; }

	close {
		// "close %".format(this.name).postln;
		// this.view.children.postln;
		this.view.children.do({|oneChild|
			oneChild.close;
			library.removeAt(oneChild.name.asSymbol);
			// oneChild.changed(\close, this.name);
		});
		library.removeAt(this.name.asSymbol);
		this.view.close;
		// this.changed(\close, this.name); // object dependency update call
	}
	onClose {|fnc| this.view.onClose_(fnc) }

	onMouseButton {|fnc|
		view.acceptsMouse = true;
		view.mouseDownAction = fnc;
	}



}
