(ns sada-abe.eric-normand
  (:require [clojure.core.async :as async :refer [<!!]]
            [detijd.units :refer [unit->ms]]))


(defn fn-throttle [rate unit burst]
  (let [delay (/ (unit->ms unit) rate)
        bucket (async/chan (async/dropping-buffer burst))]
    (async/go
      (loop []
        (async/put! bucket :token)
        (async/<! (async/timeout delay))
        (recur)))
    (fn [f]
      (fn [& args]
        (async/<!! bucket)
        (apply f args)))))

(defn throttle-fn
  ([f rate unit]
   (throttle-fn f rate unit 1))
  ([f rate unit burst]
   ((fn-throttle rate unit burst) f)))
