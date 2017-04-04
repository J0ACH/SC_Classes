CanvasButton : Canvas {

	var <text;
	var isActive, >holdState;
	var fadetimeEnter, fadetimeLeave, fps, fadeTask, fadeAlpha;

	*initClass {
		// "button init".warn;
		CanvasConfig.addColor(this, \background, Color.new255(20,20,20));
		CanvasConfig.addColor(this, \frame, Color.new255(190,190,190));
		CanvasConfig.addColor(this, \text, Color.new255(190,190,190));
		CanvasConfig.addColor(this, \over, Color.new255(50,90,90));
		CanvasConfig.addColor(this, \active, Color.new255(90,140,180));
		// CanvasConfig.addFont(this, \text, Font.new("Univers Condensed", 11, usePointSize: true));
		CanvasConfig.addFont(this, \text, Font.new("Consolas", 8, usePointSize: true));
	}

	*new { |x, y, w, h, parent|	^super.new(x, y, w, h, parent).initButton }

	initButton {
		isActive = false;
		holdState = false;

		this.add_onMouseDown(\default, {|canvas, x, y| this.onMouseDown(canvas, x, y)});
		this.add_onMouseUp(\default, {|canvas, x, y| this.onMouseUp(canvas, x, y)});
		this.add_onMouseEnter(\default, {|canvas| this.onEnter; });
		this.add_onMouseLeave(\default, {|canvas| this.onLeave; });

		// mouseDownAction = nil;

		fadeTask = nil;
		fps = 25;
		fadeAlpha = 0;
		fadetimeEnter = 0.15;
		fadetimeLeave = 0.75;

		text = CanvasText(0, 0, this.width, this.height, this);
		text.string = "CanvasButton";
		// text.acceptClickThrough = true;
		// text.showFrame = false;
		// text.alpha = 0;
		text.draw_removeLayer(\background);
		text.draw_removeLayer(\frame);
	}

	string_ {|txt| text.string = txt; }

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

	onMouseUp {|name, x, y|
		if(holdState.not)
		{
			this.background = CanvasConfig.getColor(this, \over);
			isActive = false;
		}
	}
}
