Canvas {

	var canvasParent, canvasView;
	var <config;

	*new { |x, y, w, h, parent = nil, name = nil| ^super.new.initCanvas(x, y, w, h, parent, name).init }

	initCanvas {|x, y, w, h, parent, name|

		canvasParent = parent;

		config = MultiLevelIdentityDictionary.new();

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
			// canvasView = win.asView;

			canvasView = UserView(win, Rect(0, 0, w, h));
			canvasView.name = "Canvas_WinView";

			canvasView.addAction({|v| win.close; }, \onClose);
		}
		{
			canvasView = UserView(parent.view, Rect(x, y, w, h));
			canvasView.name = name.asSymbol;
		};

		// colorTable = IdentityDictionary.new();
		this.initConfig;

		canvasView.drawingEnabled = true;
		this.background = Color.new255(90,90,90);
		this.color255_(\background, 90,90,90);
		// canvasView.background = this.color(\background);

		// canvasView.addAction({|v| library.removeAt(name.asSymbol) }, \onClose);
		// view.addAction({|view, x, y| "draw".warn }, \onRefresh);

		canvasView.addAction({|v, x, y|
			var coorScreen = QtGUI.cursorPosition;
			this.onMouseDown(this, x, y, coorScreen.x, coorScreen.y);
		}, \mouseDownAction);

		canvasView.addAction({|v, x, y|
			var coorScreen = QtGUI.cursorPosition;
			this.onMouseUp(this, x, y, coorScreen.x, coorScreen.y);
		}, \mouseUpAction);

		canvasView.addAction({|v, x, y, modifer|
			var coorScreen = QtGUI.cursorPosition;
			this.onMouseMove(this, x, y, coorScreen.x, coorScreen.y, modifer)
		}, \mouseMoveAction);

		// view.addAction({|view, x, y| "mouse %, %".format(x, y).postln; }, \mouseOverAction);
		canvasView.addAction({|v| this.onEnter(this); v.refresh; }, \mouseEnterAction);
		canvasView.addAction({|v| this.onLeave(this); v.refresh; }, \mouseLeaveAction);

		canvasView.addAction({|v| this.onResize(this) }, \onResize);

		canvasView.drawFunc_({|v|
			var rect = Rect(0,0, this.width, this.height);
			Pen.fillColor_( this.background );
			Pen.fillRect(rect);
		});

	}

	init { }

	initConfig {
		config.put(\color, \back, Color.new255(30,130,30));
	}

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

	config_ { |canvasConfig|

	}

	background_ {|color| config.put(\color, \back, color) }
	background { ^config.at(\color, \back) }
	// backgroundRGB_ {|r, g, b, a = 1| canvasView.background_(Color.new255(r,g,b,a * 255)) }

	color255_ { |name, r, g, b, a = 1| /*this.color_(name.asSymbol, Color.new255(r,g,b,a * 255)); */ }
	// color_ { |name, color| colorTable.put(name.asSymbol, color); this.refresh;}
	// color { |name| ^colorTable.at(name) }

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

	size_ {|x, y|
		if(canvasParent.isNil) { canvasView.parent.setProperty(\size, Size(x, y)) };
		canvasView.fixedSize_(Size(x, y));
	}
	size { ^canvasView.bounds.size }

	width_ {|x| this.size_(x, this.height) }
	width { ^canvasView.bounds.size.width }

	height_ {|y| this.size_(this.width, y) }
	height { ^canvasView.bounds.size.height }

	origin_ {|x, y|
		var rect = Rect(x, y, this.size.width, this.size.height);
		case
		{ canvasView.isKindOf(TopView) } { ^canvasView.findWindow.bounds_(Window.flipY(rect)) }
		{ canvasView.isKindOf(UserView) } { ^canvasView.moveTo(x, y)  };
	}
	origin {
		if(canvasView.isClosed) {^nil};
		if(canvasParent.isNil)
		{ ^this.screenOrigin }
		{ ^canvasView.bounds.origin }
	}

	originX_ {|x| this.origin_(x, this.origin.y) }
	originX { ^this.origin.x }

	originY_ {|y| this.origin_(this.origin.x, y) }
	originY { ^this.origin.y }

	screenOrigin_ {|x, y|
		var nextParent = this.parent;
		var newOrigin = Point(x, y);

		while ( { nextParent != nil }, {
			newOrigin.x = newOrigin.x - nextParent.screenOrigin.x;
			newOrigin.y = newOrigin.y - nextParent.screenOrigin.y;
			nextParent = nextParent.parent;
		});

		if(canvasParent.isNil)
		{ canvasView.parent.setProperty(\geometry, Rect(x, y, this.width, this.height)) }
		{ this.origin_(newOrigin.x, newOrigin.y) }
	}
	screenOrigin { ^canvasView.mapToGlobal(Point(0,0)) }

	screenOriginX_ {|x|	this.screenOrigin_(x, this.screenOrigin.y) }
	screenOriginX {	^this.screenOrigin.x }

	screenOriginY_ {|y|	this.screenOrigin_(this.screenOrigin.x, y) }
	screenOriginY {	^this.screenOrigin.y }

	printOn { |stream|	stream << this.class.name << "('" << canvasView.name << "')"; }

	onMouseDown {|canvas, x, y, screenX, screenY|
		// var screenMouseDown = Point(this.screenOrigin.x + x, this.screenOrigin.y + y);
		// "%.onMouseDown [%, %]".format(canvas, screenMouseDown.x, screenMouseDown.y).postln;
	}

	onMouseUp {|canvas, x, y, screenX, screenY|
		// "%.onMouseUp [%, %]".format(canvas, x, y).postln
	}

	onMouseMove {|canvas, x, y, screenX, screenY, modifer|
		// "%.onMouseOver [x:%, y:%, mod:%]".format(canvas, x, y, modifer).postln;
	}

	onMouseOver {|canvas, x, y|
		// "%.onMouseOver [%, %]".format(canvas, x, y).postln
	}

	onEnter {|canvas|
		// "%.onEnter".format(canvas).postln;
	}
	onLeave {|canvas|
		// "%.onLeave".format(canvas).postln;
	}

	onResize {|canvas|
		// "%.onResize [w:%, h:%]".format(canvas, canvas.width, canvas.height).postln;
	}

	draw { |fnc|
		canvasView.drawFunc_({|view|
			var rect = Rect(0,0, this.width, this.height);
			// var rect = Rect(0,0, 100, 100);
			Pen.fillColor_( this.background );
			// Pen.fillColor_( Color.green );
			Pen.fillRect(rect);
			fnc.value(view);
		});
		canvasView.refresh;
	}
	/*
	onDraw {
	// "view onDraw".postln
	}
	*/

	refresh { canvasView.refresh }
}


