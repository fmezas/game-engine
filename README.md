# Game Engine

Game Engine is a clojure port of the animation engine described in [Killer Game Programming in Java](http://fivedots.coe.psu.ac.th/~ad/jg/) (many thanks to [Andrew Davison](http://fivedots.coe.psu.ac.th/~ad/index.html), the author of the book, for authorizing the sharing of this code).

## Use

Game Engine uses [Leiningen](https://github.com/technomancy/leiningen) as its building tool, so 

    $ lein repl

should put you in a clojure repl where you can do

    user=> (use 'tanks.core)
    user=> (start)

to see a (very primitive) sample game that uses the engine.

Or from the command line:

    $ lein run
