CanvasText : Canvas {

	var <string;

	*initClass {
		"text init".warn;

		CanvasConfig.addColor(this, \background, Color.new255(130,50,30));
		CanvasConfig.addColor(this, \text, Color.new255(190,190,190));
	}

	init {
		string = "text";

		// this.background_(CanvasConfig.getColor(this, \background));


		this.draw({
			var rect = Rect(0,0, this.width, this.height);
			Pen.color_(CanvasConfig.getColor(this, \text));
			Pen.strokeRect(rect);
			Pen.stringCenteredIn(string, rect);
		});
	}

	string_ {|txt| string = txt; this.refresh; }

}