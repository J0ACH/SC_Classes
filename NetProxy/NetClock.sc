NetClock {
	classvar singleton;

	var <date;
	var <clock;

	*initClass { singleton = nil }

	*new {
		if( singleton.isNil ) { singleton = super.new.inti };
		^singleton;
	}

	*free {
		singleton.clock.stop;
		singleton = nil;
	}

	inti {
		date = Date.getDate;
		this.setEpochClock(date.rawSeconds);
		this.formatDate(date.rawSeconds, 1);
	}

	setEpochClock {|epochTimeStamp|
		singleton.postln;
		if(singleton.notNil) { clock.stop };
		clock = TempoClock.new(1, epochTimeStamp);
		clock.permanent = true;

		TempoClock.default = clock.copy;

		"NetClock creation epochTime % [date: %]".format(date.rawSeconds, date).postln;
		clock.sched(0, {
			"NetClock epochTime tick % [%]".format(clock.beats, clock.elapsedBeats).postln;
			1;
		});
	}

	beats { if(singleton.notNil) { ^clock.beats } { ^nil } }

	formatDate {|rawSec, gmtOffset = 1| // gmtOffset = 1 -> zimniCas, gmtOffset = 2 -> letniCas
		var year = 1970;
		var month;
		var day = rawSec div: 86400;
		var monthDayCnt = [31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31];
		var date;

		while({day >= 0},
			{
				var yearDayCnt;
				var prestupnyRok = (mod((year - 1968),4) == 0).if( {true}, {false} );
				prestupnyRok.if(
					{monthDayCnt[1] = 29},
					{monthDayCnt[1] = 28}
				);
				yearDayCnt = monthDayCnt.sum;
				(day < yearDayCnt).if({
					month = 0;
					while({day >= 0},
						{
							var hour, minute, sec, msec;
							(day < monthDayCnt[month]).if({
								hour = (((rawSec/3600)%24)+gmtOffset).floor;
								minute = ((rawSec/60)%60).floor;
								sec = (rawSec%60).floor;
								msec = rawSec % 1;
								date = [year, month+1, day+1, hour, minute, sec, msec];
							});
							day = day - monthDayCnt[month];
							month = month + 1;
						}
					);
				});
				day = day - yearDayCnt;
				year = year + 1;
			}
		);
		date.postln;
		^date;
	}

	*getSystemDate {
		^Platform.case(
			\osx,       { "whoami".unixCmdGetStdOut.replace("\n", ""); },
			\linux,     { "whoami".unixCmdGetStdOut.replace("\n", ""); },
			\windows,   { "echo %date%".unixCmdGetStdOut.replace("\n", ""); }
		);
	}

	*getSystemTime {
		^Platform.case(
			\osx,       { "whoami".unixCmdGetStdOut.replace("\n", ""); },
			\linux,     { "whoami".unixCmdGetStdOut.replace("\n", ""); },
			\windows,   { "echo %time%".unixCmdGetStdOut.replace("\n", ""); }
		);
	}

	*syncSystemClock {
			^Platform.case(
			\osx,       { "whoami".unixCmdGetStdOut.replace("\n", ""); },
			\linux,     { "whoami".unixCmdGetStdOut.replace("\n", ""); },
			// \windows,   { "w32tm /resync".unixCmdGetStdOut; }
			// \windows,   { "w32tm /stripchart /computer:time-b.nist.gov /samples:5 /dataonly".unixCmdGetStdOut;}
			\windows,   { "w32tm /stripchart /computer:cz.pool.ntp.org /samples:5 /dataonly".unixCmdGetStdOut;}
		);
	}


	*getTimeZone {
			^Platform.case(
			\osx,       { "whoami".unixCmdGetStdOut.replace("\n", ""); },
			\linux,     { "whoami".unixCmdGetStdOut.replace("\n", ""); },
			// \windows,   { "w32tm /resync".unixCmdGetStdOut; }
			// \windows,   { "w32tm /stripchart /computer:time-b.nist.gov /samples:5 /dataonly".unixCmdGetStdOut;}
			\windows,   { "w32tm /tz".unixCmdGetStdOut;}
		);
	}

}