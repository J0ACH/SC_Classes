CanvasMove : Canvas {

	var label;
	var offset = 7;
	var thickness = 30;
	var screenMouseDown, screenOriginMouseDown;

	*initClass {
		// "text init".warn;
		CanvasConfig.addColor(this, \background, Color.new255(40,0,0));
		CanvasConfig.addColor(this, \frame, Color.new255(190,190,190));
	}

	*new { |p| ^super.dummy(p).init }

	init {
		label = CanvasText(0, 0, this.width, this.height, this)
		.name_("CanvasMove_label")
		.acceptClickThrough_(true)
		.draw_removeLayer(\frame)
		.draw_removeLayer(\background)
		// .showFrame_(false)
		// .alpha_(0)
		.add_onParentResize(\default, {|parentCanvas| label.size_(parentCanvas.width, parentCanvas.height) });

		this.name = "CanvasMove";
		this.add_onParentResize(\default, {|parentCanvas|
			this.origin_(1, 1);
			this.size_(parentCanvas.width - 2, thickness);
		});

		this.add_onMouseDown(\default, {|canvas, x, y, screenX, screenY|
			screenMouseDown = Point(screenX, screenY);
			screenOriginMouseDown = Point(canvas.parent.screenOrigin.x, canvas.parent.screenOrigin.y);
		});

		this.add_onMouseMove(\default, {|canvas, x, y, screenX, screenY, modifer|
			var deltaX = screenX - screenMouseDown.x;
			var deltaY = screenY - screenMouseDown.y;
			canvas.parent.screenOrigin_(screenOriginMouseDown.x + deltaX,  screenOriginMouseDown.y + deltaY);
		});

		this.add_onMouseEnter(\animateTest, { this.draw_animateLayer(); });

		// this.draw_removeLayer(\background);
		this.draw_removeLayer(\frame);
		// this.draw_addLayer(\conuter, { canvasView.frame.postln });
		this.draw_animateLayer(1, {|frame| "frame: %".format(frame).postln })
		// this.printLayers;
	}

	string_ {|txt| label.string = txt }
}

CanvasSize {
	var manipuls;

	*new { |parent| ^super.new().init(parent) }

	init { |p|
		var sideKeys = [\right, \rightBottom, \bottom, \leftBottom, \left, \leftTop, \top, \rightTop];

		manipuls = IdentityDictionary.new();

		sideKeys.do({|side|
			var edge = CanvasSize_Edge(p, side);
			// edge.add_onParentResize(\test, {"config size parent resize".warn});
			manipuls.put(side.asSymbol, edge);
		})
	}
}

