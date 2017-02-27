Canvas {

	classvar <>library;

	var <win;

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
				name: name,
				bounds: Rect(1200, 750, 400, 200),
				border: false
			);
			win.front;
			win.view.alwaysOnTop_(true);
		}
		{
			win = View(parent.view, Rect(5,5,100,100));
			win.name_(name);
		};
		this.background_(255,255,255);
	}

	view {
		case
		{ win.isKindOf(Window) } { ^win.view.findWindow }
		{ win.isKindOf(View) } { ^win };
	}

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

	printOn { |stream|	stream << this.class.name << "('" << win.name << "')"; }

	close {
		this.view.close;
		library.remove
	}
	onClose {|fnc| this.view.onClose_(fnc) }

}
