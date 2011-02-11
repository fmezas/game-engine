(ns tanks.collision
  (:use [clojure.contrib.generic.math-functions :only (abs)]))
(defn impact? [t1 r2]
  (let [t2 @r2]
    (if-let [b2 (:bullet t2)]
      (let [p1 (:position t1)
            p2 (:position b2)
            impact (and (> 4 (abs (- (+ (:x p1) 14) (:x p2))))
                        (> 4 (abs (- (+ (:y p1) 14) (:y p2)))))]
        (when impact
          (dosync (alter r2 assoc :bullet nil)))
          impact))))
(defn hit? [t world]
  ;; need to calculate new hit, otherwise new bullets will pass
  ;; through a hit tank
  (let [new-hit (some #(impact? t %) (remove #(= @% t) world))]
    (or new-hit (:hit t))))
