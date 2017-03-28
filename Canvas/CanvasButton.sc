CanvasButton : Canvas {

	var <string;
	var >mouseDownAction;
	var isActive, holdState;
	var fadetimeEnter, fadetimeLeave, fps, fadeTask, fadeAlpha;

	*initClass {
		// "button init".warn;
		CanvasConfig.addColor(this, \background, Color.new255(20,20,20));
		CanvasConfig.addColor(this, \frame, Color.new255(190,190,190));
		CanvasConfig.addColor(this, \text, Color.new255(190,190,190));
		CanvasConfig.addColor(this, \over, Color.new255(50,90,90));
		CanvasConfig.addColor(this, \active, Color.new255(90,140,180));
	}

	*new { |x, y, w, h, parent|	^super.new(x, y, w, h, parent).initButton.init }

	initButton {

		isActive = false;
		holdState = false;

		mouseDownAction = nil;
		string = "CanvasButton";

		fadeTask = nil;
		fps = 25;
		fadeAlpha = 0;
		fadetimeEnter = 0.15;
		fadetimeLeave = 0.75;

		this.draw({
			var rect = Rect(0,0, this.width, this.height);
			Pen.color_( CanvasConfig.getColor(this, \text) );
			Pen.stringCenteredIn(string, rect);
		});
	}

	string_ {|txt| string = txt; this.view.refresh; }

	onEnter {|view|
		if(isActive.not)
		{
			var colorFrom = CanvasConfig.getColor(this, \background);
			var colorTo = CanvasConfig.getColor(this, \over);
			var alphaStep = (1-fadeAlpha) / (fps * fadetimeEnter);

			if( fadeTask.notNil ) { fadeTask.stop; fadeTask = nil };
			fadeTask = Routine({
				(fadetimeEnter * fps + 1).do({
					this.background = colorFrom.blend(colorTo, fadeAlpha);
					(1 / fps).wait;
					fadeAlpha = fadeAlpha + alphaStep;
					if(fadeAlpha >= 1) {
						fadeAlpha = 1;
						this.background = colorTo;
						fadeTask.stop;
						fadeTask = nil;
					};
				});
			}).play(AppClock);
		}
		// "%.onEnter".format(this).postln;
	}

	onLeave {|view|
		if(isActive.not)
		{
			var colorFrom  = CanvasConfig.getColor(this, \over);
			var colorTo = CanvasConfig.getColor(this, \background);
			var alphaStep = fadeAlpha / (fps * fadetimeLeave);

			if( fadeTask.notNil ) { fadeTask.stop; fadeTask = nil };
			fadeTask = Routine({
				(fadetimeLeave * fps + 1).do({
					this.background = colorTo.blend(colorFrom, fadeAlpha);
					(1 / fps).wait;
					fadeAlpha = fadeAlpha - alphaStep;
					if(fadeAlpha <= 0) {
						fadeAlpha = 0;
						this.background = colorTo;
						fadeTask = nil;
					};
				});
			}).play(AppClock);
		}
		// "%.onLeave".format(this).postln;
	}

	onClose {
		fadeTask.stop;
		fadeTask = nil;
	}

	onMouseDown {|name, x, y|
		if(mouseDownAction.notNil)
		{
			mouseDownAction.value;
			if(isActive.not)
			{
				this.background = CanvasConfig.getColor(this, \active);
				isActive = true;
			}
			{
				this.background = CanvasConfig.getColor(this, \over);
				isActive = false;
			}
		}
		{ "CanvasButton('%').mouseDownAction not set".format(name).warn }
	}

	onMouseUp {|name, x, y|
		if(holdState.not)
		{
			this.background = CanvasConfig.getColor(this, \over);
			isActive = false;
		}
	}
}
