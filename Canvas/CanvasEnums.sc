CanvasPosition {
	classvar dict;

	*initClass {
		dict = IdentityDictionary.new;
		dict.put( \center, 0 );
		dict.put( \topLeft, 1 );
		dict.put( \top, 2 );
		dict.put( \topRight, 3 );
		dict.put( \right, 4 );
		dict.put( \bottomRight, 5 );
		dict.put( \bottom, 6 );
		dict.put( \bottomLeft, 7 );
		dict.put( \left, 8 );
	}

	*new { |... positions|
		var arr = Array.newClear(positions.size);
		positions.do({|onePos, i|
			if(onePos.isNumber)
			{ if((onePos >= 0) && (onePos <= 8)) { arr.put(i, onePos) } { arr.put(i, -1) }}
			{ arr.put(i, dict[onePos]) }
		});
		^arr;
	}

	*center { ^dict[\center] }
	*left { ^dict[\left] }
	*right { ^dict[\right] }
	*top { ^dict[\top] }
	*bottom { ^dict[\bottom] }
	*topLeft { ^dict[\topLeft] }
	*topRight { ^dict[\topRight] }
	*bottomLeft { ^dict[\bottomLeft] }
	*bottomRight { ^dict[\bottomRight] }
}

CanvasOrientation {
	*horizontal { ^0 }
	*vertical { ^1 }
}
