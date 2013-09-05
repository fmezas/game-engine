(ns engine.core
  (:use [clojure.contrib.import-static :only (import-static)])
  (:import (javax.swing JFrame JPanel)
	   (java.awt Color Dimension GraphicsEnvironment)
	   (java.awt.event KeyAdapter)))
(import-static javax.swing.WindowConstants DISPOSE_ON_CLOSE)
(import-static java.awt.Transparency TRANSLUCENT)

(defn- sleep-or-yield [sleep-time end-time
                      delays dlys-pr-yld]
  (if (> sleep-time 0)
    (do
      (Thread/sleep (/ sleep-time 1000000))
      [(System/nanoTime)
       (- (- (System/nanoTime) end-time) sleep-time)
       delays])
    (let [delays (inc delays)
          yields (>= delays dlys-pr-yld)
          oversleep-time 0]
      (if yields (Thread/yield))
      [(System/nanoTime) 0
       (if yields 0 delays)])))

(defn- render-image [renderer]
  (let [gc (.. GraphicsEnvironment getLocalGraphicsEnvironment
               getDefaultScreenDevice getDefaultConfiguration)
        ci (.createCompatibleImage gc 500 400 TRANSLUCENT)
        g (.createGraphics ci)]
    (.setColor g (Color/white))
    (.fillRect g 0 0 500 400)
    (renderer g)
    (.dispose g)
    ci))

(defn- paint [panel renderer]
  (if-let [g (.getGraphics panel)]
    (.drawImage g (render-image renderer) 0 0 nil)))

(defn- make-animator [panel updater renderer]
  (fn []
    (let [fps 40
	  period (* (/ 1000 fps) 1000000)
	  delays-per-yield 10]
      (loop [start-time (System/nanoTime)
             oversleep-time 0
             delays 0]
        (when (.isDisplayable panel)
          (updater)
          (paint panel renderer)
	  (let [end-time (System/nanoTime)
	        sleep-time (- period (- end-time start-time) oversleep-time)
                [st os dlys] (sleep-or-yield sleep-time end-time
                                             delays delays-per-yield)]
            (recur (long st) os dlys))))
      true)))

(defn- make-frame [panel]
  (let [frame (JFrame. "tanks")]
    (doto frame
      (.setDefaultCloseOperation DISPOSE_ON_CLOSE)
      (.add panel)
      (.pack)
      (.setResizable false)
      (.setVisible true))))

(defn- make-panel [key-listener width length]
  (let [panel (JPanel.)]
    (doto panel
      (.addKeyListener key-listener)
      (.setBackground (Color/white))
      (.setPreferredSize (Dimension. width length))
      (.setFocusable true)
      (.requestFocus))
    (make-frame panel)
    panel))

(defn- make-key-listener [key-pressed-fn key-released-fn]
  (proxy [KeyAdapter] []
    (keyPressed [e] (key-pressed-fn e))
    (keyReleased [e] (key-released-fn e))))

(defn- start-game [animator-fn]
  (.start (Thread. animator-fn)))

(defn game
  "Creates an instance of javax.swing.JPanel within a non-resizable
instance of javax.swing.JFrame sized according to map received as last
parameter with keys :width and :length (this could be extended to accept either
this map or a function to configure the panel (set background to something
other than white pixels for instance).
  Configures the created JPanel instance with key-pressed-fn as its keyPressed
event handler and key-released-fn as its keyReleased event handler.
  Lastly, it fires up an endless loop that will invoke update-fn (parameterless)
and render-fn (with an instance of java.awt.Graphics2D as its only paramter)
trying to guarantee a frequency of 40FPS for the purpose of animating the
contents of the JPanel instance."
  [key-pressed-fn key-released-fn update-fn render-fn {width :width length :length}]
  (let [key-listener (make-key-listener key-pressed-fn key-released-fn)
        panel (make-panel key-listener width length)
        animator (make-animator panel update-fn render-fn)]
    (start-game animator)
    true))
