(ns tanks.resources
  (:use [clojure.contrib.import-static :only (import-static)]
        [tanks.movement :only (rendering-angle)]
        [tanks.events :only (turn-left
                             turn-right
                             move-forward
                             fire
                             stop-moving
                             stop-turning)])
  (:import (javax.imageio ImageIO)))
(import-static java.awt.event.KeyEvent VK_LEFT VK_RIGHT VK_UP VK_DOWN VK_A VK_D VK_W VK_S)

(defn- load-fire-image []
  (ImageIO/read (ClassLoader/getSystemResource "tanks/images/fire.gif")))
(defn- load-bullet-image []
  (ImageIO/read (ClassLoader/getSystemResource "tanks/images/bullet.gif")))
(defn- load-tank-images []
  (let [angles (range 0 360 15)
        names (map #(str "tanks/images/tank_" (.replace (format "%3d" %) \ \0) ".gif") angles)
        files (map #(ImageIO/read (ClassLoader/getSystemResource %)) names)
        l (map #(list (keyword (str %1)) %2) angles files)]
    (reduce #(apply assoc %1 %2) {} l)))

(let [imgs (load-tank-images)
      fire-img (load-fire-image)
      bullet-img (load-bullet-image)]
  (defn get-image [obj]
    (imgs (keyword (str (rendering-angle obj)))))
  (defn get-fire-image [] fire-img)
  (defn get-bullet-image [] bullet-img))

;; TODO: the idea is that this function should read the game resources
;; from an external location instead of having them hardcoded here
(defn read-from-file []
  (list {:position {:x 100 :y 100}
         :angle 0
         :speed 0
         :angular-speed 0
         :key-pressed {VK_LEFT #(dosync (alter % turn-left))
                       VK_RIGHT #(dosync (alter % turn-right))
                       VK_UP #(dosync (alter % move-forward))
                       VK_DOWN #(dosync (alter % fire))}
         :key-released {VK_LEFT #(dosync (alter % stop-turning))
                        VK_RIGHT #(dosync (alter % stop-turning))
                        VK_UP #(dosync (alter % stop-moving))}}
        {:position {:x 300 :y 100}
         :angle 180
         :speed 0
         :angular-speed 0
         :key-pressed {VK_A #(dosync (alter % turn-left))
                       VK_D #(dosync (alter % turn-right))
                       VK_W #(dosync (alter % move-forward))
                       VK_S #(dosync (alter % fire))}
         :key-released {VK_A #(dosync (alter % stop-turning))
                        VK_D #(dosync (alter % stop-turning))
                        VK_W #(dosync (alter % stop-moving))}}))
