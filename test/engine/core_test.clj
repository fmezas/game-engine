(ns engine.core-test
  (:use engine.core midje.sweet))

(against-background [(around :facts (let [running-ref (atom true)]
                                      ?form))]
  (fact "continue playing until notified"
    ((make-still-playing running-ref .panel.)) => truthy
    (provided
      (displayable? .panel.) => anything)))

(fact "animator ends if still-playing returns false"
      ((make-animator .pnl. .updtr. .rndr. .still-playing.)) => truthy
      (provided
       (.still-playing.) => false))

(against-background [(around :facts (let [value-stream (atom [true true true false])
                                          still-playing (fn []
                                                          (let [retval (first @value-stream)]
                                                            (swap! value-stream rest)
                                                            retval))]
                                      ?form))] 
  (fact "animator ends when it receives external notification"
    ((make-animator .pnl. .updtr. .rndr. still-playing)) => truthy
    (provided
      (.updtr.) => anything
      (paint .pnl. .rndr.) => anything)))

(fact
 (game :key-pressed-fn .fn1. 
        :key-released-fn .fn2.
        :update-fn .fn3.
        :render-fn .fn4.
        :running-ref .ref.) => truthy
 (provided
  (make-key-listener .fn1. .fn2.) => .kl.
  (make-panel .kl.) => .pnl.
  (make-frame .pnl.) => anything
  (make-still-playing .ref. .pnl.) => .sp.
  (start-game
   (make-animator .pnl. .fn3. .fn4. .sp.)) => anything))
