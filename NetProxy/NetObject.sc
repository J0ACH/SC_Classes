NetObject {

	classvar port;
	classvar messageID;

	var <objectID;
	var sendMessageSemaphor;

	*initClass {
		port = 4002;
		messageID = 0;
	}

	*new { |id = nil|
		^super.new.initObject(id);
	}

	initObject { |id|
		thisProcess.openUDPPort(port);
		objectID = id ? this.identityHash;
		sendMessageSemaphor = Semaphore(1);

		OSCdef.newMatching(\NetObject, {|msg, time, addr, recvPort|
			var msgID = msg[1];
			var objID = msg[2];
			var msgType = msg[3];
			var args = msg[4..msg.size-1];
			"recived NetObject msg:  % | % | % | %".format(msg, time, addr, recvPort).warn;
			(
				"\nNetObject recived msg info " +
				"\n\t - msgID: %" +
				"\n\t - objectID: %" +
				"\n\t - msgType: %" +
				"\n\t - args: %"
			).format(msgID, objectID, msgType, args).postln;

			// this.class.postln;
			this.tryPerform(msgType, *args);
			// {
			// 0.05.wait;
			NetAddr(addr.ip, recvPort).sendMsg(msgID.asSymbol);
			// }.fork;

		}, '/NetObject', recvPort: port).permanent_(true);
	}

	sendMsg { |addr, msg ... args|
		{
			var defMsg = "MsgID_%".format(messageID).asSymbol;
			var conditionMsgConfirm = Condition.new(true);
			var cntLimit = 10;
			sendMessageSemaphor.wait;

			OSCdef.newMatching(defMsg, {|msg, time, addr, recvPort|
				"\\NetObject_confirm % | % | % | %".format(msg, time, addr, recvPort).warn;
				conditionMsgConfirm.test = false;
			}, defMsg, recvPort: port).oneShot;

			while (
				{ conditionMsgConfirm.test },
				{
					NetAddr(addr, port).sendMsg('/NetObject', defMsg, objectID, msg, *args);
					if(cntLimit == 0) { conditionMsgConfirm.test = false };
					if(conditionMsgConfirm.test) {
						0.01.wait;
						cntLimit = cntLimit - 1;
					};
				}
			);

			"send & confirm DONE".warn;
			conditionMsgConfirm.test = true;
			sendMessageSemaphor.signal;
			messageID = messageID + 1;
		}.fork;
	}

	sendMethodCall { |selector ... args|
		// thisMethod.postln;
		// var call = thisProcess.interpreter.cmdLine;
		this.class.instVarNames.postln;
		this.sendMsg("172.27.1.14", selector.asSymbol, *args);
	}
}

NetTest : NetObject {
	var <val;

	*new { ^super.new.init }

	init {
		"init test done".postln;
	}

	print { |...args|
		"\nNetTest.print: ".postln;
		args.do({|one| "\t - %".format(one).postln })
	}

	setVal {|newVal|
		val = newVal;
		// this.sendMethodCall(thisMethod, newVal + 10);
		this.sendMethodCall(\lateSetVal, newVal + 10);
	}

	lateSetVal {|newVal|
		val = newVal;
	}
}


