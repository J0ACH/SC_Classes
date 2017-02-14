NetUser {
	classvar singleton;
	classvar answerDelayLimit;

	var name;
	var netMask;
	var netProfiles;

	var conditionNetMask, conditionNetProfiles;

	*initClass {
		singleton = nil;
		answerDelayLimit = 0.5;
	}

	*new {|userName|
		if( singleton.isNil ) { singleton = super.new.initUser(userName) };
		^singleton;
	}

	*free {	singleton = nil	}

	initUser {|userName|

		if(userName.isNil) { this.getNetUserName };

		conditionNetMask = Condition.new();
		conditionNetProfiles = Condition.new();

		Routine.run({
			this.getNetMask;
			"conditionNetMask hang".warn;
			conditionNetMask.unhangTimeLimit(answerDelayLimit, {"NetUser not found netMask".warn});
			conditionNetMask.hang;
			"conditionNetMask unhang".warn;
			"conditionNetProfiles hang".warn;
			this.getNetProfiles;
			// conditionNetProfiles.unhangTimeLimit(answerDelayLimit, { this.collectNetProfiles; "conditionNetProfiles time limit".warn});
			conditionNetProfiles.hang;
			"conditionNetProfiles unhang".warn;
			"NetUser.initUser DONE".warn;
		});
	}

	getNetUserName {
		name = Platform.case(
			\osx,       { "whoami".unixCmdGetStdOut.replace("\n", ""); },
			\linux,     { "whoami".unixCmdGetStdOut.replace("\n", ""); },
			\windows,   { "echo %username%".unixCmdGetStdOut.replace("\n", ""); }
		);
	}

	getNetMask {
		var tempBroadcast = NetAddr.broadcastFlag;
		NetAddr.broadcastFlag = true;
		OSCdef.newMatching(\msg_getMyNetIP, {|msg, time, addr, recvPort|
			netMask = addr.ip.split($.);
			"NetUser info \n\t - name: % \n\t - netIP: %".format(name, this.netIP).postln;
			conditionNetMask.test = true;
			conditionNetMask.unhang;
		},  '/user/getMyNetIP', nil).oneShot;
		NetAddr("255.255.255.255", NetAddr.langPort).sendMsg('/user/getMyNetIP');
		NetAddr.broadcastFlag = tempBroadcast;
	}

	getNetProfiles {
		var answered = Array.newClear(255).fill(false);
		conditionNetProfiles.unhangTimeLimit(answerDelayLimit, { this.collectNetProfiles(answered) });

		OSCdef.newMatching(\msg_getAnswerFromNetIP, {|msg, time, addr, recvPort|
			var answerMask = addr.ip.split($.);
			var answerAt = answerMask[3].asInt;
			answered.put(answerAt, true);
			if(answered.includes(false).not) { this.collectNetProfiles(answered) };
		}, '/user/getAnswerNetIP', nil);

		// NetAddr(netMask.put(3,14).join("."), NetAddr.langPort).sendMsg('/user/getAnswerNetIP');
		// /*
		255.do({|i|
		NetAddr(netMask.put(3,i).join("."), NetAddr.langPort).sendMsg('/user/getAnswerNetIP');
		// netMask.put(3,i).join(".").postln;
		});
		// */
	}

	collectNetProfiles {|answeredArray|
		var answeredIPs = List.new;
		answeredArray.collect({|bool, i|
			// if(bool && (i != netMask[3])) {
			if(bool) {
				answeredIPs.add( netMask.put(3,i).join("."))
			}
		});
		netProfiles = answeredIPs.array;
		netProfiles.postln;
		OSCdef(\msg_getAnswerFromNetIP).free;
		conditionNetProfiles.test = true;
		conditionNetProfiles.unhang;
	}

	*userName { if(singleton.notNil) { ^singleton.userName } { ^nil } }
	userName  { ^name }

	netIP { if(netMask.notNil) { ^netMask.join(".") } { ^nil } }

	printOn { |stream|	stream << this.class.name << "('" << name << "')"; }
}

+ Condition {
	unhangTimeLimit { |time, breakFunction = nil|
		this.test = false;
		SystemClock.sched(time, {
			if(this.test.not)
			{
				if(breakFunction.notNil) { breakFunction.value };
				this.test = true;
				this.unhang;
			};
			nil;
		});
	}
}
