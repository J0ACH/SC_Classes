CanvasButton : Canvas {

	var <string;
	var >mouseDownAction;

	*initClass {
		// "button init".warn;
		CanvasConfig.addColor(this, \background, Color.new255(20,20,20));
		CanvasConfig.addColor(this, \frame, Color.new255(190,190,190));
		CanvasConfig.addColor(this, \normal, Color.new255(50,50,50));
		CanvasConfig.addColor(this, \over, Color.new255(150,80,80));
		CanvasConfig.addColor(this, \active, Color.new255(150,80,80));
		CanvasConfig.addColor(this, \text, Color.new255(120,120,120));
	}

	*new { |x, y, w, h, parent|	^super.new(x, y, w, h, parent).initButton.init }

	initButton {

		mouseDownAction = nil;
		string = "CanvasButton";

		this.draw({
			var rect = Rect(0,0, this.width, this.height);
			Pen.color_( CanvasConfig.getColor(this, \text) );
			Pen.stringCenteredIn(string, rect);
		});
	}

	string_ {|txt| string = txt; this.view.refresh; }
	//
	onEnter {|view|
		// this.background = config.at(\color, \over);
		"%.onEnter".format(this).postln;
	}
	onLeave {|view|
		// this.background = config.at(\color, \normal);
		"%.onLeave".format(this).postln;
	}

	onMouseDown {|name, x, y|
		if(mouseDownAction.notNil)
		{ mouseDownAction.value }
		{ "CanvasButton('%').mouseDownAction not set".format(name).warn }
	}



}
