# Game Engine

Game Engine is a clojure implementation of the engine described in [Killer Game Programming in Java](http://fivedots.coe.psu.ac.th/~ad/jg/).

## Use

Game Engine uses [Leiningen](https://github.com/technomancy/leiningen) as its building tool, so 

    $ lein repl

should put you in a clojure repl where you can do

    user=> (use 'tanks.core)
    user=> (start)

to see a sample game that uses the engine.

Or from the command line:

    $ lein run