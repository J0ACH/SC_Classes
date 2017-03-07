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
			this.parent.origin_(ptX, ptY);
		}
		{ this.parent.view.isKindOf(UserView) } {
			ptX = this.parent.origin.x + x - mouseDownPosition.x;
			ptY = this.parent.origin.y + y - mouseDownPosition.y;

		};
		this.parent.origin_(ptX, ptY);
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
