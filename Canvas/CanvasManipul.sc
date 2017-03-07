CanvasMove : Canvas {

	var mouseDownPosition;

	*new { |parent|	^super.new(5, 5, parent.width-10, 20, parent).init(parent) }

	init { |parent|
		this.parent_(parent);
		this.background_(150,30,30);
		this.name = "CanvasMove";
	}

	onMouseDown {|name, x, y| mouseDownPosition = Point(x, y) }

	onMouseMove {|view, x, y, modifer|
		var ptX, ptY;
		case
		{ this.parent.view.isKindOf(TopView) } {
			ptX = this.screenOrigin.x + x - mouseDownPosition.x;
			ptY = this.screenOrigin.y + y - mouseDownPosition.y;
		}
		{ this.parent.view.isKindOf(UserView) } {
			ptX = this.parent.origin.x + x - mouseDownPosition.x;
			ptY = this.parent.origin.y + y - mouseDownPosition.y;
		};
		this.parent.origin_(ptX, ptY);
	}
}


CanvasSize {
	var parent;
	var mouseDownPosition;
	var manipuls;

	*new { |parent ... positions|
		var instance = super.new();
		var size = parent.size;
		var offset = 5;
		var thickness = 20;

		positions.do({|onePosition|
			case
			{ onePosition == \right } {
				instance.init(parent,
					size.width - offset - thickness,
					2*offset + thickness,
					thickness,
					size.height - (4*offset) - (2*thickness)
				)
			}
			{ onePosition == \bottom } {
				instance.init(parent,
					2*offset + thickness,
					size.height - offset - thickness,
					size.width - (4*offset) - (2*thickness),
					thickness
				)
			};
		})
		^instance;
	}

	*right { ^\right }
	*bottom { ^\bottom }

	init { |p, x, y, w, h|
		var oneManipul = Canvas(x, y, w, h, p);
		oneManipul.view.addAction({|v, x, y| this.onMouseDown(oneManipul, x, y) }, \mouseDownAction);
		oneManipul.view.addAction({|v, x, y, modifer| this.onMouseMove(oneManipul, x, y, modifer) }, \mouseMoveAction);
		oneManipul.background_(150,30,30);
		parent = p;

		parent.view.addAction({|v| this.onResize(parent) }, \onResize);
	}

	onMouseDown {|name, x, y| mouseDownPosition = Point(x, y) }
	onMouseMove {|manipul, x, y, modifer|
		var ptX, ptY;
		case
		{ parent.view.isKindOf(TopView) } {
			ptX = manipul.screenOrigin.x + x - mouseDownPosition.x;
			ptY = manipul.screenOrigin.y + y - mouseDownPosition.y;
		}
		{ parent.view.isKindOf(UserView) } {
			ptX = parent.origin.x + x - mouseDownPosition.x;
			ptY = parent.origin.y + y - mouseDownPosition.y;
		};

		parent.width_(ptX);
		parent.height_(ptY);
	}

	onResize {|canvas|
		"%.onResize [w:%, h:%]".format(canvas, canvas.width, canvas.height).postln;
	}
}
