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
		.alpha_(0.85);

		var winMove = CanvasMove(win).string_("Canvas configuration");
		var winSize = CanvasSize(win);

		var buttonClose = CanvasButton(configWinSize.width - 80, configWinSize.height - 35,70,25, win)
		.name_("ConfigButtonClose")
		.string_("close")
		.showFrame_(true)
		.add_onParentResize(\config, {|parentCanvas| buttonClose.origin_(parentCanvas.width - 80, parentCanvas.height - 35) })
		.add_onMouseDown(\config, { win.close });

		var buttonSave = CanvasButton(configWinSize.width - 160, configWinSize.height - 35,70,25, win)
		.name_("ConfigButtonSave")
		.string_("save")
		.holdState_(false)
		.showFrame_(true)
		.add_onParentResize(\config, {|parentCanvas| buttonSave.origin_(parentCanvas.width - 160, parentCanvas.height - 35) })
		.add_onMouseDown(\config, { this.writeConfig });

		var text = CanvasText(30,130,100,30,win).string_("begin");
		var text2 = CanvasText(30,830,100,30,win).string_("end");

		// winMove.add_onParentResize(\test, {"config move parent resize".warn});
		// winSize.add_onParentResize(\test, {"config size parent resize".warn});
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