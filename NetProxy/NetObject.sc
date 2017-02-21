NetObject {

	classvar port;
	classvar messageID;

	var <objectID;
	var conditionMsgConfirm;

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

			this.class.postln;
			this.tryPerform(msgType, *args);

			NetAddr(addr.ip, recvPort).sendMsg(msgID.asSymbol);

		}, '/NetObject', recvPort: port).permanent_(true);
	}

	sendMsg { |addr, msg ... args|
		Routine.run({
			var defName = "NetObject_confirm_%".format(messageID).asSymbol;
			var defMsg = "MsgID_%".format(messageID).asSymbol;
			conditionMsgConfirm = Condition.new();
			conditionMsgConfirm.unhangTimeLimit(0.1, { "resend %".format(defMsg).warn; this.sendMsg(addr, msg, *args) });

			OSCdef.newMatching(defName, {|msg, time, addr, recvPort|
				"\\NetObject_confirm % | % | % | %".format(msg, time, addr, recvPort).warn;
				conditionMsgConfirm.test = true;
				conditionMsgConfirm.unhang;
			}, defMsg, recvPort: port).oneShot;


			NetAddr(addr, port).sendMsg('/NetObject', defMsg, objectID, msg, *args);

			messageID = messageID + 1;
			conditionMsgConfirm.hang;
			// thisThread.stop;
			// "send & confirm DONE".warn;
		});
	}
}

NetTest : NetObject {

	*new { ^super.new.init }

	init { "init test done".postln }

	print { |...args|
		"\nNetTest.print: ".postln;
		args.do({|one| "\t - %".format(one).postln })
	}
}
