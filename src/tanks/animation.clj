(ns tanks.animation
  (:use [clojure.contrib.import-static :only (import-static)])
  (:import (javax.swing JFrame JPanel)
           (javax.imageio ImageIO)
	   (java.awt Color Dimension GraphicsEnvironment Toolkit)
	   (java.awt.event KeyAdapter)))
(import-static javax.swing.WindowConstants DISPOSE_ON_CLOSE)
(import-static java.awt.RenderingHints
               KEY_ANTIALIASING VALUE_ANTIALIAS_ON KEY_INTERPOLATION
               VALUE_INTERPOLATION_BILINEAR)
(import-static java.awt.Transparency TRANSLUCENT)
(import-static java.awt.event.KeyEvent VK_LEFT VK_RIGHT VK_UP)
(defn sleep-or-yield [sleep-time end-time excess delays dlys-pr-yld]
  (if (> sleep-time 0)
    (do
      (Thread/sleep (/ sleep-time 1000000))
      [(System/nanoTime)
       (- (- (System/nanoTime) end-time) sleep-time)
       excess
       delays])
    (let [delays (inc delays)
          yields (>= delays dlys-pr-yld)
          excess (- sleep-time)
          oversleep-time 0]
      (if yields (Thread/yield))
      [(System/nanoTime) 0 (- excess sleep-time) (if yields 0 delays)])))
(defn update-object [obj]
  (((meta @obj) :updater) obj))
(defn update [objs]
  (doseq [obj objs] (update-object obj)))
(defn render-object [obj g]
  (((meta @obj) :renderer) obj g))
(defn render [objs]
  (let [gc (.. GraphicsEnvironment getLocalGraphicsEnvironment
               getDefaultScreenDevice getDefaultConfiguration)
        ci (.createCompatibleImage gc 500 400 TRANSLUCENT)
        g (.createGraphics ci)]
    (.setColor g (Color/white))
    (.fillRect g 0 0 500 400)
    (doseq [obj objs] (render-object obj g))
    (.dispose g)
    ci))
(defn paint [panel i]
  (if-let [g (.getGraphics panel)]
    (.drawImage g i 0 0 nil)))
(defn make-animator [panel game-objects]
  (fn []
    (let [running (atom true)
          fps 80
	  period (* (/ 1000 fps) 1000000)
	  delays-per-yield 10]
      (loop [start-time (System/nanoTime)
	     oversleep-time 0
	     excess 0
	     delays 0]
	(when (and (.isDisplayable panel) @running)
          (update game-objects)
          (paint panel (render game-objects))
	  (let [end-time (System/nanoTime)
		sleep-time (- period (- end-time start-time) oversleep-time)
                [st os xs dlys] (sleep-or-yield sleep-time end-time excess delays delays-per-yield)]
            (recur (long st) os xs dlys)))))))
(defn process-key-pressed [obj k]
  (when-let [h (((meta @obj) :kp-hdlrs) k)] (h obj)))
(defn process-key-released [obj k]
  (when-let [h (((meta @obj) :kr-hdlrs) k)] (h obj)))
(defn listen-to-keys [panel game-objects]
  (.addKeyListener panel
   (proxy [KeyAdapter] []
     (keyPressed [e]
                 (doseq [obj game-objects]
                   (process-key-pressed obj (.getKeyCode e))))
     (keyReleased [e]
                  (doseq [obj game-objects]
                    (process-key-released obj (.getKeyCode e)))))))
(defn make-panel []
  (let [panel (JPanel.)]
    (doto panel
      (.setBackground (Color/white))
      (.setPreferredSize (Dimension. 500 400))
      (.setFocusable true)
      (.requestFocus))))
(defn make-frame [panel]
  (let [frame (JFrame. "tanks")]
    (doto frame
      (.setDefaultCloseOperation DISPOSE_ON_CLOSE)
      (.add panel)
      (.pack)
      (.setResizable false)
      (.setVisible true))))
(defn game [game-objects]
  (System/setProperty "sun.java2d.opengl" "true")
  (let [panel (make-panel)
	frame (make-frame panel)
	animator (make-animator panel game-objects)]
    (listen-to-keys panel game-objects)
    (.start (Thread. animator))
    'nil))
