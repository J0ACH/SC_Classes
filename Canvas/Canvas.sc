Canvas {

	var canvasParent, canvasView;

	var >resizeParentAction;
	var colorBackground, colorFrame, isBackgroundVisible, isFrameVisible;

	*initClass {
		// "canvas init".warn;
		CanvasConfig.addColor(this, \background, Color.new255(20,20,20));
		CanvasConfig.addColor(this, \frame, Color.new255(120,120,120));

		Document.globalKeyDownAction = {|v, char, modifiers, unicode, keycode|
			case
			{ modifiers == 262144 && keycode == 192 } { "Canvas Document.globalKeyDownAction".warn }; // Ctrl + "~"
			// "% ,% ,% ,% ,%".format(v, char, modifiers, unicode, keycode).postln;
		};
	}

	*new { |x, y, w, h, parent = nil, name = nil| ^super.new.initCanvas(x, y, w, h, parent, name).init }

	initCanvas {|x, y, w, h, parent, name|

		canvasParent = parent;

		colorBackground = CanvasConfig.getColor(this, \background);
		colorFrame = CanvasConfig.getColor(this, \frame);
		isBackgroundVisible = false;
		isFrameVisible = false;

		resizeParentAction = nil;

		if(parent.isNil)
		{
			var win = Window(
				name: name.asSymbol,
				bounds: Window.flipY(Rect(x, y, w, h)),
				border: false
			).front.view.alwaysOnTop_(true).acceptsMouseOver_(true);

			// var scroll = ScrollView(win,Rect(0, 0, w, h)).hasBorder_(false);


			// canvasView = UserView(scroll, Rect(0, 0, w, 1000));
			canvasView = UserView(win, Rect(0, 0, w, h));


			canvasView.name = "Canvas_WinView";

			canvasView.addAction({|v| win.close; }, \onClose);
		}
		{
			canvasView = UserView(parent.view, Rect(x, y, w, h));
			canvasView.name = name.asSymbol;
		};

		canvasView.drawingEnabled = true;
		this.acceptClickThrough = false;

		canvasView.addAction({|v| this.onClose(this); }, \onClose);

		canvasView.addAction({|v, x, y, modifer|
			var coorScreen = QtGUI.cursorPosition;
			this.onMouseMove(this, x, y, coorScreen.x, coorScreen.y, modifer)
		}, \mouseMoveAction);

		// view.addAction({|view, x, y| "mouse %, %".format(x, y).postln; }, \mouseOverAction);
		canvasView.addAction({|v| this.onEnter(this); v.refresh; }, \mouseEnterAction);
		canvasView.addAction({|v| this.onLeave(this); v.refresh; }, \mouseLeaveAction);

		canvasView.addAction({|v| this.onResize(this) }, \onResize);
		if(parent.notNil) {	parent.view.addAction({|v| resizeParentAction.value(parent) }, \onResize) };

		/*
		View.globalKeyDownAction = {|v, char, modifiers, unicode, keycode|
		"% ,% ,% ,% ,%".format(v, char, modifiers, unicode, keycode).postln;
		};
		Document.globalKeyDownAction = {|v, char, modifiers, unicode, keycode|
		case
		{ modifiers == 262144 && keycode == 192 } { "Canvas Document.globalKeyDownAction ACTION".warn };
		// "% ,% ,% ,% ,%".format(v, char, modifiers, unicode, keycode).postln;
		};
		*/

		this.draw();
	}

	acceptClickThrough_ {|bool|
		if(bool)
		{
			if(this.parent.notNil)
			{
				canvasView.mouseDownAction = nil;
				canvasView.mouseUpAction = nil;
				canvasView.addAction( {|v, x, y|
					this.parent.view.mouseDown(x, y);
					// this.parent.onMouseDown(this.parent, x, y)
				} , \mouseDownAction);
				canvasView.addAction( {|v, x, y| this.parent.onMouseUp(this.parent, x, y) } , \mouseUpAction);
			}
		}
		{
			canvasView.mouseDownAction = nil;
			canvasView.mouseUpAction = nil;
			canvasView.addAction({|v, x, y|
				var coorScreen = QtGUI.cursorPosition;
				this.onMouseDown(this, x, y, coorScreen.x, coorScreen.y);
			}, \mouseDownAction);

			canvasView.addAction({|v, x, y|
				var coorScreen = QtGUI.cursorPosition;
				this.onMouseUp(this, x, y, coorScreen.x, coorScreen.y);
			}, \mouseUpAction);
		}
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

	view_ {|newVal| canvasView = newVal }
	view { if(canvasView.isClosed) { ^nil } { ^canvasView } }

	close {	canvasView.close }

	name_ { |txt| canvasView.name_(txt) }
	name { ^canvasView.name  }

	background_ {|color| colorBackground = color; this.refresh }
	background { ^colorBackground }

	// hasFrame_ { |bool| isFrameVisible = bool; }
	// hasFrame { ^isFrameVisible }

	showFrame_ { |bool| isFrameVisible = bool; }
	showFrame { ^isFrameVisible }

	// backgroundRGB_ {|r, g, b, a = 1| canvasView.background_(Color.new255(r,g,b,a * 255)) }

	color255_ { |name, r, g, b, a = 1| /*this.color_(name.asSymbol, Color.new255(r,g,b,a * 255)); */ }
	// color_ { |name, color| colorTable.put(name.asSymbol, color); this.refresh;}
	// color { |name| ^colorTable.at(name) }

	alpha_ {|a|
		if(canvasParent.isNil)
		{ canvasView.parent.alpha_(a) }
		{
			var color = this.background;
			this.background_(Color.new(color.red, color.green, color.blue, a));
		}
	}
	alpha {
		if(canvasParent.isNil)
		{ ^canvasView.parent.alpha }
		{ ^this.background.alpha }
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


	onClose {|canvas|
		// "%.onClose".format(canvas).postln;
	}

	onMouseDown {|canvas, x, y, screenX, screenY|
		// "%.onMouseDown [x:%, y:%, scrX:%, scrY:%]".format(canvas, x, y, screenX, screenY).postln;
	}

	onMouseUp {|canvas, x, y, screenX, screenY|
		// "%.onMouseUp [x:%, y:%, scrX:%, scrY:%]".format(canvas, x, y, screenX, screenY).postln;
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

	onResizeParent {|canvas|
		// "%.onResizeParent [parent: %, w:%, h:%]".format(this, canvas, canvas.width, canvas.height).postln;
	}

	draw { |fnc|
		canvasView.drawFunc_({|view|
			var rect = Rect(0,0, this.width, this.height);
			// var backgroundColor = CanvasConfig.getColor(this, \background);
			// var frameColor = CanvasConfig.getColor(this, \frame);

			if(colorBackground.notNil) {
				Pen.fillColor_( colorBackground );
				Pen.fillRect(rect)
			};

			if(colorFrame.notNil && this.showFrame) {
				Pen.strokeColor_( colorFrame );
				Pen.strokeRect( rect );
			};

			fnc.value(view);
		});
		canvasView.refresh;
	}

	refresh { canvasView.refresh }
}


