
RhythmicCell : LilyRhythmObj {


    var <>struct, <>length;
    var <>template = "rhythmic";


    *new { arg thisCell;

        ^super.new.initRhythmCell(thisCell);
    }


    initRhythmCell { arg thisCell;

        this.length_(thisCell[0]);
        this.struct_(thisCell[1]);
    }



    heads {

        ^this.getHeads(struct);
    }


    lyHeads {

        ^this.heads.collect({|i|
			this.findKeyForValue(i)
        })

    }

	findKeyForValue { arg i;
		var isPause, key;
		key = durationDict.findKeyForValue(i.abs);
		isPause = i < 0;
		^isPause.if({ "-" ++ key }, key);
	}

    subHeads {

        ^this.heads.collect({|i|

            i / 8.0
        })

    }


    adjustedStruct {

        ^struct.collect({|i,j|

            case
            { i.isKindOf(Number) }
            { this.adjustedHeads[j] }

            { i.isKindOf(Array) }
            {
                [this.adjustedHeads[j], RhythmicCell([this.adjustedHeads[j], i[1]]).struct]
            };
        })
    }


    adjustedLyStruct {

        ^this.adjustedStruct.deepCollect(
            this.adjustedStruct.rank,
            {|i|
                case
                {i.isKindOf(Number)}
                {this.findKeyForValue(i)}

                {i.isKindOf(Array)}
                {i};
            }
        )
    }


    getHead { arg thisItem;

        case
        {thisItem.isKindOf(Number)}
        {^thisItem}

        {thisItem.isKindOf(Array)}
        {^this.getHead(thisItem[0])};
    }


    getHeads { arg thisList;

        ^thisList.collect({ arg item;
            this.getHead(item)
        })
    }


    factor {

        var thisFactor, adjustedSum;

        thisFactor = 1;
		adjustedSum = this.heads.abs.sum;

        while(
            { (adjustedSum) < (this.length * 8) },
            { adjustedSum = adjustedSum * 2; thisFactor = thisFactor * 2 }
        );

        ^thisFactor;
    }


    adjustedHeads {
        ^this.heads * this.factor
    }


    numer {
		^(this.adjustedHeads.abs.sum / gcd((length * 8).asInteger, this.adjustedHeads.abs.sum.asInteger))
    }


    denom {
        var thisDenom;

        thisDenom = (length * 8) / gcd((length * 8).asInteger, this.adjustedHeads.abs.sum.asInteger);

        if(thisDenom < this.numer, {
            while({(thisDenom * 2) < this.numer}, {
                thisDenom = thisDenom * 2
            })
        });

        ^thisDenom
    }


    tuplet {
		var n, d;
//        ^[this.numer, this.denom]
		#n, d = (this.adjustedHeads.abs.sum/(length * 8)).asFraction;
		while({ d * 2 < n }, { d = d * 2 });
		^[n, d];
    }


    hasTuplet {
		var n, d;
		#n, d = this.tuplet;
		^(n != d);
    }


    tupletString {
		var n, d;
        if(this.hasTuplet, {
			#n, d = this.tuplet;
			^("\\times " ++ d.asString ++ "/" ++ n.asString ++ " ")
        });
    }


    simpleString { arg thisTree, thisLevel=1;
		var str, isPause;
        var levelString = String.new;
        var stringOut = String.new;


        thisLevel.do { levelString = levelString ++ "\t"};


        this.hasTuplet.if({
            stringOut = levelString ++ this.tupletString ++ "{ \n" ++ levelString ++ "\t";
        });


        thisTree.do { arg thisNumber;
			str = thisNumber.asString;
			isPause = str[0] == $-;
			if (isPause) {
				stringOut = stringOut ++ "r" ++ str.drop(1) ++ "  "
			} {
				stringOut = stringOut ++ "c'" ++ str ++ "  "
			}
        };


        this.hasTuplet.if({
            stringOut = stringOut ++ "\n" ++ levelString ++ "}"
        });


        ^stringOut;

    }


    noTimeSigString { arg thisLevel =1;
		var isPause;
        var stringOut = String.new;
        var levelString = String.new;

        thisLevel.do { levelString = levelString ++ "\t"};

        this.struct.containsSeqColl.not.if({

            stringOut = this.simpleString(this.adjustedLyStruct, thisLevel)

        },{

            this.hasTuplet.if({
                stringOut = levelString ++ this.tupletString ++ "{ \n" ++ levelString ++ "\t";
            });

            this.adjustedStruct.do {arg thisItem, thisIndex;
				// ["adjustedStruct", thisIndex, thisItem].postcs;
                case
                {thisItem.isNumber}
                {
						isPause = thisItem < 0;
						stringOut = stringOut ++ isPause.if("r", "c'") ++
						  this.findKeyForValue(thisItem.abs) ++ "  "
				}

                {thisItem.isArray}
                {
                    stringOut = stringOut ++
                    (RhythmicCell.new(thisItem.put(0, thisItem.at(0) / 8)).noTimeSigString) ++ " \n \t";
                }

            };

            this.hasTuplet.if({
                stringOut = stringOut ++ "\n" ++ levelString ++ "}"
            });

        }

        ); //end if

        ^stringOut;
    }




    string {
		var m;
//		m = measureScaleLily[(this.length*2)-1].asString;
		m = (this.length/8).asFraction;
		m = m[0].asString ++ "/" ++ m[1].asString;
        ^("\\time " ++ m ++ "\n" ++ this.noTimeSigString ++ "\n")
    }


    musicString {
        // overrides to print on a one-line staff
        ^("\\new RhythmicStaff {" ++ this.string ++ "\n}"
        )

    }


    ////////////////////////////
    // Rhythmic Manipulations //
    ////////////////////////////


    lengthAdd { arg thisNumber;

        this.length_(this.length + thisNumber)
    }


    lengthMul  { arg thisNumber;

        this.length_(this.length * thisNumber)
    }


    // substitute an element:
    subst { arg thisIndex, thisItem;

        this.struct_(this.struct.put(thisIndex, thisItem))
    }


    reshapeLike { arg other;
        //  TODO other index operators: \foldAt \clipAt \at
        this.struct_(this.struct.reshapeLike(other.struct));
    }

}
