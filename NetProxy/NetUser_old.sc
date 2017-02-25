NetUser_old {
	classvar singleton;
	classvar answerDelayLimit;

	var name;
	var netMask, netPort;
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

	*free {
		if( singleton.notNil ) {
			singleton.prSend('/user/leaved', singleton.userName);
			OSCdef(\user_connected).free;
			OSCdef(\user_isHere).free;
			OSCdef(\user_leaved).free;
			singleton = nil;
		};
		^nil
	}

	initUser {|userName|

		if(userName.isNil) { this.getNetUserName } { name = userName };
		netPort = 4002;
		thisProcess.openUDPPort(netPort);

		conditionNetMask = Condition.new();
		conditionNetProfiles = Condition.new();

		Routine.run({
			this.getNetMask;
			// "conditionNetMask hang".warn;
			conditionNetMask.hang;
			// "conditionNetMask unhang".warn;
			// "conditionNetProfiles hang".warn;
			this.getNetProfiles;
			conditionNetProfiles.hang;
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

	prSenderFilter{ |addr, fnc| if(addr.ip != this.netIP) { ^fnc.value } { ^nil } }

	prSend {|path, args| if(netProfiles.notNil) {
		netProfiles.keysDo({|addr| NetAddr(addr, netPort).sendMsg(path.asSymbol, args) })
	}}

	sendConfirmedMsg { |addr, msg|
		/*
		OSCdef.newMatching(\netMsg_confirm, {|msg, time, addr, recvPort|
			// "\user_connected % | % | % | %".format(msg, time, addr, recvPort).warn;
			this.prSenderFilter(addr, {
				var sender = msg[1];
				var net = NetAddr(addr.ip, netPort).sendMsg('/user/isHere', name);

				"Player % has joined to session".format(sender).warn;
			})
		}, msg, recvPort: port).oneShot;
		*/
		NetAddr(addr, netPort).sendMsg('/netMsg', msg);
	}

	initReciver {
		OSCdef.newMatching(\netMsg, {|msg, time, addr, recvPort|
			"\user_connected % | % | % | %".format(msg, time, addr, recvPort).warn;

		}, '/netMsg', recvPort: netPort).permanent_(true);
	}



	getNetProfiles {
		var answered = IdentityDictionary.new();

		conditionNetProfiles.unhangTimeLimit(answerDelayLimit, {
			netProfiles = answered;
			conditionNetProfiles.test = true;
			conditionNetProfiles.unhang;
		});

		OSCdef.newMatching(\user_connected, {|msg, time, addr, recvPort|
			// "\user_connected % | % | % | %".format(msg, time, addr, recvPort).warn;
			this.prSenderFilter(addr, {
				var sender = msg[1];
				var net = NetAddr(addr.ip, netPort).sendMsg('/user/isHere', name);
				"Player % has joined to session".format(sender).warn;
			})
		}, '/user/connected', recvPort: netPort).permanent_(true);

		OSCdef.newMatching(\user_isHere, {|msg, time, addr, recvPort|
			// "\user_isHere % | % | % | %".format(msg, time, addr, recvPort).warn;
			this.prSenderFilter(addr, {
				var sender = msg[1];
				answered.put(addr.ip, sender);
				"Player % is here too".format(sender).warn;
			})
		}, '/user/isHere', recvPort: netPort).permanent_(true);

		OSCdef.newMatching(\user_leaved, {|msg, time, addr, recvPort|
			// "\user_leaved % | % | % | %".format(msg, time, addr, recvPort).warn;
			this.prSenderFilter(addr, {
				var sender = msg[1];
				answered.removeAt(addr.ip);
				"Player % leaved from session".format(sender).warn;
			})
		}, '/user/leaved', recvPort: netPort).permanent_(true);

		// 230.do({|i| NetAddr(netMask.copy.put(3,i+1).join("."), netPort).sendMsg('/user/connected', name); });
		254.do({|i| NetAddr(netMask.copy.put(3,i+1).join("."), netPort).sendMsg('/user/connected', name); });
		// NetAddr("10.0.0.35", 8000).sendMsg('/user/connected', name);
	}

	*userName { if(singleton.notNil) { ^singleton.userName } { ^nil } }
	userName  { if(singleton.notNil) { ^name } { ^nil } }

	netIP { if(singleton.notNil && netMask.notNil) { ^netMask.join(".") } { ^nil } }

	printOn { |stream|	stream << this.class.name << "('" << name << "')"; }

	players {
		if(singleton.notNil) {
			"NetUser info \n\t - name: % \n\t - netIP: %".format(name, this.netIP).postln;
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


