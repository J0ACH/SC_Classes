CanvasButton : Canvas {

	var fillColor, backColor, overColor;
	var <string;

	var >mouseDownAction;

	init {

		mouseDownAction = nil;
		string = "CanvasButton";
		backColor = Color.new255(80,80,80);
		overColor = Color.new255(180,80,80);
		fillColor = backColor;

		this.draw({
			var rect = Rect(0,0, this.width, this.height);
			Pen.fillColor_( fillColor );
			Pen.fillRect(rect);
			Pen.color_(Color.white);
			Pen.strokeRect(rect);
			Pen.stringCenteredIn(string, rect);
		});

		// view.addAction({|view, x, y| this.onPress }, \mouseDownAction);

	}

	string_ {|txt| string = txt; this.view.refresh; }
	//
	onEnter {|view| fillColor = overColor; "mouse enter CanvasButton('%')".format(view.name).postln; }
	onLeave {|view| fillColor = backColor; "mouse enter CanvasButton('%')".format(view.name).postln; }

	onMouseDown {|name, x, y|
		if(mouseDownAction.notNil)
		{ mouseDownAction.value }
		{ "CanvasButton('%').mouseDownAction not set".format(name).warn }
	}



}
