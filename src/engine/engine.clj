(ns engine.engine
  (:use [clojure.contrib.import-static :only (import-static)])
  (:import (java.awt Color)
           (javax.imageio ImageIO)
           (javax.swing JFrame JPanel)
           (javax.imageio ImageIO)
	   (java.awt Color Dimension GraphicsEnvironment Toolkit)
	   (java.awt.event KeyAdapter)))
(import-static javax.swing.WindowConstants DISPOSE_ON_CLOSE)
(import-static java.awt.RenderingHints
               KEY_ANTIALIASING VALUE_ANTIALIAS_ON KEY_INTERPOLATION
               VALUE_INTERPOLATION_BILINEAR)
(import-static java.awt.Transparency TRANSLUCENT)
(import-static java.awt.event.KeyEvent VK_LEFT VK_RIGHT VK_UP VK_DOWN VK_A VK_D VK_W VK_S)
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
(defn paint [panel i]
  (if-let [g (.getGraphics panel)]
    (.drawImage g i 0 0 nil)))
(defn update [world]
  (((meta world) :update)))
(defn render [world]
  (((meta world) :render)))
(defn make-animator [panel world]
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
          (update world)
          (paint panel (render world))
	  (let [end-time (System/nanoTime)
		sleep-time (- period (- end-time start-time) oversleep-time)
                [st os xs dlys] (sleep-or-yield sleep-time end-time excess delays delays-per-yield)]
            (recur (long st) os xs dlys)))))))
(defn key-pressed [world k]
  (((meta world) :key-pressed) k))
(defn key-released [world k]
  (((meta world) :key-released) k))
(defn make-key-listener [world]
  (proxy [KeyAdapter] []
    (keyPressed [e] (key-pressed world (.getKeyCode e)))
    (keyReleased [e] (key-released world (.getKeyCode e)))))
(defn make-panel [key-listener]
  (let [panel (JPanel.)]
    (doto panel
      (.addKeyListener key-listener)
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
(defn game [world]
  (System/setProperty "sun.java2d.opengl" "true")
  (let [key-listener (make-key-listener world)
        panel (make-panel key-listener)
	frame (make-frame panel)
	animator (make-animator panel world)]
    (.start (Thread. animator))
    nil))
