(ns tanks.movement
  (:use [tanks.collision :only (hit?)]))
(defn rendering-angle [o]
  (let [a (:angle o)] (- a (mod a 15))))
(defn move [o]
  (let [a (rendering-angle o)
        r (/ (* Math/PI a) 180)]
  {:x (+ (:x (:position o)) (* (:speed o) (Math/cos r)))
   :y (- (:y (:position o)) (* (:speed o) (Math/sin r)))}))
(defn out-of-screen? [p]
  (let [new-x (:x p)
        new-y (:y p)]
    (or (< new-x 0) (> new-x 468) (< new-y 0) (> new-y 368))))
(defn move-bullet [b]
  (let [np (move b)]
    (if (out-of-screen? np) nil np)))
(defn turn [t]
  (if (:hit t)
    (:angle t)
    (mod (+ (:angle t) (:angular-speed t)) 360)))
(defn move-tank [t]
  (if (:hit t)
    (:position t)
    (let [np (move t)]
      (if (out-of-screen? np) (:position t) np))))
(defn update-bullet [t]
  (if-let [b (:bullet t)]
    (if-let [np (move-bullet b)]
      (assoc b :position np))))
(defn update-tank [t w]
  (assoc t
    :hit (hit? t w)
    :angle (turn t)
    :position (move-tank t)
    :bullet (update-bullet t)))
