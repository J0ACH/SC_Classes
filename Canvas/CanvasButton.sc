CanvasButton : Canvas {

	var fillColor, backColor, overColor;
	var <string;

	var >mouseDownAction;

	*new { |x, y, w, h, parent|	^super.new(x, y, w, h, parent).initButton.init }

	initButton {

		mouseDownAction = nil;
		string = "CanvasButton";
		// backColor = this.color(\background);
		this.color255_(\colorBackground, 50,80,80);
		this.color255_(\colorOver, 150,80,80);
		// overColor = Color.new255(180,80,80);
		// fillColor = this.background;

		this.draw({
			var rect = Rect(0,0, this.width, this.height);
			// Pen.fillColor_( backColor );
			// Pen.fillRect(rect);
			Pen.color_(Color.white);
			Pen.strokeRect(rect);
			Pen.stringCenteredIn(string, rect);
		});

		// view.addAction({|view, x, y| this.onPress }, \mouseDownAction);

	}

	string_ {|txt| string = txt; this.view.refresh; }
	//
	onEnter {|view|
		this.color_(\colorBackground, this.color(\background));
		this.color_(\background, this.color(\colorOver));
		"%.onEnter".format(this).postln;
	}
	onLeave {|view|
		this.color_(\background, this.color(\colorBackground));
		"%.onLeave".format(this).postln;
	}

	onMouseDown {|name, x, y|
		if(mouseDownAction.notNil)
		{ mouseDownAction.value }
		{ "CanvasButton('%').mouseDownAction not set".format(name).warn }
	}



}