CanvasSize_Edge : Canvas {
	classvar <corrners = 30;
	classvar <offset = 1;
	classvar <thickness = 5;

	var edgeSide;
	var screenMouseDown, screenOriginMouseDown, mouseDownSize;
	var edgeColor, fadetimeEnter, fadetimeLeave, fps, fadeTask, fadeAlpha;

	*initClass {
		CanvasConfig.addColor(this, \normal, Color.new255(20,20,20));
		CanvasConfig.addColor(this, \over, Color.new255(70,110,110));
		CanvasConfig.addColor(this, \active, Color.new255(90,140,180));
	}

	*new {|parent, side| ^super.new(0,0,50,50,parent).initEdge(side) }

	initEdge {|side|
		edgeSide = side;

		edgeColor = CanvasConfig.getColor(this, \normal);
		fadeTask = nil;
		fps = 25;
		fadeAlpha = 0;
		fadetimeEnter = 0.15;
		fadetimeLeave = 0.75;

		this.add_onMouseEnter(\default, {|canvas| this.onEnter; });
		this.add_onMouseLeave(\default, {|canvas| this.onLeave; });
		this.add_onMouseDown(\default, {|canvas, x, y, screenX, screenY| this.onMouseDown(this, x, y, screenX, screenY, this.parent) });
		this.add_onMouseMove(\default, {|canvas, x, y, screenX, screenY| this.onMouseMove(this, side, x, y, screenX, screenY) });
		this.add_onParentResize(\default, {|parentCanvas| this.onParentResize });

		this.draw_removeLayer(\background);
		this.draw_removeLayer(\frame);
		this.draw_addLayer(\edge, {|rect|
			Pen.strokeColor_( edgeColor );
			Pen.width = 3;

			case
			{ side == \right } {
				Pen.moveTo(rect.width @ 0);
				Pen.lineTo(rect.width @ rect.height);
			}
			{ side == \rightBottom } {
				Pen.moveTo(0 @ rect.height);
				Pen.lineTo(rect.width @ rect.height);
				Pen.lineTo(rect.width @ 0);
			}
			{ side == \bottom } {
				Pen.moveTo(0 @ rect.height);
				Pen.lineTo(rect.width @ rect.height);
			}
			{ side == \leftBottom } {
				Pen.moveTo(0 @ 0);
				Pen.lineTo(0 @ rect.height);
				Pen.lineTo(rect.width @ rect.height);
			}
			{ side == \left } {
				Pen.moveTo(0 @ 0);
				Pen.lineTo(0 @ rect.height);
			}
			{ side == \leftTop } {
				Pen.moveTo(0 @ rect.height);
				Pen.lineTo(0 @ 0);
				Pen.lineTo(rect.width @ 0);
			}
			{ side == \top } {
				Pen.moveTo(0 @ 0);
				Pen.lineTo(rect.width @ 0);
			}
			{ side == \rightTop } {
				Pen.moveTo(0 @ 0);
				Pen.lineTo(rect.width @ 0);
				Pen.lineTo(rect.width @ rect.height);
			};

			Pen.stroke;
		});
	}

	onEnter {|view|
		var colorFrom = CanvasConfig.getColor(this, \normal);
		var colorTo = CanvasConfig.getColor(this, \over);
		var alphaStep = (1-fadeAlpha) / (fps * fadetimeEnter);

		if( fadeTask.notNil ) { fadeTask.stop; fadeTask = nil };
		fadeTask = Routine({
			(fadetimeEnter * fps + 1).do({
				edgeColor = colorFrom.blend(colorTo, fadeAlpha);
				(1 / fps).wait;
				fadeAlpha = fadeAlpha + alphaStep;
				if(fadeAlpha >= 1) {
					fadeAlpha = 1;
					edgeColor = colorTo;
					fadeTask.stop;
					fadeTask = nil;
				};
				this.refresh;
			});
		}).play(AppClock);
	}

	onLeave {|view|
		var colorFrom  = CanvasConfig.getColor(this, \over);
		var colorTo = CanvasConfig.getColor(this, \normal);
		var alphaStep = fadeAlpha / (fps * fadetimeLeave);

		if( fadeTask.notNil ) { fadeTask.stop; fadeTask = nil };
		fadeTask = Routine({
			(fadetimeLeave * fps + 1).do({
				edgeColor = colorTo.blend(colorFrom, fadeAlpha);
				(1 / fps).wait;
				fadeAlpha = fadeAlpha - alphaStep;
				if(fadeAlpha <= 0) {
					fadeAlpha = 0;
					edgeColor = colorTo;
					fadeTask = nil;
				};
				this.refresh;
			});
		}).play(AppClock);
	}

	onMouseDown {|manipul, x, y, screenX, screenY, p|
		screenMouseDown = Point(screenX, screenY);
		screenOriginMouseDown = Point(this.parent.screenOrigin.x, this.parent.screenOrigin.y);
		mouseDownSize = p.size;

		if( fadeTask.notNil ) { fadeTask.stop; fadeTask = nil };
		edgeColor = CanvasConfig.getColor(this, \active);
		this.refresh;
	}

	onMouseMove {|manipul, side, x, y, screenX, screenY, modifer|
		var deltaX = screenX - screenMouseDown.x;
		var deltaY = screenY - screenMouseDown.y;

		case
		{ side == \right } {
			this.parent.width_(mouseDownSize.width + deltaX);
		}
		{ side == \rightBottom } {
			this.parent.size_(mouseDownSize.width + deltaX, mouseDownSize.height + deltaY);
		}
		{ side == \bottom } {
			this.parent.height_(mouseDownSize.height + deltaY);
		}
		{ side == \leftBottom } {
			this.parent.screenOriginX_(screenOriginMouseDown.x + deltaX);
			this.parent.size_(mouseDownSize.width - deltaX, mouseDownSize.height + deltaY);
		}
		{ side == \left } {
			this.parent.screenOriginX_(screenOriginMouseDown.x + deltaX);
			this.parent.width_(mouseDownSize.width - deltaX);
		}
		{ side == \leftTop } {
			this.parent.screenOrigin_(screenOriginMouseDown.x + deltaX, screenOriginMouseDown.y + deltaY);
			this.parent.size_(mouseDownSize.width - deltaX, mouseDownSize.height - deltaY);
		}
		{ side == \top } {
			this.parent.screenOriginY_(screenOriginMouseDown.y + deltaY);
			this.parent.height_(mouseDownSize.height - deltaY);
		}
		{ side == \rightTop } {
			this.parent.screenOriginY_(screenOriginMouseDown.y + deltaY);
			this.parent.size_(mouseDownSize.width + deltaX, mouseDownSize.height - deltaY);
		};
	}

	onMouseUp {|name, x, y|
		edgeColor = CanvasConfig.getColor(this, \over);
		this.refresh;
	}

	onParentResize {
		case
		{ edgeSide == \right } {
			this.origin_(this.parent.size.width - offset - thickness, 2*offset + thickness);
			this.size_(thickness, this.parent.size.height - (4*offset) - (2*thickness));
		}
		{ edgeSide == \rightBottom } {
			this.origin_(this.parent.size.width - offset - thickness, this.parent.size.height - offset - thickness);
			this.size_(thickness, thickness);
		}
		{ edgeSide == \bottom } {
			this.origin_(2*offset + thickness, this.parent.size.height - offset - thickness);
			this.size_(this.parent.size.width - (4*offset) - (2*thickness), thickness);
		}
		{ edgeSide == \leftBottom } {
			this.origin_(offset, this.parent.size.height - offset - thickness);
			this.size_(thickness, thickness);
		}
		{ edgeSide == \left } {
			this.origin_(offset, 2*offset + thickness);
			this.size_(thickness, this.parent.size.height - (4*offset) - (2*thickness));
		}
		{ edgeSide == \leftTop } {
			this.origin_(offset, offset);
			this.size_(thickness, thickness);
		}
		{ edgeSide == \top } {
			this.origin_(2*offset + thickness, offset);
			this.size_(this.parent.size.width - (4*offset) - (2*thickness), thickness);
		}
		{ edgeSide == \rightTop } {
			this.origin_(this.parent.size.width - offset - thickness, offset);
			this.size_(thickness, thickness);
		};
	}
}



CanvasClose : CanvasButton {

	*new {|parent, offset, size| ^super.new(parent.width - offset - size, offset, size, size, parent).init(parent, offset, size) }

	init {|parent, offset, size|
		// this.background_(255,0,0);
		this.string = "X";
		this.mouseDownAction = { parent.close };
		// this.refresh;
	}
}


