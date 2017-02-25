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
			if(objID != objectID)
			{
				// "recived NetObject msg:  % | % | % | %".format(msg, time, addr, recvPort).warn;
				(
					"\nNetObject recived msg info " +
					"\n\t - msgID: %" +
					"\n\t - objectID: %" +
					"\n\t - msgType: %" +
					"\n\t - args: %"
				).format(msgID, objID, msgType, args).postln;

				this.tryPerform(msgType, *args);
				NetAddr(addr.ip, recvPort).sendMsg(msgID.asSymbol);
			}
		}, '/NetObject', recvPort: port).permanent_(true);
	}

	sendMsgFunction {|onSuccessFnc, onFailedFnc, addr, msg ... args|
		{
			var defMsg = "MsgID_%".format(messageID).asSymbol;
			var conditionMsgConfirm = Condition.new(true);
			var cntLimit = 4;
			sendMessageSemaphor.wait;

			OSCdef.newMatching(defMsg, {|msg, time, addr, recvPort|
				"\\NetObject_confirm % | % | % | %".format(msg, time, addr, recvPort).postln;
				if(onSuccessFnc.notNil) { onSuccessFnc.value(addr, msg, *args) };
				conditionMsgConfirm.test = false;
			}, defMsg, recvPort: port).oneShot;

			while (
				{ conditionMsgConfirm.test },
				{
					NetAddr(addr.asString, port).sendMsg('/NetObject', defMsg, objectID, msg, *args);
					if(cntLimit == 1) {
						if(onFailedFnc.notNil) { onFailedFnc.value(addr, msg, *args) };
						// "failed %".format(addr).postln;
						conditionMsgConfirm.test = false;
					};
					if(conditionMsgConfirm.test) {
						0.005.wait;
						cntLimit = cntLimit - 1;
					};
				}
			);

			conditionMsgConfirm.test = true;
			sendMessageSemaphor.signal;
			messageID = messageID + 1;
		}.fork;
	}

	sendMsg { |addr, msg ... args| this.sendMsgFunction (nil, nil, addr, msg, *args) }

	sendMethodCall { |selector ... args|
		// thisMethod.postln;
		// var call = thisProcess.interpreter.cmdLine;
		this.class.instVarNames.postln;
		this.sendMsg("172.27.1.14", selector.asSymbol, *args);
	}
}


