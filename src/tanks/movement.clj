(ns tanks.movement
  (:use [clojure.contrib.generic.math-functions :only (sin cos abs)]))
(defn turn [t]
  (mod (+ (:angle t) (:angular-speed t)) 360))
(defn rendering-angle [o]
  (let [a (:angle o)] (- a (mod a 15))))
(defn move [o]
  (let [a (rendering-angle o)
        r (/ (* Math/PI a) 180)]
  {:x (+ (:x (:position o)) (* (:speed o) (cos r)))
   :y (- (:y (:position o)) (* (:speed o) (sin r)))}))
(defn out-of-screen? [p]
  (let [new-x (:x p)
        new-y (:y p)]
    (or (< new-x 0) (> new-x 468) (< new-y 0) (> new-y 368))))
(defn move-tank [t]
  (let [np (move t)]
    (if (out-of-screen? np) (:position t) np)))
(defn move-bullet [b]
  (let [np (move b)]
    (if (out-of-screen? np) nil np)))
(defn make-bullet [t]
  (let [p (:position t)]
    {:position {:x (+ (:x p) 14)
                :y (+ (:y p) 14)}
     :angle (rendering-angle t)
     :speed 4}))
(defn update-bullet [t]
  (if-let [b (:bullet t)]
    (if-let [np (move-bullet b)]
      (assoc b :position np))))
(defn turn-left! [t]
  (if-not (:hit @t) (dosync (alter t #(assoc % :angular-speed 2)))))
(defn turn-right! [t]
  (if-not (:hit @t) (dosync (alter t #(assoc % :angular-speed -2)))))
(defn stop-turning! [t]
  (if-not (:hit @t) (dosync (alter t #(assoc % :angular-speed 0)))))
(defn move-forward! [t]
  (if-not (:hit @t) (dosync (alter t #(assoc % :speed 1)))))
(defn stop-moving! [t]
  (if-not (:hit @t) (dosync (alter t #(assoc % :speed 0)))))
(defn fire! [t]
  (if-not (or (:hit @t) (:bullet @t))
    (dosync (alter t #(assoc % :bullet (make-bullet %))))))
