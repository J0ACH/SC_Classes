NetObject {

	classvar port;
	classvar messageID;

	var <objectID;
	// var conditionMsgConfirm;
	var conditionMsgSemafor;

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

		conditionMsgSemafor = Semaphore(1);
		// conditionMsgConfirm = Semaphore(1);
		// conditionMsgConfirm = Condition.new(true);

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
			var defMsg = "MsgID_%".format(messageID).asSymbol;
			var conditionMsgConfirm = Condition.new(true);
			var cntLimit = 4;
			conditionMsgSemafor.wait;
			messageID = messageID + 1;

			OSCdef.newMatching(defMsg, {|msg, time, addr, recvPort|
				"\\NetObject_confirm % | % | % | %".format(msg, time, addr, recvPort).warn;
				conditionMsgConfirm.test = false;
			}, defMsg, recvPort: port).oneShot;

			while (
				{ conditionMsgConfirm.test },
				{
					NetAddr(addr, port).sendMsg('/NetObject', defMsg, objectID, msg, *args);
					if(cntLimit == 0) { conditionMsgConfirm.test = false };
					0.01.wait;
					if(conditionMsgConfirm.test) { cntLimit = cntLimit - 1; "resend".warn; };
				}
			);
			// 5.wait;
			"send & confirm DONE".warn;
			conditionMsgConfirm.test = true;
			conditionMsgSemafor.signal;
		});

	}
}

NetTest : NetObject {
	var semafor;

	*new { ^super.new.init }

	init {
		"init test done".postln;
		semafor = Semaphore(1);
	}

	print { |...args|
		"\nNetTest.print: ".postln;
		args.do({|one| "\t - %".format(one).postln })
	}

	semaforTest {|text|
		Routine.run({
			semafor.wait;
			5.do({|i|
				"%_%".format(text, i).postln;
				1.wait;
			});
			semafor.signal;
		})
	}
}


