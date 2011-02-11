(ns engine.core-test
  (:use engine.core midje.sweet))

(fact
 (against-background
  (around :facts (let [running-ref (atom true)] ?form)))
 ((make-still-playing running-ref .panel.)) => truthy
 (provided
  (displayable? .panel.) => .irrelevant.))

(fact "animator ends if still-playing returns false"
      ((make-animator .pnl. .updtr. .rndr. .still-playing.)) => truthy
      (provided
       (.still-playing.) => false))

(fact "animator ends when it receives external notification"
      (against-background
       (around :facts (let [value-stream (atom [true true true false])
                            still-playing (fn []
                                            (let [retval (first @value-stream)]
                                              (swap! value-stream rest)
                                              retval))] ?form)))
      ((make-animator .pnl. .updtr. .rndr. still-playing)) => truthy
      (provided
       (.updtr.) => .irrelevant.
       (paint .pnl. .rndr.) => .irrelevant.))

(fact
 (game :key-pressed-fn .fn1. 
        :key-released-fn .fn2.
        :update-fn .fn3.
        :render-fn .fn4.
        :running-ref .ref.) => truthy
 (provided
  (make-key-listener .fn1. .fn2.) => .kl.
  (make-panel .kl.) => .pnl.
  (make-frame .pnl.) => .irrelevant.
  (make-still-playing .ref. .pnl.) => .sp.
  (start-game
   (make-animator .pnl. .fn3. .fn4. .sp.)) => .irrelevant.))
