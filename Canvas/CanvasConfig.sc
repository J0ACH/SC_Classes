CanvasConfig {
	classvar <config;
	classvar configPath, <configFile, <configCode;
	// classvar <colors;

	*initClass {
		configFile = nil;
		configPath = PathName.new(this.class.filenameSymbol.asString);

		config = MultiLevelIdentityDictionary.new();
	}

	*win {
		var configWinSize = Size(500,700);
		var screenSize = Window.screenBounds.size;

		var win = Canvas(
			(screenSize.width/2) - (configWinSize.width/2),
			(screenSize.height/2) - (configWinSize.height/2),
			configWinSize.width,
			configWinSize.height,
			name: "CavasConfigWin"
		)
		.showFrame_(true)
		.alpha_(0.95);

		var winSize = CanvasSize(win);
		var winMove = CanvasMove(win);

		var buttonClose = CanvasButton(configWinSize.width - 80, configWinSize.height - 35,70,25, win)
		.string_("close")
		.showFrame_(true)
		.resizeParentAction_({ buttonClose.origin_(win.width - 80, win.height - 35) })
		.mouseDownAction_({ win.close });

		var buttonSave = CanvasButton(configWinSize.width - 160, configWinSize.height - 35,70,25, win)
		.string_("save")
		.holdState_(false)
		.showFrame_(true)
		.resizeParentAction_({ buttonSave.origin_(win.width - 160, win.height - 35) })
		.mouseDownAction_({ this.writeConfig });

		var text = CanvasText(30,130,100,30,win).string_("begin");
		var text2 = CanvasText(30,830,100,30,win).string_("end");

		^win;
	}

	*path {	^PathName.new(configPath.pathOnly) }

	*files {
		this.path.files.do({|path|
			path.fileName.postln;
		});
		// ^this.path.files.postln;
		Canvas.allSubclasses.postln;
		Canvas.findMethod("initConfig").postln;
	}

	*createConfigFile {
		var configFileName = "Canvas_ConfigFile.scd";
		var dir = this.path +/+ configFileName;

		if(File.exists(dir.fullPath).not) {
			"% at path % NOT exist".format(configFileName, this.path.fullPath).postln;
			// configFile = File.new(dir.fullPath, "w");
			// configFile.write(this.defaultConfig);
		}
		{
			"% at path % exist".format(configFileName, this.path.fullPath).postln;
			// configFile = File.open(dir.fullPath, "r");
		};
		// configCode = configFile.readAllString;
		// configFile.close;
	}

	*writeConfig {
		var lines = "";

		if(configFile.isNil) { this.createConfigFile };
		/*
		colors.associationsDo({|oneAssoc|
		lines = lines ++ "'%' -> %;\n".format(oneAssoc.key, oneAssoc.value.asCompileString);
		});

		configFile.write(lines);
		configFile.close;
		^lines;
		*/
	}

	*readConfig {
		var configFileName = "Canvas_ConfigFile.scd";
		var dir = this.path +/+ configFileName;
		var lines;

		if(configFile.isNil) { this.createConfigFile };
		// if(File.exists(dir.fullPath)) {
		// configFile = File.open(dir.fullPath, "r");
		lines = configFile.readAllString.split($\n);
		lines.do({|oneLine|
			var answ = oneLine.interpret;
			if(answ.isKindOf(Association))
			{
				case
				{ answ.value.isKindOf(Color) } { this.addColor(answ.key, answ.value) };
			};
		});
		configFile.close;
		// }
	}

	*addItem { |canvasObject, type, key, value|
		var objName = canvasObject.class.asString.replace("Meta_", "");
		config.put(objName.asSymbol, type.asSymbol, key.asSymbol, value);
	}
	*getItem { |canvasObject, type, key|
		var objName = canvasObject.class.asString.replace("Meta_", "");
		^config.at(objName.asSymbol, type.asSymbol, key.asSymbol)
	}

	*addColor {|canvasObject, key, color| this.addItem(canvasObject, \colors, key.asSymbol, color) }
	*getColor {|canvasObject, key| ^this.getItem(canvasObject, \colors, key.asSymbol) }

	*addFont {|canvasObject, key, font| this.addItem(canvasObject, \fonts, key.asSymbol, font) }
	*getFont {|canvasObject, key| ^this.getItem(canvasObject, \fonts, key.asSymbol) }

	*print { config.postTree }

}