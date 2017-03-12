CanvasMove : Canvas {

	var screenMouseDown, screenOriginMouseDown;
	var offset = 5;
	var thickness = 20;

	*new { |parent|	^super.new(0, 0, 50, 50, parent).init(parent) }

	init { |p|
		this.parent_(p);
		// this.background_(Color.new255(150,30,130));
		this.color255_(\background, 150,30,30);
		this.name = "CanvasMove";
		this.parent.view.addAction({|v| this.onResize(this.parent) }, \onResize);
		this.onResize(p);
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

	onResize {|canvas|
		this.origin_(2*offset + thickness, 2*offset + thickness);
		this.size_(this.parent.width - (4*offset) - (2*thickness), thickness);
	}
}

CanvasSize {
	classvar <offset = 5;
	classvar <thickness = 20;

	var parent;
	var manipuls;
	var screenMouseDown, screenOriginMouseDown, mouseDownSize;

	*new { |parent ... positions| ^super.new().init(parent)	}

	// *right { ^\right }
	// *bottom { ^\bottom }

	init { |p|
		var sideKeys = [\right, \rightBottom, \bottom, \leftBottom, \left, \leftTop, \top, \rightTop];

		parent = p;
		manipuls = IdentityDictionary.new();

		parent.view.addAction({|v| this.onResize(parent) }, \onResize);

		sideKeys.do({|side|
			var oneManipul = Canvas(0, 0, 50, 50, parent);
			oneManipul.name = "CanvasSize_%".format(side);
			// oneManipul.background = Color.new255(150,30,30);
			oneManipul.color255_(\background, 150,30,30);

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
		this.background_(255,0,0);
		this.string = "X";
		this.mouseDownAction = { parent.close };
		// this.refresh;
	}
}


