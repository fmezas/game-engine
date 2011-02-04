(ns tanks.collision
  (:use [clojure.contrib.generic.math-functions :only (sin cos abs)]))
(defn impact? [p1 p2]
  (and (> 4 (abs (- (+ (:x p1) 14) (:x p2))))
       (> 4 (abs (- (+ (:y p1) 14) (:y p2))))))
(defn check-for-impact [t world]
  (when-let [b (:bullet @t)]
    (doseq [ot world]
      (if-not (= t ot)
        (let [pot (:position @ot)
              pb (:position b)]
          (when (impact? pot pb)
            (alter ot #(assoc % :hit true :speed 0 :angular-speed 0))
            (alter t dissoc :bullet)))))))
