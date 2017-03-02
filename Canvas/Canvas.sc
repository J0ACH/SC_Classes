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
			win.acceptsMouseOver_(true);
			view = win.asView;
		}
		{
			view = UserView(parent.view, Rect(0,0,100,100));
			view.name = name.asSymbol;
		};

		library.put(name.asSymbol, this);
		view.addAction({|view| library.removeAt(name.asSymbol) }, \onClose);

		// view.addAction({|view, x, y| "draw".warn }, \onRefresh);

		view.addAction({|view, x, y| this.onMouseDown(view.name, x, y) }, \mouseDownAction);
		// view.addAction({|view, x, y| "mouse %, %".format(x, y).postln; }, \mouseOverAction);
		view.addAction({|view| this.onEnter(view); view.refresh; ^true; }, \mouseEnterAction);
		view.addAction({|view| this.onLeave(view); view.refresh; ^true; }, \mouseLeaveAction);

	}

	close {	this.view.close }

	name { ^view.name }

	background_ {|r, g, b, a = 1| this.view.background_(Color.new255(r,g,b,a * 255)) }
	background { ^this.view.background }

	alpha_ {|a|
		case
		{ view.isKindOf(TopView) } { this.view.alpha_(a) }
		{ view.isKindOf(UserView) } {
			var color = this.background;
			this.background_(color.red * 255, color.green * 255, color.blue * 255, a);
		}
	}
	alpha {
		case
		{ view.isKindOf(TopView) } { ^this.view.alpha }
		{ view.isKindOf(UserView) } { ^this.background.alpha }
	}

	size_ {|x, y| this.view.fixedSize_(Size(x, y)) }
	size { ^this.view.bounds.size }

	origin_ {|x, y|
		var rect = Rect(x, y, this.size.width, this.size.height);
		case
		{ view.isKindOf(TopView) } { ^this.view.findWindow.bounds_(rect) }
		{ view.isKindOf(UserView) } { ^this.view.moveTo(x, y)  };
	}
	origin {
		case
		{ view.isKindOf(TopView) } { ^this.view.findWindow.bounds.origin }
		{ view.isKindOf(UserView) } { ^this.view.bounds.origin  }
	}

	// screenOrigin_ {|x, y| win.bounds.origin_(Point(x, y)); }
	// screenOrigin { ^win.bounds.origin }

	printOn { |stream|	stream << this.class.name << "('" << view.name << "')"; }

	onMouseDown {|name, x, y|
		"mouse click view % [%, %]".format(name, x, y).postln
	}

	onMouseOver {|name, x, y|
		"mouse click view % [%, %]".format(name, x, y).postln
	}

	onEnter {|view| "mouse enter Canvas('%')".format(view.name).postln; }
	onLeave {|view| "mouse leave Canvas('%')".format(view.name).postln; }

	onDraw { |fnc|
		view.drawFunc_({|view| fnc.value(view) });
		view.refresh;
		/*
		{|uview|
		Pen.strokeColor_( Color.white );
		Pen.moveTo(0@uview.bounds.height.rand);
		Pen.lineTo(uview.bounds.width@uview.bounds.height.rand);
		Pen.stroke;
		})
		*/
	}

}


