(ns engine.core
  (:use [clojure.contrib.import-static :only (import-static)])
  (:import (javax.swing JFrame JPanel)
	   (java.awt Color Dimension GraphicsEnvironment)
	   (java.awt.event KeyAdapter)))
(import-static javax.swing.WindowConstants DISPOSE_ON_CLOSE)
(import-static java.awt.Transparency TRANSLUCENT)

(defn sleep-or-yield [sleep-time end-time
                      ;;excess
                      delays dlys-pr-yld]
  (if (> sleep-time 0)
    (do
      (Thread/sleep (/ sleep-time 1000000))
      [(System/nanoTime)
       (- (- (System/nanoTime) end-time) sleep-time)
       ;;excess
       delays])
    (let [delays (inc delays)
          yields (>= delays dlys-pr-yld)
          ;;excess (- sleep-time)
          oversleep-time 0]
      (if yields (Thread/yield))
      [(System/nanoTime) 0
       ;;(- excess sleep-time)
       (if yields 0 delays)])))

(defn render-image [renderer]
  (let [gc (.. GraphicsEnvironment getLocalGraphicsEnvironment
               getDefaultScreenDevice getDefaultConfiguration)
        ci (.createCompatibleImage gc 500 400 TRANSLUCENT)
        g (.createGraphics ci)]
    (.setColor g (Color/white))
    (.fillRect g 0 0 500 400)
    (renderer g)
    (.dispose g)
    ci))

(defn paint [panel renderer]
  (if-let [g (.getGraphics panel)]
    (.drawImage g (render-image renderer) 0 0 nil)))

(defn make-animator [panel updater renderer]
  (fn []
    (let [fps 80
	  period (* (/ 1000 fps) 1000000)
	  delays-per-yield 10]
      (loop [start-time (System/nanoTime)
             oversleep-time 0
             ;;excess 0
             delays 0]
        (when (.isDisplayable panel)
          (updater)
          (paint panel renderer)
	  (let [end-time (System/nanoTime)
	        sleep-time (- period (- end-time start-time) oversleep-time)
                [st os
                 ;;xs
                 dlys] (sleep-or-yield sleep-time end-time
                                       ;;excess
                                       delays delays-per-yield)]
            (recur (long st) os
                   ;;xs
                   dlys))))
      true)))

(defn make-frame [panel]
  (let [frame (JFrame. "tanks")]
    (doto frame
      (.setDefaultCloseOperation DISPOSE_ON_CLOSE)
      (.add panel)
      (.pack)
      (.setResizable false)
      (.setVisible true))))

(defn make-panel [key-listener]
  (let [panel (JPanel.)]
    (doto panel
      (.addKeyListener key-listener)
      (.setBackground (Color/white))
      (.setPreferredSize (Dimension. 500 400))
      (.setFocusable true)
      (.requestFocus))))

(defn make-key-listener [key-pressed-fn key-released-fn]
  (proxy [KeyAdapter] []
    (keyPressed [e] (key-pressed-fn e))
    (keyReleased [e] (key-released-fn e))))

(defn start-game [animator-fn]
  (.start (Thread. animator-fn)))

(defn game [key-pressed-fn key-released-fn update-fn render-fn]
  (let [key-listener (make-key-listener key-pressed-fn key-released-fn)
        panel (make-panel key-listener)
        frame (make-frame panel)
        animator (make-animator panel update-fn render-fn)]
    (start-game animator)
    true))
