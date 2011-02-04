(ns tanks.resources
  (:use [clojure.contrib.import-static :only (import-static)])
  (:import (javax.imageio ImageIO)))
(import-static java.awt.event.KeyEvent VK_LEFT VK_RIGHT VK_UP VK_DOWN VK_A VK_D VK_W VK_S)
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
(defn load-fire-image []
  (ImageIO/read (ClassLoader/getSystemResource "tanks/images/fire.gif")))
(defn load-tank-images []
  (let [angles (range 0 360 15)
        names (map #(str "tanks/images/tank_" (.replace (format "%3d" %) \ \0) ".gif") angles)
        files (map #(ImageIO/read (ClassLoader/getSystemResource %)) names)
        l (map #(list (keyword (str %1)) %2) angles files)]
    (reduce #(apply assoc %1 %2) {} l)))
