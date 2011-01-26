(ns tanks.game
  (:use [clojure.contrib.import-static :only (import-static)]
        [clojure.contrib.generic.math-functions :only (sin cos abs)]
        engine.engine)
  (:import (java.awt Color GraphicsEnvironment)
           (javax.imageio ImageIO)))
(import-static java.awt.Transparency TRANSLUCENT)
(import-static java.awt.event.KeyEvent VK_LEFT VK_RIGHT VK_UP VK_DOWN VK_A VK_D VK_W VK_S)
(defn load-fire-image []
  (ImageIO/read (ClassLoader/getSystemResource "tanks/images/fire.gif")))
(defn load-tank-images []
  (let [angles (range 0 360 15)
        names (map #(str "tanks/images/tank_" (.replace (format "%3d" %) \ \0) ".gif")
                   (range 0 360 15))
        files (map #(ImageIO/read (ClassLoader/getSystemResource %)) names)
        l (map #(list (keyword (str %1)) %2) angles files)]
    (reduce #(apply assoc %1 %2) {} l)))
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
(defn impact? [p1 p2]
  (and (> 4 (abs (- (+ (:x p1) 14) (:x p2))))
       (> 4 (abs (- (+ (:y p1) 14) (:y p2))))))
(defn update-bullet [t]
  (if-let [b (:bullet t)]
    (if-let [np (move-bullet b)]
      (assoc b :position np))))
(defn update-tank [t]
  (assoc t
    :angle (turn t)
    :position (move-tank t)
    :bullet (update-bullet t)))
(let [tanks (ref #{})]
  (defn check-for-impact [t]
    (when-let [b (:bullet @t)]
      (doseq [ot @tanks]
        (if-not (= t ot)
          (let [pot (:position @ot)
                pb (:position b)]
            (when (impact? pot pb)
              (alter ot #(assoc % :hit true :speed 0 :angular-speed 0))
              (alter t dissoc :bullet)))))))
  (defn make-tank-updater []
    (fn [tank]
      (if-not (:hit @tank)
        (dosync
         (alter tank update-tank)
         (alter tanks #(conj %1 %2) tank)
         (check-for-impact tank))))))
(defn make-bullet [t]
  (let [p (:position t)]
    {:position {:x (+ (:x p) 14)
                :y (+ (:y p) 14)}
     :angle (rendering-angle t)
     :speed 4}))
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
(defn make-tank-renderer []
  (let [imgs (load-tank-images)
        fire-img (load-fire-image)]
    (fn [tank g]
      (let [i (imgs (keyword (str (rendering-angle @tank))))
            t @tank]
        (.drawImage g i (int (:x (:position t))) (int (:y (:position t))) nil)
        (if (:hit t)
          (.drawImage g fire-img (int (+ (:x (:position t)) 8)) (int (+ (:y (:position t)) 8)) nil))
        (when-let [b (:bullet t)]
          (.setColor g (Color/black))
          (.fillOval g (int (:x (:position b))) (int (:y (:position b))) 4 4))))))
(defn make-tank [tank]
  (let [tank-updater (make-tank-updater)
        tank-renderer (make-tank-renderer)
        keys (:keys tank)]
    (ref (with-meta tank {:updater tank-updater
                          :renderer tank-renderer
                          :kp-hdlrs {(:left keys) turn-left!
                                     (:right keys) turn-right!
                                     (:up keys) move-forward!
                                     (:fire keys) fire!}
                          :kr-hdlrs {(:left keys) stop-turning!
                                     (:right keys) stop-turning!
                                     (:up keys) stop-moving!}}))))
(defn make-tanks [data]
    (map make-tank data))
(defn read-from-file []
  (list {:position {:x 100 :y 100}
         :angle 0
         :speed 0
         :angular-speed 0
         :keys {:left VK_LEFT :right VK_RIGHT :up VK_UP :fire VK_DOWN}}
        {:position {:x 300 :y 100}
         :angle 180
         :speed 0
         :angular-speed 0
         :keys {:left VK_A :right VK_D :up VK_W :fire VK_S}}))
(defn make-world []
  (let [world (make-tanks (read-from-file))
        update-object (fn [obj] (((meta @obj) :updater) obj))
        render-object (fn [obj g] (((meta @obj) :renderer) obj g))
        process-key-event (fn [obj k type]
                            (when-let [h (((meta @obj) type) k)] (h obj)))
        process-key-pressed (fn [obj k] (process-key-event obj k :kp-hdlrs))
        process-key-released (fn [obj k] (process-key-event obj k :kr-hdlrs))]
    (with-meta
      world
      {:key-pressed
       (fn [code] (doseq [obj world] (process-key-pressed obj code)))
       :key-released
       (fn [code] (doseq [obj world] (process-key-released obj code)))
       :update
       (fn [] (doseq [obj world] (update-object obj)))
       :render
       (fn []
         (let [gc (.. GraphicsEnvironment getLocalGraphicsEnvironment
                      getDefaultScreenDevice getDefaultConfiguration)
               ci (.createCompatibleImage gc 500 400 TRANSLUCENT)
               g (.createGraphics ci)]
           (.setColor g (Color/white))
           (.fillRect g 0 0 500 400)
           (doseq [obj world] (render-object obj g))
           (.dispose g)
           ci))})))
(defn start []
  (game (make-world)))
