NetUser {
	classvar singleton;
	classvar answerDelayLimit;

	var name;
	var netMask;

	var conditionNetMask;

	*initClass {
		singleton = nil;
		answerDelayLimit = 2;
	}

	*new {|userName|
		if( singleton.isNil ) { singleton = super.new.initUser(userName) };
		^singleton;
	}

	*free {	singleton = nil	}

	initUser {|userName|

		if(userName.notNil)
		{ name = userName }
		{
			name = Platform.case(
				\osx,       { "whoami".unixCmdGetStdOut.replace("\n", ""); },
				\linux,     { "whoami".unixCmdGetStdOut.replace("\n", ""); },
				\windows,   { "echo %username%".unixCmdGetStdOut.replace("\n", ""); }
			);
		};

		conditionNetMask = Condition.new();
		Routine.run({
			this.getUserIP;
			"conditionNetMask hang".warn;
			conditionNetMask.unhangTimeLimit(answerDelayLimit, {"NetUser not found netMask".warn});
			conditionNetMask.hang;
			"conditionNetMask unhang".warn;
			// this.scanBroadcast;
		});
	}

	getUserIP {
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

	scanBroadcast {
		OSCdef.newMatching(\msg_getAnswerFromNetIP, {|msg, time, addr, recvPort|
			"zprava dorazila [%,%,%,%]".format(msg, time, addr, recvPort).warn;
		}, '/user/getAnswerNetIP', nil);

		20.do({|i|
			NetAddr(netMask.put(3,i).join("."), NetAddr.langPort).sendMsg('/user/getAnswerNetIP');
			netMask.put(3,i).join(".").postln;
		});
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