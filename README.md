## Preparations

On OS X, to be able to launch lilypond from the terminal (i.e. SuperCollider), the executable is `/Applications/Lilypond.app/Contents/Resources/bin/lilypond`. However it seems necessary to cd into the `Resources` directory first, in order for Lilypond to find extra files. Therefore, you can use the `lilypond` shell script in this repository as a replacement for the executable. E.g.

    $ cp lilypond ~/bin/
    
(Assuming that `~/bin` is in the `$PATH`).

