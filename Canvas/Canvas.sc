Canvas {

	classvar <>library;

	var <view, canvasParent;

	*initClass {
		library = IdentityDictionary.new;
	}

	*new { |x, y, w, h, parent = nil, name = nil|
		var instance = this.exist(name);
		if ( instance.isNil )
		{ instance = super.new.initCanvas(x, y, w, h, parent, name).init }
		{
			instance.origin_(x, y);
			instance.size_(w, h);
			instance.parent_(parent);
		};
		if ( name.notNil ) { library.put(name.asSymbol, instance) }
		^instance;
	}

	*exist { |name|
		var sigleton = this.library.at(name.asSymbol);
		if(sigleton.notNil) { ^sigleton } { ^nil; }
	}

	*printAll { this.library.postTree; ^nil; }

	initCanvas {|x, y, w, h, parent, name|

		canvasParent = parent;

		if(parent.isNil)
		{
			var win = Window(
				name: name.asSymbol,
				bounds: Window.flipY(Rect(x, y, w, h)),
				border: false
			);
			win.front;
			win.view.alwaysOnTop_(true);
			win.acceptsMouseOver_(true);
			view = win.asView;
		}
		{
			view = UserView(parent.view, Rect(x, y, w, h));
			view.name = name.asSymbol;
		};


		view.addAction({|view| library.removeAt(name.asSymbol) }, \onClose);

		// view.addAction({|view, x, y| "draw".warn }, \onRefresh);

		view.addAction({|view, x, y| this.onMouseDown(view.name, x, y) }, \mouseDownAction);
		view.addAction({|view, x, y| this.onMouseUp(view.name, x, y) }, \mouseUpAction);
		// view.addAction({|view, x, y| "mouse %, %".format(x, y).postln; }, \mouseOverAction);
		view.addAction({|view| this.onEnter(view); view.refresh; }, \mouseEnterAction);
		view.addAction({|view| this.onLeave(view); view.refresh; }, \mouseLeaveAction);

		view.addAction({|view, x, y, modifer| this.onMouseMove(view.name, x, y, modifer) }, \mouseMoveAction);
		// view.addAction({|view| this.onResize(view); view.refresh; }, \mouseLeaveAction);
	}

	init { }
	close {	this.view.close }

	name_ { |txt| view.name_(txt) }
	name { ^view.name }

	parent_ { |parent|
		if(parent.notNil)
		{
			canvasParent = parent;
			view.setParent(parent.view);
			view.front;
		}
	}
	parent { ^this.view.parent }
	// parent { ^canvasParent }

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

	// width_ {|x, y| this.view.fixedSize_(Size(x, y)) }
	width { ^this.view.bounds.size.width }

	// height_ {|x, y| this.view.fixedSize_(Size(x, y)) }
	height { ^this.view.bounds.size.height }

	origin_ {|x, y|
		var rect = Rect(x, y, this.size.width, this.size.height);
		case
		{ view.isKindOf(TopView) } { ^this.view.findWindow.bounds_(Window.flipY(rect)) }
		{ view.isKindOf(UserView) } { ^this.view.moveTo(x, y)  };
	}
	origin {
		case
		{ view.isKindOf(TopView) } { ^this.view.findWindow.bounds.origin }
		{ view.isKindOf(UserView) } { ^this.view.bounds.origin  }
	}

	// screenOrigin_ {|x, y| win.bounds.origin_(Point(x, y)); }
	screenOrigin {  ^view.mapToGlobal(Point(0,0)) }

	printOn { |stream|	stream << this.class.name << "('" << view.name << "')"; }

	onMouseDown {|name, x, y|
		"mouse click view % [%, %]".format(name, x, y).postln
	}

	onMouseUp {|name, x, y|
		"mouse up view % [%, %]".format(name, x, y).postln
	}

	onMouseOver {|name, x, y|
		"mouse click view % [%, %]".format(name, x, y).postln
	}

	onEnter {|view| "mouse enter Canvas('%')".format(view.name).postln; }
	onLeave {|view| "mouse leave Canvas('%')".format(view.name).postln; }

	onMouseMove {|view, x, y, modifer|
		"mouse move Canvas('%') [x:%,y:%, mod:%]".format(view, x, y, modifer).postln;
	}


	draw { |fnc|
		view.drawFunc_({|view|
			fnc.value(view);
			// this.onDraw;
			// view.refresh;
		});
		/*
		{|uview|
		Pen.strokeColor_( Color.white );
		Pen.moveTo(0@uview.bounds.height.rand);
		Pen.lineTo(uview.bounds.width@uview.bounds.height.rand);
		Pen.stroke;
		})
		*/
	}
	/*
	onDraw {
	// "view onDraw".postln
	}
	*/

}


