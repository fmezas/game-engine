(ns tanks.main
  (:use [clojure.contrib.import-static :only (import-static)]
        [clojure.contrib.generic.math-functions :only (sin cos abs)]
        tanks.resources
        tanks.movement
        engine.core)
  (:import (java.awt Color GraphicsEnvironment)))
(import-static java.awt.Transparency TRANSLUCENT)
(let [impact?
      (fn [p1 p2]
        (and (> 4 (abs (- (+ (:x p1) 14) (:x p2))))
             (> 4 (abs (- (+ (:y p1) 14) (:y p2))))))
      check-for-impact
      (fn [t world]
        (when-let [b (:bullet @t)]
          (doseq [ot world]
            (if-not (= t ot)
              (let [pot (:position @ot)
                    pb (:position b)]
                (when (impact? pot pb)
                  (alter ot #(assoc % :hit true :speed 0 :angular-speed 0))
                  (alter t dissoc :bullet)))))))
      update-tank
      (fn [t]
        (assoc t
          :angle (turn t)
          :position (move-tank t)
          :bullet (update-bullet t)))]
  (defn make-tank-updater []
    (fn [tank world]
      (if-not (:hit @tank)
        (dosync
         (alter tank update-tank)
         (check-for-impact tank world))))))
(let [imgs (load-tank-images)
      fire-img (load-fire-image)]
  (defn make-tank-renderer []
    (fn [t g]
      (let [i (imgs (keyword (str (rendering-angle t))))]
        (.drawImage g i (int (:x (:position t))) (int (:y (:position t))) nil)
        (if (:hit t)
          (.drawImage g fire-img (int (+ (:x (:position t)) 8)) (int (+ (:y (:position t)) 8)) nil))
        (when-let [b (:bullet t)]
          (.setColor g (Color/black))
          (.fillOval g (int (:x (:position b))) (int (:y (:position b))) 4 4))))))
(defn make-tank [tank]
  (let [keys (:keys tank)]
    (with-meta tank {:updater (make-tank-updater)
                     :renderer (make-tank-renderer)
                     :kp-hdlrs {(:left keys) turn-left
                                (:right keys) turn-right
                                (:up keys) move-forward
                                (:fire keys) fire}
                     :kr-hdlrs {(:left keys) stop-turning
                                (:right keys) stop-turning
                                (:up keys) stop-moving}})))
(defn make-world []
  (let [world (map ref (map make-tank (read-from-file)))
        process-key-event (fn [obj k type]
                            (if-let [h (((meta obj) type) k)]
                              (h obj)
                              obj))
        process-key-pressed (fn [obj k] (process-key-event obj k :kp-hdlrs))
        process-key-released (fn [obj k] (process-key-event obj k :kr-hdlrs))]
    (with-meta
      world
      {:key-pressed
       (fn [code] (doseq [obj world]
                    (dosync
                     (print obj) (print @obj)
                     (alter obj #(process-key-pressed % code)))))
       :key-released
       (fn [code] (doseq [obj world]
                    (dosync (alter obj #(process-key-released % code)))))
       :update
       (fn [] (doseq [obj world] (((meta @obj) :updater) obj world)))
       :render
       (fn []
         (let [gc (.. GraphicsEnvironment getLocalGraphicsEnvironment
                      getDefaultScreenDevice getDefaultConfiguration)
               ci (.createCompatibleImage gc 500 400 TRANSLUCENT)
               g (.createGraphics ci)]
           (.setColor g (Color/white))
           (.fillRect g 0 0 500 400)
           (doseq [obj world] (((meta @obj) :renderer) @obj g))
           (.dispose g)
           ci))})))
(defn start []
  (game (make-world)))
