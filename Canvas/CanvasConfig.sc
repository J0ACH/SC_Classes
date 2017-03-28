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
		);
		var winSize = CanvasSize(win);
		var winMove = CanvasMove(win);
		var buttonClose = CanvasButton(configWinSize.width - 80, configWinSize.height - 35,70,25, win);
		var buttonSave = CanvasButton(configWinSize.width - 160, configWinSize.height - 35,70,25, win);
		var text = CanvasText(30,130,100,30,win);

		win.hasFrame = true;

		buttonClose.resizeParentAction_({ buttonClose.origin_(win.width - 80, win.height - 35) });
		buttonSave.resizeParentAction_({ buttonSave.origin_(win.width - 160, win.height - 35) });

		buttonClose.string = "Close";
		buttonClose.hasFrame = true;
		buttonClose.mouseDownAction = { win.close };

		buttonSave.string = "Save";
		buttonSave.hasFrame = true;
		buttonSave.mouseDownAction = { this.writeConfig };

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

	*addColor {|canvasObject, key, color|
		var objName = canvasObject.class.asString.replace("Meta_", "");
		objName = objName.replace("Meta_", "");
		// "CanvasConfig.addColor[%, %] object: %".format(objName.asSymbol, key.asSymbol, color).postln;
		config.put(objName.asSymbol, \colors, key.asSymbol, color);
	}

	*getColor {|canvasObject, key|
		var objName = canvasObject.class.asString.replace("Meta_", "");
		// "CanvasConfig.getColor[%, %] object: %".format(objName.asSymbol, key.asSymbol,  config.at(objName.asSymbol, key.asSymbol)).postln;
		^config.at(objName.asSymbol, \colors, key.asSymbol)
	}

	// *background_ { |canvasObject, color| this.addColor(canvasObject, \background, color) }
	// *background { |canvasObject| ^this.getColor(canvasObject, \background) }

	*print { config.postTree }

}