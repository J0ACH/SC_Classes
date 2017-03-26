CanvasText : Canvas {

	var <string;

	*initClass {
		// "text init".warn;
		CanvasConfig.addColor(this, \background, Color.new255(220,20,20));
		CanvasConfig.addColor(this, \frame, Color.new255(190,190,190));
		CanvasConfig.addColor(this, \text, Color.new255(10,190,10));
	}

	init {
		string = "text";

		this.draw({
			var rect = Rect(0,0, this.width, this.height);
			Pen.color_( CanvasConfig.getColor(this, \text) );
			Pen.stringCenteredIn(string, rect);
		});
	}

	string_ {|txt| string = txt; this.refresh }

}