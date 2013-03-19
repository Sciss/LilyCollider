
LilyShowableObj : LilyObj {

    classvar <>fileName = "~/Desktop/sketch";
	classvar <>pdfViewer = "open";
	classvar <>midiPlayer = "vlc";
	classvar <>textEditor = "frescobaldi";
	classvar <>template = "doc";
	classvar <>lilyCmd= "lilypond";
	classvar <>templatesFolder;

	*initClass {
		Class.initClass(Platform);
		templatesFolder = Platform.userExtensionDir ++ "/LilyCollider/templates";
	}

//    *new {
//        ^super.new.initShowableObj;
//    }

//	initShowableObj {
//		LilyShowableObj.templatesFolder = Platform.userExtensionDir ++ "/LilyCollider/templates";
//	}

    /* Music expression between curly brackets */
    musicString {

        ^("{\n" ++  this.string ++ "\n}\n").asString;
    }


    /* Path of the choosen template LilyPond file */
    *templateFile {
        ^(LilyShowableObj.templatesFolder ++ "/" ++ LilyShowableObj.template ++ ".ly").standardizePath
    }


    header {
        var file, content;

        file = File(LilyShowableObj.templateFile,"r");
        content = file.readAllString;
        file.close;
        ^content;
    }


    /* Write the File to Disk */
    write {
        var file;

        file = File(LilyShowableObj.fileName.standardizePath ++ ".ly","w");
        file.write(this.header);
        file.write(this.musicString);
        file.close;
    }


    /* Array of the available LilyPond templates Paths */
    templatePathList {

        ^(templatesFolder ++ "/*").pathMatch
    }


    /* Array of the available LilyPond templates Names */
    templateList {

        ^(LilyShowableObj.templatePathList.collect {|i| i.basename})

    }


    /* Call the PDF Viewer to show the produced PDF File: */
    show { (LilyShowableObj.pdfViewer ++ " " ++ LilyShowableObj.fileName.standardizePath ++ ".pdf" ).unixCmd }


    /* Call the MIDI player program */
    playMidi { ( LilyShowableObj.midiPlayer ++ " " ++ LilyShowableObj.fileName.standardizePath ++ ".midi" ).unixCmd }


    /* Produce the score with LilyPond, when it's done open with the PDF viewer */
    plot {

        fork {
            this.write;
            0.1.wait;
            (
		LilyShowableObj.lilyCmd ++ " -o " ++ LilyShowableObj.fileName.standardizePath ++ " " ++
		LilyShowableObj.fileName.standardizePath ++ ".ly"
            ).unixCmd { |res|
				if (res == 127) {
					"LilyPond failed with result 127. Make sure LilyShowableObj.lilyCmd is corret.".warn;
				} {
					this.show;
				};
			};
        }
    }


    /* Call the text editor to open the .ly file */
    edit {
        this.write;
        (LilyShowableObj.textEditor ++ " " ++ LilyShowableObj.fileName.standardizePath ++ ".ly").unixCmd;
    }

}
