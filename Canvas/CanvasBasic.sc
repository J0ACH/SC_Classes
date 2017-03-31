CanvasText : Canvas {

	var <string, font, color, alignment;
	var offset, position, orientation;

	*initClass {
		// "text init".warn;
		CanvasConfig.addColor(this, \background, Color.new255(20,20,20));
		CanvasConfig.addColor(this, \frame, Color.new255(190,190,190));
		CanvasConfig.addColor(this, \text, Color.new255(190,190,190));
		CanvasConfig.addFont(this, \text, Font.new("Consolas", 8, usePointSize: true));
		// CanvasConfig.addFont(this, \text, Font.new("Univers Condensed", 11, usePointSize:true));
		// CanvasConfig.addFont(this, \text, Font.new("Univers 57 Condensed", 14));
	}


	init {

		// fontType = CanvasConfig.getFont(this, \text);
		// fontSize = 14;

		string = "text";
		font = CanvasConfig.getFont(this, \text);
		color = CanvasConfig.getColor(this, \text);
		offset = 0;
		orientation = CanvasOrientation.horizontal;
		this.position = CanvasPosition.center;
		this.showFrame = true;

		this.draw({
			var rect;
			if(orientation == CanvasOrientation.horizontal)
			{ rect = Rect(offset, offset, this.width - (2*offset), this.height - (2*offset)) }
			{
				Pen.rotateDeg(-90);
				rect = Rect(this.height.neg + offset, offset, this.height - (2*offset) , this.width - (2*offset) );
			};
			Pen.stringInRect(string, rect, font, color, alignment);
		});
	}

	string_ {|txt| string = txt; this.refresh }
	position_ {|canvasPositon|
		position = canvasPositon;
		switch (canvasPositon,
			0, { alignment = QAlignment(\center) },
			1, { if(orientation == CanvasOrientation.horizontal) { alignment = QAlignment(\topLeft) } { alignment = QAlignment(\topRight) } },
			2, { if(orientation == CanvasOrientation.horizontal) { alignment = QAlignment(\top) } { alignment = QAlignment(\right) } },
			3, { if(orientation == CanvasOrientation.horizontal) { alignment = QAlignment(\topRight) } { alignment = QAlignment(\bottomRight) } },
			4, { if(orientation == CanvasOrientation.horizontal) { alignment = QAlignment(\right) } { alignment = QAlignment(\bottom) } },
			5, { if(orientation == CanvasOrientation.horizontal) { alignment = QAlignment(\bottomRight) } { alignment = QAlignment(\bottomLeft) } },
			6, { if(orientation == CanvasOrientation.horizontal) { alignment = QAlignment(\bottom) } { alignment = QAlignment(\left) } },
			7, { if(orientation == CanvasOrientation.horizontal) { alignment = QAlignment(\bottomLeft) } { alignment = QAlignment(\topLeft) } },
			8, { if(orientation == CanvasOrientation.horizontal) { alignment = QAlignment(\left) } { alignment = QAlignment(\top) } }
		);
		this.refresh;
	}
	offset_ {|val| offset = val; this.refresh }
	font_ {|val| font = val; this.refresh }
	orientation_ {|val|
		orientation = val;
		this.position = position;
		// position.postln;
		this.refresh

	}
}