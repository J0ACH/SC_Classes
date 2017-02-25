NetUser : NetObject {

	classvar singleton;
	classvar answerDelayLimit;

	var name;
	var netMask, netPort;
	var <netProfiles;

	var conditionNetMask, conditionNetProfiles;

	*initClass {
		singleton = nil;
		answerDelayLimit = 1;
	}

	*new {|userName|
		if( singleton.isNil ) { singleton = super.new.initUser(userName) };
		^singleton;
	}

	initUser {|userName|

		if(userName.isNil) { this.getNetUserName } { name = userName };

		conditionNetMask = Condition.new();
		conditionNetProfiles = Condition.new();

		netProfiles = IdentityDictionary.new;

		Routine.run({
			this.getNetMask;
			// "conditionNetMask hang".warn;
			conditionNetMask.hang;
			// "conditionNetMask unhang".warn;
			// "conditionNetProfiles hang".warn;
			"NetUser scan network for other players".warn;
			this.getNetProfiles;

			conditionNetProfiles.hang;
			"NetUser scan network done".warn;
			this.players;
			// "conditionNetProfiles unhang".warn;
			// "NetUser.initUser DONE".warn;
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

		conditionNetMask.unhangTimeLimit(answerDelayLimit, {"NetUser not found netMask".warn});

		OSCdef.newMatching(\user_getMyNetIP, {|msg, time, addr, recvPort|
			netMask = addr.ip.split($.);
			this.players;
			conditionNetMask.test = true;
			conditionNetMask.unhang;
		},  '/user/getMyNetIP', nil).oneShot;
		NetAddr("255.255.255.255", NetAddr.langPort).sendMsg('/user/getMyNetIP');
		NetAddr.broadcastFlag = tempBroadcast;
	}

	getNetProfiles {

		// var answeredProfiles = IdentityDictionary.new();
		// var answeredProfiles = 0;
		var upperLimitIP = 230;
		upperLimitIP.do({|i|
			// this.sendMsg(netMask.copy.put(3,i+1).join("."),	\netUserConnected, this.netIP.asString,	name );
			this.sendMsgFunction(
				{|addr, sender|
					// "Player % is here too".format(addr).warn;
					// answeredProfiles.put(addr.asSymbol, sender);
				},
				{|addr, sender, msg, args|
					// "failed addr:% fnc: %".format(addr, msg).postln;
					// addr.asString.split($.)[3].postln;
					if( addr.asString.split($.)[3].asInt == upperLimitIP) {
						// "done".warn;
						conditionNetProfiles.test = true;
						conditionNetProfiles.unhang;
					}
				},
				netMask.copy.put(3,i+1).join("."),	\netUserConnected, this.netIP.asString,	name );
			// (i+1).postln;
		});
/*
		conditionNetProfiles.unhangTimeLimit(answerDelayLimit, {
			// netProfiles = answeredProfiles;

			conditionNetProfiles.test = true;
			conditionNetProfiles.unhang;
		});
		*/
	}

	netIP { if(singleton.notNil && netMask.notNil) { ^netMask.join(".") } { ^nil } }

	netUserConnected {|addr, sender|
		"Player % has joined to session".format(sender).warn;
		this.sendMsg(addr, \netUserIsHere, this.netIP.asString, name);
		this.addPlayer(addr, sender);
	}

	netUserIsHere {|addr, sender|
		"Player % is here too".format(sender).warn;
		// answeredProfiles.put(addr.asSymbol, sender);
		this.addPlayer(addr, sender);
	}

	addPlayer {|addr, sender|
		netProfiles.put(addr.asSymbol, sender);

	}

	printOn { |stream|	stream << this.class.name << "('" << name << "')"; }

	players {
		if(singleton.notNil) {
			"NetUser info \n\t - name: % \n\t - netIP: % \n\t - objID: %".format(name, this.netIP, this.objectID).postln;
			if(netProfiles.notNil)
			{
				"\n\nother profiles:".postln;
				netProfiles.sortedKeysValuesDo({|playerNet, playerName|
					("\t- name:" + playerName).postln;
					("\t- addr:" + playerNet + "\n").postln;
				});
			}
		} { ^nil }
	}
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