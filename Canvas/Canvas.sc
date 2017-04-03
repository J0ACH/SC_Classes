Canvas {

	var canvasParent, canvasView;

	// var >resizeParentAction;
	var colorBackground, colorFrame, isBackgroundVisible, isFrameVisible;

	var canvasActions;

	*initClass {
		CanvasConfig.addColor(this, \background, Color.new255(20,20,20));
		CanvasConfig.addColor(this, \frame, Color.new255(120,120,120));
		/*
		Document.globalKeyDownAction = {|v, char, modifiers, unicode, keycode|
		case
		{ modifiers == 262144 && keycode == 192 } { "Canvas Document.globalKeyDownAction".warn }; // Ctrl + "~"
		// "% ,% ,% ,% ,%".format(v, char, modifiers, unicode, keycode).postln;
		};
		*/
	}

	*new { |x, y, w, h, parent = nil, name = nil| ^super.new.initCanvas(x, y, w, h, parent, name).init }

	init { }

	initCanvas {|x, y, w, h, parent, name|

		canvasParent = parent;

		colorBackground = CanvasConfig.getColor(this, \background);
		colorFrame = CanvasConfig.getColor(this, \frame);
		isBackgroundVisible = false;
		isFrameVisible = false;

		// resizeParentAction = nil;

		canvasActions = MultiLevelIdentityDictionary.new;

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
		/*
		canvasView.addAction({|v, x, y, modifer|
		var coorScreen = QtGUI.cursorPosition;
		this.onMouseMove(this, x, y, coorScreen.x, coorScreen.y, modifer)
		}, \mouseMoveAction);
		*/
		// view.addAction({|view, x, y| "mouse %, %".format(x, y).postln; }, \mouseOverAction);
		// canvasView.addAction({|v| this.onEnter(this); v.refresh; }, \mouseEnterAction);
		// canvasView.addAction({|v| this.onLeave(this); v.refresh; }, \mouseLeaveAction);

		// canvasView.addAction({|v| this.onResize(this) }, \onResize);
		// if(parent.notNil) {	parent.view.addAction({|v| resizeParentAction.value(parent) }, \onResize) };

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

	// FUNCTIONS ///////////////////////////////////////////

	addAction {|action, name, fnc|
		var currentFnc = canvasActions.at(action.asSymbol, name.asSymbol);
		if(currentFnc.notNil) { this.removeAction(action, name) };
		canvasActions.put(action.asSymbol, name.asSymbol, fnc);
		canvasView.addAction(fnc, action.asSymbol);
	}
	removeAction {|action, name|
		var fnc = canvasActions.at(action.asSymbol, name.asSymbol);
		canvasView.removeAction(fnc, action.asSymbol);
		canvasActions.removeEmptyAtPath([action.asSymbol, name.asSymbol]);
	}
	printActions { canvasActions.postTree }

	add_onClose {|name, fnc| this.addAction(\onClose, name.asSymbol, {|v| fnc.value(this) }) }
	remove_onClose {|name| this.removeAction(\onClose, name.asSymbol) }

	add_onMouseDown {|name, fnc|
		this.addAction(\mouseDownAction, name.asSymbol,	{|v, x, y|
			var coorScreen = QtGUI.cursorPosition;
			fnc.value(this, x, y, coorScreen.x, coorScreen.y);
		})
	}
	remove_onMouseDown {|name| this.removeAction(\mouseDownAction, name.asSymbol) }

	add_onMouseUp {|name, fnc|
		this.addAction(\mouseUpAction, name.asSymbol, {|v, x, y|
			var coorScreen = QtGUI.cursorPosition;
			fnc.value(this, x, y, coorScreen.x, coorScreen.y);
		})
	}
	remove_onMouseUp {|name| this.removeAction(\mouseUpAction, name.asSymbol) }

	add_onMouseEnter {|name, fnc| this.addAction(\mouseEnterAction, name.asSymbol, {|v| fnc.value(this) })	}
	remove_onMouseEnter {|name| this.removeAction(\mouseEnterAction, name.asSymbol) }

	add_onMouseLeave {|name, fnc| this.addAction(\mouseLeaveAction, name.asSymbol, {|v| fnc.value(this) })	}
	remove_onMouseLeave {|name| this.removeAction(\mouseLeaveAction, name.asSymbol) }

	add_onMouseMove {|name, fnc|
		this.addAction(\mouseMoveAction, name.asSymbol, {|v, x, y|
			var coorScreen = QtGUI.cursorPosition;
			fnc.value(this, x, y, coorScreen.x, coorScreen.y);
		})
	}
	remove_onMouseMove {|name| this.removeAction(\mouseMoveAction, name.asSymbol) }

	add_onParentResize {|name, fnc|
		if(this.parent.notNil) {
			var currentFnc = canvasActions.at(\onParentResize, name.asSymbol);
			if(currentFnc.notNil) { this.remove_onParentResize(name) };
			canvasActions.put(\onParentResize, name.asSymbol, fnc);
			this.parent.view.addAction({|v| fnc.value(this.parent) }, \onResize);
		}
	}
	remove_onParentResize {|name|
		if(this.parent.notNil) {
			var fnc = canvasActions.at(\onParentResize, name.asSymbol);
			this.parent.view.removeAction(fnc, \onResize);
			canvasActions.removeEmptyAtPath([\onParentResize, name.asSymbol]);
		}
	}

	///////////////////////////////////////////////////////

	acceptClickThrough_ {|bool|
		if(bool) {
			canvasView.mouseDownAction = { false };
			canvasView.mouseUpAction = { false };
			canvasView.mouseEnterAction = { false };
			canvasView.mouseLeaveAction = { false };
			canvasView.mouseMoveAction = { false };
			canvasView.mouseOverAction = { false };
		}
		{
			canvasActions.leafDo({|path, fnc|
				var action = path[0].asSymbol;
				var name = path[1].asSymbol;
				this.addAction(action, name, fnc);
			})
		};
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

	view_ {|newVal| canvasView = newVal }
	view { if(canvasView.isClosed) { ^nil } { ^canvasView } }

	close {	canvasView.close }

	name_ { |txt| canvasView.name_(txt) }
	name { ^canvasView.name  }

	background_ {|color| colorBackground = color; this.refresh }
	background { ^colorBackground }

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
		if(canvasView.isClosed) { ^nil };
		if(canvasParent.isNil) { ^this.screenOrigin } { ^canvasView.bounds.origin };
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
		canvasView.drawFunc_({|v|
			var rect = Rect(0,0, this.width, this.height);

			if(colorBackground.notNil) {
				Pen.fillColor_( colorBackground );
				Pen.fillRect(rect)
			};

			if(colorFrame.notNil && this.showFrame) {
				Pen.strokeColor_( colorFrame );
				Pen.strokeRect( rect );
			};

			fnc.value(this);
		});
		canvasView.refresh;
	}

	refresh { canvasView.refresh }
}



