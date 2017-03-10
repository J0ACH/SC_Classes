CanvasMove : Canvas {

	var screenMouseDown, screenOriginMouseDown;
	var offset = 5;
	var thickness = 20;

	*new { |parent|	^super.new(0, 0, 50, 50, parent).init(parent) }

	init { |p|
		this.parent_(p);
		this.background_(150,30,130);
		this.name = "CanvasMove";
		this.parent.view.addAction({|v| this.onResize(this.parent) }, \onResize);
		this.onResize(p);
	}

	onMouseDown {|canvas, x, y, screenX, screenY|
		// screenMouseDown = QtGUI.cursorPosition;
		screenMouseDown = Point(screenX, screenY);
		screenOriginMouseDown = Point(this.parent.screenOrigin.x, this.parent.screenOrigin.y);
	}

	onMouseMove {|canvas, x, y, screenX, screenY, modifer|
		// var mouse = QtGUI.cursorPosition;
		// var deltaX = mouse.x - screenMouseDown.x;
		// var deltaY = mouse.y - screenMouseDown.y;
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
	var mouseDownPosition, mouseDownSize;
	var manipuls;

	*new { |parent ... positions| ^super.new().init(parent)	}

	*right { ^\right }
	*bottom { ^\bottom }

	init { |p|
		var sideKeys = [\right, \rightBottom, \bottom, \leftBottom, \left];

		parent = p;
		manipuls = IdentityDictionary.new();

		parent.view.addAction({|v| this.onResize(parent) }, \onResize);

		sideKeys.do({|side|
			var oneManipul = Canvas(0, 0, 50, 50, parent);
			oneManipul.name = "CanvasSize_%".format(side);
			oneManipul.background_(150,30,30);
			oneManipul.view.addAction({|v, x, y| this.onMouseDown(oneManipul, x, y, parent) }, \mouseDownAction);
			oneManipul.view.addAction({|v, x, y, modifer| this.onMouseMove(oneManipul, side, x, y, modifer) }, \mouseMoveAction);
			manipuls.put(side.asSymbol, oneManipul);
		});

		this.onResize(parent);
	}

	onMouseDown {|manipul, x, y, p|
		/*
		case
		{ parent.view.isKindOf(TopView) } {
		mouseDownPosition = Point(manipul.width + offset - x, manipul.height + offset - y);
		}
		{ parent.view.isKindOf(UserView) } {
		mouseDownPosition = Point(x,y)
		};
		*/
		mouseDownPosition = Point(x + (2*offset) + thickness, y+ (2*offset) + thickness);
		mouseDownSize = p.size;
	}
	onMouseMove {|manipul, side, x, y, modifer|
		// var ptX, ptY;
		var ptX = manipul.origin.x + x + mouseDownPosition.x;
		var ptY = manipul.origin.y + y + mouseDownPosition.y;

		"%.onMouseMove [x:%, y:%]".format(manipul, x, y).postln;

		case
		{ side == \right } {
			// ptX = manipul.origin.x + x + mouseDownPosition.x;
			parent.width_(ptX);
		}
		{ side == \rightBottom } {
			// ptX = manipul.origin.x + x + mouseDownPosition.x;
			// ptY = manipul.origin.y + y + mouseDownPosition.y;
			parent.size_(ptX, ptY);
		}
		{ side == \bottom } {
			// ptY = manipul.origin.y + y + mouseDownPosition.y;
			parent.height_(ptY);
		}
		{ side == \left } {
			parent.width_(mouseDownSize.width - ptX);

			case
			{ parent.view.isKindOf(TopView) } {
				mouseDownPosition = Point(manipul.width + offset - x, manipul.height + offset - y);
			}
			{ parent.view.isKindOf(UserView) } {
				mouseDownPosition = Point(x,y)
			};

			// parent.origin_(mouseDownPosition.x - ptX);
			// parent.originX_(ptX);
			/*
			case
			{ parent.view.isKindOf(TopView) } {
			ptX = parent.screenOrigin.x + x - mouseDownPosition.x;
			// parent.origin_(ptX, parent.screenOrigin.y)
			}
			{ parent.view.isKindOf(UserView) } {
			ptX = manipul.origin.x + x + mouseDownPosition.x;
			// parent.origin_(ptX, mouseDownPosition.y)
			};
			*/
			// parent.origin_(ptX, ptY);
			// ptY = manipul.origin.y + y + mouseDownPosition.y;
			// parent.origin_(ptX, parent.screenOrigin.y)
		};
	}

	onResize {|canvas|
		// "%.onResize [w:%, h:%]".format(canvas, canvas.width, canvas.height).postln;
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
		});
	}
}
