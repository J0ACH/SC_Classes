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
		this.hasFrame = true;
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
	classvar <offset = 5;
	classvar <thickness = 20;

	var parent;
	var manipuls;
	var screenMouseDown, screenOriginMouseDown, mouseDownSize;

	var manipulsColors;
	var isActive, holdState;
	var fadetimeEnter, fadetimeLeave, fps, fadeTask, fadeAlpha;

	*initClass {
		// "text init".warn;
		// CanvasConfig.addColor(this, \background, Color.new255(50,20,20));
		CanvasConfig.addColor(this, \frame, Color.new255(190,10,10));
		CanvasConfig.addColor(this, \normal, Color.new255(50,50,50));
		CanvasConfig.addColor(this, \over, Color.new255(50,190,190));
		CanvasConfig.addColor(this, \active, Color.new255(90,140,180));
	}

	*new { |parent ... positions| ^super.new().init(parent)	}

	// *right { ^\right }
	// *bottom { ^\bottom }

	init { |p|
		var sideKeys = [\right, \rightBottom, \bottom, \leftBottom, \left, \leftTop, \top, \rightTop];

		parent = p;
		manipuls = IdentityDictionary.new();
		manipulsColors = IdentityDictionary.new();
		fadeTask = IdentityDictionary.new();
		fadeAlpha = IdentityDictionary.new();

		isActive = false;
		holdState = false;
		// fadeTask = nil;
		fps = 25;
		// fadeAlpha = 0;
		fadetimeEnter = 0.15;
		fadetimeLeave = 0.75;

		parent.view.addAction({|v| this.onResize(parent) }, \onResize);

		sideKeys.do({|side|
			var oneManipul = Canvas(0, 0, 50, 50, parent);
			oneManipul.name = "CanvasSize_%".format(side);

			manipulsColors.put(oneManipul.name.asSymbol, CanvasConfig.getColor(this, \normal));
			fadeTask.put(oneManipul.name.asSymbol, nil);
			fadeAlpha.put(oneManipul.name.asSymbol, 0);

			oneManipul.draw({
				var rect = Rect(0,0, oneManipul.width, oneManipul.height);
				// Pen.strokeColor_( CanvasConfig.getColor(this, \frame) );
				// Pen.strokeColor_( CanvasConfig.getColor(this, \normal) );
				// Pen.strokeRect( rect );
				Pen.strokeColor_( manipulsColors.at(oneManipul.name.asSymbol) );

				case
				{ side == \right } {
					Pen.moveTo(oneManipul.width @ 0);
					Pen.lineTo(oneManipul.width @ oneManipul.height);
				}
				{ side == \rightBottom } {
					Pen.moveTo(0 @ oneManipul.height);
					Pen.lineTo(oneManipul.width @ oneManipul.height);
					Pen.lineTo(oneManipul.width @ 0);
				}
				{ side == \bottom } {
					Pen.moveTo(0 @ oneManipul.height);
					Pen.lineTo(oneManipul.width @ oneManipul.height);
				}
				{ side == \leftBottom } {
					Pen.moveTo(0 @ 0);
					Pen.lineTo(0 @ oneManipul.height);
					Pen.lineTo(oneManipul.width @ oneManipul.height);
				}
				{ side == \left } {
					Pen.moveTo(0 @ 0);
					Pen.lineTo(0 @ oneManipul.height);
				}
				{ side == \leftTop } {
					Pen.moveTo(0 @ oneManipul.height);
					Pen.lineTo(0 @ 0);
					Pen.lineTo(oneManipul.width @ 0);
				}
				{ side == \top } {
					Pen.moveTo(0 @ 0);
					Pen.lineTo(oneManipul.width @ 0);
				}
				{ side == \rightTop } {
					Pen.moveTo(0 @ 0);
					Pen.lineTo(oneManipul.width @ 0);
					Pen.lineTo(oneManipul.width @ oneManipul.height);
				};

				Pen.stroke;
			});

			oneManipul.view.addAction({|v| this.onEnter(oneManipul); oneManipul.refresh; }, \mouseEnterAction );
			oneManipul.view.addAction({|v| this.onLeave(oneManipul); oneManipul.refresh; }, \mouseLeaveAction );

			oneManipul.view.addAction({|v, x, y|
				var coorScreen = QtGUI.cursorPosition;
				this.onMouseDown(this, x, y, coorScreen.x, coorScreen.y, parent);
			}, \mouseDownAction);

			oneManipul.view.addAction({|v, x, y, modifer|
				var coorScreen = QtGUI.cursorPosition;
				this.onMouseMove(this, side, x, y, coorScreen.x, coorScreen.y, modifer)
			}, \mouseMoveAction);

			manipuls.put(side.asSymbol, oneManipul);
		});

		this.onResize(parent);
	}

	onEnter {|manipul|
		// if(isActive.not)
		// {
		// "onManipul enter %".format(manipul).postln;
		var fTask = fadeTask.at(manipul.name.asSymbol);
		var fAlpha = fadeAlpha.at(manipul.name.asSymbol);
		var colorFrom = CanvasConfig.getColor(this, \normal);
		var colorTo = CanvasConfig.getColor(this, \over);
		var alphaStep = (1-fAlpha) / (fps * fadetimeEnter);

		if( fTask.notNil ) { fTask.stop; fTask = nil };
		fTask = Routine({
			(fadetimeEnter * fps + 1).do({
				manipulsColors.put(manipul.name.asSymbol, colorFrom.blend(colorTo, fAlpha));
				(1 / fps).wait;
				fAlpha = fAlpha + alphaStep;
				fadeAlpha.put(manipul.name.asSymbol, fAlpha);
				if(fAlpha >= 1) {
					fAlpha = 1;
					manipulsColors.put(manipul.name.asSymbol, colorTo);
					fTask.stop;
					fTask = nil;
				};
				manipul.refresh;
			});
		}).play(AppClock);
		// }
	}

	onLeave {|manipul|
		// if(isActive.not)
		// {
		var fTask = fadeTask.at(manipul.name.asSymbol);
		var fAlpha = fadeAlpha.at(manipul.name.asSymbol);
		var colorFrom  = CanvasConfig.getColor(this, \over);
		var colorTo = CanvasConfig.getColor(this, \normal);
		var alphaStep = fAlpha / (fps * fadetimeLeave);

		if( fTask.notNil ) { fTask.stop; fTask = nil };
		fTask = Routine({
			(fadetimeLeave * fps + 1).do({
				manipulsColors.put(manipul.name.asSymbol, colorTo.blend(colorFrom, fAlpha));
				(1 / fps).wait;
				fAlpha = fAlpha - alphaStep;
				fadeAlpha.put(manipul.name.asSymbol, fAlpha);
				if(fAlpha <= 0) {
					fAlpha = 0;
					manipulsColors.put(manipul.name.asSymbol, colorTo);
					fTask.stop;
					fTask = nil;
				};
				manipul.refresh;
			});
		}).play(AppClock);
		// }
		// "%.onLeave".format(this).postln;
	}

	onMouseDown {|manipul, x, y, screenX, screenY, p|
		screenMouseDown = Point(screenX, screenY);
		screenOriginMouseDown = Point(parent.screenOrigin.x, parent.screenOrigin.y);
		mouseDownSize = p.size;
	}

	onMouseMove {|manipul, side, x, y, screenX, screenY, modifer|
		var deltaX = screenX - screenMouseDown.x;
		var deltaY = screenY - screenMouseDown.y;

		case
		{ side == \right } {
			parent.width_(mouseDownSize.width + deltaX);
		}
		{ side == \rightBottom } {
			parent.size_(mouseDownSize.width + deltaX, mouseDownSize.height + deltaY);
		}
		{ side == \bottom } {
			parent.height_(mouseDownSize.height + deltaY);
		}
		{ side == \leftBottom } {
			parent.screenOriginX_(screenOriginMouseDown.x + deltaX);
			parent.size_(mouseDownSize.width - deltaX, mouseDownSize.height + deltaY);
		}
		{ side == \left } {
			parent.screenOriginX_(screenOriginMouseDown.x + deltaX);
			parent.width_(mouseDownSize.width - deltaX);
		}
		{ side == \leftTop } {
			parent.screenOrigin_(screenOriginMouseDown.x + deltaX, screenOriginMouseDown.y + deltaY);
			parent.size_(mouseDownSize.width - deltaX, mouseDownSize.height - deltaY);
		}
		{ side == \top } {
			parent.screenOriginY_(screenOriginMouseDown.y + deltaY);
			parent.height_(mouseDownSize.height - deltaY);
		}
		{ side == \rightTop } {
			parent.screenOriginY_(screenOriginMouseDown.y + deltaY);
			parent.size_(mouseDownSize.width + deltaX, mouseDownSize.height - deltaY);
		};
	}

	onResize {|canvas|
		manipuls.associationsDo({|association|
			var key = association.key;
			var manipulator = association.value;

			case
			{ key == \right } {
				manipulator.origin_(parent.size.width - offset - thickness, 2*offset + thickness);
				manipulator.size_(thickness, parent.size.height - (4*offset) - (2*thickness));
			}
			{ key == \rightBottom } {
				manipulator.origin_(parent.size.width - offset - thickness, parent.size.height - offset - thickness);
				manipulator.size_(thickness, thickness);
			}
			{ key == \bottom } {
				manipulator.origin_(2*offset + thickness, parent.size.height - offset - thickness);
				manipulator.size_(parent.size.width - (4*offset) - (2*thickness), thickness);
			}
			{ key == \leftBottom } {
				manipulator.origin_(offset, parent.size.height - offset - thickness);
				manipulator.size_(thickness, thickness);
			}
			{ key == \left } {
				manipulator.origin_(offset, 2*offset + thickness);
				manipulator.size_(thickness, parent.size.height - (4*offset) - (2*thickness));
			}
			{ key == \leftTop } {
				manipulator.origin_(offset, offset);
				manipulator.size_(thickness, thickness);
			}
			{ key == \top } {
				manipulator.origin_(2*offset + thickness, offset);
				manipulator.size_(parent.size.width - (4*offset) - (2*thickness), thickness);
			}
			{ key == \rightTop } {
				manipulator.origin_(parent.size.width - offset - thickness, offset);
				manipulator.size_(thickness, thickness);
			}
		});
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


