(ns tanks.core
  (:use [engine.core :only (game)]
        [tanks.resources :only (read-from-file)]
        [tanks.movement :only (update-tank)]
        [tanks.rendering :only (render-tank)]))

(def kr :key-released)
(def kp :key-pressed)

(defn find-event-subscribers [e k coll]
  (keep (fn [obj]
          (if-let [handlers (obj e)]
            (if-let [handler (handlers k)]
              {:handler handler :target obj})))
        coll))

(let [world-objects (ref nil)]
  (defn init-world []
    (let [coll (read-from-file)
          refs (map ref coll)]
      (dosync
       (ref-set world-objects refs))))
  (defn get-world-objects [] @world-objects)
  (defn get-world-event-subscribers [e k]
    (find-event-subscribers e k @world-objects)))

(defn update-object [obj]
  (dosync
   (alter obj update-tank (get-world-objects))))
(defn update-world []
  (doseq [obj (get-world-objects)]
    (update-object obj)))

(defn render-world [g]
  (doseq [obj (get-world-objects)]
    (render-tank (deref obj) g)))

(defn apply-event-to-subscribers [e k]
  (doseq [{handler :handler obj :target} (get-world-event-subscribers e k)]
    (handler obj)))
(defn subscription-key [e]
  (.getKeyCode e))
(defn world-kp [ke]
  (apply-event-to-subscribers kp (subscription-key ke)))
(defn world-kr [ke]
  (apply-event-to-subscribers kr (subscription-key ke)))

(defn start []
  (init-world)
  (game
   :key-pressed-fn world-kp
   :key-released-fn world-kr
   :update-fn update-world
   :render-fn render-world
   :running-ref (atom true)))
