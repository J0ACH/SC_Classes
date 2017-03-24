CanvasConfig {
	classvar <config2;
	classvar configPath, <configFile, <configCode;
	// classvar <colors;

	*initClass {
		configFile = nil;
		configPath = PathName.new(this.class.filenameSymbol.asString);
		// colors = IdentityDictionary.new();

		config2 = MultiLevelIdentityDictionary.new();
		"config init".warn;
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
		var buttonClose = CanvasButton(configWinSize.width - 80, configWinSize.height - 35,70,25, win);
		var buttonSave = CanvasButton(configWinSize.width - 160, configWinSize.height - 35,70,25, win);
		var text = CanvasText(30,30,100,30,win);

		win.view.addAction({|v|
			buttonClose.origin_(win.width - 80, win.height - 35);
			buttonSave.origin_(win.width - 160, win.height - 35);
		}, \onResize);

		buttonClose.string = "Close";
		buttonClose.mouseDownAction = { win.close };

		buttonSave.string = "Save";
		buttonSave.mouseDownAction = { this.writeConfig };
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
		var objName = canvasObject.class.asString;
		"CanvasConfig.addColor object: %".format(objName).postln;
		config2.put(canvasObject.class.asSymbol, key.asSymbol, color);
		// colors.put(key.asSymbol, color);
		// "%.addColor % -> %".format(this, key, color).postln;
	}

	*getColor {|canvasObject, key|
		var objName = canvasObject.class.asString;
		// objName = objName.
		"CanvasConfig.getColor object: %".format(objName).postln;
		^config2.at(canvasObject.class.asSymbol, key.asSymbol)
	}

	*print { config2.postTree }

}