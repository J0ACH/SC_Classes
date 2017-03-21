CanvasButton : Canvas {

	var <string;
	var >mouseDownAction;

	*new { |x, y, w, h, parent|	^super.new(x, y, w, h, parent).initButton.init }

	initButton {

		mouseDownAction = nil;
		string = "CanvasButton";


		this.background = config.at(\color, \normal);

		this.draw({
			var rect = Rect(0,0, this.width, this.height);
			Pen.color_(Color.white);
			Pen.strokeRect(rect);
			Pen.stringCenteredIn(string, rect);
		});


		// view.addAction({|view, x, y| this.onPress }, \mouseDownAction);

	}

	initConfig {
		config.put(\color, \normal, Color.new255(50,50,50));
		config.put(\color, \over, Color.new255(150,80,80));
		config.put(\color, \active, Color.new255(150,80,80));
	}

	string_ {|txt| string = txt; this.view.refresh; }
	//
	onEnter {|view|
		this.background = config.at(\color, \over);
		"%.onEnter".format(this).postln;
	}
	onLeave {|view|
		this.background = config.at(\color, \normal);
		"%.onLeave".format(this).postln;
	}

	onMouseDown {|name, x, y|
		if(mouseDownAction.notNil)
		{ mouseDownAction.value }
		{ "CanvasButton('%').mouseDownAction not set".format(name).warn }
	}



}
