(ns tanks.events
  (:use [tanks.movement :only (rendering-angle)]))
(defn make-bullet [t]
  (let [p (:position t)]
    {:position {:x (+ (:x p) 14)
                :y (+ (:y p) 14)}
     :angle (rendering-angle t)
     :speed 4}))
(defn turn-left [t]
  (if-not (:hit t) (assoc t :angular-speed 2) t))
(defn turn-right [t]
  (if-not (:hit t) (assoc t :angular-speed -2) t))
(defn stop-turning [t]
  (if-not (:hit t) (assoc t :angular-speed 0) t))
(defn move-forward [t]
  (if-not (:hit t) (assoc t :speed 1) t))
(defn stop-moving [t]
  (if-not (:hit t) (assoc t :speed 0) t))
(defn fire [t]
  (if-not (or (:hit t) (:bullet t))
    (assoc t :bullet (make-bullet t))
    t))
