CanvasMove : Canvas {

	var screenMouseDown, screenOriginMouseDown;
	var offset = 5;
	var thickness = 20;

	*initClass {
		// "text init".warn;
		// CanvasConfig.addColor(this, \background, Color.new255(50,20,20));
		CanvasConfig.addColor(this, \frame, Color.new255(190,190,190));
	}

	*new { |parent|	^super.new(0, 0, 50, 50, parent).init(parent) }

	init { |p|
		this.parent_(p);

		this.name = "CanvasMove";
		this.showFrame = true;
		this.resizeParentAction_({
			this.origin_(2*offset + thickness, 2*offset + thickness);
			this.size_(this.parent.width - (4*offset) - (2*thickness), thickness);
		});

		// this.draw();
	}

	onMouseDown {|canvas, x, y, screenX, screenY|
		screenMouseDown = Point(screenX, screenY);
		screenOriginMouseDown = Point(this.parent.screenOrigin.x, this.parent.screenOrigin.y);
	}

	onMouseMove {|canvas, x, y, screenX, screenY, modifer|
		var deltaX = screenX - screenMouseDown.x;
		var deltaY = screenY - screenMouseDown.y;
		this.parent.screenOrigin_(screenOriginMouseDown.x + deltaX,  screenOriginMouseDown.y + deltaY);
	}
}


CanvasSize {
	var manipuls;

	*new { |parent| ^super.new().init(parent) }

	init { |p|
		var sideKeys = [\right, \rightBottom, \bottom, \leftBottom, \left, \leftTop, \top, \rightTop];

		manipuls = IdentityDictionary.new();

		sideKeys.do({|side|
			var edge = CanvasSize_Edge(p, side);
			manipuls.put(side.asSymbol, edge);
		})
	}
}

CanvasSize_Edge : Canvas {
	classvar <corrners = 30;
	classvar <offset = 1;
	classvar <thickness = 15;

	var edgeSide;
	var screenMouseDown, screenOriginMouseDown, mouseDownSize;
	var edgeColor, fadetimeEnter, fadetimeLeave, fps, fadeTask, fadeAlpha;

	*initClass {
		CanvasConfig.addColor(this, \normal, Color.new255(20,20,20));
		CanvasConfig.addColor(this, \over, Color.new255(50,90,90));
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

		this.resizeParentAction_({ this.onParentResize });

		this.view.addAction({|v, x, y|
			var coorScreen = QtGUI.cursorPosition;
			this.onMouseDown(this, x, y, coorScreen.x, coorScreen.y, this.parent);
		}, \mouseDownAction);

		this.view.addAction({|v, x, y, modifer|
			var coorScreen = QtGUI.cursorPosition;
			this.onMouseMove(this, side, x, y, coorScreen.x, coorScreen.y, modifer)
		}, \mouseMoveAction);

		this.draw({
			Pen.strokeColor_( edgeColor );
			Pen.width = 3;

			case
			{ side == \right } {
				Pen.moveTo(this.width @ 0);
				Pen.lineTo(this.width @ this.height);
			}
			{ side == \rightBottom } {
				Pen.moveTo(0 @ this.height);
				Pen.lineTo(this.width @ this.height);
				Pen.lineTo(this.width @ 0);
			}
			{ side == \bottom } {
				Pen.moveTo(0 @ this.height);
				Pen.lineTo(this.width @ this.height);
			}
			{ side == \leftBottom } {
				Pen.moveTo(0 @ 0);
				Pen.lineTo(0 @ this.height);
				Pen.lineTo(this.width @ this.height);
			}
			{ side == \left } {
				Pen.moveTo(0 @ 0);
				Pen.lineTo(0 @ this.height);
			}
			{ side == \leftTop } {
				Pen.moveTo(0 @ this.height);
				Pen.lineTo(0 @ 0);
				Pen.lineTo(this.width @ 0);
			}
			{ side == \top } {
				Pen.moveTo(0 @ 0);
				Pen.lineTo(this.width @ 0);
			}
			{ side == \rightTop } {
				Pen.moveTo(0 @ 0);
				Pen.lineTo(this.width @ 0);
				Pen.lineTo(this.width @ this.height);
			};

			Pen.stroke;
		});

		this.onParentResize;
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


