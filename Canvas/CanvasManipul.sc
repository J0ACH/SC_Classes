CanvasMove : Canvas {

	var mouseDownPosition, mouseDownOrigin;

	*new { |parent|	^super.new(5, 5, parent.width-10, 30, parent).init(parent) }

	init { |parent|
		this.parent_(parent);
		this.background_(150,30,30);
		this.name = "CanvasMove";
	}

	onMouseDown {|name, x, y|
		mouseDownPosition = Point(x, y) ;
		mouseDownOrigin = this.origin;
	}

	onMouseMove {|view, x, y, modifer|
		var ptX, ptY, rect;
		"mouse move Canvas('%') [x:%,y:%, mod:%]".format(view, x, y, modifer).postln;
		case
		{ this.parent.isKindOf(TopView) } {
			ptX = this.screenOrigin.x + x - mouseDownPosition.x;
			ptY = this.screenOrigin.y + y - mouseDownPosition.y;
			rect = Rect(ptX, ptY, this.parent.bounds.width, this.parent.bounds.height);
			this.parent.findWindow.bounds_(Window.flipY(rect))
		}
		{ this.parent.isKindOf(UserView) } {
			ptX = this.origin.x + x - mouseDownOrigin.x;
			ptY = this.origin.y + y - mouseDownOrigin.y;
			rect = Rect(ptX, ptY, this.parent.bounds.width, this.parent.bounds.height);
			this.parent.bounds_(rect);
			// this.parent.origin_(ptX, ptY);
		};
		"screen [x:%,y:%]".format(ptX, ptY).postln;
	}
}


CanvasSize {
	*new { |parent|
		var boarderManipul = Array.new(3);
		super.new(0, 0, parent.width, parent.height, parent).init(parent)
		^boarderManipul;
	}

	init { |parent|
		this.parent_(parent);
		this.background_(150,30,30);
	}
}
