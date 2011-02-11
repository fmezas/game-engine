(ns tanks.rendering
  (:use [tanks.resources :only (get-image get-fire-image get-bullet-image)]))

(defn position-for-tank [t]
  (let [p (:position t)]
    [(int (:x p)) (int (:y p))]))
(defn position-for-explosion [t]
  (let [p (:position t)]
    [(int (+ (:x p) 8)) (int (+ (:y p) 8))]))
(defn position-for-bullet [b]
  (let [p (:position b)]
    [(int (:x p)) (int (:y p))]))
(defn render-image [g i [x y]]
  (.drawImage g i x y nil))
(defn render-tank [obj g]
  (render-image g (get-image obj) (position-for-tank obj))
  (if (:hit obj)
    (render-image g (get-fire-image) (position-for-explosion obj)))
  (when-let [b (:bullet obj)]
    (render-image g (get-bullet-image) (position-for-bullet b))))
