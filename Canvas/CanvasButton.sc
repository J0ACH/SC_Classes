CanvasButton : Canvas {

	var <string;
	var >mouseDownAction;
	var fadetimeEnter, fadetimeLeave, fadeTask;

	*initClass {
		// "button init".warn;
		CanvasConfig.addColor(this, \background, Color.new255(20,20,20));
		CanvasConfig.addColor(this, \frame, Color.new255(190,190,190));
		// CanvasConfig.addColor(this, \normal, Color.new255(50,50,50));
		CanvasConfig.addColor(this, \over, Color.new255(150,80,80));
		CanvasConfig.addColor(this, \active, Color.new255(150,80,80));
		CanvasConfig.addColor(this, \text, Color.new255(120,120,120));
	}

	*new { |x, y, w, h, parent|	^super.new(x, y, w, h, parent).initButton.init }

	initButton {
		mouseDownAction = nil;
		string = "CanvasButton";

		fadeTask = nil;
		fadetimeEnter = 1;
		fadetimeLeave = 4;

		this.draw({
			var rect = Rect(0,0, this.width, this.height);
			Pen.color_( CanvasConfig.getColor(this, \text) );
			Pen.stringCenteredIn(string, rect);
		});
	}

	string_ {|txt| string = txt; this.view.refresh; }

	onEnter {|view|
		var alpha = 0;
		var backCol = CanvasConfig.getColor(this, \background);
		var overCol = CanvasConfig.getColor(this, \over);

		// AppClock.sched(

		// fadeTask = Routine.run({
		/*
		{
			10.do({
				var col = backCol.blend(overCol, alpha);
				// this.background = backCol.blend(overCol, alpha);
				this.background = col;
				0.1.wait;
				alpha = alpha + 0.1;
				alpha.postln;
				col.postln;
			});
			this.background = overCol;
		}.defer;
		*/
		this.background = overCol;
		// });
		// "%.onEnter".format(this).postln;
	}
	onLeave {|view|
		this.background = CanvasConfig.getColor(this, \background);
		// "%.onLeave".format(this).postln;
	}

	onMouseDown {|name, x, y|
		if(mouseDownAction.notNil)
		{ mouseDownAction.value }
		{ "CanvasButton('%').mouseDownAction not set".format(name).warn }
	}



}
