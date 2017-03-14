CanvasConfig {
	classvar configPath, <configFile, <configCode;
	classvar <colors;

	*initClass {
		configFile = nil;
		configPath = PathName.new(this.class.filenameSymbol.asString);
		colors = IdentityDictionary.new();
	}

	*path {	^PathName.new(configPath.pathOnly) }

	*files {
		this.path.files.do({|path|
			path.fileName.postln;
		})
		// ^this.path.files.postln;
	}

	*createConfigFile {
		var configFileName = "Canvas_ConfigFile.scd";
		var dir = this.path +/+ configFileName;

		if(File.exists(dir.fullPath).not) {
			"% at path % NOT exist".format(configFileName, this.path.fullPath).postln;
			configFile = File.new(dir.fullPath, "w");
			configFile.write(this.defaultConfig);
		}
		{
			"% at path % exist".format(configFileName, this.path.fullPath).postln;
			configFile = File.open(dir.fullPath, "r");
		};
		// configCode = configFile.readAllString;
		// configFile.close;
	}

	*writeConfig {
		var lines = "";

		if(configFile.isNil) { this.createConfigFile };

		colors.associationsDo({|oneAssoc|
			lines = lines ++ "'%' -> %;\n".format(oneAssoc.key, oneAssoc.value.asCompileString);
		});

		configFile.write(lines);
		configFile.close;
		^lines;
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

	*addColor {|key, color|
		colors.put(key.asSymbol, color);
		"%.addColor % -> %".format(this, key, color).postln;
	}

}