(ns tanks.core
  (:gen-class)
  (:use [engine.core :only (game)]
        [tanks.resources :only (read-from-file)]
        [tanks.movement :only (update-tank)]
        [tanks.rendering :only (render-tank)]))

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
(defn update []
  (doseq [obj (get-world-objects)]
    (update-object obj)))

(defn render [g]
  (doseq [obj (get-world-objects)]
    (render-tank (deref obj) g)))

(defn apply-event-to-subscribers [e k]
  (doseq [{handler :handler obj :target} (get-world-event-subscribers e k)]
    (handler obj)))
(defn key-pressed [ke]
  (apply-event-to-subscribers :key-pressed (.getKeyCode ke)))
(defn key-released [ke]
  (apply-event-to-subscribers :key-released (.getKeyCode ke)))

(defn start []
  (init-world)
  (game key-pressed key-released update render {:width 500 :length 400}))

(defn -main [& args]
  (start))
