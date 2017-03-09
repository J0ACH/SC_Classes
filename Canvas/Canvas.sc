Canvas {

	classvar <>library;

	// var <view;
	var canvasParent, canvasView;

	*initClass {
		library = IdentityDictionary.new;
	}

	*new { |x, y, w, h, parent = nil, name = nil|
		// ^super.new.initCanvas(x, y, w, h, parent, name).init;
		// /*
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
		// */
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
			canvasView = win.asView;
		}
		{
			canvasView = UserView(parent.view, Rect(x, y, w, h));
			canvasView.name = name.asSymbol;
		};

		this.background_(90,90,90);

		canvasView.addAction({|v| library.removeAt(name.asSymbol) }, \onClose);

		// view.addAction({|view, x, y| "draw".warn }, \onRefresh);

		canvasView.addAction({|v, x, y| this.onMouseDown(this, x, y) }, \mouseDownAction);
		canvasView.addAction({|v, x, y| this.onMouseUp(this, x, y) }, \mouseUpAction);
		// view.addAction({|view, x, y| "mouse %, %".format(x, y).postln; }, \mouseOverAction);
		canvasView.addAction({|v| this.onEnter(this); v.refresh; }, \mouseEnterAction);
		canvasView.addAction({|v| this.onLeave(this); v.refresh; }, \mouseLeaveAction);

		canvasView.addAction({|v, x, y, modifer| this.onMouseMove(this, x, y, modifer) }, \mouseMoveAction);

		canvasView.addAction({|v| this.onResize(this) }, \onResize);

	}

	init { }

	parent_ { |parent|
		if(parent.notNil)
		{
			canvasParent = parent;
			canvasView.setParent(parent.view);
			canvasView.front;
		}
	}
	parent { ^canvasParent }

	view { if(canvasView.isClosed) { ^nil } { ^canvasView } }
	view_ {|newVal| canvasView = newVal }

	close {	canvasView.close }

	name_ { |txt| canvasView.name_(txt) }
	name { ^canvasView.name  }

	background_ {|r, g, b, a = 1| canvasView.background_(Color.new255(r,g,b,a * 255)) }
	background { ^canvasView.background }

	alpha_ {|a|
		case
		{ canvasView.isKindOf(TopView) } { canvasView.alpha_(a) }
		{ canvasView.isKindOf(UserView) } {
			var color = this.background;
			this.background_(color.red * 255, color.green * 255, color.blue * 255, a);
		}
	}
	alpha {
		case
		{ canvasView.isKindOf(TopView) } { ^canvasView.alpha }
		{ canvasView.isKindOf(UserView) } { ^this.background.alpha }
	}

	size_ {|x, y| canvasView.fixedSize_(Size(x, y)) }
	size { ^canvasView.bounds.size }

	width_ {|x| canvasView.fixedWidth_(x) }
	width { ^canvasView.bounds.size.width }

	height_ {|y| canvasView.fixedHeight_(y) }
	height { ^canvasView.bounds.size.height }

	origin_ {|x, y|
		var rect = Rect(x, y, this.size.width, this.size.height);
		case
		{ canvasView.isKindOf(TopView) } { ^canvasView.findWindow.bounds_(Window.flipY(rect)) }
		{ canvasView.isKindOf(UserView) } { ^canvasView.moveTo(x, y)  };
	}
	origin {
		if(canvasView.isClosed) {^nil};
		case
		{ canvasView.isKindOf(TopView) } { ^canvasView.findWindow.bounds.origin }
		{ canvasView.isKindOf(UserView) } { ^canvasView.bounds.origin  }
	}

	originX_ {|x| this.origin_(x, this.origin.y) }
	originX { ^this.origin.x }

	originY_ {|y| this.origin_(this.origin.x, y) }
	originY { ^this.origin.y }

	// screenOrigin_ {|x, y| win.bounds.origin_(Point(x, y)); }
	screenOrigin {  ^canvasView.mapToGlobal(Point(0,0)) }

	printOn { |stream|	stream << this.class.name << "('" << canvasView.name << "')"; }

	onMouseDown {|canvas, x, y|
		"%.onMouseDown [%, %]".format(canvas, x, y).postln
	}

	onMouseUp {|canvas, x, y|
		"%.onMouseUp [%, %]".format(canvas, x, y).postln
	}

	onMouseOver {|canvas, x, y|
		"%.onMouseOver [%, %]".format(canvas, x, y).postln
	}

	onEnter {|canvas| "%.onEnter".format(canvas).postln; }
	onLeave {|canvas| "%.onLeave".format(canvas).postln; }

	onMouseMove {|canvas, x, y, modifer|
		// "%.onMouseOver [x:%, y:%, mod:%]".format(canvas, x, y, modifer).postln;
	}

	onResize {|canvas|
		// "%.onResize [w:%, h:%]".format(canvas, canvas.width, canvas.height).postln;
	}

	draw { |fnc|
		canvasView.drawFunc_({|view|
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


