(ns tanks.core-test
  (:use tanks.core midje.sweet))

(fact
 (find-event-subscribers anything anything []) => [])
(fact
 (let [obj {:key-pressed {.k. .fn.}}]
   (find-event-subscribers :key-pressed .k. [obj]) => [{:handler .fn. :target obj}]))
(fact
 (let [obj {:key-pressed {.k. .fn.}}]
   (find-event-subscribers :key-pressed .k. [obj .foo.]) => [{:handler .fn. :target obj}]))
(fact
 (let [obj1 {:key-pressed {.k1. .fn.}}
       obj2 {:key-pressed {.k2. .fn.}}]
   (find-event-subscribers :key-pressed .k. [obj1 obj2]) => []))
(fact
 (let [obj1 {:key-pressed {.k1. .fn.}}
       obj2 {:key-pressed {.k2. .fn.}}]
   (find-event-subscribers :key-pressed .k2. [obj1 obj2]) => [{:handler .fn., :target obj2}]))
(fact
 (let [obj1 {:key-pressed {.k1. .fn1.}}
       obj2 {:key-pressed {.k1. .fn2.}}]
   (find-event-subscribers :key-pressed .k1. [obj1 obj2]) => (contains
                                                    [{:handler .fn1., :target obj1}
                                                     {:handler .fn2., :target obj2}]
                                                    :in-any-order)))

(fact
 (let [r (ref 1)]
 (apply-event-to-subscribers .e. .k.) => falsey
 (provided
  (get-world-event-subscribers .e. .k.) => [{:handler #(dosync (alter % inc)) :target r}])
  @r => 2))
(fact
 (render .g.) => falsey
 (provided
  (get-world-objects) => []))
